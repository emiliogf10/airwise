# ADR-0002 — Monolito modular como dirección inicial

- Estado: **Aceptado como diseño en AIR-010**.
- Fecha: 2026-11-11.
- Historial: propuesto en fase 1; revisado y aceptado en AIR-010 por el alcance de diseño autorizado. No prueba implementación.

## Contexto

Airwise es un portfolio con presupuesto limitado y necesidades de búsqueda, comparación y varias fuentes. No existe lógica que justifique servicios separados ni una estructura compleja. El backend generado Java 21/Spring Boot 4.1.2 se conserva.

## Decisión

Mantener un backend desplegable como unidad, con cuatro límites funcionales: Airport, Flight Search, Flight Provider y Flight Comparison. Search declara modelo normalizado/puerto; los adaptadores dependen de ese contrato y se conectan desde composición raíz. Comparison depende de API pública de Search, nunca de adaptadores concretos.

Separar dominio, aplicación, infraestructura y API donde existan responsabilidades reales. No crear módulos Maven, microservicios, librería Shared/Common, módulo Security de negocio o capas vacías. User y Favorites se aplazan con ADR-0004.

## Consecuencias y pendientes

Facilita evolución local y pruebas, pero exige vigilar dependencias acíclicas y no acceder a internos de otro módulo. [MODULES.md](../architecture/MODULES.md) define responsabilidades, interfaces, dependencias y persistencia; [OVERVIEW.md](../architecture/OVERVIEW.md) fija MVP.

AIR-011 verificó el contexto demo y AIR-012 ratifica contratos, incluida la propuesta de paquetes y una frontera HTTP transversal que depende de APIs públicas. Los cuatro límites aceptados se mantienen. Seguridad efectiva corresponde a AIR-016 y frontend a AIR-018. El código de los módulos se desarrollará con autorización propia.
