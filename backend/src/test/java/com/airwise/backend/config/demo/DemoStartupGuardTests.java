package com.airwise.backend.config.demo;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.sql.DataSource;
import javax.sql.XADataSource;

import jakarta.persistence.EntityManagerFactory;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.boot.jdbc.autoconfigure.JdbcConnectionDetails;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.env.MapPropertySource;

import com.airwise.backend.flightsearch.application.FlightProvider;

class DemoStartupGuardTests {
    private final ApplicationContextRunner demo = new ApplicationContextRunner()
            .withInitializer(context -> context.getEnvironment().setActiveProfiles("demo"))
            .withUserConfiguration(DemoRuntimeConfiguration.class);

    @Test
    void rejectsExternalPersistencePropertiesWithoutLeakingTheirValues() {
        for (var key : List.of("spring.datasource.url", "spring.datasource.username", "spring.datasource.password",
                "spring.datasource.driver-class-name", "spring.datasource.jndi-name", "spring.datasource.xa.data-source-class-name",
                "spring.datasource.hikari.jdbc-url", "spring.flyway.url", "spring.flyway.user", "spring.flyway.password",
                "SPRING_DATASOURCE_URL", "SPRING_FLYWAY_PASSWORD")) {
            var created = new AtomicInteger();
            demo.withInitializer(context -> context.getEnvironment().getPropertySources()
                    .addFirst(new MapPropertySource("incompatible-test", Map.of(key, "synthetic-sensitive-value"))))
                    .withBean("connectionAttempt", Object.class, () -> {
                        created.incrementAndGet();
                        return new Object();
                    })
                    .run(context -> {
                        assertNotNull(context.getStartupFailure(), key);
                        assertTrue(context.getStartupFailure().getMessage().contains("external persistence properties"), key);
                        assertFalse(context.getStartupFailure().getMessage().contains("synthetic-sensitive-value"), key);
                        assertEquals(0, created.get(), key);
                    });
        }
    }

    @Test
    void rejectsAdditionalProfilesBeforeAnySingletonIsConstructed() {
        var created = new AtomicInteger();
        demo.withInitializer(context -> context.getEnvironment().setActiveProfiles("demo", "persistence"))
                .withBean("connectionAttempt", Object.class, () -> {
                    created.incrementAndGet();
                    return new Object();
                }).run(context -> {
                    assertNotNull(context.getStartupFailure());
                    assertTrue(context.getStartupFailure().getMessage().contains("additional active profile"));
                    assertEquals(0, created.get());
                });
    }

    @Test
    void rejectsPersistenceBeanDefinitionsBeforeTheirSuppliersCanConnect() {
        for (var type : List.of(DataSource.class, XADataSource.class, JdbcConnectionDetails.class,
                EntityManagerFactory.class, Flyway.class)) {
            rejectWithoutInstantiation(type);
        }
    }

    private <T> void rejectWithoutInstantiation(Class<T> type) {
        var created = new AtomicInteger();
        demo.withBean("forbiddenPersistence", type, () -> {
            created.incrementAndGet();
            throw new AssertionError("Persistence supplier must not run");
        }).run(context -> {
            assertNotNull(context.getStartupFailure(), type.getSimpleName());
            assertTrue(context.getStartupFailure().getMessage().contains("persistence bean definitions"), type.getSimpleName());
            assertEquals(0, created.get(), type.getSimpleName());
        });
    }

    @Test
    void demoCannotBeEnabledAsAnImplicitDefaultProfile() {
        new ApplicationContextRunner()
                .withInitializer(context -> context.getEnvironment().setDefaultProfiles("demo"))
                .withUserConfiguration(DemoRuntimeConfiguration.class).run(context -> {
                    assertNotNull(context.getStartupFailure());
                    assertTrue(context.getStartupFailure().getMessage().contains("implicit demo profile activation"));
                });
    }

    @Test
    void runtimeCompositionDoesNotLeakOutsideExplicitDemoProfile() {
        new ApplicationContextRunner().withUserConfiguration(DemoRuntimeConfiguration.class).run(context -> {
            assertNull(context.getStartupFailure());
            assertTrue(context.getBeansOfType(FlightProvider.class).isEmpty());
            assertTrue(context.getBeansOfType(Clock.class).isEmpty());
            assertFalse(context.containsBean("demoStartupGuard"));
            assertNull(context.getEnvironment().getProperty("spring.autoconfigure.exclude"));
        });
    }
}
