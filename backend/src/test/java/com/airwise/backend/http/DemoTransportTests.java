package com.airwise.backend.http;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.json.JsonMapper;
import static org.assertj.core.api.Assertions.*;

/** Real Tomcat + socket tests; no MockMvc replacement for transport or ERROR dispatch. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "airwise.demo.cors.allowed-origins=http://localhost:5173")
@ActiveProfiles("demo")
@Import(DemoTransportTests.FaultConfiguration.class)
class DemoTransportTests {
    @LocalServerPort int port;
    @Autowired JsonMapper mapper;
    @Autowired FaultGate gate;
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
    private URI uri(String path) { return URI.create("http://127.0.0.1:" + port + path); }
    private HttpRequest get(String query) { return HttpRequest.newBuilder(uri("/api/v1/demo/airports" + query)).timeout(Duration.ofSeconds(8)).GET().build(); }

    @Test void catalogAndSearchExamplesWorkOnActualHttpServer() throws Exception {
        var catalog = client.send(get("?q=Porto"), HttpResponse.BodyHandlers.ofString());
        assertThat(catalog.statusCode()).isEqualTo(200);
        assertThat(mapper.readTree(catalog.body()).at("/items/0/code").asString()).isEqualTo("OPO");
        String body = "{\"query\":{\"tripType\":\"ONE_WAY\",\"origins\":[\"OPO\"],\"destination\":\"MAD\","
                + "\"outboundDate\":\"2027-02-10\",\"travellers\":{\"adults\":1},\"cabin\":\"ECONOMY\"}}";
        var response = client.send(HttpRequest.newBuilder(uri("/api/v1/demo/flight-searches"))
                .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(200);
        var json = mapper.readTree(response.body());
        assertThat(json.at("/offers/0/price/money/amount").asString()).isEqualTo("100.00");
        assertThat(response.headers().firstValue("X-Correlation-ID")).contains(json.get("correlationId").asString());
        assertThat(response.headers().firstValue("Set-Cookie")).isEmpty();
        assertThat(response.headers().firstValue("Server")).isEmpty();
        var denied = client.send(HttpRequest.newBuilder(uri("/api/v1/demo/comparisons")).POST(HttpRequest.BodyPublishers.noBody()).build(), HttpResponse.BodyHandlers.ofString());
        assertThat(denied.statusCode()).isEqualTo(403);
    }

    @Test void effectiveByteLimitIncludesChunkedRequestsWithoutContentLength() throws Exception {
        String body = "{\"query\":null}" + " ".repeat(65522);
        assertThat(rawChunked(body)).startsWith("HTTP/1.1 422");
        String excessive = rawChunked(body + " ");
        assertThat(excessive).startsWith("HTTP/1.1 413").contains("PAYLOAD_TOO_LARGE");
        String header = raw("GET /api/v1/demo/airports HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\nX-Large: " + "x".repeat(8200) + "\r\n\r\n");
        assertThat(header).startsWith("HTTP/1.1 400").doesNotContain("X-Large");
    }

    @Test void actualContainerErrorPreservesContextCorsAndSanitizedJson() throws Exception {
        var response = client.send(HttpRequest.newBuilder(uri("/api/v1/demo/airports?q=container-error"))
                .header("Origin", "http://localhost:5173").GET().build(), HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(500);
        assertThat(response.body()).contains("INTERNAL_ERROR").doesNotContain("synthetic-container-secret");
        assertThat(response.headers().firstValue("Access-Control-Allow-Origin")).contains("http://localhost:5173");
        assertThat(response.headers().firstValue("X-Correlation-ID")).contains(mapper.readTree(response.body()).get("correlationId").asString());
        assertThat(response.headers().firstValue("Set-Cookie")).isEmpty();
        var postError = client.send(HttpRequest.newBuilder(uri("/api/v1/demo/flight-searches"))
                .header("Content-Type", "application/json").header("X-Test-Container-Error", "true")
                .POST(HttpRequest.BodyPublishers.ofString("{}" )).build(), HttpResponse.BodyHandlers.ofString());
        assertThat(postError.statusCode()).isEqualTo(500);
        assertThat(postError.body()).contains("INTERNAL_ERROR").doesNotContain("synthetic-container-secret");
        assertThat(postError.headers().firstValue("Set-Cookie")).isEmpty();
    }

    @Test void simultaneousRequestsHaveNoQueueAndPermitsAreReleasedAfterwards() throws Exception {
        gate.block = true;
        var one = client.sendAsync(get("?q=hold"), HttpResponse.BodyHandlers.ofString());
        var two = client.sendAsync(get("?q=hold"), HttpResponse.BodyHandlers.ofString());
        try {
            assertThat(gate.entered.await(3, TimeUnit.SECONDS)).isTrue();
            var rejected = client.send(HttpRequest.newBuilder(uri("/api/v1/demo/airports")).header("Origin", "http://localhost:5173").GET().build(), HttpResponse.BodyHandlers.ofString());
            assertThat(rejected.statusCode()).isEqualTo(429);
            assertThat(rejected.body()).contains("RATE_LIMITED");
            assertThat(rejected.headers().firstValue("Retry-After")).contains("1");
            assertThat(rejected.headers().firstValue("Access-Control-Allow-Origin")).contains("http://localhost:5173");
        } finally { gate.release.countDown(); gate.block = false; }
        assertThat(one.get(4, TimeUnit.SECONDS).statusCode()).isEqualTo(200);
        assertThat(two.get(4, TimeUnit.SECONDS).statusCode()).isEqualTo(200);
        assertThat(client.send(get(""), HttpResponse.BodyHandlers.ofString()).statusCode()).isEqualTo(200);
    }

    private String rawChunked(String body) throws Exception {
        return raw("POST /api/v1/demo/flight-searches HTTP/1.1\r\nHost: localhost\r\nContent-Type: application/json\r\nTransfer-Encoding: chunked\r\nConnection: close\r\n\r\n"
                + Integer.toHexString(body.length()) + "\r\n" + body + "\r\n0\r\n\r\n");
    }
    private String raw(String request) throws Exception {
        try (var socket = new Socket("127.0.0.1", port)) {
            socket.setSoTimeout(8000);
            socket.getOutputStream().write(request.getBytes(StandardCharsets.UTF_8)); socket.getOutputStream().flush();
            return new String(socket.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        }
    }
    @TestConfiguration(proxyBeanMethods = false)
    static class FaultConfiguration {
        @Bean FaultGate faultGate() { return new FaultGate(); }
        @Bean FilterRegistrationBean<FaultGate> faultRegistration(FaultGate gate) {
            var bean = new FilterRegistrationBean<>(gate);
            bean.setDispatcherTypes(DispatcherType.REQUEST); bean.setOrder(100); return bean;
        }
    }
    static class FaultGate implements Filter {
        volatile boolean block;
        final CountDownLatch entered = new CountDownLatch(2), release = new CountDownLatch(1);
        public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
            HttpServletRequest request = (HttpServletRequest) req;
            if ("q=container-error".equals(request.getQueryString()) || request.getHeader("X-Test-Container-Error") != null) {
                ((HttpServletResponse) res).sendError(500, "synthetic-container-secret"); return;
            }
            if (block && "q=hold".equals(request.getQueryString())) {
                entered.countDown();
                try { if (!release.await(4, TimeUnit.SECONDS)) throw new ServletException("Test barrier timeout"); }
                catch (InterruptedException exception) { Thread.currentThread().interrupt(); throw new ServletException(); }
            }
            chain.doFilter(req, res);
        }
    }
}
