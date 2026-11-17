package com.airwise.backend.flightcomparison.domain;

import java.util.List;
import com.airwise.backend.flightsearch.domain.*;
import static com.airwise.backend.flightcomparison.domain.ComparisonTypes.*;

/** Rankings apply only within each compatible group, not across currencies, evidence or coverage. */
public record ComparisonResult(List<Group> groups) {
    public ComparisonResult { groups = List.copyOf(groups); }
    public record Group(String groupId, FlightOffer representative, List<Row> rows, boolean rankingComplete,
            List<String> recommendationIds) {
        public Group { rows = List.copyOf(rows); recommendationIds = List.copyOf(recommendationIds); }
    }
    public record Row(String offerId, Integer rank, boolean primaryKnown, Money flightPrice, Long durationSeconds,
            int connectionCount, Money knownSubtotal, List<GroundTransportEstimate> groundEstimates,
            List<MissingField> missingFields, List<Reason> reasons) {
        public Row { groundEstimates = List.copyOf(groundEstimates); missingFields = List.copyOf(missingFields); reasons = List.copyOf(reasons); }
    }
}
