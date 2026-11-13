package com.airwise.backend.flightsearch.domain;

public record Travellers(int adults) {
    public Travellers { DomainChecks.require(adults == 1, "Demo supports one adult"); }
}
