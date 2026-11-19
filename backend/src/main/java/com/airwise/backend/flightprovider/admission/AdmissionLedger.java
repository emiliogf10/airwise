package com.airwise.backend.flightprovider.admission;

import static com.airwise.backend.flightprovider.admission.AdmissionModel.*;

import java.time.Instant;

/**
 * Atomic synthetic ledger port. Every operation must serialize on the same shared ledger.
 * A failure with a possible mutation MUST close that ledger before releasing its transaction/lock;
 * callers cannot provide atomicity by calling close after an exception. Never retry a failed mutation.
 * This contract supplies no durable storage or permission to execute a real request.
 */
public interface AdmissionLedger {
    Decision<Void> recordEvidence(Evidence evidence, Instant now);
    Decision<Reservation> reserve(Plan plan, Instant now);
    Decision<State> start(Reservation reservation, int step, Instant now);
    Decision<State> uncertain(Reservation reservation, int step, Instant now);
    /** Reliable synthetic evidence settles commercial credits only; a sent attempt is never refunded. */
    Decision<State> confirm(Reservation reservation, int step, long consumedCredits, Instant now);
    Decision<State> cancel(Reservation reservation, int step, Instant now);
    Decision<State> state(Reservation reservation, int step, Instant now);
    /** Irreversible closure retains every reservation and debit. */
    void close();
}
