package com.airwise.backend.flightsearch.application;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.stream.Collectors;
import com.airwise.backend.flightsearch.domain.*;
import static com.airwise.backend.flightsearch.domain.DomainChecks.*;

/** airwise-v1 canonical bytes are independent of HTTP serialization settings and operational clocks. */
public final class StableIdentity {
    private StableIdentity() {}

    public static String canonicalPayload(String datasetVersion, FlightSearchRequest query) {
        id(datasetVersion); present(query);
        return "{\"contractVersion\":\"airwise-v1\",\"datasetVersion\":" + quote(datasetVersion)
                + ",\"query\":{\"tripType\":" + quote(query.tripType().name())
                + ",\"origins\":[" + query.origins().stream().map(StableIdentity::quote).collect(Collectors.joining(","))
                + "],\"destination\":" + quote(query.destination())
                + ",\"outboundDate\":" + quote(query.outboundDate().toString())
                + ",\"returnDate\":" + (query.returnDate() == null ? "null" : quote(query.returnDate().toString()))
                + ",\"travellers\":{\"adults\":" + query.travellers().adults() + "},\"cabin\":" + quote(query.cabin().name()) + "}}";
    }

    public static String fingerprint(String datasetVersion, FlightSearchRequest query) {
        return "q_" + hash(canonicalPayload(datasetVersion, query));
    }

    public static String offerId(String fingerprint, String recordKey) {
        id(recordKey); return "of_" + hash("offer\n" + fingerprint + "\n" + recordKey);
    }

    public static String observationId(String fingerprint, String recordKey) {
        id(recordKey); return "ob_" + hash("observation\n" + fingerprint + "\n" + recordKey);
    }

    public static String groupId(String fingerprint, FlightTypes.DataOrigin origin, PriceInformation price,
            BaggageInformation baggage) {
        return "g_" + hash("{\"fingerprint\":" + quote(fingerprint)
                + ",\"dataOrigin\":" + quote(origin.name()) + ",\"evidenceKind\":" + quote(price.evidenceKind().name())
                + ",\"currency\":" + quote(price.money() == null ? null : price.money().currency())
                + ",\"scope\":" + scope(price.scope()) + ",\"taxes\":" + quote(price.taxes().name())
                + ",\"baggage\":{\"cabin\":" + allowance(baggage.cabin()) + ",\"checked\":" + allowance(baggage.checked()) + "}"
                + ",\"additionalCostKinds\":[" + price.additionalCosts().stream().sorted(java.util.Comparator.comparing(c -> c.kind().name()))
                    .map(c -> "{\"kind\":" + quote(c.kind().name()) + ",\"includedInQuotedPrice\":" + c.includedInQuotedPrice()
                        + ",\"currency\":" + quote(c.money() == null ? null : c.money().currency()) + "}")
                    .collect(Collectors.joining(",")) + "]}");
    }

    private static String scope(PriceScope s) {
        return "{\"basis\":" + quote(s.basis().name()) + ",\"travellerCount\":" + s.travellerCount()
                + ",\"cabin\":" + quote(s.cabin() == null ? null : s.cabin().name())
                + ",\"coverage\":" + quote(s.coverage().name()) + ",\"coveredSegmentIds\":["
                + s.coveredSegmentIds().stream().map(StableIdentity::quote).collect(Collectors.joining(",")) + "]}";
    }

    private static String allowance(BaggageAllowance a) {
        return "{\"status\":" + quote(a.status().name()) + ",\"pieces\":" + a.pieces()
                + ",\"weightKg\":" + quote(a.weightKg() == null ? null : a.weightKg().stripTrailingZeros().toPlainString()) + "}";
    }

    private static String quote(String value) {
        if (value == null) return "null";
        require(value.matches("[A-Za-z0-9._-]+"), "Noncanonical identity scalar");
        return "\"" + value + "\"";
    }

    private static String hash(String text) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8))); }
        catch (NoSuchAlgorithmException e) { throw new IllegalStateException("SHA-256 unavailable", e); }
    }
}
