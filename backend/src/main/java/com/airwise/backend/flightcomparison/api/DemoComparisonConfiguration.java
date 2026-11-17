package com.airwise.backend.flightcomparison.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import com.airwise.backend.flightcomparison.application.*;
import com.airwise.backend.flightsearch.application.*;

@Configuration(proxyBeanMethods = false)
@Profile("demo")
public class DemoComparisonConfiguration {
    @Bean ComparisonEngine comparisonEngine() { return new ComparisonEngine(); }
    @Bean DemoComparisonService demoComparisonService(DemoSearchService search, DemoCatalog catalog, ComparisonEngine engine) {
        return new DemoComparisonService(search, catalog, engine);
    }
}
