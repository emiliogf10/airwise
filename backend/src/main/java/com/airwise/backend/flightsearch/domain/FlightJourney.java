package com.airwise.backend.flightsearch.domain;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import static com.airwise.backend.flightsearch.domain.DomainChecks.*;
import static com.airwise.backend.flightsearch.domain.FlightTypes.*;

public record FlightJourney(Direction direction, List<FlightSegment> segments, List<Connection> connections,
        Long durationSeconds) {
    public FlightJourney {
        present(direction); segments = list(segments, 1, 4); connections = list(connections, 0, 3);
        require(connections.size() == segments.size() - 1, "Missing connection");
        require(segments.stream().map(FlightSegment::segmentId).distinct().count() == segments.size(), "Duplicate segment ID");
        for (int i = 0; i < connections.size(); i++) {
            Connection c = connections.get(i); FlightSegment a = segments.get(i), b = segments.get(i + 1);
            require(c.afterSegmentIndex() == i, "Connection order");
            require(c.airportChange() == !a.arrivalAirport().equals(b.departureAirport()), "Incorrect airport change");
            Long expected = a.arrivalTime().instant() != null && b.departureTime().instant() != null
                    ? Duration.between(a.arrivalTime().instant(), b.departureTime().instant()).getSeconds() : null;
            require(expected == null || expected >= 0, "Impossible connection chronology");
            require(Objects.equals(expected, c.durationSeconds()), "Incorrect connection duration");
        }
        seconds(durationSeconds);
        FlightTime start = segments.getFirst().departureTime(), end = segments.getLast().arrivalTime();
        Long expected = start.instant() != null && end.instant() != null
                ? Duration.between(start.instant(), end.instant()).getSeconds() : null;
        require(expected == null || expected > 0, "Invalid journey chronology");
        require(Objects.equals(expected, durationSeconds), "Incorrect journey duration");
    }
}
