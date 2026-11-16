package com.airwise.backend.http;

import java.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
@Profile("demo")
public class ApiExceptionHandler {
    private final ErrorWriter errors;
    public ApiExceptionHandler(ErrorWriter errors) { this.errors = errors; }
    @ExceptionHandler(ApiException.class)
    public void explicit(ApiException ex, HttpServletRequest request, HttpServletResponse response) throws IOException {
        errors.write(request, response, ex);
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public void unreadable(HttpServletRequest request, HttpServletResponse response) throws IOException {
        errors.write(request, response, 400, "INVALID_REQUEST");
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public void validation(HttpServletRequest request, HttpServletResponse response) throws IOException {
        errors.write(request, response, ApiException.validation("/", "INVALID_FORMAT"));
    }
    @ExceptionHandler(NoResourceFoundException.class)
    public void missing(HttpServletRequest request, HttpServletResponse response) throws IOException {
        errors.write(request, response, 404, "RESOURCE_NOT_FOUND");
    }
    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public void unacceptable(HttpServletRequest request, HttpServletResponse response) throws IOException {
        errors.write(request, response, 406, "NOT_ACCEPTABLE");
    }
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public void unsupported(HttpServletRequest request, HttpServletResponse response) throws IOException {
        errors.write(request, response, 415, "UNSUPPORTED_MEDIA_TYPE");
    }
    @ExceptionHandler(Exception.class)
    public void unexpected(HttpServletRequest request, HttpServletResponse response) throws IOException {
        errors.write(request, response, 500, "INTERNAL_ERROR");
    }
}
