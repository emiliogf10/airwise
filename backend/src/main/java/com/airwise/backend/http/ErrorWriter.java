package com.airwise.backend.http;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.json.JsonMapper;

/** Shared protocol writer for servlet filters, Security, MVC and the internal error dispatch. */
public final class ErrorWriter {
    private static final Set<String> FIELD_CODES = Set.of("REQUIRED", "INVALID_FORMAT", "OUT_OF_RANGE",
            "UNKNOWN_AIRPORT", "DUPLICATE", "INVALID_COMBINATION", "UNSUPPORTED_VALUE", "UNKNOWN_OFFER_ID",
            "INVALID_AMOUNT", "INVALID_CURRENCY", "INVALID_ID", "UNEXPECTED_FIELD", "TYPE_MISMATCH");
    private static final Pattern POINTER = Pattern.compile("/(?:query|datasetVersion|filters|sort|page|pageSize|q)"
            + "(?:/(?:tripType|origins|destination|outboundDate|returnDate|travellers|adults|cabin|currency|"
            + "maxConnections|maxDurationMinutes|departureTimeFrom|departureTimeTo|airlineCodes|includeUnknown|"
            + "minPrice|maxPrice|[0-9]|1[0-9])){0,5}");
    private final JsonMapper mapper;
    public ErrorWriter(JsonMapper mapper) { this.mapper = mapper; }

    public void write(HttpServletRequest request, HttpServletResponse response, int status, String code) throws IOException {
        write(request, response, ApiException.failure(status, code));
    }
    public void write(HttpServletRequest request, HttpServletResponse response, ApiException failure) throws IOException {
        if (response.isCommitted()) return;
        response.resetBuffer();
        ResponseHeaders.apply(request, response);
        response.setStatus(failure.status());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        List<FieldError> fields = failure.fieldErrors().stream()
                .map(f -> new FieldError(safePath(f.path()), FIELD_CODES.contains(f.code()) ? f.code() : "INVALID_FORMAT"))
                .distinct().sorted(Comparator.comparing(FieldError::path).thenComparing(FieldError::code)).toList();
        ApiError error = new ApiError("airwise-v1", RequestContext.current(request).correlationId(), failure.code(),
                message(failure.code()), fields.stream().limit(20).toList(), fields.size() > 20,
                failure.providerResults());
        mapper.writeValue(response.getOutputStream(), error);
    }
    private static String safePath(String path) {
        return path != null && POINTER.matcher(path).matches() ? path : "/";
    }
    private static String message(String code) {
        return switch (code) {
            case "INVALID_REQUEST" -> "Solicitud JSON o parámetros no válidos.";
            case "ACCESS_DENIED" -> "Solicitud no permitida.";
            case "RESOURCE_NOT_FOUND" -> "Recurso no disponible.";
            case "NOT_ACCEPTABLE" -> "La respuesta requiere aceptar application/json.";
            case "DATASET_VERSION_UNAVAILABLE" -> "Versión del dataset no disponible.";
            case "PAYLOAD_TOO_LARGE" -> "El cuerpo supera el límite permitido.";
            case "UNSUPPORTED_MEDIA_TYPE" -> "Se requiere JSON UTF-8 sin compresión.";
            case "VALIDATION_FAILED" -> "La solicitud no cumple las reglas de validación.";
            case "CAPABILITY_UNAVAILABLE" -> "Consulta no admitida por el dataset demo.";
            case "RATE_LIMITED" -> "Límite temporal de solicitudes alcanzado.";
            case "PROVIDERS_FAILED" -> "No se pudo completar ninguna unidad de búsqueda.";
            default -> "Error interno al procesar la solicitud.";
        };
    }
}
