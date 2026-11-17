package com.airwise.backend.flightcomparison.api;

import java.math.BigDecimal;
import java.util.*;
import jakarta.validation.Validator;
import tools.jackson.databind.JsonNode;
import com.airwise.backend.flightcomparison.domain.*;
import com.airwise.backend.flightsearch.api.SearchRequestReader;
import com.airwise.backend.flightsearch.application.*;
import com.airwise.backend.flightsearch.domain.Money;
import com.airwise.backend.http.ApiException;
import static com.airwise.backend.api.RequestValues.*;
import static com.airwise.backend.flightcomparison.domain.ComparisonTypes.*;
import static com.airwise.backend.flightsearch.domain.FlightTypes.*;

/** Closed HTTP schema; an offer selection can never supply prices or source conditions. */
final class ComparisonRequestReader {
    private static final Set<String> ROOT = Set.of("query", "datasetVersion", "offerIds", "primary", "requiredComponents", "groundEstimates");
    private static final Set<String> ESTIMATE = Set.of("offerId", "component", "money", "basis", "travellerCount", "source", "assumption");
    private final DemoCatalog catalog;
    private final SearchRequestReader queries;
    ComparisonRequestReader(DemoCatalog catalog, Validator validator) {
        this.catalog = catalog; this.queries = new SearchRequestReader(catalog, validator);
    }
    ComparisonRequest read(JsonNode root) {
        object(root, ROOT, Set.of("query", "datasetVersion", "offerIds"), "");
        String version = id(text(root.get("datasetVersion"), "/datasetVersion", false), "/datasetVersion");
        DatasetDescriptor dataset;
        try { dataset = catalog.resolve(version); } catch (DemoCatalog.VersionUnavailable exception) { throw ApiException.datasetUnavailable(); }
        var query = queries.query(root.get("query"), dataset);
        var ids = new ArrayList<String>();
        JsonNode selected = root.get("offerIds");
        array(selected, "/offerIds", 2, 4);
        for (int i = 0; i < selected.size(); i++) {
            String id = offerId(selected.get(i), "/offerIds/" + i);
            if (ids.contains(id)) throw ApiException.validation("/offerIds/" + i, "DUPLICATE");
            ids.add(id);
        }
        var primary = root.has("primary") ? enumeration(root.get("primary"), "/primary", Primary.class) : Primary.FLIGHT_PRICE;
        var required = new ArrayList<GroundComponent>();
        if (root.has("requiredComponents")) {
            JsonNode node = root.get("requiredComponents"); array(node, "/requiredComponents", 0, 2);
            for (int i = 0; i < node.size(); i++) {
                var component = enumeration(node.get(i), "/requiredComponents/" + i, GroundComponent.class);
                if (required.contains(component)) throw ApiException.validation("/requiredComponents/" + i, "DUPLICATE");
                if (component == GroundComponent.ORIGIN_GROUND_RETURN && query.tripType() != TripType.ROUND_TRIP)
                    throw ApiException.validation("/requiredComponents/" + i, "INVALID_COMBINATION");
                required.add(component);
            }
        }
        if ((primary == Primary.KNOWN_SUBTOTAL) != !required.isEmpty())
            throw ApiException.validation("/requiredComponents", "INVALID_COMBINATION");
        var estimates = new ArrayList<GroundTransportEstimate>();
        var estimateKeys = new HashSet<String>();
        if (root.has("groundEstimates")) {
            JsonNode node = root.get("groundEstimates"); array(node, "/groundEstimates", 0, 8);
            if (primary != Primary.KNOWN_SUBTOTAL && !node.isEmpty()) throw ApiException.validation("/groundEstimates", "INVALID_COMBINATION");
            for (int i = 0; i < node.size(); i++) {
                String path = "/groundEstimates/" + i;
                JsonNode estimate = node.get(i); object(estimate, ESTIMATE, ESTIMATE, path);
                String offerId = offerId(estimate.get("offerId"), path + "/offerId");
                if (!ids.contains(offerId)) throw ApiException.validation(path + "/offerId", "UNKNOWN_OFFER_ID");
                var component = enumeration(estimate.get("component"), path + "/component", GroundComponent.class);
                if (component == GroundComponent.ORIGIN_GROUND_RETURN && query.tripType() != TripType.ROUND_TRIP)
                    throw ApiException.validation(path + "/component", "INVALID_COMBINATION");
                if (!estimateKeys.add(offerId + "/" + component)) throw ApiException.validation(path + "/component", "DUPLICATE");
                var basis = enumeration(estimate.get("basis"), path + "/basis", Basis.class);
                if (basis != Basis.GROUP) throw ApiException.validation(path + "/basis", "UNSUPPORTED_VALUE");
                int count = integer(estimate.get("travellerCount"), path + "/travellerCount");
                if (count != 1) throw ApiException.validation(path + "/travellerCount", "UNSUPPORTED_VALUE");
                var source = enumeration(estimate.get("source"), path + "/source", EstimateSource.class);
                var assumption = enumeration(estimate.get("assumption"), path + "/assumption", EstimateAssumption.class);
                Money money = money(estimate.get("money"), path + "/money", dataset);
                estimates.add(new GroundTransportEstimate(offerId, component, money, basis, count, source, assumption));
            }
        }
        return new ComparisonRequest(query, version, ids, primary, required, estimates);
    }
    private static Money money(JsonNode node, String path, DatasetDescriptor dataset) {
        object(node, Set.of("amount", "currency"), Set.of("amount", "currency"), path);
        String currency = text(node.get("currency"), path + "/currency", false);
        var definition = dataset.currencies().stream().filter(c -> c.code().equals(currency)).findFirst()
                .orElseThrow(() -> ApiException.validation(path + "/currency", "INVALID_CURRENCY"));
        String amount = text(node.get("amount"), path + "/amount", false);
        if (!amount.matches("(0|[1-9][0-9]{0,6})(\\.[0-9]+)?")
                || (amount.contains(".") && amount.length() - amount.indexOf('.') - 1 > definition.minorUnits()))
            throw ApiException.validation(path + "/amount", "INVALID_AMOUNT");
        var decimal = new BigDecimal(amount);
        if (decimal.compareTo(new BigDecimal("1000000")) > 0) throw ApiException.validation(path + "/amount", "OUT_OF_RANGE");
        return new Money(decimal, currency);
    }
    private static void array(JsonNode node, String path, int min, int max) {
        if (node == null || node.isNull()) throw ApiException.validation(path, "REQUIRED");
        if (!node.isArray()) throw ApiException.invalidRequest(path, "TYPE_MISMATCH");
        if (node.size() < min || node.size() > max) throw ApiException.validation(path, "OUT_OF_RANGE");
    }
    private static String offerId(JsonNode node, String path) {
        String value = text(node, path, false);
        if (!value.matches("of_[0-9a-f]{64}")) throw ApiException.validation(path, "INVALID_ID");
        return value;
    }
    private static <T extends Enum<T>> T enumeration(JsonNode node, String path, Class<T> type) {
        String value = text(node, path, false);
        try { return Enum.valueOf(type, value); } catch (IllegalArgumentException exception) { throw ApiException.validation(path, "UNSUPPORTED_VALUE"); }
    }
}
