package com.airwise.backend.flightprovider.admission;

import static com.airwise.backend.flightprovider.admission.AdmissionModel.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

class AdmissionTests {
    private static final Instant NOW = Instant.parse("2026-11-19T12:00:00Z");
    private static final Operation INITIAL = new Operation("providerA", "sharedAccount", "search");
    private static final Operation RETURN = new Operation("providerA", "sharedAccount", "return");
    private static final Operation SELLER = new Operation("providerA", "sharedAccount", "seller");
    private static final Operation OTHER_PROVIDER = new Operation("providerB", "sharedAccount", "search");
    private static final Limits GENEROUS = new Limits(100, 100, 100, 100, 100);

    @Test
    void freshLedgerHasUnknownBudgetAndDefaultPolicyIsClosed() {
        Fixture fixture = fixture(GENEROUS);
        assertEquals(Reason.UNKNOWN_BUDGET, fixture.service.reserve(one(INITIAL, 1)).reason());
        var closed = new AdmissionService(new InMemoryAdmissionLedger(Policy.closed()), fixture.clock,
                Scope.SYNTHETIC_SINGLE_PROCESS);
        assertFalse(closed.reserve(one(INITIAL, 0)).admitted());
        assertEquals(Reason.UNKNOWN_BUDGET, fixture.service.reserve(one(INITIAL, 0)).reason(),
                "A zero-credit account operation still requires evidence and attempt admission");
    }

    @Test
    void evidenceMustBeSyntheticAndFreshWithKnownCurrentCommercialPeriod() {
        Fixture fixture = fixture(GENEROUS);
        for (Scope origin : Scope.values()) {
            if (origin != Scope.SYNTHETIC_SINGLE_PROCESS) {
                var real = new Evidence(INITIAL.commercialKey(), origin, 10, NOW, NOW.plusSeconds(60),
                        NOW.minusSeconds(60), NOW.plusSeconds(60));
                assertEquals(Reason.INVALID_EVIDENCE, fixture.service.recordEvidence(real).reason());
            }
        }
        assertEquals(Reason.INVALID_EVIDENCE, fixture.service.recordEvidence(evidence(INITIAL, 10,
                NOW.plusSeconds(1), NOW.plusSeconds(60), NOW.minusSeconds(60), NOW.plusSeconds(60))).reason());
        assertEquals(Reason.INVALID_EVIDENCE, fixture.service.recordEvidence(evidence(INITIAL, 10,
                NOW.minusSeconds(60), NOW, NOW.minusSeconds(60), NOW.plusSeconds(60))).reason());
        assertEquals(Reason.INVALID_EVIDENCE, fixture.service.recordEvidence(evidence(INITIAL, 10,
                NOW.minusSeconds(60), NOW.plusSeconds(60), NOW.minusSeconds(60), NOW)).reason());
        assertEquals(Reason.UNKNOWN_BUDGET, fixture.service.reserve(one(INITIAL, 1)).reason());
    }

    @Test
    void unsupportedScopesNeverEvenReadClockOrTouchLedger() {
        Fixture fixture = fixture(GENEROUS);
        Clock unusable = new Clock() {
            public ZoneId getZone() { throw new AssertionError(); }
            public Clock withZone(ZoneId zone) { throw new AssertionError(); }
            public Instant instant() { throw new AssertionError("Unsupported scope accessed clock"); }
        };
        for (Scope scope : Scope.values()) {
            if (scope == Scope.SYNTHETIC_SINGLE_PROCESS) continue;
            var blocked = new AdmissionService(fixture.ledger, unusable, scope);
            assertEquals(Reason.UNSUPPORTED_SCOPE, blocked.recordEvidence(validEvidence(INITIAL, 10)).reason());
            assertEquals(Reason.UNSUPPORTED_SCOPE, blocked.reserve(one(INITIAL, 1)).reason());
            assertEquals(Reason.UNSUPPORTED_SCOPE, blocked.start(null, 0).reason());
        }
        assertEquals(Reason.UNKNOWN_BUDGET, fixture.service.reserve(one(INITIAL, 1)).reason());
    }

    @Test
    void onlyKnownExactlyZeroMonetaryCostIsAdmissible() {
        Fixture fixture = ready(GENEROUS);
        for (BigDecimal cost : List.of(new BigDecimal("0.01"), new BigDecimal("-0.01"))) {
            assertEquals(Reason.NONZERO_COST,
                    fixture.service.reserve(new Plan(List.of(new Step(INITIAL, Phase.INITIAL, 1, cost)))).reason());
        }
        assertEquals(Reason.UNKNOWN_COST,
                fixture.service.reserve(new Plan(List.of(new Step(INITIAL, Phase.INITIAL, 1, null)))).reason());
        assertTrue(fixture.service.reserve(new Plan(List.of(new Step(INITIAL, Phase.INITIAL, 1,
                new BigDecimal("0.000"))))).admitted());
    }

    @Test
    void fullMaximumFlowIsReservedBeforeFirstSyntheticExecution() {
        Fixture fixture = ready(new Limits(2, 2, 2, 2, 10));
        Plan flow = new Plan(List.of(step(INITIAL, Phase.INITIAL, 1), step(RETURN, Phase.CONTINUATION, 1),
                step(SELLER, Phase.SELLER, 1)));
        AtomicInteger syntheticExecutions = new AtomicInteger();
        Decision<Reservation> denied = fixture.service.reserve(flow);
        if (denied.admitted() && fixture.service.start(denied.value(), 0).admitted()) syntheticExecutions.incrementAndGet();
        assertFalse(denied.admitted());
        assertEquals(0, syntheticExecutions.get());
        assertTrue(fixture.service.reserve(new Plan(flow.steps().subList(0, 2))).admitted(),
                "Failed all-or-nothing admission cannot leave a partial hold");
    }

    @Test
    void phasesAreOrderedAndEachStartIsOneUse() {
        Fixture fixture = ready(GENEROUS);
        var reservation = fixture.service.reserve(new Plan(List.of(step(INITIAL, Phase.INITIAL, 1),
                step(RETURN, Phase.CONTINUATION, 1), step(SELLER, Phase.SELLER, 1)))).value();
        assertEquals(Reason.INVALID_TRANSITION, fixture.service.start(reservation, 1).reason());
        for (int i = 0; i < 3; i++) {
            assertEquals(State.STARTED, fixture.service.start(reservation, i).value());
            assertEquals(Reason.INVALID_TRANSITION, fixture.service.start(reservation, i).reason());
            assertEquals(State.CONFIRMED, fixture.service.confirm(reservation, i, 1).value());
        }
        assertEquals(Reason.INVALID_HANDLE, fixture.service.start(reservation, 3).reason());
    }

    @Test
    void timeoutRetainsAttemptCreditAndConcurrencyUntilReliableReconciliation() {
        Fixture fixture = ready(new Limits(10, 10, 10, 10, 1));
        var reservation = fixture.service.reserve(one(INITIAL, 1)).value();
        fixture.service.start(reservation, 0);
        assertEquals(State.UNCERTAIN, fixture.service.uncertain(reservation, 0).value());
        assertEquals(Reason.INVALID_TRANSITION, fixture.service.cancel(reservation, 0).reason());
        assertEquals(Reason.INVALID_TRANSITION, fixture.service.start(reservation, 0).reason());
        assertEquals(Reason.LIMIT_EXCEEDED, fixture.service.reserve(one(INITIAL, 1)).reason());
        assertEquals(State.CONFIRMED, fixture.service.confirm(reservation, 0, 0).value());
        assertTrue(fixture.service.reserve(one(INITIAL, 1)).admitted());
    }

    @Test
    void reliableZeroCreditConfirmationNeverRefundsSentAttemptAndCannotBeRepeated() {
        Fixture fixture = ready(new Limits(1, 1, 10, 10, 10));
        var reservation = fixture.service.reserve(one(INITIAL, 1)).value();
        assertEquals(Reason.INVALID_TRANSITION, fixture.service.confirm(reservation, 0, 0).reason());
        fixture.service.start(reservation, 0);
        assertEquals(Reason.INVALID_TRANSITION, fixture.service.confirm(reservation, 0, -1).reason());
        assertEquals(Reason.INVALID_TRANSITION, fixture.service.confirm(reservation, 0, 2).reason());
        fixture.service.confirm(reservation, 0, 0);
        assertEquals(Reason.INVALID_TRANSITION, fixture.service.confirm(reservation, 0, 0).reason());
        assertEquals(Reason.INVALID_TRANSITION, fixture.service.uncertain(reservation, 0).reason());
        assertEquals(Reason.LIMIT_EXCEEDED, fixture.service.reserve(one(INITIAL, 0)).reason());
    }

    @Test
    void creditsAndAttemptsHaveIndependentLimitsIncludingZeroCreditOperations() {
        Fixture attempts = ready(new Limits(1, 1, 10, 10, 10));
        assertTrue(attempts.service.reserve(one(INITIAL, 0)).admitted());
        assertEquals(Reason.LIMIT_EXCEEDED, attempts.service.reserve(one(INITIAL, 0)).reason());
        Fixture credits = ready(new Limits(10, 10, 1, 1, 10));
        assertTrue(credits.service.reserve(one(INITIAL, 1)).admitted());
        assertEquals(Reason.LIMIT_EXCEEDED, credits.service.reserve(one(RETURN, 1)).reason());
    }

    @Test
    void onlyUnstartedCancellationReleasesItsReservation() {
        Fixture fixture = ready(new Limits(1, 1, 1, 1, 1));
        var reservation = fixture.service.reserve(one(INITIAL, 1)).value();
        assertEquals(State.CANCELLED, fixture.service.cancel(reservation, 0).value());
        assertEquals(Reason.INVALID_TRANSITION, fixture.service.cancel(reservation, 0).reason());
        assertEquals(Reason.INVALID_TRANSITION, fixture.service.start(reservation, 0).reason());
        assertTrue(fixture.service.reserve(one(INITIAL, 1)).admitted());
    }

    @Test
    void retryRequiresNewAdmissionAndCannotBeAnAutomaticLaterStep() {
        Fixture fixture = ready(new Limits(1, 1, 10, 10, 10));
        var reservation = fixture.service.reserve(one(INITIAL, 1)).value();
        fixture.service.start(reservation, 0);
        fixture.service.uncertain(reservation, 0);
        assertEquals(Reason.INVALID_TRANSITION, fixture.service.start(reservation, 0).reason());
        assertEquals(Reason.INVALID_PLAN, fixture.service.reserve(new Plan(List.of(step(INITIAL, Phase.INITIAL, 1),
                step(INITIAL, Phase.RETRY, 1)))).reason());
        assertEquals(Reason.LIMIT_EXCEEDED, fixture.service.reserve(new Plan(List.of(step(INITIAL, Phase.RETRY, 1)))).reason());
        Fixture available = ready(GENEROUS);
        var retry = available.service.reserve(new Plan(List.of(step(INITIAL, Phase.RETRY, 1))));
        assertTrue(retry.admitted());
        assertTrue(available.service.start(retry.value(), 0).admitted());
    }

    @Test
    void forgedForeignAndRestartedHandlesCannotStartOrSettle() {
        Fixture first = ready(GENEROUS);
        Fixture restarted = ready(GENEROUS);
        var reservation = first.service.reserve(one(INITIAL, 1)).value();
        assertEquals(Reason.INVALID_HANDLE, first.service.start(newReservation(), 0).reason());
        assertEquals(Reason.INVALID_HANDLE, restarted.service.start(reservation, 0).reason());
        assertEquals(Reason.INVALID_HANDLE, restarted.service.confirm(reservation, 0, 0).reason());
        assertEquals(Reason.INVALID_HANDLE, first.service.start(reservation, -1).reason());
        assertEquals(Reason.INVALID_HANDLE, first.service.start(null, 0).reason());
        var recovery = new AdmissionService(restarted.ledger, restarted.clock, Scope.RESTART_RECOVERY);
        assertEquals(Reason.UNSUPPORTED_SCOPE, recovery.reserve(one(INITIAL, 1)).reason());
    }

    @Test
    void recreatedLedgerStartsUnknownAndIndependentLedgersSupplyNoSharedGuarantee() {
        Fixture first = ready(new Limits(1, 1, 1, 1, 1));
        assertTrue(first.service.reserve(one(INITIAL, 1)).admitted());
        Fixture independent = fixture(new Limits(1, 1, 1, 1, 1));
        assertEquals(Reason.UNKNOWN_BUDGET, independent.service.reserve(one(INITIAL, 1)).reason());
        independent.service.recordEvidence(validEvidence(INITIAL, 100));
        assertTrue(independent.service.reserve(one(INITIAL, 1)).admitted(),
                "Independent synthetic ledgers do not coordinate; their combined use is unsupported");
        var unsupported = new AdmissionService(independent.ledger, independent.clock, Scope.MULTIPLE_LEDGERS);
        assertEquals(Reason.UNSUPPORTED_SCOPE, unsupported.reserve(one(INITIAL, 0)).reason());
    }

    @Test
    void newBalanceSampleCannotResetLocalCreditDebt() {
        Fixture fixture = ready(GENEROUS);
        fixture.service.recordEvidence(validEvidence(INITIAL, 1));
        var reservation = fixture.service.reserve(one(INITIAL, 1)).value();
        fixture.service.start(reservation, 0);
        fixture.service.confirm(reservation, 0, 1);
        fixture.service.recordEvidence(validEvidence(INITIAL, 1));
        assertEquals(Reason.COMMERCIAL_QUOTA_EXHAUSTED, fixture.service.reserve(one(INITIAL, 1)).reason());
    }

    @Test
    void unknownEvidenceForOneAccountRejectsEntireMultiAccountFlow() {
        Fixture fixture = ready(GENEROUS);
        assertEquals(Reason.UNKNOWN_BUDGET, fixture.service.reserve(new Plan(List.of(step(INITIAL, Phase.INITIAL, 1),
                step(OTHER_PROVIDER, Phase.INITIAL, 1)))).reason());
        fixture.service.recordEvidence(validEvidence(OTHER_PROVIDER, 100));
        assertTrue(fixture.service.reserve(one(INITIAL, 100)).admitted(), "No partial debit from failed flow");
    }

    @Test
    void expiredBalanceBlocksStartAndPreservesReservation() {
        Fixture fixture = fixture(GENEROUS);
        fixture.service.recordEvidence(evidence(INITIAL, 10, NOW, NOW.plusSeconds(1),
                NOW.minusSeconds(60), NOW.plusSeconds(60)));
        var reservation = fixture.service.reserve(one(INITIAL, 1)).value();
        fixture.clock.now = NOW.plusSeconds(1);
        assertEquals(Reason.UNKNOWN_BUDGET, fixture.service.start(reservation, 0).reason());
        assertEquals(State.RESERVED, fixture.service.state(reservation, 0).value());
        assertEquals(State.CANCELLED, fixture.service.cancel(reservation, 0).value());
    }

    @Test
    void lowerBalanceRevalidationCanBlockPreviouslyReservedStart() {
        Fixture fixture = ready(GENEROUS);
        var reservation = fixture.service.reserve(one(INITIAL, 1)).value();
        fixture.service.recordEvidence(validEvidence(INITIAL, 0));
        assertEquals(Reason.COMMERCIAL_QUOTA_EXHAUSTED, fixture.service.start(reservation, 0).reason());
        assertEquals(State.RESERVED, fixture.service.state(reservation, 0).value());
    }

    @Test
    void backwardsClockIrreversiblyClosesSharedLedger() {
        Fixture fixture = ready(GENEROUS);
        var second = new AdmissionService(fixture.ledger, fixture.clock, Scope.SYNTHETIC_SINGLE_PROCESS);
        fixture.clock.now = NOW.minusNanos(1);
        assertEquals(Reason.CLOCK_UNRELIABLE, second.reserve(one(INITIAL, 1)).reason());
        fixture.clock.now = NOW.plusSeconds(60);
        assertEquals(Reason.CLOCK_UNRELIABLE, fixture.service.recordEvidence(validEvidence(INITIAL, 100)).reason());
        assertEquals(Reason.CLOCK_UNRELIABLE, fixture.service.reserve(one(INITIAL, 1)).reason());
    }

    @Test
    void utcDayResetsConfirmedDailyUsageButNotMonthlyUsage() {
        Fixture fixture = ready(new Limits(1, 2, 100, 100, 100));
        complete(fixture, 0);
        fixture.clock.now = Instant.parse("2026-11-20T00:00:00Z");
        assertTrue(fixture.service.reserve(one(INITIAL, 0)).admitted());
        assertEquals(Reason.LIMIT_EXCEEDED, fixture.service.reserve(one(INITIAL, 0)).reason());
        fixture.clock.now = Instant.parse("2026-11-21T00:00:00Z");
        assertEquals(Reason.LIMIT_EXCEEDED, fixture.service.reserve(one(INITIAL, 0)).reason(),
                "One completed and one outstanding attempt exhaust the month");
    }

    @Test
    void utcCalendarMonthResetsConfirmedMonthlyUsageIndependentlyOfCommercialPeriod() {
        Fixture fixture = ready(new Limits(1, 1, 100, 100, 100));
        complete(fixture, 0);
        fixture.clock.now = Instant.parse("2026-12-01T00:00:00Z");
        assertTrue(fixture.service.reserve(one(INITIAL, 0)).admitted());
    }

    @Test
    void uncertaintyCarriesAcrossUtcDayAndMonthAndCannotBeCancelled() {
        Fixture fixture = ready(new Limits(1, 1, 1, 1, 100));
        var reservation = fixture.service.reserve(one(INITIAL, 1)).value();
        fixture.service.start(reservation, 0);
        fixture.service.uncertain(reservation, 0);
        fixture.clock.now = Instant.parse("2026-12-01T00:00:00Z");
        assertEquals(Reason.LIMIT_EXCEEDED, fixture.service.reserve(one(INITIAL, 1)).reason());
        assertEquals(Reason.INVALID_TRANSITION, fixture.service.cancel(reservation, 0).reason());
        assertEquals(State.UNCERTAIN, fixture.service.state(reservation, 0).value());
    }

    @Test
    void reservationCannotStartAfterUtcWindowChangesAndHoldRemains() {
        Fixture fixture = ready(new Limits(1, 1, 1, 1, 100));
        var reservation = fixture.service.reserve(one(INITIAL, 1)).value();
        fixture.clock.now = Instant.parse("2026-11-20T00:00:00Z");
        assertEquals(Reason.WINDOW_CHANGED, fixture.service.start(reservation, 0).reason());
        assertEquals(Reason.LIMIT_EXCEEDED, fixture.service.reserve(one(INITIAL, 1)).reason());
        fixture.service.cancel(reservation, 0);
        assertTrue(fixture.service.reserve(one(INITIAL, 1)).admitted());
    }

    @Test
    void commercialRenewalWithinSameUtcDayCannotRevalidateOldPermissionOrEraseUncertainty() {
        Fixture fixture = fixture(GENEROUS);
        Instant renewal = NOW.plusSeconds(60);
        fixture.service.recordEvidence(evidence(INITIAL, 2, NOW, renewal.plusSeconds(60), NOW.minusSeconds(60), renewal));
        var uncertain = fixture.service.reserve(one(INITIAL, 1)).value();
        fixture.service.start(uncertain, 0);
        fixture.service.uncertain(uncertain, 0);
        var pending = fixture.service.reserve(one(INITIAL, 1)).value();
        fixture.clock.now = renewal;
        assertEquals(Reason.UNKNOWN_BUDGET, fixture.service.start(pending, 0).reason());
        fixture.service.recordEvidence(evidence(INITIAL, 2, renewal, renewal.plusSeconds(60), renewal, renewal.plusSeconds(60)));
        assertEquals(Reason.WINDOW_CHANGED, fixture.service.start(pending, 0).reason());
        assertEquals(Reason.COMMERCIAL_QUOTA_EXHAUSTED, fixture.service.reserve(one(INITIAL, 1)).reason());
        fixture.service.cancel(pending, 0);
        assertTrue(fixture.service.reserve(one(INITIAL, 1)).admitted(), "Only explicit unstarted cancellation releases its hold");
    }

    @Test
    void evidenceCannotMoveCommercialPeriodBackwardsOrChangeExistingBoundary() {
        Fixture fixture = ready(GENEROUS);
        Evidence sample = validEvidence(INITIAL, 100);
        assertEquals(Reason.INVALID_EVIDENCE, fixture.service.recordEvidence(evidence(INITIAL, 100,
                NOW.minusSeconds(1), sample.expiresAt(), sample.periodStart(), sample.periodEnd())).reason());
        assertEquals(Reason.INVALID_EVIDENCE, fixture.service.recordEvidence(evidence(INITIAL, 100,
                NOW, sample.expiresAt(), sample.periodStart(), sample.periodEnd().plusSeconds(1))).reason());
    }

    @Test
    void twoServicesShareLastOperationUnitAtomically() throws Exception {
        raceLastUnit(INITIAL, INITIAL, INITIAL.budgets().getFirst());
    }

    @Test
    void twoDifferentOperationsShareLastProviderUnitAtomically() throws Exception {
        raceLastUnit(INITIAL, RETURN, INITIAL.budgets().get(1));
    }

    @Test
    void twoDifferentProvidersShareExplicitAccountAttemptLimitAtomically() throws Exception {
        raceLastUnit(INITIAL, OTHER_PROVIDER, INITIAL.budgets().get(2));
    }

    @Test
    void maximumFlowAndGlobalConcurrencyLimitsAlsoApply() {
        var map = configured(GENEROUS);
        Fixture flow = fixture(new Policy(map, 100, 1, 1));
        flow.service.recordEvidence(validEvidence(INITIAL, 100));
        assertEquals(Reason.INVALID_PLAN, flow.service.reserve(new Plan(List.of(step(INITIAL, Phase.INITIAL, 0),
                step(RETURN, Phase.CONTINUATION, 0)))).reason());
        assertEquals(Reason.LIMIT_EXCEEDED, flow.service.reserve(one(INITIAL, 2)).reason());
        Fixture global = fixture(new Policy(map, 1, 256, Long.MAX_VALUE));
        global.service.recordEvidence(validEvidence(INITIAL, 100));
        assertTrue(global.service.reserve(one(INITIAL, 0)).admitted());
        assertEquals(Reason.LIMIT_EXCEEDED, global.service.reserve(one(RETURN, 0)).reason());
    }

    @Test
    void racingCancellationAndStartCannotBothSucceedOrReleaseASentAttempt() throws Exception {
        Fixture fixture = ready(new Limits(1, 1, 1, 1, 100));
        var reservation = fixture.service.reserve(one(INITIAL, 1)).value();
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch release = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(2)) {
            var start = pool.submit(() -> {
                ready.countDown();
                if (!release.await(5, TimeUnit.SECONDS)) throw new AssertionError("Race not released");
                return fixture.service.start(reservation, 0);
            });
            var cancel = pool.submit(() -> {
                ready.countDown();
                if (!release.await(5, TimeUnit.SECONDS)) throw new AssertionError("Race not released");
                return fixture.service.cancel(reservation, 0);
            });
            assertTrue(ready.await(5, TimeUnit.SECONDS));
            release.countDown();
            var started = start.get(5, TimeUnit.SECONDS);
            var cancelled = cancel.get(5, TimeUnit.SECONDS);
            assertNotEquals(started.admitted(), cancelled.admitted());
            if (started.admitted()) {
                assertEquals(State.STARTED, fixture.service.state(reservation, 0).value());
                assertEquals(Reason.LIMIT_EXCEEDED, fixture.service.reserve(one(INITIAL, 1)).reason());
            } else {
                assertEquals(State.CANCELLED, fixture.service.state(reservation, 0).value());
                assertTrue(fixture.service.reserve(one(INITIAL, 1)).admitted());
            }
        } finally { release.countDown(); }
    }

    @Test
    void arithmeticOverflowDeniesWithoutPartialReservation() {
        Fixture fixture = ready(new Limits(Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE, 100));
        assertEquals(Reason.INVALID_PLAN, fixture.service.reserve(new Plan(List.of(
                step(INITIAL, Phase.INITIAL, Long.MAX_VALUE), step(RETURN, Phase.CONTINUATION, 1)))).reason());
        assertTrue(fixture.service.reserve(one(INITIAL, 1)).admitted());
    }

    private void raceLastUnit(Operation first, Operation second, BudgetKey limited) throws Exception {
        var map = configured(GENEROUS);
        map.put(limited, new Limits(1, 1, 100, 100, 100));
        Fixture fixture = fixture(new Policy(map, 100, 256, Long.MAX_VALUE));
        fixture.service.recordEvidence(validEvidence(INITIAL, 100));
        fixture.service.recordEvidence(validEvidence(OTHER_PROVIDER, 100));
        var secondService = new AdmissionService(fixture.ledger, fixture.clock, Scope.SYNTHETIC_SINGLE_PROCESS);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch release = new CountDownLatch(1);
        AtomicInteger executions = new AtomicInteger();
        try (var pool = Executors.newFixedThreadPool(2)) {
            var tasks = List.of(pool.submit(() -> raceAttempt(fixture.service, first, ready, release, executions)),
                    pool.submit(() -> raceAttempt(secondService, second, ready, release, executions)));
            assertTrue(ready.await(5, TimeUnit.SECONDS));
            release.countDown();
            int admitted = 0;
            for (var task : tasks) if (task.get(5, TimeUnit.SECONDS)) admitted++;
            assertEquals(1, admitted);
            assertEquals(1, executions.get(), "Synthetic marker occurs only after successful one-use start");
        } finally { release.countDown(); }
    }

    private boolean raceAttempt(AdmissionService service, Operation operation, CountDownLatch ready,
            CountDownLatch release, AtomicInteger executions) throws InterruptedException {
        ready.countDown();
        if (!release.await(5, TimeUnit.SECONDS)) throw new AssertionError("Race not released");
        var reservation = service.reserve(one(operation, 1));
        if (!reservation.admitted()) return false;
        if (service.start(reservation.value(), 0).admitted()) executions.incrementAndGet();
        return true;
    }

    private static void complete(Fixture fixture, long credits) {
        var reservation = fixture.service.reserve(one(INITIAL, credits)).value();
        assertTrue(fixture.service.start(reservation, 0).admitted());
        assertTrue(fixture.service.confirm(reservation, 0, credits).admitted());
    }
    static Plan one(Operation operation, long credits) { return new Plan(List.of(step(operation, Phase.INITIAL, credits))); }
    static Step step(Operation operation, Phase phase, long credits) { return new Step(operation, phase, credits, BigDecimal.ZERO); }
    static Evidence validEvidence(Operation operation, long remaining) {
        return evidence(operation, remaining, NOW, Instant.parse("2027-01-01T00:00:00Z"),
                Instant.parse("2026-11-01T00:00:00Z"), Instant.parse("2027-01-01T00:00:00Z"));
    }
    private static Evidence evidence(Operation operation, long remaining, Instant observed, Instant expiry, Instant start, Instant end) {
        return new Evidence(operation.commercialKey(), Scope.SYNTHETIC_SINGLE_PROCESS, remaining, observed, expiry, start, end);
    }
    private static HashMap<BudgetKey, Limits> configured(Limits limits) {
        var map = new HashMap<BudgetKey, Limits>();
        for (var operation : List.of(INITIAL, RETURN, SELLER, OTHER_PROVIDER))
            for (var key : operation.budgets()) map.put(key, limits);
        return map;
    }
    private static Fixture fixture(Limits limits) {
        return fixture(new Policy(configured(limits), 100, 256, Long.MAX_VALUE));
    }
    private static Fixture fixture(Policy policy) {
        var clock = new MutableClock(NOW);
        var ledger = new InMemoryAdmissionLedger(policy);
        return new Fixture(ledger, clock, new AdmissionService(ledger, clock, Scope.SYNTHETIC_SINGLE_PROCESS));
    }
    private static Fixture ready(Limits limits) {
        Fixture fixture = fixture(limits);
        assertTrue(fixture.service.recordEvidence(validEvidence(INITIAL, 100)).admitted());
        return fixture;
    }
    private record Fixture(InMemoryAdmissionLedger ledger, MutableClock clock, AdmissionService service) { }
    static final class MutableClock extends Clock {
        volatile Instant now;
        MutableClock(Instant now) { this.now = now; }
        public ZoneId getZone() { return ZoneOffset.UTC; }
        public Clock withZone(ZoneId zone) { return this; }
        public Instant instant() { return now; }
    }
}
