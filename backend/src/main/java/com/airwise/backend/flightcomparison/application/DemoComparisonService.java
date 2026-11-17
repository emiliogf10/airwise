package com.airwise.backend.flightcomparison.application;

import java.util.List;
import java.util.TreeMap;
import com.airwise.backend.flightcomparison.domain.*;
import com.airwise.backend.flightsearch.application.*;
import com.airwise.backend.flightsearch.domain.*;
import com.airwise.backend.flightsearch.domain.FlightTypes.ProviderIssueCode;

/** Rebuilds the complete server-owned demo set, never a stored search or a client-supplied price. */
public final class DemoComparisonService {
    private final DemoSearchService search;
    private final DemoCatalog catalog;
    private final ComparisonEngine engine;
    public DemoComparisonService(DemoSearchService search, DemoCatalog catalog, ComparisonEngine engine) {
        this.search = search; this.catalog = catalog; this.engine = engine;
    }
    public record Result(String fingerprint, ComparisonResult comparison, List<DataIssue> warnings, List<ProviderIssueCode> providerIssueCodes) {}
    public static final class SelectionInvalid extends RuntimeException {
        private final String path, code;
        SelectionInvalid(String path, String code) { this.path = path; this.code = code; }
        public String path() { return path; }
        public String code() { return code; }
    }
    public Result compare(ComparisonRequest request, ProviderExecutionContext context) {
        var dataset = catalog.resolve(request.datasetVersion());
        var reconstructed = search.reconstruct(request.query(), request.datasetVersion(), context);
        var offers = new TreeMap<String, FlightOffer>();
        reconstructed.offers().forEach(o -> offers.put(o.offerId(), o));
        var selected = new java.util.ArrayList<FlightOffer>();
        for (int i = 0; i < request.offerIds().size(); i++) {
            FlightOffer offer = offers.get(request.offerIds().get(i));
            if (offer == null) throw new SelectionInvalid("/offerIds/" + i, "UNKNOWN_OFFER_ID");
            selected.add(offer);
        }
        for (int i = 0; i < request.groundEstimates().size(); i++) {
            var estimate = request.groundEstimates().get(i);
            var price = offers.get(estimate.offerId()).price().money();
            if (price != null && !price.currency().equals(estimate.money().currency()))
                throw new SelectionInvalid("/groundEstimates/" + i + "/money/currency", "INVALID_COMBINATION");
        }
        var warnings = new TreeMap<String, DataIssue>();
        selected.forEach(o -> o.warnings().forEach(w -> warnings.putIfAbsent((w.path() == null ? "" : w.path()) + "/" + w.code(), w)));
        List<ProviderIssueCode> issueCodes = reconstructed.providers().stream().flatMap(p -> p.issues().stream())
                .map(ProviderIssue::code).distinct().sorted(java.util.Comparator.comparing(Enum::name)).toList();
        return new Result(reconstructed.fingerprint(), engine.compare(request, selected, dataset.evaluationInstant()),
                warnings.values().stream().limit(20).toList(), issueCodes);
    }
}
