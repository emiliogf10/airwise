package com.airwise.backend.http;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

final class ResponseHeaders {
    private ResponseHeaders() { }
    static void apply(HttpServletRequest request, HttpServletResponse response) {
        response.setHeader("X-Correlation-ID", RequestContext.current(request).correlationId().toString());
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("Referrer-Policy", "no-referrer");
        response.setHeader("Content-Security-Policy", "default-src 'none'; frame-ancestors 'none'; base-uri 'none'");
        response.setHeader("Permissions-Policy", "geolocation=(), camera=(), microphone=()");
        DemoCors.restore(request, response);
        if (request.isSecure()) response.setHeader("Strict-Transport-Security", "max-age=31536000");
    }
}
