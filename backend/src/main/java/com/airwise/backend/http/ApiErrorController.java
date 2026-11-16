package com.airwise.backend.http;

import java.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/** Only the conjunction enforced by RoutePolicy.ERROR may reach this sanitized fallback. */
@Controller
@Profile("demo")
public class ApiErrorController implements ErrorController {
    private final ErrorWriter errors;
    public ApiErrorController(ErrorWriter errors) { this.errors = errors; }
    @RequestMapping("/error")
    public void error(HttpServletRequest request, HttpServletResponse response) throws IOException {
        errors.write(request, response, RoutePolicy.internalError(request) ? 500 : 403,
                RoutePolicy.internalError(request) ? "INTERNAL_ERROR" : "ACCESS_DENIED");
    }
}
