package com.airwise.backend.http;

import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.coyote.UpgradeProtocol;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import com.airwise.backend.flightprovider.demo.DemoFlightProvider;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** The client keeps sending past the deadline; the server must finish before the client finishes. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "airwise.demo.cors.allowed-origins=http://localhost:5173")
@ActiveProfiles("demo")
@Import(DemoBodyDeadlineTests.ReadProbeConfiguration.class)
class DemoBodyDeadlineTests {
    private static final String VALID = "{\"query\":{\"tripType\":\"ONE_WAY\",\"origins\":[\"OPO\"],\"destination\":\"MAD\","
            + "\"outboundDate\":\"2027-02-10\",\"travellers\":{\"adults\":1},\"cabin\":\"ECONOMY\"}}";
    @LocalServerPort int port;
    @MockitoSpyBean DemoFlightProvider provider;
    @Autowired AdmissionLimiter limiter;
    @Autowired ReadProbe probe;

    @ParameterizedTest
    @ValueSource(strings = {"fixed", "chunk-header", "chunk-trailers", "blocked-near-deadline"})
    void absoluteDeadlineStopsContinuousAndInternallyBlockedReads(String mode) throws Exception {
        clearInvocations(provider);
        String framing = mode.startsWith("chunk") ? "Transfer-Encoding: chunked" : "Content-Length: 65536";
        String initial = switch (mode) {
            case "chunk-header" -> "1;extension=";
            case "chunk-trailers" -> "e\r\n{\"query\":null}\r\n0\r\nX-Trailer: ";
            default -> "{\"query\":null}";
        };
        try (var socket = new Socket("127.0.0.1", port); var writer = Executors.newSingleThreadExecutor()) {
            socket.setSoTimeout(8000);
            var out = socket.getOutputStream();
            out.write(("POST /api/v1/demo/flight-searches HTTP/1.1\r\nHost: localhost\r\nContent-Type: application/json\r\n"
                    + "Origin: http://localhost:5173\r\n" + framing + "\r\nConnection: close\r\n\r\n" + initial)
                    .getBytes(StandardCharsets.UTF_8));
            out.flush();
            long started = System.nanoTime();
            var writing = writer.submit(() -> {
                try {
                    if (mode.equals("blocked-near-deadline")) {
                        Thread.sleep(4200); out.write(' '); out.flush(); Thread.sleep(4000);
                    } else {
                        for (int i = 0; i < 40; i++) {
                            Thread.sleep(200);
                            out.write(mode.startsWith("chunk") ? 'x' : ' '); out.flush();
                        }
                    }
                } catch (IOException e) { /* Expected once the timed-out connection closes. */ }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            });
            try {
                String response = new String(socket.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
                assertThat(elapsedMillis).as(mode + " elapsed ms").isBetween(4500L, 6000L);
                assertThat(response).startsWith("HTTP/1.1 400").contains("INVALID_REQUEST")
                        .contains("Access-Control-Allow-Origin: http://localhost:5173")
                        .contains("X-Correlation-ID:").doesNotContain("Set-Cookie", "X-Trailer", "extension=");
                verify(provider, never()).search(any());
                // Both same-IP permits must be immediately available; the old reader must not remain alive.
                assertTwoPermitsAvailable();
            } finally {
                socket.close(); writing.cancel(true); writer.shutdownNow();
                assertThat(writer.awaitTermination(2, TimeUnit.SECONDS)).isTrue();
            }
        }
    }

    @Test void alreadyBufferedBodyAndSubsequentRequestStillWork() throws Exception {
        try (var client = HttpClient.newHttpClient()) {
            var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api/v1/demo/flight-searches"))
                    .header("Content-Type", "application/json").timeout(Duration.ofSeconds(3))
                    .POST(HttpRequest.BodyPublishers.ofString(VALID)).build();
            assertThat(client.send(request, HttpResponse.BodyHandlers.ofString()).statusCode()).isEqualTo(200);
            assertThat(client.send(request, HttpResponse.BodyHandlers.ofString()).statusCode()).isEqualTo(200);
        }
        assertTwoPermitsAvailable();
    }

    @Test void disconnectedReaderDoesNotKeepThePermitOrExecuteBusiness() throws Exception {
        clearInvocations(provider);
        probe.entered = new CountDownLatch(1);
        try (var socket = new Socket("127.0.0.1", port)) {
            socket.getOutputStream().write(("POST /api/v1/demo/flight-searches HTTP/1.1\r\nHost: localhost\r\n"
                    + "Content-Type: application/json\r\nContent-Length: 65536\r\nX-Test-Disconnect: true\r\n\r\n{\"query\":null}")
                    .getBytes(StandardCharsets.UTF_8));
            socket.getOutputStream().flush();
            // Ensure the request reached admission before disconnecting; otherwise Tomcat may
            // observe the FIN while parsing headers and legitimately never enter the application.
            assertThat(probe.entered.await(2, TimeUnit.SECONDS)).isTrue();
        }
        long end = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        boolean released = false;
        while (System.nanoTime() < end) {
            var one = limiter.acquire("127.0.0.1");
            var two = limiter.acquire("127.0.0.1");
            try { released = one.permit() != null && two.permit() != null; }
            finally {
                if (one.permit() != null) one.permit().close();
                if (two.permit() != null) two.permit().close();
            }
            if (released) break;
            Thread.sleep(100);
        }
        assertThat(released).isTrue();
        verify(provider, never()).search(any());
    }

    @Test void getBodyIsRejectedWithoutWaitingForTheRemainingContent() throws Exception {
        try (var socket = new Socket("127.0.0.1", port)) {
            socket.setSoTimeout(2000);
            socket.getOutputStream().write(("GET /api/v1/demo/airports HTTP/1.1\r\nHost: localhost\r\n"
                    + "Content-Length: 65536\r\nConnection: close\r\n\r\n{")
                    .getBytes(StandardCharsets.UTF_8));
            socket.getOutputStream().flush();
            String response = new String(socket.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            assertThat(response).startsWith("HTTP/1.1 400").contains("INVALID_REQUEST");
        }
        assertTwoPermitsAvailable();
    }

    @Test void unsupportedTlsAndUpgradeProtocolsFailBeforeBinding() {
        var tls = new DemoDeadlineProtocol(); tls.setSSLEnabled(true);
        assertThatThrownBy(tls::init).isInstanceOf(IllegalStateException.class);
        var upgrade = new DemoDeadlineProtocol(); upgrade.addUpgradeProtocol(mock(UpgradeProtocol.class));
        assertThatThrownBy(upgrade::init).isInstanceOf(IllegalStateException.class);
    }

    private void assertTwoPermitsAvailable() {
        var one = limiter.acquire("127.0.0.1");
        var two = limiter.acquire("127.0.0.1");
        try { assertThat(one.permit()).isNotNull(); assertThat(two.permit()).isNotNull(); }
        finally {
            if (one.permit() != null) one.permit().close();
            if (two.permit() != null) two.permit().close();
        }
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class ReadProbeConfiguration {
        @Bean ReadProbe readProbe() { return new ReadProbe(); }
        @Bean FilterRegistrationBean<ReadProbe> readProbeRegistration(ReadProbe probe) {
            var registration = new FilterRegistrationBean<>(probe);
            registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 11);
            registration.setDispatcherTypes(DispatcherType.REQUEST);
            return registration;
        }
    }
    static class ReadProbe implements Filter {
        volatile CountDownLatch entered = new CountDownLatch(1);
        @Override public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            if (((HttpServletRequest) request).getHeader("X-Test-Disconnect") != null) entered.countDown();
            chain.doFilter(request, response);
        }
    }
}
