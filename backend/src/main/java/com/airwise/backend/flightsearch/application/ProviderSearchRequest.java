package com.airwise.backend.flightsearch.application;

import com.airwise.backend.flightsearch.domain.FlightSearchRequest;
import static com.airwise.backend.flightsearch.domain.DomainChecks.*;

public record ProviderSearchRequest(FlightSearchRequest query, String origin, DatasetDescriptor dataset,
        ProviderExecutionContext context) {
    public ProviderSearchRequest {
        present(query); present(context); origin = normalizeAirport(origin);
        require(query.origins().contains(origin), "Origin outside canonical query");
    }
}
