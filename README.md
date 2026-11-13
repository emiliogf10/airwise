# Airwise

Proyecto de portfolio full stack para explorar y comparar alternativas de viaje: precio del vuelo, aeropuerto de origen, fechas, escalas, duración y estimaciones de transporte terrestre. Su propuesta es explicar los compromisos entre coste y comodidad con información trazable.

**Estado actual: proveedor demo determinista implementado y probado en AIR-013.** El backend incorpora modelos de dominio puros, puerto de proveedor y fixtures sintéticas versionadas. Los endpoints REST, frontend y motor de comparación siguen pendientes.

## Qué está presente y qué está previsto

| Área | Presente en este checkout | Previsto, aún sin implementar o validar |
|---|---|---|
| Backend | Java 21, Spring Boot 4.1.2, modelos, puerto y proveedor demo; compilación offline correcta | Búsqueda REST, wiring de ejecución demo y comparación |
| Dependencias backend | Web MVC, Security, Data JPA, Validation, Flyway, PostgreSQL, Actuator y DevTools resueltos | Configuración de persistencia/seguridad de negocio e integración completa |
| Pruebas | Jupiter 6.0.3; 62 casos satisfactorios de contexto, dominio, proveedor, corpus y aislamiento | Pruebas HTTP y de integración PostgreSQL/JPA/Flyway |
| Frontend | No existe | React, TypeScript, Vite, Tailwind CSS, TanStack Query; Vitest y Playwright |
| Infraestructura | Git inicializado y plantillas GitHub | Docker Compose, GitHub Actions, observabilidad y despliegue |
| Documentación | Instrucciones, roles, skills, planificación, ADR y [contratos normalizados](docs/architecture/NORMALIZED_CONTRACTS.md) con [ejemplos JSON](docs/api/EXAMPLES.md) | OpenAPI, Javadoc del dominio y manual de funciones cuando existan |

El [inventario tecnológico](docs/developer-guide/TECHNOLOGY_INVENTORY.md) detalla las 15 dependencias directas y las comprobaciones pendientes. Una dependencia declarada no demuestra que una funcionalidad esté implementada o que el contexto Spring arranque.

## Alcance del MVP acordado

El MVP será demo determinista sin Internet: búsqueda ida/ida-vuelta, 1–3 aeropuertos de origen, un destino, un adulto/turista, itinerarios, filtros/orden/paginación y comparación explicable. Las preferencias son transitorias y las estimaciones terrestres manuales opcionales. Cuentas, favoritos persistentes, historial, alertas, compra/pagos y proveedores reales se aplazan; no se necesita BD de negocio inicial. Requisitos y UX en la visión arquitectónica.

La arquitectura aceptada como diseño es un monolito con cuatro límites: Airport, Flight Search, Flight Provider y Flight Comparison. El [adaptador demo](docs/architecture/PROVIDERS.md) ya implementa el puerto de Search; Travelpayouts/SerpApi siguen sin implementar. Las capas se introducirán donde aporten valor, sin estructuras vacías. Véanse la [visión arquitectónica](docs/architecture/OVERVIEW.md) y las [decisiones](docs/decisions/README.md).

Toda presentación deberá distinguir **precio observado**, **resultado reciente**, **precio anunciado por vendedor**, **confirmación en reserva**, **estimación** y **desconocido**. Demo siempre significa datos sintéticos. No se inventarán disponibilidad, equipaje, tarifas ni datos ausentes.

## Evidencia previa de proveedores

Resultados aportados por el usuario, sin repetir las pruebas ni copiar las PoC en este checkout:

- Travelpayouts: 16 consultas HTTP 200 y 25 registros de precios observados; exploración económica, cobertura irregular, caché y equipaje insuficiente.
- SerpApi: 6 peticiones HTTP 200, incluida una de cuenta; 61 entradas con fases y solapamientos; segmentos, múltiples orígenes, regreso y vendedor comprobados; equipaje parcial y plan observado de 250 búsquedas mensuales.

El presupuesto de API es **0 €**. Publicación, conservación y reutilización siguen pendientes para ambos proveedores. No hay adaptadores implementados en `backend/`. Consulte la [política de proveedores](docs/api/PROVIDER_POLICY.md) y la [evidencia previa](docs/architecture/PROVIDER_EVIDENCE.md).

## Estructura actual

```text
airwise/
├── AGENTS.md                         # Acuerdos y coordinador
├── .codex/
│   ├── config.toml                   # Configuración de agentes del proyecto
│   └── agents/                       # Seis especialistas TOML
├── .agents/skills/                   # Cinco procedimientos reutilizables
├── .github/                         # Plantillas de issues y PR, sin pipelines
├── backend/                         # Proyecto Initializr existente, intacto
├── planning/                        # Roadmap, backlog, mejoras y changelog
├── docs/
│   ├── user-manual/                  # Alcance del manual, sin pantallas ficticias
│   ├── developer-guide/              # Entorno real, flujo Codex y validación
│   ├── architecture/                 # Diseño acordado, todavía sin implementar
│   ├── api/                          # Límites del contrato y política de proveedores
│   ├── security/                     # Criterios iniciales y gestión de secretos
│   └── decisions/                    # ADR y estado de cada decisión
├── scripts/validate_repository.py    # Validación documental local, sin red
├── CONTRIBUTING.md
├── SECURITY.md
└── .gitignore
```

El árbol detallado y los resultados están en [VALIDATION.md](docs/developer-guide/VALIDATION.md). No se ha creado un frontend, un proyecto Maven raíz ni una base de datos.

## Trabajar desde PowerShell

Abra la raíz `airwise/` en VS Code. No regenere el backend ni cambie su POM para ajustarlo a documentación previa.

```powershell
java -version
mvn -version
py -3.14 scripts/validate_repository.py
```

Python 3.11+ se usa únicamente para esta validación documental y TOML, no forma parte del stack de Airwise. El backend tiene `mvnw.cmd`, pero su distribución y su construcción aún no se ejecutaron. No se presenta un comando de arranque, tests o frontend como validado. La [guía de desarrollo](docs/developer-guide/README.md) distingue comandos de inspección y pasos futuros.

## Uso de Codex

El agente principal sigue `AGENTS.md` como **Orchestrator**. Los seis especialistas están definidos en `.codex/agents/`: Backend, Frontend, Database, Security & QA, DevOps y Documentation. Configuración escrita y validación estática no equivalen a una invocación real comprobada: el [informe de Codex](docs/developer-guide/CODEX_SETUP.md) registra versión, descubrimiento y límites.

Ejemplo de petición: «Revisa AIR-010 como Orchestrator; utiliza el rol Backend solo si necesitas una revisión independiente. No cambies código». Si la sesión no permite seleccionar el agente personalizado, el principal puede leer su TOML y aplicar esas instrucciones, informando que es un rol sin proceso independiente.

Skills locales: `$task-planning`, `$api-integration`, `$database-migrations`, `$testing-and-review` y `$documentation-sync`. Su uso no autoriza por sí mismo llamadas reales, cambios del backend ni pasar de fase.

## Planificación y colaboración

- [Roadmap](planning/ROADMAP.md): fases, dependencias y entregables.
- [Backlog](planning/BACKLOG.md): tareas AIR, estados, Definition of Done y evidencia.
- [Mejoras propuestas](planning/IMPROVEMENTS.md): ideas no aprobadas para desarrollo.
- [Changelog](planning/CHANGELOG.md): cambios sin versión publicada.
- [Contribución y revisión](CONTRIBUTING.md), [seguridad](SECURITY.md) y [manual de usuario](docs/user-manual/README.md).

AIR-011 valida el contexto demo mediante exclusión de DataSourceAutoConfiguration limitada al test y aserciones de inicialización real. El diagnóstico inicial y el resultado corregido constan en el registro de validación. PostgreSQL, JPA y Flyway mantienen sus pruebas de integración pendientes. AIR-012 conserva su estado Pendiente. Ya existe un primer commit local.
