package com.airwise.backend.flightprovider.admission;

import static com.airwise.backend.flightprovider.admission.AdmissionModel.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Estimates the complete maximum flow; it never assumes a cache hit or an automatic retry. */
public final class FlowEstimator {
    public record Stage(Operation operation, Phase phase, long branches, long callsPerBranch,
            long creditsPerCall, BigDecimal monetaryCostPerCall) { }

    public Decision<Plan> estimate(List<Stage> stages) {
        if (stages == null || stages.isEmpty() || stages.size() > 256) return Decision.deny(Reason.INVALID_PLAN);
        var steps = new ArrayList<Step>();
        long totalCredits = 0;
        try {
            for (Stage stage : stages) {
                if (stage == null || stage.operation() == null || stage.phase() == null
                        || stage.branches() <= 0 || stage.callsPerBranch() <= 0 || stage.creditsPerCall() < 0)
                    return Decision.deny(Reason.INVALID_PLAN);
                long calls = Math.multiplyExact(stage.branches(), stage.callsPerBranch());
                if (calls > 256 - steps.size()) return Decision.deny(Reason.INVALID_PLAN);
                totalCredits = Math.addExact(totalCredits, Math.multiplyExact(calls, stage.creditsPerCall()));
                if (stage.monetaryCostPerCall() == null) return Decision.deny(Reason.UNKNOWN_COST);
                if (stage.monetaryCostPerCall().signum() != 0) return Decision.deny(Reason.NONZERO_COST);
                for (long i = 0; i < calls; i++) steps.add(new Step(stage.operation(), stage.phase(),
                        stage.creditsPerCall(), stage.monetaryCostPerCall()));
            }
            if (steps.size() != 1 && steps.stream().anyMatch(step -> step.phase() == Phase.RETRY))
                return Decision.deny(Reason.INVALID_PLAN);
            return Decision.allow(new Plan(steps));
        } catch (ArithmeticException exception) {
            return Decision.deny(Reason.INVALID_PLAN);
        }
    }
}
