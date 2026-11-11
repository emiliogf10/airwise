# Política inicial de contratos y proveedores

No hay endpoints de negocio ni especificación OpenAPI implementados en Airwise. Spring Web MVC está declarado, pero no se inventan rutas como si ya estuvieran disponibles. OpenAPI y Javadoc útil se incorporarán cuando existan contratos y código autorizados.

## Categorías de información

| Categoría | Qué representa | Obligación del modelo/UI |
|---|---|---|
| Precio observado | Registro previo de una búsqueda o caché | Identificar fuente y tiempos conocidos; disponibilidad pendiente |
| Resultado reciente de búsqueda | Respuesta obtenida en un contexto de viaje, con caché posible | Conservar parámetros, fase y ámbito; no prometer inventario actual |
| Estimación | Cálculo con fuente o supuestos, p. ej. transporte terrestre | Mostrar esos supuestos y no mezclarlo con precio confirmado |
| Precio confirmado por vendedor | Confirmación concreta de un vendedor bajo condiciones identificadas | Exigir esa evidencia; una opción de reserva por sí sola no basta |

Cada adaptador deberá declarar capacidades y límites. Separar DTO externo y modelo normalizado; importe `BigDecimal`, moneda, trayectos cubiertos, datos desconocidos y procedencia explícitos. El demo debe ser sintético y visible.

## Restricciones vigentes

- **Fase 1: ninguna llamada a Travelpayouts o SerpApi**, incluidos endpoints de cuenta, continuación y reserva. No se consumen créditos ni se ejecutan PoC.
- Presupuesto económico 0 €; no planes de pago, datos bancarios, renovaciones o créditos adicionales.
- Variables previstas: `TRAVELPAYOUTS_API_TOKEN` y `SERPAPI_API_KEY`; sus valores nunca deben ir al repositorio, frontend, informes o logs.
- Integración posterior sujeta a documentación vigente, autorización, cuota y condiciones concretas de publicación/retención/reutilización. No se contactará con proveedores sin petición autorizada.
- No seguir tokens ni enlaces automáticamente, no tratar identificadores temporales como permanentes y no ejecutar mecanismos de compra por inferencia del contexto.
- No asumir equipaje incluido, peso permitido, condiciones de cambios/cancelación ni costes adicionales exactos cuando falten datos fiables por vendedor.

Los tests futuros de adaptadores serán sintéticos y locales. La evidencia histórica está en [PROVIDER_EVIDENCE.md](../architecture/PROVIDER_EVIDENCE.md); el contrato de dominio y errores pertenece a AIR-012 y la revisión de permisos a AIR-019.
