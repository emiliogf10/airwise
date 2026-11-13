package com.airwise.backend.flightsearch.domain;

import java.time.Instant;
import java.util.List;
import static com.airwise.backend.flightsearch.domain.DomainChecks.*;
import static com.airwise.backend.flightsearch.domain.FlightTypes.*;

public record PriceInformation(Money money, UnknownReason unknownReason, EvidenceKind evidenceKind, PriceScope scope,
        Taxes taxes, List<AdditionalCost> additionalCosts, Instant observedAt, Instant retrievedAt,
        Instant expiresAt, Freshness freshness) {
    public PriceInformation {
        present(evidenceKind); present(scope); present(taxes); present(retrievedAt); present(freshness);
        require((money == null) == (unknownReason != null), "Missing price reason");
        if (money != null) money.requireQuotedAmount();
        additionalCosts = list(additionalCosts, 0, 5);
        require(additionalCosts.stream().map(AdditionalCost::kind).distinct().count() == additionalCosts.size(), "Duplicate additional cost");
        instant(observedAt); instant(retrievedAt); instant(expiresAt);
        require(observedAt == null || !observedAt.isAfter(retrievedAt), "Observation after retrieval");
        require(observedAt == null || expiresAt == null || !expiresAt.isBefore(observedAt), "Expiry before observation");
        require(freshness != Freshness.FRESH || (observedAt != null && expiresAt != null), "Freshness lacks evidence");
        require(freshness != Freshness.EXPIRED || expiresAt != null, "Expiry lacks evidence");
    }

    public Freshness freshnessAt(Instant evaluationInstant) {
        present(evaluationInstant); instant(evaluationInstant);
        require(!retrievedAt.isAfter(evaluationInstant), "Retrieval after evaluation");
        if (expiresAt != null && !expiresAt.isAfter(evaluationInstant)) return Freshness.EXPIRED;
        if (observedAt != null && expiresAt != null && !observedAt.isAfter(evaluationInstant)) return Freshness.FRESH;
        return Freshness.UNKNOWN;
    }
}
