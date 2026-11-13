package com.airwise.backend.flightsearch.domain;

import java.time.LocalDate;
import java.util.List;
import static com.airwise.backend.flightsearch.domain.DomainChecks.*;
import static com.airwise.backend.flightsearch.domain.FlightTypes.*;

public record PriceObservation(String observationId, ProviderId providerId, DataOrigin dataOrigin, String origin,
        String destination, TripType tripType, LocalDate outboundDate, LocalDate returnDate,
        PriceInformation price, List<DataIssue> warnings) {
    public PriceObservation {
        require(observationId != null && observationId.matches("ob_[0-9a-f]{64}"), "Invalid observation ID");
        present(providerId); present(dataOrigin); airport(origin); airport(destination); present(price);
        require(!origin.equals(destination), "Invalid observed route");
        require(tripType != TripType.ONE_WAY || returnDate == null, "Unexpected observed return");
        require(outboundDate == null || returnDate == null || !returnDate.isBefore(outboundDate), "Invalid observed dates");
        require(price.scope().coverage() != Coverage.SEGMENT_ONLY, "Observation cannot reference absent segments");
        require((providerId == ProviderId.DEMO) == (dataOrigin == DataOrigin.DEMO), "Inconsistent demo provenance");
        require(providerId != ProviderId.DEMO || price.evidenceKind() != EvidenceKind.BOOKING_CONFIRMED, "Demo cannot confirm bookings");
        warnings = list(warnings, 0, 20);
    }
}
