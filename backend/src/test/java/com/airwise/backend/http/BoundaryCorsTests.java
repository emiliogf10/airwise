package com.airwise.backend.http;

import java.time.Clock;
import java.net.URL;
import java.net.URLClassLoader;
import jakarta.servlet.DispatcherType;
import org.apache.catalina.connector.Connector;
import org.apache.coyote.ProtocolHandler;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.json.JsonMapper;
import static org.assertj.core.api.Assertions.*;

class BoundaryCorsTests {
    @Test void quotaErrorRetainsCorsForAValidPreflightAndDoesNotAdmitInvalidHeaders() throws Exception {
        var limiter = new AdmissionLimiter(() -> 0L);
        for (int i = 0; i < 60; i++) limiter.acquire("127.0.0.1").permit().close();
        var filter = new BoundaryFilter(Clock.systemUTC(), limiter, new ErrorWriter(JsonMapper.builder().build()), "http://localhost:5173");
        for (String header : new String[] {"Content-Type", "Authorization"}) {
            var request = new MockHttpServletRequest("OPTIONS", "/api/v1/demo/flight-searches");
            request.setDispatcherType(DispatcherType.REQUEST); request.setRemoteAddr("127.0.0.1");
            request.addHeader("Origin", "http://localhost:5173");
            request.addHeader("Access-Control-Request-Method", "POST");
            request.addHeader("Access-Control-Request-Headers", header);
            var response = new MockHttpServletResponse();
            filter.doFilter(request, response, (req, res) -> { throw new AssertionError("Business must not run"); });
            assertThat(response.getStatus()).isEqualTo(429);
            assertThat(response.getContentAsString()).contains("RATE_LIMITED");
            assertThat(response.getHeader("Retry-After")).isEqualTo("1");
            if (header.equals("Content-Type"))
                assertThat(response.getHeader("Access-Control-Allow-Origin")).isEqualTo("http://localhost:5173");
            else assertThat(response.getHeader("Access-Control-Allow-Origin")).isNull();
        }
    }

    @Test void bodyReadScopeIsRemovedOnExceptionalExitBeforeAReusedThreadReadsAgain() {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        assertThatThrownBy(() -> {
            try (var scope = BodyReadDeadline.open(request, response)) {
                assertThat(scope.remainingMillis()).isBetween(1L, 5000L);
                throw new IllegalStateException("Synthetic scope failure");
            }
        }).isInstanceOf(IllegalStateException.class);
        assertThat(BodyReadDeadline.current()).isNull();
        try (var next = BodyReadDeadline.open(request, response)) { assertThat(BodyReadDeadline.current()).isSameAs(next); }
        assertThat(BodyReadDeadline.current()).isNull();
    }

    @Test void transportWithTheSameNameButAnotherClassloaderIsRejectedBeforeInitialization() throws Exception {
        assertThatCode(() -> DemoHttpConfiguration.requireSharedDeadlineTransport(new Connector(new DemoDeadlineProtocol())))
                .doesNotThrowAnyException();
        URL classes = DemoDeadlineProtocol.class.getProtectionDomain().getCodeSource().getLocation();
        String protocolName = DemoDeadlineProtocol.class.getName();
        try (var isolated = new URLClassLoader(new URL[] { classes }, DemoDeadlineProtocol.class.getClassLoader()) {
            @Override protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (!name.startsWith(protocolName)) return super.loadClass(name, resolve);
                synchronized (getClassLoadingLock(name)) {
                    Class<?> loaded = findLoadedClass(name);
                    if (loaded == null) loaded = findClass(name);
                    if (resolve) resolveClass(loaded);
                    return loaded;
                }
            }
        }) {
            ProtocolHandler handler = (ProtocolHandler) isolated.loadClass(protocolName).getConstructor().newInstance();
            assertThat(handler.getClass().getName()).isEqualTo(protocolName);
            assertThat(handler.getClass()).isNotEqualTo(DemoDeadlineProtocol.class);
            assertThatThrownBy(() -> DemoHttpConfiguration.requireSharedDeadlineTransport(new Connector(handler)))
                    .isInstanceOf(IllegalStateException.class).hasMessageContaining("-Dspring.devtools.restart.enabled=false");
        }
    }
}
