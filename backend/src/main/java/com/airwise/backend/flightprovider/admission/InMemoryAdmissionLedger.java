package com.airwise.backend.flightprovider.admission;

import static com.airwise.backend.flightprovider.admission.AdmissionModel.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * SYNTHETIC_SINGLE_PROCESS only. Two services coordinate only when sharing THIS instance.
 * Independent ledgers, restart, recovery and multiple processes have no shared protection.
 * There is deliberately no snapshot import, reset, real evidence bootstrap or durable claim.
 * Synchronized operations reserve a complete flow atomically before any synthetic start.
 */
public final class InMemoryAdmissionLedger implements AdmissionLedger {
    private static final int MAX_RESERVATIONS = 10_000;
    private final Policy policy;
    private final Map<CommercialKey, Evidence> evidence = new HashMap<>();
    private final Map<Reservation, Flow> flows = new IdentityHashMap<>();
    private Instant lastSeen;
    private Reason closed;

    public InMemoryAdmissionLedger(Policy policy) { this.policy = java.util.Objects.requireNonNull(policy); }

    @Override
    public synchronized Decision<Void> recordEvidence(Evidence sample, Instant now) {
        return run(now, () -> {
            if (sample == null || !sample.validAt(now)) return Decision.deny(Reason.INVALID_EVIDENCE);
            Evidence old = evidence.get(sample.key());
            if (old != null && (sample.observedAt().isBefore(old.observedAt())
                    || sample.periodStart().isBefore(old.periodStart())
                    || (sample.periodStart().equals(old.periodStart()) && !sample.periodEnd().equals(old.periodEnd()))
                    || (sample.periodStart().isAfter(old.periodStart()) && sample.periodStart().isBefore(old.periodEnd()))))
                return Decision.deny(Reason.INVALID_EVIDENCE);
            // Re-observation never erases any committed or uncertain local debit.
            evidence.put(sample.key(), sample);
            return Decision.allow(null);
        });
    }

    @Override
    public synchronized Decision<Reservation> reserve(Plan plan, Instant now) {
        return run(now, () -> {
            Reason invalid = validate(plan);
            if (invalid != Reason.NONE) return Decision.deny(invalid);
            if (flows.size() >= MAX_RESERVATIONS) return Decision.deny(Reason.LIMIT_EXCEEDED);
            try {
                Set<BudgetKey> keys = keys(plan.steps());
                if (activeFlows(null) >= policy.globalConcurrentFlows()) return Decision.deny(Reason.LIMIT_EXCEEDED);
                for (BudgetKey key : keys) {
                    Limits limit = policy.limits().getOrDefault(key, Limits.closed());
                    if (activeFlows(key) >= limit.concurrentFlows()) return Decision.deny(Reason.LIMIT_EXCEEDED);
                    long[] usage = usage(key, now);
                    for (Step step : plan.steps()) {
                        if (step.operation().budgets().contains(key)) {
                            usage[0] = Math.addExact(usage[0], 1);
                            usage[1] = Math.addExact(usage[1], 1);
                            usage[2] = Math.addExact(usage[2], step.potentialCredits());
                            usage[3] = Math.addExact(usage[3], step.potentialCredits());
                        }
                    }
                    if (usage[0] > limit.dailyAttempts() || usage[1] > limit.monthlyAttempts()
                            || usage[2] > limit.dailyCredits() || usage[3] > limit.monthlyCredits())
                        return Decision.deny(Reason.LIMIT_EXCEEDED);
                }
                Map<CommercialKey, Long> requested = new HashMap<>();
                for (Step step : plan.steps()) requested.merge(step.operation().commercialKey(), step.potentialCredits(), Math::addExact);
                for (var request : requested.entrySet()) {
                    Evidence sample = evidence.get(request.getKey());
                    if (sample == null || !sample.validAt(now)) return Decision.deny(Reason.UNKNOWN_BUDGET);
                    if (Math.addExact(commercialDebit(request.getKey()), request.getValue()) > sample.remainingCredits())
                        return Decision.deny(Reason.COMMERCIAL_QUOTA_EXHAUSTED);
                }
                Reservation handle = newReservation();
                flows.put(handle, new Flow(plan, now, evidence));
                return Decision.allow(handle);
            } catch (ArithmeticException overflow) {
                return Decision.deny(Reason.LIMIT_EXCEEDED);
            }
        });
    }

    @Override
    public synchronized Decision<State> start(Reservation handle, int index, Instant now) {
        return run(now, () -> {
            Row row = row(handle, index);
            if (row == null) return Decision.deny(Reason.INVALID_HANDLE);
            Flow flow = flows.get(handle);
            if (row.state != State.RESERVED) return Decision.deny(Reason.INVALID_TRANSITION);
            for (int i = 0; i < index; i++) {
                if (flow.rows.get(i).state != State.CONFIRMED) return Decision.deny(Reason.INVALID_TRANSITION);
            }
            if (!day(now).equals(day(flow.reservedAt))) return Decision.deny(Reason.WINDOW_CHANGED);
            CommercialKey key = row.step.operation().commercialKey();
            Evidence sample = evidence.get(key);
            if (sample == null || !sample.validAt(now)) return Decision.deny(Reason.UNKNOWN_BUDGET);
            Evidence original = flow.periods.get(key);
            if (!sample.periodStart().equals(original.periodStart()) || !sample.periodEnd().equals(original.periodEnd()))
                return Decision.deny(Reason.WINDOW_CHANGED);
            if (commercialDebit(key) > sample.remainingCredits()) return Decision.deny(Reason.COMMERCIAL_QUOTA_EXHAUSTED);
            // Mark irrevocable attempted execution BEFORE returning a synthetic grant.
            row.state = State.STARTED;
            return Decision.allow(State.STARTED);
        });
    }

    @Override
    public synchronized Decision<State> uncertain(Reservation handle, int index, Instant now) {
        return run(now, () -> {
            Row row = row(handle, index);
            if (row == null) return Decision.deny(Reason.INVALID_HANDLE);
            if (row.state != State.STARTED) return Decision.deny(Reason.INVALID_TRANSITION);
            row.state = State.UNCERTAIN;
            return Decision.allow(State.UNCERTAIN);
        });
    }

    @Override
    public synchronized Decision<State> confirm(Reservation handle, int index, long consumedCredits, Instant now) {
        return run(now, () -> {
            Row row = row(handle, index);
            if (row == null) return Decision.deny(Reason.INVALID_HANDLE);
            if ((row.state != State.STARTED && row.state != State.UNCERTAIN)
                    || consumedCredits < 0 || consumedCredits > row.step.potentialCredits())
                return Decision.deny(Reason.INVALID_TRANSITION);
            row.confirmedCredits = consumedCredits;
            row.state = State.CONFIRMED;
            return Decision.allow(State.CONFIRMED);
        });
    }

    @Override
    public synchronized Decision<State> cancel(Reservation handle, int index, Instant now) {
        return run(now, () -> {
            Row row = row(handle, index);
            if (row == null) return Decision.deny(Reason.INVALID_HANDLE);
            if (row.state != State.RESERVED) return Decision.deny(Reason.INVALID_TRANSITION);
            row.state = State.CANCELLED;
            return Decision.allow(State.CANCELLED);
        });
    }

    @Override
    public synchronized Decision<State> state(Reservation handle, int index, Instant now) {
        return run(now, () -> {
            Row row = row(handle, index);
            return row == null ? Decision.deny(Reason.INVALID_HANDLE) : Decision.allow(row.state);
        });
    }

    @Override
    public synchronized void close() { if (closed == null) closed = Reason.LEDGER_UNAVAILABLE; }

    private <T> Decision<T> run(Instant now, Supplier<Decision<T>> action) {
        if (closed != null) return Decision.deny(closed);
        if (now == null || (lastSeen != null && now.isBefore(lastSeen))) {
            closed = Reason.CLOCK_UNRELIABLE;
            return Decision.deny(closed);
        }
        lastSeen = now;
        try {
            return action.get();
        } catch (RuntimeException failure) {
            // Still holding the common monitor: poison before another service can observe state.
            closed = Reason.LEDGER_UNAVAILABLE;
            return Decision.deny(closed);
        }
    }

    private Reason validate(Plan plan) {
        if (plan == null || plan.steps().isEmpty() || plan.steps().size() > 256
                || plan.steps().size() > policy.maxAttemptsPerFlow()) return Reason.INVALID_PLAN;
        if (plan.steps().size() != 1 && plan.steps().stream().anyMatch(step -> step.phase() == Phase.RETRY))
            return Reason.INVALID_PLAN;
        long credits = 0;
        try {
            for (Step step : plan.steps()) {
                if (step.monetaryCost() == null) return Reason.UNKNOWN_COST;
                if (step.monetaryCost().signum() != 0) return Reason.NONZERO_COST;
                credits = Math.addExact(credits, step.potentialCredits());
            }
        } catch (ArithmeticException overflow) { return Reason.INVALID_PLAN; }
        return credits <= policy.maxCreditsPerFlow() ? Reason.NONE : Reason.LIMIT_EXCEEDED;
    }

    private Row row(Reservation handle, int index) {
        Flow flow = flows.get(handle);
        return flow == null || index < 0 || index >= flow.rows.size() ? null : flow.rows.get(index);
    }
    private int activeFlows(BudgetKey key) {
        int count = 0;
        for (Flow flow : flows.values()) {
            if ((key == null || flow.keys.contains(key)) && flow.rows.stream().anyMatch(Row::outstanding)) count++;
        }
        return count;
    }
    private long[] usage(BudgetKey key, Instant now) {
        long[] usage = new long[4];
        for (Flow flow : flows.values()) {
            for (Row row : flow.rows) {
                if (row.state == State.CANCELLED || !row.step.operation().budgets().contains(key)) continue;
                if (row.outstanding() || day(now).equals(day(flow.reservedAt))) {
                    usage[0] = Math.addExact(usage[0], 1);
                    usage[2] = Math.addExact(usage[2], row.debit());
                }
                if (row.outstanding() || month(now).equals(month(flow.reservedAt))) {
                    usage[1] = Math.addExact(usage[1], 1);
                    usage[3] = Math.addExact(usage[3], row.debit());
                }
            }
        }
        return usage;
    }
    private long commercialDebit(CommercialKey key) {
        long debit = 0;
        for (Flow flow : flows.values()) for (Row row : flow.rows) {
            if (row.step.operation().commercialKey().equals(key)) debit = Math.addExact(debit, row.debit());
        }
        return debit;
    }
    private static Set<BudgetKey> keys(List<Step> steps) {
        return steps.stream().flatMap(step -> step.operation().budgets().stream()).collect(Collectors.toUnmodifiableSet());
    }
    private static LocalDate day(Instant now) { return now.atOffset(ZoneOffset.UTC).toLocalDate(); }
    private static YearMonth month(Instant now) { return YearMonth.from(now.atOffset(ZoneOffset.UTC)); }

    private static final class Flow {
        final Instant reservedAt;
        final List<Row> rows;
        final Set<BudgetKey> keys;
        final Map<CommercialKey, Evidence> periods = new HashMap<>();
        Flow(Plan plan, Instant now, Map<CommercialKey, Evidence> samples) {
            reservedAt = now;
            rows = new ArrayList<>(plan.steps().stream().map(Row::new).toList());
            keys = keys(plan.steps());
            for (Step step : plan.steps()) periods.put(step.operation().commercialKey(), samples.get(step.operation().commercialKey()));
        }
    }
    private static final class Row {
        final Step step;
        State state = State.RESERVED;
        long confirmedCredits;
        Row(Step step) { this.step = step; }
        boolean outstanding() { return state == State.RESERVED || state == State.STARTED || state == State.UNCERTAIN; }
        long debit() { return state == State.CANCELLED ? 0 : state == State.CONFIRMED ? confirmedCredits : step.potentialCredits(); }
    }
}
