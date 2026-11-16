package com.airwise.backend.config.demo;

import java.time.Clock;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.ConfigurableEnvironment;

import com.airwise.backend.flightprovider.demo.DemoFlightProvider;

/** Explicit composition of the synthetic, in-memory runtime; never a fallback for another provider. */
@Configuration(proxyBeanMethods = false)
@Profile("demo")
public class DemoRuntimeConfiguration {

    @Bean
    static BeanFactoryPostProcessor demoStartupGuard(ConfigurableEnvironment environment) {
        return new DemoStartupGuard(environment);
    }

    @Bean
    Clock demoOperationalClock() {
        return Clock.systemUTC();
    }

    @Bean
    DemoFlightProvider demoFlightProvider(@Qualifier("demoOperationalClock") Clock operationalClock) {
        return new DemoFlightProvider(operationalClock);
    }
}
