# Decisiones arquitectónicas

Un ADR registra contexto, decisión, estado, consecuencias y pendientes. **Aceptado** significa acordado dentro del alcance actual; no prueba implementación de producto. **Propuesto** necesita validación antes de considerarse decisión final. Los cambios posteriores deben preservar trazabilidad con un ADR que lo sustituya cuando corresponda.

| ADR | Estado | Contenido |
|---|---|---|
| [0001](0001-preserve-generated-backend.md) | Aceptado | Preservar la base generada y separar organización de implementación |
| [0002](0002-modular-monolith.md) | Aceptado en AIR-010 | Cuatro límites funcionales y responsabilidades sin capas vacías |
| [0003](0003-provider-boundaries.md) | Principios AIR-010 y contratos AIR-012 aceptados | Capacidades/precios explícitos, normalización, identidad y DTO ratificados |
| [0004](0004-demo-mvp-and-persistence.md) | Aceptado en AIR-010 | MVP demo, preferencias transitorias y ausencia de cuentas/BD inicial |
| [0005](0005-demo-body-read-deadline.md) | Aceptado en AIR-014 | Plazo absoluto de lectura en HTTP/1.1 NIO demo; acoplamiento y validación |
| [0006](0006-demo-frontend.md) | Aceptado para implementar AIR-018 | Cliente React/TypeScript/Vite, estado transitorio, CSS propio, pruebas y proxy demo local |
| [0007](0007-real-provider-admission.md) | Aceptado para el alcance documental AIR-019 | Solo DEMO habilitado; permisos pendientes y admisión futura con presupuesto de 0 € |

AIR-010 acepta alcance, UX y persistencia aplazada. AIR-011 verifica el contexto demo de test, AIR-012 ratifica los contratos serializados y AIR-013/014/015 implementan el backend demo. AIR-018 autoriza el frontend funcional; su decisión no acredita por sí sola la implementación ni los tests. Esquema eventual PostgreSQL, proveedores y despliegue conservan sus tareas propias. ADR-0001 conserva el historial de la base generada.
