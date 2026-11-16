package com.airwise.backend.flightsearch.application;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import com.airwise.backend.airport.domain.Airport;

/** Immutable server-owned dataset; paging never changes the selected version. */
public record DemoCatalog(DatasetDescriptor dataset, List<Airport> airports) {
    public DemoCatalog {
        airports = airports.stream().sorted(java.util.Comparator.comparing(Airport::code)).toList();
    }

    public DatasetDescriptor resolve(String version) {
        if (version != null && !dataset.datasetVersion().equals(version)) throw new VersionUnavailable();
        return dataset;
    }

    public List<Airport> matching(String text) {
        String needle = normalize(text);
        return airports.stream().filter(a -> normalize(a.code()).contains(needle)
                || normalize(a.name()).contains(needle) || normalize(a.city()).contains(needle)).toList();
    }

    private static String normalize(String text) {
        return text == null ? "" : Normalizer.normalize(text.trim(), Normalizer.Form.NFKD)
                .replaceAll("\\p{M}", "").toUpperCase(Locale.ROOT);
    }

    public static final class VersionUnavailable extends RuntimeException {}
}
