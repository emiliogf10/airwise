package com.airwise.backend.flightcomparison.api;

import java.util.*;
import com.airwise.backend.api.NormalizedViews;
import com.airwise.backend.flightcomparison.domain.*;
import com.airwise.backend.flightsearch.application.*;
import com.airwise.backend.flightsearch.domain.FlightOffer;

/** HTTP-only projections; records from domain/application are not serialized directly. */
final class ComparisonViews {
    private ComparisonViews() {}
    record Context(String datasetVersion, String fingerprint, NormalizedViews.Query query, String dataOrigin, String evaluationInstant) {}
    record Estimate(String offerId, String component, NormalizedViews.MoneyView money, String basis, int travellerCount, String source, String assumption) {}
    record CostKind(String kind, Boolean includedInQuotedPrice, String currency) {}
    record PriceGroup(String groupId, String dataOrigin, String evidenceKind, String currency, NormalizedViews.Scope scope,
            String taxes, NormalizedViews.Baggage baggage, List<CostKind> additionalCostKinds, boolean monetarilyComparable, List<String> reasons) {}
    record Row(String offerId, Integer rank, boolean primaryKnown, NormalizedViews.MoneyView flightPrice, Long durationSeconds,
            int connectionCount, NormalizedViews.MoneyView knownSubtotal, List<Estimate> groundEstimates, List<String> missingFields, List<String> reasons) {}
    record Group(String groupId, PriceGroup priceGroup, List<Row> rows, boolean rankingComplete, List<String> recommendationIds, String recommendationScope) {}
    record Response(String contractVersion, String correlationId, Context context, String primary, List<String> requiredComponents,
            List<Group> groups, List<NormalizedViews.Warning> warnings) {}
    static Group group(ComparisonResult.Group group) {
        return new Group(group.groupId(), priceGroup(group.representative()), group.rows().stream().map(r ->
                new Row(r.offerId(), r.rank(), r.primaryKnown(), NormalizedViews.money(r.flightPrice()), r.durationSeconds(), r.connectionCount(),
                        NormalizedViews.money(r.knownSubtotal()), r.groundEstimates().stream().map(e -> new Estimate(e.offerId(), e.component().name(),
                                NormalizedViews.money(e.money()), e.basis().name(), e.travellerCount(), e.source().name(), e.assumption().name())).toList(),
                        r.missingFields().stream().map(Enum::name).toList(), r.reasons().stream().map(Enum::name).toList())).toList(),
                group.rankingComplete(), group.recommendationIds(), "EVALUABLE_GROUP");
    }
    private static PriceGroup priceGroup(FlightOffer offer) {
        var price = offer.price(); var reasons = DemoSearchService.priceReasons(offer);
        return new PriceGroup(offer.comparisonGroupId(), offer.dataOrigin().name(), price.evidenceKind().name(),
                price.money() == null ? null : price.money().currency(), NormalizedViews.scope(price.scope()), price.taxes().name(), NormalizedViews.baggage(offer.baggage()),
                price.additionalCosts().stream().sorted(Comparator.comparing(c -> c.kind().name()))
                        .map(c -> new CostKind(c.kind().name(), c.includedInQuotedPrice(), c.money() == null ? null : c.money().currency())).toList(), reasons.isEmpty(), reasons);
    }
}
