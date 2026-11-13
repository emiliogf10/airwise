package com.airwise.backend.flightsearch.application;

import java.time.Instant;
import java.util.UUID;
import static com.airwise.backend.flightsearch.domain.DomainChecks.*;

public record ProviderExecutionContext(UUID correlationId, Instant deadlineAt) {
    public ProviderExecutionContext { present(correlationId); present(deadlineAt); instant(deadlineAt); }
}
