package com.airwise.backend.flightprovider.demo;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

class DemoCorpusValidationTests {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2030-01-01T00:00:00Z"), ZoneOffset.UTC);

    private String corpus() throws IOException {
        try (var input = DemoFlightProvider.class.getResourceAsStream(DemoCorpus.RESOURCE)) {
            assertNotNull(input, "The versioned corpus must exist");
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private DemoFlightProvider load(String source) {
        return new DemoFlightProvider(CLOCK, new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8)));
    }

    private Properties properties(String source) throws IOException {
        var p = new Properties();
        p.load(new StringReader(source));
        return p;
    }

    private String replace(String source, String key, String value) {
        assertTrue(source.lines().anyMatch(line -> line.startsWith(key + "=")), "Fixture property " + key);
        return source.lines().map(line -> line.startsWith(key + "=") ? key + "=" + value : line)
                .collect(Collectors.joining("\n", "", "\n"));
    }

    private String scenarioKey(Properties p, String prefix) {
        return p.stringPropertyNames().stream().filter(k -> k.startsWith("scenario.") && p.getProperty(k).startsWith(prefix))
                .findFirst().orElseThrow();
    }

    private void invalid(String source) {
        var error = assertThrows(IllegalArgumentException.class, () -> load(source));
        assertEquals("Invalid demo dataset", error.getMessage());
    }

    @Test
    void missingMalformedDuplicateAndUnexpectedCorpusFieldsFailExplicitly() throws IOException {
        assertThrows(IllegalArgumentException.class, () -> new DemoFlightProvider(CLOCK, null));
        invalid("this-is-not-a-dataset=true\n");
        var source = corpus();
        invalid(source + "\ndataOrigin=DEMO\n");
        invalid(source + "\nexternalHost=synthetic.invalid\n");
        invalid(replace(source, "dataOrigin", "REAL"));
        invalid(replace(source, "currencies", "EUR:3,USD:2"));
        var p = properties(source);
        var ids = p.getProperty("scenarios");
        invalid(replace(source, "scenarios", ids + "," + ids.split(",")[0]));
    }

    @Test
    void invalidUtf8AndOversizedCorpusAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new DemoFlightProvider(CLOCK, new ByteArrayInputStream(new byte[] {(byte) 0xC3, (byte) 0x28})));
        invalid("#" + "x".repeat(65536));
    }

    @Test
    void fixtureMoneyCannotBeRoundedOrAssignedUnregisteredCurrency() throws IOException {
        var source = corpus();
        var p = properties(source);
        var row = p.getProperty("record.opo-mad-oneway.price").split("\\|", -1);
        row[0] = "100.000";
        invalid(replace(source, "record.opo-mad-oneway.price", String.join("|", row)));
        row[0] = "1e2";
        invalid(replace(source, "record.opo-mad-oneway.price", String.join("|", row)));
        row[0] = "100";
        row[1] = "GBP";
        invalid(replace(source, "record.opo-mad-oneway.price", String.join("|", row)));
        row[1] = "EUR";
        row[7] = "2027-01-01T00:00:01Z";
        invalid(replace(source, "record.opo-mad-oneway.price", String.join("|", row)));
    }

    @Test
    void recordAndScenarioDuplicatesDoNotDisappearDuringLoading() throws IOException {
        var source = corpus();
        var p = properties(source);
        var key = scenarioKey(p, "ONE_WAY|2027-02-10||OPO|MAD|");
        var row = p.getProperty(key).split("\\|", -1);
        row[8] = "opo-mad-oneway,opo-mad-oneway";
        invalid(replace(source, key, String.join("|", row)));
        row[8] = "missing-record";
        invalid(replace(source, key, String.join("|", row)));
        row[8] = "opo-mad-oneway";
        row[6] = "0"; // Incomplete execution without an operational cause is not an empty success.
        invalid(replace(source, key, String.join("|", row)));
    }

    @Test
    void contradictoryRouteDatesChronologyAndRoundTripSegmentIdentityRejectWholeCorpus() throws IOException {
        var source = corpus();
        var p = properties(source);
        var key = "record.opo-mad-oneway.outbound";
        var row = p.getProperty(key).split(",", -1);
        row[2] = "XXX";
        invalid(replace(source, key, String.join(",", row)));
        row = p.getProperty(key).split(",", -1);
        row[3] = row[3].replace("2027-02-10", "2027-02-09");
        invalid(replace(source, key, String.join(",", row)));
        row = p.getProperty(key).split(",", -1);
        row[4] = "2027-02-10T08:00:00";
        invalid(replace(source, key, String.join(",", row)));
        var outbound = p.getProperty("record.opo-mad-roundtrip.outbound").split(",", -1);
        var inbound = p.getProperty("record.opo-mad-roundtrip.inbound").split(",", -1);
        inbound[0] = outbound[0];
        invalid(replace(source, "record.opo-mad-roundtrip.inbound", String.join(",", inbound)));
    }

    @Test
    void moreThanTwentyDistinctOffersOrObservationsAreRejectedByResultLimit() throws IOException {
        assertResultLimit("opo-mad-oneway", "ONE_WAY|2027-02-10||OPO|MAD|");
        assertResultLimit("opo-outbound-observed", "ROUND_TRIP|2027-02-10|2027-02-17|OPO|MAD|");
    }

    private void assertResultLimit(String original, String scenarioPrefix) throws IOException {
        var source = corpus();
        var p = properties(source);
        var key = scenarioKey(p, scenarioPrefix);
        var row = p.getProperty(key).split("\\|", -1);
        var clones = IntStream.range(1, 21).mapToObj(i -> "extra-" + i).toList();
        row[8] += "," + String.join(",", clones);
        var mutated = new StringBuilder(replace(source, key, String.join("|", row)));
        var recordPrefix = "record." + original + ".";
        for (var clone : clones) {
            for (var property : p.stringPropertyNames().stream().sorted().filter(k -> k.startsWith(recordPrefix)).toList()) {
                mutated.append(property.replace(recordPrefix, "record." + clone + ".")).append('=')
                        .append(p.getProperty(property)).append('\n');
            }
        }
        var error = assertThrows(IllegalArgumentException.class, () -> load(mutated.toString()));
        assertNotNull(error.getCause());
        assertEquals("Scenario exceeds result bounds", error.getCause().getMessage());
    }
}
