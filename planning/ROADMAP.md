# Roadmap de Airwise

Este documento ordena trabajo; no autoriza iniciarlo. El [backlog](BACKLOG.md) concreta las tareas y la Definition of Done. Las ideas no aprobadas se conservan en [mejoras](IMPROVEMENTS.md).

| Fase | Objetivo y dependencias | Entregables | Criterio de finalización |
|---|---|---|---|
| 0 — PoC previas | Validar fuentes sin coste; histórico aportado por el usuario | Evidencia resumida de Travelpayouts y SerpApi | Resultados previos identificados, límites y permisos pendientes documentados; no se vuelven a probar aquí |
| 1 — Organización | Inventariar backend existente y preparar trabajo profesional | AGENTS, seis roles, cinco skills, planificación, documentación y plantillas | Backend intacto, formatos/enlaces coherentes, evidencia local y límites de disponibilidad de agentes explícitos |
| 2 — Alcance y arquitectura | AIR-010/011/012 verificadas y seguridad/testing AIR-016 diseñados; implementación HTTP requiere petición propia | Diseño MVP demo, cuatro módulos, dominio, HTTP conceptual y riesgos; después, validación técnica y contratos definitivos | Diseño revisado y verificable sin código; no confundirlo con dependencias resueltas, arranque o tests de producto |
| 3 — Núcleo con demo | Depende de contratos aprobados y base backend validada | Proveedor sintético identificado, normalización, búsqueda y comparación | Casos de uso y errores probados sin red; trazabilidad de origen/moneda/fecha y desconocidos respetada |
| 4 — Experiencia inicial | Depende de contrato API y decisión de frontend | Interfaz accesible, estados claros y documentación de uso real | Flujo demo verificable y responsive, pruebas pertinentes, seguridad y privacidad mínimas revisadas |
| 5 — Fuentes reales y operación | Depende de permisos/coste y demanda concreta | Adaptadores independientes, cuotas, caché autorizada y automatización progresiva | Ninguna llamada fuera del presupuesto; datos y errores no equivalentes bien diferenciados; CI solo sobre comandos validados |
| Posteriores, opcionales | Requieren priorización independiente | Usuarios, preferencias, favoritos, historial, alertas y recomendaciones más avanzadas | Cada propuesta aprobada y con permisos, coste y criterios propios; no son requisitos automáticos del MVP |

## Alcance acordado en AIR-010, implementación parcial en AIR-013/014

Demo determinista sin Internet/credenciales/BD de negocio: ida e ida/vuelta, un adulto/turista, 1–3 aeropuertos de origen, un destino, segmentos/conexiones, filtros, orden, paginación y comparación explicable de 2–4 ofertas. Preferencias transitorias y estimaciones terrestres manuales opcionales, sin total exacto cuando falten componentes. Cuentas, favoritos, historial, alertas, compra/pagos y proveedores reales se aplazan. Requisitos y UX en [OVERVIEW.md](../docs/architecture/OVERVIEW.md); persistencia y módulos en [MODULES.md](../docs/architecture/MODULES.md).

## Puertas de decisión

- **Base técnica:** Spring Boot 4.1.2 y Java 21 son lo declarado. Resolver compatibilidad de tests, wrapper y entorno antes de adaptar el proyecto al stack imaginado.
- **Producto:** alcance y cuatro límites aceptados como diseño; conservar diferencias de trayecto/viaje/ámbito, moneda, evidencia y desconocido al implementar. No crear paquetes o pantallas hasta una tarea autorizada.
- **Proveedores:** publicar, retener o reutilizar datos exige aclarar las condiciones pendientes. Clave disponible no equivale a permiso ni presupuesto aprobado.
- **Operación:** no crear pipelines, contenedores o despliegues antes de tener componentes y comandos comprobados. Presupuesto de API de 0 € en todas las fases salvo cambio expreso del usuario.

## Estado y siguiente hito

La fase 1, el diseño AIR-010 y el contexto demo comprobado en AIR-011 constan en [VALIDATION.md](../docs/developer-guide/VALIDATION.md). AIR-012 fija contratos serializados; AIR-013 incorpora el proveedor demo y sus fixtures, con compilación y 62 tests offline satisfactorios. Las implementaciones posteriores conservan sus tareas y autorizaciones propias.

AIR-016 fija la política objetivo. AIR-014 completó catálogo, búsqueda REST, perfil y seguridad demo. AIR-015 incorpora comparación, estimaciones explícitas y endpoint sobre el conjunto demo reconstruido:238 pruebas satisfactorias y24 comprobaciones HTTP locales el17/11/2026. AIR-017 y AIR-018 no se han iniciado y requieren petición propia; el siguiente paso funcional recomendado es preparar la experiencia frontend AIR-018, sin comenzar automáticamente. Fuentes reales necesitan además AIR-019 y autorización específica.
