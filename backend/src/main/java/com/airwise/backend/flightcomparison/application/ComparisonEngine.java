package com.airwise.backend.flightcomparison.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import com.airwise.backend.flightcomparison.domain.*;
import com.airwise.backend.flightsearch.application.DemoSearchService;
import com.airwise.backend.flightsearch.application.StableIdentity;
import com.airwise.backend.flightsearch.domain.*;
import static com.airwise.backend.flightcomparison.domain.ComparisonTypes.*;
import static com.airwise.backend.flightsearch.domain.DomainChecks.*;
import static com.airwise.backend.flightsearch.domain.FlightTypes.*;

/** Deterministic commercial ranking; an ID may order presentation but never break a commercial tie. */
public final class ComparisonEngine {
    public ComparisonResult compare(ComparisonRequest request, List<FlightOffer> selected, Instant evaluationInstant) {
        present(request); present(evaluationInstant);
        selected = list(selected, 2, 4);
        require(selected.size() == request.offerIds().size() && selected.stream().map(FlightOffer::offerId).distinct().count() == selected.size()
                && request.offerIds().containsAll(selected.stream().map(FlightOffer::offerId).toList()), "Selection must match validated offers");
        var groups = new TreeMap<String, List<FlightOffer>>();
        String fingerprint = StableIdentity.fingerprint(request.datasetVersion(), request.query());
        for (var offer : selected) {
            require(offer.itinerary().tripType() == request.query().tripType(), "Incompatible trip modality");
            require(offer.comparisonGroupId().equals(StableIdentity.groupId(fingerprint, offer.dataOrigin(), offer.price(), offer.baggage())), "Offer group outside query context");
            groups.computeIfAbsent(offer.comparisonGroupId(), ignored -> new ArrayList<>()).add(offer);
        }
        var results = new ArrayList<ComparisonResult.Group>();
        groups.forEach((id, offers) -> results.add(rank(id, offers, request, evaluationInstant)));
        return new ComparisonResult(results);
    }

    private ComparisonResult.Group rank(String id, List<FlightOffer> offers, ComparisonRequest request, Instant evaluation) {
        List<Candidate> rows = offers.stream().map(o -> candidate(o, request, evaluation)).toList();
        List<Candidate> eligible = new ArrayList<>(rows.stream().filter(c -> c.eligible).toList());
        Comparator<Candidate> secondary = secondary(request.primary());
        eligible.sort(Comparator.comparing((Candidate c) -> c.primary).thenComparing(secondary).thenComparing(c -> c.offer.offerId()));
        boolean unresolved = false;
        int start = 0;
        while (start < eligible.size()) {
            int end = start + 1;
            while (end < eligible.size() && eligible.get(start).primary.compareTo(eligible.get(end).primary) == 0) end++;
            var block = eligible.subList(start, end);
            if (block.size() > 1 && block.stream().anyMatch(c -> !secondaryKnown(c, request.primary()))) {
                unresolved = true;
                for (Candidate row : block) { row.rank = start + 1; row.reasons.add(Reason.TIE_UNRESOLVED); }
            } else {
                int at = start;
                while (at < end) {
                    int next = at + 1;
                    while (next < end && secondary.compare(eligible.get(at), eligible.get(next)) == 0) next++;
                    for (int i = at; i < next; i++) {
                        eligible.get(i).rank = at + 1;
                        if (next - at > 1) eligible.get(i).reasons.add(Reason.TIE);
                    }
                    at = next;
                }
            }
            start = end;
        }
        if (eligible.size() == 1) eligible.getFirst().reasons.add(Reason.SINGLE_EVALUABLE_OPTION);
        List<String> recommendations = eligible.size() >= 2 && !eligible.getFirst().reasons.contains(Reason.TIE_UNRESOLVED)
                ? eligible.stream().filter(c -> c.rank == 1).map(c -> c.offer.offerId()).sorted().toList() : List.of();
        var ordered = new ArrayList<>(eligible);
        rows.stream().filter(c -> !c.eligible).sorted(Comparator.comparing(c -> c.offer.offerId())).forEach(ordered::add);
        return new ComparisonResult.Group(id, offers.stream().min(Comparator.comparing(FlightOffer::offerId)).orElseThrow(),
                ordered.stream().map(Candidate::view).toList(), eligible.size() == rows.size() && !unresolved, recommendations);
    }

    private Candidate candidate(FlightOffer offer, ComparisonRequest request, Instant evaluation) {
        var row = new Candidate(offer);
        var price = offer.price();
        List<Reason> monetary = DemoSearchService.priceReasons(offer).stream().map(Reason::valueOf).toList();
        row.reasons.addAll(monetary);
        if (price.money() == null) row.missing.add(MissingField.PRICE);
        if (monetary.contains(Reason.PRICE_SCOPE_UNKNOWN)) row.missing.add(MissingField.PRICE_SCOPE);
        if (offer.itinerary().durationSeconds() == null) { row.missing.add(MissingField.DURATION); row.reasons.add(Reason.DURATION_UNKNOWN); }
        Freshness freshness = price.freshnessAt(evaluation);
        if (freshness == Freshness.UNKNOWN) { row.missing.add(MissingField.FRESHNESS); row.reasons.add(Reason.FRESHNESS_UNKNOWN); }
        if (freshness == Freshness.EXPIRED) row.reasons.add(Reason.EXPIRED_DATA);
        if (price.taxes() == Taxes.NOT_INCLUDED || price.taxes() == Taxes.PARTIAL
                || price.additionalCosts().stream().anyMatch(c -> Boolean.FALSE.equals(c.includedInQuotedPrice())))
            row.reasons.add(Reason.ADDITIONAL_COSTS_NOT_INCLUDED);
        row.estimates = request.groundEstimates().stream().filter(e -> e.offerId().equals(offer.offerId()))
                .sorted(Comparator.comparing(GroundTransportEstimate::component)).toList();
        if (request.primary() == Primary.KNOWN_SUBTOTAL) {
            boolean complete = monetary.isEmpty();
            BigDecimal amount = price.money() == null ? null : price.money().amount();
            for (GroundComponent component : request.requiredComponents()) {
                var estimate = row.estimates.stream().filter(e -> e.component() == component).findFirst();
                if (estimate.isEmpty()) {
                    row.missing.add(MissingField.valueOf(component.name())); row.reasons.add(Reason.GROUND_COMPONENT_MISSING); complete = false;
                } else if (price.money() != null && !price.money().currency().equals(estimate.get().money().currency())) {
                    row.reasons.add(Reason.INCOMPATIBLE_CURRENCY); complete = false;
                } else if (amount != null) amount = amount.add(estimate.get().money().amount());
            }
            if (complete) row.subtotal = new Money(amount, price.money().currency());
        }
        row.primary = switch (request.primary()) {
            case FLIGHT_PRICE -> price.money() == null ? null : price.money().amount();
            case FLIGHT_DURATION -> offer.itinerary().durationSeconds() == null ? null : BigDecimal.valueOf(offer.itinerary().durationSeconds());
            case CONNECTIONS -> BigDecimal.valueOf(offer.itinerary().connectionCount());
            case KNOWN_SUBTOTAL -> row.subtotal == null ? null : row.subtotal.amount();
        };
        row.eligible = row.primary != null && freshness == Freshness.FRESH
                && (!(request.primary() == Primary.FLIGHT_PRICE || request.primary() == Primary.KNOWN_SUBTOTAL) || monetary.isEmpty());
        return row;
    }

    private static BigDecimal comparableAmount(Candidate c) {
        return DemoSearchService.priceReasons(c.offer).isEmpty() ? c.offer.price().money().amount() : null;
    }
    private static boolean secondaryKnown(Candidate c, Primary primary) {
        return switch (primary) {
            case FLIGHT_PRICE, KNOWN_SUBTOTAL -> c.offer.itinerary().durationSeconds() != null;
            case FLIGHT_DURATION -> comparableAmount(c) != null;
            case CONNECTIONS -> c.offer.itinerary().durationSeconds() != null && comparableAmount(c) != null;
        };
    }
    private static Comparator<Candidate> secondary(Primary primary) {
        Comparator<Candidate> duration = Comparator.comparing(c -> c.offer.itinerary().durationSeconds(), Comparator.nullsLast(Long::compareTo));
        Comparator<Candidate> connections = Comparator.comparingInt(c -> c.offer.itinerary().connectionCount());
        Comparator<Candidate> amount = Comparator.comparing(ComparisonEngine::comparableAmount, Comparator.nullsLast(BigDecimal::compareTo));
        return switch (primary) {
            case FLIGHT_PRICE, KNOWN_SUBTOTAL -> duration.thenComparing(connections);
            case FLIGHT_DURATION -> connections.thenComparing(amount);
            case CONNECTIONS -> duration.thenComparing(amount);
        };
    }
    private static final class Candidate {
        final FlightOffer offer;
        final EnumSet<MissingField> missing = EnumSet.noneOf(MissingField.class);
        final EnumSet<Reason> reasons = EnumSet.noneOf(Reason.class);
        List<GroundTransportEstimate> estimates = List.of();
        Money subtotal;
        BigDecimal primary;
        Integer rank;
        boolean eligible;
        Candidate(FlightOffer offer) { this.offer = offer; }
        ComparisonResult.Row view() {
            return new ComparisonResult.Row(offer.offerId(), rank, primary != null, offer.price().money(), offer.itinerary().durationSeconds(),
                    offer.itinerary().connectionCount(), subtotal, estimates, List.copyOf(missing), List.copyOf(reasons));
        }
    }
}
