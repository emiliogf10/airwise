package com.airwise.backend.flightsearch.api;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("demo")
class SearchValidationHttpTests {
    @Autowired MockMvc mvc;
    private static final AtomicInteger IP = new AtomicInteger();
    static String base() { return FlightSearchHttpTests.body("ONE_WAY", 12, "[\"OPO\"]"); }
    static String add(String fields) { String b = base(); return b.substring(0, b.length()-1) + "," + fields + "}"; }
    static Stream<Arguments> invalid() {
        return Stream.of(
                Arguments.of(base().replace("\"OPO\"", "\"VGO\""), 422, "UNKNOWN_AIRPORT"),
                Arguments.of(base().replace("[\"OPO\"]", "[\"opo\",\" OPO \"]"), 422, "DUPLICATE"),
                Arguments.of(base().replace("\"OPO\"", "\"ＯＰＯ\""), 422, "INVALID_FORMAT"),
                Arguments.of(base().replace("\"OPO\"", "\"MAD\""), 422, "INVALID_COMBINATION"),
                Arguments.of(base().replace("2027-02-12", "2027-02-30"), 422, "INVALID_FORMAT"),
                Arguments.of(base().replace("2027-02-12", "2026-12-31"), 422, "OUT_OF_RANGE"),
                Arguments.of(base().replace("ONE_WAY", "ROUND_TRIP"), 422, "REQUIRED"),
                Arguments.of(base().replace("\"adults\":1", "\"adults\":2"), 422, "UNSUPPORTED_VALUE"),
                Arguments.of(base().replace("ECONOMY", "BUSINESS"), 422, "UNSUPPORTED_VALUE"),
                Arguments.of(add("\"sort\":null"), 422, "REQUIRED"),
                Arguments.of(add("\"filters\":null"), 422, "REQUIRED"),
                Arguments.of(add("\"page\":null"), 422, "REQUIRED"),
                Arguments.of(add("\"page\":100001"), 422, "OUT_OF_RANGE"),
                Arguments.of(add("\"pageSize\":0"), 422, "OUT_OF_RANGE"),
                Arguments.of(add("\"page\":1"), 422, "REQUIRED"),
                Arguments.of(add("\"datasetVersion\":\"bad/version\""), 422, "INVALID_ID"),
                Arguments.of(add("\"filters\":{\"origins\":[\"SCQ\"]}"), 422, "INVALID_COMBINATION"),
                Arguments.of(add("\"filters\":{\"airlineCodes\":[\"ZZ\"]}"), 422, "UNSUPPORTED_VALUE"),
                Arguments.of(add("\"filters\":{\"currency\":\"JPY\"}"), 422, "INVALID_CURRENCY"),
                Arguments.of(add("\"filters\":{\"maxPrice\":\"1.00\"}"), 422, "REQUIRED"),
                Arguments.of(add("\"filters\":{\"maxConnections\":7}"), 422, "OUT_OF_RANGE"),
                Arguments.of(add("\"filters\":{\"maxDurationMinutes\":0}"), 422, "OUT_OF_RANGE"),
                Arguments.of(add("\"provider\":\"synthetic-secret\""), 400, ""),
                Arguments.of(base().replace("\"adults\":1", "\"adults\":\"1\""), 400, ""),
                Arguments.of(base().replace("\"ONE_WAY\"", "1"), 400, ""),
                Arguments.of(add("\"page\":1.0"), 400, ""),
                Arguments.of(add("\"page\":1e0"), 400, ""),
                Arguments.of(add("\"page\":0,\"page\":1"), 400, ""),
                Arguments.of(base() + " {}", 400, ""),
                Arguments.of(base().replace("\"cabin\":", "\"token\":\"synthetic-secret\",\"cabin\":"), 400, ""),
                Arguments.of(add("\"filters\":{\"currency\":\"EUR\",\"maxPrice\":1}"), 400, "")
        );
    }
    @ParameterizedTest @MethodSource("invalid")
    void strictInputRejectsWithoutReflectingValues(String json, int status, String fieldCode) throws Exception {
        var result = mvc.perform(post("/api/v1/demo/flight-searches").with(r -> { r.setRemoteAddr("203.0.113."+IP.incrementAndGet()); return r; })
                .contentType("application/json").content(json)).andExpect(status().is(status))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("synthetic-secret"))))
                .andExpect(jsonPath("$.providerResults").isEmpty());
        if (!fieldCode.isEmpty()) result.andExpect(jsonPath("$.fieldErrors[0].code").value(fieldCode));
    }
    static Stream<String> money() { return Stream.of("-1", "+1", "1e2", "01", "1,00", " 1", "1.000", "1000000000", ".5", "1."); }
    @ParameterizedTest @MethodSource("money")
    void lexicalMonetaryPrecisionIsNeverSilentlyRounded(String amount) throws Exception {
        mvc.perform(post("/api/v1/demo/flight-searches").with(r -> { r.setRemoteAddr("203.0.114."+IP.incrementAndGet()); return r; })
                .contentType("application/json").content(add("\"filters\":{\"currency\":\"EUR\",\"maxPrice\":\""+amount+"\"}")))
                .andExpect(status().isUnprocessableContent()).andExpect(jsonPath("$.fieldErrors[0].code").value("INVALID_AMOUNT"));
    }
}
