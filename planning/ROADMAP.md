# Roadmap de Airwise

Este documento ordena trabajo; no autoriza iniciarlo. El [backlog](BACKLOG.md) concreta las tareas y la Definition of Done. Las ideas no aprobadas se conservan en [mejoras](IMPROVEMENTS.md).

| Fase | Objetivo y dependencias | Entregables | Criterio de finalización |
|---|---|---|---|
| 0 — PoC previas | Validar fuentes sin coste; histórico aportado por el usuario | Evidencia resumida de Travelpayouts y SerpApi | Resultados previos identificados, límites y permisos pendientes documentados; no se vuelven a probar aquí |
| 1 — Organización | Inventariar backend existente y preparar trabajo profesional | AGENTS, seis roles, cinco skills, planificación, documentación y plantillas | Backend intacto, formatos/enlaces coherentes, evidencia local y límites de disponibilidad de agentes explícitos |
| 2 — Alcance y arquitectura | Depende de fase 1 y una nueva tarea autorizada | MVP acotado, límites modulares, conceptos de precio, contrato demo y validación técnica del backend generado | ADR revisados, dependencias reales comprobadas y plan de tests/entorno acordado; cualquier cambio de base autorizado |
| 3 — Núcleo con demo | Depende de contratos aprobados y base backend validada | Proveedor sintético identificado, normalización, búsqueda y comparación | Casos de uso y errores probados sin red; trazabilidad de origen/moneda/fecha y desconocidos respetada |
| 4 — Experiencia inicial | Depende de contrato API y decisión de frontend | Interfaz accesible, estados claros y documentación de uso real | Flujo demo verificable y responsive, pruebas pertinentes, seguridad y privacidad mínimas revisadas |
| 5 — Fuentes reales y operación | Depende de permisos/coste y demanda concreta | Adaptadores independientes, cuotas, caché autorizada y automatización progresiva | Ninguna llamada fuera del presupuesto; datos y errores no equivalentes bien diferenciados; CI solo sobre comandos validados |
| Posteriores, opcionales | Requieren priorización independiente | Usuarios, preferencias, favoritos, historial, alertas y recomendaciones más avanzadas | Cada propuesta aprobada y con permisos, coste y criterios propios; no son requisitos automáticos del MVP |

## Alcance inicial propuesto, aún por aprobar en AIR-010

Una demo de búsqueda y comparación con datos sintéticos explícitos que permita explicar itinerarios, alternativas y límites de los precios. No exige cuentas de usuario, notificaciones, reserva ni datos actuales de proveedores. Las estimaciones terrestres necesitarán una fuente o supuestos visibles aprobados; no se inventará un coste puerta a puerta.

## Puertas de decisión

- **Base técnica:** Spring Boot 4.1.2 y Java 21 son lo declarado. Resolver compatibilidad de tests, wrapper y entorno antes de adaptar el proyecto al stack imaginado.
- **Producto:** separar trayecto mostrado, viaje completo y ámbito del precio; decidir qué necesita el MVP antes de crear módulos o pantallas.
- **Proveedores:** publicar, retener o reutilizar datos exige aclarar las condiciones pendientes. Clave disponible no equivale a permiso ni presupuesto aprobado.
- **Operación:** no crear pipelines, contenedores o despliegues antes de tener componentes y comandos comprobados. Presupuesto de API de 0 € en todas las fases salvo cambio expreso del usuario.

## Estado y siguiente hito

La fase 1 se documenta y valida en [VALIDATION.md](../docs/developer-guide/VALIDATION.md). La ejecución de fases siguientes no comienza con esta entrega. Hito recomendado: AIR-010, delimitar MVP y arquitectura. AIR-011 resolverá después la validación ejecutable del backend sin cambiar su base de forma implícita.
