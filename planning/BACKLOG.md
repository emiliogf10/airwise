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
- Límites al cierre de AIR-006: ejecución individual de los otros cinco sin probar entonces; el seguimiento de AIR-010 registra después `frontend`, `database` y `security_qa`. Selector visual y otras sesiones/clientes no verificados. No se invocaron todos ni se ejecutaron compilaciones, instalaciones o APIs externas; backend preservado.

## Diseño y fases posteriores

AIR-010 autoriza únicamente análisis y documentación de diseño. Las tareas de implementación, validación ejecutable e integración posteriores necesitan petición expresa.

### AIR-010 — Validar alcance MVP y arquitectura modular

- Descripción: acordar MVP demo ida/ida-vuelta, módulos, dominio, persistencia, proveedores, comparación, contratos HTTP conceptuales, UX y riesgos; convertir propuestas relevantes en decisiones de diseño.
- Prioridad: P0.
- Estado: Hecha.
- Responsable: Orchestrator Agent.
- Dependencias: AIR-001, AIR-002, AIR-005.
- Aceptación: límites funcionales explícitos; cuatro módulos sin sobreingeniería; dominio para ida/ida-vuelta, tiempos y dinero; proveedores/capacidades diferenciados; reglas de comparación; persistencia justificada; contratos HTTP conceptuales coherentes; riesgos/pruebas; roadmap/backlog y documentos verificables. Sin código, BD, llamadas externas o estructuras vacías.
- Evidencia: 11/11/2026, dependencias AIR-001, AIR-002 y AIR-005 satisfechas; análisis real de `backend`, `database`, `frontend` y `security_qa`, todos de solo lectura. Diseño integrado en [OVERVIEW](../docs/architecture/OVERVIEW.md), MODULES, DOMAIN_MODEL, PROVIDERS, TRAVEL_COST_ENGINE, API_DESIGN y BASELINE; ADR-0002/0003 aceptados con historial y ADR-0004 añadido. `security_qa` revisó el diseño y confirmó resueltas sus dos observaciones. `py -3.14 scripts/validate_repository.py`: código 0, RESULTADO CORRECTO (101 enlaces, seis TOML, cinco skills, 17 tareas, diez archivos backend con SHA-256 preservados); `git diff --check` código 0 y revisión de archivos nuevos sin errores. Diez criterios, comandos y trazabilidad en [VALIDATION](../docs/developer-guide/VALIDATION.md); DoD satisfecha tras En curso y En revisión. Validador, backend, HEAD e índice preservados. Sin ejecutar AIR-011 ni otras tareas.

### AIR-011 — Validar construcción y entorno del backend existente

- Descripción: comprobar wrapper, dependencias resueltas, versión de Jupiter y requisitos del test de contexto sin sustituir la base.
- Prioridad: P0.
- Estado: Hecha.
- Responsable: Backend Agent.
- Dependencias: AIR-010.
- Aceptación: Wrapper test satisfactorio e inicialización real del contexto demo sin persistencia; exclusión local al test y comportamiento de producción preservado. Validador satisfactorio con artefactos excluidos e histórico conservado. Integración PostgreSQL/JPA/Flyway pendiente explícitamente; documentación y pruebas pertinentes verificadas.
- Evidencia: 12/11/2026, diagnóstico inicial de `backend`, `database` y `security_qa` conservado, con Temurin 21.0.12+8, Wrapper 3.3.4/Maven 3.10.0 y Boot 4.1.2. Tras autorización expresa se modificó solo BackendApplicationTests.java dentro del backend. `mvnw.cmd test` código 0, un test, cero fallos/errores/omitidos; contexto activo, bean BackendApplication y cero DataSources comprobados. Exclusión DataSourceAutoConfiguration compatible con Boot 4.1.2, sin exclusiones adicionales. `py -3.14 scripts/validate_repository.py` código 0, 107 enlaces, seis TOML, cinco skills y 17 tareas; cinco regresiones aisladas del validador satisfactorias. Relectura real de security_qa aprobada. `git diff --check` código 0; snapshot histórico intacto, nueve originales idénticos y test autorizado modificado. Comandos, logs, alcance y limitaciones en [VALIDATION](../docs/developer-guide/VALIDATION.md). DoD satisfecha tras reanudar En curso y pasar por En revisión. AIR-012 sigue Pendiente.
- Límites: se valida el contexto demo configurado en el test. PostgreSQL, JPA y Flyway requieren integración posterior, y el arranque con configuración principal sigue pendiente. El histórico SHA-256 es informativo y las fuentes esenciales continúan comprobándose.

### AIR-012 — Definir contrato normalizado y capacidades de proveedores

- Descripción: separar DTO externo, itinerario, trayectos y ámbito monetario; representar fuente, tiempos, moneda, errores y desconocidos.
- Prioridad: P0.
- Estado: Hecha.
- Responsable: Backend Agent.
- Dependencias: AIR-010, AIR-011.
- Aceptación: campos, tipos, nulabilidad e invariantes de búsqueda, vuelos/precios, puerto/capacidades y DTO HTTP precisos; identidad demo, estados/errores, límites y ejemplos coherentes; sin implementación ni tokens externos en dominio.
- Evidencia: 12/11/2026, contratos airwise-v1, 18 ejemplos JSON y matriz CT-01 a CT-37; delegación real y relecturas conformes de backend y security_qa. Comprobador documental local código 0, 1531 aserciones; validador del repositorio código 0; git diff --check código 0. SHA-256 confirma los 19 archivos backend, validador e índice intactos respecto al inicio; HEAD sin cambios. Puertos/normalización, moneda/ámbito, identidad de segmentos y ranking con TIE_UNRESOLVED revisados. Registro y límites en [VALIDATION](../docs/developer-guide/VALIDATION.md), tipos en [contrato normativo](../docs/architecture/NORMALIZED_CONTRACTS.md), HTTP y ejemplos enlazados desde allí. DoD documental satisfecha tras En curso → En revisión → Hecha. Tests Java de contratos, ejecución demo e integración PostgreSQL/JPA/Flyway pendientes; AIR-013/AIR-016 no iniciadas.

### AIR-013 — Implementar proveedor demo identificado

- Descripción: crear datos sintéticos y adaptador demo explícito como primera fuente de desarrollo.
- Prioridad: P1.
- Estado: Hecha.
- Responsable: Backend Agent.
- Dependencias: AIR-012.
- Aceptación: origen demo visible, fixtures/catálogo/calendario/reloj por versión, resultados e IDs deterministas; ida/ida-vuelta, varios orígenes, vacío, desconocidos, monedas y fallos parciales/totales probados; ningún fallback silencioso; sin red ni BD de negocio.
- Evidencia: 13/11/2026, proveedor demo puro con 32 fuentes Java nuevas, recurso demo-v1 (11 escenarios/16 registros) y cinco clases nuevas de tests. Delegación real backend y revisión security_qa conforme (539229); Orchestrator integra/ejecuta. Wrapper offline clean compile y test con código 0: 33 fuentes compiladas, 62 tests, cero fallos/errores/omitidos, incluido contexto existente. Validador y diff código 0; 38 archivos backend nuevos, preexistentes/POM/Wrapper/configuración/testcontexto intactos, HEAD/índice sin cambios. Escala, ida/vuelta/múltiples orígenes, empates, monedas, desconocidos/cero, parcialidad, caducidad, vacío/fallo, corpus inválido y aislamiento probados. Evidencia y límites en [VALIDATION](../docs/developer-guide/VALIDATION.md), escenarios en [PROVIDERS](../docs/architecture/PROVIDERS.md). Sin REST, comparación, BD ni APIs; AIR-014/015/016 no iniciadas.

### AIR-014 — Implementar búsqueda y normalización con demo

- Descripción: exponer catálogo y búsqueda demo por aeropuertos/fechas bajo contrato aprobado, con validación, filtros, orden, paginación y errores explícitos.
- Prioridad: P1.
- Estado: Hecha.
- Responsable: Backend Agent.
- Dependencias: AIR-013, AIR-016.
- Aceptación: ida/ida-vuelta y 1–3 orígenes, búsqueda reproducible, paginación estable, observaciones separadas, validación/vacío/fallo total/parcial diferenciados y tests; API documentada según implementación, sin fuente real oculta.
- Evidencia: 16/11/2026, incrementos A–D completos; O-01, perfil demo/guardas, catálogo/búsqueda REST con DTO, validación, filtros/orden/páginas y causas parciales. Wrapper offline `clean test`: BUILD SUCCESS, 181 pruebas, cero fallos/errores/omitidas; Java independiente/curl y Tomcat real verifican catálogo200, búsqueda200, fallo503 y comparación403. Plazo absoluto5s corregido y probado en HTTP/1.1 NIO, incluidos chunked/trailers/bloqueo/desconexión y permisos liberados. Revisiones estáticas reales Backend sobre aplicación/transporte y entrega Security integradas; coordinador ejecuta y cierra tras En curso → En revisión → corrección → En revisión → Hecha. Validador documental y diff sin errores, ejemplos/enlaces/secretos revisados. POM/Wrapper/base protegida intactos, sin nuevas dependencias, APIs de vuelos, commits o push. `package` offline sigue sin plugin JAR en caché; no es un artefacto publicado. Evidencia y limitaciones en [VALIDATION](../docs/developer-guide/VALIDATION.md), arranque en [DEMO_RUNTIME](../docs/developer-guide/DEMO_RUNTIME.md). AIR-015 no iniciada.

### AIR-015 — Comparar alternativas de viaje con criterios transparentes

- Descripción: comparar precio, escalas y duración con preferencias acotadas; introducir costes terrestres solo con fuente o supuestos aprobados.
- Prioridad: P1.
- Estado: Hecha.
- Responsable: Backend Agent.
- Dependencias: AIR-014.
- Aceptación: selección de 2–4 ofertas reconstituida en servidor; grupos de moneda/evidencia/ámbito, prioridades/desempates explicables y desconocidos probados. Estimaciones opcionales separadas, sin duplicación ni total exacto; sin inventar equipaje o viaje completo desde un trayecto.
- Evidencia: alcance autorizado el16/11/2026 e implementado/revisado el17/11. Baseline AIR-014:23 pruebas correctas; motor20, aplicación/DTO21 y nuevas pruebas HTTP/errores16. Wrapper offline `clean test`: BUILD SUCCESS,238 pruebas, cero fallos/errores/omitidas,74 fuentes principales y25 de test. Java independiente/curl:24 comprobaciones correctas, incluidos cuatro criterios, selección entre páginas, grupos/unknown/vigencia, estimaciones/subtotal incompleto, CORS y errores422/409/400/503; proceso detenido. Delegación real Backend, Security & QA y Documentation; revisión estática de Security y revisión documental conformes. Validador documental código0,203 destinos locales y17 tareas;18 bloques JSON válidos, captura de subtotal idéntica al ejemplo y diff sin errores. Ocho archivos protegidos, incluido corpus demo-v1, sin cambios; POM/Wrapper/base conservados, sin dependencias nuevas, persistencia, APIs externas, commits o push. DoD satisfecha tras En curso → En revisión → Hecha. Evidencias y límites de empaquetado/TLS/HTTP2 en [VALIDATION](../docs/developer-guide/VALIDATION.md). AIR-018 no iniciada.

### AIR-016 — Definir seguridad y estrategia de pruebas del MVP

- Descripción: concretar superficies públicas/operativas, configuración de seguridad y estrategia de pruebas del MVP a partir de la baseline conceptual de AIR-010 y el entorno validado en AIR-011.
- Prioridad: P0.
- Estado: Hecha.
- Responsable: Security & QA Agent.
- Dependencias: AIR-010, AIR-011.
- Aceptación: política HTTP exacta y compatible, perfil demo sin PostgreSQL diseñado, superficies y riesgos revisados, matriz por niveles y controles pendientes explícitos; documentación/validaciones pertinentes satisfactorias, sin modificar producción ni iniciar AIR-014/015.
- Evidencia: 13/11/2026, diseño integrado en [BASELINE](../docs/security/BASELINE.md), [DEMO_RUNTIME](../docs/developer-guide/DEMO_RUNTIME.md) y [TESTING_STRATEGY](../docs/developer-guide/TESTING_STRATEGY.md): 49 casos y 12 riesgos. Revisión real de security_qa y backend conforme tras precisar CSRF/sesión, Accept406, 10000tokens y admisión única/ERROR. APIs/metadata verificadas estáticamente en JAR locales Boot4.1.2/Security7.1.2/Jackson3.1.7. O-01 capacidades all-UNVERIFIED con outputKinds no vacío documentada como desviación contractual interna de prioridad baja, corrección autorizada posterior; no vulnerabilidad remota confirmada. Verificador estático independiente código 0 (167 destinos locales, seis TOML, cinco skills, 17 tareas, 18 ejemplos); git diff --check código 0. Los 130 archivos backend, validador versionado, HEAD e índice intactos. Evidencia en [VALIDATION](../docs/developer-guide/VALIDATION.md); DoD documental satisfecha tras En curso → En revisión → Hecha. No Maven nuevo ni cambios backend/configuración. Los 62 tests se conservan como evidencia histórica AIR-013; AIR-014/015 no iniciadas.

### AIR-017 — Acordar persistencia y entorno de pruebas, si lo exige el MVP

- Descripción: justificar datos a conservar y definir modelo relacional, migraciones y pruebas; coordinar JPA con Backend Agent.
- Prioridad: P1.
- Estado: Pendiente.
- Responsable: Database Agent.
- Dependencias: AIR-010, AIR-011, AIR-016.
- Aceptación: necesidad de persistencia demostrada o decisión explícita de posponerla; si se introduce, migraciones probadas sin datos/servicios de producción.
- Evidencia: pendiente de ejecución; AIR-010 justifica MVP sin BD de negocio ni migraciones iniciales. AIR-017 ratificará aplazamiento o justificará un caso durable concreto; no hay base ni entidades creadas.

### AIR-018 — Implementar frontend React y TypeScript sobre demo local

- Descripción: implementar experiencia React/TypeScript/Vite de catálogo, búsqueda y comparación sobre los tres endpoints demo locales. El adjunto AIR-018 autoriza expresamente ampliar la preparación inicial a implementación completa; backend protegido.
- Prioridad: P1.
- Estado: Hecha.
- Responsable: Frontend Agent.
- Dependencias: AIR-014, AIR-015, AIR-016.
- Aceptación: frontend funcional responsive/accesible, catálogo real local, formulario validado, filtros/orden/páginas de servidor, selección2–4 conservada dentro de consulta/versión y comparación de cuatro criterios por grupos con estimaciones explícitas. Errores/parcialidad/desconocidos/demo claros; typecheck/build/lint/tests satisfactorios, integración UI local y documentación actualizada; backend íntegro y sin credenciales ni APIs de vuelos.
- Evidencia: implementación verificada el18/11/2026 tras autorización ampliada: estructura/dependencias justificadas en ADR-0006, React/TS/Vite, controles nativos, filtros/orden/páginas servidor y comparación de cuatro criterios con estimaciones explícitas. Delegación real Frontend, Security & QA y Documentation; hallazgos corregidos e integrados por Orchestrator. `npm test`:53 pasadas, cero fallos y una integración omitida por ser opt-in; `npm run test:integration:ui`:esa prueba pasada con6HTTP reales entre React/jsdom, proxy y backend; `npm run test:integration`:12HTTP reales del cliente con tres endpoints/cuatro criterios, subtotal, desconocidos/cero, parcial/vacío y409/503. Typecheck/build/lint código0 sin avisos;22pares de contraste de la paleta calculados satisfactoriamente (no auditoría renderizada). SHA-256 de109archivos backend idéntico al inicio. Evidencia y límites en [VALIDATION](../docs/developer-guide/VALIDATION.md); guía [FRONTEND](../docs/developer-guide/FRONTEND.md). Sin claves, APIs de vuelos, cambios globales, commits ni push.
- Cierre de DoD,18/11/2026: tras el bloqueo histórico de Computer Use, el usuario pidió expresamente `agent-browser`. Revisión asistida en Chrome155.0.8059.39 con CLI0.39.1, perfil temporal y dominio local: tres endpoints por UI, cuatro criterios/estimaciones, filtros/paginación/selección, monedas/desconocidos/cero, vacío/parcial/error y teclado. Viewports1440/768/720/390/320 sin desbordamiento observado; texto CSS al200% probado en escritorio. Se detectó ISSUE-001 de contraste efectivo1.97:1 y se corrigió la prioridad de la regla hero entre capas; renderizado8.31:1. Ocho auditorías axe posteriores con cero violaciones y una regla incompleta de glifos decorativos `aria-hidden`, revisada manualmente. Tests53pasados+1opt-inomitido, typecheck/build/lint repetidos satisfactoriamente tras CSS. Security & QA revisó independientemente JSON/capturas/diff y dio conformidad acotada; documentación sincronizada y seguimiento temporal retirado al terminar las verificaciones. Informe y límites en [BROWSER_REVIEW](../docs/developer-guide/BROWSER_REVIEW.md). No se acredita zoom nativo, lector de pantalla, todos los navegadores ni certificación WCAG integral. Backend109hashes intactos; navegador y servidores detenidos, sin APIs externas, commits/push ni siguiente fase iniciada.

### AIR-019 — Aclarar permisos y presupuesto antes de adaptadores reales

- Descripción: revisar condiciones concretas de publicación, almacenamiento, reutilización y cuota de cada proveedor.
- Prioridad: P0.
- Estado: Hecha.
- Responsable: Orchestrator Agent.
- Dependencias: AIR-012, AIR-016.
- Aceptación: alcance permitido y dudas registradas, plan persistente de consumo de 0 €; autorización específica antes de cualquier prueba real o contacto externo.
- Evidencia: autorización expresa el19/11/2026; investigación de fuentes públicas oficiales registrada en [PROVIDER_ASSESSMENT](../docs/api/PROVIDER_ASSESSMENT.md), con matriz, fecha, fuentes y preguntas Q-T/Q-S/Q-B. [ADR-0007](../docs/decisions/0007-real-provider-admission.md) mantiene solo DEMO habilitado. Plan persistente0€ documentado, distingue intentos HTTP y créditos, todas las fases y reservas durables/atómicas futuras; control no implementado. Revisión real independiente Security & QA sin bloqueos e integrada precisión de acceso Aviasales; Documentation sincronizó README/ROADMAP/CHANGELOG. `py -3.14 scripts/validate_repository.py`, verificación local de hashes/URLs/seguridad y `git diff --check` satisfactorios:153archivos protegidos idénticos (109backend/31frontend/5skills/7Codex/1script) y2artefactos previos AIR-018 intactos. DoD documental satisfecha tras En curso → En revisión → Hecha; evidencia en [VALIDATION](../docs/developer-guide/VALIDATION.md). Cero llamadas a APIs/cuenta/continuaciones, sin contacto con proveedores ni código de aplicación; preguntas de permisos/cuota pendientes bloquean futura habilitación, no el cierre investigador. Sin commits/push ni otra fase iniciada.

### AIR-020 — Planificar CI y entorno sobre comandos comprobados

- Descripción: definir automatización mínima cuando los componentes y pruebas reales estén disponibles; separar validación y publicación.
- Prioridad: P2.
- Estado: Hecha.
- Responsable: DevOps Agent.
- Dependencias: AIR-011, AIR-016, AIR-018.
- Aceptación: plan o workflow autorizado basado en comandos ejecutados, mínimos permisos y secretos externos; ningún despliegue automático no solicitado.
- Evidencia: autorizada el19/11/2026; creado [ci.yml](../.github/workflows/ci.yml) con tres jobs sobre componentes existentes, SHA oficiales, permisos de lectura, timeouts y concurrencia. [Guía CI](../docs/developer-guide/CI.md) separa comprobación local de ejecución GitHub/Linux pendiente. Revisión DevOps real sin bloqueos; integrado matiz de jobs paralelos. Wrapper existente offline `clean test` con logger temporal limitado:238/0fallos/0errores/0omitidas; `npm ci` con descarga autorizada tras caché offline incompleta, `npm test`:53pasadas/1opt-inomitida; typecheck/lint/build0. Parser SnakeYAML seguro/política0, validador documental0 y diff-check0;152hashes backend/frontend/skills/Codex idénticos y validador original sin cambios. En curso → En revisión → Hecha para entrega local; comandos/evidencia/límites en [VALIDATION](../docs/developer-guide/VALIDATION.md). Sin cambios de aplicación, activación remota, publicación, contenedores o APIs; configuración y primera ejecución GitHub siguen pendientes y no son condición acreditada del cierre local.
