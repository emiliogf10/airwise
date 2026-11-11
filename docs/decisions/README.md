# Decisiones arquitectónicas

Un ADR registra contexto, decisión, estado, consecuencias y pendientes. **Aceptado** significa acordado dentro del alcance actual; no prueba implementación de producto. **Propuesto** necesita validación antes de considerarse decisión final. Los cambios posteriores deben preservar trazabilidad con un ADR que lo sustituya cuando corresponda.

| ADR | Estado | Contenido |
|---|---|---|
| [0001](0001-preserve-generated-backend.md) | Aceptado | Preservar la base generada y separar organización de implementación |
| [0002](0002-modular-monolith.md) | Propuesto | Monolito modular y responsabilidades sin capas vacías |
| [0003](0003-provider-boundaries.md) | Propuesto | Proveedores intercambiables con semántica/capacidades explícitas |

No hay todavía decisiones cerradas de esquema PostgreSQL, contratos de endpoints, autenticación de negocio, frontend o despliegue. Se registrarán cuando una tarea autorizada las justifique.
