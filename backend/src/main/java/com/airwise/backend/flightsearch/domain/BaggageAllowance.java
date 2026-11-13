package com.airwise.backend.flightsearch.domain;

import java.math.BigDecimal;
import static com.airwise.backend.flightsearch.domain.DomainChecks.*;
import static com.airwise.backend.flightsearch.domain.FlightTypes.*;

public record BaggageAllowance(BaggageStatus status, Integer pieces, BigDecimal weightKg) {
    public BaggageAllowance {
        present(status);
        switch (status) {
            case UNKNOWN -> require(pieces == null && weightKg == null, "Unknown baggage must stay unknown");
            case NOT_INCLUDED -> require(Integer.valueOf(0).equals(pieces) && weightKg == null, "Not-included baggage details");
            case INCLUDED -> {
                require(pieces == null || (pieces >= 1 && pieces <= 10), "Invalid baggage pieces");
                require(weightKg == null || (weightKg.signum() > 0 && weightKg.compareTo(new BigDecimal("100")) <= 0
                        && weightKg.scale() >= 0 && weightKg.scale() <= 3), "Invalid baggage weight");
            }
        }
    }
}
