package com.airwise.backend.flightcomparison.api;

import java.util.*;
import java.util.stream.Stream;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tools.jackson.databind.json.JsonMapper;
import com.airwise.backend.flightprovider.demo.DemoFlightProvider;
import com.airwise.backend.flightsearch.application.DemoCatalog;
import com.airwise.backend.http.ApiException;
import static com.airwise.backend.flightcomparison.domain.ComparisonTypes.*;
import static org.assertj.core.api.Assertions.*;

class ComparisonRequestReaderTests {
    private static ValidatorFactory validation;
    private static ComparisonRequestReader reader;
    private static final JsonMapper JSON=JsonMapper.builder().build();
    private static final String A="of_"+"a".repeat(64),B="of_"+"b".repeat(64);
    private static final String QUERY="{\"tripType\":\"ONE_WAY\",\"origins\":[\" opo \"],\"destination\":\"mad\",\"outboundDate\":\"2027-02-12\",\"travellers\":{\"adults\":1},\"cabin\":\"ECONOMY\"}";
    @BeforeAll static void configure() {
        validation=Validation.buildDefaultValidatorFactory();var provider=new DemoFlightProvider();
        reader=new ComparisonRequestReader(new DemoCatalog(provider.dataset(),provider.airports()),validation.getValidator());
    }
    @AfterAll static void close() { validation.close(); }
    static String base() { return "{\"query\":"+QUERY+",\"datasetVersion\":\"demo-v1\",\"offerIds\":[\""+A+"\",\""+B+"\"]}"; }
    static String add(String fields) { String root=base();return root.substring(0,root.length()-1)+","+fields+"}"; }
    @Test void omissionDefaultsAreExplicitAndQueryValidationIsSharedWithSearch() {
        var request=reader.read(JSON.readTree(base()));
        assertThat(request.primary()).isEqualTo(Primary.FLIGHT_PRICE);assertThat(request.requiredComponents()).isEmpty();assertThat(request.groundEstimates()).isEmpty();
        assertThat(request.query().origins()).containsExactly("OPO");assertThat(request.query().destination()).isEqualTo("MAD");
        assertThat(request.query().returnDate()).isNull();
    }
    static Stream<Arguments> invalid() {
        return Stream.of(
                Arguments.of(add("\"primary\":null"),422,"REQUIRED"),
                Arguments.of(add("\"requiredComponents\":null"),422,"REQUIRED"),
                Arguments.of(add("\"groundEstimates\":null"),422,"REQUIRED"),
                Arguments.of(add("\"primary\":\"KNOWN_SUBTOTAL\""),422,"INVALID_COMBINATION"),
                Arguments.of(add("\"requiredComponents\":[\"ORIGIN_GROUND_OUTBOUND\"]"),422,"INVALID_COMBINATION"),
                Arguments.of(add("\"primary\":\"KNOWN_SUBTOTAL\",\"requiredComponents\":[\"ORIGIN_GROUND_RETURN\"]"),422,"INVALID_COMBINATION"),
                Arguments.of(add("\"primary\":\"KNOWN_SUBTOTAL\",\"requiredComponents\":[\"ORIGIN_GROUND_OUTBOUND\",\"ORIGIN_GROUND_OUTBOUND\"]"),422,"DUPLICATE"),
                Arguments.of(add("\"primary\":1"),400,"TYPE_MISMATCH"),
                Arguments.of(add("\"requiredComponents\":\"ORIGIN_GROUND_OUTBOUND\""),400,"TYPE_MISMATCH"),
                Arguments.of(base().replace("\"datasetVersion\":\"demo-v1\",",""),422,"REQUIRED"),
                Arguments.of(base().replace("\"demo-v1\"","null"),422,"REQUIRED"),
                Arguments.of(base().replace("\""+A+"\"","1"),400,"TYPE_MISMATCH"),
                Arguments.of(base().replace(A,"ob_"+"a".repeat(64)),422,"INVALID_ID")
        );
    }
    @ParameterizedTest @MethodSource("invalid")
    void nullTypeAndCombinationRulesRemainDistinct(String json,int status,String fieldCode) {
        assertThatThrownBy(() -> reader.read(JSON.readTree(json))).isInstanceOfSatisfying(ApiException.class,e -> {
            assertThat(e.status()).isEqualTo(status);assertThat(e.fieldErrors()).isNotEmpty();assertThat(e.fieldErrors().getFirst().code()).isEqualTo(fieldCode);
        });
    }
}
