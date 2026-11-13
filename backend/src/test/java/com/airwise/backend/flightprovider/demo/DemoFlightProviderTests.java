package com.airwise.backend.flightprovider.demo;

import static com.airwise.backend.flightsearch.domain.FlightTypes.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.airwise.backend.flightsearch.application.*;
import com.airwise.backend.flightsearch.domain.*;

class DemoFlightProviderTests {

    private static final Instant OPERATIONAL_NOW = Instant.parse("2030-01-01T00:00:00Z");
    private static final Clock CLOCK = Clock.fixed(OPERATIONAL_NOW, ZoneOffset.UTC);
    private static final UUID CORRELATION = UUID.fromString("00000000-0000-4000-8000-000000000001");
    private final DemoFlightProvider provider = new DemoFlightProvider(CLOCK);

    private FlightSearchRequest query(TripType type, List<String> origins, String outbound, String inbound) {
        return new FlightSearchRequest(type, origins, "MAD", LocalDate.parse(outbound),
                inbound == null ? null : LocalDate.parse(inbound), new Travellers(1), Cabin.ECONOMY);
    }

    private FlightSearchRequest oneWay(String date) {
        return query(TripType.ONE_WAY, List.of("OPO"), date, null);
    }

    private ProviderSearchRequest request(FlightSearchRequest query, String origin) {
        return new ProviderSearchRequest(query, origin, provider.dataset(),
                new ProviderExecutionContext(CORRELATION, OPERATIONAL_NOW.plusSeconds(60)));
    }

    private ProviderResult search(FlightSearchRequest query, String origin) {
        return provider.search(request(query, origin));
    }

    @Test
    void directFlightMatchesApprovedIdentityAndPriceVector() {
        var result = search(oneWay("2027-02-10"), "OPO");
        assertEquals(ProviderStatus.COMPLETE, result.status());
        assertEquals(1, result.requiredUnits());
        assertEquals(1, result.completedUnits());
        assertTrue(result.issues().isEmpty());
        assertTrue(result.observations().isEmpty());
        assertEquals(1, result.offers().size());
        var offer = result.offers().getFirst();
        assertEquals("of_f04264b1cb41c28788bb5f40d3c18f96f88966050d68ffc6f3f3cc286afc3c83", offer.offerId());
        assertEquals("g_35e6380641ec2860a4ddc4ff437324314a8d55316744695a07c26210f66ad190", offer.comparisonGroupId());
        assertEquals(DataOrigin.DEMO, offer.dataOrigin());
        assertEquals(ProviderId.DEMO, offer.providerId());
        assertEquals(new BigDecimal("100.00"), offer.price().money().amount());
        assertEquals("EUR", offer.price().money().currency());
        assertEquals(Coverage.FULL_ITINERARY, offer.price().scope().coverage());
        assertEquals(1, offer.price().scope().travellerCount());
        assertEquals(EvidenceKind.RECENT_SEARCH, offer.price().evidenceKind());
        assertEquals(Freshness.FRESH, offer.price().freshness());
        assertEquals(4200L, offer.itinerary().durationSeconds());
        assertEquals("OPO", offer.itinerary().journeys().getFirst().segments().getFirst().departureAirport());
        assertEquals("MAD", offer.itinerary().journeys().getFirst().segments().getLast().arrivalAirport());
        assertEquals(BaggageStatus.UNKNOWN, offer.baggage().checked().status());
        assertNull(offer.baggage().checked().pieces());
        assertNull(offer.baggage().checked().weightKg());
        assertNull(offer.seller());
    }

    @Test
    void roundTripKeepsBothJourneysAndPartialObservationSeparate() {
        var query = query(TripType.ROUND_TRIP, List.of("SCQ", " opo "), "2027-02-10", "2027-02-17");
        var opo = search(query, "OPO");
        var scq = search(query, "SCQ");
        assertEquals(List.of("OPO", "SCQ"), query.origins());
        assertEquals(ProviderStatus.COMPLETE, opo.status());
        assertEquals(ProviderStatus.COMPLETE, scq.status());
        assertEquals(1, opo.offers().size());
        assertEquals(1, scq.offers().size());
        assertEquals(1, opo.observations().size());
        var offer = opo.offers().getFirst();
        assertEquals("of_d76f2960d8421d80e8407e0484e0aae160d30b4fb644a688b3f204da577b08df", offer.offerId());
        assertEquals(2, offer.itinerary().journeys().size());
        assertEquals(8400L, offer.itinerary().durationSeconds());
        var outbound = offer.itinerary().journeys().getFirst();
        var inbound = offer.itinerary().journeys().getLast();
        assertEquals(Direction.OUTBOUND, outbound.direction());
        assertEquals(Direction.INBOUND, inbound.direction());
        assertEquals("MAD", inbound.segments().getFirst().departureAirport());
        assertEquals("OPO", inbound.segments().getLast().arrivalAirport());
        assertNotEquals(outbound.segments().getFirst().segmentId(), inbound.segments().getFirst().segmentId());
        assertEquals(new BigDecimal("110.00"), scq.offers().getFirst().price().money().amount());
        assertEquals(7200L, scq.offers().getFirst().itinerary().durationSeconds());
        assertEquals(offer.comparisonGroupId(), scq.offers().getFirst().comparisonGroupId());
        var observation = opo.observations().getFirst();
        assertEquals(Coverage.OUTBOUND_ONLY, observation.price().scope().coverage());
        assertEquals(EvidenceKind.OBSERVED_CACHE, observation.price().evidenceKind());
        assertEquals(new BigDecimal("45.00"), observation.price().money().amount());
        assertTrue(observation.observationId().startsWith("ob_"));
        assertTrue(opo.offers().stream().noneMatch(o -> o.offerId().equals(observation.observationId())));
    }

    @Test
    void equivalentCanonicalQueriesAndOperationalContextsHaveSameFunctionalResult() {
        var a = query(TripType.ROUND_TRIP, List.of("SCQ", " opo "), "2027-02-10", "2027-02-17");
        var b = query(TripType.ROUND_TRIP, List.of("OPO", "SCQ"), "2027-02-10", "2027-02-17");
        var expected = search(a, "OPO");
        assertEquals(expected, search(b, "OPO"));
        var otherContext = new ProviderExecutionContext(
                UUID.fromString("00000000-0000-4000-8000-000000000002"), OPERATIONAL_NOW.plusSeconds(120));
        assertEquals(expected, provider.search(new ProviderSearchRequest(b, "OPO", provider.dataset(), otherContext)));
        var otherClock = new DemoFlightProvider(Clock.fixed(OPERATIONAL_NOW.plusSeconds(10), ZoneOffset.UTC));
        assertEquals(expected, otherClock.search(request(b, "OPO")));
        assertEquals(Instant.parse("2027-01-01T00:00:00Z"), provider.dataset().evaluationInstant());
    }

    @Test
    void severalOriginsKeepIndependentSuccessAndFailure() {
        var q = query(TripType.ROUND_TRIP, List.of("OPO", "SCQ"), "2027-02-11", "2027-02-18");
        assertEquals(ProviderStatus.COMPLETE, search(q, "OPO").status());
        var failed = search(q, "SCQ");
        assertEquals(ProviderStatus.FAILED, failed.status());
        assertEquals(0, failed.completedUnits());
        assertTrue(failed.offers().isEmpty());
        assertTrue(failed.observations().isEmpty());
        assertEquals(ProviderIssueCode.PROVIDER_OFFLINE, failed.issues().getFirst().code());
    }

    @Test
    void validEmptyAndProviderFailureAreDifferent() {
        var empty = search(oneWay("2027-02-14"), "OPO");
        assertEquals(ProviderStatus.EMPTY, empty.status());
        assertEquals(1, empty.completedUnits());
        assertTrue(empty.issues().isEmpty());
        assertTrue(empty.offers().isEmpty());
        var failed = search(oneWay("2027-02-13"), "OPO");
        assertEquals(ProviderStatus.FAILED, failed.status());
        assertEquals(0, failed.completedUnits());
        assertEquals(ProviderIssueCode.PROVIDER_TIMEOUT, failed.issues().getFirst().code());
    }

    @Test
    void partialProviderResultRetainsDataCauseAndLogicalUnits() {
        var partial = search(oneWay("2027-02-19"), "OPO");
        assertEquals(ProviderStatus.PARTIAL, partial.status());
        assertEquals(2, partial.requiredUnits());
        assertEquals(1, partial.completedUnits());
        assertFalse(partial.offers().isEmpty());
        assertFalse(partial.issues().isEmpty());
    }

    @Test
    void expiredPriceUsesDatasetClockEvenInLaterOperationalYear() {
        var result = search(oneWay("2027-02-15"), "OPO");
        assertEquals(ProviderStatus.COMPLETE, result.status());
        var price = result.offers().getFirst().price();
        assertEquals(Freshness.EXPIRED, price.freshness());
        assertTrue(price.expiresAt().isBefore(price.retrievedAt()));
        assertTrue(price.expiresAt().isBefore(provider.dataset().evaluationInstant()));
    }

    @Test
    void completeRoundTripWithOutboundOnlyPriceIsKeptAsOffer() {
        var result = search(query(TripType.ROUND_TRIP, List.of("OPO"), "2027-02-16", "2027-02-23"), "OPO");
        assertEquals(ProviderStatus.COMPLETE, result.status());
        var offer = result.offers().getFirst();
        assertEquals(2, offer.itinerary().journeys().size());
        assertEquals(Coverage.OUTBOUND_ONLY, offer.price().scope().coverage());
        assertTrue(result.observations().isEmpty());
    }

    @Test
    void allReturnedCollectionsAreImmutable() {
        var origins = new ArrayList<>(List.of("OPO"));
        var query = query(TripType.ONE_WAY, origins, "2027-02-10", null);
        origins.clear();
        assertEquals(List.of("OPO"), query.origins());
        var result = search(query, "OPO");
        assertThrows(UnsupportedOperationException.class, () -> result.offers().clear());
        assertThrows(UnsupportedOperationException.class, () -> result.offers().getFirst().warnings().clear());
        assertThrows(UnsupportedOperationException.class, () -> result.offers().getFirst().itinerary().journeys().clear());
        assertThrows(UnsupportedOperationException.class, () -> provider.airports().clear());
        assertEquals(result, search(query, "OPO"));
    }

    @Test
    void connectingFixturePreservesSegmentOrderConnectionAndElapsedTime() {
        var result = search(oneWay("2027-02-12"), "OPO");
        assertEquals(ProviderStatus.COMPLETE, result.status());
        assertEquals(8, result.offers().size());
        var via = result.offers().stream().filter(o -> o.itinerary().connectionCount() == 1).findFirst().orElseThrow();
        var journey = via.itinerary().journeys().getFirst();
        assertEquals(List.of("OPO", "BCN"), journey.segments().stream().map(FlightSegment::departureAirport).toList());
        assertEquals(List.of("BCN", "MAD"), journey.segments().stream().map(FlightSegment::arrivalAirport).toList());
        assertEquals(2, journey.segments().size());
        assertEquals(3600L, journey.connections().getFirst().durationSeconds());
        assertFalse(journey.connections().getFirst().airportChange());
        assertNull(journey.connections().getFirst().selfTransfer());
        assertEquals(15000L, via.itinerary().durationSeconds());
        assertEquals(new BigDecimal("90.00"), via.price().money().amount());
        assertTrue(via.warnings().stream().anyMatch(w -> w.code() == DataIssueCode.CONNECTION_UNVERIFIED));
        assertEquals(result.offers().stream().map(FlightOffer::offerId).sorted().toList(),
                result.offers().stream().map(FlightOffer::offerId).toList());
    }

    @Test
    void varietyKeepsDistinctTiesCurrenciesUnknownsZeroAndBaggageConditions() {
        var result = search(oneWay("2027-02-12"), "OPO");
        var ties = result.offers().stream().filter(o -> o.price().money() != null
                && o.price().money().equals(new Money(new BigDecimal("100.00"), "EUR"))).toList();
        assertEquals(2, ties.size());
        assertNotEquals(ties.getFirst().offerId(), ties.getLast().offerId());
        assertEquals(ties.getFirst().comparisonGroupId(), ties.getLast().comparisonGroupId());
        assertEquals(ties.getFirst().itinerary().durationSeconds(), ties.getLast().itinerary().durationSeconds());
        var usd = result.offers().stream().filter(o -> o.price().money() != null
                && o.price().money().currency().equals("USD")).findFirst().orElseThrow();
        assertEquals(new BigDecimal("95.00"), usd.price().money().amount());
        assertNotEquals(usd.comparisonGroupId(), ties.getFirst().comparisonGroupId());
        var unknown = result.offers().stream().filter(o -> o.price().money() == null).findFirst().orElseThrow();
        assertEquals(UnknownReason.NOT_RETURNED, unknown.price().unknownReason());
        assertEquals(EvidenceKind.RECENT_SEARCH, unknown.price().evidenceKind());
        assertNotEquals(unknown.comparisonGroupId(), ties.getFirst().comparisonGroupId());
        var zero = result.offers().stream().filter(o -> o.price().money() != null
                && o.price().money().amount().signum() == 0).findFirst().orElseThrow();
        assertEquals(new BigDecimal("0.00"), zero.price().money().amount());
        assertNull(zero.price().unknownReason());
        var fast = result.offers().stream().filter(o -> Long.valueOf(3600L).equals(o.itinerary().durationSeconds()))
                .findFirst().orElseThrow();
        assertEquals(BaggageStatus.INCLUDED, fast.baggage().cabin().status());
        assertEquals(BaggageStatus.NOT_INCLUDED, fast.baggage().checked().status());
        assertEquals(0, fast.baggage().checked().pieces());
        assertTrue(result.offers().stream().anyMatch(o -> o.price().freshness() == Freshness.UNKNOWN
                && o.price().expiresAt() == null));
    }

    @Test
    void supportedQueryWithoutRegisteredMatchesIsEmptyWithoutFallback() {
        var query = oneWay("2027-02-20");
        assertTrue(provider.supports(request(query, "OPO")));
        var result = search(query, "OPO");
        assertEquals(ProviderStatus.EMPTY, result.status());
        assertEquals(1, result.completedUnits());
        assertTrue(result.offers().isEmpty());
        assertTrue(result.observations().isEmpty());
        assertTrue(result.issues().isEmpty());
    }

    @Test
    void demoCapabilitiesDeclareNoCredentialsNetworkOrSellerDetails() {
        var c = provider.capabilities();
        assertEquals(ProviderId.DEMO, c.providerId());
        assertFalse(c.requiresNetwork());
        assertFalse(c.requiresCredentials());
        assertEquals(1, c.maxOriginsPerCall());
        assertEquals(CapabilityState.SUPPORTED, c.itineraries());
        assertEquals(CapabilityState.SUPPORTED, c.priceObservations());
        assertEquals(CapabilityState.SUPPORTED, c.roundTrip());
        assertEquals(CapabilityState.UNSUPPORTED, c.sellerDetails());
        assertTrue(provider.supports(request(oneWay("2027-02-10"), "OPO")));
    }

    @Test
    void deadlineIsOperationalAndCannotRejuvenateDataset() {
        var req = new ProviderSearchRequest(oneWay("2027-02-10"), "OPO", provider.dataset(),
                new ProviderExecutionContext(CORRELATION, OPERATIONAL_NOW));
        var result = provider.search(req);
        assertEquals(ProviderStatus.FAILED, result.status());
        assertEquals(ProviderIssueCode.PROVIDER_TIMEOUT, result.issues().getFirst().code());
        assertTrue(result.offers().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"2026-12-31", "2028-01-01"})
    void outsideDatasetCalendarIsUnsupported(String date) {
        var req = request(oneWay(date), "OPO");
        assertFalse(provider.supports(req));
        var result = provider.search(req);
        assertEquals(ProviderStatus.UNSUPPORTED, result.status());
        assertEquals(0, result.completedUnits());
        assertTrue(result.offers().isEmpty());
    }

    @Test
    void unknownAirportAndOriginOutsideQueryAreNotAccepted() {
        var unknown = query(TripType.ONE_WAY, List.of("XXX"), "2027-02-10", null);
        assertFalse(provider.supports(request(unknown, "XXX")));
        assertEquals(ProviderStatus.UNSUPPORTED, search(unknown, "XXX").status());
        assertThrows(IllegalArgumentException.class, () -> request(oneWay("2027-02-10"), "SCQ"));
    }

    @Test
    void malformedBaseQueriesFailBeforeProviderExecution() {
        assertThrows(IllegalArgumentException.class,
                () -> query(TripType.ROUND_TRIP, List.of("OPO"), "2027-02-10", null));
        assertThrows(IllegalArgumentException.class,
                () -> query(TripType.ROUND_TRIP, List.of("OPO"), "2027-02-10", "2027-02-09"));
        assertThrows(IllegalArgumentException.class,
                () -> query(TripType.ONE_WAY, List.of("OPO"), "2027-02-10", "2027-02-17"));
        assertThrows(IllegalArgumentException.class,
                () -> query(TripType.ONE_WAY, List.of("OPO", " opo "), "2027-02-10", null));
        assertThrows(IllegalArgumentException.class,
                () -> query(TripType.ONE_WAY, List.of("MAD"), "2027-02-10", null));
        assertThrows(IllegalArgumentException.class, () -> new Travellers(2));
    }
}
