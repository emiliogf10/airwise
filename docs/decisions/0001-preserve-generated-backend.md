# ADR-0001 — Preservar el backend generado

- Estado: **Aceptado**.
- Fecha: 2026-11-11.
- Origen: instrucciones explícitas del usuario para la fase 1.

## Contexto

El workspace ya contiene `backend/`, generado mediante Initializr, con Java 21 y Spring Boot 4.1.2 declarados. Regenerarlo o ajustar sus dependencias al stack planeado destruiría el punto de partida que se pide conservar.

## Decisión

No modificar ningún archivo del backend durante la preparación organizativa. Crear configuración de agentes, skills, planificación y documentación desde la raíz. No ejecutar Initializr, construir frontend/BD, integrar proveedores ni comenzar negocio. Cambiar la base requerirá justificación técnica y autorización previa.

## Consecuencias y verificación

El inventario distingue declarado de resuelto y previsto. La compilación/arranque del backend se mantiene pendiente, sin corregir supuestos problemas. Se compara el conjunto y hash de sus archivos antes/después de la entrega; evidencia en [VALIDATION.md](../developer-guide/VALIDATION.md).
