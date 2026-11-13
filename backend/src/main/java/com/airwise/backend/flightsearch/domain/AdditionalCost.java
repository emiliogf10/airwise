package com.airwise.backend.flightsearch.domain;

import static com.airwise.backend.flightsearch.domain.DomainChecks.*;
import static com.airwise.backend.flightsearch.domain.FlightTypes.*;

public record AdditionalCost(AdditionalCostKind kind, Money money, UnknownReason unknownReason, Boolean includedInQuotedPrice) {
    public AdditionalCost {
        present(kind); require((money == null) == (unknownReason != null), "Missing cost reason");
        if (money != null) money.requireQuotedAmount();
    }
}
