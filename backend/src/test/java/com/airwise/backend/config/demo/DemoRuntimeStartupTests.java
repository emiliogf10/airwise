package com.airwise.backend.config.demo;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Clock;
import java.time.ZoneOffset;
import javax.sql.DataSource;
import javax.sql.XADataSource;

import jakarta.persistence.EntityManagerFactory;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.autoconfigure.JdbcConnectionDetails;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;

import com.airwise.backend.flightprovider.demo.DemoFlightProvider;
import com.airwise.backend.flightsearch.application.FlightProvider;
import com.airwise.backend.flightsearch.domain.FlightTypes.ProviderId;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("demo")
@ExtendWith(OutputCaptureExtension.class)
class DemoRuntimeStartupTests {
    @Autowired private ConfigurableApplicationContext context;
    @Autowired private DemoFlightProvider provider;
    @Autowired private Clock clock;

    @Test
    void explicitDemoStartsWithOneSyntheticProviderAndNoPersistenceOrGeneratedUser(CapturedOutput output) {
        assertTrue(context.isActive());
        assertEquals(1, context.getBeansOfType(FlightProvider.class).size());
        assertSame(provider, context.getBean(FlightProvider.class));
        assertEquals(ProviderId.DEMO, provider.capabilities().providerId());
        assertFalse(provider.airports().isEmpty());
        assertNotNull(provider.dataset());
        assertEquals(ZoneOffset.UTC, clock.getZone());
        assertTrue(context.getBeansOfType(DataSource.class).isEmpty());
        assertTrue(context.getBeansOfType(XADataSource.class).isEmpty());
        assertTrue(context.getBeansOfType(JdbcConnectionDetails.class).isEmpty());
        assertTrue(context.getBeansOfType(EntityManagerFactory.class).isEmpty());
        assertTrue(context.getBeansOfType(Flyway.class).isEmpty());
        assertTrue(context.getBeansOfType(UserDetailsService.class).isEmpty());
        assertFalse(output.getAll().contains("Using generated security password"));
    }
}
