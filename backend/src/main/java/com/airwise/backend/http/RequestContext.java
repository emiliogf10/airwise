package com.airwise.backend.http;

import java.time.Instant;
import java.util.UUID;
import jakarta.servlet.http.HttpServletRequest;

/** Trusted server state, installed before Security and retained across internal ERROR dispatch. */
public record RequestContext(UUID correlationId, Instant deadlineAt) {
    static final String ATTRIBUTE = RequestContext.class.getName();
    public static RequestContext current(HttpServletRequest request) {
        Object value = request.getAttribute(ATTRIBUTE);
        if (!(value instanceof RequestContext context)) {
            throw new IllegalStateException("Missing server request context");
        }
        return context;
    }
}
