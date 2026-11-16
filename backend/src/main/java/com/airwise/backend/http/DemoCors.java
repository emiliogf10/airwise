package com.airwise.backend.http;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.filter.CorsFilter;

final class DemoCors {
    private static final String ALLOWED_ORIGIN = DemoCors.class.getName() + ".origin";
    private DemoCors() { }
    static CorsFilter filter(String configured, ErrorWriter errors) {
        Set<String> origins = parseOrigins(configured);
        CorsFilter filter = new CorsFilter(request -> null);
        filter.setCorsProcessor((configuration, request, response) -> process(origins, errors, request, response));
        return filter;
    }
    static Set<String> parseOrigins(String configured) {
        Set<String> result = new LinkedHashSet<>();
        if (configured.isBlank()) return result;
        for (String value : configured.split(",", -1)) {
            value = value.trim();
            URI uri;
            try { uri = URI.create(value); }
            catch (IllegalArgumentException e) { throw new IllegalStateException("Invalid demo CORS origin"); }
            if (!("http".equals(uri.getScheme()) || "https".equals(uri.getScheme())) || uri.getHost() == null
                    || uri.getRawUserInfo() != null || uri.getRawQuery() != null || uri.getRawFragment() != null
                    || uri.getPort() > 65535 || (uri.getRawPath() != null && !uri.getRawPath().isEmpty()) || value.contains("*")) {
                throw new IllegalStateException("Invalid demo CORS origin");
            }
            result.add(value);
        }
        return Set.copyOf(result);
    }
    private static boolean process(Set<String> origins, ErrorWriter errors, HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        vary(response);
        if (request.getHeader("Origin") == null) return true;
        String method = RoutePolicy.methodForPath(RoutePolicy.path(request));
        boolean preflight = CorsUtils.isPreFlightRequest(request);
        if (!allowed(origins, request)) { errors.write(request, response, 403, "ACCESS_DENIED"); return false; }
        request.setAttribute(ALLOWED_ORIGIN, request.getHeader("Origin"));
        restore(request, response);
        if (preflight) {
            response.setHeader("Access-Control-Allow-Methods", method);
            if (request.getHeader("Access-Control-Request-Headers") != null)
                response.setHeader("Access-Control-Allow-Headers", "Content-Type");
            response.setHeader("Access-Control-Max-Age", "600");
            response.setStatus(200);
        }
        return true;
    }
    /** Headers for an early quota/firewall failure; this never admits a request or runs business. */
    static void prepare(Set<String> origins, HttpServletRequest request, HttpServletResponse response) {
        vary(response);
        if (allowed(origins, request)) {
            request.setAttribute(ALLOWED_ORIGIN, request.getHeader("Origin"));
            restore(request, response);
        }
    }
    private static boolean allowed(Set<String> origins, HttpServletRequest request) {
        String method = RoutePolicy.methodForPath(RoutePolicy.path(request));
        boolean preflight = CorsUtils.isPreFlightRequest(request);
        if (java.util.Collections.list(request.getHeaders("Origin")).size() != 1
                || !origins.contains(request.getHeader("Origin")) || method == null
                || !method.equals(preflight ? request.getHeader("Access-Control-Request-Method") : request.getMethod())) return false;
        if (!preflight) return true;
        return java.util.Collections.list(request.getHeaders("Access-Control-Request-Method")).size() == 1
                && java.util.Collections.list(request.getHeaders("Access-Control-Request-Headers")).stream()
                        .flatMap(headers -> Arrays.stream(headers.split(",", -1)))
                        .allMatch(h -> h.trim().equalsIgnoreCase("Content-Type"))
                && request.getHeader("Access-Control-Request-Private-Network") == null;
    }
    private static void vary(HttpServletResponse response) {
        for (String header : new String[]{"Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"})
            if (!response.getHeaders("Vary").contains(header)) response.addHeader("Vary", header);
    }
    static void restore(HttpServletRequest request, HttpServletResponse response) {
        Object origin = request.getAttribute(ALLOWED_ORIGIN);
        if (!(origin instanceof String value)) return;
        response.setHeader("Access-Control-Allow-Origin", value);
        response.setHeader("Access-Control-Expose-Headers", "X-Correlation-ID, Retry-After");
    }
}
