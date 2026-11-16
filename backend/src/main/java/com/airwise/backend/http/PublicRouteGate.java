package com.airwise.backend.http;

import java.io.IOException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Rejects unauthorized unsafe requests before CSRF can lazily create a session. */
final class PublicRouteGate implements Filter {
    private final ErrorWriter errors;
    PublicRouteGate(ErrorWriter errors) { this.errors = errors; }
    @Override public void doFilter(ServletRequest incoming, ServletResponse outgoing, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) incoming;
        HttpServletResponse response = (HttpServletResponse) outgoing;
        if (RoutePolicy.PUBLIC.matches(request)) {
            request.setAttribute(RoutePolicy.ADMITTED, true);
            request.setAttribute(RoutePolicy.ORIGINAL_URI, request.getRequestURI());
            chain.doFilter(request, response);
        } else if (RoutePolicy.ERROR.matches(request)) {
            // A POST error must never reach CSRF/token repositories or re-enter MVC business.
            errors.write(request, response, 500, "INTERNAL_ERROR");
        } else errors.write(request, response, 403, "ACCESS_DENIED");
    }
}
