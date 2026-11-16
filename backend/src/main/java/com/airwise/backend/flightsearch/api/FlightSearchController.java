package com.airwise.backend.flightsearch.api;

import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Validator;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;
import com.airwise.backend.api.NormalizedViews;
import com.airwise.backend.flightsearch.application.*;
import com.airwise.backend.flightsearch.domain.FlightOffer;
import com.airwise.backend.http.*;

@RestController
@Profile("demo")
public final class FlightSearchController {
    private final DemoSearchService service;
    private final DemoCatalog catalog;
    private final SearchRequestReader reader;
    public FlightSearchController(DemoSearchService service, DemoCatalog catalog, Validator validator) {
        this.service = service; this.catalog = catalog; this.reader = new SearchRequestReader(catalog, validator);
    }

    @PostMapping(value = "/api/v1/demo/flight-searches", produces = "application/json")
    public SearchResponse search(@RequestBody JsonNode body, HttpServletRequest request) {
        SearchView view = reader.read(body);
        var context = RequestContext.current(request);
        DemoSearchService.Result result;
        try { result = service.search(view, new ProviderExecutionContext(context.correlationId(), context.deadlineAt())); }
        catch (DemoSearchService.Unavailable exception) {
            var summaries = exception.results().stream().map(ProviderSummary::from).toList();
            throw exception.capability() ? ApiException.capabilityUnavailable(summaries) : ApiException.providersFailed(summaries);
        }
        var groups = new TreeMap<String, PriceGroup>();
        result.offers().forEach(o -> groups.putIfAbsent(o.comparisonGroupId(), group(o)));
        return new SearchResponse("airwise-v1", context.correlationId().toString(),
                new SearchContext(view.datasetVersion(), result.fingerprint(), NormalizedViews.query(view.query()), "DEMO", catalog.dataset().evaluationInstant().toString()),
                result.partial() ? "PARTIAL" : "COMPLETE", result.offers().stream().map(NormalizedViews::offer).toList(),
                List.copyOf(groups.values()), result.observations().stream().map(NormalizedViews::observation).toList(),
                result.observationCount(), result.observationsTruncated(),
                new Pagination(view.page(), view.pageSize(), result.baseCount(), result.filteredCount(), result.excludedUnknownCount(), result.hasMore()),
                result.providers().stream().map(ProviderSummary::from).toList(), result.warnings().stream().map(NormalizedViews::warning).toList(),
                result.errors().stream().map(ProviderIssueView::from).toList());
    }

    private static PriceGroup group(FlightOffer offer) {
        var price = offer.price();
        var reasons = DemoSearchService.priceReasons(offer);
        return new PriceGroup(offer.comparisonGroupId(), offer.dataOrigin().name(), price.evidenceKind().name(),
                price.money() == null ? null : price.money().currency(), NormalizedViews.scope(price.scope()), price.taxes().name(),
                NormalizedViews.baggage(offer.baggage()), price.additionalCosts().stream().sorted(Comparator.comparing(c -> c.kind().name()))
                .map(c -> new CostKind(c.kind().name(), c.includedInQuotedPrice(), c.money() == null ? null : c.money().currency())).toList(),
                reasons.isEmpty(), reasons);
    }
    public record SearchContext(String datasetVersion, String fingerprint, NormalizedViews.Query query, String dataOrigin, String evaluationInstant) {}
    public record Pagination(int page, int pageSize, int baseCount, int filteredCount, int excludedUnknownCount, boolean hasMore) {}
    public record CostKind(String kind, Boolean includedInQuotedPrice, String currency) {}
    public record PriceGroup(String groupId, String dataOrigin, String evidenceKind, String currency, NormalizedViews.Scope scope,
            String taxes, NormalizedViews.Baggage baggage, List<CostKind> additionalCostKinds, boolean monetarilyComparable, List<String> reasons) {}
    public record SearchResponse(String contractVersion, String correlationId, SearchContext context, String status,
            List<NormalizedViews.Offer> offers, List<PriceGroup> priceGroups, List<NormalizedViews.Observation> observations,
            int observationCount, boolean observationsTruncated, Pagination pagination, List<ProviderSummary> providerResults,
            List<NormalizedViews.Warning> warnings, List<ProviderIssueView> errors) {}
}
