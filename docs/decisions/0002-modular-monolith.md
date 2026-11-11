# ADR-0002 — Monolito modular como dirección inicial

- Estado: **Propuesto**; revisar en AIR-010.
- Fecha: 2026-11-11.

## Contexto

Airwise es un portfolio con presupuesto limitado y necesidades de búsqueda, comparación y varias fuentes. No existe aún lógica que justifique servicios separados ni una estructura compleja.

## Propuesta

Mantener un backend desplegable como unidad, con límites por capacidades. Separar dominio, aplicación, infraestructura y API cuando haya responsabilidades concretas que lo requieran. Evitar microservicios, interfaces o módulos vacíos y no crear paquetes antes de aprobar casos de uso.

## Consecuencias y pendientes

Facilita evolución local y pruebas, pero exige vigilar dependencias entre módulos. Definir MVP, límites y contrato de comparación antes de implementar. No se ha aplicado esta propuesta al código; la estructura generada continúa intacta.
