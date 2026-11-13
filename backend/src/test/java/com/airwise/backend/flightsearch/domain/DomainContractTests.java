package com.airwise.backend.flightsearch.domain;

import static com.airwise.backend.flightsearch.domain.FlightTypes.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DomainContractTests {

    private final Instant evaluation = Instant.parse("2027-01-01T00:00:00Z");
    private final PriceScope fullScope = new PriceScope(Basis.GROUP, 1, Cabin.ECONOMY, Coverage.FULL_ITINERARY, List.of());

    private PriceInformation price(Money amount, UnknownReason reason, PriceScope scope,
            Instant observed, Instant retrieved, Instant expiry, Freshness freshness) {
        return new PriceInformation(amount, reason, EvidenceKind.RECENT_SEARCH, scope, Taxes.INCLUDED,
                List.of(), observed, retrieved, expiry, freshness);
    }

    private FlightTime known(String value) {
        return FlightTime.fromLocal(LocalDateTime.parse(value), ZoneId.of("Europe/Madrid"), null);
    }

    private FlightSegment segment(String id, String origin, String destination, String departure, String arrival) {
        var d = known(departure);
        var a = known(arrival);
        return new FlightSegment(id, origin, destination, d, a, null, null, null, 0,
                Duration.between(d.instant(), a.instant()).getSeconds());
    }

    @ParameterizedTest
    @ValueSource(strings = {"1e2", "1E+2", "-1", "+1", "01.00", "00", ".50", "1.", "1,50",
            " 1.00", "1.00 ", "1.000", "1002000000", "99999999999"})
    void rejectsNonContractMonetarySpellingsOrPrecision(String input) {
        assertThrows(IllegalArgumentException.class, () -> Money.parse(input, "EUR"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"eur", "ZZZ", "XXX", "$", "978", "EURO"})
    void rejectsInvalidOrFractionlessCurrencies(String code) {
        assertThrows(IllegalArgumentException.class, () -> Money.parse("1.00", code));
    }

    @Test
    void explicitZeroIsKnownAndCurrencyDeterminesScaleWithoutRounding() {
        var zero = Money.parse("0", "EUR");
        assertEquals(new BigDecimal("0.00"), zero.amount());
        assertEquals(new BigDecimal("100"), Money.parse("100", "JPY").amount());
        assertThrows(IllegalArgumentException.class, () -> Money.parse("1.0", "JPY"));
        assertEquals(new BigDecimal("1.250"), Money.parse("1.25", "KWD").amount());
        assertDoesNotThrow(() -> Money.parse("1001999999.99", "EUR"));
        assertThrows(IllegalArgumentException.class, () -> Money.parse("1000000000", "EUR").requireQuotedAmount());
    }

    @Test
    void moneyUnknownHasReasonAndNeverBecomesZero() {
        var unknown = price(null, UnknownReason.NOT_RETURNED, fullScope, evaluation, evaluation,
                evaluation.plusSeconds(60), Freshness.FRESH);
        assertNull(unknown.money());
        assertEquals(EvidenceKind.RECENT_SEARCH, unknown.evidenceKind());
        assertThrows(IllegalArgumentException.class, () ->
                price(null, null, fullScope, evaluation, evaluation, evaluation.plusSeconds(60), Freshness.FRESH));
        assertThrows(IllegalArgumentException.class, () ->
                price(Money.parse("0", "EUR"), UnknownReason.NOT_RETURNED, fullScope,
                        evaluation, evaluation, evaluation.plusSeconds(60), Freshness.FRESH));
    }

    @Test
    void freshnessIsEvaluatedAtExplicitInstantIncludingExpiryBoundary() {
        var p = price(Money.parse("100", "EUR"), null, fullScope,
                evaluation.minusSeconds(60), evaluation, evaluation.plusSeconds(60), Freshness.FRESH);
        assertEquals(Freshness.FRESH, p.freshnessAt(evaluation));
        assertEquals(Freshness.EXPIRED, p.freshnessAt(evaluation.plusSeconds(60)));
        assertThrows(IllegalArgumentException.class, () -> p.freshnessAt(evaluation.minusSeconds(1)));
        assertThrows(IllegalArgumentException.class, () -> price(null, UnknownReason.NOT_RETURNED, fullScope,
                evaluation.plusSeconds(1), evaluation, null, Freshness.UNKNOWN));
    }

    @Test
    void ambiguousDstDoesNotChooseAnOffsetAndSuppliedOffsetMustMatchZone() {
        var local = LocalDateTime.parse("2027-10-31T02:30:00");
        var ambiguous = FlightTime.fromLocal(local, ZoneId.of("Europe/Madrid"), null);
        assertNull(ambiguous.instant());
        assertEquals(UnknownReason.AMBIGUOUS_LOCAL_TIME, ambiguous.unknownReason());
        assertNull(ambiguous.offset());
        var resolved = FlightTime.fromLocal(local, ZoneId.of("Europe/Madrid"), ZoneOffset.ofHours(2));
        assertEquals(Instant.parse("2027-10-31T00:30:00Z"), resolved.instant());
        assertThrows(IllegalArgumentException.class,
                () -> FlightTime.fromLocal(local, ZoneId.of("Europe/Madrid"), ZoneOffset.ofHours(4)));
        assertThrows(IllegalArgumentException.class, () -> FlightTime.fromLocal(
                LocalDateTime.parse("2027-03-28T02:30:00"), ZoneId.of("Europe/Madrid"), null));
        assertThrows(IllegalArgumentException.class, () ->
                new FlightTime(local, "+02:00", "Europe/Madrid", Instant.parse("2027-10-31T01:30:00Z"), null));
    }

    @Test
    void knownInstantAndZoneCannotContradictEachOther() {
        assertThrows(IllegalArgumentException.class, () -> new FlightTime(
                LocalDateTime.parse("2027-02-10T09:00:00"), "+00:00", "Europe/Madrid",
                Instant.parse("2027-02-10T09:00:00Z"), null));
        assertThrows(IllegalArgumentException.class, () -> new FlightTime(
                LocalDateTime.parse("2027-02-10T09:00:00"), "+01:00", "Europe/Madrid",
                Instant.parse("2027-02-10T09:00:00.001Z"), null));
    }

    @Test
    void connectionChronologyAndDeclaredDurationMustAgree() {
        var a = segment("a", "OPO", "BCN", "2027-02-10T09:00:00", "2027-02-10T10:00:00");
        var b = segment("b", "BCN", "MAD", "2027-02-10T11:00:00", "2027-02-10T12:00:00");
        var journey = new FlightJourney(Direction.OUTBOUND, List.of(a, b),
                List.of(new Connection(0, 3600L, false, null)), 10800L);
        assertEquals(10800L, journey.durationSeconds());
        assertThrows(IllegalArgumentException.class, () -> new FlightJourney(Direction.OUTBOUND, List.of(a, b),
                List.of(new Connection(0, 0L, false, null)), 10800L));
        var early = segment("early", "BCN", "MAD", "2027-02-10T09:30:00", "2027-02-10T10:30:00");
        assertThrows(IllegalArgumentException.class, () -> new FlightJourney(Direction.OUTBOUND, List.of(a, early),
                List.of(new Connection(0, 0L, false, null)), 5400L));
        assertThrows(IllegalArgumentException.class, () -> new FlightSegment("bad", "OPO", "MAD",
                a.departureTime(), a.arrivalTime(), null, null, null, 0, 0L));
    }

    @Test
    void itineraryExcludesStayAndRejectsDuplicateSegmentIdsAcrossDirections() {
        var outSegment = segment("out", "OPO", "MAD", "2027-02-10T09:00:00", "2027-02-10T10:00:00");
        var inSegment = segment("in", "MAD", "OPO", "2027-02-17T18:00:00", "2027-02-17T19:00:00");
        var out = new FlightJourney(Direction.OUTBOUND, List.of(outSegment), List.of(), 3600L);
        var in = new FlightJourney(Direction.INBOUND, List.of(inSegment), List.of(), 3600L);
        var itinerary = new FlightItinerary(TripType.ROUND_TRIP, List.of(out, in), 7200L, 0);
        assertEquals(7200L, itinerary.durationSeconds());
        assertThrows(IllegalArgumentException.class, () ->
                new FlightItinerary(TripType.ROUND_TRIP, List.of(out), 3600L, 0));
        var duplicate = new FlightSegment("out", "MAD", "OPO", inSegment.departureTime(), inSegment.arrivalTime(),
                null, null, null, 0, 3600L);
        var badIn = new FlightJourney(Direction.INBOUND, List.of(duplicate), List.of(), 3600L);
        assertThrows(IllegalArgumentException.class, () ->
                new FlightItinerary(TripType.ROUND_TRIP, List.of(out, badIn), 7200L, 0));
    }

    @Test
    void baggageAndCoverageKeepExplicitUnknownsAndRejectInventedValues() {
        assertThrows(IllegalArgumentException.class, () -> new BaggageAllowance(BaggageStatus.UNKNOWN, 0, null));
        assertThrows(IllegalArgumentException.class, () -> new BaggageAllowance(BaggageStatus.NOT_INCLUDED, null, null));
        assertThrows(IllegalArgumentException.class, () -> new BaggageAllowance(BaggageStatus.INCLUDED, 0, null));
        assertEquals(0, new BaggageAllowance(BaggageStatus.NOT_INCLUDED, 0, null).pieces());
        assertThrows(IllegalArgumentException.class,
                () -> new PriceScope(Basis.GROUP, 1, Cabin.ECONOMY, Coverage.FULL_ITINERARY, List.of("a")));
        assertThrows(IllegalArgumentException.class,
                () -> new PriceScope(Basis.GROUP, 1, Cabin.ECONOMY, Coverage.SEGMENT_ONLY, List.of("a", "a")));
        assertThrows(IllegalArgumentException.class,
                () -> new DataIssue(DataIssueCode.INVALID_PROVIDER_DATA, "/untrusted", "Invalid data."));
    }

    @Test
    void demoProviderCannotClaimRealOriginOrBookingConfirmation() {
        var segment = segment("s", "OPO", "MAD", "2027-02-10T09:00:00", "2027-02-10T10:00:00");
        var journey = new FlightJourney(Direction.OUTBOUND, List.of(segment), List.of(), 3600L);
        var itinerary = new FlightItinerary(TripType.ONE_WAY, List.of(journey), 3600L, 0);
        var p = price(Money.parse("100", "EUR"), null, fullScope, evaluation, evaluation,
                evaluation.plusSeconds(60), Freshness.FRESH);
        var unknown = new BaggageAllowance(BaggageStatus.UNKNOWN, null, null);
        var baggage = new BaggageInformation(unknown, unknown);
        assertThrows(IllegalArgumentException.class, () -> new FlightOffer("of_" + "0".repeat(64),
                ProviderId.DEMO, DataOrigin.REAL, itinerary, p, null, baggage, "g_" + "0".repeat(64), List.of()));
        assertThrows(IllegalArgumentException.class, () -> new PriceObservation("ob_" + "0".repeat(64),
                ProviderId.DEMO, DataOrigin.REAL, "OPO", "MAD", TripType.ONE_WAY, LocalDate.parse("2027-02-10"),
                null, p, List.of()));
        var confirmed = new PriceInformation(p.money(), null, EvidenceKind.BOOKING_CONFIRMED, p.scope(),
                p.taxes(), p.additionalCosts(), p.observedAt(), p.retrievedAt(), p.expiresAt(), p.freshness());
        assertThrows(IllegalArgumentException.class, () -> new FlightOffer("of_" + "0".repeat(64),
                ProviderId.DEMO, DataOrigin.DEMO, itinerary, confirmed, null, baggage, "g_" + "0".repeat(64), List.of()));
        assertThrows(IllegalArgumentException.class, () -> new PriceObservation("ob_" + "0".repeat(64),
                ProviderId.DEMO, DataOrigin.DEMO, "OPO", "MAD", TripType.ONE_WAY, LocalDate.parse("2027-02-10"),
                null, confirmed, List.of()));
    }
}
