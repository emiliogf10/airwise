package com.airwise.backend.flightsearch.application;

public interface FlightProvider {
    ProviderCapabilities capabilities();

    /** Admission is side-effect free; an unsupported query never initiates transport. */
    boolean supports(ProviderSearchRequest request);

    /** A result belongs to exactly one requested origin; observations need no invented itinerary. */
    ProviderResult search(ProviderSearchRequest request);
}
