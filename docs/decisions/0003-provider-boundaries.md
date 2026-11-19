# ADR-0003 — Adaptadores con capacidades y precios explícitos

- Estado: **Aceptado en AIR-010 y contratos ratificados en AIR-012**.
- Fecha: 2026-11-11.
- Historial: propuesto en fase 1, principios aceptados en AIR-010 y campos/puertos/DTO/identidad ratificados en AIR-012 el 12/11/2026 tras AIR-011. AIR-013 materializará las fixtures y el adaptador demo.

## Contexto

Las PoC informadas muestran datos y límites diferentes. Caché, fase de búsqueda, anuncio de vendedor y reserva confirmada no representan el mismo producto o actualidad. La evidencia histórica no autoriza retención, publicación o llamadas nuevas.

## Decisión

Adaptadores independientes para demo, Travelpayouts y SerpApi, con capacidades declaradas y dominio ajeno a DTO/tokens externos. Search posee el puerto; Provider lo implementa. Solo demo participa en el MVP inicial.

Normalizar moneda/importe BigDecimal, cobertura de trayectos/personas, condiciones conocidas, tiempos y procedencia. Separar OBSERVED_CACHE, RECENT_SEARCH, SELLER_ADVERTISED, BOOKING_CONFIRMED, ESTIMATE y UNKNOWN. DEMO es procedencia sintética separada y no produce confirmación de reserva real.

Una observación parcial no es una oferta con itinerario completo. No inventar regreso/equipaje, sumar fragmentos de fuentes distintas o convertir errores en vacío/demo. Monedas/evidencias/ámbitos incompatibles se muestran separados.

## Consecuencias y pendientes

El puerto admite ofertas y observaciones diferenciadas, capacidades no soportadas y errores parciales sanitizados. Es necesario planificar flujos y presupuesto, no solo una llamada nominal; [PROVIDERS.md](../architecture/PROVIDERS.md) detalla el diseño.

El contrato normativo es airwise-v1, con consulta canónica e identidad demo SHA-256, dinero BigDecimal/ISO 4217, campos desconocidos explícitos y estados COMPLETE/EMPTY/PARTIAL/FAILED/UNSUPPORTED. [NORMALIZED_CONTRACTS.md](../architecture/NORMALIZED_CONTRACTS.md) es la fuente de campos e invariantes. API_DESIGN referencia esos tipos y documenta los DTO HTTP. La forma serializada deja de estar pendiente. Los contratos y permisos concretos de fuentes reales se verificarán antes de integrarlas.

Estado posterior: AIR-013 implementó el adaptador demo y AIR-014/015 su uso HTTP; no hay adaptadores reales ni red de proveedores ejecutada en estas tareas. Fuentes reales deshabilitadas y gasto máximo 0 €. AIR-019 documenta permisos, retención y cuota publicada sin comprobar cuenta; [ADR-0007](0007-real-provider-admission.md) conserva las dudas y condiciones previas a cualquier integración autorizada. No asumir condiciones actuales desde la cuota histórica.
