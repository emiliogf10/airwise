package com.airwise.backend.http;

import java.util.List;
import java.util.UUID;

public record ApiError(String contractVersion, UUID correlationId, String code, String message,
        List<FieldError> fieldErrors, boolean fieldErrorsTruncated, List<ProviderSummary> providerResults) { }
