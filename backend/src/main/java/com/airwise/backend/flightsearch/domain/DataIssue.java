package com.airwise.backend.flightsearch.domain;

import java.util.List;
import static com.airwise.backend.flightsearch.domain.DomainChecks.*;
import static com.airwise.backend.flightsearch.domain.FlightTypes.*;

public record DataIssue(DataIssueCode code, String path, String message) {
    private static final List<String> PATHS = List.of("/price/money", "/price/scope", "/price/additionalCosts",
            "/baggage/cabin", "/baggage/checked", "/itinerary", "/departureTime", "/arrivalTime", "/observedAt", "/expiresAt");
    public DataIssue {
        present(code); text(message, 240);
        require(path == null || PATHS.contains(path), "Unknown data issue path");
    }
}
