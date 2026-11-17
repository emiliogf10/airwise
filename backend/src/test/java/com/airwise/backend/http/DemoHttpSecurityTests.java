package com.airwise.backend.http;

import java.util.concurrent.atomic.AtomicInteger;
import jakarta.servlet.DispatcherType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.json.JsonMapper;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "airwise.demo.cors.allowed-origins=http://localhost:5173")
@AutoConfigureMockMvc
@ActiveProfiles("demo")
class DemoHttpSecurityTests {
    private static final String AIRPORTS = "/api/v1/demo/airports";
    private static final String SEARCH = "/api/v1/demo/flight-searches";
    private static final AtomicInteger IP = new AtomicInteger();
    @Autowired MockMvc mvc;
    @Autowired JsonMapper mapper;
    @Autowired org.springframework.security.web.SecurityFilterChain security;
    private static MockHttpServletRequestBuilder external(MockHttpServletRequestBuilder request) {
        return request.with(r -> { r.setRemoteAddr("192.0.2." + IP.incrementAndGet()); return r; });
    }

    @ParameterizedTest
    @ValueSource(strings = {"/login", "/logout", "/error", "/actuator", "/actuator/health", "/actuator/env",
            "/api/v1/demo/airports/", "/api/v1/demo/airports/extra", "/api/v1/demo/airports.json", "/index.html",
            "/v3/api-docs", "/api/v1/demo/comparisons"})
    void routesOutsideExactAllowlistAreDeniedBeforeMvc(String path) throws Exception {
        mvc.perform(external(get(path)).accept("text/html")).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"))
                .andExpect(header().doesNotExist("Set-Cookie"))
                .andExpect(header().doesNotExist("WWW-Authenticate"));
    }

    @Test void headWrongMethodOptionsAndUnsafeRequestsNeverCreateSessions() throws Exception {
        for (var request : new MockHttpServletRequestBuilder[] { head(AIRPORTS), post(AIRPORTS), get(SEARCH),
                options(AIRPORTS), post("/logout"), post("/api/v1/demo/comparisons/") }) {
            var result = mvc.perform(external(request).cookie(new jakarta.servlet.http.Cookie("JSESSIONID", "synthetic")))
                    .andExpect(status().isForbidden()).andExpect(header().doesNotExist("Set-Cookie")).andReturn();
            assertThat(result.getRequest().getSession(false)).isNull();
        }
    }

    @Test void secondaryDispatchCannotRepeatBusinessAndEncodedAmbiguityIsSanitized() throws Exception {
        for (DispatcherType dispatcher : new DispatcherType[] {DispatcherType.FORWARD, DispatcherType.INCLUDE, DispatcherType.ASYNC}) {
            mvc.perform(external(get(AIRPORTS)).with(r -> { r.setDispatcherType(dispatcher); return r; }))
                    .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
        }
        mvc.perform(external(get("/api/v1/demo/airports;anything=1")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test void publicCatalogIgnoresClientCorrelationCookiesAndForwardedHeaders() throws Exception {
        var result = mvc.perform(external(get(AIRPORTS)).header("X-Correlation-ID", "client-value")
                .header("X-Forwarded-Proto", "https").header("X-Forwarded-For", "198.51.100.1")
                .cookie(new jakarta.servlet.http.Cookie("JSESSIONID", "synthetic")))
                .andExpect(status().isOk()).andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("Referrer-Policy", "no-referrer"))
                .andExpect(header().exists("Content-Security-Policy"))
                .andExpect(header().exists("Permissions-Policy"))
                .andExpect(header().doesNotExist("Set-Cookie"))
                .andExpect(header().doesNotExist("Server"))
                .andExpect(header().doesNotExist("X-Powered-By"))
                .andExpect(header().doesNotExist("Strict-Transport-Security"))
                .andReturn();
        var json = mapper.readTree(result.getResponse().getContentAsByteArray());
        var correlation = result.getResponse().getHeader("X-Correlation-ID");
        assertThat(java.util.UUID.fromString(correlation)).isNotNull();
        assertThat(json.get("correlationId").asString()).isEqualTo(correlation);
        assertThat(result.getRequest().getSession(false)).isNull();
    }

    @Test void validPreflightIsBodylessAndErrorsRetainAllowedCors() throws Exception {
        mvc.perform(external(options(SEARCH)).header("Origin", "http://localhost:5173")
                .header("Access-Control-Request-Method", "POST").header("Access-Control-Request-Headers", "Content-Type"))
                .andExpect(status().isOk()).andExpect(content().string(""))
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Methods", "POST"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Credentials"));
        mvc.perform(external(post(SEARCH)).header("Origin", "http://localhost:5173").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest()).andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    @Test void corsRejectsUnlistedOriginRequestedHeadersAndWrongMethodWithJson() throws Exception {
        for (String origin : new String[] {"null", "http://127.0.0.1:5173", "http://localhost:5174", "https://localhost:5173"}) {
            mvc.perform(external(options(SEARCH)).header("Origin", origin).header("Access-Control-Request-Method", "POST"))
                    .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("ACCESS_DENIED"))
                    .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
        }
        for (String header : new String[] {"Authorization", "X-Correlation-ID", "Content-Type, Authorization"}) {
            mvc.perform(external(options(SEARCH)).header("Origin", "http://localhost:5173")
                    .header("Access-Control-Request-Method", "POST").header("Access-Control-Request-Headers", header))
                    .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
        }
        mvc.perform(external(options(AIRPORTS)).header("Origin", "http://localhost:5173").header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
    }

    @Test void acceptHonorsSpecificQAndDoesNotOverridePriorMediaOrAuthorizationFailure() throws Exception {
        for (String accept : new String[] {"text/html", "application/json;q=0", "application/json;q=0,*/*;q=1"}) {
            mvc.perform(external(get(AIRPORTS)).header("Accept", accept)).andExpect(status().isNotAcceptable())
                    .andExpect(jsonPath("$.code").value("NOT_ACCEPTABLE"));
        }
        mvc.perform(external(get(AIRPORTS)).header("Accept", "text/html;q=1,application/json;q=0.2"))
                .andExpect(status().isOk());
        mvc.perform(external(post(SEARCH)).contentType("text/plain").accept("text/html").content("{}"))
                .andExpect(status().isUnsupportedMediaType());
        mvc.perform(external(post("/no-route")).contentType("text/plain").accept("text/html"))
                .andExpect(status().isForbidden());
    }

    @Test void bodyBytesUtf8AndContentEncodingAreEnforcedBeforeParsing() throws Exception {
        mvc.perform(external(post(SEARCH)).contentType("application/json;charset=ISO-8859-1").content("{}"))
                .andExpect(status().isUnsupportedMediaType());
        mvc.perform(external(post(SEARCH)).contentType(MediaType.APPLICATION_JSON).header("Content-Encoding", "gzip").content("{}"))
                .andExpect(status().isUnsupportedMediaType());
        mvc.perform(external(post(SEARCH)).contentType(MediaType.APPLICATION_JSON).content(new byte[] {'{', '"', (byte) 0xC3, '"', '}'}))
                .andExpect(status().isBadRequest());
        String atLimit = "{\"query\":null}" + " ".repeat(65522);
        assertThat(atLimit.getBytes(java.nio.charset.StandardCharsets.UTF_8)).hasSize(65536);
        mvc.perform(external(post(SEARCH)).contentType(MediaType.APPLICATION_JSON).content(atLimit))
                .andExpect(status().isUnprocessableEntity());
        mvc.perform(external(post(SEARCH)).contentType(MediaType.APPLICATION_JSON).content(atLimit + " "))
                .andExpect(status().isPayloadTooLarge()).andExpect(jsonPath("$.code").value("PAYLOAD_TOO_LARGE"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{\"query\":null,\"query\":null}", "{} {}", "{\"query\":null,\"page\":1e0}", "{\"query\":null,\"page\":1.0}", "{\"query\":null,\"page\":\"1\"}", "{\"secret-marker\":1}"})
    void duplicateTrailingAndWrongTokenTypesAreInvalidWithoutEcho(String json) throws Exception {
        mvc.perform(external(post(SEARCH)).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("secret-marker"))));
    }

    @Test void jacksonConstraintsAreEffectiveOnTheProductionMapper() {
        for (String value : new String[] { "[".repeat(13) + "0" + "]".repeat(13),
                "{\"" + "x".repeat(129) + "\":0}", "\"" + "x".repeat(1025) + "\"",
                "1".repeat(33), "[" + "0,".repeat(9998) + "0]" }) {
            assertThatExceptionOfType(tools.jackson.core.JacksonException.class)
                    .as("JSON constraint length=%s prefix=%s", value.length(), value.substring(0, Math.min(16, value.length())))
                    .isThrownBy(() -> mapper.readTree(value));
        }
        assertThatCode(() -> mapper.readTree("[".repeat(12) + "0" + "]".repeat(12))).doesNotThrowAnyException();
    }

    @Test void csrfRemainsInstalledAndProtectsAnUnsafeNonExemptMatcher() throws Exception {
        var csrf = security.getFilters().stream().filter(org.springframework.security.web.csrf.CsrfFilter.class::isInstance)
                .map(org.springframework.security.web.csrf.CsrfFilter.class::cast).findFirst().orElseThrow();
        // Isolate the production CSRF filter only to prove that it was not globally disabled.
        var req = new org.springframework.mock.web.MockHttpServletRequest("POST", "/unsafe-probe");
        var res = new org.springframework.mock.web.MockHttpServletResponse();
        req.setAttribute(RequestContext.ATTRIBUTE, new RequestContext(java.util.UUID.randomUUID(), java.time.Instant.parse("2030-01-01T00:00:00Z")));
        var calls = new AtomicInteger();
        csrf.doFilter(req, res, (request, response) -> calls.incrementAndGet());
        assertThat(res.getStatus()).isEqualTo(403); assertThat(calls).hasValue(0);
    }
}
