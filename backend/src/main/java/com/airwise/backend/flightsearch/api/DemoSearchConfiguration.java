package com.airwise.backend.flightsearch.api;

import java.time.Clock;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import com.airwise.backend.flightprovider.demo.DemoFlightProvider;
import com.airwise.backend.flightsearch.application.DemoCatalog;
import com.airwise.backend.flightsearch.application.DemoSearchService;

@Configuration(proxyBeanMethods = false)
@Profile("demo")
public class DemoSearchConfiguration {
    @Bean DemoCatalog demoCatalog(DemoFlightProvider provider) { return new DemoCatalog(provider.dataset(), provider.airports()); }
    @Bean DemoSearchService demoSearchService(DemoFlightProvider provider, DemoCatalog catalog,
            @Qualifier("demoOperationalClock") Clock clock) { return new DemoSearchService(provider, catalog, clock); }
}
