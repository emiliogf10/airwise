package com.airwise.backend.airport.domain;

import java.time.ZoneId;

public record Airport(String code, String name, String city, String countryCode, String timeZone) {
    public Airport {
        if (code == null || !code.matches("[A-Z]{3}")) throw new IllegalArgumentException("Invalid airport code");
        for (String text : new String[]{name, city}) {
            if (text != null && (text.isBlank() || text.codePointCount(0, text.length()) > 120
                    || text.codePoints().anyMatch(Character::isISOControl))) {
                throw new IllegalArgumentException("Invalid airport text");
            }
        }
        if (countryCode != null && !countryCode.matches("[A-Z]{2}")) throw new IllegalArgumentException("Invalid country code");
        if (timeZone != null) {
            if (!ZoneId.getAvailableZoneIds().contains(timeZone)) throw new IllegalArgumentException("Invalid IANA zone");
        }
    }
}
