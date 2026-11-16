package com.airwise.backend.http;

import java.util.List;

/** Explicit boundary failures only. Unexpected domain/programming exceptions remain HTTP 500. */
public final class ApiException extends RuntimeException {
    private final int status;
    private final String code;
    private final List<FieldError> fieldErrors;
    private final List<ProviderSummary> providerResults;

    private ApiException(int status, String code, List<FieldError> fields, List<ProviderSummary> providers) {
        super(code);
        this.status = status; this.code = code;
        this.fieldErrors = List.copyOf(fields); this.providerResults = List.copyOf(providers);
    }
    public int status() { return status; }
    public String code() { return code; }
    public List<FieldError> fieldErrors() { return fieldErrors; }
    public List<ProviderSummary> providerResults() { return providerResults; }
    public static ApiException invalidRequest() { return failure(400, "INVALID_REQUEST"); }
    public static ApiException invalidRequest(String path, String code) {
        return new ApiException(400, "INVALID_REQUEST", List.of(new FieldError(path, code)), List.of());
    }
    public static ApiException validation(String path, String code) {
        return validation(List.of(new FieldError(path, code)));
    }
    public static ApiException validation(List<FieldError> fields) {
        return new ApiException(422, "VALIDATION_FAILED", fields, List.of());
    }
    public static ApiException datasetUnavailable() { return failure(409, "DATASET_VERSION_UNAVAILABLE"); }
    public static ApiException capabilityUnavailable(List<ProviderSummary> providers) {
        return new ApiException(422, "CAPABILITY_UNAVAILABLE", List.of(), providers);
    }
    public static ApiException providersFailed(List<ProviderSummary> providers) {
        return new ApiException(503, "PROVIDERS_FAILED", List.of(), providers);
    }
    static ApiException failure(int status, String code) {
        return new ApiException(status, code, List.of(), List.of());
    }
}
