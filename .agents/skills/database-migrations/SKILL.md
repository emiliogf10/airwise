---
name: database-migrations
description: "Diseñar o revisar cambios de esquema PostgreSQL y migraciones Flyway de Airwise coordinados con JPA, cuando exista una tarea de persistencia autorizada."
---

# Preparar migraciones con evidencia

Lee el inventario tecnológico y los ADR de persistencia, si existen. La presencia de dependencias no implica base ni esquema disponibles. En fase 1 no crees ni ejecutes migraciones.

1. Identifica invariantes, consultas, datos existentes y compatibilidad entre versiones. Acuerda con Backend Agent la propiedad de entidades y repositorios antes de editar.
2. En una tarea autorizada, inspecciona la convención real de Flyway y sus migraciones aplicadas; no edites una ya aplicada ni elijas versiones duplicadas.
3. Evalúa nulos, claves, unicidad, índices, transacciones y bloqueos. Explica el efecto y la recuperación; no prometas rollback automático de cualquier DDL.
4. Ensaya la secuencia sobre una base de pruebas aislada y representativa cuando esté disponible, incluida actualización desde el estado anterior e integridad. No uses credenciales o datos de producción.
5. Para destrucción o transformaciones irreversibles, presenta impacto y recuperación concretos antes de solicitar la autorización específica pendiente.

Entrega: decisión de esquema, migración revisable o propuesta, coordinación JPA, pruebas ejecutadas y riesgos pendientes. Actualiza el ADR y backlog afectados.
