package com.airwise.backend.flightsearch.domain;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public final class DomainChecks {
    private DomainChecks() {}
    public static void require(boolean valid, String message) {
        if (!valid) throw new IllegalArgumentException(message);
    }
    public static <T> T present(T value) {
        require(value != null, "Required domain value");
        return value;
    }
    public static String id(String value) {
        require(value != null && value.matches("[A-Za-z0-9][A-Za-z0-9._-]{0,127}"), "Invalid identifier");
        return value;
    }
    public static String airport(String value) {
        require(value != null && value.matches("[A-Z]{3}"), "Invalid airport code");
        return value;
    }
    public static String normalizeAirport(String value) {
        present(value);
        require(value.chars().allMatch(c -> c <= 127), "Airport code must be ASCII");
        int start = 0, end = value.length();
        while (start < end && value.charAt(start) == ' ') start++;
        while (end > start && value.charAt(end - 1) == ' ') end--;
        return airport(value.substring(start, end).toUpperCase(java.util.Locale.ROOT));
    }
    public static String text(String value, int max) {
        require(value != null && !value.isBlank() && value.codePointCount(0, value.length()) <= max
                && value.codePoints().noneMatch(Character::isISOControl), "Invalid text");
        return value;
    }
    public static <T> List<T> list(List<T> values, int min, int max) {
        present(values);
        require(values.size() >= min && values.size() <= max && values.stream().allMatch(Objects::nonNull), "Invalid collection");
        return List.copyOf(values);
    }
    public static void seconds(Long value) {
        require(value == null || (value >= 0 && value <= 604800), "Invalid duration");
    }
    public static void instant(Instant value) {
        require(value == null || value.getNano() == 0, "Instant requires whole seconds");
    }
}
