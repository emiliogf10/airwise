package com.airwise.backend.flightcomparison.domain;

import java.math.BigDecimal;
import com.airwise.backend.flightsearch.domain.Money;
import com.airwise.backend.flightsearch.domain.FlightTypes.Basis;
import static com.airwise.backend.flightsearch.domain.DomainChecks.*;
import static com.airwise.backend.flightcomparison.domain.ComparisonTypes.*;

/** A declared group-of-one estimate; never an inferred transport fare or a modification of flight price. */
public record GroundTransportEstimate(String offerId, GroundComponent component, Money money, Basis basis,
        int travellerCount, EstimateSource source, EstimateAssumption assumption) {
    public GroundTransportEstimate {
        require(offerId != null && offerId.matches("of_[0-9a-f]{64}"), "Invalid selected offer identifier");
        present(component); present(money); present(source); present(assumption);
        require(basis == Basis.GROUP && travellerCount == 1, "Ground estimate must cover the group of one");
        require(money.amount().compareTo(new BigDecimal("1000000")) <= 0, "Ground estimate exceeds limit");
    }
}
