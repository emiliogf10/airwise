package com.airwise.backend.http;

import java.time.Clock;
import java.util.EnumSet;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.servlet.DispatcherType;
import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jackson.autoconfigure.JsonFactoryBuilderCustomizer;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.context.NullSecurityContextRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import tools.jackson.core.StreamReadConstraints;
import tools.jackson.core.StreamReadFeature;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.cfg.CoercionAction;
import tools.jackson.databind.cfg.CoercionInputShape;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.type.LogicalType;

@Configuration(proxyBeanMethods = false)
@Profile("demo")
public class DemoHttpConfiguration {
    @Bean ErrorWriter apiErrorWriter(JsonMapper mapper) { return new ErrorWriter(mapper); }
    @Bean AdmissionLimiter demoAdmissionLimiter() { return new AdmissionLimiter(System::nanoTime); }

    @Bean FilterRegistrationBean<BoundaryFilter> demoBoundaryFilter(Clock clock, AdmissionLimiter limiter, ErrorWriter errors,
            @Value("${airwise.demo.cors.allowed-origins:}") String origins) {
        FilterRegistrationBean<BoundaryFilter> bean = new FilterRegistrationBean<>(new BoundaryFilter(clock, limiter, errors, origins));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        bean.setDispatcherTypes(EnumSet.allOf(DispatcherType.class));
        bean.setAsyncSupported(false);
        return bean;
    }

    @Bean SecurityFilterChain demoSecurity(HttpSecurity http, ErrorWriter errors,
            @Value("${airwise.demo.cors.allowed-origins:}") String origins) throws Exception {
        http.formLogin(c -> c.disable()).httpBasic(c -> c.disable()).logout(c -> c.disable())
                .requestCache(c -> c.disable())
                .sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .securityContext(c -> c.securityContextRepository(new NullSecurityContextRepository()))
                .exceptionHandling(c -> c
                        .authenticationEntryPoint((req, res, ex) -> errors.write(req, res, 403, "ACCESS_DENIED"))
                        .accessDeniedHandler((req, res, ex) -> errors.write(req, res, 403, "ACCESS_DENIED")))
                .csrf(c -> c.ignoringRequestMatchers(RoutePolicy.PUBLIC_POSTS))
                .authorizeHttpRequests(c -> c.requestMatchers(RoutePolicy.PUBLIC, RoutePolicy.ERROR).permitAll().anyRequest().denyAll())
                // Headers are supplied at the outer boundary, including failures before this chain.
                .headers(c -> c.disable());
        http.addFilterBefore(DemoCors.filter(origins, errors), CsrfFilter.class);
        http.addFilterBefore(new PublicRouteGate(errors), CsrfFilter.class);
        http.addFilterAfter(new RepresentationFilter(errors), AuthorizationFilter.class);
        return http.build();
    }

    @Bean WebSecurityCustomizer demoFirewallErrors(ErrorWriter errors) {
        return web -> web.requestRejectedHandler((req, res, ex) -> errors.write(req, res, 400, "INVALID_REQUEST"));
    }

    @Bean JsonFactoryBuilderCustomizer demoJsonLimits() {
        return builder -> builder.streamReadConstraints(StreamReadConstraints.builder()
                .maxNestingDepth(12).maxTokenCount(10_000).maxStringLength(1024).maxNameLength(128)
                .maxNumberLength(32).build()).enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION);
    }

    @Bean JsonMapperBuilderCustomizer demoJsonTypes() {
        return builder -> {
            builder.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                    .disable(DeserializationFeature.ACCEPT_FLOAT_AS_INT)
                    .disable(MapperFeature.ALLOW_COERCION_OF_SCALARS)
                    .enable(EnumFeature.FAIL_ON_NUMBERS_FOR_ENUMS)
                    .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                    .changeDefaultPropertyInclusion(value -> value.withValueInclusion(JsonInclude.Include.ALWAYS));
            builder.withCoercionConfig(LogicalType.Textual, c -> c
                    .setCoercion(CoercionInputShape.Integer, CoercionAction.Fail)
                    .setCoercion(CoercionInputShape.Float, CoercionAction.Fail)
                    .setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail));
            builder.withCoercionConfig(LogicalType.Integer, c -> c
                    .setCoercion(CoercionInputShape.String, CoercionAction.Fail)
                    .setCoercion(CoercionInputShape.Float, CoercionAction.Fail)
                    .setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail));
            builder.withCoercionConfig(LogicalType.Boolean, c -> c
                    .setCoercion(CoercionInputShape.String, CoercionAction.Fail)
                    .setCoercion(CoercionInputShape.Integer, CoercionAction.Fail)
                    .setCoercion(CoercionInputShape.Float, CoercionAction.Fail));
        };
    }

    @Bean WebServerFactoryCustomizer<TomcatServletWebServerFactory> demoBodySocketTimeout() {
        return factory -> {
            factory.setProtocol(DemoDeadlineProtocol.class.getName());
            factory.addContextCustomizers(context -> context.getPipeline().addValve(new BodyReadValve()));
            factory.addConnectorCustomizers(connector -> {
                requireSharedDeadlineTransport(connector);
                connector.setProperty("disableUploadTimeout", "false");
                connector.setProperty("connectionUploadTimeout", "5000");
            });
        };
    }

    /**
     * Tomcat creates a named protocol handler reflectively. DevTools restart can otherwise load
     * a second copy in its base loader, silently separating the transport and filter deadlines.
     * Reject that runtime before binding, rather than serving with an ineffective read budget.
     */
    static void requireSharedDeadlineTransport(Connector connector) {
        if (connector.getProtocolHandler().getClass() != DemoDeadlineProtocol.class
                || DemoDeadlineProtocol.class.getClassLoader() != BodyReadDeadline.class.getClassLoader()
                || BodyReadDeadline.class.getClassLoader() != RepresentationFilter.class.getClassLoader()) {
            throw new IllegalStateException("Demo deadline transport requires one application classloader; "
                    + "start the JVM with -Dspring.devtools.restart.enabled=false");
        }
    }
}
