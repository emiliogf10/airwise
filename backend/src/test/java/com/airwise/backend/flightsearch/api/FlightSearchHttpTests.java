package com.airwise.backend.flightsearch.api;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("demo")
class FlightSearchHttpTests {
    @Autowired MockMvc mvc;
    @Autowired JsonMapper mapper;
    private static final AtomicInteger IP = new AtomicInteger();
    static String body(String trip, int day, String origins) {
        return "{\"query\":{\"tripType\":\"" + trip + "\",\"origins\":" + origins
                + ",\"destination\":\"MAD\",\"outboundDate\":\"2027-02-" + day + "\","
                + (trip.equals("ROUND_TRIP") ? "\"returnDate\":\"2027-02-" + (day + 7) + "\"," : "")
                + "\"travellers\":{\"adults\":1},\"cabin\":\"ECONOMY\"}}";
    }
    ResultActions send(String body) throws Exception {
        return mvc.perform(post("/api/v1/demo/flight-searches").with(r -> { r.setRemoteAddr("198.51.100." + IP.incrementAndGet()); return r; })
                .contentType("application/json").content(body));
    }
    @Test void publicOneWayHasExactResponseAndUnknownRemainsNull() throws Exception {
        var result = send(body("ONE_WAY", 10, "[\" opo \"]")).andExpect(status().isOk())
                .andExpect(jsonPath("$.context.query.origins[0]").value("OPO"))
                .andExpect(jsonPath("$.context.dataOrigin").value("DEMO"))
                .andExpect(jsonPath("$.context.evaluationInstant").value("2027-01-01T00:00:00Z"))
                .andExpect(jsonPath("$.offers[0].price.money.amount").value("100.00"))
                .andExpect(jsonPath("$.offers[0].itinerary.journeys[0].segments[0].departureTime.localDateTime").value("2027-02-10T09:00:00"))
                .andExpect(jsonPath("$.offers[0].baggage.checked.status").value("UNKNOWN"))
                .andExpect(jsonPath("$.status").value("COMPLETE")).andReturn();
        JsonNode root = mapper.readTree(result.getResponse().getContentAsByteArray());
        assertThat(root.propertyNames()).containsExactlyInAnyOrder("contractVersion", "correlationId", "context", "status", "offers",
                "priceGroups", "observations", "observationCount", "observationsTruncated", "pagination", "providerResults", "warnings", "errors");
        assertThat(root.at("/context/query/returnDate").isNull()).isTrue();
        assertThat(root.at("/offers/0/seller").isNull()).isTrue();
        assertThat(root.at("/offers/0/baggage/checked/pieces").isNull()).isTrue();
        assertThat(root.at("/offers/0/baggage/checked/weightKg").isNull()).isTrue();
        assertThat(result.getResponse().getHeader("X-Correlation-ID")).isEqualTo(root.get("correlationId").asString());
        assertThat(result.getRequest().getSession(false)).isNull();
    }
    @Test void returnAndSeveralOriginsKeepObservationSeparateAndCanonical() throws Exception {
        send(body("ROUND_TRIP", 10, "[\"scq\",\" opo \"]")).andExpect(status().isOk())
                .andExpect(jsonPath("$.context.query.origins[0]").value("OPO"))
                .andExpect(jsonPath("$.offers.length()").value(2)).andExpect(jsonPath("$.observations.length()").value(1))
                .andExpect(jsonPath("$.observations[0].price.scope.coverage").value("OUTBOUND_ONLY"))
                .andExpect(jsonPath("$.providerResults[1].origin").value("SCQ"));
    }
    @Test void completeEmptyPartialAndFailureHaveDistinctStatuses() throws Exception {
        send(body("ONE_WAY", 14, "[\"OPO\"]")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("COMPLETE"))
                .andExpect(jsonPath("$.offers").isEmpty());
        send(body("ROUND_TRIP", 11, "[\"SCQ\",\"OPO\"]")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PARTIAL"))
                .andExpect(jsonPath("$.errors[0].code").value("PROVIDER_OFFLINE"));
        send(body("ONE_WAY", 19, "[\"OPO\"]")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PARTIAL"));
        send(body("ONE_WAY", 13, "[\"OPO\"]")).andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("PROVIDERS_FAILED"))
                .andExpect(jsonPath("$.providerResults[0].issues[0].code").value("PROVIDER_TIMEOUT"));
    }
    @Test void filtersBeforePagingKeepIdentityAndDoNotMixCurrencyGroups() throws Exception {
        var body = body("ONE_WAY", 12, "[\"OPO\"]");
        JsonNode base = mapper.readTree(send(body).andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray());
        String filtered = body.substring(0, body.length() - 1) + ",\"filters\":{\"currency\":\"EUR\",\"maxPrice\":\"100.00\"},\"sort\":\"PRICE_ASC\",\"pageSize\":2}";
        JsonNode page = mapper.readTree(send(filtered).andExpect(status().isOk()).andExpect(jsonPath("$.pagination.baseCount").value(8))
                .andExpect(jsonPath("$.pagination.filteredCount").value(4)).andExpect(jsonPath("$.pagination.excludedUnknownCount").value(1))
                .andExpect(jsonPath("$.pagination.hasMore").value(true)).andExpect(jsonPath("$.offers[0].price.money.amount").value("0.00"))
                .andReturn().getResponse().getContentAsByteArray());
        assertThat(page.at("/context/fingerprint").asString()).isEqualTo(base.at("/context/fingerprint").asString());
        assertThat(page.get("priceGroups").size()).isEqualTo(1);
        send(filtered.substring(0, filtered.length()-1) + ",\"datasetVersion\":\"demo-v1\",\"page\":1}")
                .andExpect(status().isOk()).andExpect(jsonPath("$.offers[0].price.money.amount").value("100.00"))
                .andExpect(jsonPath("$.pagination.hasMore").value(false));
    }
}
