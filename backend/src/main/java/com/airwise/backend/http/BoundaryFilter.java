package com.airwise.backend.http;

import java.io.IOException;
import java.time.Clock;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Runs outside Security so firewall and CORS failures receive the same context, budget and headers. */
final class BoundaryFilter implements Filter {
    private final Clock clock;
    private final AdmissionLimiter limiter;
    private final ErrorWriter errors;
    private final java.util.Set<String> origins;
    BoundaryFilter(Clock clock, AdmissionLimiter limiter, ErrorWriter errors, String configuredOrigins) {
        this.clock = clock; this.limiter = limiter; this.errors = errors;
        this.origins = DemoCors.parseOrigins(configuredOrigins);
    }
    @Override public void doFilter(ServletRequest incoming, ServletResponse outgoing, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) incoming;
        HttpServletResponse response = (HttpServletResponse) outgoing;
        boolean external = request.getDispatcherType() == DispatcherType.REQUEST;
        if (external || !(request.getAttribute(RequestContext.ATTRIBUTE) instanceof RequestContext)) {
            request.setAttribute(RequestContext.ATTRIBUTE, new RequestContext(UUID.randomUUID(),
                    clock.instant().truncatedTo(ChronoUnit.SECONDS).plusSeconds(2)));
        }
        ResponseHeaders.apply(request, response);
        if (external) DemoCors.prepare(origins, request, response);
        AdmissionLimiter.Permit permit = null;
        if (external) {
            AdmissionLimiter.Result result = limiter.acquire(request.getRemoteAddr());
            permit = result.permit();
            if (permit == null) {
                response.setHeader("Retry-After", Long.toString(result.retryAfterSeconds()));
                errors.write(request, response, 429, "RATE_LIMITED");
                return;
            }
        }
        try {
            chain.doFilter(request, response);
        } catch (ApiException e) {
            errors.write(request, response, e);
        } catch (RuntimeException | ServletException e) {
            // Do not log attacker input, exception messages, query strings or remote addresses.
            errors.write(request, response, 500, "INTERNAL_ERROR");
        } finally {
            if (permit != null) permit.close();
        }
    }
}
