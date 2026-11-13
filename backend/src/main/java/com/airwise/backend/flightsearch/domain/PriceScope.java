package com.airwise.backend.flightsearch.domain;

import java.util.List;
import static com.airwise.backend.flightsearch.domain.DomainChecks.*;
import static com.airwise.backend.flightsearch.domain.FlightTypes.*;

public record PriceScope(Basis basis, Integer travellerCount, Cabin cabin, Coverage coverage, List<String> coveredSegmentIds) {
    public PriceScope {
        present(basis); present(coverage); coveredSegmentIds = list(coveredSegmentIds, 0, 8);
        require(travellerCount == null || travellerCount == 1, "Unsupported traveller count");
        coveredSegmentIds.forEach(DomainChecks::id);
        require(coveredSegmentIds.stream().distinct().count() == coveredSegmentIds.size(), "Duplicate covered segment");
        require(coveredSegmentIds.equals(coveredSegmentIds.stream().sorted().toList()), "Covered segments must be sorted");
        require(coverage == Coverage.SEGMENT_ONLY ? !coveredSegmentIds.isEmpty() : coveredSegmentIds.isEmpty(), "Invalid segment coverage");
    }
}
