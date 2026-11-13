package com.airwise.backend.flightsearch.domain;

import static com.airwise.backend.flightsearch.domain.DomainChecks.*;
public record Seller(String id, String name) {
    public Seller { DomainChecks.id(id); text(name, 120); }
}
