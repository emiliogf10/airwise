package com.airwise.backend.flightcomparison.domain;

import java.util.List;
import com.airwise.backend.flightsearch.domain.FlightSearchRequest;
import static com.airwise.backend.flightsearch.domain.DomainChecks.*;
import static com.airwise.backend.flightsearch.domain.FlightTypes.*;
import static com.airwise.backend.flightcomparison.domain.ComparisonTypes.*;

/** The query and server dataset identify a selection; prices, source and evaluation time are not client inputs. */
public record ComparisonRequest(FlightSearchRequest query, String datasetVersion, List<String> offerIds,
        Primary primary, List<GroundComponent> requiredComponents, List<GroundTransportEstimate> groundEstimates) {
    public ComparisonRequest {
        present(query); id(datasetVersion); present(primary);
        offerIds = list(offerIds, 2, 4);
        offerIds.forEach(value -> require(value.matches("of_[0-9a-f]{64}"), "Invalid offer identifier"));
        require(offerIds.stream().distinct().count() == offerIds.size(), "Duplicate selected offer");
        requiredComponents = list(requiredComponents, 0, 2);
        require(requiredComponents.stream().distinct().count() == requiredComponents.size(), "Duplicate required component");
        requiredComponents = requiredComponents.stream().sorted().toList();
        groundEstimates = list(groundEstimates, 0, 8);
        require((primary == Primary.KNOWN_SUBTOTAL) == !requiredComponents.isEmpty(), "Incompatible primary and components");
        require(primary == Primary.KNOWN_SUBTOTAL || groundEstimates.isEmpty(), "Estimates require subtotal primary");
        require(query.tripType() == TripType.ROUND_TRIP || !requiredComponents.contains(GroundComponent.ORIGIN_GROUND_RETURN), "Return component requires return trip");
        for (var estimate : groundEstimates) {
            require(offerIds.contains(estimate.offerId()), "Estimate outside selected offers");
            require(query.tripType() == TripType.ROUND_TRIP || estimate.component() != GroundComponent.ORIGIN_GROUND_RETURN, "Return estimate requires return trip");
        }
        require(groundEstimates.stream().map(e -> e.offerId() + "/" + e.component()).distinct().count() == groundEstimates.size(), "Duplicate ground component");
    }
}
