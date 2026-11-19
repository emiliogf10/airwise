package com.airwise.backend.flightprovider.admission;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Synthetic admission values. Identifiers are opaque local labels, never credentials or URLs. */
public final class AdmissionModel {
    private AdmissionModel() { }

    public enum Scope { SYNTHETIC_SINGLE_PROCESS, REAL, RESTART_RECOVERY, MULTIPROCESS, MULTIPLE_LEDGERS }
    public enum Phase { INITIAL, CONTINUATION, SELLER, ACCOUNT, ARCHIVE, POLLING, RETRY, OTHER }
    public enum State { RESERVED, STARTED, CONFIRMED, UNCERTAIN, CANCELLED }
    public enum Reason {
        NONE, UNSUPPORTED_SCOPE, UNKNOWN_BUDGET, INVALID_EVIDENCE, INVALID_PLAN,
        UNKNOWN_COST, NONZERO_COST, LIMIT_EXCEEDED, COMMERCIAL_QUOTA_EXHAUSTED,
        INVALID_HANDLE, INVALID_TRANSITION, WINDOW_CHANGED, CLOCK_UNRELIABLE, LEDGER_UNAVAILABLE
    }
    public enum Dimension { OPERATION, PROVIDER, ACCOUNT }

    /** Denials contain only fixed local codes, with no exception or provider payload. */
    public record Decision<T>(Reason reason, T value) {
        public Decision { Objects.requireNonNull(reason); if (reason != Reason.NONE && value != null) throw invalid(); }
        public boolean admitted() { return reason == Reason.NONE; }
        public static <T> Decision<T> allow(T value) { return new Decision<>(Reason.NONE, value); }
        public static <T> Decision<T> deny(Reason reason) {
            if (reason == Reason.NONE) throw invalid();
            return new Decision<>(reason, null);
        }
    }

    public record Operation(String provider, String account, String operation) {
        public Operation { identifier(provider); identifier(account); identifier(operation); }
        public CommercialKey commercialKey() { return new CommercialKey(provider, account); }
        public List<BudgetKey> budgets() {
            return List.of(new BudgetKey(Dimension.OPERATION, provider, account, operation),
                    new BudgetKey(Dimension.PROVIDER, provider, "", ""),
                    new BudgetKey(Dimension.ACCOUNT, "", account, ""));
        }
    }
    public record CommercialKey(String provider, String account) {
        public CommercialKey { identifier(provider); identifier(account); }
    }
    public record BudgetKey(Dimension dimension, String provider, String account, String operation) {
        public BudgetKey {
            Objects.requireNonNull(dimension);
            Objects.requireNonNull(provider); Objects.requireNonNull(account); Objects.requireNonNull(operation);
            switch (dimension) {
                case OPERATION -> { identifier(provider); identifier(account); identifier(operation); }
                case PROVIDER -> { identifier(provider); if (!account.isEmpty() || !operation.isEmpty()) throw invalid(); }
                case ACCOUNT -> { identifier(account); if (!provider.isEmpty() || !operation.isEmpty()) throw invalid(); }
            }
        }
    }
    public record Limits(long dailyAttempts, long monthlyAttempts, long dailyCredits, long monthlyCredits,
            int concurrentFlows) {
        public Limits {
            if (dailyAttempts < 0 || monthlyAttempts < 0 || dailyCredits < 0 || monthlyCredits < 0
                    || concurrentFlows < 0) throw invalid();
        }
        public static Limits closed() { return new Limits(0, 0, 0, 0, 0); }
    }
    public record Policy(Map<BudgetKey, Limits> limits, int globalConcurrentFlows,
            int maxAttemptsPerFlow, long maxCreditsPerFlow) {
        public Policy {
            limits = Map.copyOf(limits);
            if (globalConcurrentFlows < 0 || maxAttemptsPerFlow < 0 || maxAttemptsPerFlow > 256
                    || maxCreditsPerFlow < 0) throw invalid();
        }
        public static Policy closed() { return new Policy(Map.of(), 0, 0, 0); }
    }

    /**
     * Monetary cost is EUR without conversion; null means unknown and only exact numeric zero is admitted.
     * Credits are abstract commercial units, never EUR or logical requiredUnits. Account aggregation of
     * credits requires explicitly compatible units; identifiers alone do not establish that compatibility.
     */
    public record Step(Operation operation, Phase phase, long potentialCredits, BigDecimal monetaryCost) {
        public Step {
            Objects.requireNonNull(operation); Objects.requireNonNull(phase);
            if (potentialCredits < 0) throw invalid();
        }
    }
    public record Plan(List<Step> steps) {
        public Plan { steps = List.copyOf(steps); }
    }

    /** Only explicitly synthetic evidence is accepted, including during bootstrap. End instants are exclusive. */
    public record Evidence(CommercialKey key, Scope origin, long remainingCredits,
            Instant observedAt, Instant expiresAt, Instant periodStart, Instant periodEnd) {
        public Evidence {
            Objects.requireNonNull(key); Objects.requireNonNull(origin);
            Objects.requireNonNull(observedAt); Objects.requireNonNull(expiresAt);
            Objects.requireNonNull(periodStart); Objects.requireNonNull(periodEnd);
            if (remainingCredits < 0 || !observedAt.isBefore(expiresAt) || !periodStart.isBefore(periodEnd)) throw invalid();
        }
        boolean validAt(Instant now) {
            return origin == Scope.SYNTHETIC_SINGLE_PROCESS && !now.isBefore(observedAt)
                    && now.isBefore(expiresAt) && !now.isBefore(periodStart) && now.isBefore(periodEnd);
        }
    }

    /** Opaque identity capability. No export/import or recovery format is provided. */
    public static final class Reservation {
        private Reservation() { }
    }
    static Reservation newReservation() { return new Reservation(); }
    static void identifier(String value) {
        if (value == null || !value.matches("[A-Za-z0-9_-]{1,64}")) throw invalid();
    }
    static IllegalArgumentException invalid() { return new IllegalArgumentException("Invalid synthetic admission value"); }
}
