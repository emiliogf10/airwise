# Registro de validación de Airwise

Evidencia organizativa y de diseño desde el **2026-11-11**, actualizada con AIR-011/012 y la implementación demo AIR-013 validada el **2026-11-13**. Cada apartado conserva su alcance, resultados y límites.

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
