package com.airwise.backend.flightsearch.application;

import java.util.Currency;
import static com.airwise.backend.flightsearch.domain.DomainChecks.*;

public record CurrencyDefinition(String code, int minorUnits) {
    public CurrencyDefinition {
        require(code != null && code.matches("[A-Z]{3}"), "Invalid currency code");
        require(minorUnits >= 0 && minorUnits <= 4, "Invalid minor units");
        require(Currency.getInstance(code).getDefaultFractionDigits() == minorUnits, "Currency scale mismatch");
    }
}
