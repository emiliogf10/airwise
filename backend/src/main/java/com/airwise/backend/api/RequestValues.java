package com.airwise.backend.api;

import java.util.Set;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import com.airwise.backend.http.ApiException;
import tools.jackson.databind.JsonNode;

/** Token types are checked before semantics: JSON coercion cannot change client intent. */
public final class RequestValues {
    private RequestValues() {}

    public static void object(JsonNode node, Set<String> allowed, Set<String> required, String path) {
        if (node == null || node.isNull()) throw ApiException.validation(path, "REQUIRED");
        if (!node.isObject()) throw ApiException.invalidRequest(path, "TYPE_MISMATCH");
        for (String name : node.propertyNames()) if (!allowed.contains(name)) throw ApiException.invalidRequest("/", "UNEXPECTED_FIELD");
        for (String name : required) if (!node.has(name)) throw ApiException.validation(path + "/" + name, "REQUIRED");
    }

    public static String text(JsonNode node, String path, boolean nullable) {
        if (node == null || node.isNull()) {
            if (nullable) return null;
            throw ApiException.validation(path, "REQUIRED");
        }
        if (!node.isString()) throw ApiException.invalidRequest(path, "TYPE_MISMATCH");
        return node.asString();
    }

    public static int integer(JsonNode node, String path) {
        if (node == null || node.isNull()) throw ApiException.validation(path, "REQUIRED");
        if (!node.isIntegralNumber()) throw ApiException.invalidRequest(path, "TYPE_MISMATCH");
        if (!node.canConvertToInt()) throw ApiException.validation(path, "OUT_OF_RANGE");
        return node.intValue();
    }

    public static int queryInteger(String text, String path) {
        if (!text.matches("-?(0|[1-9][0-9]*)")) throw ApiException.invalidRequest();
        try { return Integer.parseInt(text); }
        catch (NumberFormatException exception) { throw ApiException.validation(path, "OUT_OF_RANGE"); }
    }

    public static String id(String value, String path) {
        if (value != null && !value.matches("[A-Za-z0-9][A-Za-z0-9._-]{0,127}"))
            throw ApiException.validation(path, "INVALID_ID");
        return value;
    }

    public record Page(@Min(0) @Max(100000) int page, @Min(1) @Max(50) int pageSize) {
        public void validate(Validator validator) {
            if (!validator.validateProperty(this, "page").isEmpty()) throw ApiException.validation("/page", "OUT_OF_RANGE");
            if (!validator.validateProperty(this, "pageSize").isEmpty()) throw ApiException.validation("/pageSize", "OUT_OF_RANGE");
        }
        public long offset() { return (long) page * pageSize; }
    }
}
