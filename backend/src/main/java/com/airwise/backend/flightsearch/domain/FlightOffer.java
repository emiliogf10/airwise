package com.airwise.backend.flightsearch.domain;

import java.util.List;
import static com.airwise.backend.flightsearch.domain.DomainChecks.*;
import static com.airwise.backend.flightsearch.domain.FlightTypes.*;

public record FlightOffer(String offerId, ProviderId providerId, DataOrigin dataOrigin, FlightItinerary itinerary,
        PriceInformation price, Seller seller, BaggageInformation baggage, String comparisonGroupId, List<DataIssue> warnings) {
    public FlightOffer {
        require(offerId != null && offerId.matches("of_[0-9a-f]{64}"), "Invalid offer ID");
        require(comparisonGroupId != null && comparisonGroupId.matches("g_[0-9a-f]{64}"), "Invalid group ID");
        present(providerId); present(dataOrigin); present(itinerary); present(price); present(baggage);
        warnings = list(warnings, 0, 20);
        require((providerId == ProviderId.DEMO) == (dataOrigin == DataOrigin.DEMO), "Inconsistent demo provenance");
        require(providerId != ProviderId.DEMO || price.evidenceKind() != EvidenceKind.BOOKING_CONFIRMED, "Demo cannot confirm bookings");
        List<String> segments = itinerary.journeys().stream().flatMap(j -> j.segments().stream()).map(FlightSegment::segmentId).toList();
        require(segments.containsAll(price.scope().coveredSegmentIds()), "Coverage references unknown segment");
        require(itinerary.tripType() != TripType.ONE_WAY || (price.scope().coverage() != Coverage.INBOUND_ONLY
                && price.scope().coverage() != Coverage.OUTBOUND_ONLY), "One-way scope must be normalized");
    }
}
