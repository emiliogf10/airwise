# Registro de validación de Airwise

Evidencia desde el **2026-11-11**, actualizada con AIR-019 del **19/11/2026**. Cada apartado conserva su alcance, resultados y límites. AIR-019 solo verifica documentación e integridad; no repite ni acredita nuevas ejecuciones Maven, npm o navegador. Las pruebas de aplicación de AIR-014/015/018 conservan su fecha y alcance históricos.

## AIR-019 — permisos y presupuesto: investigación documental completada

La petición «Adelante con AIR-019» autoriza revisar condiciones públicas, no integrar ni consumir proveedores. Se consultaron páginas oficiales de Travelpayouts/Aviasales, SerpApi y condiciones de contenido de Google el 19/11/2026. La [evaluación](../api/PROVIDER_ASSESSMENT.md) conserva matriz, fecha, fuentes, inferencias y pendientes; [ADR-0007](../decisions/0007-real-provider-admission.md) mantiene solo DEMO habilitado. El plan de consumo0€ es una política versionada; sus controles durables/atómicos todavía necesitan implementación autorizada y pruebas con dobles.

### Evidencia ejecutada

| Comando / comprobación | Resultado y alcance |
|---|---|
| `py -3.14 scripts/validate_repository.py` | Código0; TOML de seis especialistas, cinco skills,274destinos locales y17tareas coherentes; seguridad acotada y directorios comprobados. No valida contratos externos ni ejecuta producto |
| `py -3.14 .local/air019/verify_integrity.py` | Código0;153SHA-256 idénticos al inicio:109backend,31frontend,5skills,7Codex,1script; sin altas/bajas en esos conjuntos. Dos artefactos AIR-018 (informe de navegador y CSS) también idénticos |
| Revisión dirigida de12documentos de AIR-019 | Sin valores de las credenciales de proveedor disponibles en entorno, URLs autenticadas ni marcadores de clave privada encontrados;18URLs de fuentes documentales oficiales y ningún enlace ejecutable de búsqueda/cuenta. Escaneo acotado, no auditoría general de secretos |
| `git diff --check` | Código0, sin errores; avisos informativos LF/CRLF de Git, sin cambiar configuración |
| Revisión independiente Security & QA | Sin bloqueos tras contrastar fuentes oficiales de las dos APIs y coherencia de permiso/caché/retención/cuota. Integrada precisión sobre conexión a Aviasales también en FAQ Data API; sin atribuir llamadas API ni pruebas de producto a la revisión |

El validador compara además el snapshot histórico de fase1: nueve archivos originales iguales, un test modificado y99archivos posteriores. Es historial autorizado por AIR-011/013/014/015; **no son cambios de AIR-019**. Para esta tarea el contraste relevante son los153hashes iniciales idénticos. Los cambios de AIR-018 que ya estaban pendientes de commit se preservaron; no se revierten ni se atribuyen a AIR-019.

### Coordinación, skills y límites

Delegación real y selectiva: `air019_travelpayouts` (backend) investigó Travelpayouts en solo lectura; `air019_qa` revisó matriz/ADR y fuentes; `air019_docs` editó exclusivamente README, ROADMAP y CHANGELOG. Orchestrator investigó SerpApi, redactó matriz/ADR/política, integró precisión/revisión, verificó y cerró backlog. No se presume ejecución automática de los seis roles ni disponibilidad de herramientas por tener archivos TOML.

Se descubrieron catálogo de sesión y disco: cinco skills locales; globales agent-browser, aws-emulator, find-skills, modern-web-guidance y security-audit; sistema y skills de artefactos/cloud del catálogo. `review-agent` hallada en disco no equivale a registrada en sesión. Aplicadas `task-planning`, `api-integration`, `documentation-sync` y `testing-and-review`; la de integración guía esta investigación y las otras cubren sus etapas. Security & QA consultó también orientación acotada de `security-audit`, sin ejecutar auditoría integral. No se instalaron skills ni herramientas, ni se modificó configuración global. No eran pertinentes migraciones, UI/navegador, artefactos o despliegue.

Límites: reglas completas actuales del programa Aviasales no recuperadas desde el panel privado; algunas páginas FAQ SerpApi no entregaron cuerpo, con alternativas oficiales accesibles registradas en la evaluación. Cuenta, plan efectivo, saldo, elegibilidad, licencias específicas de Airwise y derechos de terceros pendientes. Solo se leyeron páginas públicas; **cero endpoints API, cuenta, archivo, continuación o reintento ejecutados**, sin contactos externos ni gasto. No hay adaptadores, persistencia de consumo real, pruebas de proveedor ni cambios backend/frontend. No se repiten suites de aplicación para documentación. El cierre satisface la aceptación de registrar condiciones/dudas y plan persistente, y no declara las dudas resueltas.

Tras la revisión y comprobaciones, AIR-019 pasa En revisión → Hecha. El checkpoint temporal se retira al finalizar la verificación; la evaluación, ADR y este informe conservan evidencia permanente. No hay commit, push, publicación, versión nueva ni siguiente AIR iniciada.

## AIR-018 — frontend funcional completado

El adjunto autoriza expresamente implementar React/TypeScript/Vite en `frontend/` sobre catálogo, búsqueda y comparación demo, ampliando la preparación prevista inicialmente. El backend no forma parte del alcance de edición. AIR-018 queda **Hecha** tras automatizaciones, revisión asistida en navegador real y revisión independiente conforme. El coordinador conservó `.local/ACTIVE_TASK.md` durante el trabajo y elimina únicamente ese seguimiento al completar las verificaciones finales; las evidencias locales se conservan.

### Reanudación con navegador real — revisión ejecutada

El usuario solicitó expresamente repetir la revisión con `agent-browser`, ahora disponible en el catálogo e instalado por él. El coordinador aplica esa skill con una sesión dedicada, perfil temporal y dominio permitido `127.0.0.1`; no usa el perfil personal de Brave ni modifica configuración global. La herramienta observada es agent-browser 0.39.1 con Chrome 155.0.8059.39. Esto resuelve la limitación operativa de aquella revisión CUA; el histórico de rechazo y su autorización posterior se conserva abajo, sin atribuirle pruebas que no ejecutó.

La revisión final del coordinador observa React y CSS renderizados en navegador headless, con HTTP real del demo local y sin respuestas simuladas: tres endpoints, ida/vuelta, los cuatro criterios y estimaciones, selección máxima, páginas/orden/filtros, grupos de monedas, desconocidos y estados vacío/parcial/fallo. Matriz, viewports y límites exactos en [BROWSER_REVIEW](BROWSER_REVIEW.md). No son precios reales ni se presenta una suite E2E de navegador versionada.

Teclado observado: Tab/Enter en el enlace de salto lleva foco al título principal; Space activa orígenes/selección; Enter abre detalles; los errores llevan foco a su resumen. Se inspeccionaron capturas en 1440×1000, 768×1024, 720×900, 390×844 y 320×740, sin desbordamiento horizontal en las mediciones. Redimensionado CSS de texto al 200%/32 px en escritorio sin desbordamiento; no se acredita zoom nativo. La revisión independiente y el cierre del coordinador completan la DoD.

Se reprodujo ISSUE-001 en el antetítulo «Vuela con criterio»: la cascada de capas CSS aplicaba color #00685f sobre #173342, con contraste renderizado 1,97:1, pese al cálculo previo de la paleta. Frontend movió la regla correspondiente a la capa de componentes. Reverificación: color efectivo #abd7be y contraste 8,31:1; incidencia corregida. Ocho auditorías axe posteriores devolvieron cero violaciones y una regla incompleta que agrupa de 3 a 9 nodos decorativos según el estado (puntos y flechas con aria-hidden); se revisó manualmente su redundancia respecto al texto y nombres accesibles. No es una certificación WCAG completa. Informe, JSON y capturas permanecen en `.local/air018/browser-review/`; el informe permanente conserva resultados sin duplicar evidencia sensible.

Gates repetidos después del cambio CSS: 53 pruebas satisfactorias y una integración opt-in omitida; typecheck/build/lint con código 0 y sin advertencias. Build 25 módulos, JS 276,21 kB y CSS 13,48 kB. Los 109 hashes backend permanecen idénticos; no se ejecuta Maven de nuevo. Las integraciones locales de 12 y 6 peticiones de la etapa anterior conservan su alcance y se complementan con la revisión de navegador.

Tras la sincronización de cierre, `py -3.14 scripts/validate_repository.py` devolvió código 0: 248 destinos locales y 17 tareas comprobados. `git diff --check` no detectó errores; solo avisos informativos LF/CRLF. Se confirmó de nuevo la igualdad de los 109 hashes, la ausencia de daemons activos de agent-browser y de listeners en 8080/5173. El coordinador retiró únicamente el checkpoint de tarea al terminar; los informes y capturas locales se conservaron. No hubo commits, push ni inicio de otra fase.

Security & QA revisó independientemente el informe, los ocho JSON de axe, el JSON de Vitest, el diff CSS mínimo y capturas 768/320. Confirmó resultados y alcance, sin bloqueos reproducibles; no se le atribuyen ejecuciones del coordinador. El navegador se cerró por la CLI y se verificaron Java/Vite detenidos y puertos 8080/5173 sin listeners. El cierre no publica una versión, no hace commit/push ni inicia otra AIR. Los rechazos CUA y el estado En revisión anteriores se conservan abajo como etapas pasadas.

Documentation Agent aplicó `documentation-sync` y contrastó planificación, contrato HTTP, reglas de comparación, guía de ejecución y documentación de seguridad antes de preparar la guía frontend. Se corrigieron referencias arquitectónicas obsoletas que aún presentaban los endpoints y el arranque AIR-014/015 como futuros, conservando la evidencia histórica. No se atribuye a esta revisión documental ninguna ejecución Maven, npm, navegador o petición HTTP.

### Coordinación y procedimientos aplicados

El coordinador delegó realmente en los agentes de esta sesión `air018_frontend`, `air018_qa` y `air018_docs`, con propiedad separada de UI, pruebas y documentación. El principal mantuvo configuración, cliente HTTP, integración, backlog y seguimiento. Esta evidencia corresponde a esos procesos reales y no supone que todo agente configurado se invoque automáticamente.

Se descubrieron las cinco skills locales y las globales/sistema del catálogo. Se aplicaron `task-planning`, `testing-and-review` y `documentation-sync`, que concretan planificación, comprobaciones y documentación de Airwise. `api-integration` y `database-migrations` no se activaron: no hay integración de proveedor ni cambios de esquema autorizados. `modern-web-guidance` aportó guías de formularios, HTML, accesibilidad, CSS y responsive antes de implementar; no se instaló como skill nueva ni se cambió configuración global. La skill de computer-use se leyó para el intento de comprobación visual; el acceso fallido no acredita una prueba ejecutada. Las herramientas de artefactos, sitios alojados, AWS y creación/instalación de skills no eran pertinentes. La revisión de seguridad fue proporcional al frontend y sus dependencias, no una auditoría integral.

Comprobación incremental inicial tras esos cambios: `py -3.14 scripts/validate_repository.py`, código0, 215 destinos locales y17 tareas; `git diff --check -- README.md docs planning/ROADMAP.md planning/CHANGELOG.md`, código0. Se conserva como evidencia documental parcial anterior a las verificaciones finales siguientes.

### Etapa anterior — automatización verificada, navegador pendiente

El coordinador comunicó la ejecución final a las 18:53; Documentation contrastó además el JSON de Vitest, los scripts versionados, la configuración y las fuentes. No se atribuyen estas ejecuciones al agente documental.

| Comando / comprobación | Resultado y límite |
|---|---|
| `npm test -- --reporter=json --outputFile=../.local/air018/vitest-final.json` | Código 0, 54 casos: 53 satisfactorios, 0 fallidos y 1 omitido intencionadamente por ser integración local opt-in. JSON con `success:true`. Pruebas de cliente/componentes en jsdom con respuestas sintéticas |
| `npm run test:integration:ui` | Código 0, 1 prueba satisfactoria, 6 peticiones HTTP reales: catálogo, búsqueda y cuatro comparaciones, incluido subtotal. Monta App en jsdom con user-event y usa proxy/backend; no es navegador E2E |
| `npm run test:integration` | Código 0, 12 peticiones reales mediante cliente TypeScript y proxy, tres endpoints, cuatro criterios y casos de error/datos descritos abajo |
| `npm run typecheck` | Código 0 tras corregir el modo opt-in de la prueba; TypeScript estricto |
| `npm run build` | Código 0, 25 módulos; JS 276,21 kB/gzip 84,13 kB, CSS 13,48 kB/gzip 3,57 kB e index 0,67 kB. Build estático, sin acreditar despliegue |
| `npm run lint` | Código 0, sin advertencias; Oxlint |
| Integridad backend | 109 archivos fuente/configuración con hashes idénticos al snapshot previo AIR-018; sin cambios en POM, Wrapper o configuración versionada |
| Paleta y accesibilidad | 22 pares calculados estáticamente cumplieron los umbrales seleccionados; foco sobre fondo oscuro 8,31:1. Labels, controles nativos, mensajes, selección/foco y desconocidos cubiertos por pruebas de componentes; no auditoría completa de accesibilidad |

Vitest usa workers de tipo threads, máximo 2, dentro del sandbox gestionado. Los intentos iniciales mediante fork encontraron EPERM; la configuración final no necesita rebajar permisos ni controles. El test de integración UI se habilita solo mediante el wrapper `scripts/verify-ui.mjs` y el modo Vite `local-integration`, sin variables nuevas o globals Node dentro del componente/test. La suite normal no necesita servicios activos ni convierte fallos de conexión en fixtures.

En esta etapa AIR-018 quedó **En revisión**: implementación y automatización verificadas, con revisión visual/responsive y teclado real pendientes del entorno de navegador. Se conservó `.local/ACTIVE_TASK.md` con ese siguiente paso; no se inició otra AIR, no se publicó, no se hizo commit ni push. La instalación npm informó cero vulnerabilidades conocidas; eso no es una auditoría exhaustiva.

El coordinador detuvo los procesos Java y Vite de verificación y comprobó que no quedan listeners en 8080/5173. Volvió a contrastar los 109 hashes backend, todos idénticos. Tras la sincronización final ejecutó el validador del repositorio: código 0, 239 destinos locales y 17 tareas; `git diff --check` también devolvió código 0, solo con avisos informativos LF/CRLF. No se deja un servidor local en ejecución como supuesto de la entrega.

### Integración local nueva — 18/11/2026

El coordinador ejecutó `npm run test:integration` desde `frontend/` con backend en 127.0.0.1:8080 y Vite en 127.0.0.1:5173. El script versionado `frontend/scripts/verify-demo.mjs` carga `src/api/client.ts` mediante Vite SSR, usa fetch real y fuerza la procedencia exacta autorizada. Restringe sus destinos a las tres rutas demo locales. Resultado código 0: 12 peticiones HTTP a través del proxy, catálogo y búsqueda, cuatro criterios de comparación, subtotal con estimaciones, paginación de ocho ofertas, cero frente a precio desconocido, resultados vacíos/parciales y errores 409/503. No modifica backend ni usa APIs de proveedores.

Esta es evidencia de integración del cliente HTTP TypeScript con proxy/backend reales. No monta la interfaz React ni acredita E2E en navegador. La prueba opt-in de UI registrada arriba sí monta App en jsdom y realiza seis peticiones reales; tampoco observa layout de navegador, CSS renderizado o teclado físico. Los resultados unitarios, integración HTTP e integración UI se conservan como niveles distintos.

En aquella etapa los intentos de revisión en navegador no pudieron ejecutarse. Primero, el browser host no estaba disponible y la herramienta de acceso nativo rechazó Brave con «Computer Use was not approved to use Brave». El usuario autorizó después ese navegador; al reintentar, la herramienta no pudo determinar con confianza la URL de la ventana en Windows para aplicar su política y mantuvo el bloqueo técnico. No hubo acciones de interfaz ni capturas en esos intentos, ni se cambió configuración para eludir controles. AIR-018 quedó **En revisión** con seguimiento conservado. La reanudación agent-browser descrita arriba resolvió después esa verificación pendiente por un mecanismo autorizado y operativo.

Comprobación documental posterior a instalación/guías/manual/ADR: `py -3.14 scripts/validate_repository.py`, código0,233 destinos locales y17 tareas. `git diff --check -- README.md docs planning/ROADMAP.md planning/CHANGELOG.md frontend/README.md`, código0, con advertencia informativa de normalización LF/CRLF. El validador busca valores de proveedor disponibles en el entorno sin mostrarlos; no es un escáner general de secretos.

## Reverificación de AIR-015 y seguimiento recuperable — 17/11/2026

El nuevo adjunto vuelve a solicitar AIR-015 e incorpora descubrimiento de skills y seguimiento para tareas futuras. Se contrastó con la implementación ya completada y el checkout inicialmente limpio. Las revisiones reales de Backend, Security & QA y Documentation no identificaron carencias nuevas verificables frente al adjunto. AIR-015 conserva estado Hecha; no se reimplementó negocio ni se inició AIR-018.

Se añadieron reglas permanentes en AGENTS.md y un procedimiento breve en la guía de desarrollo: crear/leer `.local/ACTIVE_TASK.md`, registrar alcance, plan, responsables, hitos, comandos/resultados, pendientes y siguiente paso; conservarlo si queda trabajo y eliminarlo únicamente al completar las verificaciones. `.local/` ya estaba ignorado. El checkpoint se mantuvo durante esta tarea, sin credenciales ni payloads sensibles; no sustituye al backlog ni a evidencia persistente.

### Descubrimiento y selección de skills

Se consultaron el catálogo de esta sesión, las descripciones actuales y los directorios reales. Estar instalado en disco no se trató como registro ejecutable o disponibilidad en catálogo.

| Ámbito descubierto | Skills | Aplicación en esta petición |
|---|---|---|
| Local, `.agents/skills/` | task-planning, testing-and-review, documentation-sync, api-integration, database-migrations | Aplicadas las tres primeras: planificación, verificaciones normales del proyecto y sincronización documental |
| Global personal, `~/.agents/skills/` | aws-emulator, find-skills, modern-web-guidance, security-audit | Aplicada security-audit únicamente como guidance/source-first para inspección estática de Security & QA, sin auditoría integral ni ejecución objetivo bajo ese procedimiento |
| Sistema, `~/.codex/skills/.system/` | imagegen, openai-docs, review-agent, skill-creator, skill-installer | Ninguna aplicada. review-agent está instalado pero no figura en el catálogo de esta sesión; no se afirma su registro ni se usa |
| Plugins visibles en el catálogo | Documentos, PDF, presentaciones, hojas de cálculo, sitios, visualización y otras herramientas de producto | Descartados por no aportar un procedimiento pertinente para esta revisión Java/Markdown |

Las skills locales resultan más ajustadas para pruebas y documentación de Airwise. La global security-audit se limita a fronteras de confianza y evidencia estática, complementando la revisión sin repetir la ejecución local. No se instalan skills: no hay tarea de AWS, frontend/HTML, migraciones, integración real, artefactos visuales ni configuración ejecutable nueva de Codex. No se ejecutaron pruebas bajo el sandbox especializado de auditoría ni se les atribuye esa garantía.

### Evidencia nueva, separada del cierre original

| Comprobación del coordinador | Resultado actual |
|---|---|
| `py -3.14 .local/air015-recheck/run_maven.py full-tests clean test` | Wrapper existente offline, código0/BUILD SUCCESS,238 pruebas en24 clases, sin fallos/errores/omitidas; compilación Java21 incluida |
| `py -3.14 .local/air015-recheck/verify_runtime.py` | Java independiente y curl,24 comprobaciones correctas en loopback, puerto temporal60387; servidor detenido al terminar |
| Catálogo, búsqueda y comparación | Endpoint `POST /api/v1/demo/comparisons` operativo; cuatro criterios, selección2–4, grupos, unknown/vigencia, estimaciones, subtotales incompletos, permisos y errores comprobados |
| Reproducibilidad | Cuatro seleccionados con rangos1,2,3,3; invertir IDs no cambia la clasificación comercial; los ejemplos de precio100/110EUR y subtotal130/160EUR siguen siendo sintéticos |
| Revisiones delegadas | Backend: modelos/motor/reconstrucción/DTO; Security & QA: entrada/dinero/frontera HTTP, source-first; Documentation: contratos/ejemplos y procedimiento. Conformidades estáticas acotadas, sin afirmar auditoría exhaustiva |
| Validación documental y formato | `py -3.14 scripts/validate_repository.py`: código0,204 enlaces/17tareas; `git diff --check`: código0. Sin diff del backend |

Los runners reutilizan únicamente la distribución/caché existentes, retiran variables de proveedores/opciones externas del proceso hijo y guardan salida en `.local/air015-recheck/`. Los logs y capturas originales de AIR-015 permanecen intactos. No hubo cambios del backend, POM, Wrapper, dependencias, corpus, configuración global, commits o push; ninguna llamada a Travelpayouts, SerpApi u otros proveedores. Se mantienen los límites ya documentados de HTTP/1.1 demo, TLS/HTTP2 y empaquetado offline.

## AIR-015 — comparación demo implementada y verificada

Autorización específica para modelos, motor, endpoint de comparación y su integración con la búsqueda demo existente. Sin POM, dependencias nuevas, persistencia, frontend, autenticación o APIs externas. La evidencia AIR-014 de 181 pruebas se conserva como histórico; no acredita por sí sola las modificaciones actuales.

La implementación reconstruye de dos a cuatro IDs contra el conjunto completo de la consulta/versión, forma grupos compatibles y aplica primarias/desempates sin ganador global entre grupos. Las estimaciones terrestres son entradas explícitas de grupo y no modifican la tarifa de vuelo. Fuente contractual: [TRAVEL_COST_ENGINE](../architecture/TRAVEL_COST_ENGINE.md), [NORMALIZED_CONTRACTS](../architecture/NORMALIZED_CONTRACTS.md) y [API_DESIGN](../api/API_DESIGN.md).

Baseline AIR-014 ejecutada de nuevo por el coordinador: `AirportHttpTests,FlightSearchHttpTests,DemoSearchServiceTests`, 23 pruebas satisfactorias, log local ignorado `.local/air015/baseline.log`. Esto acreditó el punto de partida. El cierre usa la nueva suite integral y HTTP independiente, sin atribuir ejecuciones Maven a la revisión documental.

### Implementación presente y verificaciones incrementales

Valores en flightcomparison.domain: ComparisonTypes, ComparisonRequest, GroundTransportEstimate y ComparisonResult. ComparisonEngine realiza elegibilidad/ranking/subtotal; DemoComparisonService reconstituye el conjunto mediante `DemoSearchService.reconstruct`, comprueba IDs y moneda por oferta. ComparisonRequestReader reutiliza el lector canónico de Search; ComparisonViews/Controller/Configuration componen y proyectan el endpoint. Sin nuevas dependencias ni persistencia. Security añade únicamente el POST exacto y preflight de comparación, conserva CSRF específico, errores, CORS y límites existentes.

| Evidencia comunicada y contrastada en logs | Resultado |
|---|---|
| A–D: ComparisonEngineTests, `.local/air015/increment-abcd.log` | BUILD SUCCESS, 16 pruebas, cero fallos/errores/omitidas |
| E: seguridad/HTTP, `.local/air015/increment-e-security.log` | BUILD SUCCESS, 47 pruebas, cero fallos/errores/omitidas |
| Servicio/DTO, `.local/air015/backend-service-dto.log` | BUILD SUCCESS, 21 pruebas, cero fallos/errores/omitidas |
| Límites de evidencia/duración, `.local/air015/backend-evidence-boundaries.log` | Cuatro casos adicionales correctos; el motor contiene20 casos en la suite final |
| Java independiente y curl, `.local/air015/runtime-results.json` | 24 comprobaciones correctas; puerto temporal63505; proceso detenido |
| Suite integral limpia, `.local/air015/full-tests.log` | Wrapper offline `clean test`, BUILD SUCCESS/código0, 238 pruebas en24 clases JUnit; cero fallos/errores/omitidas;74 fuentes principales y25 de tests |

El arranque reutilizó la receta AIR-014 con perfil demo y `-Dspring.devtools.restart.enabled=false` antes del main. Se ejecutaron ambos JSON publicados en docs/api: FLIGHT_PRICE mantiene OPO100EUR antes de SCQ110EUR; KNOWN_SUBTOTAL con estimaciones explícitas de30+30EUR y10+10EUR da SCQ130EUR antes de OPO160EUR. Las respuestas se capturaron como verified-comparison-response.json y verified-subtotal-response.json en la carpeta local ignorada. [EXAMPLES](../api/EXAMPLES.md) incorpora la segunda, con UUID identificado como variable; no son tarifas de mercado.

La sesión HTTP final verificó las cuatro primarias, cuatro seleccionados con rangos1,2,3,3 y la invariancia al invertir IDs; selección entre páginas/filtros, monedas/grupos incompatibles, desconocidos/vigencia, estimaciones incompletas, CORS y errores400/409/422. También se verificó503 PROVIDERS_FAILED en Comparison con causa PROVIDER_TIMEOUT al reconstruir un escenario de fallo total. No se creó almacenamiento de búsquedas ni se consultaron fuentes reales.

La parcialidad operativa se representa sin ampliar el schema: warning global INVALID_PROVIDER_DATA/pathnull, mensaje propio con ProviderIssueCode distintos ordenados y deduplicación por causa antes del límite de warnings. Es incompletitud del conjunto, no invalidez de cada oferta. El corpus parcial solo aporta una oferta por consulta; la comparación válida de dos ofertas con ese warning se prueba con dobles de aplicación/contrato, incluidos60 resultados reconstituidos y colisión de warnings. No se inventa un ejemplo curl con las fixtures actuales.

### Alcance de las pruebas y conservación

Los57 casos nuevos se suman a los181 anteriores, sin eliminar tests. Las pruebas de dominio/aplicación cubren métricas monetarias/no monetarias, precio desconocido/cero, ámbito/evidencia/vigencia, bloques de empate, líder inequívoco con bloque inferior irresoluble, estimaciones/subtotal completo e incompleto y límite monetario derivado. Los tests de contrato/DTO verifican campos exactos, tipos, defaults/null y representaciones; MockMvc ejerce filtros reales y rutas/errores negativos. El arranque Java y curl acreditan servidor HTTP independiente, no solo contexto simulado. Las regresiones AIR-014 de catálogo/búsqueda, cuotas, lectura lenta y classloaders permanecen en la suite completa.

Backend implementó y revisó modelos/motor/servicio/DTO; Security & QA revisó contratos, entradas, desconocidos y frontera HTTP; Documentation sincronizó fuentes y ejemplos, sin ejecutar Maven. El coordinador integró y ejecutó la validación final. Ocho archivos protegidos, incluido el corpus demo-v1, contrastados sin diff; POM y sus15 dependencias, Wrapper, aplicación principal y configuración común conservados. Sin tablas, frontend, autenticación, APIs externas, instalación global, commits o push.

Empaquetado/producción, TLS/HTTP2, persistencia y bloqueo/captura de egress a nivelSO no quedan acreditados por esta tarea. Se conserva el fallo de package offline AIR-014 como histórico, sin presentarlo como empaquetado probado en AIR-015. Documentation ejecutó `py -3.14 scripts/validate_repository.py`: código0,203 enlaces locales y17 tareas correctos. `git diff --check` documental pasó; los18 bloques JSON y las dos solicitudes se analizaron, y la respuesta de subtotal publicada coincide exactamente con la captura final. El coordinador registra el cierre en BACKLOG y no inicia AIR-018.

## AIR-014 — catálogo y búsqueda REST demo completados

Autorización específica para código/configuración/test del buscador demo sobre la base existente; no POM, Wrapper, base de datos, comparación, frontend ni proveedores reales. Se preservaron los cambios documentales AIR-016 anteriores. No hubo commits, push, instalación ni configuración global.

### Entrega implementada

- O-01: outputKinds no vacío exige alguna capacidad SUPPORTED. Tres regresiones; no se amplía a una correspondencia bidireccional no ratificada.
- Incremento A: `config.demo` y `application-demo.properties`, Clock operativo/proveedor únicos; guardas anteriores a singletons, perfil explícito y sin persistencia/usuario generado.
- Incremento B: `airport.api.AirportController` y `flightsearch.application.DemoCatalog`, catálogo normalizado, versión y página estables.
- Incrementos C/D: `flightsearch.application.SearchView/DemoSearchService`, `flightsearch.api.SearchRequestReader/FlightSearchController/DemoSearchConfiguration`; DTO explícitos compartidos en `api.NormalizedViews/RequestValues`. Sin serializar directamente dominio/proveedor.
- `http`: Security exacta para GET catálogo/POST búsqueda, gate anterior a CSRF, contexto servidor, CORS, admisión, bytes/JSON estricto, cabeceras y escritor compartido. ERROR interno GET/POST reutiliza contexto y escribe directamente sin CSRF/sesión ni negocio nuevo.
- Monedas y grupos incompatibles separados, observaciones aparte, null/cero conservados, filtros AND/OR, órdenes estables antes de página y fallos/vacíos/parcialidad diferenciados. No comparación implementada.

Se usaron agentes ejecutables `backend` y `security_qa`, con propiedad de archivos separada. Backend entregó incremento A y su ejecución; Security implementó la frontera y pruebas. El coordinador integró DTO/casos de uso/controladores, ejecutó validaciones y revisó las correcciones de borde. Las pruebas actuales no se atribuyen a una revisión independiente completa si no existe su entrega.

### Comandos y resultados ejecutados

Windows PowerShell, Temurin21.0.12+8, Maven Wrapper existente3.3.4/Maven3.10.0; Boot4.1.2, Security7.1.2, Jackson3.1.7 y Tomcat11.0.27. Mismo POM de15 dependencias. Caché y distribución en `.local/air011`; runners locales ignorados en `.local/air014` retiran únicamente del proceso hijo las variables de proveedores, opciones Java/Maven/Spring y DEBUG/TRACE/LOGGING_LEVEL_*. No se muestran sus valores. TEMP/TMP dentro del workspace.

Todos los goals Maven siguientes usan `mvnw.cmd -o -B -ntp -Dmaven.repo.local=<workspace>/.local/air011/repository` desde backend:

| Verificación | Resultado / evidencia local ignorada |
|---|---|
| A: `-Dtest=ProviderCapabilitiesTests,ProviderContractTests,DemoStartupGuardTests,DemoRuntimeStartupTests test` | 15 pruebas correctas; `increment-a/selected-tests.log` |
| B: `-Dtest=AirportHttpTests test` | 4 pruebas correctas; `increment-b.log` |
| C: `-Dtest=FlightSearchHttpTests,DemoSearchServiceTests test` | 15 pruebas correctas en ese punto; `increment-c.log` |
| D: validación, Security, admisión y transporte | 75 pruebas correctas tras correcciones; `increment-d-fixed.log` |
| Primera integral `clean test` | BUILD SUCCESS, código0, 61 fuentes principales/16 fuentes de tests, 169 pruebas, cero fallos/errores/omitidas; `full-tests.log` y Surefire XML |
| `package` offline | Código1: maven-jar-plugin3.5.1 no está en caché. No se descargó nada; no hay JAR generado; `package.log` |
| `java @.local/air014/demo-java.args` | Aplicación principal independiente, perfil demo, loopback/puerto63317 observado. Classpath ya resuelto sin test-classes; receta en [DEMO_RUNTIME](DEMO_RUNTIME.md) |
| curl.exe contra ese proceso | Catálogo200/OPO, búsqueda200/COMPLETE/100.00EUR, fallo503/PROVIDERS_FAILED y comparación403; `runtime-results.json`, proceso detenido al terminar |

Los errores intermedios no se ocultaron: import Boot4 y orden de argumentos de fingerprint detectados por compilación; test TTL retenía permisos y test de tokens contaba comas. Se corrigieron causas, sin desactivar tests. Se añadieron regresiones para ausencia de CSRF globalmente deshabilitado, ERROR real de POST y cabeceras CORS en rechazo temprano por concurrencia.

La suite final cubre 62 casos anteriores y119 nuevos: contexto/guardas, O-01, catálogo, búsqueda/contrato, validación léxica/semántica, Security y cuotas/concurrencia. HTTP real prueba chunked con65536/65537bytes, cabeceras del conector, casos200/403/500, permisos liberados y fallbackERROR GET/POST. Cuarenta observaciones conservan conteo y se truncan explícitamente a20. Las pruebas no contactan APIs; los clientes de integración solo apuntan a127.0.0.1.

### Incidencia de lectura y cierre verificado

**Fallo corregido y AIR-014 Hecha:** el diagnóstico anterior6,06s se conserva en `.local/air014/runtime-results-before-deadline.json`. La corrección del transporte mantiene lectura en el mismo hilo, Selector local por tiempo restante y retirada del scope/entrada incompleta en finally. Selección corregida10/10 de plazo/CORS y otras35 pruebas de frontera satisfactorias; la desconexión necesitó una barrera de entrada real de test, sin sustituir el limiter productivo por un spy temprano. Backend revisó estáticamente aplicación y transporte y contrastó APIs/bytecode locales Tomcat11.0.27; sin defectos identificados.

Cierre ejecutado: `mvnw.cmd -o -B -ntp -Dmaven.repo.local=<cache> clean test`, log `.local/air014/full-tests-final.log`, BUILD SUCCESS, 64 fuentes principales/18 de tests, 181 pruebas sin fallos/errores/omitidas. Java independiente y curl repetidos tras la corrección: cuatro estados HTTP correctos y rechazo del goteo continuo en5.01s. Recursos/2permisos liberados, framing/trailers/bloqueo/desconexión probados. Solo HTTP/1.1 NIO demo; TLS/HTTP2 rechazados explícitamente hasta validación propia. Decisión en [ADR-0005](../decisions/0005-demo-body-read-deadline.md).

Empaquetado offline, arranque mediante spring-boot:run y distribución de producción no están acreditados. La falta de plugin JAR no invalida compilación/tests ni el arranque Java directo comprobado. Tampoco se acredita TLS/proxy/egress bloqueado por sistema operativo, DDoS distribuido, adaptación real o persistencia. La ausencia de transporte en negocio se comprueba estáticamente y con proveedor DEMO único; no equivale a captura exhaustiva de red.

POM, Wrapper, aplicación principal, configuración común y test original no presentan diff en AIR-014. Solo se modifica ProviderCapabilities entre fuentes anteriores; el resto productivo es nuevo. No se regeneró Initializr ni snapshot histórico. `py -3.14 scripts/validate_repository.py` y `git diff --check` satisfactorios tras sincronización; comprobación adicional de18 ejemplos JSON sin duplicadas, espacios finales y7 archivos protegidos sin diff. No es un escáner general de secretos. El backlog registra el cierre con evidencia y mantiene AIR-015 Pendiente.

## AIR-016 — seguridad y estrategia de pruebas diseñadas

Revisión documental y estática del **13/11/2026**, iniciada sobre HEAD 4d738f5582b8d17638d0ac00247b13558d2664a1 y checkout limpio. AIR-010/011/013 estaban Hechas. La autorización comprende diseño, pruebas acotadas si aportan evidencia y documentación; no autoriza cambiar producción ni configuración Spring, ni iniciar AIR-014/015.

### Entrega y criterios

| Criterio | Decisión y fuente revisable |
|---|---|
| Política HTTP compatible | [BASELINE](../security/BASELINE.md): solo tres método/ruta públicos, gate previo a CSRF, autorización final denyAll y dispatchers acotados |
| Demo sin PostgreSQL | [DEMO_RUNTIME](DEMO_RUNTIME.md): perfil explícito, exclusión DataSource/UserDetailsService solo en demo, guardas anteriores a conexiones |
| Superficies públicas/privadas | CORS/preflight exactos, resto 403, ERROR interno restringido, Actuator web/JMX cerrado |
| Pruebas verificables | [TESTING_STRATEGY](TESTING_STRATEGY.md): 49 casos, niveles unitario/contrato/web slice/integración y estados E13/P |
| Revisión del código | 32 fuentes AIR-013, recurso y seis clases de tests inspeccionados; O-01 y límites de sanitización/frescura documentados |
| Controles pendientes | Todo HTTP/perfil/limitador es diseño; no se declara runtime implementado ni ruta segura |
| Validaciones | Inspección JAR/metadata, verificador estático independiente, enlaces/metadatos/planificación/hash/diff satisfactorios |
| Planificación | AIR-016 revisada y cerrada por su DoD documental; AIR-014/015 permanecen Pendientes |

El diseño conserva los contratos de negocio AIR-012. API_DESIGN añade los estados de borde 403 ACCESS_DENIED, 404 RESOURCE_NOT_FOUND y 406 NOT_ACCEPTABLE sin cambiar ErrorResponseDTO. El preflight válido no devuelve body de negocio. Accept incompatible produce 406 JSON; un rechazo anterior mantiene su estado. No se promete JSON/UUID para errores de conector/TLS/proxy anteriores al proceso.

CSRF requiere atención al stack real: STATELESS no impide por sí solo creación de sesión por el repositorio de token. El gate de admisión comparte matchers REQUEST/método/ruta y rechaza antes de CsrfFilter; no abre todo ERROR ni desactiva Security/CSRF globalmente. La excepción ERROR exige destino /error, atributo del contenedor y marca interna de petición pública original. No recarga cuota/permisos/deadline y solo escribe error sanitizado.

Se fijan cotas de body 65536 bytes efectivos, profundidad12, 10000 tokens y límites de parser/dominio/admisión. Los valores de buckets/concurrencia/timeouts son diseño inicial, sin benchmark ni SLO. El perfil local se liga a loopback y CORS externo comienza vacío. Publicación y futuros proveedores requieren controles/configuración verificados y autorización propios.

### Agentes y compatibilidad real

Se invocaron realmente por collaboration.spawn_agent:

- /root/air016_security, agent_type security_qa: análisis principal HTTP/riesgos y revisión de AIR-013; solo lectura.
- /root/air016_backend, agent_type backend: contraste técnico con Boot 4.1.2, Security 7.1.2, Framework 7.0.10 y Jackson 3.1.7 locales; solo lectura.

No se invocó database: no había cambio de persistencia y backend comprobó las condiciones de JPA/Flyway/JDBC directamente. El Orchestrator integró y escribió exclusivamente Markdown y evidencia local ignorada. Se aplicaron testing-and-review, documentation-sync y security-audit en modo de revisión acotada, sin auditoría integral, pentest o investigación de CVE actuales.

Ambos especialistas releyeron los borradores. Se cerraron las precisiones de Accept/406, límite concreto de tokens y cobro único de admisión en ERROR; confirmaron que no quedaban contradicciones materiales en el diseño. Esto es revisión de un diseño, no test HTTP.

Inspecciones de firmas/bytecode con jar tf y javap -classpath <jar local> -p/-c/-v, sin ejecutar clases del backend:

- PathPatternRequestMatcher, RequestMatchers y HttpSecurity/addFilterBefore existen. Orden estándar HeaderWriterFilter → CorsFilter → CsrfFilter; OncePerRequestFilter omite ERROR condicionado al atributo servlet real.
- WebMvcTest/AutoConfigureMockMvc de Boot 4 pertenecen a org.springframework.boot.webmvc.test.autoconfigure; filtros activos por defecto. No usar imports Boot 3.
- Jackson 3 ofrece constraints/coerciones/duplicadas/trailing tokens. FAIL_ON_NUMBERS_FOR_ENUMS pertenece a EnumFeature; annotations sigue com.fasterxml.jackson.annotation.
- JPA requiere DataSource en las condiciones revisadas. Flyway admite DataSource, JdbcConnectionDetails o URL; JNDI/XA son independientes. La exclusión del test AIR-011 no es garantía universal ante configuración externa.
- Una cadena propia no hace retroceder UserDetailsServiceAutoConfiguration; el diseño demo evita usuario/password generados mediante su exclusión acotada.
- Metadata Boot 4 usa spring.web.error.*; server.error.include-* está deprecado con nivel error. Security incluye cinco dispatchers.

Evidencia estática de backend: cdf900 (Security), 01fc56 (Boot/MockMvc), d5a2ad y verificaciones posteriores (JPA/Flyway), ce0688/7c6b50/9b221f (Jackson), 6ac920 (CORS/handlers), 70e155 (filtros/ERROR) y 76d041 (usuario generado), todos con comprobaciones pertinentes de código 0. Clases exploratorias inexistentes se descartaron; no forman parte de la propuesta. Root leyó metadata local mediante zipfile/json, códigos 0 en fc833f y 8a0756, sin cargar esas clases.

### Hallazgos y límites de la revisión

No se confirmó vulnerabilidad explotable en la superficie existente. No hay controladores de negocio, adaptadores externos o configuración HTTP propia. No se ha auditado toda dependencia ni una instalación/despliegue.

O-01: desviación contractual interna de prioridad baja en [ProviderCapabilities](../../backend/src/main/java/com/airwise/backend/flightsearch/application/ProviderCapabilities.java), constructor líneas 13–17. Todos los estados UNVERIFIED con outputKinds=[OFFER] superan sus condiciones según fuente; el contrato exige vacío si nada está verificado. No se ejecutó ese constructor durante AIR-016, no existe entrada remota a ese camino y el descriptor DEMO actual es correcto. Propuesta mínima y test de regresión en BASELINE/T-07; corrección solo en tarea posterior autorizada. No se refactorizó producción para resolverla.

ProviderIssue/DataIssue permiten texto sintácticamente válido: el demo usa catálogo, pero un adaptador futuro no puede copiar mensajes remotos. DemoCorpus deriva correctamente frescura en su reloj fijo; el nuevo borde debe conservarlo. La prueba de bytecode es una regresión de transportes conocidos, no captura exhaustiva de tráfico.

### Comprobaciones ejecutadas

Se eligieron comprobaciones estáticas porque solo cambia documentación. No se ejecutó Maven, Spring ni código objetivo, ni se repitieron los 62 tests. La ejecución anterior se mantiene como evidencia histórica; los XML se leyeron sin mostrar stdout sensible.

| Comando / inspección | Resultado |
|---|---|
| py -3.14 .local/air016/check_repository.py | Código 0; 167 destinos locales, seis TOML, cinco skills, 17 tareas y 18 JSON; verificación independiente sin importar/ejecutar código del repositorio |
| Lectura estática de metadata/JAR | Firmas/claves/condiciones anteriores verificadas; sin instalación/red |
| Revisión de los 18 JSON de EXAMPLES | Sintaxis y ausencia de claves duplicadas comprobadas |
| git diff --check | Código 0; avisos LF/CRLF existentes, sin errores de espacios |
| git status --short y candidatos | Solo documentación; dos documentos nuevos, sin build/evidencia privada |
| SHA-256 | Los 130 archivos backend actuales —fuentes/configuración/target— idénticos al inicio; validador versionado, HEAD e índice intactos |

El verificador local revisa destinos de enlaces contra candidatos Git, sintaxis TOML de seis especialistas, metadatos de cinco skills, 17 tareas/dependencias sin ciclos, 49 filas de casos y 12 riesgos, estados AIR, POM Java21/Boot4.1.2/15 dependencias, formato/espacios/marcadores sensibles y preservación completa. Lee datos fuente/reportes; no importa ni ejecuta scripts/validate_repository.py ni los tests del backend. No es un escáner general de secretos o auditoría de dependencias.

El primer intento del verificador se lanzó antes de mover AIR-016 a En revisión y se detuvo por esa precondición del arnés. Tras registrar el estado correspondiente terminó con código 0 (027e0d); no fue un fallo del producto. El resultado final y la lista de documentos quedan en .local/air016/static-checks.json, y los hashes iniciales en before.json, ambos ignorados.

Contraste completo del cierre: 95 candidatos Git, 11 documentos creados/actualizados (dos nuevos), 49 casos diseñados y 12 riesgos; verificador 642d3f y diff 2d9f7e con código 0. El archivo Java/configuración/artefacto backend no ha cambiado y los 62 casos de Surefire siguen siendo históricos de AIR-013.

### Cierre y trabajo pendiente

Los controles descritos permanecen por implementar y ejecutar en una tarea autorizada. El entorno actual no acredita HTTP, perfil demo, límites de transporte, seguridad efectiva, Actuator cerrado o ausencia universal de conexiones ante configuración arbitraria. Las futuras pruebas runtime requieren aislamiento y recursos/red acotados; su validación no se sustituye por inspección estática. PostgreSQL/JPA/Flyway siguen pendientes de integración.

Se conservan todas las evidencias históricas siguientes, incluidos resultados y estados propios de cada fase. No se modificaron Java, tests, recursos demo, POM, Wrapper, propiedades, configuración global, .codex ni .agents. Sin dependencias instaladas, APIs de vuelos, commits o push.

AIR-016 satisface sus ocho criterios como tarea de diseño/revisión tras En curso → En revisión → Hecha. O-01 queda documentada para corrección autorizada y no bloquea este alcance. La próxima tarea recomendada es AIR-014 con autorización para el perfil/cadena/borde HTTP y sus pruebas; AIR-015 mantiene su dependencia de esa implementación. Ninguna de ellas se ha iniciado.

## AIR-013 — proveedor demo implementado y probado

Trabajo autorizado iniciado el 12/11/2026 y validado el **13/11/2026**, sobre HEAD da8a046bac4987396cdb022f438edb8655ebfe63 y checkout inicialmente limpio. AIR-012 estaba Hecha. Se implementó el primer componente de negocio del backend: modelos puros, puerto de proveedor y adaptador demo con recursos sintéticos.

### Implementación y alcance de archivos

- airport.domain: Airport.
- flightsearch.domain: FlightSearchRequest, Travellers, Money, FlightTime, FlightSegment, Connection, FlightJourney, FlightItinerary, Airline, PriceInformation, PriceScope, AdditionalCost, BaggageAllowance, BaggageInformation, FlightOffer, PriceObservation, Seller, DataIssue, FlightTypes y DomainChecks.
- flightsearch.application: FlightProvider, ProviderSearchRequest, ProviderExecutionContext, DatasetDescriptor, CurrencyDefinition, ProviderCapabilities, ProviderResult, ProviderIssue y StableIdentity.
- flightprovider.demo: [DemoFlightProvider](../../backend/src/main/java/com/airwise/backend/flightprovider/demo/DemoFlightProvider.java) y DemoCorpus.
- Recurso cerrado [demo-v1.properties](../../backend/src/main/resources/demo/demo-v1.properties): cuatro referencias de aeropuerto, 11 escenarios por origen y 16 registros, 15 ofertas y una observación.
- Cinco clases nuevas de tests: DemoFlightProviderTests, DemoCorpusValidationTests, DemoIsolationTests, ProviderContractTests y DomainContractTests.

Son 32 archivos Java principales nuevos, un recurso y cinco archivos Java de pruebas. No se modificó ningún archivo backend preexistente, incluidos test de contexto, clase de arranque, POM, propiedades y Wrapper. No hay anotaciones Spring/JPA en dominio, entidades, repositorios, migraciones, controladores ni motor de comparación. Las listas y resultados publicados son inmutables; los horarios y cantidades desconocidos conservan null/motivo.

[PROVIDERS.md](../architecture/PROVIDERS.md) publica el mapa completo de fechas/escenarios, admisión y límites. Los ejemplos base conservan precios, horarios y vectores de identidad de AIR-012. Los nombres de presentación del catálogo son referencias mínimas redactadas manualmente; no se importó un dataset externo ni se verificó un catálogo comercial/licencia completa. Códigos/zonas del corpus se contrastaron con los ejemplos y Java IANA. Todos los vuelos, precios y aerolíneas D1/D2 son sintéticos.

El proveedor usa un origen por solicitud, manteniendo la consulta canónica de hasta tres orígenes. La agregación entre orígenes queda para AIR-014. No modifica la semántica de COMPLETE, EMPTY, PARTIAL, FAILED o UNSUPPORTED. Una consulta admitida sin escenario tiene EMPTY completo; un fallo conserva su causa. Una oferta ida/vuelta con precio OUTBOUND_ONLY mantiene ambos trayectos y ámbito parcial, y la observación de ruta permanece separada.

El reloj simulado es 2027-01-01T00:00:00Z y el calendario es 2027, independientes del sistema. El descriptor de solicitud debe coincidir íntegramente con el del servidor. supports devuelve false para descriptor inexacto/null; search rechaza ese dato con IllegalArgumentException antes de ejecutar. Un deadline operativo agotado devuelve FAILED/TIMEOUT, sin rejuvenecer datos ni alterar identidad. El componente se construye directamente en Java; no está registrado como bean Spring ni existe perfil de ejecución demo nuevo.

### Agentes realmente invocados

Se utilizaron collaboration.spawn_agent con agent_type backend y security_qa, sin sustituciones de rol ni cambios de permisos.

- /root/air013_backend implementó exclusivamente los archivos nuevos de producción y el recurso. Su turno sufrió una interrupción por límite de uso; se reanudó tras la petición del usuario y entregó el componente. No ejecutó Maven ni escribió tests/documentación.
- /root/air013_security_review realizó revisión de solo lectura de contratos, código y pruebas. Señaló la coherencia bidireccional DEMO/proveedor; se corrigió y se añadieron negativos de ofertas y observaciones. Revisión final conforme, incluida cobertura de variedad/escala/monedas/empates, chunk_id=539229 con código 0.

El Orchestrator escribió las cinco clases de pruebas, revisó e integró, ejecutó compilación/tests/validador y actualizó la documentación. Se aplicaron testing-and-review y documentation-sync. No hubo escritores simultáneos sobre un mismo archivo.

### Compilación y ejecución real

Las ejecuciones usaron el Wrapper existente, Temurin 21.0.12+8 y Maven 3.10.0. Solo se reutilizaron distribución y artefactos ya presentes en .local/air011; ningún artefacto fue instalado o descargado. MAVEN_USER_HOME y repositorio local apuntaron a esa caché del workspace, TEMP/TMP a .local/air013/tmp. Las variables de proveedores y opciones externas Java/Maven/Spring se retiraron únicamente del proceso hijo; no se cambió configuración global.

Desde backend, con -Dmaven.repo.local apuntando a la ruta absoluta de .local/air011/repository:

| Comando | Resultado ejecutado |
|---|---|
| mvnw.cmd --version | Código 0; distribución existente Maven 3.10.0 / Java 21.0.12 |
| mvnw.cmd -o -B -ntp clean compile | Código 0, BUILD SUCCESS, 33 fuentes principales compiladas con release 21 |
| mvnw.cmd -o -B -ntp test | Código 0, BUILD SUCCESS, 62 tests, cero fallos, errores u omitidos |
| py -3.14 scripts/validate_repository.py | Código 0, RESULTADO CORRECTO; 137 destinos locales, seis especialistas TOML, cinco skills y 17 tareas; fuentes nuevas incluidas y target excluido |
| git diff --check y revisión de archivos nuevos | Código 0; sin errores de espacios ni marcadores de conflicto |

El primer ciclo ejecutó 59 tests; tras incorporar las tres comprobaciones del escenario de variedad/vacío sin coincidencias y el negativo de hueco DST, la ejecución final comprende 62. No se omitieron ni desactivaron pruebas para obtener éxito.

| Clase Surefire | Tests / fallos / errores / omitidos |
|---|---|
| BackendApplicationTests, conservado | 1 / 0 / 0 / 0 |
| DemoFlightProviderTests | 18 / 0 / 0 / 0 |
| DemoCorpusValidationTests | 6 / 0 / 0 / 0 |
| DemoIsolationTests | 2 / 0 / 0 / 0 |
| ProviderContractTests | 6 / 0 / 0 / 0 |
| DomainContractTests | 29 / 0 / 0 / 0 |

Evidencia local ignorada: .local/air013/version.log, clean-compile.log, test.log, before.json y scope-and-tests.json. Los reportes Surefire están en backend/target/surefire-reports. Los logs compartibles sanitizan el password generado por Spring Security. Continúa el aviso de autoenganche de Mockito/Byte Buddy en Java 21; no bloquea tests ni se cambió infraestructura para ocultarlo. El modo -o evita consultas de resolución; las referencias a índices de repositorios del log corresponden a metadatos ya cacheados.

Trazas de esta sesión: Wrapper/version 40a8f6, compilación ffd9a5/5e7c6e, ejecución final de tests 6bfb26/bd8ea9, lectura Surefire bddc2c, validador 67e968, diff 1d8805 y alcance SHA-256 d4d774, todos con código final 0. El informe de alcance se actualiza al cierre con los 38 archivos nuevos y 11 documentos existentes modificados. AIR-013 ha satisfecho la DoD tras pasar por En curso y En revisión.

### Cobertura de los criterios solicitados

| Criterios | Evidencia ejecutada |
|---|---|
| Ida, ida/vuelta, varios orígenes | Resultados OPO/SCQ, vuelta vinculada y duración sin estancia |
| Segmentos/conexiones | OPO–BCN–MAD ordenado, conexión 3600 s y total 15000 s; cronología inválida rechazada |
| Moneda, ámbito y equipaje | EUR/USD separados, tarifa parcial, cero frente a null, estados de equipaje conocidos/desconocidos |
| Observación separada | Registro ob_ sin itinerario ficticio, fuera de offers |
| Determinismo/identidad | Vectores q_/of_/g_ ratificados, consultas normalizadas equivalentes, correlationId/deadline/Clock operativo sin efecto funcional |
| Caducidad y reloj | EXPIRED y UNKNOWN en escenario fijo, descriptor forjado rechazado, deadline real agotado |
| Vacío/fallo/parcial | EMPTY 1/1, FAILED 0/1, PARTIAL 1/2 con causa; orígenes con éxito/fallo independientes |
| Admisión/negativos | Calendario/catálogo, consulta inválida, versión/capacidades/reloj alterados, estados y unidades incoherentes |
| Corpus íntegro | Falta/malformación, UTF-8, tamaño, propiedades/records duplicados, rutas/fechas/moneda y límite20 de ofertas/observaciones rechazados |
| Ausencia de llamadas externas | Código sin transporte/credenciales/procesos, inspección automatizada del bytecode nuevo y ejecución offline sin variables de proveedor |

Los 37 casos CT de AIR-012 son una matriz para varios incrementos. AIR-013 ejecuta los casos de su componente y negativos relevantes; no acredita los de HTTP, filtros/páginas ni rankings de comparación todavía inexistentes. La revisión de bytecode y el diseño cerrado del componente no constituyen una auditoría general de todas las dependencias o una captura exhaustiva de red.

### Preservación, documentación y límites

El contraste SHA-256 al inicio confirma intactos todos los archivos backend preexistentes versionados y scripts/validate_repository.py; índice y HEAD sin cambios. El validador conserva el snapshot histórico Initializr: nueve originales idénticos, test autorizado de AIR-011 modificado, 38 archivos nuevos y ninguno ausente. Los artefactos target y evidencias locales siguen ignorados. Se enumeraron 93 candidatos Git, sin incluir build ni evidencia privada.

Se sincronizaron README, DOMAIN_MODEL, MODULES, OVERVIEW, PROVIDERS, guía/inventario técnico, roadmap, backlog, changelog y este registro. Las precisiones reflejan implementación; no cambian los contratos de AIR-012 ni los ADR aceptados.

No se iniciaron PostgreSQL, otros servicios, proveedores reales ni APIs; no se instalaron dependencias ni se hicieron commits/push. PostgreSQL, JPA y Flyway siguen pendientes de integración. Tampoco se comprobaron arranque de ejecución demo, seguridad HTTP, controladores, frontend o comparación. Viajes/precios no acreditan disponibilidad o reserva real.

AIR-013 pasa por En curso y En revisión, y se marca Hecha únicamente tras los resultados satisfactorios y la revisión independiente. AIR-014, AIR-015 y AIR-016 permanecen Pendientes. El siguiente paso recomendado es preparar AIR-016 antes de exponer las operaciones de AIR-014; no se ha iniciado.

## AIR-012 — contratos normalizados ratificados

Validación documental del **12/11/2026**, con AIR-010 y AIR-011 Hechas. El alcance autorizado comprende especificar contratos y ejemplos; no implementa proveedor, controladores ni clases Java.

### Entrega y decisiones verificadas

| Requisito | Fuente revisable |
|---|---|
| Búsqueda ida/ida-vuelta, varios orígenes, canonicalización, identidad, filtros, orden y páginas | [Contrato normativo](../architecture/NORMALIZED_CONTRACTS.md) y [DTO HTTP](../api/API_DESIGN.md) |
| Campos/tipos/nulls de vuelos, dinero, precios, cobertura, equipaje y tiempos | Contrato normativo; DOMAIN_MODEL conserva vocabulario y fundamento |
| Puerto, capacidades y estados completos/vacíos/parciales/fallidos | Contrato normativo y [PROVIDERS](../architecture/PROVIDERS.md) |
| Tres endpoints, errores sanitizados, límites y correlationId | API_DESIGN y [18 ejemplos JSON](../api/EXAMPLES.md) |
| Paquetes acíclicos e incremento vertical pequeño | [MODULES](../architecture/MODULES.md), sin directorios/clases nuevos |
| Test frente a ejecución demo y persistencia futura | [Guía de desarrollo](README.md), sin cambiar configuración |
| Casos positivos/negativos sin proveedores | Matriz CT-01 a CT-37 del contrato normativo, pendiente de ejecutar como tests de producto |

Se conservan los cuatro límites de AIR-010 y ausencia de persistencia de negocio. Una FlightOffer exige itinerario completo para la modalidad, aunque su importe/cobertura sean desconocidos o parciales; PriceObservation carece de itinerario completo y queda fuera de selección/comparación. La evidencia no depende del conocimiento del importe. No se inventan regreso, equipaje, moneda, TTL ni disponibilidad.

La identidad demo usa SHA-256 sobre consulta canónica/versión y recordKey inmutable; filtros/página y reloj operativo no la alteran. Hay grupos separados por evidencia, moneda, ámbito y condiciones. Los subtotales suman únicamente vuelo y componentes requeridos de usuario, sin FX, y soportan el máximo de entradas válidas. segmentId es único en todo el itinerario. La normalización diferencia atributos opcionales recuperables de estructura contradictoria que exige aislar el registro.

El ranking fija bloques de igual primaria. Un desempate comercial desconocido en un bloque empatado produce TIE_UNRESOLVED y rango común; si es el líder, no hay recomendación. Orden conocido-primero/ID es presentación. Un empate completo conocido conserva ganadores empatados. La respuesta incluye PriceGroup para explicar sus condiciones.

### Delegación real y revisión independiente

Se invocaron los agentes personalizados mediante la herramienta de colaboración con agent_type backend y security_qa. Ambos recibieron análisis de solo lectura, límites y entrega concretos; el Orchestrator fue el único escritor de los documentos.

- backend: /root/air012_backend_contracts. Revisó valores, invariantes, límites, puertos, normalización e identidad. Detectó cinco precisiones; después de integrarlas confirmó que no quedaban contradicciones materiales. Relectura final ejecutada con código 0, chunk_id=825d28.
- security_qa: /root/air012_security_contracts. Revisó entradas, errores, sanitización, parciales y casos negativos. Confirmó resolución de identidad de segmentos, desempates, agregación de incidencias y metadata del comparador. Relectura final con código 0, chunk_id=53dfe9.

Las entregas y los resultados de herramientas de esos agentes forman parte de la sesión; no se simularon desde el coordinador. No se invocó database: la separación dominio/DTO/persistencia ya estaba decidida y se ratificó sin tablas ni JPA. Se aplicaron task-planning, api-integration para semántica/procedencia sin investigación remota, y documentation-sync.

### Comprobaciones ejecutadas

| Comando o comprobación local | Resultado |
|---|---|
| py -3.14 .local/air012/check_examples.py | Código 0: 18 bloques JSON, 1531 aserciones documentales |
| py -3.14 scripts/validate_repository.py | Código 0, RESULTADO CORRECTO; 128 destinos locales, seis especialistas TOML, cinco skills y 17 tareas |
| git diff --check | Código 0; avisos existentes LF/CRLF, sin errores de espacios |
| Enumeración git ls-files --cached --others --exclude-standard | 55 candidatos; revisión del alcance documental y ausencia de archivos locales/build entre candidatos |
| SHA-256 contra la instantánea al inicio de AIR-012 | Los 19 archivos backend actuales, incluido target, idénticos; script validador e índice Git idénticos; HEAD sin cambios |

El comprobador local de ejemplos usa solo Python estándar y está en .local, ignorado. No es una herramienta de producto versionada. Lee los bloques de EXAMPLES, rechaza claves duplicadas y contrasta campos, consulta normalizada, SHA-256, IDs de ofertas/observación/grupo, fechas/offsets del corpus invernal, duraciones, estados/conteos, nulabilidad, frescura, selección y sumas de 130/160 EUR. Comprueba también la solicitud inválida y el límite derivado de Money. No acredita cobertura general de zonas IANA ni ejecución de algoritmos Java.

Resultados de la revisión final de esta sesión: validador chunk_id=ed938d, ejemplos chunk_id=db2670, diff chunk_id=a27c8b y alcance/candidatos chunk_id=51a15e, todos con código 0. El contraste de inicio identifica 19 documentos creados/actualizados en AIR-012 y ninguna eliminación; la lista queda en el informe local ignorado .local/air012/scope-review.json.

Se conserva el registro histórico de los diez archivos Initializr: nueve siguen idénticos y el test difiere por el cambio autorizado y validado en AIR-011. AIR-012 no altera ese test ni ningún otro archivo backend. Las modificaciones previas de AIR-010/011 siguen en el working tree y se distinguen mediante la instantánea de inicio.

### Límites y cierre

No se ejecutó Maven de nuevo: esta tarea solo cambia Markdown. Se conserva el resultado de AIR-011 sin atribuirlo a una ejecución nueva. Los 37 casos CT son requisitos de pruebas futuras; no son tests de negocio ejecutados. No existen todavía endpoints, adaptador demo, corpus implementado ni perfil de ejecución demo.

PostgreSQL, JPA y Flyway continúan pendientes de integración. Quedan para tareas autorizadas materializar fixtures/versiones/escenarios, verificar metadata y licencia del catálogo, configurar y comprobar arranque demo y seguridad HTTP. Travelpayouts/SerpApi siguen deshabilitados y UNVERIFIED; permisos, contratos actuales y cuotas se verificarán antes de cualquier integración autorizada. No se realizaron consultas externas, instalaciones, cambios globales, commits ni push. AIR-013 y AIR-016 permanecen Pendientes.

Tras la revisión independiente y las verificaciones documentales satisfactorias, AIR-012 satisface su DoD documental; el backlog registra la transición En curso → En revisión → Hecha.

## AIR-011 — corrección autorizada y cierre del contexto demo

El usuario autorizó el cambio de un único archivo backend, `BackendApplicationTests.java`, y la corrección de alcance del validador. Las siguientes comprobaciones actualizan el diagnóstico inicial conservado más abajo.

### Cambio y cobertura de la prueba

`@SpringBootTest` excluye únicamente `org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration` mediante una propiedad local al test. La clase corresponde al artefacto resuelto de Spring Boot 4.1.2 y el test recompiló con Java 21.

El método `demoContextLoadsWithoutPersistence` comprueba tres condiciones reales del contexto inicializado.

- `ConfigurableApplicationContext.isActive()` devuelve verdadero.
- Existe el bean `BackendApplication`, que representa la configuración principal cargada.
- La colección de beans DataSource está vacía.

`mvnw.cmd test` finalizó con **BUILD SUCCESS y código 0**. Surefire registra **un test, cero fallos, cero errores y cero omitidos**, con el nombre de método nuevo. El log confirma `Started BackendApplicationTests` y actividad de la autoconfiguración Security. Sus credenciales generadas se sanitizaron en la evidencia compartible.

La exclusión resultó suficiente. El contexto completo de prueba inició con las dependencias JPA/Flyway presentes y sin DataSource. Esto acredita el alcance demo sin persistencia. PostgreSQL, JPA, Flyway, transacciones y migraciones permanecen pendientes de integración. El arranque con la configuración de producción y su política HTTP también requieren verificaciones propias.

No se creó un perfil Spring de producción denominado demo. El alcance demo se establece en las propiedades de esta prueba y el perfil Spring registrado sigue siendo el predeterminado. Los nueve archivos originales restantes, incluidos POM, configuración, Wrapper y código de producción, conservan su contenido.

### Validador y conservación del histórico

[scripts/validate_repository.py](../../scripts/validate_repository.py) excluye exactamente `backend/target/` en la enumeración, el escaneo de fuentes y el contraste histórico. Carpetas llamadas target bajo `src/` se siguen analizando. El script comprueba POM, existencia de archivos backend esenciales, metadatos, enlaces y posibles secretos.

El snapshot original registra los diez archivos de fase 1 y conserva su SHA-256. La comparación actual informa nueve idénticos y un test modificado, con su ruta. Es información histórica revisable junto al alcance autorizado y sus pruebas. El validador permite evolución de contenido y detecta la ausencia de sus archivos esenciales actuales.

`py -3.14 scripts/validate_repository.py` terminó con **código 0 y RESULTADO CORRECTO**, con las salidas de compilación presentes.

Se ejecutaron cinco comprobaciones aisladas del validador mediante fixtures temporales.

| Caso | Resultado |
|---|---|
| Salida de build y carpeta fuente homónima | backend/target excluido y src/main/resources/target incluido |
| Test evolucionado frente al histórico | Diferencia informada y validación satisfactoria |
| Marcador sensible simulado en artefacto | Excluido del escaneo de fuentes |
| Marcador sensible simulado en fuente | Detectado, incluso dentro de una carpeta fuente llamada target |
| Clase principal ausente | Error de archivo esencial detectado |

La batería temporal requirió un reintento autorizado por permisos del sandbox y un ajuste del arnés para comparar rutas Windows. La ejecución final pasó los cinco casos con código 0. Las fixtures quedaron retiradas de su carpeta local de pruebas.

### Comandos y evidencia de esta corrección

| Comando / comprobación | Resultado ejecutado |
|---|---|
| `.\\mvnw.cmd test -B -ntp "-Dmaven.repo.local=<cache local>"` desde backend | Código 0, Surefire 1/0/0/0 para ejecutados/fallos/errores/omitidos |
| `py -3.14 scripts/validate_repository.py` | Código 0, seis TOML, cinco skills, 17 tareas y contraste histórico informativo |
| Batería aislada del validador | Código 0, cinco casos |
| Revisión de security_qa | Visto bueno del ajuste y de los archivos esenciales, lectura código 0 |
| Enlaces, diff y alcance final | 107 destinos locales válidos e incluibles; diff código 0 y cero espacios finales. Ocho archivos modificados en esta continuación, incluido un único archivo fuente backend |

Evidencia local ignorada `.local/air011/test-attempt3.log` y reporte Surefire del método nuevo. Trazas de herramientas `e96ecd`/`84b8be` para Maven, `a0525d` para el validador, `3d2f4f` para la batería, `8c67de` para el contraste del reporte y `b73b9c` para la relectura del especialista. Solo se invocó de nuevo a security_qa para esta revisión acotada. El Orchestrator realizó las ediciones y ejecuciones.

Los avisos HTTP 401 de prefijos de repositorios adicionales de Flyway siguen apareciendo durante Maven. El goal solicitado completó con éxito. AIR-012 conserva su estado Pendiente.

Los ocho archivos de esta continuación son el test, el validador, TECHNOLOGY_INVENTORY, este registro, la guía de desarrollo, README raíz, BACKLOG y CHANGELOG. El snapshot, HEAD y el índice mantienen sus hashes. Se conservaron los cambios documentales previos de AIR-010 y del diagnóstico inicial.

Los criterios de cierre quedan satisfechos dentro del alcance autorizado. AIR-011 pasa de Bloqueada a En curso, En revisión y Hecha tras los resultados ejecutados y el visto bueno de la revisión. Permanecen pendientes la integración PostgreSQL/JPA/Flyway y el arranque con la configuración principal.

## AIR-011 — diagnóstico inicial del 12/11/2026 (histórico)

Este apartado conserva el estado bloqueado y las propuestas previas a la autorización. P1 y la corrección de alcance de P3 se resolvieron en la continuación documentada anteriormente.

**Estado Bloqueada** por el error reproducido de carga del contexto y la autorización pendiente para modificar el test. El checkout inicial contenía los cambios documentales de AIR-010, seis documentos nuevos y tres archivos previos en `backend/target/`. Se capturaron hashes antes de ejecutar Maven.

### Agentes y entorno de ejecución

Se invocaron realmente `backend`, `database` y `security_qa` mediante `collaboration.spawn_agent`. Los hilos `/root/air011_backend_review`, `/root/air011_database_review` y `/root/air011_security_review` entregaron informes de solo lectura. El Orchestrator ejecutó Maven e integró la documentación.

Backend revisó POM/Wrapper con lecturas de código 0 (`a9ee01`, `5e89d3`, `182f87`). Database inspeccionó fuentes y condiciones de los JARs con código 0 (`eb8020`, `8fe218`, `560662`, `25b12e`, `a08624`, `e7fee4`, `eca1cb`). Security QA revisó configuración, variables por nombre y reporte Surefire con código 0 (`008d8d`, `e0bb95`, `53626d`, `d347d4`, `3267be`). Son identificadores de herramientas de esta conversación.

Entorno efectivo Windows 11 amd64, Temurin 21.0.12+8-LTS de Eclipse Adoptium y Maven Wrapper 3.3.4 que ejecutó Maven 3.10.0. El Maven instalado 3.9.16 se inspeccionó mediante `mvn -version`. Los comandos de proyecto utilizaron el Wrapper.

Se eligieron cachés de proceso dentro del workspace para reproducir la ejecución. `MAVEN_USER_HOME=.local/air011/maven-home`, repositorio mediante `-Dmaven.repo.local=<ruta absoluta a .local/air011/repository>` y `TEMP`/`TMP` en `.local/air011/tmp`. Los procesos hijos excluyeron las dos variables de proveedor y opciones externas de Java/Maven/Spring. Se conservó JAVA_HOME y el transporte del entorno. No había settings de usuario ni `maven.config` o `jvm.config` de proyecto. Los logs locales se sanitizaron.

### Comandos y resultados ejecutados

Desde `backend/`, con `-B -ntp` y el repositorio local indicado para los goals Maven.

| Comando | Código y resultado | Evidencia local ignorada |
|---|---|---|
| `java -version` y `mvn -version` | Entorno identificado, Temurin 21.0.12 y Maven instalado 3.9.16 | Traza de herramienta `41afd7` |
| `.\mvnw.cmd --version` | Primer intento código 1, error DownloadFile/recepción dentro del sandbox. Reintento autorizado fuera del sandbox código 0, Maven 3.10.0 | `.local/air011/--version.log`, traza `e31f90` y `8e5a87` |
| `.\mvnw.cmd clean compile` | Código 0, BUILD SUCCESS, una fuente principal compilada con release 21 | `.local/air011/clean-compile.log` |
| `.\mvnw.cmd test` | Repetición verificada código 1, BUILD FAILURE. Un test, cero fallos de aserción, un error y cero omitidos | `.local/air011/test-attempt2.log` y `backend/target/surefire-reports/` |
| `.\mvnw.cmd help:effective-pom -Dverbose "-Doutput=../.local/air011/effective-pom.xml"` | Código 0, Help Plugin 3.5.2 | `.local/air011/effective-pom.xml` y `help-effective-pom-attempt2.log` |
| `.\mvnw.cmd dependency:tree -Dverbose -DoutputType=text "-DoutputFile=../.local/air011/dependency-tree.txt"` | Código 0, Dependency Plugin 3.10.0 | `.local/air011/dependency-tree.txt` y `dependency-tree.log` |

La captura inicial del test sufrió un UnicodeEncodeError al imprimir su salida. Se corrigió la codificación del capturador y se repitió el test original, confirmando el mismo fallo de contexto y el código Maven 1. El primer intento de exportar el POM efectivo sufrió un argumento dividido por PowerShell. La repetición con el argumento entrecomillado terminó con código 0. Esas incidencias de instrumentación quedan diferenciadas del fallo reproducido de la aplicación.

POM efectivo y árbol confirmaron parent `spring-boot-starter-parent:4.1.2` y gestión `spring-boot-dependencies:4.1.2`. Versiones relevantes en el [inventario actualizado](TECHNOLOGY_INVENTORY.md). Jupiter/Platform resueltos son 6.0.3. Compiler 3.15.0 y Surefire 3.5.6 compilaron/ejecutaron con Java 21.

Hubo advertencias HTTP 401 para `/.meta/prefixes.txt` de los repositorios adicionales flyway-community-db-support y MavenFeed. Su origen se localizó en el POM resuelto flyway-parent 12.4.0. Las operaciones de compilación, árbol y POM efectivo completaron correctamente. Las claves de proveedores permanecieron fuera del entorno hijo.

### Primer fallo relevante y alcance comprobado

Surefire XML confirma `BackendApplicationTests.contextLoads`, con `tests=1`, `errors=1`, `failures=0` y `skipped=0`. La fuente principal y el test compilaron. Boot 4.1.2 intentó cargar el contexto bajo el perfil default.

```text
entityManagerFactory -> flyway -> dataSource (Hikari)
Failed to configure a DataSource: 'url' attribute is not specified
and no embedded datasource could be configured.
Reason: Failed to determine a suitable driver class
```

PostgreSQL JDBC 42.7.14 está en el árbol resuelto. La ausencia de URL impide configurar el DataSource automático. El fallo observado se produce antes de una conexión PostgreSQL. JPA/Flyway se activan desde las dependencias existentes pese a que las fuentes todavía carecen de entidades, repositorios y migraciones. Las propiedades principales contienen únicamente `spring.application.name=backend`.

| Categoría | Estado |
|---|---|
| Declarado | Java 21, parent Boot 4.1.2 y quince dependencias |
| Resuelto | Parent/BOM, quince dependencias directas, transitivas y plugins relevantes comprobados |
| Compilado | Fuente principal y fuente del test, release 21 |
| Ejecutado | Wrapper/Maven, Jupiter y tentativa de carga de contexto |
| Probado | Un test disponible ejecutado y reproducido con error de configuración |
| Pendiente | Contexto satisfactorio, arranque completo, acceso HTTP/Actuator, PostgreSQL, JPA/Flyway y seguridad efectiva |

### Correcciones propuestas por prioridad

**P1** requiere autorización sobre un único archivo, [BackendApplicationTests.java](../../backend/src/test/java/com/airwise/backend/BackendApplicationTests.java). Para el MVP demo sin persistencia, proponer un contexto de prueba explícitamente delimitado mediante esta anotación y un nombre de prueba que describa ese alcance.

```java
@SpringBootTest(properties =
    "spring.autoconfigure.exclude=" +
    "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration")
```

Database confirmó la clase exacta y las condiciones en los JARs de Boot 4.1.2. Hibernate requiere un DataSource y Flyway condiciona su activación a fuentes de conexión. Con el entorno utilizado, esta exclusión limitada al test es una corrección candidata mínima. Su eficacia queda pendiente de aplicar el cambio autorizado y volver a ejecutar las pruebas.

La anotación conserva las autoconfiguraciones de Security, Actuator y Validation. Una prueba satisfactoria acreditaría el contexto demo sin infraestructura JDBC. La configuración principal y las integraciones de persistencia seguirían pendientes de sus verificaciones. Se conserva en este registro el resultado original.

**P2** permite validar el contexto completo cuando se autorice persistencia real. Requeriría PostgreSQL aislado, configuración externa y perfil de integración. Los archivos candidatos serían el test existente y un recurso específico bajo `backend/src/test/resources/`. Testcontainers añadiría cambios al POM y requisitos de contenedores que deben justificarse entonces. H2 está ausente y su comportamiento no acreditaría PostgreSQL/Flyway.

**P3** afecta al validador histórico. Su comparación SHA-256 incluye artefactos de `target/`, mientras el snapshot de fase 1 contiene diez archivos originales. Conviene distinguir fuentes originales de salidas generadas y conservar el snapshot histórico. También imprime una línea de preservación incondicional aunque el resultado global falle. La evolución propuesta deberá corregir esa ambigüedad y gestionar expresamente futuros cambios autorizados de fuentes. La integridad de los originales se contrasta también por separado.

Como mejora informativa futura del Wrapper, puede proponerse `distributionSha256Sum` con checksum oficial verificado. La distribución HTTPS se ejecutó correctamente. Su incorporación necesita autorización sobre el archivo generado.

### Estrategia de pruebas y reproducción

| Nivel | Ejecución inmediata o condición futura |
|---|---|
| Contexto generado | Ejecutado en AIR-011 y fallido, con reporte Surefire conservado |
| Dominio | Tras crear objetos autorizados, tests unitarios de dinero, viajes, horarios y desconocidos |
| Casos de uso/servicios | Dobles de puertos, reloj controlado y fixtures, con errores y parcialidad |
| Controladores/validación | Tras existir controladores, slices HTTP con filtros Security y errores públicos |
| Contratos | Fixtures sintéticas para normalización, capacidades y aislamiento de adaptadores |
| Persistencia | PostgreSQL aislado cuando existan entidades, consultas o migraciones aprobadas |
| Local y futura CI | Wrapper con JDK 21, goals sin omitir tests y reportes. Comandos verificados antes de automatizarlos |

MVP demo utiliza catálogo/fixtures y estado transitorio. La presencia de JPA/Flyway/PostgreSQL responde al generado y a capacidades futuras. El diagnóstico actual justifica una decisión explícita del alcance del test. La ejecución local se describe en la [guía del desarrollador](README.md).

### Verificación documental e integridad

| Comprobación ejecutada | Resultado |
|---|---|
| `py -3.14 scripts/validate_repository.py` | Código 1, un error por conjunto backend distinto del snapshot. Hay diez originales y nueve salidas actuales en target. El mensaje global de error prevalece sobre la línea de preservación incondicional |
| Funciones documentales del validador, invocadas mediante runpy | Código 0 para TOML, skills, enlaces, backlog y seguridad. Se declara este alcance complementario expresamente |
| Destinos contra candidatos Git | 106 enlaces internos válidos e incluibles, 53 candidatos, seis especialistas, cinco skills y 17 tareas |
| Integridad de fuentes | Diez originales idénticos al inicio y al snapshot histórico, incluidos POM, Wrapper, propiedades, Java y test |
| `git diff --check` y revisión de documentos nuevos | Código 0 y ausencia de espacios finales. Avisos LF/CRLF de la configuración existente |
| Alcance de Git | Índice y HEAD idénticos al inicio. Los cambios documentales previos de AIR-010 se conservan |

Las comprobaciones se ejecutaron con código 0 salvo el validador completo indicado. Evidencia de herramientas `ae3632` para ese fallo histórico, `970c48` para comprobación documental complementaria y `c3fba8` para diff. Los nueve archivos de target son salidas ignoradas del build. El script y el snapshot mantienen su contenido original.

En AIR-011 se actualizaron TECHNOLOGY_INVENTORY, este registro, BACKLOG, la guía de desarrollo, README raíz y CHANGELOG. AIR-011 queda Bloqueada hasta autorizar una corrección, ejecutarla y comprobar sus resultados. AIR-012 conserva estado Pendiente.

## AIR-010: diseño posterior al primer commit

Punto de partida: primer commit local `439826c`, checkout limpio. AIR-010 autoriza exclusivamente análisis/documentación; no ejecuta AIR-011, código Java, BD, migraciones, React, proveedores o instalaciones. Los apartados posteriores conservan las evidencias previas al commit y de la fase organizativa.

### Especialistas realmente invocados

`collaboration.spawn_agent` seleccionó cada `agent_type` real, sin cambios de modelo, esfuerzo o permisos. Los cuatro hilos devolvieron informes finales de solo lectura; el Orchestrator integró y escribió la documentación.

| Rol / hilo | Aportación y evidencia de lecturas con código 0 |
|---|---|
| backend: `/root/air010_backend_design` | Módulos, dominio, proveedores, HTTP y comparación; `chunk_id=6286ac`, `89469e` |
| database: `/root/air010_database_design` | Persistencia inicial innecesaria, recursos versionados y esquema posterior condicional; `921c8d`, `6485e4` |
| frontend: `/root/air010_frontend_design` | Dos vistas, estados/accesibilidad y contrato para selección/paginación; `de2d20`, `760e90` |
| security_qa: `/root/air010_security_design` | Fronteras, límites, errores, dinero y matriz de pruebas; `d8ed9e`, `49d429` |

Se integró el acuerdo común: sin cuentas/BD de negocio, comparación reconstruida en servidor y demo versionado. Se corrigió la ambigüedad “confirmado por vendedor” separando anuncio y confirmación de reserva. Los IDs de herramienta corresponden a esta conversación, no a artefactos públicos. DevOps y Documentation no fueron necesarios.

### Criterios de aceptación de AIR-010

| Criterio | Documento y decisión de diseño revisable |
|---|---|
| 1. MVP delimitado | [OVERVIEW](../architecture/OVERVIEW.md): MVP-01 a MVP-09 y exclusiones; un adulto/turista, demo sin cuentas |
| 2. Módulos proporcionados | [MODULES](../architecture/MODULES.md): cuatro límites, interfaces, grafo acíclico y dependencias prohibidas |
| 3. Dominio esencial | [DOMAIN_MODEL](../architecture/DOMAIN_MODEL.md): ida/vuelta, segmentos, tiempos/zonas, dinero y procedencia |
| 4. Proveedores separados | [PROVIDERS](../architecture/PROVIDERS.md): capacidades, observaciones/ofertas y admisión futura sin gasto |
| 5. Comparación explicable | [TRAVEL_COST_ENGINE](../architecture/TRAVEL_COST_ENGINE.md): grupos, desempates, casos límite y subtotales |
| 6. Persistencia justificada | MODULES y [ADR-0004](../decisions/0004-demo-mvp-and-persistence.md): cero tablas/migraciones iniciales; casos futuros condicionados |
| 7. HTTP coherente | [API_DESIGN](../api/API_DESIGN.md): tres rutas demo, parámetros, errores, filtros/páginas y selección sin confiar precios del cliente |
| 8. Riesgos y pruebas | [BASELINE](../security/BASELINE.md): amenazas y matriz de pruebas futuras; ningún test de producto declarado pasado |
| 9. Planificación | [Roadmap](../../planning/ROADMAP.md) y [backlog](../../planning/BACKLOG.md) sincronizados; AIR-011 y siguientes no iniciadas |
| 10. Coherencia/evidencia | ADR con transición explícita, autorización por tarea en AGENTS y verificación local del cierre |

### Verificaciones del cierre

Revisión final acotada de `security_qa` completada: diez criterios cubiertos, sin bloqueos materiales tras explicitar KNOWN_SUBTOTAL como cuarta primaria y la parcialidad por unidades requeridas no soportadas. Lecturas con código 0 (`chunk_id=b593ae`, `e341f9`, `7ca3aa`, `d66a2f`) y relectura de correcciones con código 0 (`8b92db`). Integrado también el ajuste editorial para impedir cambio silencioso de primaria si faltan estimaciones.

| Verificación ejecutada | Resultado |
|---|---|
| `py -3.14 scripts/validate_repository.py` | Código 0, RESULTADO CORRECTO: seis TOML, cinco skills, 101 enlaces locales, 17 tareas y diez archivos backend preservados |
| Enlaces contra candidatos Git y grafo de tareas | 101 destinos locales válidos e incluibles; grafo completo de dependencias sin ciclos; 53 candidatos tras añadir seis documentos, sin archivos sensibles nuevos |
| `git diff --check` y revisión adicional | Código 0; sin errores de espacios. Git avisa de futura normalización LF/CRLF según configuración existente, que no se modifica. Archivos nuevos revisados aparte, sin espacios finales |
| Preservación y alcance | Los diez SHA-256 backend iguales al inicio y snapshot histórico; backend y script sin diff. Validador idéntico por SHA-256; `.codex/`, `.agents/` y `.gitignore` sin cambios |
| Git | HEAD `439826c` e índice idénticos al inicio; sin contenido preparado para commit, commits nuevos o push |

La transición de AIR-010 fue Pendiente → En curso → En revisión → Hecha tras integración y verificaciones. El resultado es aceptación de diseño, no compilación o disponibilidad funcional. Ninguna tarea AIR-011 a AIR-020 cambia de Pendiente.

El validador se conserva: sus invariantes de fase 1 siguen siendo correctos mientras AIR-010 no cambia backend. La [guía](README.md) propone separar chequeo histórico y validación de proyecto para una futura tarea autorizada, sin borrar/regenerar el snapshot ni debilitar controles. No se ha implementado ese cambio.

**Pendiente de ejecución de producto:** resolver dependencias, compilar, probar contexto JPA/Flyway y arrancar Spring (AIR-011); contratos finales/fixtures (AIR-012/013), seguridad ejecutable, frontend y permisos reales. La aceptación de estos diseños no acredita ninguna de esas capacidades.

## Estado validado antes del primer commit (histórico)

Actualización del **2026-11-11**, posterior al cierre inicial de AIR-001 a AIR-005. Se conservan más abajo sus comandos, resultados y limitaciones históricos.

| Comprobación | Resultado verificado |
|---|---|
| AIR-006 | Completada y marcada Hecha en el backlog; catálogo efectivo y prueba acotada de delegación real |
| Backend Agent | Invocación mediante `collaboration.spawn_agent` con `agent_type="backend"`; hilo `/root/air006_backend_readonly` completado, informe propio y dos lecturas con código 0. Mecanismo y trazabilidad en [CODEX_SETUP.md](CODEX_SETUP.md) |
| Otros cinco especialistas | `frontend`, `database`, `security_qa`, `devops` y `documentation` detectados en el selector con descripciones coincidentes con sus TOML. AIR-006 no los invocó todos; `security_qa` se ejecutó después para la revisión previa al commit |
| Skills | Las cinco disponibles en el catálogo de la sesión; sus archivos y metadatos comprobados por el validador local |
| Revisión de seguridad previa al commit | Completada por `security_qa` en `/root/precommit_security_readonly`: 47 candidatos, sin secretos, credenciales ni datos privados detectados; PoC limitadas a resúmenes agregados. Escaneo y exclusiones con código 0 (`chunk_id=c47667`, `f67906`, `1030ea`, `8828d5`). Revisión acotada, sin garantía absoluta de ausencia de secretos |
| Correcciones preventivas | Añadidas diez exclusiones al [.gitignore raíz](../../.gitignore), conservando todas las anteriores; contrastadas mediante rutas sintéticas sin crear archivos |
| Validación del repositorio | `py -3.14 scripts/validate_repository.py`: código 0 y RESULTADO CORRECTO; seis especialistas TOML, cinco skills, enlaces internos y 17 tareas comprobados; diez archivos backend preservados mediante SHA-256 |
| Construcción del backend | Compilación, tests y arranque todavía NO EJECUTADOS. Continúan pendientes en AIR-011; no se modifica POM, Java, Spring, wrapper ni `backend/.gitignore` |

Las nuevas reglas `*.tmp`, `*.temp`, `*.bak`, `*~`, `*.swp`, `credentials.json`, `secrets.json`, `id_rsa` e `id_ed25519` se aplican a esos nombres o sufijos en cualquier nivel. `reports/private/` solo protege esa ruta desde la raíz; no excluye todos los informes ni carpetas homónimas dentro de otros directorios. Los documentos privados deben guardarse en `.local/` o en esa ubicación protegida. `.env.example` sigue siendo una excepción deliberada y debe contener solo ejemplos sin secretos.

Se comprobaron exclusiones mediante `git check-ignore -z --no-index --verbose --non-matching --stdin`, con rutas sintéticas ignorables y controles de archivos legítimos. Los 47 candidatos originales siguen visibles, incluidos los nueve archivos versionables de Initializr, configuración compartida de Codex y cinco skills. `backend/HELP.md` conserva su exclusión generada; no existe un repositorio Git anidado en backend. Las excepciones de `target` bajo fuentes de Initializr se mantienen expresamente.

`git status --short --untracked-files=all` confirma los candidatos sin seguimiento y el índice vacío; `git diff --check` termina sin errores. Como ese diff no cubre archivos sin seguimiento, se revisaron también los cambios contra el contenido capturado al inicio y los espacios finales de los documentos modificados. Los enlaces internos se comprobaron además contra el conjunto candidato, para evitar destinos presentes solo en archivos ignorados. No se consultaron URLs externas.

No se ejecutaron `git add`, commits ni push, instalaciones, cambios globales, desarrollo de producto o llamadas a proveedores. Los cambios son preventivos y documentales; no cierran ninguna tarea de producto.

## Evidencia histórica inicial de AIR-001 a AIR-005

La tabla y los párrafos de este apartado describen el cierre inicial, anterior a AIR-006 y a la revisión previa al commit; su estado de agentes y sus recuentos de enlaces son históricos.

| Comprobación | Estado y evidencia |
|---|---|
| Inspección del backend | Ejecutada: POM, estructura, clase de arranque, test, configuración y wrapper; inventario en TECHNOLOGY_INVENTORY.md |
| Conservación del backend | Ejecutada: los mismos 10 archivos, hashes SHA-256 idénticos y sin construcción dentro de backend/ |
| Herramientas | Java Temurin 21.0.12, Maven 3.9.16, Python 3.14.7; wrapper declara otra distribución, no ejecutada |
| Extensión / binario | Metadatos locales de openai.chatgpt 26.51106.51428; codex-cli 0.167.0-alpha.4.1 |
| Documentación oficial Codex | Consultada; fuentes y mecanismo en CODEX_SETUP.md |
| Configuración de arranque Codex | Comando nativo estricto terminó con código 0; no prueba registro de cada especialista |
| Instrucciones y skills visibles | Renderizador local terminó con código 0 tras autorización de acceso temporal; AGENTS raíz y catálogo con nombre/descripción de las cinco skills presentes; rutas expuestas mediante alias del catálogo |
| Agentes ejecutables | Seis archivos configurados; selección/invocación de esos agentes no comprobada. Coordinador es la sesión principal |
| Validador oficial de skills | Intentado: falta PyYAML; no instalado. Validación local del subconjunto de metadatos preparada |
| Metadatos, enlaces y backlog | Ejecutado `scripts/validate_repository.py`: RESULTADO CORRECTO; 6 especialistas TOML, 5 skills, 59 enlaces locales y 17 tareas AIR |
| Secretos y directorios vacíos | Sin coincidencias de los 2 valores de proveedores disponibles en el entorno ni marcadores de claves privadas; sin nuevos directorios vacíos. Se preservaron los 3 vacíos de backend/. No es auditoría general |
| Compilación/tests/arranque Spring | NO EJECUTADOS: fuera del alcance organizativo, backend conservado; AIR-011 |
| Frontend, BD, Docker y CI | No generados; no se presentan comandos o servicios como disponibles |
| APIs de vuelos | Cero llamadas durante esta fase; sin consumo de créditos |
| Publicación y Git | Sin commits, push ni publicación realizados por esta tarea; rama inicial sin commits al inspeccionar |

Los temporales de evidencia se guardan en `.local/`, excluida de Git. El renderizador nativo falló inicialmente por acceso denegado a temporales fuera del workspace; se repitió únicamente tras autorización específica. No se modificó configuración global ni se ejecutó una tarea adicional de inferencia.

La validación local se ejecutó sobre archivos aún no añadidos a Git. `git diff --check` terminó sin errores, pero no verifica por sí solo archivos nuevos sin seguimiento; sus rutas, metadatos, enlaces y coherencia se comprobaron con el script. No se crearon commits. En aquel cierre, AIR-001 a AIR-005 estaban cerradas con evidencia y AIR-006 y las tareas de producto permanecían pendientes. El estado posterior de AIR-006 se recoge en la actualización anterior; las tareas de producto siguen pendientes.

## Reproducir las comprobaciones documentales

```powershell
py -3.14 scripts/validate_repository.py
git diff --check
git status --short
```

Requiere Python 3.11+ para TOML. El script no usa red, no inicia Spring y no instala paquetes. Si la instantánea local del backend no existe en otro clon, indica que no puede comprobar la preservación histórica; no la inventa. No se debe regenerar para ocultar cambios. Este snapshot pertenece al cierre de la fase 1; una fase futura que autorice cambios de backend deberá revisar ese criterio explícitamente.

## Árbol de entrega

```text
airwise/
├── AGENTS.md
├── README.md
├── CONTRIBUTING.md
├── SECURITY.md
├── .gitignore
├── .codex/
│   ├── config.toml
│   └── agents/
│       ├── backend.toml
│       ├── frontend.toml
│       ├── database.toml
│       ├── security-qa.toml
│       ├── devops.toml
│       └── documentation.toml
├── .agents/skills/
│   ├── task-planning/SKILL.md
│   ├── api-integration/SKILL.md
│   ├── database-migrations/SKILL.md
│   ├── testing-and-review/SKILL.md
│   └── documentation-sync/SKILL.md
├── .github/
│   ├── ISSUE_TEMPLATE/bug_report.md
│   ├── ISSUE_TEMPLATE/feature_request.md
│   └── pull_request_template.md
├── backend/                         # Los 10 archivos iniciales, sin modificar
├── planning/
│   ├── ROADMAP.md
│   ├── BACKLOG.md
│   ├── IMPROVEMENTS.md
│   └── CHANGELOG.md
├── docs/
│   ├── user-manual/README.md
│   ├── developer-guide/
│   │   ├── README.md
│   │   ├── TECHNOLOGY_INVENTORY.md
│   │   ├── CODEX_SETUP.md
│   │   └── VALIDATION.md
│   ├── architecture/
│   │   ├── OVERVIEW.md
│   │   └── PROVIDER_EVIDENCE.md
│   ├── api/PROVIDER_POLICY.md
│   ├── security/BASELINE.md
│   └── decisions/
│       ├── README.md
│       ├── 0001-preserve-generated-backend.md
│       ├── 0002-modular-monolith.md
│       └── 0003-provider-boundaries.md
└── scripts/validate_repository.py
```

`.git/` y `.local/` no se incluyen en el árbol de archivos de entrega. No se crearon directorios vacíos ni un proyecto de aplicación nuevo. Se conservaron los tres directorios vacíos del backend generado (`static`, `templates` y `db/migration`) por la instrucción de mantener esa base intacta.

## AIR-014 — comprobación adicional de classloaders

Tras la primera suite de180 pruebas, el proceso Java independiente encontró un fallo que SpringBootTest no reproducía: goteo400 tras8,09s. DevTools había cargado BodyReadDeadline en AppClassLoader y RestartClassLoader; Protocol y filtro no compartían el ThreadLocal. La traza `.local/air014/deadline-classloaders.log` confirma la causa. Se añadió una guarda previa al bind por identidad real del handler/loader y una regresión con URLClassLoader aislado, sin puentes globales, reflexión propia, cambio del main o retirada de la dependencia.

Receta corregida: `-Dspring.devtools.restart.enabled=false` en la JVM, antes del main; application-demo.properties desactiva además defaults de desarrollo. Java independiente/curl repetido con DevTools presente: cuatro estados esperados y goteo400 en5,01s. Arranque negativo con restart habilitado: mensaje fijo de guarda y Spring abortado sin puerto abierto, `.local/air014/restart-guard-results.json`. DevTools RestartLauncher devolvió código0 a pesar del fallo; no se confunde con un contexto arrancado. Todos los procesos de verificación se detuvieron. La suite final se repitió desde limpio después de esta guarda, con el total indicado en el cierre AIR-014.
