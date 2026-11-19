package com.airwise.backend.flightprovider.admission;

import static com.airwise.backend.flightprovider.admission.AdmissionModel.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

class FlowEstimatorTests {
    private final FlowEstimator estimator = new FlowEstimator();
    private final Operation operation = new Operation("synthetic", "account", "search");

    @Test
    void estimatesMaximumFanOutAllPhasesAndKeepsAttemptsSeparateFromCredits() {
        var result = estimator.estimate(List.of(stage(Phase.INITIAL, 3, 1, 1),
                stage(Phase.CONTINUATION, 3, 2, 1), stage(Phase.SELLER, 3, 2, 1),
                stage(Phase.ACCOUNT, 1, 1, 0)));
        assertTrue(result.admitted());
        assertEquals(16, result.value().steps().size());
        assertEquals(15, result.value().steps().stream().mapToLong(Step::potentialCredits).sum());
        assertEquals(Phase.ACCOUNT, result.value().steps().getLast().phase());
        assertThrows(UnsupportedOperationException.class, () -> result.value().steps().clear());
    }

    @Test
    void capsExpansionAndRejectsArithmeticOverflowWithoutAllocatingHugeFlow() {
        assertEquals(Reason.INVALID_PLAN, estimator.estimate(List.of(stage(Phase.INITIAL, Long.MAX_VALUE, 2, 1))).reason());
        assertEquals(Reason.INVALID_PLAN, estimator.estimate(List.of(stage(Phase.INITIAL, 257, 1, 0))).reason());
        assertEquals(Reason.INVALID_PLAN, estimator.estimate(List.of(stage(Phase.INITIAL, 2, 1, Long.MAX_VALUE))).reason());
        assertEquals(Reason.INVALID_PLAN, estimator.estimate(List.of(stage(Phase.INITIAL, 1, 1, Long.MAX_VALUE),
                stage(Phase.SELLER, 1, 1, 1))).reason());
        assertTrue(estimator.estimate(List.of(stage(Phase.INITIAL, 256, 1, 0))).admitted());
    }

    @Test
    void missingAndNegativeQuantitiesAreRejected() {
        assertEquals(Reason.INVALID_PLAN, estimator.estimate(null).reason());
        assertEquals(Reason.INVALID_PLAN, estimator.estimate(List.of()).reason());
        assertEquals(Reason.INVALID_PLAN, estimator.estimate(List.of(stage(Phase.INITIAL, 0, 1, 0))).reason());
        assertEquals(Reason.INVALID_PLAN, estimator.estimate(List.of(stage(Phase.INITIAL, 1, -1, 0))).reason());
        assertEquals(Reason.INVALID_PLAN, estimator.estimate(List.of(stage(Phase.INITIAL, 1, 1, -1))).reason());
        assertThrows(IllegalArgumentException.class, () -> new Limits(-1, 0, 0, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new Step(operation, Phase.INITIAL, -1, BigDecimal.ZERO));
    }

    @Test
    void unknownAndNonzeroCostsCannotBecomeFreeByOmission() {
        assertEquals(Reason.UNKNOWN_COST, estimator.estimate(List.of(new FlowEstimator.Stage(operation,
                Phase.INITIAL, 1, 1, 1, null))).reason());
        assertEquals(Reason.NONZERO_COST, estimator.estimate(List.of(new FlowEstimator.Stage(operation,
                Phase.INITIAL, 1, 1, 1, new BigDecimal("0.01")))).reason());
    }

    @Test
    void retryMustBeAdmittedSeparatelyAsOneStep() {
        assertEquals(Reason.INVALID_PLAN, estimator.estimate(List.of(stage(Phase.INITIAL, 1, 1, 1),
                stage(Phase.RETRY, 1, 1, 1))).reason());
        assertEquals(Reason.INVALID_PLAN, estimator.estimate(List.of(stage(Phase.RETRY, 2, 1, 1))).reason());
        assertTrue(estimator.estimate(List.of(stage(Phase.RETRY, 1, 1, 1))).admitted());
    }

    private FlowEstimator.Stage stage(Phase phase, long branches, long calls, long credits) {
        return new FlowEstimator.Stage(operation, phase, branches, calls, credits, BigDecimal.ZERO);
    }
}
