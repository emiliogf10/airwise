package com.airwise.backend.flightsearch.application;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import static com.airwise.backend.flightsearch.domain.DomainChecks.*;

public record DatasetDescriptor(String datasetVersion, Instant evaluationInstant, LocalDate referenceDate,
        LocalDate calendarStart, LocalDate calendarEnd, List<CurrencyDefinition> currencies,
        List<String> airlineCodes, ProviderCapabilities capabilities) {
    public DatasetDescriptor {
        id(datasetVersion); present(evaluationInstant); instant(evaluationInstant);
        present(referenceDate); present(calendarStart); present(calendarEnd); present(capabilities);
        require(!referenceDate.isAfter(calendarStart) && !calendarStart.isAfter(calendarEnd)
                && ChronoUnit.DAYS.between(calendarStart, calendarEnd) < 366, "Invalid dataset calendar");
        currencies = list(currencies, 1, 16);
        require(currencies.stream().map(CurrencyDefinition::code).distinct().count() == currencies.size(), "Duplicate currency");
        airlineCodes = list(airlineCodes, 0, 16);
        require(airlineCodes.stream().distinct().count() == airlineCodes.size(), "Duplicate airline");
        require(airlineCodes.stream().allMatch(code -> code.matches("[A-Z0-9]{2}")), "Invalid airline code");
    }

    public boolean contains(LocalDate date) {
        return date != null && !date.isBefore(referenceDate) && !date.isBefore(calendarStart) && !date.isAfter(calendarEnd);
    }
}
