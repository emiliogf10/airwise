package com.airwise.backend.flightsearch.domain;

import static com.airwise.backend.flightsearch.domain.DomainChecks.*;
public record Airline(String code, String name) {
    public Airline {
        require(code != null || name != null, "Unknown airline must be null");
        if (code != null) require(code.matches("[A-Z0-9]{2}"), "Invalid airline code");
        if (name != null) text(name, 120);
    }
}
