package com.airwise.backend.flightsearch.domain;

import static com.airwise.backend.flightsearch.domain.DomainChecks.*;
public record Connection(int afterSegmentIndex, Long durationSeconds, boolean airportChange, Boolean selfTransfer) {
    public Connection {
        require(afterSegmentIndex >= 0 && afterSegmentIndex <= 2, "Invalid connection index");
        seconds(durationSeconds);
    }
}
