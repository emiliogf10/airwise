# Decisiones arquitectónicas

Un ADR registra contexto, decisión, estado, consecuencias y pendientes. **Aceptado** significa acordado dentro del alcance actual; no prueba implementación de producto. **Propuesto** necesita validación antes de considerarse decisión final. Los cambios posteriores deben preservar trazabilidad con un ADR que lo sustituya cuando corresponda.

| ADR | Estado | Contenido |
|---|---|---|
| [0001](0001-preserve-generated-backend.md) | Aceptado | Preservar la base generada y separar organización de implementación |
| [0002](0002-modular-monolith.md) | Aceptado en AIR-010 | Cuatro límites funcionales y responsabilidades sin capas vacías |
| [0003](0003-provider-boundaries.md) | Principios AIR-010 y contratos AIR-012 aceptados | Capacidades/precios explícitos, normalización, identidad y DTO ratificados |
| [0004](0004-demo-mvp-and-persistence.md) | Aceptado en AIR-010 | MVP demo, preferencias transitorias y ausencia de cuentas/BD inicial |

AIR-010 acepta alcance, UX y persistencia aplazada. AIR-011 verifica el contexto demo de test y AIR-012 ratifica los contratos serializados. Implementación de negocio, esquema eventual PostgreSQL, frontend, seguridad efectiva y despliegue necesitan sus tareas propias. ADR-0001 conserva el historial de la base generada.
