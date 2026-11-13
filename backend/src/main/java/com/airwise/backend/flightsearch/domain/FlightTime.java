package com.airwise.backend.flightsearch.domain;

import java.time.*;
import java.util.List;
import static com.airwise.backend.flightsearch.domain.DomainChecks.*;
import static com.airwise.backend.flightsearch.domain.FlightTypes.*;

public record FlightTime(LocalDateTime localDateTime, String offset, String zoneId,
        Instant instant, UnknownReason unknownReason) {
    public FlightTime {
        DomainChecks.instant(instant);
        require(localDateTime == null || localDateTime.getNano() == 0, "Local time requires whole seconds");
        ZoneOffset parsedOffset = null;
        if (offset != null) {
            require(offset.matches("[+-][0-9]{2}:[0-9]{2}"), "Invalid offset");
            parsedOffset = ZoneOffset.of(offset);
        }
        ZoneId zone = null;
        if (zoneId != null) {
            require(ZoneId.getAvailableZoneIds().contains(zoneId), "Invalid IANA zone");
            zone = ZoneId.of(zoneId);
        }
        if (instant != null) {
            require(localDateTime != null && parsedOffset != null && unknownReason == null, "Incomplete known instant");
            require(localDateTime.toInstant(parsedOffset).equals(instant), "Inconsistent instant");
        } else {
            require(unknownReason == UnknownReason.NOT_RETURNED || unknownReason == UnknownReason.TIME_ZONE_UNKNOWN
                    || unknownReason == UnknownReason.AMBIGUOUS_LOCAL_TIME, "Missing time reason");
        }
        if (zone != null && localDateTime != null && parsedOffset != null) {
            require(zone.getRules().getValidOffsets(localDateTime).contains(parsedOffset), "Offset contradicts zone");
        }
    }

    /** Resolve only a unique zone offset, never choose arbitrarily during a DST overlap. */
    public static FlightTime fromLocal(LocalDateTime local, ZoneId zone, ZoneOffset suppliedOffset) {
        present(local); present(zone);
        List<ZoneOffset> offsets = zone.getRules().getValidOffsets(local);
        if (suppliedOffset != null) {
            require(offsets.contains(suppliedOffset), "Offset contradicts zone");
            return new FlightTime(local, format(suppliedOffset), zone.getId(), local.toInstant(suppliedOffset), null);
        }
        require(!offsets.isEmpty(), "Local time does not exist in zone");
        if (offsets.size() != 1) {
            return new FlightTime(local, null, zone.getId(), null, UnknownReason.AMBIGUOUS_LOCAL_TIME);
        }
        ZoneOffset unique = offsets.getFirst();
        return new FlightTime(local, format(unique), zone.getId(), local.toInstant(unique), null);
    }

    private static String format(ZoneOffset offset) {
        return offset.getTotalSeconds() == 0 ? "+00:00" : offset.getId();
    }
}
