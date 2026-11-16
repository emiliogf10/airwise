package com.airwise.backend.http;

/** A schema-owned pointer and a public validation code; never a rejected input value. */
public record FieldError(String path, String code) { }
