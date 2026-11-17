package com.airwise.backend.http;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

final class RoutePolicy {
    static final String ADMITTED = RoutePolicy.class.getName() + ".admitted";
    static final String ORIGINAL_URI = RoutePolicy.class.getName() + ".originalUri";
    static final RequestMatcher AIRPORTS = PathPatternRequestMatcher.withDefaults()
            .matcher(HttpMethod.GET, "/api/v1/demo/airports");
    static final RequestMatcher SEARCH = PathPatternRequestMatcher.withDefaults()
            .matcher(HttpMethod.POST, "/api/v1/demo/flight-searches");
    static final RequestMatcher COMPARISONS = PathPatternRequestMatcher.withDefaults()
            .matcher(HttpMethod.POST, "/api/v1/demo/comparisons");
    static final RequestMatcher PUBLIC_POSTS = request -> request.getDispatcherType() == DispatcherType.REQUEST
            && (SEARCH.matches(request) || COMPARISONS.matches(request));
    static final RequestMatcher PUBLIC = request -> request.getDispatcherType() == DispatcherType.REQUEST
            && (AIRPORTS.matches(request) || PUBLIC_POSTS.matches(request));
    static final RequestMatcher ERROR = RoutePolicy::internalError;
    private RoutePolicy() { }
    static boolean internalError(HttpServletRequest request) {
        return request.getDispatcherType() == DispatcherType.ERROR && "/error".equals(path(request))
                && request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI) instanceof String original
                && original.equals(request.getAttribute(ORIGINAL_URI))
                && Boolean.TRUE.equals(request.getAttribute(ADMITTED))
                && request.getAttribute(RequestContext.ATTRIBUTE) instanceof RequestContext;
    }
    static String path(HttpServletRequest request) {
        return request.getRequestURI().substring(request.getContextPath().length());
    }
    static String methodForPath(String path) {
        return switch (path) {
            case "/api/v1/demo/airports" -> "GET";
            case "/api/v1/demo/flight-searches" -> "POST";
            case "/api/v1/demo/comparisons" -> "POST";
            default -> null;
        };
    }
}
