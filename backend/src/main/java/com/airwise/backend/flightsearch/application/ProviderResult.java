package com.airwise.backend.flightsearch.application;

import java.util.List;
import com.airwise.backend.flightsearch.domain.FlightOffer;
import com.airwise.backend.flightsearch.domain.PriceObservation;
import static com.airwise.backend.flightsearch.domain.DomainChecks.*;
import static com.airwise.backend.flightsearch.domain.FlightTypes.*;

public record ProviderResult(ProviderId providerId, String origin, ProviderStatus status, int requiredUnits,
        int completedUnits, List<FlightOffer> offers, List<PriceObservation> observations, List<ProviderIssue> issues) {
    public ProviderResult {
        present(providerId); airport(origin); present(status);
        require(requiredUnits >= 1 && requiredUnits <= 16 && completedUnits >= 0 && completedUnits <= requiredUnits,
                "Invalid logical units");
        offers = list(offers, 0, 20); observations = list(observations, 0, 20); issues = list(issues, 0, 20);
        require(offers.stream().map(FlightOffer::offerId).distinct().count() == offers.size(), "Duplicate offer");
        require(observations.stream().map(PriceObservation::observationId).distinct().count() == observations.size(), "Duplicate observation");
        for (FlightOffer offer : offers) {
            require(offer.providerId() == providerId
                    && offer.itinerary().journeys().getFirst().segments().getFirst().departureAirport().equals(origin),
                    "Offer outside provider context");
        }
        for (PriceObservation observation : observations) {
            require(observation.providerId() == providerId && observation.origin().equals(origin), "Observation outside provider context");
        }
        boolean data = !offers.isEmpty() || !observations.isEmpty();
        boolean capabilityOnly = !issues.isEmpty() && issues.stream().allMatch(i ->
                i.code() == ProviderIssueCode.CAPABILITY_UNSUPPORTED || i.code() == ProviderIssueCode.CAPABILITY_UNVERIFIED);
        switch (status) {
            case COMPLETE -> require(completedUnits == requiredUnits && data && issues.isEmpty(), "Invalid complete result");
            case EMPTY -> require(completedUnits == requiredUnits && !data && issues.isEmpty(), "Invalid empty result");
            case PARTIAL -> require(completedUnits > 0 && completedUnits < requiredUnits && !issues.isEmpty(), "Invalid partial result");
            case FAILED -> require(completedUnits == 0 && !data && !issues.isEmpty() && !capabilityOnly, "Invalid failed result");
            case UNSUPPORTED -> require(completedUnits == 0 && !data && capabilityOnly, "Invalid unsupported result");
        }
    }
}
