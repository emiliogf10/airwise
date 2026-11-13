package com.airwise.backend.flightsearch.domain;

import java.time.Duration;
import java.util.Objects;
import static com.airwise.backend.flightsearch.domain.DomainChecks.*;

public record FlightSegment(String segmentId, String departureAirport, String arrivalAirport,
        FlightTime departureTime, FlightTime arrivalTime, Airline marketingAirline, Airline operatingAirline,
        String flightNumber, Integer technicalStops, Long durationSeconds) {
    public FlightSegment {
        id(segmentId); airport(departureAirport); airport(arrivalAirport); present(departureTime); present(arrivalTime);
        require(!departureAirport.equals(arrivalAirport), "Invalid segment route");
        if (flightNumber != null) text(flightNumber, 120);
        require(technicalStops == null || (technicalStops >= 0 && technicalStops <= 3), "Invalid technical stops");
        seconds(durationSeconds);
        Long expected = departureTime.instant() != null && arrivalTime.instant() != null
                ? Duration.between(departureTime.instant(), arrivalTime.instant()).getSeconds() : null;
        require(expected == null || expected > 0, "Nonpositive segment duration");
        require(Objects.equals(expected, durationSeconds), "Inconsistent segment duration");
    }
}
