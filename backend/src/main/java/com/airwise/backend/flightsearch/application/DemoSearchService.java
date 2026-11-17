package com.airwise.backend.flightsearch.application;

import java.time.Clock;
import java.time.Instant;
import java.util.*;
import com.airwise.backend.flightsearch.domain.*;
import static com.airwise.backend.flightsearch.domain.FlightTypes.*;

/** Bounded, sequential demo fan-out. Failures remain failures; no retries or alternate sources. */
public final class DemoSearchService {
    private final FlightProvider provider;
    private final DemoCatalog catalog;
    private final Clock clock;

    public DemoSearchService(FlightProvider provider, DemoCatalog catalog, Clock clock) {
        var capabilities = provider.capabilities();
        if (capabilities.providerId() != ProviderId.DEMO || capabilities.requiresNetwork() || capabilities.requiresCredentials())
            throw new IllegalArgumentException("Demo requires an isolated provider");
        this.provider = provider; this.catalog = catalog; this.clock = clock;
    }

    public record Result(String fingerprint, List<FlightOffer> offers, List<PriceObservation> observations,
            int observationCount, boolean observationsTruncated, int baseCount, int filteredCount,
            int excludedUnknownCount, boolean hasMore, boolean partial, List<ProviderResult> providers,
            List<DataIssue> warnings, List<ProviderIssue> errors) {}
    public static final class Unavailable extends RuntimeException {
        private final boolean capability;
        private final List<ProviderResult> results;
        public Unavailable(boolean capability, List<ProviderResult> results) { this.capability = capability; this.results = List.copyOf(results); }
        public boolean capability() { return capability; }
        public List<ProviderResult> results() { return results; }
    }

    public Result search(SearchView view, ProviderExecutionContext execution) {
        return execute(view, execution, false);
    }

    /** Reconstructs the complete bounded demo set once, independently of any browser view or page. */
    public Result reconstruct(FlightSearchRequest query, String datasetVersion, ProviderExecutionContext execution) {
        return execute(new SearchView(query, datasetVersion, SearchView.Filters.none(), SearchView.Sort.DEFAULT, 0, 50), execution, true);
    }

    private Result execute(SearchView view, ProviderExecutionContext execution, boolean completeBase) {
        DatasetDescriptor dataset = catalog.resolve(view.datasetVersion());
        Instant deadline = clock.instant().plusSeconds(2);
        if (execution.deadlineAt().isBefore(deadline)) deadline = execution.deadlineAt();
        // The provider contract requires whole seconds; never round a deadline into the future.
        var context = new ProviderExecutionContext(execution.correlationId(), deadline.truncatedTo(java.time.temporal.ChronoUnit.SECONDS));
        var requests = view.query().origins().stream().map(origin -> new ProviderSearchRequest(view.query(), origin, dataset, context)).toList();
        var capabilities = provider.capabilities();
        boolean mode = (view.query().tripType() == TripType.ONE_WAY ? capabilities.oneWay() : capabilities.roundTrip()) == CapabilityState.SUPPORTED;
        boolean output = capabilities.itineraries() == CapabilityState.SUPPORTED || capabilities.priceObservations() == CapabilityState.SUPPORTED;
        var supported = requests.stream().map(r -> mode && output && provider.supports(r)).toList();
        var results = new ArrayList<ProviderResult>();
        if (supported.stream().noneMatch(Boolean::booleanValue)) {
            requests.forEach(r -> results.add(problem(r.origin(), ProviderStatus.UNSUPPORTED, ProviderIssueCode.CAPABILITY_UNSUPPORTED)));
            throw new Unavailable(true, results);
        }
        for (int i = 0; i < requests.size(); i++) {
            var request = requests.get(i);
            if (!supported.get(i)) { results.add(problem(request.origin(), ProviderStatus.UNSUPPORTED, ProviderIssueCode.CAPABILITY_UNSUPPORTED)); continue; }
            if (!clock.instant().isBefore(deadline)) { results.add(problem(request.origin(), ProviderStatus.FAILED, ProviderIssueCode.PROVIDER_TIMEOUT)); continue; }
            try {
                ProviderResult result = provider.search(request);
                if (result == null || result.providerId() != ProviderId.DEMO || !result.origin().equals(request.origin()))
                    results.add(problem(request.origin(), ProviderStatus.FAILED, ProviderIssueCode.INVALID_PROVIDER_DATA));
                else if (!clock.instant().isBefore(deadline))
                    results.add(problem(request.origin(), ProviderStatus.FAILED, ProviderIssueCode.PROVIDER_TIMEOUT));
                else results.add(result);
            } catch (RuntimeException exception) {
                results.add(problem(request.origin(), ProviderStatus.FAILED, ProviderIssueCode.PROVIDER_FAILURE));
            }
        }
        if (results.stream().allMatch(r -> r.status() == ProviderStatus.UNSUPPORTED)) throw new Unavailable(true, results);
        if (results.stream().mapToInt(ProviderResult::completedUnits).sum() == 0) throw new Unavailable(false, results);
        var byId = new TreeMap<String, FlightOffer>();
        var observations = new TreeMap<String, PriceObservation>();
        results.forEach(r -> { r.offers().forEach(o -> byId.putIfAbsent(o.offerId(), o));
            r.observations().forEach(o -> observations.putIfAbsent(o.observationId(), o)); });
        List<FlightOffer> filtered = new ArrayList<>();
        int unknown = 0;
        for (var offer : byId.values()) {
            Match match = matches(offer, view.filters());
            if (match == Match.UNKNOWN) unknown++;
            if (match == Match.YES) filtered.add(offer);
        }
        filtered.sort(order(view.sort()));
        long offset = (long) view.page() * view.pageSize();
        var page = completeBase ? List.copyOf(filtered) : filtered.stream().skip(offset).limit(view.pageSize()).toList();
        var warningMap = new TreeMap<String, DataIssue>();
        byId.values().forEach(o -> o.warnings().forEach(w -> warningMap.putIfAbsent(warningKey(w), w)));
        observations.values().forEach(o -> o.warnings().forEach(w -> warningMap.putIfAbsent(warningKey(w), w)));
        var errorMap = new TreeMap<String, ProviderIssue>();
        results.forEach(r -> r.issues().forEach(e -> errorMap.putIfAbsent(r.providerId().name() + "/" + r.origin() + "/" + e.code(), ProviderIssue.of(e.code()))));
        boolean partial = results.stream().anyMatch(r -> r.status() != ProviderStatus.COMPLETE && r.status() != ProviderStatus.EMPTY);
        return new Result(StableIdentity.fingerprint(dataset.datasetVersion(), view.query()), page,
                observations.values().stream().limit(20).toList(), observations.size(), observations.size() > 20,
                byId.size(), filtered.size(), unknown, !completeBase && offset + page.size() < filtered.size(), partial,
                List.copyOf(results), warningMap.values().stream().limit(20).toList(), errorMap.values().stream().limit(20).toList());
    }

    private static String warningKey(DataIssue w) { return (w.path() == null ? "" : w.path()) + "/" + w.code(); }
    private static ProviderResult problem(String origin, ProviderStatus status, ProviderIssueCode code) {
        return new ProviderResult(ProviderId.DEMO, origin, status, 1, 0, List.of(), List.of(), List.of(ProviderIssue.of(code)));
    }

    /** Monetary comparability says nothing about real availability, freshness or a total trip cost. */
    public static List<String> priceReasons(FlightOffer offer) {
        var price = offer.price(); var scope = price.scope(); var reasons = new ArrayList<String>();
        if (price.money() == null) reasons.add("PRICE_UNKNOWN");
        if (scope.coverage() != Coverage.FULL_ITINERARY || scope.basis() == Basis.UNKNOWN
                || !Integer.valueOf(1).equals(scope.travellerCount()) || scope.cabin() != Cabin.ECONOMY) reasons.add("PRICE_SCOPE_UNKNOWN");
        if (!Set.of(EvidenceKind.OBSERVED_CACHE, EvidenceKind.RECENT_SEARCH, EvidenceKind.SELLER_ADVERTISED).contains(price.evidenceKind()))
            reasons.add("EVIDENCE_UNSUPPORTED");
        return List.copyOf(reasons);
    }

    private enum Match { YES, NO, UNKNOWN }
    private static Match matches(FlightOffer offer, SearchView.Filters f) {
        boolean unknown = false, rejected = false;
        var itinerary = offer.itinerary(); var price = offer.price();
        String origin = itinerary.journeys().getFirst().segments().getFirst().departureAirport();
        if (!f.origins().isEmpty() && !f.origins().contains(origin)) rejected = true;
        if (!f.airlineCodes().isEmpty()) {
            var airlines = itinerary.journeys().stream().flatMap(j -> j.segments().stream())
                    .flatMap(s -> java.util.stream.Stream.of(s.marketingAirline(), s.operatingAirline())).filter(Objects::nonNull)
                    .map(Airline::code).filter(Objects::nonNull).toList();
            if (airlines.stream().noneMatch(f.airlineCodes()::contains)) {
                boolean incomplete = itinerary.journeys().stream().flatMap(j -> j.segments().stream())
                        .anyMatch(s -> s.marketingAirline() == null || s.marketingAirline().code() == null
                                || s.operatingAirline() == null || s.operatingAirline().code() == null);
                if (incomplete) unknown = true; else rejected = true;
            }
        }
        if (f.maxConnections() != null && itinerary.connectionCount() > f.maxConnections()) rejected = true;
        if (f.maxDurationMinutes() != null) {
            if (itinerary.durationSeconds() == null) unknown = true;
            else if (itinerary.durationSeconds() > f.maxDurationMinutes() * 60L) rejected = true;
        }
        if (f.currency() != null) {
            if (price.money() == null) unknown = true;
            else if (!f.currency().equals(price.money().currency())) rejected = true;
        }
        if (f.maxPrice() != null) {
            if (!priceReasons(offer).isEmpty()) unknown = true;
            else if (f.currency().equals(price.money().currency()) && price.money().amount().compareTo(f.maxPrice()) > 0) rejected = true;
        }
        // Unknown is counted once per excluded offer, even if several required fields are absent.
        return unknown ? Match.UNKNOWN : rejected ? Match.NO : Match.YES;
    }

    private static Comparator<FlightOffer> order(SearchView.Sort sort) {
        Comparator<FlightOffer> id = Comparator.comparing(FlightOffer::offerId);
        Comparator<FlightOffer> duration = Comparator.comparing(o -> o.itinerary().durationSeconds(), Comparator.nullsLast(Long::compareTo));
        Comparator<FlightOffer> connections = Comparator.comparingInt(o -> o.itinerary().connectionCount());
        return switch (sort) {
            case DEFAULT -> id;
            case DURATION_ASC -> duration.thenComparing(connections).thenComparing(id);
            case CONNECTIONS_ASC -> connections.thenComparing(duration).thenComparing(id);
            case PRICE_ASC -> Comparator.<FlightOffer, Boolean>comparing(o -> !priceReasons(o).isEmpty())
                    .thenComparing(FlightOffer::comparisonGroupId).thenComparing((a, b) ->
                        priceReasons(a).isEmpty() && priceReasons(b).isEmpty() ? a.price().money().amount().compareTo(b.price().money().amount()) : 0)
                    .thenComparing(id);
        };
    }
}
