# Backlog de Airwise

Fuente de estado del trabajo. Las tareas futuras necesitan una petición autorizada; el backlog no concede por sí mismo permiso para ejecutarlas. El responsable es un rol funcional, no una afirmación de que se haya invocado un subagente.

## Estados y prioridades

Estados: **Propuesta** (sin aprobar alcance), **Pendiente** (definida, sin iniciar), **En curso**, **En revisión**, **Bloqueada**, **Hecha**, **Cancelada**. Prioridades: **P0** bloqueo o fundamento; **P1** siguiente valor; **P2** posterior.

Transiciones: Propuesta → Pendiente cuando se acuerde alcance; Pendiente → En curso al autorizar su ejecución y comprobar dependencias; En curso → En revisión con entrega y evidencia; En revisión → Hecha solo tras la DoD. Cualquier estado activo puede quedar Bloqueada con causa y condición para reanudar; vuelve al estado activo al resolverse. Una revisión fallida vuelve a En curso. Cancelada conserva motivo e historial. No se marca Hecha por tener archivos o un plan.

## Definition of Done

- Alcance y criterios de la tarea satisfechos; dependencias o excepciones justificadas.
- Resultado revisable, sin cambios ajenos ni credenciales, y con errores/desconocidos correctamente representados.
- Verificaciones pertinentes ejecutadas y registradas con resultado; límites y pruebas no ejecutadas explícitos. Para documentación basta comprobar fuentes, formatos, enlaces y coherencia; no inventar tests de producto.
- Documentos/ADR afectados y este backlog sincronizados. No confundir configuración escrita con disponibilidad o invocación comprobada.
- Autorizaciones específicas respetadas, sin gasto de API, publicación o cambios globales no permitidos.

## Fase 1 — entrega organizativa

### AIR-001 — Inventariar y preservar el backend generado

- Descripción: inspeccionar POM, estructura, código generado, wrapper y herramientas sin alterar backend/.
- Prioridad: P0.
- Estado: Hecha.
- Responsable: Backend Agent.
- Dependencias: ninguna.
- Aceptación: distinguir dependencias declaradas/resueltas y stack previsto; verificar preservación de todos los archivos iniciales.
- Evidencia: [inventario](../docs/developer-guide/TECHNOLOGY_INVENTORY.md) y [validación](../docs/developer-guide/VALIDATION.md); inspección XML/archivos y comparación SHA-256, sin compilar ni arrancar Spring.

### AIR-002 — Establecer documentación y planificación inicial

- Descripción: preparar README, manuales iniciales, ADR, roadmap, backlog, mejoras y changelog sin documentar funciones inexistentes.
- Prioridad: P0.
- Estado: Hecha.
- Responsable: Documentation Agent.
- Dependencias: AIR-001.
- Aceptación: enlaces internos válidos, estados reales, decisiones aceptadas/propuestas distinguidas y DoD operativa.
- Evidencia: documentos enlazados desde [README](../README.md) y resultados del validador en [VALIDATION.md](../docs/developer-guide/VALIDATION.md).

### AIR-003 — Preparar instrucciones, agentes y skills compatibles

- Descripción: configurar coordinador, seis especialistas y cinco procedimientos locales usando formatos oficiales.
- Prioridad: P0.
- Estado: Hecha.
- Responsable: Orchestrator Agent.
- Dependencias: AIR-001.
- Aceptación: TOML válido, metadatos de skills correctos, delegación selectiva y estado de disponibilidad expresamente documentado; sin cambios globales.
- Evidencia: [configuración Codex](../docs/developer-guide/CODEX_SETUP.md), [AGENTS](../AGENTS.md) y validación de archivos. No se declara invocación de especialistas; esa comprobación separada está en AIR-006.

### AIR-004 — Preparar contribución y plantillas GitHub

- Descripción: establecer proceso de revisión, convenciones de commits, incidencias y PR, con política de seguridad inicial.
- Prioridad: P1.
- Estado: Hecha.
- Responsable: DevOps Agent.
- Dependencias: AIR-002.
- Aceptación: plantillas locales utilizables, sin workflows, commits, push, publicación ni contactos inventados.
- Evidencia: [CONTRIBUTING](../CONTRIBUTING.md), [SECURITY](../SECURITY.md) y `.github/`; ningún pipeline generado.

### AIR-005 — Verificar la entrega organizativa

- Descripción: comprobar estructura, metadatos, enlaces, coherencia AIR, ausencia de secretos y preservación del backend.
- Prioridad: P0.
- Estado: Hecha.
- Responsable: Security & QA Agent.
- Dependencias: AIR-001, AIR-002, AIR-003, AIR-004.
- Aceptación: validador local sin errores; resumen con resultados reales y limitaciones, sin APIs de vuelos ni desarrollo de producto.
- Evidencia: `scripts/validate_repository.py` terminó con RESULTADO CORRECTO: seis TOML de especialistas, cinco skills, 59 enlaces locales, 17 tareas y diez archivos backend preservados; límites del chequeo documentados en [VALIDATION.md](../docs/developer-guide/VALIDATION.md).
- Seguimiento previo al primer commit (11/11/2026): revisión real de `security_qa` sobre 47 candidatos sin secretos ni datos privados detectados; aplicadas diez exclusiones preventivas al `.gitignore` raíz y actualizado el informe de validación sin eliminar la evidencia inicial. `py -3.14 scripts/validate_repository.py`, comprobación de enlaces, `git diff --check` y pruebas de `git check-ignore` satisfactorios; los 47 candidatos se conservan y los diez archivos backend mantienen sus SHA-256. Sin compilaciones, cambios del backend, instalaciones, llamadas a proveedores ni operaciones de escritura en Git.

### AIR-006 — Confirmar selección e invocación de agentes en una sesión nueva

- Descripción: comprobar que una sesión IDE del proyecto confiable descubre y puede seleccionar los seis agentes y las cinco skills, sin implementar producto.
- Prioridad: P2.
- Estado: Hecha.
- Responsable: Orchestrator Agent.
- Dependencias: AIR-003.
- Aceptación: evidencia de catálogo/selección o prueba acotada de un especialista; distinguir límites de la sesión actual y no invocar todos automáticamente.
- Evidencia: 11/11/2026, los seis especialistas expuestos en `collaboration.spawn_agent.agent_type`, con descripciones coincidentes con sus TOML; invocación real de `backend` en `/root/air006_backend_readonly`, confirmada por `list_agents` como `completed` y por su informe final. Dos lecturas con código 0 (`chunk_id=3165b7` y `c16188`): Java 21, Spring Boot 4.1.2 y 15 dependencias declaradas. Las cinco skills figuran en el catálogo de esta sesión. `py -3.14 scripts/validate_repository.py` dio código 0 y RESULTADO CORRECTO: seis TOML, cinco skills, 60 enlaces, 17 tareas y diez archivos backend preservados mediante SHA-256. Revisión de fuentes, documentos y límites completada; DoD satisfecha tras pasar por En curso y En revisión. Mecanismo, informe, trazabilidad y límites en [CODEX_SETUP.md](../docs/developer-guide/CODEX_SETUP.md).
- Límites: ejecución individual de `frontend`, `database`, `security_qa`, `devops` y `documentation` sin probar, aunque seleccionables; selector visual y otras sesiones/clientes no verificados. No se invocaron todos ni se ejecutaron compilaciones, instalaciones o APIs externas; backend preservado.

## Fases posteriores — no iniciadas

### AIR-010 — Validar alcance MVP y arquitectura modular

- Descripción: acordar casos mínimos, módulos, dependencias y categorías de precio; convertir ADR propuestos en decisiones revisadas cuando corresponda.
- Prioridad: P0.
- Estado: Pendiente.
- Responsable: Orchestrator Agent.
- Dependencias: AIR-001, AIR-002, AIR-005.
- Aceptación: MVP acotado, contexto y límites modulares, no objetivos explícitos y criterios verificables; sin estructuras vacías.
- Evidencia: no ejecutada; [ADR-0002](../docs/decisions/0002-modular-monolith.md) es propuesta.

### AIR-011 — Validar construcción y entorno del backend existente

- Descripción: comprobar wrapper, dependencias resueltas, versión de Jupiter y requisitos del test de contexto sin sustituir la base.
- Prioridad: P0.
- Estado: Pendiente.
- Responsable: Backend Agent.
- Dependencias: AIR-010.
- Aceptación: comandos/resultados reproducibles, necesidades de BD/test identificadas y plan de corrección si falla. Cualquier cambio de POM, wrapper o configuración deberá justificarse y autorizarse antes.
- Evidencia: no ejecutada; construcción, arranque y contexto Spring no probados en fase 1.

### AIR-012 — Definir contrato normalizado y capacidades de proveedores

- Descripción: separar DTO externo, itinerario, trayectos y ámbito monetario; representar fuente, tiempos, moneda, errores y desconocidos.
- Prioridad: P0.
- Estado: Pendiente.
- Responsable: Backend Agent.
- Dependencias: AIR-010, AIR-011.
- Aceptación: contrato revisado, ejemplos sintéticos y casos negativos; observado/reciente/estimado/confirmado diferenciados, tokens fuera del dominio persistente.
- Evidencia: no ejecutada; [ADR-0003](../docs/decisions/0003-provider-boundaries.md) es propuesta.

### AIR-013 — Implementar proveedor demo identificado

- Descripción: crear datos sintéticos y adaptador demo explícito como primera fuente de desarrollo.
- Prioridad: P1.
- Estado: Pendiente.
- Responsable: Backend Agent.
- Dependencias: AIR-012.
- Aceptación: origen demo visible, resultados deterministas y parciales probados; ningún fallo real sustituido silenciosamente por demo; sin red.
- Evidencia: no implementado.

### AIR-014 — Implementar búsqueda y normalización con demo

- Descripción: exponer el caso de búsqueda por ruta/fecha bajo el contrato aprobado, con validación y errores explícitos.
- Prioridad: P1.
- Estado: Pendiente.
- Responsable: Backend Agent.
- Dependencias: AIR-013, AIR-016.
- Aceptación: búsqueda reproducible, validación/errores/cero resultados diferenciados y tests; contrato API documentado cuando exista.
- Evidencia: no implementada; no hay endpoints de negocio actuales.

### AIR-015 — Comparar alternativas de viaje con criterios transparentes

- Descripción: comparar precio, escalas y duración con preferencias acotadas; introducir costes terrestres solo con fuente o supuestos aprobados.
- Prioridad: P1.
- Estado: Pendiente.
- Responsable: Backend Agent.
- Dependencias: AIR-014.
- Aceptación: alternativas comparables, reglas explicables y tratamiento de desconocidos; sin inventar equipaje ni total puerta a puerta.
- Evidencia: no implementado.

### AIR-016 — Definir seguridad y estrategia de pruebas del MVP

- Descripción: acordar superficies públicas/privadas, errores, secretos y pruebas unitarias, de contrato e integración apropiadas al alcance.
- Prioridad: P0.
- Estado: Pendiente.
- Responsable: Security & QA Agent.
- Dependencias: AIR-010, AIR-011.
- Aceptación: criterios de seguridad verificables, aislamiento de proveedores y herramientas/versiones justificadas; no obliga a introducir cuentas de usuario en el MVP.
- Evidencia: no ejecutada; [baseline](../docs/security/BASELINE.md) contiene criterios iniciales.

### AIR-017 — Acordar persistencia y entorno de pruebas, si lo exige el MVP

- Descripción: justificar datos a conservar y definir modelo relacional, migraciones y pruebas; coordinar JPA con Backend Agent.
- Prioridad: P1.
- Estado: Pendiente.
- Responsable: Database Agent.
- Dependencias: AIR-010, AIR-011, AIR-016.
- Aceptación: necesidad de persistencia demostrada o decisión explícita de posponerla; si se introduce, migraciones probadas sin datos/servicios de producción.
- Evidencia: no ejecutada; no hay base ni entidades creadas.

### AIR-018 — Preparar experiencia frontend tras aprobar el contrato

- Descripción: decidir estructura React/TypeScript y diseñar estados accesibles de búsqueda y comparación antes de generar el proyecto.
- Prioridad: P1.
- Estado: Pendiente.
- Responsable: Frontend Agent.
- Dependencias: AIR-014, AIR-015, AIR-016.
- Aceptación: flujo acotado, contratos y estados de carga/vacío/error/parcialidad aprobados; herramientas justificadas antes de instalar.
- Evidencia: no ejecutada; no hay frontend ni pantallas.

### AIR-019 — Aclarar permisos y presupuesto antes de adaptadores reales

- Descripción: revisar condiciones concretas de publicación, almacenamiento, reutilización y cuota de cada proveedor.
- Prioridad: P0.
- Estado: Pendiente.
- Responsable: Orchestrator Agent.
- Dependencias: AIR-012, AIR-016.
- Aceptación: alcance permitido y dudas registradas, plan persistente de consumo de 0 €; autorización específica antes de cualquier prueba real o contacto externo.
- Evidencia: no ejecutada; PoC históricas no equivalen a permisos de uso público.

### AIR-020 — Planificar CI y entorno sobre comandos comprobados

- Descripción: definir automatización mínima cuando los componentes y pruebas reales estén disponibles; separar validación y publicación.
- Prioridad: P2.
- Estado: Pendiente.
- Responsable: DevOps Agent.
- Dependencias: AIR-011, AIR-016, AIR-018.
- Aceptación: plan o workflow autorizado basado en comandos ejecutados, mínimos permisos y secretos externos; ningún despliegue automático no solicitado.
- Evidencia: no ejecutada; no se han creado workflows ni contenedores.
