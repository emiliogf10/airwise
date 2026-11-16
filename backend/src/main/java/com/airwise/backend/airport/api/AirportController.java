package com.airwise.backend.airport.api;

import java.util.List;
import java.util.Set;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Validator;
import org.springframework.context.annotation.Profile;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.airwise.backend.api.NormalizedViews;
import com.airwise.backend.api.RequestValues;
import com.airwise.backend.flightsearch.application.DemoCatalog;
import com.airwise.backend.http.ApiException;
import com.airwise.backend.http.RequestContext;

@RestController
@Profile("demo")
public final class AirportController {
    private final DemoCatalog catalog;
    private final Validator validator;
    public AirportController(DemoCatalog catalog, Validator validator) { this.catalog = catalog; this.validator = validator; }

    @GetMapping(value = "/api/v1/demo/airports", produces = "application/json")
    public CatalogResponse airports(@RequestParam MultiValueMap<String, String> parameters, HttpServletRequest request) {
        Set<String> allowed = Set.of("q", "datasetVersion", "page", "pageSize");
        if (parameters.entrySet().stream().anyMatch(e -> !allowed.contains(e.getKey()) || e.getValue().size() != 1))
            throw ApiException.invalidRequest();
        String q = parameters.getFirst("q");
        if (q == null) q = "";
        if (q.codePointCount(0, q.length()) > 64 || q.codePoints().anyMatch(Character::isISOControl))
            throw ApiException.validation("/q", "INVALID_FORMAT");
        int page = parameters.containsKey("page") ? RequestValues.queryInteger(parameters.getFirst("page"), "/page") : 0;
        int size = parameters.containsKey("pageSize") ? RequestValues.queryInteger(parameters.getFirst("pageSize"), "/pageSize") : 20;
        var paging = new RequestValues.Page(page, size);
        paging.validate(validator);
        String version = RequestValues.id(parameters.getFirst("datasetVersion"), "/datasetVersion");
        if (page > 0 && version == null) throw ApiException.validation("/datasetVersion", "REQUIRED");
        try { catalog.resolve(version); } catch (DemoCatalog.VersionUnavailable exception) { throw ApiException.datasetUnavailable(); }
        var matches = catalog.matching(q);
        var items = matches.stream().skip(paging.offset()).limit(size).map(NormalizedViews::airport).toList();
        return new CatalogResponse("airwise-v1", RequestContext.current(request).correlationId().toString(),
                NormalizedViews.dataset(catalog.dataset()), items,
                new CatalogPagination(page, size, matches.size(), paging.offset() + items.size() < matches.size()));
    }

    public record CatalogPagination(int page, int pageSize, int totalCount, boolean hasMore) {}
    public record CatalogResponse(String contractVersion, String correlationId, NormalizedViews.Dataset dataset,
            List<NormalizedViews.AirportView> items, CatalogPagination pagination) {}
}
