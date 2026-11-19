# Changelog

## Unreleased

### 2026-11-19 — promoción IMP-008 y núcleo sintético AIR-021 completado

- Autorizado núcleo Java puro de admisión y estimación con dobles; contratos de cuota/intentos/coste, ledger atómico limitado y estados explícitos. Sin conectar al demo ni añadir dependencias o transporte.
- ADR-0008 delimita memoria compartida en un escenario sintético y bloqueo de recuperación/multiproceso/uso real. Durabilidad futura vinculada a AIR-017, sin tablas, JPA, Flyway o infraestructura.
- Implementadas5clases y4clases de tests; Wrapper offline clean compile,42tests específicos y regresión limpia280/0fallos/0errores/0omitidas. Corregida carrera de reloj encontrada por QA, con regresión; revisiones Database/DevOps/Security & QA conformes y documentación sincronizada.
- Base previa, frontend, POM, Wrapper y endpoints preservados, sin dependencias nuevas. AIR-021 Hecha tras comprobaciones de alcance/documentación; cambios previos AIR-019 preservados, sin commits/push o consumo de proveedores. Durabilidad y permisos reales siguen pendientes.

### 2026-11-19 — ampliación AIR-019 según adjunto detallado

- Añadidas clasificación PENDIENTE DE ACLARACIÓN para ambos proveedores y tabla de usos previstos, prohibiciones y dudas específicas, sin convertir condiciones provisionales en permisos.
- Incorporada matriz técnica de11capacidades con PoC/documentación/contrato separados y estimaciones de fases para1/3orígenes. Inspección real de interfaces y DemoSearchService sin cambios de código; no existe coordinación genérica real habilitable por un flag.
- Precisados topes diarios/mensuales por operación y agregados de cuenta, ventanas UTC/ciclo comercial, reservas inciertas, concurrencia, fallo de registro y exclusión de credenciales/llamadas de proveedores en tests/CI. Todo es diseño, con0intentos y0€ autorizados.
- Registrada IMP-008 como propuesta de núcleo sintético, sin iniciar desarrollo. Preservadas las entregas anteriores AIR-019/020 y evidencias históricas; revisión/validación de esta ampliación en su registro propio.

### 2026-11-19 — workflow local de CI AIR-020

- Añadido `.github/workflows/ci.yml` con tres jobs: validador documental Python, compilación/tests backend mediante Wrapper y frontend con instalación por lockfile, tipos, lint, tests y build.
- Acciones oficiales fijadas por SHA completo; permisos de lectura, checkout sin persistir credenciales, topes temporales y cancelación por referencia. Sin cachés compartidas, artefactos publicados, despliegue ni secretos de proveedores. Logger de contraseña temporal limitado por argumento de pruebas, sin modificar backend.
- Verificados localmente 238 tests backend, 53 frontend con 1 integración opt-in omitida, npm ci autorizado, typecheck/lint/build, parser YAML y política. DevOps revisó fuentes oficiales y entrega sin bloqueos; documentación y contribución sincronizadas.
- AIR-020 Hecha para preparación local. GitHub/Linux y descarga de herramientas en runner pendientes; sin activación remota, commits/push ni gasto de API. Integridad de aplicación/configuración conservada; evidencia en [VALIDATION](../docs/developer-guide/VALIDATION.md).

### 2026-11-19 — investigación documental AIR-019 completada

- Documentadas condiciones de Travelpayouts Data API y SerpApi Google Flights desde fuentes oficiales públicas, con fecha, alcance acreditado, inferencias y dudas en la [evaluación de proveedores](../docs/api/PROVIDER_ASSESSMENT.md). AIR-019 Hecha tras revisión independiente sin bloqueos y validación documental/local; las dudas externas siguen bloqueando la habilitación real.
- [ADR-0007](../docs/decisions/0007-real-provider-admission.md) mantiene DEMO como única fuente habilitada y los proveedores reales UNVERIFIED, sin adaptadores. Las dudas de publicación, retención, reutilización y cuota bloquean una habilitación posterior.
- Definido un plan documental de consumo de 0 €, pendiente de implementación autorizada. Sin llamadas API/cuenta/continuaciones, contactos, cambios de producto, versión publicada ni siguiente fase iniciada.
- Preservados por SHA-256 los 153 archivos de código/configuración al inicio de AIR-019 y los artefactos previos de revisión AIR-018. Validador y diff-check satisfactorios; evidencia y límites en [VALIDATION](../docs/developer-guide/VALIDATION.md).

### 2026-11-18 — frontend demo AIR-018 completado

- Creado `frontend/` con el generador oficial React/TypeScript/Vite e instalación local reproducible mediante lockfile. Dependencias de pruebas añadidas; se conserva Oxlint del template sin añadir ESLint.
- Implementados cliente HTTP separado, estado transitorio, navegación por anclas, CSS propio, búsqueda/filtros/páginas, selección y comparación con estimaciones. ADR-0006 registra dependencias justificadas, alternativas aplazadas y límites de operación.
- Guía frontend, manual e inventario sincronizados; integración HTTP local real separada de componentes simulados y revisión asistida en navegador. AIR-018 Hecha tras revisión independiente conforme, sin cambios backend, proveedores, publicación ni versión publicada.
- Verificados 53 casos ordinarios, sin fallos y con 1 integración opt-in omitida; esa integración ejecutada aparte pasa con 6 peticiones reales desde App/jsdom. Cliente HTTP real verificado con 12 peticiones; typecheck, build y Oxlint satisfactorios, repetidos tras corregir CSS donde correspondía.
- Completadas 18 comprobaciones asistidas en Chrome real headless: flujos, teclado, estados y cinco viewports. Corregido contraste efectivo del antetítulo (ISSUE-001, 1,97→8,31:1); ocho auditorías posteriores sin violaciones y una regla incompleta de nodos decorativos revisados manualmente. No se afirma zoom nativo, suite E2E completa o certificación WCAG.

### 2026-11-17 — motor de comparación demo AIR-015

- Implementado POST /api/v1/demo/comparisons: selección servidor de2–4 ofertas sin depender de filtros/páginas, cuatro primarias, grupos compatibles, rankings de competición y recomendaciones limitadas a EVALUABLE_GROUP.
- Estimaciones terrestres explícitas, subtotales de componentes seleccionados, null/cero diferenciados y vigencia/desempates explicados; sin FX, puntuaciones opacas o ganador global entre grupos. Parcialidad operativa conserva causas mediante warning seguro sin ampliar schema.
- Security habilita solo la nueva combinación POST exacta y preflight, conservando CSRF específico, CORS, cuotas, JSON, errores y plazo de lectura. Moneda admitida pero incompatible produce INVALID_COMBINATION conforme a CT-26.
- Wrapper offline clean test:238 pruebas, cero fallos/errores/omitidas;24 comprobaciones con Java independiente/curl y ejemplos capturados. Sin dependencias nuevas, cambios del POM/Wrapper/base protegida o corpus, persistencia, APIs externas, commits o push. No se publica versión ni se inicia AIR-018.

### 2026-11-16 — buscador REST demo AIR-014 completado

- Perfil demo explícito, guardas antes de conexiones, catálogo GET y búsqueda POST sobre DEMO; corregida O-01. DTO HTTP separados, validación estricta, filtros, orden, páginas y causas parciales conservadas.
- Security restrictiva para dos rutas, sin sesión/login, CORS por allowlist, CSRF específico, Actuator cerrado, errores/cabeceras uniformes y admisión acotada. Comparación sigue denegada y AIR-015 sin iniciar.
- Wrapper offline `clean test`: 181 pruebas satisfactorias; HTTP real, chunked, bytes/concurrencia/ERROR GET y POST, proceso Java independiente y curl comprobados. POM/Wrapper/base generada intactos en esta tarea; sin APIs ni nuevas dependencias.
- Corregido el plazo total de lectura lenta mediante NIO demo; goteo, framing incompleto y desconexión probados. HTTP/1.1 local únicamente, TLS/HTTP2 pendientes; ADR-0005 registra decisión y consecuencias. `package` offline no generó JAR por plugin ausente en caché; no se descargó ni alteró configuración para eludirlo.

### 2026-11-13 — diseño de seguridad y pruebas AIR-016

- Definidos permisos exactos, denegación por defecto, gate anterior a CSRF, CORS, errores/cabeceras, cierre Actuator y cotas operativas. Perfil demo y guardas de persistencia diseñados con APIs reales Boot 4.1.2/Security 7/Jackson 3.
- Revisiones reales de security_qa y backend; documentada O-01 de capacidades como desviación funcional interna de prioridad baja. Matriz de 49 casos distingue pruebas históricas de controles HTTP/runtime pendientes.
- Solo documentación y comprobaciones estáticas, sin modificar backend/configuración ni repetir los tests de AIR-013. AIR-014/015 no iniciadas.

### 2026-11-13 — proveedor demo AIR-013

- Implementados modelos inmutables, puerto independiente y proveedor demo sin red, con identidad SHA-256 y corpus sintético versionado: 11 escenarios/16 registros, incluidas escalas, empates, monedas, desconocidos, caducidad y estados vacíos/fallidos/parciales.
- Añadidos 61 casos nuevos automatizados; el total con el contexto conservado es 62, todos satisfactorios. Compilación y tests mediante Wrapper offline; validador documental satisfactorio. Revisión real de backend/security_qa integrada.
- Preservados todos los archivos backend anteriores, POM, Wrapper, configuración y script validador. HTTP, frontend, comparación, wiring demo y persistencia siguen pendientes.

### 2026-11-12 — contratos AIR-012

- Ratificados contratos airwise-v1 de búsqueda, vuelos/precios, proveedores y tres operaciones HTTP, con 18 ejemplos sintéticos y 37 casos de contrato previstos. Separados itinerario completo, observación parcial y desconocidos; definidos identidad estable, estados/errores, cotas, grupos y desempates explicables.
- Revisiones reales de backend y security_qa conformes tras integrar precisiones. Validador y comprobación documental de ejemplos satisfactorios; backend y script validador preservados. Paquetes e incremento vertical propuestos; opciones de arranque demo documentadas sin configurar ni implementar componentes.

### 2026-11-12 — diagnóstico AIR-011

- Tras autorización expresa, ajustado únicamente el test de contexto para el demo sin persistencia y corregido el alcance del validador. Maven Wrapper test y validador completados con código 0; cinco regresiones aisladas satisfactorias. AIR-011 Hecha para ese alcance. Se conserva el diagnóstico fallido inicial y el snapshot histórico de los diez archivos.

- En el diagnóstico inicial se verificaron Temurin 21.0.12, Wrapper Maven 3.10.0, Boot 4.1.2 y árbol resuelto con Jupiter 6.0.3. Compilación correcta y test de contexto reproducido con error DataSource. En esa etapa AIR-011 quedó Bloqueada hasta autorizar su corrección.
- Registrado el límite del validador histórico ante salidas de target y contrastada por separado la integridad de los archivos originales.

### 2026-11-11 — organización y diseño

- AIR-010: acordado MVP demo ida/ida-vuelta y cuatro módulos, dominio, proveedores, comparación, HTTP conceptual, UX, seguridad y persistencia aplazada; ADR-0002/0003 aceptados con historial y ADR-0004 añadido. Análisis reales de backend, database, frontend y security_qa; sin implementación.
- Generalizadas las restricciones de AGENTS por autorización de tarea; backend y validador preservados, con propuesta documentada de evolución del chequeo histórico.

- Inventariado el backend Initializr existente sin cambiar sus archivos.
- Añadidas instrucciones raíz, configuración local de seis especialistas y cinco skills de procedimiento.
- Preparados roadmap, backlog con Definition of Done, mejoras propuestas y documentación inicial con ADR.
- Añadidas plantillas GitHub y proceso de contribución/seguridad, sin pipelines.
- Preparada validación local de metadatos, enlaces, planificación y preservación; resultados en [VALIDATION.md](../docs/developer-guide/VALIDATION.md).
- Aplicadas diez exclusiones preventivas para temporales, nombres de credenciales e informes privados antes del primer commit; actualizado el informe con AIR-006 y la revisión real de `security_qa`, conservando la evidencia histórica y el backend original.

No hay versión publicada. `backend:0.0.1-SNAPSHOT` es la coordenada generada, no una release de Airwise. Existe el primer commit local `439826c`; AIR-010 no hace commits, push, despliegues ni llamadas a las APIs de vuelos.
