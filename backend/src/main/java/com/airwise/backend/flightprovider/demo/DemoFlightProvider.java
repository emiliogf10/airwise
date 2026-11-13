package com.airwise.backend.flightprovider.demo;

import java.io.InputStream;
import java.time.Clock;
import java.util.List;
import com.airwise.backend.airport.domain.Airport;
import com.airwise.backend.flightsearch.application.*;
import static com.airwise.backend.flightsearch.domain.DomainChecks.*;
import static com.airwise.backend.flightsearch.domain.FlightTypes.*;

public final class DemoFlightProvider implements FlightProvider {
    private final DemoCorpus corpus;
    private final Clock operationalClock;

    public DemoFlightProvider() { this(Clock.systemUTC()); }

    public DemoFlightProvider(Clock operationalClock) {
        this(operationalClock, DemoFlightProvider.class.getResourceAsStream(DemoCorpus.RESOURCE));
    }

    /** Package-scoped input supports corpus validation tests without accepting client paths or URLs. */
    DemoFlightProvider(Clock operationalClock, InputStream input) {
        this.operationalClock = present(operationalClock);
        try { corpus = new DemoCorpus(input); }
        catch (RuntimeException e) { throw new IllegalArgumentException("Invalid demo dataset", e); }
    }

    public DatasetDescriptor dataset() { return corpus.dataset; }
    public List<Airport> airports() { return corpus.airports; }
    @Override public ProviderCapabilities capabilities() { return corpus.dataset.capabilities(); }

    @Override public boolean supports(ProviderSearchRequest request) {
        if (request == null || !corpus.dataset.equals(request.dataset())) return false;
        var query = request.query();
        return corpus.dataset.contains(query.outboundDate())
                && (query.returnDate() == null || corpus.dataset.contains(query.returnDate()))
                && corpus.airports.stream().anyMatch(a -> a.code().equals(query.destination()))
                && query.origins().stream().allMatch(code -> corpus.airports.stream().anyMatch(a -> a.code().equals(code)));
    }

    @Override public ProviderResult search(ProviderSearchRequest request) {
        present(request);
        // A caller cannot retain a version label while changing its clock, calendar or capabilities.
        require(corpus.dataset.equals(request.dataset()), "Dataset descriptor is not the server descriptor");
        if (!supports(request)) return result(request.origin(), ProviderStatus.UNSUPPORTED, ProviderIssueCode.CAPABILITY_UNSUPPORTED);
        if (!operationalClock.instant().isBefore(request.context().deadlineAt())) {
            return result(request.origin(), ProviderStatus.FAILED, ProviderIssueCode.PROVIDER_TIMEOUT);
        }
        var query = request.query();
        var key = new DemoCorpus.ScenarioKey(query.tripType(), query.outboundDate(), query.returnDate(), request.origin(), query.destination());
        var scenario = corpus.scenarios.get(key);
        ProviderResult result = scenario == null
                ? new ProviderResult(ProviderId.DEMO, request.origin(), ProviderStatus.EMPTY, 1, 1, List.of(), List.of(), List.of())
                : scenario.result(request.origin(), StableIdentity.fingerprint(corpus.dataset.datasetVersion(), query));
        if (!operationalClock.instant().isBefore(request.context().deadlineAt())) {
            return result(request.origin(), ProviderStatus.FAILED, ProviderIssueCode.PROVIDER_TIMEOUT);
        }
        return result;
    }

    private static ProviderResult result(String origin, ProviderStatus status, ProviderIssueCode issue) {
        return new ProviderResult(ProviderId.DEMO, origin, status, 1, 0, List.of(), List.of(), List.of(ProviderIssue.of(issue)));
    }
}
