package com.airwise.backend.flightsearch.application;

import java.math.BigDecimal;
import java.util.List;
import com.airwise.backend.flightsearch.domain.FlightSearchRequest;

/** Presentation choices are deliberately excluded from the canonical query identity. */
public record SearchView(FlightSearchRequest query, String datasetVersion, Filters filters, Sort sort, int page, int pageSize) {
    public enum Sort { DEFAULT, PRICE_ASC, DURATION_ASC, CONNECTIONS_ASC }
    public record Filters(List<String> origins, List<String> airlineCodes, Integer maxConnections,
            Integer maxDurationMinutes, String currency, BigDecimal maxPrice) {
        public Filters { origins = List.copyOf(origins); airlineCodes = List.copyOf(airlineCodes); }
        public static Filters none() { return new Filters(List.of(), List.of(), null, null, null, null); }
    }
}
