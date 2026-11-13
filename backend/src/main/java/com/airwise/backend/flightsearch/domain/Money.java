package com.airwise.backend.flightsearch.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import static com.airwise.backend.flightsearch.domain.DomainChecks.*;

public record Money(BigDecimal amount, String currency) {
    public Money {
        present(amount);
        require(currency != null && currency.matches("[A-Z]{3}"), "Invalid currency");
        int fraction;
        try { fraction = Currency.getInstance(currency).getDefaultFractionDigits(); }
        catch (IllegalArgumentException e) { throw new IllegalArgumentException("Invalid currency"); }
        require(fraction >= 0 && fraction <= 4 && amount.scale() >= 0 && amount.scale() <= fraction,
                "Invalid monetary scale");
        require(amount.signum() >= 0 && amount.compareTo(new BigDecimal("1002000000")) < 0, "Invalid monetary amount");
        amount = amount.setScale(fraction, RoundingMode.UNNECESSARY);
    }

    /** Validates source lexical precision before BigDecimal discards its original spelling. */
    public static Money parse(String amount, String currency) {
        require(amount != null && amount.matches("(0|[1-9][0-9]{0,9})(\\.[0-9]+)?"), "Invalid decimal syntax");
        return new Money(new BigDecimal(amount), currency);
    }

    public void requireQuotedAmount() {
        require(amount.compareTo(new BigDecimal("1000000000")) < 0, "Quoted amount exceeds limit");
    }
}
