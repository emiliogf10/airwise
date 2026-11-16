package com.airwise.backend.api;

import java.time.format.DateTimeFormatter;
import java.util.List;
import com.airwise.backend.airport.domain.Airport;
import com.airwise.backend.flightsearch.application.DatasetDescriptor;
import com.airwise.backend.flightsearch.application.ProviderCapabilities;
import com.airwise.backend.flightsearch.domain.*;

/** Explicit HTTP projections. Domain objects and provider implementation details never become response bodies. */
public final class NormalizedViews {
    private static final DateTimeFormatter LOCAL = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss");
    private NormalizedViews() {}
    private static String name(Enum<?> value) { return value == null ? null : value.name(); }
    private static String text(Object value) { return value == null ? null : value.toString(); }

    public record AirportView(String code, String name, String city, String countryCode, String timeZone) {}
    public record CurrencyView(String code, int minorUnits) {}
    public record Capabilities(String providerId, String itineraries, String priceObservations, String oneWay,
            String roundTrip, String sellerDetails, String baggageDetails, List<String> outputKinds,
            int maxOriginsPerCall, boolean requiresNetwork, boolean requiresCredentials) {}
    public record Dataset(String datasetVersion, String evaluationInstant, String referenceDate,
            String calendarStart, String calendarEnd, List<CurrencyView> currencies, List<String> airlineCodes,
            Capabilities capabilities) {}
    public record TravellersView(int adults) {}
    public record Query(String tripType, List<String> origins, String destination, String outboundDate,
            String returnDate, TravellersView travellers, String cabin) {}
    public record MoneyView(String amount, String currency) {}
    public record Scope(String basis, Integer travellerCount, String cabin, String coverage, List<String> coveredSegmentIds) {}
    public record ExtraCost(String kind, MoneyView money, String unknownReason, Boolean includedInQuotedPrice) {}
    public record Price(MoneyView money, String unknownReason, String evidenceKind, Scope scope, String taxes,
            List<ExtraCost> additionalCosts, String observedAt, String retrievedAt, String expiresAt, String freshness) {}
    public record Allowance(String status, Integer pieces, String weightKg) {}
    public record Baggage(Allowance cabin, Allowance checked) {}
    public record AirlineView(String code, String name) {}
    public record Time(String localDateTime, String offset, String zoneId, String instant, String unknownReason) {}
    public record Segment(String segmentId, String departureAirport, String arrivalAirport, Time departureTime,
            Time arrivalTime, AirlineView marketingAirline, AirlineView operatingAirline, String flightNumber,
            Integer technicalStops, Long durationSeconds) {}
    public record ConnectionView(int afterSegmentIndex, Long durationSeconds, boolean airportChange, Boolean selfTransfer) {}
    public record Journey(String direction, List<Segment> segments, List<ConnectionView> connections, Long durationSeconds) {}
    public record Itinerary(String tripType, List<Journey> journeys, Long durationSeconds, int connectionCount) {}
    public record SellerView(String id, String name) {}
    public record Warning(String code, String path, String message) {}
    public record Offer(String offerId, String providerId, String dataOrigin, Itinerary itinerary, Price price,
            SellerView seller, Baggage baggage, String comparisonGroupId, List<Warning> warnings) {}
    public record Observation(String observationId, String providerId, String dataOrigin, String origin,
            String destination, String tripType, String outboundDate, String returnDate, Price price, List<Warning> warnings) {}

    public static AirportView airport(Airport a) { return new AirportView(a.code(), a.name(), a.city(), a.countryCode(), a.timeZone()); }
    public static Capabilities capabilities(ProviderCapabilities c) {
        return new Capabilities(name(c.providerId()), name(c.itineraries()), name(c.priceObservations()), name(c.oneWay()),
                name(c.roundTrip()), name(c.sellerDetails()), name(c.baggageDetails()), c.outputKinds().stream().map(Enum::name).toList(),
                c.maxOriginsPerCall(), c.requiresNetwork(), c.requiresCredentials());
    }
    public static Dataset dataset(DatasetDescriptor d) {
        return new Dataset(d.datasetVersion(), text(d.evaluationInstant()), text(d.referenceDate()), text(d.calendarStart()),
                text(d.calendarEnd()), d.currencies().stream().map(c -> new CurrencyView(c.code(), c.minorUnits())).toList(),
                d.airlineCodes(), capabilities(d.capabilities()));
    }
    public static Query query(FlightSearchRequest q) {
        return new Query(name(q.tripType()), q.origins(), q.destination(), text(q.outboundDate()), text(q.returnDate()),
                new TravellersView(q.travellers().adults()), name(q.cabin()));
    }
    public static MoneyView money(Money m) { return m == null ? null : new MoneyView(m.amount().toPlainString(), m.currency()); }
    public static Scope scope(PriceScope s) { return new Scope(name(s.basis()), s.travellerCount(), name(s.cabin()), name(s.coverage()), s.coveredSegmentIds()); }
    public static Price price(PriceInformation p) {
        return new Price(money(p.money()), name(p.unknownReason()), name(p.evidenceKind()), scope(p.scope()), name(p.taxes()),
                p.additionalCosts().stream().map(c -> new ExtraCost(name(c.kind()), money(c.money()), name(c.unknownReason()), c.includedInQuotedPrice())).toList(),
                text(p.observedAt()), text(p.retrievedAt()), text(p.expiresAt()), name(p.freshness()));
    }
    private static Allowance allowance(BaggageAllowance b) {
        return new Allowance(name(b.status()), b.pieces(), b.weightKg() == null ? null : b.weightKg().stripTrailingZeros().toPlainString());
    }
    public static Baggage baggage(BaggageInformation b) { return new Baggage(allowance(b.cabin()), allowance(b.checked())); }
    private static AirlineView airline(Airline a) { return a == null ? null : new AirlineView(a.code(), a.name()); }
    private static Time time(FlightTime t) {
        return new Time(t.localDateTime() == null ? null : LOCAL.format(t.localDateTime()), t.offset(), t.zoneId(), text(t.instant()), name(t.unknownReason()));
    }
    private static Segment segment(FlightSegment s) {
        return new Segment(s.segmentId(), s.departureAirport(), s.arrivalAirport(), time(s.departureTime()), time(s.arrivalTime()),
                airline(s.marketingAirline()), airline(s.operatingAirline()), s.flightNumber(), s.technicalStops(), s.durationSeconds());
    }
    public static Itinerary itinerary(FlightItinerary i) {
        return new Itinerary(name(i.tripType()), i.journeys().stream().map(j -> new Journey(name(j.direction()),
                j.segments().stream().map(NormalizedViews::segment).toList(), j.connections().stream().map(c ->
                new ConnectionView(c.afterSegmentIndex(), c.durationSeconds(), c.airportChange(), c.selfTransfer())).toList(), j.durationSeconds())).toList(),
                i.durationSeconds(), i.connectionCount());
    }
    public static Warning warning(DataIssue w) {
        String message = switch (w.code()) {
            case PRICE_UNKNOWN -> "Importe de vuelo desconocido.";
            case CURRENCY_UNKNOWN -> "Moneda desconocida.";
            case PRICE_SCOPE_UNKNOWN -> "Ámbito del precio incompleto o desconocido.";
            case TIME_UNKNOWN -> "Horario desconocido.";
            case TIME_AMBIGUOUS -> "Horario local ambiguo.";
            case EXPIRED_DATA -> "Dato caducado en el escenario.";
            case FRESHNESS_UNKNOWN -> "Vigencia desconocida.";
            case BAGGAGE_UNKNOWN -> "Información de equipaje desconocida.";
            case ADDITIONAL_COST_UNKNOWN -> "Coste adicional desconocido.";
            case INVALID_PROVIDER_DATA -> "Dato de fuente inválido.";
            case CONNECTION_UNVERIFIED -> "Conexión sin garantía de traslado.";
            case OBSERVATION_ONLY -> "Observación sin itinerario completo.";
        };
        return new Warning(name(w.code()), w.path(), message);
    }
    public static Offer offer(FlightOffer o) {
        return new Offer(o.offerId(), name(o.providerId()), name(o.dataOrigin()), itinerary(o.itinerary()), price(o.price()),
                o.seller() == null ? null : new SellerView(o.seller().id(), o.seller().name()), baggage(o.baggage()), o.comparisonGroupId(),
                o.warnings().stream().map(NormalizedViews::warning).toList());
    }
    public static Observation observation(PriceObservation o) {
        return new Observation(o.observationId(), name(o.providerId()), name(o.dataOrigin()), o.origin(), o.destination(), name(o.tripType()),
                text(o.outboundDate()), text(o.returnDate()), price(o.price()), o.warnings().stream().map(NormalizedViews::warning).toList());
    }
}
