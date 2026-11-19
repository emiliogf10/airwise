# Inventario tecnológico observado

Inventario inicial del **11/11/2026**, con histórico AIR-011/013 preservado y actualización AIR-014 del **16/11/2026**: perfil demo, catálogo/búsqueda REST, DTO explícitos, Security/JSON/admisión y 181 pruebas satisfactorias. No se han modificado POM, Wrapper, aplicación principal, configuración común ni test original en AIR-014. Se conservan las 15 dependencias y las versiones siguientes. Empaquetado offline no disponible por ausencia de maven-jar-plugin 3.5.1; no hay JAR generado. Plazo absoluto de lectura verificado en HTTP/1.1 NIO demo; tarea Hecha. TLS/HTTP2 se rechazan hasta validación posterior.

**Actualización AIR-015 del17/11/2026:** módulo flightcomparison y endpoint demo implementados, reutilizando Search para reconstrucción completa. Versiones y15 dependencias sin cambios; no se añadió tecnología de persistencia, frontend o transporte externo. Wrapper offline clean test compila74 fuentes principales y25 de tests:238 pruebas satisfactorias en24 clases JUnit. El histórico181 anterior se conserva como tal; resultados actuales en [VALIDATION](VALIDATION.md).

**Actualización AIR-021 del 19/11/2026, Hecha:** IMP-008 se promueve a un núcleo Java puro en `flightprovider.admission`, sin Spring bean, HTTP, conexión al demo o adaptador real. No añade dependencias ni cambia versiones. El ledger en memoria es contabilidad de un escenario sintético, con atomicidad limitada a servicios que comparten la misma instancia; no es persistencia ni garantiza recuperación o coordinación multiproceso. Contrato y límites en [PROVIDER_ADMISSION](../architecture/PROVIDER_ADMISSION.md) y [ADR-0008](../decisions/0008-synthetic-admission-ledger.md). Compilación,42tests específicos y regresión280 satisfactoria en [VALIDATION](VALIDATION.md), con79fuentesmain/29tests y28clases ejecutadas; las cifras anteriores se conservan como histórico.

## Base existente

| Elemento | Evidencia observada | Límite de la afirmación |
|---|---|---|
| Coordenadas Maven | `com.airwise:backend:0.0.1-SNAPSHOT` | Versión de artefacto generada; no es una release publicada |
| Java | Propiedad `java.version=21`; Temurin 21.0.12+8-LTS efectivo, Eclipse Adoptium | Fuente principal y test compilados con `release 21` |
| Spring Boot | Parent `spring-boot-starter-parent:4.1.2`, resuelto | Contexto demo de prueba inicializado satisfactoriamente con exclusión local de DataSourceAutoConfiguration |
| Maven instalado | `mvn -version` devuelve 3.9.16 | Inspección del entorno; los comandos del proyecto se ejecutaron con Wrapper |
| Maven Wrapper | Script 3.3.4, `only-script`; Maven 3.10.0 ejecutado, revisión `c43a36b8d67be7e0805a411bc0898af1a51f5472` | Distribución descargada en `.local/air011/maven-home/`; reintento autorizado tras error de transporte dentro del sandbox |
| Arranque | `com.airwise.backend.BackendApplication`, `@SpringBootApplication` | Bean principal y contexto activo comprobados en el test demo; arranque con configuración principal pendiente |
| Configuración | `spring.application.name=backend` | No hay conexión de BD, perfiles o claves de proveedor configurados en ese archivo |
| Test | `demoContextLoadsWithoutPersistence`, `@SpringBootTest`, Jupiter 6.0.3 | Un test, cero fallos, cero errores y cero omitidos; contexto activo, bean principal presente y cero DataSources |

## Dependencias directas declaradas: 15

Las quince dependencias se comprobaron en el POM efectivo y el árbol resuelto de AIR-011. Los artefactos de Spring Boot usan 4.1.2, `flyway-database-postgresql` usa 12.4.0 y `postgresql` usa 42.7.14. Se conservan a continuación los scopes y la distinción entre dependencia y funcionalidad implementada.

| Dependencia | Scope / atributo | Presencia frente a implementación |
|---|---|---|
| `org.springframework.boot:spring-boot-starter-actuator` | compile implícito | Declarada; exposición demo web/JMX cerrada y rutas denegadas en tests |
| `org.springframework.boot:spring-boot-starter-data-jpa` | compile implícito | Declarada; no hay entidades ni repositorios de negocio |
| `org.springframework.boot:spring-boot-starter-flyway` | compile implícito | Declarada; no hay migraciones |
| `org.springframework.boot:spring-boot-starter-security` | compile implícito | Cadena demo restrictiva implementada; sin usuarios ni autenticación de cuentas |
| `org.springframework.boot:spring-boot-starter-validation` | compile implícito | Invariantes puras y DTO/parser HTTP; Bean Validation en paginación |
| `org.springframework.boot:spring-boot-starter-webmvc` | compile implícito | Servlet/Web MVC; no se presupone WebFlux ni un starter llamado web |
| `org.flywaydb:flyway-database-postgresql` | compile implícito | Soporte declarado, sin BD creada ni scripts |
| `org.springframework.boot:spring-boot-devtools` | runtime; optional=true | Herramienta declarada, sin uso de ejecución verificado |
| `org.postgresql:postgresql` | runtime | Driver declarado; no demuestra servidor PostgreSQL disponible |
| `org.springframework.boot:spring-boot-starter-actuator-test` | test | Declarado |
| `org.springframework.boot:spring-boot-starter-data-jpa-test` | test | Declarado |
| `org.springframework.boot:spring-boot-starter-flyway-test` | test | Declarado |
| `org.springframework.boot:spring-boot-starter-security-test` | test | Declarado |
| `org.springframework.boot:spring-boot-starter-validation-test` | test | Declarado |
| `org.springframework.boot:spring-boot-starter-webmvc-test` | test | Declarado |

Plugin explícito `spring-boot-maven-plugin`, resuelto a 4.1.2 por herencia. El parent resuelto hereda `spring-boot-dependencies:4.1.2`, que gestiona entre otros los BOM de Spring Framework, Spring Security y JUnit. No hay POM agregador en la raíz.

## Versiones resueltas y compatibilidad observada en AIR-011

| Componente | Versión comprobada |
|---|---|
| Spring Framework | 7.0.10 |
| Spring Security | 7.1.2 |
| Hibernate ORM | 7.4.13.Final |
| Flyway Core y soporte PostgreSQL | 12.4.0 |
| PostgreSQL JDBC | 42.7.14 |
| HikariCP | 7.0.2 |
| JUnit Jupiter API y Engine | 6.0.3 |
| JUnit Platform Engine | 6.0.3 |
| Maven Compiler / Surefire | 3.15.0 / 3.5.6 |
| Maven Clean / Resources / Jar | 3.5.0 / 3.5.0 / 3.5.1 |
| Maven Install / Deploy / Site | 3.1.4 / 3.1.4 / 3.22.0, configurados por herencia |
| Plugins diagnósticos Help / Dependency | 3.5.2 / 3.10.0, ejecutados |

`clean compile` terminó con código 0 durante el diagnóstico inicial. Tras la corrección autorizada, `test` recompiló el test y terminó con BUILD SUCCESS y código 0. La prueba verifica la inicialización real del contexto demo con Java 21. Las integraciones de persistencia y el arranque con la configuración principal permanecen pendientes.

El diagnóstico inicial reprodujo `Failed to determine a suitable driver class` al faltar URL y una BD embebida configurable, con driver PostgreSQL resuelto. El ajuste autorizado excluye solo DataSourceAutoConfiguration mediante una propiedad de `@SpringBootTest`. El contexto demo inicia satisfactoriamente y sus autoconfiguraciones de persistencia condicionadas quedan fuera de esa prueba. Los resultados anteriores y el cierre se conservan en [VALIDATION.md](VALIDATION.md).

El generado incluye directorios vacíos `src/main/resources/static`, `templates` y `db/migration`. Se conservan por la instrucción de preservar el backend; no indican frontend ni migraciones implementadas. No se crearon nuevos directorios vacíos fuera de esa base.

## Frontend instalado en AIR-018 — 18/11/2026

El coordinador observó Node.js 24.21.0 y npm 11.7.0, ejecutó el generador oficial create-vite 9.3.4 para React/TypeScript y realizó la instalación local en `frontend/`. `package.json` declara rangos; `package-lock.json` fija el árbol resuelto y `npm ls --depth=0` informó estas versiones. No son instalaciones globales ni versiones publicadas de Airwise; `frontend:0.0.0` es un marcador privado del paquete.

El campo `engines.node` declara `^22.22.2 || ^24.15.0 || >=26.0.0` tras contrastar los requisitos de las herramientas resueltas; la versión realmente probada es 24.21.0. No se afirma haber ejecutado la matriz completa de versiones admitidas.

| Dependencia directa | Versión resuelta observada | Uso |
|---|---|---|
| react / react-dom | 19.3.1 / 19.3.1 | Interfaz y renderizado |
| typescript | 7.0.2 | Comprobación estricta de tipos |
| vite / @vitejs/plugin-react | 8.3.8 / 6.3.0 | Desarrollo y build del cliente |
| oxlint | 1.94.0 | Lint del template; no se añade ESLint |
| vitest | 5.1.1 | Ejecución de pruebas |
| @testing-library/react | 16.3.3 | Comportamiento de componentes |
| @testing-library/user-event | 14.6.12 | Interacciones en pruebas |
| @testing-library/jest-dom | 7.0.1 | Aserciones del DOM |
| jsdom | 31.0.1 | Entorno DOM simulado |
| @types/node | 24.19.2 | Tipos de configuración Node |
| @types/react / @types/react-dom | 19.3.0 / 19.3.0 | Tipos de React |

La instalación npm informó cero vulnerabilidades conocidas en su comprobación; no acredita una auditoría exhaustiva ni elimina el seguimiento de dependencias. Typecheck, lint y build se ejecutaron satisfactoriamente; pruebas unitarias y dos niveles de integración local se detallan en [VALIDATION](VALIDATION.md), separados de la [revisión asistida de navegador](BROWSER_REVIEW.md). Para esta revisión se observó agent-browser 0.39.1, Chrome 155.0.8059.39 headless y axe 4.12.1, en perfil/sesión temporal sin añadir dependencias al frontend. No se infiere cobertura de otros navegadores. [FRONTEND](FRONTEND.md) contiene los comandos y [ADR-0006](../decisions/0006-demo-frontend.md) justifica las dependencias añadidas y aplazadas.

## Objetivos documentales y componentes pendientes

- **JUnit 5** fue el objetivo documental inicial. AIR-011 comprobó Jupiter/Platform **6.0.3**, gestionados por el parent actual. La propiedad heredada `junit.version=4.13.2` no supone que JUnit 4 participe en las pruebas. El árbol examinado carece de `junit:junit`. Se actualiza la descripción al resultado ejecutado.
- H2 y Testcontainers están ausentes del árbol resuelto. Una herramienta OpenAPI no aparece como dependencia directa. No hay especificación OpenAPI ejecutable ni Javadoc del dominio.
- React, TypeScript, Vite y Vitest están instalados para AIR-018. Tailwind CSS, TanStack Query, React Router y Redux no son necesarios para este flujo; Playwright no está instalado como dependencia del proyecto. No confundir herramienta prevista con componente presente.
- PostgreSQL como servidor y Docker Compose: previstos; sin servicios configurados. AIR-020 prepara [GitHub Actions](CI.md) localmente mediante ci.yml, con validación estática y comandos equivalentes Windows; ejecución remota/Linux pendiente. No añade dependencias de producto ni cambia versiones de Java/Node/Python observadas.
- Monolito modular: dominio/aplicación/proveedor AIR-013, fronteras HTTP y seguridad AIR-014, y flightcomparison.domain/application/api AIR-015. Search expone reconstrucción completa y Comparison no depende de fuentes reales. Adaptadores Travelpayouts/SerpApi siguen sin implementar.

## Compatibilidad estática revisada en AIR-016

El 13/11/2026 se inspeccionaron JAR/metadata/bytecode locales, sin Maven ni ejecución del backend. Security 7.1.2 ofrece PathPatternRequestMatcher, matchers compuestos, handlers y addFilterBefore; Boot 4 sitúa WebMvcTest/AutoConfigureMockMvc en org.springframework.boot.webmvc.test.autoconfigure. Jackson 3 ofrece restricciones/coerciones y EnumFeature para enum numérico.

Las propiedades de error actuales son spring.web.error.*; server.error.include-* figura como alias deprecado con nivel error. La metadata de Security incluye los cinco dispatchers servlet. La exclusión demo de DataSource no cubre por sí sola Flyway con URL/connection details, JNDI o XA. Una cadena propia tampoco impide usuario generado: se ha diseñado la exclusión UserDetailsServiceAutoConfiguration solo en demo.

Diseño/criterios en [DEMO_RUNTIME](DEMO_RUNTIME.md) y [TESTING_STRATEGY](TESTING_STRATEGY.md). No han cambiado versiones, dependencias, configuración ni la evidencia histórica de 62 tests.

## Entorno de agentes observado

Extensión instalada `openai.chatgpt` **26.51106.51428**, con requisito VS Code `^1.96.2`. El ejecutable incluido respondió **codex-cli 0.167.0-alpha.4.1**. La versión de VS Code en ejecución no se ha verificado. Python 3.14.7 está disponible para validación documental; no se incorpora como dependencia del producto.

## Pendientes y tratamiento

AIR-011 valida el contexto demo sin persistencia tras aplicar el cambio autorizado al único archivo de test. El test conserva SpringBootTest y comprueba contexto activo, configuración principal presente como bean y ausencia de DataSources. La configuración de producción conserva su contenido.

PostgreSQL, JPA y Flyway siguen pendientes de integración. El test original usa su exclusión local y perfil predeterminado; los tests AIR-014 activan demo explícitamente y acreditan ausencia de recursos persistentes y usuario generado, no funcionamiento de persistencia.

Actuator cerrado y reglas HTTP demo se verificaron en AIR-014. Conexiones PostgreSQL, transacciones, migraciones y controles de publicación siguen pendientes. El plazo absoluto de lectura lenta se verificó en HTTP/1.1 NIO local; TLS/HTTP2 requieren validación independiente. Las advertencias HTTP401 de metadatos pertenecen al diagnóstico histórico AIR-011; AIR-014 usó modo offline. `distributionSha256Sum` sigue ausente y no se ha modificado el Wrapper para incorporarla.
