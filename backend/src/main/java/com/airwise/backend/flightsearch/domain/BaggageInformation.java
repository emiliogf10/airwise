package com.airwise.backend.flightsearch.domain;

import static com.airwise.backend.flightsearch.domain.DomainChecks.*;
public record BaggageInformation(BaggageAllowance cabin, BaggageAllowance checked) {
    public BaggageInformation { present(cabin); present(checked); }
}
