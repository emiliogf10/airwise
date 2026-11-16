package com.airwise.backend.flightsearch.application;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import com.airwise.backend.flightprovider.demo.DemoFlightProvider;
import com.airwise.backend.flightsearch.domain.*;
import static com.airwise.backend.flightsearch.domain.FlightTypes.*;
import static org.assertj.core.api.Assertions.*;

class DemoSearchServiceTests {
    private final Clock clock = Clock.fixed(Instant.parse("2030-01-01T12:00:00Z"), ZoneOffset.UTC);
    private final DemoFlightProvider demo = new DemoFlightProvider(clock);
    private final DemoCatalog catalog = new DemoCatalog(demo.dataset(), demo.airports());
    private final DemoSearchService service = new DemoSearchService(demo, catalog, clock);
    private ProviderExecutionContext context() { return new ProviderExecutionContext(UUID.randomUUID(), clock.instant().plusSeconds(2)); }
    private FlightSearchRequest query(int day, List<String> origins, boolean returning) {
        return new FlightSearchRequest(returning ? TripType.ROUND_TRIP : TripType.ONE_WAY, origins, "MAD",
                LocalDate.of(2027, 2, day), returning ? LocalDate.of(2027, 2, day + 7) : null, new Travellers(1), Cabin.ECONOMY);
    }
    private SearchView view(FlightSearchRequest query, SearchView.Filters filters, SearchView.Sort sort, int page, int size) {
        return new SearchView(query, "demo-v1", filters, sort, page, size);
    }
    private DemoSearchService.Result search(int day, boolean returning, List<String> origins) {
        return service.search(view(query(day, origins, returning), SearchView.Filters.none(), SearchView.Sort.DEFAULT, 0, 20), context());
    }

    @Test void oneWayAndReturnRetainCompleteJourneysAndSeparateObservation() {
        var one = search(10, false, List.of("OPO"));
        assertThat(one.offers()).hasSize(1);
        assertThat(one.offers().getFirst().itinerary().journeys()).hasSize(1);
        var returning = search(10, true, List.of("SCQ", "OPO"));
        assertThat(returning.offers()).hasSize(2).allSatisfy(o -> assertThat(o.itinerary().journeys()).hasSize(2));
        assertThat(returning.observations()).hasSize(1);
        assertThat(returning.observations().getFirst().price().scope().coverage()).isEqualTo(Coverage.OUTBOUND_ONLY);
        assertThat(returning.providers()).extracting(ProviderResult::origin).containsExactly("OPO", "SCQ");
        assertThat(returning.partial()).isFalse();
    }

    @Test void viewChoicesDoNotChangeFingerprintOrOfferIdentity() {
        var q = query(12, List.of("OPO"), false);
        var base = service.search(view(q, SearchView.Filters.none(), SearchView.Sort.DEFAULT, 0, 20), context());
        var sorted = service.search(view(q, SearchView.Filters.none(), SearchView.Sort.DURATION_ASC, 0, 1), context());
        assertThat(sorted.fingerprint()).isEqualTo(base.fingerprint());
        assertThat(base.offers()).extracting(FlightOffer::offerId).contains(sorted.offers().getFirst().offerId());
        assertThat(sorted.hasMore()).isTrue();
        var outOfRange = service.search(view(q, SearchView.Filters.none(), SearchView.Sort.DEFAULT, 100000, 50), context());
        assertThat(outOfRange.offers()).isEmpty();
        assertThat(outOfRange.baseCount()).isEqualTo(8);
        assertThat(outOfRange.hasMore()).isFalse();
        assertThat(search(10, false, List.of("OPO")).fingerprint()).isNotEqualTo(base.fingerprint());
    }

    @Test void threeOriginsIncludeCompletedEmptyOriginInCanonicalOrder() {
        var result = search(10, true, List.of("SCQ", "OPO", "BCN"));
        assertThat(result.providers()).extracting(ProviderResult::origin).containsExactly("BCN", "OPO", "SCQ");
        assertThat(result.providers().getFirst().status()).isEqualTo(ProviderStatus.EMPTY);
        assertThat(result.offers()).hasSize(2); assertThat(result.observations()).hasSize(1);
        assertThat(result.partial()).isFalse();
    }

    @Test void priceFilterKeepsZeroExcludesUnknownOnceAndNeverConvertsCurrency() {
        var f = new SearchView.Filters(List.of(), List.of(), null, null, "EUR", new BigDecimal("100.00"));
        var result = service.search(view(query(12, List.of("OPO"), false), f, SearchView.Sort.PRICE_ASC, 0, 20), context());
        assertThat(result.baseCount()).isEqualTo(8);
        assertThat(result.filteredCount()).isEqualTo(4);
        assertThat(result.excludedUnknownCount()).isEqualTo(1);
        assertThat(result.offers()).extracting(o -> o.price().money().currency()).containsOnly("EUR");
        assertThat(result.offers()).extracting(o -> o.price().money().amount().toPlainString()).containsExactly("0.00", "90.00", "100.00", "100.00");
    }

    @Test void filtersAreAndAcrossClassesAndOrWithinLists() {
        var q = query(12, List.of("OPO"), false);
        var f = new SearchView.Filters(List.of("OPO"), List.of("D1", "D2"), 0, 60, "EUR", null);
        var result = service.search(view(q, f, SearchView.Sort.DEFAULT, 0, 20), context());
        assertThat(result.offers()).hasSize(1);
        assertThat(result.offers().getFirst().price().money().amount()).isEqualByComparingTo("130");
        var noAirline = new SearchView.Filters(List.of(), List.of("D2"), null, null, null, null);
        assertThat(service.search(view(q, noAirline, SearchView.Sort.DEFAULT, 0, 20), context()).offers()).isEmpty();
    }

    @Test void priceGroupsAreOrderedWithoutCrossCurrencyPriceRanking() {
        var result = service.search(view(query(12, List.of("OPO"), false), SearchView.Filters.none(), SearchView.Sort.PRICE_ASC, 0, 20), context());
        var offers = result.offers();
        assertThat(offers.getLast().price().money()).isNull();
        assertThat(DemoSearchService.priceReasons(offers.getLast())).containsExactly("PRICE_UNKNOWN");
        var groups = offers.stream().filter(o -> o.price().money() != null).map(FlightOffer::comparisonGroupId).distinct().toList();
        assertThat(groups).isSorted();
        for (String group : groups) assertThat(offers.stream().filter(o -> o.comparisonGroupId().equals(group))
                .map(o -> o.price().money().amount()).toList()).isSorted();
    }

    @Test void durationAndConnectionsSortBeforePagingAndIdsBreakPresentationTies() {
        var q = query(12, List.of("OPO"), false);
        var result = service.search(view(q, SearchView.Filters.none(), SearchView.Sort.DURATION_ASC, 0, 20), context());
        assertThat(result.offers().getFirst().itinerary().durationSeconds()).isEqualTo(3600L);
        assertThat(result.offers().getLast().itinerary().connectionCount()).isEqualTo(1);
        var p1 = service.search(view(q, SearchView.Filters.none(), SearchView.Sort.CONNECTIONS_ASC, 0, 4), context());
        var p2 = service.search(view(q, SearchView.Filters.none(), SearchView.Sort.CONNECTIONS_ASC, 1, 4), context());
        var all = new ArrayList<>(p1.offers()); all.addAll(p2.offers());
        assertThat(all).containsExactlyElementsOf(service.search(view(q, SearchView.Filters.none(), SearchView.Sort.CONNECTIONS_ASC, 0, 20), context()).offers());
        assertThat(p1.hasMore()).isTrue(); assertThat(p2.hasMore()).isFalse();
    }

    @Test void partialAndEmptyAreDistinctFromCompleteFailure() {
        var mixed = search(11, true, List.of("SCQ", "OPO"));
        assertThat(mixed.partial()).isTrue(); assertThat(mixed.offers()).hasSize(1);
        assertThat(mixed.errors()).extracting(ProviderIssue::code).containsExactly(ProviderIssueCode.PROVIDER_OFFLINE);
        var partial = search(19, false, List.of("OPO"));
        assertThat(partial.partial()).isTrue(); assertThat(partial.providers().getFirst().completedUnits()).isEqualTo(1);
        var empty = search(14, false, List.of("OPO"));
        assertThat(empty.offers()).isEmpty(); assertThat(empty.partial()).isFalse();
        assertThatThrownBy(() -> search(13, false, List.of("OPO"))).isInstanceOfSatisfying(DemoSearchService.Unavailable.class,
                ex -> { assertThat(ex.capability()).isFalse(); assertThat(ex.results().getFirst().status()).isEqualTo(ProviderStatus.FAILED); });
    }

    @Test void observationsAreNotOfferFiltersAndPartialPricesAreNotFullTravelAmounts() {
        var filter = new SearchView.Filters(List.of(), List.of(), null, null, "EUR", BigDecimal.ZERO);
        var result = service.search(view(query(10, List.of("OPO"), true), filter, SearchView.Sort.DEFAULT, 0, 20), context());
        assertThat(result.offers()).isEmpty(); assertThat(result.observations()).hasSize(1);
        var partial = service.search(view(query(16, List.of("OPO"), true), filter, SearchView.Sort.DEFAULT, 0, 20), context());
        assertThat(partial.offers()).isEmpty(); assertThat(partial.excludedUnknownCount()).isEqualTo(1);
    }

    @Test void unsupportedIsPreflightAndMakesZeroSearchCalls() {
        var calls = new AtomicInteger();
        FlightProvider port = new FlightProvider() {
            public ProviderCapabilities capabilities() { return demo.capabilities(); }
            public boolean supports(ProviderSearchRequest request) { return false; }
            public ProviderResult search(ProviderSearchRequest request) { calls.incrementAndGet(); throw new AssertionError(); }
        };
        assertThatThrownBy(() -> new DemoSearchService(port, catalog, clock).search(
                view(query(10, List.of("OPO", "SCQ"), false), SearchView.Filters.none(), SearchView.Sort.DEFAULT, 0, 20), context()))
                .isInstanceOfSatisfying(DemoSearchService.Unavailable.class, ex -> assertThat(ex.capability()).isTrue());
        assertThat(calls).hasValue(0);
    }

    @Test void emptyCompletedOriginPlusThrownFailureIsPartialWithoutLeakingCause() {
        FlightProvider port = new FlightProvider() {
            public ProviderCapabilities capabilities() { return demo.capabilities(); }
            public boolean supports(ProviderSearchRequest request) { return true; }
            public ProviderResult search(ProviderSearchRequest request) {
                if (request.origin().equals("SCQ")) throw new IllegalStateException("synthetic-secret-provider-url");
                return new ProviderResult(ProviderId.DEMO, request.origin(), ProviderStatus.EMPTY, 1, 1, List.of(), List.of(), List.of());
            }
        };
        var result = new DemoSearchService(port, catalog, clock).search(view(query(10, List.of("OPO", "SCQ"), false),
                SearchView.Filters.none(), SearchView.Sort.DEFAULT, 0, 20), context());
        assertThat(result.partial()).isTrue(); assertThat(result.offers()).isEmpty();
        assertThat(result.errors().getFirst().message()).isEqualTo("Fallo de fuente.");
    }

    @Test void expiredAndFreshnessUnknownArePreservedWithoutPretendingBookingConfirmation() {
        assertThat(search(15, false, List.of("OPO")).offers().getFirst().price().freshness()).isEqualTo(Freshness.EXPIRED);
        assertThat(search(12, false, List.of("OPO")).offers()).anySatisfy(o -> assertThat(o.price().freshness()).isEqualTo(Freshness.UNKNOWN));
    }

    @Test void fortyObservationsAreCountedButOnlyTwentyAreReturnedWithoutOfferPagination() {
        var q = query(10, List.of("OPO", "SCQ"), true);
        var sample = demo.search(new ProviderSearchRequest(q, "OPO", demo.dataset(), context())).observations().getFirst();
        FlightProvider port = new FlightProvider() {
            public ProviderCapabilities capabilities() { return demo.capabilities(); }
            public boolean supports(ProviderSearchRequest request) { return true; }
            public ProviderResult search(ProviderSearchRequest request) {
                var observations = java.util.stream.IntStream.range(0, 20).mapToObj(i -> new PriceObservation(
                        StableIdentity.observationId(StableIdentity.fingerprint("demo-v1", q), request.origin()+"-"+i), ProviderId.DEMO,
                        DataOrigin.DEMO, request.origin(), "MAD", TripType.ROUND_TRIP, q.outboundDate(), q.returnDate(), sample.price(), sample.warnings())).toList();
                return new ProviderResult(ProviderId.DEMO, request.origin(), ProviderStatus.COMPLETE, 1, 1, List.of(), observations, List.of());
            }
        };
        var result = new DemoSearchService(port, catalog, clock).search(view(q, SearchView.Filters.none(), SearchView.Sort.DEFAULT, 100000, 1), context());
        assertThat(result.observationCount()).isEqualTo(40); assertThat(result.observationsTruncated()).isTrue();
        assertThat(result.observations()).hasSize(20).extracting(PriceObservation::observationId).isSorted();
        assertThat(result.offers()).isEmpty();
    }

    @Test void elapsedOperationalBudgetCallsNoProviderAndPreservesTimeoutCause() {
        var calls = new AtomicInteger();
        FlightProvider port = new FlightProvider() {
            public ProviderCapabilities capabilities() { return demo.capabilities(); }
            public boolean supports(ProviderSearchRequest request) { return true; }
            public ProviderResult search(ProviderSearchRequest request) { calls.incrementAndGet(); throw new AssertionError(); }
        };
        assertThatThrownBy(() -> new DemoSearchService(port, catalog, clock).search(view(query(10, List.of("OPO"), false),
                SearchView.Filters.none(), SearchView.Sort.DEFAULT, 0, 20), new ProviderExecutionContext(UUID.randomUUID(), clock.instant().minusSeconds(1))))
                .isInstanceOfSatisfying(DemoSearchService.Unavailable.class, ex -> assertThat(ex.results().getFirst().issues().getFirst().code()).isEqualTo(ProviderIssueCode.PROVIDER_TIMEOUT));
        assertThat(calls).hasValue(0);
    }

    @Test void unknownRequiredDurationAndAirlineAreCountedOnceAndSortedLast() {
        var q = query(10, List.of("OPO"), false);
        var sample = search(10, false, List.of("OPO")).offers().getFirst();
        var unknownTime = new FlightTime(null, null, null, null, UnknownReason.NOT_RETURNED);
        var segment = new FlightSegment("s-unknown-time", "OPO", "MAD", unknownTime, unknownTime, null, null, null, null, null);
        var itinerary = new FlightItinerary(TripType.ONE_WAY, List.of(new FlightJourney(Direction.OUTBOUND, List.of(segment), List.of(), null)), null, 0);
        var unknown = new FlightOffer(StableIdentity.offerId(StableIdentity.fingerprint("demo-v1", q), "unknown"), ProviderId.DEMO, DataOrigin.DEMO,
                itinerary, sample.price(), null, sample.baggage(), sample.comparisonGroupId(), List.of());
        FlightProvider port = new FlightProvider() {
            public ProviderCapabilities capabilities() { return demo.capabilities(); }
            public boolean supports(ProviderSearchRequest request) { return true; }
            public ProviderResult search(ProviderSearchRequest request) { return new ProviderResult(ProviderId.DEMO, "OPO", ProviderStatus.COMPLETE,
                    1, 1, List.of(sample, unknown), List.of(), List.of()); }
        };
        var useCase = new DemoSearchService(port, catalog, clock);
        var sorted = useCase.search(view(q, SearchView.Filters.none(), SearchView.Sort.DURATION_ASC, 0, 20), context());
        assertThat(sorted.offers().getLast().itinerary().durationSeconds()).isNull();
        var filtered = useCase.search(view(q, new SearchView.Filters(List.of(), List.of("D1"), null, 10080, null, null), SearchView.Sort.DEFAULT, 0, 20), context());
        assertThat(filtered.filteredCount()).isEqualTo(1); assertThat(filtered.excludedUnknownCount()).isEqualTo(1);
    }
}
