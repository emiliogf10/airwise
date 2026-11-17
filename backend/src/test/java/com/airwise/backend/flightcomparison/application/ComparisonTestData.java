package com.airwise.backend.flightcomparison.application;

import java.time.*;
import java.util.*;
import com.airwise.backend.flightcomparison.domain.*;
import com.airwise.backend.flightsearch.application.StableIdentity;
import com.airwise.backend.flightsearch.domain.*;
import static com.airwise.backend.flightcomparison.domain.ComparisonTypes.*;
import static com.airwise.backend.flightsearch.domain.FlightTypes.*;

/** Synthetic data for policies not all represented by the immutable public demo-v1 corpus. */
public final class ComparisonTestData {
    public static final Instant EVALUATION = Instant.parse("2027-01-01T00:00:00Z");
    private ComparisonTestData() {}
    public static FlightSearchRequest query() { return query(12, false, List.of("OPO")); }
    public static FlightSearchRequest query(int day, boolean returning, List<String> origins) {
        return new FlightSearchRequest(returning ? TripType.ROUND_TRIP : TripType.ONE_WAY, origins, "MAD", LocalDate.of(2027,2,day),
                returning ? LocalDate.of(2027,2,day+7) : null, new Travellers(1), Cabin.ECONOMY);
    }
    public static PriceInformation price(String amount, String currency) {
        return price(amount, currency, EvidenceKind.RECENT_SEARCH, Coverage.FULL_ITINERARY, Freshness.FRESH);
    }
    public static PriceInformation price(String amount, String currency, EvidenceKind evidence, Coverage coverage, Freshness freshness) {
        return new PriceInformation(amount == null ? null : Money.parse(amount,currency), amount == null ? UnknownReason.NOT_RETURNED : null,
                evidence, new PriceScope(Basis.GROUP,1,Cabin.ECONOMY,coverage,List.of()), Taxes.INCLUDED,List.of(),
                EVALUATION.minusSeconds(60), EVALUATION, freshness == Freshness.UNKNOWN ? null : freshness == Freshness.EXPIRED ? EVALUATION.minusSeconds(1) : EVALUATION.plusSeconds(600), freshness);
    }
    public static FlightOffer offer(String key, String amount, Long duration, int connections) {
        return offer(query(),"OPO",key,price(amount,"EUR"),duration,connections);
    }
    public static FlightOffer offer(FlightSearchRequest query, String origin, String key, PriceInformation price, Long duration, int connections) {
        var baggage = new BaggageInformation(new BaggageAllowance(BaggageStatus.UNKNOWN,null,null),new BaggageAllowance(BaggageStatus.UNKNOWN,null,null));
        var journeys = new ArrayList<FlightJourney>();
        Long outDuration = duration == null ? null : query.tripType() == TripType.ONE_WAY ? duration : duration / 2;
        journeys.add(journey(key+"-out",origin,"MAD",query.outboundDate(),Direction.OUTBOUND,outDuration,connections));
        if (query.tripType() == TripType.ROUND_TRIP)
            journeys.add(journey(key+"-in","MAD",origin,query.returnDate(),Direction.INBOUND,outDuration,0));
        var itinerary = new FlightItinerary(query.tripType(),journeys,duration,connections);
        String fingerprint = StableIdentity.fingerprint("demo-v1",query);
        return new FlightOffer(StableIdentity.offerId(fingerprint,key),ProviderId.DEMO,DataOrigin.DEMO,itinerary,price,null,baggage,
                StableIdentity.groupId(fingerprint,DataOrigin.DEMO,price,baggage),List.of());
    }
    private static FlightJourney journey(String key,String origin,String destination,LocalDate date,Direction direction,Long duration,int connections) {
        var start = date.atTime(9,0).toInstant(ZoneOffset.UTC);
        var airline = new Airline("D1","Synthetic test airline");
        if (connections == 0) {
            var segment = new FlightSegment(key,origin,destination,time(start,duration!=null),time(duration==null?start:start.plusSeconds(duration),duration!=null),airline,null,null,0,duration);
            return new FlightJourney(direction,List.of(segment),List.of(),duration);
        }
        long third = duration == null ? 0 : duration/3;
        var a = new FlightSegment(key+"-a",origin,"BCN",time(start,duration!=null),time(start.plusSeconds(third),duration!=null),airline,null,null,0,duration==null?null:third);
        var b = new FlightSegment(key+"-b","BCN",destination,time(start.plusSeconds(third*2),duration!=null),time(duration==null?start:start.plusSeconds(duration),duration!=null),airline,null,null,0,duration==null?null:duration-third*2);
        return new FlightJourney(direction,List.of(a,b),List.of(new Connection(0,duration==null?null:third,false,null)),duration);
    }
    private static FlightTime time(Instant instant,boolean known) {
        return known ? new FlightTime(LocalDateTime.ofInstant(instant,ZoneOffset.UTC),"+00:00",null,instant,null)
                : new FlightTime(null,null,null,null,UnknownReason.NOT_RETURNED);
    }
    public static ComparisonRequest request(Primary primary,List<FlightOffer> offers) {
        return new ComparisonRequest(query(),"demo-v1",offers.stream().map(FlightOffer::offerId).toList(),primary,List.of(),List.of());
    }
    public static GroundTransportEstimate estimate(FlightOffer offer,GroundComponent component,String amount,String currency) {
        return new GroundTransportEstimate(offer.offerId(),component,Money.parse(amount,currency),Basis.GROUP,1,EstimateSource.USER_ESTIMATE,EstimateAssumption.USER_PROVIDED);
    }
}
