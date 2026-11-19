package com.airwise.backend.flightprovider.admission;

import static com.airwise.backend.flightprovider.admission.AdmissionModel.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

class AdmissionFailureTests {
    private static final Operation OPERATION = new Operation("synthetic", "account", "search");
    private static final Instant NOW = Instant.parse("2026-11-19T12:00:00Z");

    @Test
    void failureBeforeReservationNeverGrantsExecutionAndClosesSharedLedger() {
        var port = new FailingLedger(memory());
        var service = service(port);
        var secondService = service(port);
        port.failReserve = true;
        assertEquals(Reason.LEDGER_UNAVAILABLE, service.reserve(AdmissionTests.one(OPERATION, 1)).reason());
        assertEquals(0, port.starts.get());
        assertEquals(Reason.LEDGER_UNAVAILABLE, secondService.reserve(AdmissionTests.one(OPERATION, 1)).reason());
        assertEquals(Reason.LEDGER_UNAVAILABLE, service.start(null, 0).reason());
    }

    @Test
    void failureAfterStartPoisonsBeforeThrowAndNeverReplaysOrRefunds() {
        var port = new FailingLedger(memory());
        var service = service(port);
        var reservation = service.reserve(AdmissionTests.one(OPERATION, 1)).value();
        port.failStartAfterMutation = true;
        assertEquals(Reason.LEDGER_UNAVAILABLE, service.start(reservation, 0).reason());
        assertEquals(1, port.starts.get());
        var secondService = service(port);
        assertEquals(Reason.LEDGER_UNAVAILABLE, secondService.start(reservation, 0).reason());
        assertEquals(Reason.LEDGER_UNAVAILABLE, secondService.confirm(reservation, 0, 0).reason());
        assertEquals(Reason.LEDGER_UNAVAILABLE, secondService.cancel(reservation, 0).reason());
        assertEquals(1, port.starts.get());
    }

    @Test
    void failureDuringReconciliationCannotBeRetriedOrUsedToRestoreBudget() {
        var port = new FailingLedger(memory());
        var service = service(port);
        var reservation = service.reserve(AdmissionTests.one(OPERATION, 1)).value();
        service.start(reservation, 0);
        port.failConfirmAfterMutation = true;
        assertEquals(Reason.LEDGER_UNAVAILABLE, service.confirm(reservation, 0, 0).reason());
        assertEquals(1, port.confirmations.get());
        assertEquals(Reason.LEDGER_UNAVAILABLE, service.confirm(reservation, 0, 0).reason());
        assertEquals(1, port.confirmations.get());
        assertEquals(Reason.LEDGER_UNAVAILABLE, service(port).reserve(AdmissionTests.one(OPERATION, 1)).reason());
    }

    @Test
    void closeFailureStillLeavesServiceIrreversiblyClosedAndErrorsAreSanitized() {
        var port = new FailingLedger(memory());
        var service = service(port);
        port.failReserve = true;
        port.failClose = true;
        var decision = service.reserve(AdmissionTests.one(OPERATION, 1));
        assertEquals("Decision[reason=LEDGER_UNAVAILABLE, value=null]", decision.toString());
        assertEquals(Reason.LEDGER_UNAVAILABLE, service.reserve(AdmissionTests.one(OPERATION, 1)).reason());
        assertEquals(1, port.reservations.get());
    }

    @Test
    void clockFailureCannotProduceSyntheticGrant() {
        var port = new FailingLedger(memory());
        Clock broken = new Clock() {
            public ZoneId getZone() { return ZoneOffset.UTC; }
            public Clock withZone(ZoneId zone) { return this; }
            public Instant instant() { throw new IllegalStateException("Synthetic private clock diagnostic"); }
        };
        var service = new AdmissionService(port, broken, Scope.SYNTHETIC_SINGLE_PROCESS);
        assertEquals(Reason.LEDGER_UNAVAILABLE, service.reserve(AdmissionTests.one(OPERATION, 1)).reason());
        assertEquals(0, port.reservations.get());
    }

    @Test
    void twoServicesSampleClockInsideSameLedgerMonitor() throws Exception {
        var ledger = memory();
        CountDownLatch firstSampled = new CountDownLatch(1);
        CountDownLatch allowFirstToProceed = new CountDownLatch(1);
        CountDownLatch secondCalling = new CountDownLatch(1);
        CountDownLatch secondSampled = new CountDownLatch(1);
        AtomicInteger calls = new AtomicInteger();
        Clock clock = new Clock() {
            public ZoneId getZone() { return ZoneOffset.UTC; }
            public Clock withZone(ZoneId zone) { return this; }
            public Instant instant() {
                int call = calls.incrementAndGet();
                if (call == 1) {
                    firstSampled.countDown();
                    try {
                        if (!allowFirstToProceed.await(5, TimeUnit.SECONDS)) throw new AssertionError("Clock not released");
                    } catch (InterruptedException interrupted) {
                        Thread.currentThread().interrupt();
                        throw new AssertionError(interrupted);
                    }
                } else secondSampled.countDown();
                return NOW.plusNanos(call);
            }
        };
        var first = new AdmissionService(ledger, clock, Scope.SYNTHETIC_SINGLE_PROCESS);
        var second = new AdmissionService(ledger, clock, Scope.SYNTHETIC_SINGLE_PROCESS);
        try (var pool = Executors.newFixedThreadPool(2)) {
            var a = pool.submit(() -> first.reserve(AdmissionTests.one(OPERATION, 0)));
            assertTrue(firstSampled.await(5, TimeUnit.SECONDS));
            var b = pool.submit(() -> {
                secondCalling.countDown();
                return second.reserve(AdmissionTests.one(OPERATION, 0));
            });
            assertTrue(secondCalling.await(5, TimeUnit.SECONDS));
            assertFalse(secondSampled.await(100, TimeUnit.MILLISECONDS),
                    "The second service must not sample a newer clock value outside the shared monitor");
            allowFirstToProceed.countDown();
            assertTrue(a.get(5, TimeUnit.SECONDS).admitted());
            assertTrue(b.get(5, TimeUnit.SECONDS).admitted());
        } finally { allowFirstToProceed.countDown(); }
    }

    private static InMemoryAdmissionLedger memory() {
        var map = new HashMap<BudgetKey, Limits>();
        OPERATION.budgets().forEach(key -> map.put(key, new Limits(100, 100, 100, 100, 100)));
        var ledger = new InMemoryAdmissionLedger(new Policy(map, 100, 256, 100));
        assertTrue(ledger.recordEvidence(AdmissionTests.validEvidence(OPERATION, 100), NOW).admitted());
        return ledger;
    }
    private static AdmissionService service(AdmissionLedger ledger) {
        return new AdmissionService(ledger, Clock.fixed(NOW, ZoneOffset.UTC), Scope.SYNTHETIC_SINGLE_PROCESS);
    }

    /** A compliant synthetic failure double poisons its shared state BEFORE releasing its monitor. */
    private static final class FailingLedger implements AdmissionLedger {
        final AdmissionLedger delegate;
        final AtomicInteger starts = new AtomicInteger();
        final AtomicInteger confirmations = new AtomicInteger();
        final AtomicInteger reservations = new AtomicInteger();
        boolean failReserve;
        boolean failStartAfterMutation;
        boolean failConfirmAfterMutation;
        boolean failClose;
        FailingLedger(AdmissionLedger delegate) { this.delegate = delegate; }
        public synchronized Decision<Void> recordEvidence(Evidence evidence, Instant now) { return delegate.recordEvidence(evidence, now); }
        public synchronized Decision<Reservation> reserve(Plan plan, Instant now) {
            reservations.incrementAndGet();
            if (failReserve) { delegate.close(); throw failure(); }
            return delegate.reserve(plan, now);
        }
        public synchronized Decision<State> start(Reservation handle, int step, Instant now) {
            var result = delegate.start(handle, step, now);
            if (result.admitted()) {
                starts.incrementAndGet();
                if (failStartAfterMutation) { delegate.close(); throw failure(); }
            }
            return result;
        }
        public synchronized Decision<State> uncertain(Reservation handle, int step, Instant now) { return delegate.uncertain(handle, step, now); }
        public synchronized Decision<State> confirm(Reservation handle, int step, long credits, Instant now) {
            var result = delegate.confirm(handle, step, credits, now);
            if (result.admitted()) {
                confirmations.incrementAndGet();
                if (failConfirmAfterMutation) { delegate.close(); throw failure(); }
            }
            return result;
        }
        public synchronized Decision<State> cancel(Reservation handle, int step, Instant now) { return delegate.cancel(handle, step, now); }
        public synchronized Decision<State> state(Reservation handle, int step, Instant now) { return delegate.state(handle, step, now); }
        public synchronized void close() {
            delegate.close();
            if (failClose) throw failure();
        }
        private RuntimeException failure() { return new IllegalStateException("Synthetic private ledger diagnostic"); }
    }
}
