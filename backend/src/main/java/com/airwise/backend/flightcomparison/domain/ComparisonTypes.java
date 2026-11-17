package com.airwise.backend.flightcomparison.domain;

public final class ComparisonTypes {
    private ComparisonTypes() {}
    public enum Primary { FLIGHT_PRICE, FLIGHT_DURATION, CONNECTIONS, KNOWN_SUBTOTAL }
    public enum GroundComponent { ORIGIN_GROUND_OUTBOUND, ORIGIN_GROUND_RETURN }
    public enum EstimateSource { USER_ESTIMATE }
    public enum EstimateAssumption { USER_PROVIDED }
    public enum MissingField { PRICE, PRICE_SCOPE, DURATION, FRESHNESS, ORIGIN_GROUND_OUTBOUND, ORIGIN_GROUND_RETURN }
    public enum Reason { PRICE_UNKNOWN, PRICE_SCOPE_UNKNOWN, EVIDENCE_UNSUPPORTED, FRESHNESS_UNKNOWN,
        EXPIRED_DATA, DURATION_UNKNOWN, GROUND_COMPONENT_MISSING, INCOMPATIBLE_CURRENCY,
        ADDITIONAL_COSTS_NOT_INCLUDED, SINGLE_EVALUABLE_OPTION, TIE, TIE_UNRESOLVED }
}
