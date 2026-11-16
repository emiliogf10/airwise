package com.airwise.backend.flightsearch.application;

import static com.airwise.backend.flightsearch.domain.FlightTypes.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class ProviderCapabilitiesTests {
    private ProviderCapabilities capabilities(CapabilityState state, List<OutputKind> outputs) {
        return new ProviderCapabilities(ProviderId.DEMO, state, state, state, state, state, state,
                outputs, 1, false, false);
    }

    @Test
    void unverifiedOrUnsupportedCapabilitiesCannotAdvertiseOutputs() {
        for (var state : List.of(CapabilityState.UNVERIFIED, CapabilityState.UNSUPPORTED)) {
            for (var outputs : List.of(List.of(OutputKind.OFFER), List.of(OutputKind.OBSERVATION),
                    List.of(OutputKind.OFFER, OutputKind.OBSERVATION))) {
                assertThrows(IllegalArgumentException.class, () -> capabilities(state, outputs));
            }
        }
        assertThrows(IllegalArgumentException.class, () -> new ProviderCapabilities(ProviderId.DEMO,
                CapabilityState.UNSUPPORTED, CapabilityState.UNVERIFIED, CapabilityState.UNSUPPORTED,
                CapabilityState.UNVERIFIED, CapabilityState.UNSUPPORTED, CapabilityState.UNVERIFIED,
                List.of(OutputKind.OFFER), 1, false, false));
    }

    @Test
    void noVerifiedCapabilityAllowsEmptyOutputKinds() {
        assertTrue(capabilities(CapabilityState.UNVERIFIED, List.of()).outputKinds().isEmpty());
        assertTrue(capabilities(CapabilityState.UNSUPPORTED, List.of()).outputKinds().isEmpty());
    }

    @Test
    void verifiedOutputImplicationsAndValidDemoRemainIntact() {
        assertThrows(IllegalArgumentException.class, () -> capabilities(CapabilityState.SUPPORTED, List.of()));
        assertThrows(IllegalArgumentException.class, () -> capabilities(CapabilityState.SUPPORTED, List.of(OutputKind.OFFER)));
        assertEquals(2, capabilities(CapabilityState.SUPPORTED,
                List.of(OutputKind.OFFER, OutputKind.OBSERVATION)).outputKinds().size());
    }
}
