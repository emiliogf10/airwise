package com.airwise.backend.http;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import jakarta.servlet.DispatcherType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "airwise.demo.cors.allowed-origins=http://localhost:5173")
@AutoConfigureMockMvc
@ActiveProfiles("demo")
class ComparisonHttpSecurityTests {
    private static final String PATH = "/api/v1/demo/comparisons";
    private static final String QUERY = "{\"tripType\":\"ONE_WAY\",\"origins\":[\"OPO\"],\"destination\":\"MAD\","
            + "\"outboundDate\":\"2027-02-12\",\"travellers\":{\"adults\":1},\"cabin\":\"ECONOMY\"}";
    private static final AtomicInteger IP = new AtomicInteger();
    @Autowired MockMvc mvc;
    @Autowired JsonMapper mapper;

    private static MockHttpServletRequestBuilder external(MockHttpServletRequestBuilder request) {
        return request.with(r -> { r.setRemoteAddr("198.18.1." + IP.incrementAndGet()); return r; });
    }
    private ResultActions send(String json) throws Exception {
        return mvc.perform(external(post(PATH)).contentType("application/json").content(json));
    }
    private JsonNode search(String extra) throws Exception {
        var result = mvc.perform(external(post("/api/v1/demo/flight-searches")).contentType("application/json")
                .content("{\"query\":" + QUERY + extra + "}")).andExpect(status().isOk()).andReturn();
        return mapper.readTree(result.getResponse().getContentAsByteArray());
    }
    private List<String> ids() throws Exception {
        var ids = new ArrayList<String>();
        search("").get("offers").forEach(o -> ids.add(o.get("offerId").asString()));
        return ids;
    }
    private String request(List<String> ids) {
        return "{\"query\":" + QUERY + ",\"datasetVersion\":\"demo-v1\",\"offerIds\":" + mapper.writeValueAsString(ids) + "}";
    }
    private String add(String json, String fields) { return json.substring(0, json.length() - 1) + "," + fields + "}"; }

    @Test void exactPublicPostAcceptsValidSelectionWithoutCsrfSessionOrClientAuthority() throws Exception {
        var json = request(ids().subList(0, 2));
        var result = mvc.perform(external(post(PATH)).contentType("application/json").content(json)
                .header("Authorization", "Basic c3ludGhldGljOnN5bnRoZXRpYw==").header("X-Correlation-ID", "client-controlled")
                .cookie(new jakarta.servlet.http.Cookie("JSESSIONID", "synthetic")))
                .andExpect(status().isOk()).andExpect(header().doesNotExist("Set-Cookie"))
                .andExpect(header().doesNotExist("WWW-Authenticate")).andExpect(header().string("Cache-Control", "no-store")).andReturn();
        var response = mapper.readTree(result.getResponse().getContentAsByteArray());
        assertThat(response.propertyNames()).containsExactlyInAnyOrder("contractVersion", "correlationId", "context", "primary", "requiredComponents", "groups", "warnings");
        assertThat(response.get("correlationId").asString()).isEqualTo(result.getResponse().getHeader("X-Correlation-ID")).isNotEqualTo("client-controlled");
        assertThat(response.at("/context/dataOrigin").asString()).isEqualTo("DEMO");
        assertThat(result.getRequest().getSession(false)).isNull();
        int rows = 0;
        for (var group : response.get("groups")) rows += group.get("rows").size();
        assertThat(rows).isEqualTo(2);
    }

    @Test void idsFromDifferentPagesAreReconstructedWithoutClientFilters() throws Exception {
        String filters = ",\"filters\":{\"currency\":\"EUR\",\"maxPrice\":\"100.00\"},\"sort\":\"PRICE_ASC\",\"pageSize\":1";
        String first = search(filters).at("/offers/0/offerId").asString();
        String second = search(filters + ",\"page\":1,\"datasetVersion\":\"demo-v1\"").at("/offers/0/offerId").asString();
        var result = mapper.readTree(send(request(List.of(second, first))).andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray());
        assertThat(result.get("groups").size()).isEqualTo(1);
        assertThat(result.at("/groups/0/rows/0/flightPrice/amount").asString()).isEqualTo("0.00");
        assertThat(result.at("/groups/0/recommendationIds/0").asString()).isEqualTo(first);
    }

    @Test void selectionCountAndMembershipAreValidatedBeforeRanking() throws Exception {
        var ids = ids();
        send(request(ids.subList(0, 1))).andExpect(status().isUnprocessableContent());
        send(request(ids.subList(0, 5))).andExpect(status().isUnprocessableContent());
        send(request(List.of(ids.getFirst(), ids.getFirst()))).andExpect(status().isUnprocessableContent());
        send(request(List.of(ids.getFirst(), "of_" + "0".repeat(64))))
                .andExpect(status().isUnprocessableContent()).andExpect(jsonPath("$.fieldErrors[0].code").value("UNKNOWN_OFFER_ID"));
        var other = mvc.perform(external(post("/api/v1/demo/flight-searches")).contentType("application/json")
                .content("{\"query\":" + QUERY.replace("2027-02-12", "2027-02-10") + "}"))
                .andExpect(status().isOk()).andReturn();
        String foreign = mapper.readTree(other.getResponse().getContentAsByteArray()).at("/offers/0/offerId").asString();
        send(request(List.of(ids.getFirst(), foreign))).andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.fieldErrors[0].code").value("UNKNOWN_OFFER_ID"));
        send(request(ids.subList(0, 2)).replace("demo-v1", "retired-v0"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("DATASET_VERSION_UNAVAILABLE"));
    }

    @ParameterizedTest @ValueSource(strings = {"fingerprint", "filters", "page", "price", "provider", "asOf", "token"})
    void injectedSelectionAuthorityIsRejectedWithoutEcho(String name) throws Exception {
        send(add(request(ids().subList(0, 2)), "\"" + name + "\":\"synthetic-secret\""))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("synthetic-secret"))));
    }

    @Test void comparisonCorsIsRestrictedAndErrorsKeepAllowedOrigin() throws Exception {
        mvc.perform(external(options(PATH)).header("Origin", "http://localhost:5173").header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "Content-Type")).andExpect(status().isOk()).andExpect(content().string(""))
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
        mvc.perform(external(options(PATH)).header("Origin", "http://localhost:5173").header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "Authorization")).andExpect(status().isForbidden());
        mvc.perform(external(post(PATH)).header("Origin", "http://localhost:5173").contentType("application/json").content("{}"))
                .andExpect(status().isUnprocessableContent()).andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
        mvc.perform(external(post(PATH)).header("Origin", "null").contentType("application/json").content("{}"))
                .andExpect(status().isForbidden()).andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test void groundEstimatesRemainExplicitAndMissingCostsDoNotBecomeZero() throws Exception {
        var availableIds = new ArrayList<String>();
        search(",\"filters\":{\"currency\":\"EUR\",\"maxPrice\":\"100.00\"}").get("offers")
                .forEach(offer -> availableIds.add(offer.get("offerId").asString()));
        var ids = availableIds.subList(0, 2);
        String base = add(request(ids), "\"primary\":\"KNOWN_SUBTOTAL\",\"requiredComponents\":[\"ORIGIN_GROUND_OUTBOUND\"]");
        var missing = mapper.readTree(send(base).andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray());
        for (var group : missing.get("groups")) {
            assertThat(group.get("recommendationIds").size()).isZero();
            for (var row : group.get("rows")) {
                assertThat(row.get("knownSubtotal").isNull()).isTrue();
                assertThat(row.get("rank").isNull()).isTrue();
                assertThat(row.get("primaryKnown").asBoolean()).isFalse();
            }
        }
        String estimates = "[" + estimate(ids.get(0), "20.00", "EUR") + "," + estimate(ids.get(1), "30.00", "EUR") + "]";
        var complete = mapper.readTree(send(add(base, "\"groundEstimates\":" + estimates))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray());
        int rows = 0;
        for (var group : complete.get("groups")) for (var row : group.get("rows")) {
            rows++;
            assertThat(row.get("knownSubtotal").isObject()).isTrue();
            assertThat(row.get("groundEstimates").size()).isEqualTo(1);
            assertThat(row.at("/groundEstimates/0/source").asString()).isEqualTo("USER_ESTIMATE");
            assertThat(row.at("/groundEstimates/0/assumption").asString()).isEqualTo("USER_PROVIDED");
        }
        assertThat(rows).isEqualTo(2);
    }

    @Test void invalidGroundLexemesScopeCurrencyAndDuplicateComponentsAreRejected() throws Exception {
        var offers = search(",\"filters\":{\"currency\":\"EUR\",\"maxPrice\":\"100.00\"}").get("offers");
        var ids = List.of(offers.get(0).get("offerId").asString(), offers.get(1).get("offerId").asString());
        String base = add(request(ids), "\"primary\":\"KNOWN_SUBTOTAL\",\"requiredComponents\":[\"ORIGIN_GROUND_OUTBOUND\"]");
        for (String amount : new String[]{"-1", "+1", "01", "1e2", "1.000", " 1", "1000000.01"})
            send(add(base, "\"groundEstimates\":[" + estimate(ids.getFirst(), amount, "EUR") + "]"))
                    .andExpect(status().isUnprocessableContent()).andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
        String valid = estimate(ids.getFirst(), "20.00", "EUR");
        for (String invalid : new String[]{valid.replace("EUR", "USD"), valid.replace("GROUP", "PER_PERSON"),
                valid.replace("\"travellerCount\":1", "\"travellerCount\":2"), valid.replace("USER_ESTIMATE", "SELLER"),
                valid.replace("USER_PROVIDED", "synthetic-secret"), valid.replace("ORIGIN_GROUND_OUTBOUND", "ORIGIN_GROUND_RETURN")})
            send(add(base, "\"groundEstimates\":[" + invalid + "]")).andExpect(status().isUnprocessableContent())
                    .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("synthetic-secret"))));
        send(add(base, "\"groundEstimates\":[" + valid + "," + valid + "]")).andExpect(status().isUnprocessableContent());
        send(add(base, "\"groundEstimates\":[" + valid.replace("\"amount\":\"20.00\"", "\"amount\":20.00") + "]"))
                .andExpect(status().isBadRequest());
    }

    private String estimate(String id, String amount, String currency) {
        return "{\"offerId\":\"" + id + "\",\"component\":\"ORIGIN_GROUND_OUTBOUND\",\"money\":{\"amount\":\"" + amount
                + "\",\"currency\":\"" + currency + "\"},\"basis\":\"GROUP\",\"travellerCount\":1,\"source\":\"USER_ESTIMATE\",\"assumption\":\"USER_PROVIDED\"}";
    }

    @Test void wrongMethodsPathsDispatchersAndContentFormatsStayProtected() throws Exception {
        for (var req : new MockHttpServletRequestBuilder[]{get(PATH), head(PATH), put(PATH), delete(PATH), options(PATH),
                post(PATH + "/"), post(PATH + "/extra"), post(PATH + ".json")})
            mvc.perform(external(req)).andExpect(status().isForbidden()).andExpect(header().doesNotExist("Set-Cookie"));
        for (var dispatcher : new DispatcherType[]{DispatcherType.FORWARD, DispatcherType.INCLUDE, DispatcherType.ASYNC})
            mvc.perform(external(post(PATH)).with(r -> { r.setDispatcherType(dispatcher); return r; }).contentType("application/json").content("{}"))
                    .andExpect(status().isForbidden());
        mvc.perform(external(post(PATH)).contentType("text/plain").content("{}"))
                .andExpect(status().isUnsupportedMediaType());
        mvc.perform(external(post(PATH)).contentType("application/json").content(" ".repeat(65537)))
                .andExpect(status().isPayloadTooLarge());
        send("{\"query\":null,\"query\":null}").andExpect(status().isBadRequest());
        send("{} {}").andExpect(status().isBadRequest());
    }
}
