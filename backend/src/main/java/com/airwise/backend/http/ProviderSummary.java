package com.airwise.backend.http;

import java.util.List;
import com.airwise.backend.flightsearch.application.ProviderResult;

public record ProviderSummary(String providerId, String origin, String status, int requiredUnits,
        int completedUnits, int offerCount, int observationCount, List<ProviderIssueView> issues) {
    public ProviderSummary { issues = List.copyOf(issues); }

    public static ProviderSummary from(ProviderResult result) {
        return new ProviderSummary(result.providerId().name(), result.origin(), result.status().name(),
                result.requiredUnits(), result.completedUnits(), result.offers().size(),
                result.observations().size(), result.issues().stream().map(ProviderIssueView::from).toList());
    }
}
