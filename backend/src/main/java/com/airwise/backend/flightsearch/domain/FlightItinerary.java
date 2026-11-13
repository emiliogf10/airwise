package com.airwise.backend.flightsearch.domain;

import java.util.List;
import java.util.Objects;
import static com.airwise.backend.flightsearch.domain.DomainChecks.*;
import static com.airwise.backend.flightsearch.domain.FlightTypes.*;

public record FlightItinerary(TripType tripType, List<FlightJourney> journeys, Long durationSeconds, int connectionCount) {
    public FlightItinerary {
        present(tripType); journeys = list(journeys, 1, 2);
        require(journeys.size() == (tripType == TripType.ONE_WAY ? 1 : 2), "Incomplete itinerary");
        require(journeys.getFirst().direction() == Direction.OUTBOUND, "Outbound must be first");
        List<String> ids = journeys.stream().flatMap(j -> j.segments().stream()).map(FlightSegment::segmentId).toList();
        require(ids.stream().distinct().count() == ids.size(), "Duplicate itinerary segment ID");
        if (tripType == TripType.ROUND_TRIP) {
            FlightJourney out = journeys.getFirst(), in = journeys.getLast();
            require(in.direction() == Direction.INBOUND, "Missing inbound");
            require(out.segments().getFirst().departureAirport().equals(in.segments().getLast().arrivalAirport())
                    && out.segments().getLast().arrivalAirport().equals(in.segments().getFirst().departureAirport()), "Unlinked return");
            var arrival = out.segments().getLast().arrivalTime().instant();
            var departure = in.segments().getFirst().departureTime().instant();
            require(arrival == null || departure == null || departure.isAfter(arrival), "Return precedes outbound arrival");
        }
        Long expected = journeys.stream().allMatch(j -> j.durationSeconds() != null)
                ? journeys.stream().mapToLong(FlightJourney::durationSeconds).sum() : null;
        seconds(durationSeconds);
        require(Objects.equals(expected, durationSeconds), "Incorrect itinerary duration");
        require(connectionCount >= 0 && connectionCount <= 6
                && connectionCount == journeys.stream().mapToInt(j -> j.connections().size()).sum(), "Incorrect connection count");
    }
}
