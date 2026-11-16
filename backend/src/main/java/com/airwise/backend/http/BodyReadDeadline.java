package com.airwise.backend.http;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * A synchronous body-read scope, shared with the demo NIO channel on this same request thread.
 * No timer or background reader can outlive the scope or access a recycled servlet request.
 */
final class BodyReadDeadline implements AutoCloseable {
    static final String ABORT_INPUT = BodyReadDeadline.class.getName() + ".abortInput";
    private static final ThreadLocal<BodyReadDeadline> CURRENT = new ThreadLocal<>();
    private static final long BUDGET_NANOS = 5_000_000_000L;
    private final long started = System.nanoTime();
    private final Runnable abortInput;

    private BodyReadDeadline(Runnable abortInput) { this.abortInput = abortInput; }

    static BodyReadDeadline open(HttpServletRequest request, HttpServletResponse response) {
        if (CURRENT.get() != null) throw new IllegalStateException("Nested body read");
        Runnable abort = request.getAttribute(ABORT_INPUT) instanceof Runnable action ? action
                : () -> response.setHeader("Connection", "close");
        var deadline = new BodyReadDeadline(abort);
        CURRENT.set(deadline);
        return deadline;
    }

    static BodyReadDeadline current() { return CURRENT.get(); }

    long remainingMillis() {
        long remaining = BUDGET_NANOS - (System.nanoTime() - started);
        if (remaining <= 0) {
            abortInput.run();
            // A controlled runtime failure bypasses Tomcat's IOException -> sendError/ERROR handling.
            // The RepresentationFilter writes the ordinary sanitized 400 response on this same thread.
            throw ApiException.invalidRequest();
        }
        return (remaining + 999_999L) / 1_000_000L;
    }

    @Override public void close() { CURRENT.remove(); }
}
