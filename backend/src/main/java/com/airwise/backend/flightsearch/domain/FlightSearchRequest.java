package com.airwise.backend.flightsearch.domain;

import java.time.LocalDate;
import java.util.List;
import static com.airwise.backend.flightsearch.domain.DomainChecks.*;
import static com.airwise.backend.flightsearch.domain.FlightTypes.*;

public record FlightSearchRequest(TripType tripType, List<String> origins, String destination,
        LocalDate outboundDate, LocalDate returnDate, Travellers travellers, Cabin cabin) {
    public FlightSearchRequest {
        present(tripType); present(outboundDate); present(travellers); present(cabin);
        List<String> normalized = list(origins, 1, 3).stream().map(DomainChecks::normalizeAirport).sorted().toList();
        require(normalized.stream().distinct().count() == normalized.size(), "Duplicate origin");
        origins = normalized;
        destination = normalizeAirport(destination);
        require(!origins.contains(destination), "Origin equals destination");
        require(tripType == TripType.ONE_WAY ? returnDate == null
                : returnDate != null && !returnDate.isBefore(outboundDate), "Invalid return date");
    }
}
