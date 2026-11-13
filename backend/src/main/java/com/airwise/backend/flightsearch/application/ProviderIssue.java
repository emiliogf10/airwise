package com.airwise.backend.flightsearch.application;

import static com.airwise.backend.flightsearch.domain.DomainChecks.*;
import static com.airwise.backend.flightsearch.domain.FlightTypes.*;

public record ProviderIssue(ProviderIssueCode code, String message) {
    public ProviderIssue { present(code); text(message, 240); }

    public static ProviderIssue of(ProviderIssueCode code) {
        return new ProviderIssue(code, switch (code) {
            case PROVIDER_DISABLED -> "Fuente deshabilitada.";
            case PROVIDER_OFFLINE -> "Incidencia simulada de fuente desconectada.";
            case PROVIDER_TIMEOUT -> "Plazo de búsqueda agotado.";
            case PROVIDER_QUOTA_EXHAUSTED -> "Cuota de fuente agotada.";
            case CAPABILITY_UNSUPPORTED -> "Consulta no admitida por el dataset demo.";
            case CAPABILITY_UNVERIFIED -> "Capacidad no verificada.";
            case INVALID_PROVIDER_DATA -> "Datos de fuente inválidos.";
            case PROVIDER_FAILURE -> "Fallo de fuente.";
        });
    }
}
