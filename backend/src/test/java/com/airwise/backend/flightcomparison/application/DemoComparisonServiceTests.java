package com.airwise.backend.flightcomparison.application;

import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import com.airwise.backend.flightcomparison.domain.*;
import com.airwise.backend.flightprovider.demo.DemoFlightProvider;
import com.airwise.backend.flightsearch.application.*;
import com.airwise.backend.flightsearch.domain.*;
import static com.airwise.backend.flightcomparison.application.ComparisonTestData.*;
import static com.airwise.backend.flightcomparison.domain.ComparisonTypes.*;
import static com.airwise.backend.flightsearch.domain.FlightTypes.*;
import static org.assertj.core.api.Assertions.*;

class DemoComparisonServiceTests {
    private final Clock clock = Clock.fixed(Instant.parse("2030-01-01T00:00:00Z"), ZoneOffset.UTC);
    private final DemoFlightProvider demo = new DemoFlightProvider(clock);
    private final DemoCatalog catalog = new DemoCatalog(demo.dataset(), demo.airports());
    private ProviderExecutionContext context() { return new ProviderExecutionContext(UUID.randomUUID(), clock.instant().plusSeconds(2)); }
    private ComparisonRequest request(FlightSearchRequest query,List<FlightOffer> offers) {
        return new ComparisonRequest(query,"demo-v1",offers.stream().map(FlightOffer::offerId).toList(),Primary.FLIGHT_PRICE,List.of(),List.of());
    }
    private DemoComparisonService service(FlightProvider provider) {
        return new DemoComparisonService(new DemoSearchService(provider,catalog,clock),catalog,new ComparisonEngine());
    }

    @Test void selectionAfterTheFiftiethOfferIsFoundWithExactlyOneFanOut() {
        var q=query(12,false,List.of("SCQ","OPO","BCN"));
        var all=new ArrayList<FlightOffer>();
        for(String origin:q.origins())all.addAll(twenty(q,origin));
        all.sort(Comparator.comparing(FlightOffer::offerId));
        var selected=List.of(all.getFirst(),all.getLast());
        var calls=new AtomicInteger();
        var contexts=new ArrayList<ProviderExecutionContext>();
        FlightProvider provider=new FlightProvider() {
            public ProviderCapabilities capabilities() { return demo.capabilities(); }
            public boolean supports(ProviderSearchRequest request) { return true; }
            public ProviderResult search(ProviderSearchRequest request) {
                calls.incrementAndGet();contexts.add(request.context());
                return new ProviderResult(ProviderId.DEMO,request.origin(),ProviderStatus.COMPLETE,1,1,twenty(q,request.origin()),List.of(),List.of());
            }
        };
        var operation=context();
        var result=service(provider).compare(request(q,selected),operation);
        assertThat(calls).hasValue(3);
        assertThat(contexts).allSatisfy(c -> { assertThat(c.correlationId()).isEqualTo(operation.correlationId()); assertThat(c.deadlineAt()).isEqualTo(operation.deadlineAt()); });
        assertThat(result.comparison().groups().stream().flatMap(g -> g.rows().stream()).map(ComparisonResult.Row::offerId).toList())
                .containsExactlyInAnyOrderElementsOf(selected.stream().map(FlightOffer::offerId).toList());
        assertThat(result.fingerprint()).isEqualTo(StableIdentity.fingerprint("demo-v1",q));
    }

    @Test void partialBaseRetainsEveryDistinctOperationalCauseAndLimitsRecommendationToSelectedGroup() {
        var q=query(12,false,List.of("BCN","OPO","SCQ"));
        var selected=List.of(offer(q,"OPO","a",price("100.00","EUR"),3600L,0),offer(q,"OPO","b",price("120.00","EUR"),3600L,0));
        FlightProvider provider=new FlightProvider() {
            public ProviderCapabilities capabilities() { return demo.capabilities(); }
            public boolean supports(ProviderSearchRequest request) { return true; }
            public ProviderResult search(ProviderSearchRequest request) {
                return switch(request.origin()) {
                    case "BCN" -> new ProviderResult(ProviderId.DEMO,"BCN",ProviderStatus.EMPTY,1,1,List.of(),List.of(),List.of());
                    case "OPO" -> new ProviderResult(ProviderId.DEMO,"OPO",ProviderStatus.PARTIAL,3,1,selected,List.of(),
                            List.of(new ProviderIssue(ProviderIssueCode.PROVIDER_QUOTA_EXHAUSTED,"synthetic-secret"),ProviderIssue.of(ProviderIssueCode.PROVIDER_OFFLINE)));
                    default -> new ProviderResult(ProviderId.DEMO,"SCQ",ProviderStatus.FAILED,1,0,List.of(),List.of(),
                            List.of(ProviderIssue.of(ProviderIssueCode.PROVIDER_TIMEOUT),ProviderIssue.of(ProviderIssueCode.PROVIDER_OFFLINE)));
                };
            }
        };
        var result=service(provider).compare(request(q,selected),context());
        assertThat(result.providerIssueCodes()).containsExactly(ProviderIssueCode.PROVIDER_OFFLINE,ProviderIssueCode.PROVIDER_QUOTA_EXHAUSTED,ProviderIssueCode.PROVIDER_TIMEOUT);
        assertThat(result.comparison().groups()).hasSize(1);
        assertThat(result.comparison().groups().getFirst().recommendationIds()).containsExactly(selected.getFirst().offerId());
    }

    @Test void unknownSelectionIsRejectedAgainstReconstructedServerSet() {
        var q=query();var search=new DemoSearchService(demo,catalog,clock);
        var valid=search.reconstruct(q,"demo-v1",context()).offers().getFirst();
        var request=new ComparisonRequest(q,"demo-v1",List.of(valid.offerId(),"of_"+"0".repeat(64)),Primary.FLIGHT_PRICE,List.of(),List.of());
        assertThatThrownBy(() -> service(demo).compare(request,context())).isInstanceOfSatisfying(DemoComparisonService.SelectionInvalid.class,
                e -> { assertThat(e.path()).isEqualTo("/offerIds/1");assertThat(e.code()).isEqualTo("UNKNOWN_OFFER_ID"); });
    }

    @Test void validDifferentCurrencyIsAnInvalidCombinationRatherThanAnInvalidCurrencyCode() {
        var q=query(10,true,List.of("OPO","SCQ"));
        var offers=new DemoSearchService(demo,catalog,clock).reconstruct(q,"demo-v1",context()).offers();
        var estimate=estimate(offers.getFirst(),GroundComponent.ORIGIN_GROUND_OUTBOUND,"10.00","USD");
        var request=new ComparisonRequest(q,"demo-v1",offers.stream().map(FlightOffer::offerId).toList(),Primary.KNOWN_SUBTOTAL,
                List.of(GroundComponent.ORIGIN_GROUND_OUTBOUND),List.of(estimate));
        assertThatThrownBy(() -> service(demo).compare(request,context())).isInstanceOfSatisfying(DemoComparisonService.SelectionInvalid.class,
                e -> { assertThat(e.path()).isEqualTo("/groundEstimates/0/money/currency");assertThat(e.code()).isEqualTo("INVALID_COMBINATION"); });
    }

    @Test void unavailableDatasetIsRejectedBeforeAnyProviderExecution() {
        var calls=new AtomicInteger();
        FlightProvider provider=new FlightProvider() {
            public ProviderCapabilities capabilities() { return demo.capabilities(); }
            public boolean supports(ProviderSearchRequest request) { return true; }
            public ProviderResult search(ProviderSearchRequest request) { calls.incrementAndGet();throw new AssertionError(); }
        };
        var selected=List.of(offer("a","100.00",3600L,0),offer("b","110.00",3600L,0));
        var request=new ComparisonRequest(query(),"retired-v0",selected.stream().map(FlightOffer::offerId).toList(),Primary.FLIGHT_PRICE,List.of(),List.of());
        assertThatThrownBy(() -> service(provider).compare(request,context())).isInstanceOf(DemoCatalog.VersionUnavailable.class);
        assertThat(calls).hasValue(0);
    }

    private List<FlightOffer> twenty(FlightSearchRequest query,String origin) {
        return java.util.stream.IntStream.range(0,20).mapToObj(i -> offer(query,origin,origin+"-"+i,price(Integer.toString(100+i),"EUR"),3600L,0)).toList();
    }
}
