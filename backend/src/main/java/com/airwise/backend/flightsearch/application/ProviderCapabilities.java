package com.airwise.backend.flightsearch.application;

import java.util.List;
import static com.airwise.backend.flightsearch.domain.DomainChecks.*;
import static com.airwise.backend.flightsearch.domain.FlightTypes.*;

public record ProviderCapabilities(ProviderId providerId, CapabilityState itineraries, CapabilityState priceObservations,
        CapabilityState oneWay, CapabilityState roundTrip, CapabilityState sellerDetails, CapabilityState baggageDetails,
        List<OutputKind> outputKinds, int maxOriginsPerCall, boolean requiresNetwork, boolean requiresCredentials) {
    public ProviderCapabilities {
        present(providerId); present(itineraries); present(priceObservations); present(oneWay);
        present(roundTrip); present(sellerDetails); present(baggageDetails);
        outputKinds = list(outputKinds, 0, 2);
        require(outputKinds.stream().distinct().count() == outputKinds.size(), "Duplicate output kind");
        require(maxOriginsPerCall == 1, "Provider port operates on one origin");
        require(itineraries != CapabilityState.SUPPORTED || outputKinds.contains(OutputKind.OFFER), "Missing offer output");
        require(priceObservations != CapabilityState.SUPPORTED || outputKinds.contains(OutputKind.OBSERVATION), "Missing observation output");
        require(outputKinds.isEmpty() || List.of(itineraries, priceObservations, oneWay, roundTrip,
                sellerDetails, baggageDetails).contains(CapabilityState.SUPPORTED),
                "Output kinds require a verified supported capability");
    }
}
