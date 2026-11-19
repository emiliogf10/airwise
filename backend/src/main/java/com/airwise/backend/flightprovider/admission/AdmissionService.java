package com.airwise.backend.flightprovider.admission;

import static com.airwise.backend.flightprovider.admission.AdmissionModel.*;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.function.Function;

/** No Spring wiring, callback or transport: start grants a one-use synthetic state transition only. */
public final class AdmissionService {
    private final AdmissionLedger ledger;
    private final Clock clock;
    private final Scope scope;
    private volatile boolean failed;

    public AdmissionService(AdmissionLedger ledger, Clock clock, Scope scope) {
        this.ledger = Objects.requireNonNull(ledger);
        this.clock = Objects.requireNonNull(clock);
        this.scope = Objects.requireNonNull(scope);
    }

    public Decision<Void> recordEvidence(Evidence evidence) { return call(now -> ledger.recordEvidence(evidence, now)); }
    public Decision<Reservation> reserve(Plan plan) { return call(now -> ledger.reserve(plan, now)); }
    public Decision<State> start(Reservation reservation, int step) { return call(now -> ledger.start(reservation, step, now)); }
    public Decision<State> uncertain(Reservation reservation, int step) { return call(now -> ledger.uncertain(reservation, step, now)); }
    public Decision<State> confirm(Reservation reservation, int step, long consumedCredits) {
        return call(now -> ledger.confirm(reservation, step, consumedCredits, now));
    }
    public Decision<State> cancel(Reservation reservation, int step) { return call(now -> ledger.cancel(reservation, step, now)); }
    public Decision<State> state(Reservation reservation, int step) { return call(now -> ledger.state(reservation, step, now)); }

    private <T> Decision<T> call(Function<Instant, Decision<T>> operation) {
        if (scope != Scope.SYNTHETIC_SINGLE_PROCESS) return Decision.deny(Reason.UNSUPPORTED_SCOPE);
        // Sample time under the same shared monitor: thread scheduling cannot invert valid samples.
        synchronized (ledger) {
            if (failed) return Decision.deny(Reason.LEDGER_UNAVAILABLE);
            try {
                return Objects.requireNonNull(operation.apply(clock.instant()));
            } catch (RuntimeException exception) {
                failed = true;
                try { ledger.close(); } catch (RuntimeException closeFailure) { /* Remain closed, never retry. */ }
                return Decision.deny(Reason.LEDGER_UNAVAILABLE);
            }
        }
    }
}
