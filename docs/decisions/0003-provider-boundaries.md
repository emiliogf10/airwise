# ADR-0003 — Adaptadores con capacidades y precios explícitos

- Estado: **Propuesto**; revisar en AIR-012 tras AIR-010 y AIR-011.
- Fecha: 2026-11-11.

## Contexto

Las PoC informadas muestran datos y límites diferentes. Una respuesta de caché, una fase de búsqueda y una oferta de vendedor no representan necesariamente el mismo producto o la misma actualidad.

## Propuesta

Definir adaptadores independientes para Travelpayouts, SerpApi y demo, sin acoplar el dominio a DTOs o tokens temporales externos. Declarar capacidades y normalizar importes, moneda, contexto temporal, trayectos cubiertos, fuente y desconocidos. Mantener estimaciones separadas de precios y no suplir errores reales con demo silenciosa.

## Consecuencias y pendientes

La comparación solo podrá mezclar alternativas compatibles y explicar diferencias. Se necesitarán control de cuota, resultados parciales y revisión de permisos antes de conservar/publicar datos. No se implementan adaptadores ni se realizan consultas en esta fase. El contrato y su criterio de prueba siguen pendientes.
