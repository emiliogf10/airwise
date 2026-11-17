package com.airwise.backend.flightcomparison.application;

import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import com.airwise.backend.flightcomparison.domain.*;
import com.airwise.backend.flightsearch.domain.*;
import static com.airwise.backend.flightcomparison.application.ComparisonTestData.*;
import static com.airwise.backend.flightcomparison.domain.ComparisonTypes.*;
import static com.airwise.backend.flightsearch.domain.FlightTypes.*;
import static org.assertj.core.api.Assertions.*;

class ComparisonEngineTests {
    private final ComparisonEngine engine = new ComparisonEngine();
    private ComparisonResult.Group compare(Primary primary,FlightOffer... offers) {
        var selected = List.of(offers);
        return engine.compare(request(primary,selected),selected,EVALUATION).groups().getFirst();
    }
    @Test void priceRanksFourOffersAndTreatsExplicitZeroAsKnown() {
        var a=offer("a","100.00",3600L,0); var b=offer("b","90.00",7200L,0);
        var c=offer("c","0.00",3600L,0); var d=offer("d","110.00",3600L,0);
        var group=compare(Primary.FLIGHT_PRICE,a,b,c,d);
        assertThat(group.rows()).extracting(ComparisonResult.Row::offerId).containsExactly(c.offerId(),b.offerId(),a.offerId(),d.offerId());
        assertThat(group.rows()).extracting(ComparisonResult.Row::rank).containsExactly(1,2,3,4);
        assertThat(group.recommendationIds()).containsExactly(c.offerId()); assertThat(group.rankingComplete()).isTrue();
    }
    @Test void commercialSecondaryValuesResolvePriceTieBeforeIds() {
        var slow=offer("slow","100.00",7200L,0); var fast=offer("fast","100.00",3600L,0);
        var group=compare(Primary.FLIGHT_PRICE,slow,fast);
        assertThat(group.rows().getFirst().offerId()).isEqualTo(fast.offerId());
        assertThat(group.recommendationIds()).containsExactly(fast.offerId());
    }
    @Test void fullKnownTieSharesCompetitionRankAndRecommendations() {
        var a=offer("tie-a","100.00",3600L,0);var b=offer("tie-b","100.00",3600L,0);var c=offer("later","120.00",3600L,0);
        var group=compare(Primary.FLIGHT_PRICE,c,b,a);
        assertThat(group.rows()).extracting(ComparisonResult.Row::rank).containsExactly(1,1,3);
        assertThat(group.rows().subList(0,2)).allSatisfy(r -> assertThat(r.reasons()).contains(Reason.TIE));
        assertThat(group.recommendationIds()).containsExactlyElementsOf(List.of(a.offerId(),b.offerId()).stream().sorted().toList());
    }
    @Test void unknownSecondaryBlocksEntirePrimaryTieEvenWhenOtherSecondaryDiffers() {
        var a=offer("unknown-time","100.00",null,1);var b=offer("known-time","100.00",3600L,0);var c=offer("later","120.00",3600L,0);
        var group=compare(Primary.FLIGHT_PRICE,a,b,c);
        assertThat(group.rows()).extracting(ComparisonResult.Row::rank).containsExactly(1,1,3);
        assertThat(group.rows().subList(0,2)).allSatisfy(r -> assertThat(r.reasons()).contains(Reason.TIE_UNRESOLVED));
        assertThat(group.recommendationIds()).isEmpty(); assertThat(group.rankingComplete()).isFalse();
        assertThat(group.rows().getFirst().offerId()).isEqualTo(b.offerId());
    }
    @Test void unresolvedLowerBlockDoesNotEraseUnambiguousLeader() {
        var a=offer("leader","90.00",3600L,0);var b=offer("u","100.00",null,0);var c=offer("k","100.00",3600L,0);
        var group=compare(Primary.FLIGHT_PRICE,c,b,a);
        assertThat(group.recommendationIds()).containsExactly(a.offerId()); assertThat(group.rankingComplete()).isFalse();
        assertThat(group.rows()).extracting(ComparisonResult.Row::rank).containsExactly(1,2,2);
    }
    @Test void durationAndConnectionsHaveDifferentPrimaryOrder() {
        var quickConnection=offer("quick","90.00",3600L,1);var direct=offer("direct","100.00",7200L,0);
        assertThat(compare(Primary.FLIGHT_DURATION,direct,quickConnection).recommendationIds()).containsExactly(quickConnection.offerId());
        assertThat(compare(Primary.CONNECTIONS,direct,quickConnection).recommendationIds()).containsExactly(direct.offerId());
    }
    @Test void nonmonetaryMetricCanRankUnknownPriceButTiedUnknownPriceCannotBreakTie() {
        var a=offer("a",null,3600L,0);var b=offer("b",null,7200L,0);
        var group=compare(Primary.FLIGHT_DURATION,a,b);
        assertThat(group.recommendationIds()).containsExactly(a.offerId()); assertThat(group.rows().getFirst().primaryKnown()).isTrue();
        assertThat(group.rows().getFirst().missingFields()).contains(MissingField.PRICE);
        assertThat(compare(Primary.CONNECTIONS,a,b).recommendationIds()).isEmpty();
        assertThat(compare(Primary.CONNECTIONS,a,b).rows()).allSatisfy(r -> assertThat(r.reasons()).contains(Reason.TIE_UNRESOLVED));
    }
    @Test void currenciesEvidenceAndPartialCoverageStayInDifferentGroupsWithNoGlobalWinner() {
        var q=query();
        var eur=offer(q,"OPO","eur",price("100.00","EUR"),3600L,0);
        var usd=offer(q,"OPO","usd",price("95.00","USD"),3600L,0);
        var cache=offer(q,"OPO","cache",price("90.00","EUR",EvidenceKind.OBSERVED_CACHE,Coverage.FULL_ITINERARY,Freshness.FRESH),3600L,0);
        var unknownScope=offer(q,"OPO","scope",price("80.00","EUR",EvidenceKind.RECENT_SEARCH,Coverage.UNKNOWN,Freshness.FRESH),3600L,0);
        var selected=List.of(eur,usd,cache,unknownScope);
        var result=engine.compare(request(Primary.FLIGHT_PRICE,selected),selected,EVALUATION);
        assertThat(result.groups()).hasSize(4).allSatisfy(g -> assertThat(g.recommendationIds()).isEmpty());
        var partial=result.groups().stream().flatMap(g -> g.rows().stream()).filter(r -> r.offerId().equals(unknownScope.offerId())).findFirst().orElseThrow();
        assertThat(partial.rank()).isNull(); assertThat(partial.primaryKnown()).isTrue(); assertThat(partial.reasons()).contains(Reason.PRICE_SCOPE_UNKNOWN);
    }
    @ParameterizedTest @EnumSource(value=Freshness.class,names={"EXPIRED","UNKNOWN"})
    void nonFreshPricesHaveKnownPrimaryButNoRankOrRecommendation(Freshness freshness) {
        var q=query();var bad=offer(q,"OPO","bad",price("90.00","EUR",EvidenceKind.RECENT_SEARCH,Coverage.FULL_ITINERARY,freshness),3600L,0);
        var good=offer("good","100.00",3600L,0);
        var group=compare(Primary.FLIGHT_PRICE,bad,good);
        var row=group.rows().stream().filter(r -> r.offerId().equals(bad.offerId())).findFirst().orElseThrow();
        assertThat(row.primaryKnown()).isTrue(); assertThat(row.rank()).isNull(); assertThat(group.recommendationIds()).isEmpty();
        assertThat(group.rows().getFirst().reasons()).contains(Reason.SINGLE_EVALUABLE_OPTION);
    }
    @Test void freshnessIsRecomputedAtDatasetEvaluationRatherThanTrustingStoredTag() {
        var expired=price("90.00","EUR",EvidenceKind.RECENT_SEARCH,Coverage.FULL_ITINERARY,Freshness.EXPIRED);
        var inconsistent=new PriceInformation(expired.money(),null,expired.evidenceKind(),expired.scope(),expired.taxes(),List.of(),expired.observedAt(),expired.retrievedAt(),expired.expiresAt(),Freshness.FRESH);
        var a=offer(query(),"OPO","a",inconsistent,3600L,0);var b=offer("b","100.00",3600L,0);
        assertThat(compare(Primary.FLIGHT_PRICE,a,b).rows().getLast().reasons()).contains(Reason.EXPIRED_DATA);
    }
    @Test void subtotalUsesOnlySelectedComponentsAndChangesOrderWithoutChangingFlightPrice() {
        var a=offer("a","100.00",3600L,0);var b=offer("b","110.00",3600L,0);var selected=List.of(a,b);
        var estimates=List.of(estimate(a,GroundComponent.ORIGIN_GROUND_OUTBOUND,"30.00","EUR"),estimate(b,GroundComponent.ORIGIN_GROUND_OUTBOUND,"10.00","EUR"));
        var request=new ComparisonRequest(query(),"demo-v1",selected.stream().map(FlightOffer::offerId).toList(),Primary.KNOWN_SUBTOTAL,List.of(GroundComponent.ORIGIN_GROUND_OUTBOUND),estimates);
        var group=engine.compare(request,selected,EVALUATION).groups().getFirst();
        assertThat(group.recommendationIds()).containsExactly(b.offerId());
        assertThat(group.rows().getFirst().knownSubtotal().amount()).isEqualByComparingTo("120.00");
        assertThat(group.rows().getFirst().flightPrice().amount()).isEqualByComparingTo("110.00");
    }
    @Test void missingRequiredEstimateDoesNotBecomeZeroOrSwitchPrimary() {
        var a=offer("a","100.00",3600L,0);var b=offer("b","110.00",3600L,0);var selected=List.of(a,b);
        var request=new ComparisonRequest(query(),"demo-v1",selected.stream().map(FlightOffer::offerId).toList(),Primary.KNOWN_SUBTOTAL,
                List.of(GroundComponent.ORIGIN_GROUND_OUTBOUND),List.of(estimate(a,GroundComponent.ORIGIN_GROUND_OUTBOUND,"0.00","EUR")));
        var group=engine.compare(request,selected,EVALUATION).groups().getFirst();
        var missing=group.rows().getLast(); assertThat(missing.offerId()).isEqualTo(b.offerId());assertThat(missing.knownSubtotal()).isNull();
        assertThat(missing.primaryKnown()).isFalse();assertThat(missing.rank()).isNull();assertThat(missing.missingFields()).contains(MissingField.ORIGIN_GROUND_OUTBOUND);
        assertThat(group.recommendationIds()).isEmpty(); assertThat(group.rows().getFirst().knownSubtotal().amount()).isEqualByComparingTo("100.00");
    }
    @Test void maximumDerivedSubtotalCanExceedQuotedLimitWithoutRounding() {
        var q=query(10,true,List.of("OPO"));
        var a=offer(q,"OPO","max",price("999999999.99","EUR"),7200L,0);var b=offer(q,"OPO","other",price("999999999.98","EUR"),7200L,0);
        var estimates=new ArrayList<GroundTransportEstimate>();
        for(var offer:List.of(a,b))for(var component:GroundComponent.values())estimates.add(estimate(offer,component,"1000000.00","EUR"));
        var request=new ComparisonRequest(q,"demo-v1",List.of(a.offerId(),b.offerId()),Primary.KNOWN_SUBTOTAL,List.of(GroundComponent.values()),estimates);
        var group=engine.compare(request,List.of(a,b),EVALUATION).groups().getFirst();
        assertThat(group.rows().getLast().knownSubtotal().amount().toPlainString()).isEqualTo("1001999999.99");
    }
    @Test void extraDeclaredReturnEstimateIsVisibleButNotAddedUnlessRequired() {
        var q=query(10,true,List.of("OPO"));var a=offer(q,"OPO","a",price("100.00","EUR"),7200L,0);var b=offer(q,"OPO","b",price("110.00","EUR"),7200L,0);
        var estimates=List.of(estimate(a,GroundComponent.ORIGIN_GROUND_OUTBOUND,"10.00","EUR"),estimate(a,GroundComponent.ORIGIN_GROUND_RETURN,"90.00","EUR"));
        var request=new ComparisonRequest(q,"demo-v1",List.of(a.offerId(),b.offerId()),Primary.KNOWN_SUBTOTAL,List.of(GroundComponent.ORIGIN_GROUND_OUTBOUND),estimates);
        var row=engine.compare(request,List.of(a,b),EVALUATION).groups().getFirst().rows().getFirst();
        assertThat(row.groundEstimates()).hasSize(2); assertThat(row.knownSubtotal().amount()).isEqualByComparingTo("110.00");
    }
    @Test void unknownPrimaryRowsRemainAtEndWithNullRank() {
        var a=offer("a","100.00",null,0);var b=offer("b","110.00",3600L,0);
        var group=compare(Primary.FLIGHT_DURATION,a,b);
        assertThat(group.rows().getLast().rank()).isNull();assertThat(group.rows().getLast().primaryKnown()).isFalse();
        assertThat(group.rows().getLast().reasons()).contains(Reason.DURATION_UNKNOWN);
        assertThat(group.recommendationIds()).isEmpty();
    }

    @Test void allExpiredOffersKeepKnownPricesButNoRankOrCurrentRecommendation() {
        var q=query();
        var a=offer(q,"OPO","expired-a",price("100.00","EUR",EvidenceKind.RECENT_SEARCH,Coverage.FULL_ITINERARY,Freshness.EXPIRED),3600L,0);
        var b=offer(q,"OPO","expired-b",price("110.00","EUR",EvidenceKind.RECENT_SEARCH,Coverage.FULL_ITINERARY,Freshness.EXPIRED),3600L,0);
        var group=compare(Primary.FLIGHT_PRICE,a,b);
        assertThat(group.rows()).allSatisfy(row -> {
            assertThat(row.primaryKnown()).isTrue();assertThat(row.rank()).isNull();assertThat(row.reasons()).contains(Reason.EXPIRED_DATA);
        });
        assertThat(group.recommendationIds()).isEmpty();assertThat(group.rankingComplete()).isFalse();
    }

    @ParameterizedTest @EnumSource(value=EvidenceKind.class,names={"ESTIMATE","UNKNOWN"})
    void unsupportedMonetaryEvidenceNeverRanksKnownAmounts(EvidenceKind evidence) {
        var q=query();
        var a=offer(q,"OPO","unsupported-a",price("100.00","EUR",evidence,Coverage.FULL_ITINERARY,Freshness.FRESH),3600L,0);
        var b=offer(q,"OPO","unsupported-b",price("110.00","EUR",evidence,Coverage.FULL_ITINERARY,Freshness.FRESH),3600L,0);
        var group=compare(Primary.FLIGHT_PRICE,a,b);
        assertThat(group.rows()).allSatisfy(row -> {
            assertThat(row.primaryKnown()).isTrue();assertThat(row.rank()).isNull();assertThat(row.reasons()).contains(Reason.EVIDENCE_UNSUPPORTED);
        });
        assertThat(group.recommendationIds()).isEmpty();assertThat(group.rankingComplete()).isFalse();
    }

    @Test void partialMonetaryScopeDoesNotEraseKnownNonmonetaryMetric() {
        var q=query(10,true,List.of("OPO"));
        var a=offer(q,"OPO","partial-a",price("100.00","EUR",EvidenceKind.RECENT_SEARCH,Coverage.OUTBOUND_ONLY,Freshness.FRESH),7200L,0);
        var b=offer(q,"OPO","partial-b",price("110.00","EUR",EvidenceKind.RECENT_SEARCH,Coverage.OUTBOUND_ONLY,Freshness.FRESH),8400L,0);
        var request=new com.airwise.backend.flightcomparison.domain.ComparisonRequest(q,"demo-v1",List.of(a.offerId(),b.offerId()),Primary.FLIGHT_DURATION,List.of(),List.of());
        var group=engine.compare(request,List.of(a,b),EVALUATION).groups().getFirst();
        assertThat(group.recommendationIds()).containsExactly(a.offerId());assertThat(group.rankingComplete()).isTrue();
        assertThat(group.rows().getFirst().missingFields()).contains(MissingField.PRICE_SCOPE);
        assertThat(group.rows().getFirst().primaryKnown()).isTrue();assertThat(group.rows().getFirst().rank()).isEqualTo(1);
    }
}
