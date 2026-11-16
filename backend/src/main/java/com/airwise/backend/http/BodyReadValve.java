package com.airwise.backend.http;

import java.io.IOException;
import jakarta.servlet.ServletException;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.coyote.ActionCode;

/** Bridges the supported Tomcat request API; never unwraps a servlet facade or uses reflection. */
final class BodyReadValve extends ValveBase {
    BodyReadValve() { super(false); }

    @Override public void invoke(Request request, Response response) throws IOException, ServletException {
        Runnable abort = () -> {
            request.getCoyoteRequest().action(ActionCode.DISABLE_SWALLOW_INPUT, null);
            response.setHeader("Connection", "close");
        };
        request.setAttribute(BodyReadDeadline.ABORT_INPUT, abort);
        try {
            getNext().invoke(request, response);
        } finally {
            // Rejections must not leave Tomcat draining an unfinished attacker-controlled body
            // after the application budget and concurrency permit have already been released.
            if (!request.getCoyoteRequest().isFinished()) abort.run();
            request.removeAttribute(BodyReadDeadline.ABORT_INPUT);
        }
    }
}
