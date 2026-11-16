package com.airwise.backend.config.demo;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.sql.DataSource;
import javax.sql.XADataSource;

import jakarta.persistence.EntityManagerFactory;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.jdbc.autoconfigure.JdbcConnectionDetails;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;

/** Rejects persistence configuration before singleton construction, without resolving credentials or connections. */
final class DemoStartupGuard implements BeanFactoryPostProcessor {
    private static final Set<String> FLYWAY_CONNECTION_KEYS = Set.of(
            "spring.flyway.url", "spring.flyway.user", "spring.flyway.password");
    private static final List<Class<?>> PERSISTENCE_TYPES = List.of(
            DataSource.class, XADataSource.class, JdbcConnectionDetails.class, EntityManagerFactory.class, Flyway.class);
    private final ConfigurableEnvironment environment;

    DemoStartupGuard(ConfigurableEnvironment environment) {
        this.environment = environment;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        if (!Arrays.asList(environment.getActiveProfiles()).contains("demo")) {
            throw incompatible("implicit demo profile activation");
        }
        for (var profile : environment.getActiveProfiles()) {
            if (!profile.equals("demo")) {
                throw incompatible("additional active profile");
            }
        }
        for (var source : environment.getPropertySources()) {
            if (source instanceof EnumerablePropertySource<?> enumerable) {
                for (var key : enumerable.getPropertyNames()) {
                    var canonicalKey = key.toLowerCase(Locale.ROOT).replace('_', '.');
                    if (canonicalKey.startsWith("spring.datasource.") || FLYWAY_CONNECTION_KEYS.contains(canonicalKey)) {
                        throw incompatible("external persistence properties");
                    }
                }
            }
        }
        for (var type : PERSISTENCE_TYPES) {
            // false forbids initializing FactoryBeans while predicting their types.
            if (beanFactory.getBeanNamesForType(type, true, false).length != 0) {
                throw incompatible("persistence bean definitions");
            }
        }
    }

    private static BeanDefinitionStoreException incompatible(String category) {
        return new BeanDefinitionStoreException("Demo runtime rejects " + category);
    }
}
