package com.airwise.backend.flightsearch.api;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.*;
import jakarta.validation.Validator;
import tools.jackson.databind.JsonNode;
import com.airwise.backend.api.RequestValues;
import com.airwise.backend.flightsearch.application.*;
import com.airwise.backend.flightsearch.domain.*;
import com.airwise.backend.http.ApiException;
import static com.airwise.backend.api.RequestValues.*;
import static com.airwise.backend.flightsearch.domain.FlightTypes.*;

/** HTTP-only parsing preserves omitted/default/null distinctions before creating domain values. */
final class SearchRequestReader {
    private static final Set<String> ROOT = Set.of("query", "datasetVersion", "filters", "sort", "page", "pageSize");
    private static final Set<String> QUERY = Set.of("tripType", "origins", "destination", "outboundDate", "returnDate", "travellers", "cabin");
    private static final Set<String> FILTERS = Set.of("origins", "airlineCodes", "maxConnections", "maxDurationMinutes", "currency", "maxPrice");
    private final DemoCatalog catalog;
    private final Validator validator;
    SearchRequestReader(DemoCatalog catalog, Validator validator) { this.catalog = catalog; this.validator = validator; }

    SearchView read(JsonNode root) {
        object(root, ROOT, Set.of("query"), "");
        int page = root.has("page") ? integer(root.get("page"), "/page") : 0;
        int size = root.has("pageSize") ? integer(root.get("pageSize"), "/pageSize") : 20;
        new RequestValues.Page(page, size).validate(validator);
        String version = id(text(root.get("datasetVersion"), "/datasetVersion", true), "/datasetVersion");
        if (page > 0 && version == null) throw ApiException.validation("/datasetVersion", "REQUIRED");
        DatasetDescriptor dataset;
        try { dataset = catalog.resolve(version); } catch (DemoCatalog.VersionUnavailable exception) { throw ApiException.datasetUnavailable(); }
        FlightSearchRequest query = query(root.get("query"), dataset);
        var filters = root.has("filters") ? filters(root.get("filters"), query, dataset) : SearchView.Filters.none();
        var sort = root.has("sort") ? enumeration(root.get("sort"), "/sort", SearchView.Sort.class) : SearchView.Sort.DEFAULT;
        return new SearchView(query, dataset.datasetVersion(), filters, sort, page, size);
    }

    private FlightSearchRequest query(JsonNode node, DatasetDescriptor dataset) {
        object(node, QUERY, Set.of("tripType", "origins", "destination", "outboundDate", "travellers", "cabin"), "/query");
        var trip = enumeration(node.get("tripType"), "/query/tripType", TripType.class);
        var origins = codes(node.get("origins"), "/query/origins", 1, 3, true);
        String destination = code(text(node.get("destination"), "/query/destination", false), "/query/destination", true);
        if (origins.contains(destination)) throw ApiException.validation("/query/destination", "INVALID_COMBINATION");
        LocalDate outbound = date(node.get("outboundDate"), "/query/outboundDate", dataset);
        LocalDate returning = null;
        if (trip == TripType.ROUND_TRIP) {
            returning = date(node.get("returnDate"), "/query/returnDate", dataset);
            if (returning.isBefore(outbound)) throw ApiException.validation("/query/returnDate", "INVALID_COMBINATION");
        } else if (node.hasNonNull("returnDate")) {
            // A scalar with the wrong token type is still a structural error.
            text(node.get("returnDate"), "/query/returnDate", false);
            throw ApiException.validation("/query/returnDate", "INVALID_COMBINATION");
        }
        object(node.get("travellers"), Set.of("adults"), Set.of("adults"), "/query/travellers");
        int adults = integer(node.get("travellers").get("adults"), "/query/travellers/adults");
        if (adults != 1) throw ApiException.validation("/query/travellers/adults", "UNSUPPORTED_VALUE");
        Cabin cabin = enumeration(node.get("cabin"), "/query/cabin", Cabin.class);
        return new FlightSearchRequest(trip, origins, destination, outbound, returning, new Travellers(adults), cabin);
    }

    private SearchView.Filters filters(JsonNode node, FlightSearchRequest query, DatasetDescriptor dataset) {
        object(node, FILTERS, Set.of(), "/filters");
        var origins = node.has("origins") ? codes(node.get("origins"), "/filters/origins", 0, 3, true) : List.<String>of();
        if (!query.origins().containsAll(origins)) throw ApiException.validation("/filters/origins", "INVALID_COMBINATION");
        var airlines = node.has("airlineCodes") ? codes(node.get("airlineCodes"), "/filters/airlineCodes", 0, 16, false) : List.<String>of();
        if (!dataset.airlineCodes().containsAll(airlines)) throw ApiException.validation("/filters/airlineCodes", "UNSUPPORTED_VALUE");
        Integer connections = optionalInteger(node.get("maxConnections"), "/filters/maxConnections", 0, 6);
        Integer minutes = optionalInteger(node.get("maxDurationMinutes"), "/filters/maxDurationMinutes", 1, 10080);
        String currency = text(node.get("currency"), "/filters/currency", true);
        if (currency != null && dataset.currencies().stream().noneMatch(c -> c.code().equals(currency)))
            throw ApiException.validation("/filters/currency", "INVALID_CURRENCY");
        String amount = text(node.get("maxPrice"), "/filters/maxPrice", true);
        BigDecimal maximum = null;
        if (amount != null) {
            if (currency == null) throw ApiException.validation("/filters/currency", "REQUIRED");
            int scale = dataset.currencies().stream().filter(c -> c.code().equals(currency)).findFirst().orElseThrow().minorUnits();
            if (!amount.matches("(0|[1-9][0-9]{0,8})(\\.[0-9]+)?")
                    || (amount.contains(".") && amount.length() - amount.indexOf('.') - 1 > scale))
                throw ApiException.validation("/filters/maxPrice", "INVALID_AMOUNT");
            maximum = new BigDecimal(amount);
        }
        return new SearchView.Filters(origins, airlines, connections, minutes, currency, maximum);
    }

    private Integer optionalInteger(JsonNode node, String path, int min, int max) {
        if (node == null || node.isNull()) return null;
        int value = integer(node, path);
        if (value < min || value > max) throw ApiException.validation(path, "OUT_OF_RANGE");
        return value;
    }
    private List<String> codes(JsonNode node, String path, int min, int max, boolean airport) {
        if (node == null || node.isNull()) throw ApiException.validation(path, "REQUIRED");
        if (!node.isArray()) throw ApiException.invalidRequest();
        if (node.size() < min || node.size() > max) throw ApiException.validation(path, "OUT_OF_RANGE");
        var result = new ArrayList<String>();
        for (int i = 0; i < node.size(); i++) {
            String code = code(text(node.get(i), path + "/" + i, false), path + "/" + i, airport);
            if (result.contains(code)) throw ApiException.validation(path + "/" + i, "DUPLICATE");
            result.add(code);
        }
        return result.stream().sorted().toList();
    }
    private String code(String value, String path, boolean airport) {
        // trim() would also remove tabs/control characters; only U+0020 is normalizable here.
        String normalized = value.replaceAll("^ +| +$", "");
        if (!normalized.matches(airport ? "[A-Za-z]{3}" : "[A-Za-z0-9]{2}")) throw ApiException.validation(path, "INVALID_FORMAT");
        normalized = normalized.toUpperCase(Locale.ROOT);
        String result = normalized;
        if (airport && catalog.airports().stream().noneMatch(a -> a.code().equals(result))) throw ApiException.validation(path, "UNKNOWN_AIRPORT");
        return normalized;
    }
    private LocalDate date(JsonNode node, String path, DatasetDescriptor dataset) {
        String value = text(node, path, false);
        if (!value.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) throw ApiException.validation(path, "INVALID_FORMAT");
        LocalDate date;
        try { date = LocalDate.parse(value); } catch (DateTimeException exception) { throw ApiException.validation(path, "INVALID_FORMAT"); }
        if (!dataset.contains(date)) throw ApiException.validation(path, "OUT_OF_RANGE");
        return date;
    }
    private <T extends Enum<T>> T enumeration(JsonNode node, String path, Class<T> type) {
        String value = text(node, path, false);
        try { return Enum.valueOf(type, value); } catch (IllegalArgumentException exception) { throw ApiException.validation(path, "UNSUPPORTED_VALUE"); }
    }
}
