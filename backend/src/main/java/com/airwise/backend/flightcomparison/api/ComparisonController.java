package com.airwise.backend.flightcomparison.api;

import java.util.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Validator;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;
import com.airwise.backend.api.NormalizedViews;
import com.airwise.backend.flightcomparison.application.DemoComparisonService;
import com.airwise.backend.flightsearch.application.*;
import com.airwise.backend.flightsearch.domain.DataIssue;
import com.airwise.backend.flightsearch.domain.FlightTypes.ProviderIssueCode;
import com.airwise.backend.http.*;

@RestController
@Profile("demo")
public final class ComparisonController {
    private final DemoComparisonService service;
    private final DemoCatalog catalog;
    private final ComparisonRequestReader reader;
    public ComparisonController(DemoComparisonService service, DemoCatalog catalog, Validator validator) {
        this.service = service; this.catalog = catalog; this.reader = new ComparisonRequestReader(catalog, validator);
    }
    @PostMapping(value = "/api/v1/demo/comparisons", produces = "application/json")
    public ComparisonViews.Response compare(@RequestBody JsonNode body, HttpServletRequest httpRequest) {
        var request = reader.read(body);
        var context = RequestContext.current(httpRequest);
        DemoComparisonService.Result result;
        try { result = service.compare(request, new ProviderExecutionContext(context.correlationId(), context.deadlineAt())); }
        catch (DemoComparisonService.SelectionInvalid exception) { throw ApiException.validation(exception.path(), exception.code()); }
        catch (DemoCatalog.VersionUnavailable exception) { throw ApiException.datasetUnavailable(); }
        catch (DemoSearchService.Unavailable exception) {
            var providers = exception.results().stream().map(ProviderSummary::from).toList();
            throw exception.capability() ? ApiException.capabilityUnavailable(providers) : ApiException.providersFailed(providers);
        }
        return new ComparisonViews.Response("airwise-v1", context.correlationId().toString(),
                new ComparisonViews.Context(request.datasetVersion(), result.fingerprint(), NormalizedViews.query(request.query()),
                        "DEMO", catalog.dataset().evaluationInstant().toString()), request.primary().name(),
                request.requiredComponents().stream().map(Enum::name).toList(), result.comparison().groups().stream().map(ComparisonViews::group).toList(),
                warnings(result.warnings(), result.providerIssueCodes()));
    }

    static List<NormalizedViews.Warning> warnings(List<DataIssue> issues, List<ProviderIssueCode> providerIssues) {
        var warnings = new HashMap<String, NormalizedViews.Warning>();
        issues.stream().map(NormalizedViews::warning).forEach(w -> warnings.put((w.path() == null ? "" : w.path()) + "/" + w.code(), w));
        if (!providerIssues.isEmpty()) {
            // Enum names, never provider message text. One global issue preserves every distinct cause.
            String message = "Conjunto demo incompleto; incidencias de ejecución: "
                    + String.join(", ", providerIssues.stream().map(Enum::name).distinct().sorted().toList()) + ".";
            warnings.put("/INVALID_PROVIDER_DATA", new NormalizedViews.Warning("INVALID_PROVIDER_DATA", null, message));
        }
        return warnings.values().stream().sorted(Comparator.comparing(NormalizedViews.Warning::path, Comparator.nullsFirst(String::compareTo))
                .thenComparing(NormalizedViews.Warning::code)).limit(20).toList();
    }
}
