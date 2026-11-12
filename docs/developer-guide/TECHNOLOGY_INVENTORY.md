# Inventario tecnológico observado

Inventario inicial del **11/11/2026**, actualizado con el cierre autorizado de AIR-011 el **12/11/2026**. Fuentes del estado actual son el [POM](../../backend/pom.xml), POM efectivo, árbol resuelto, ejecución del Wrapper y reporte Surefire. El histórico conserva diez originales, nueve idénticos y el test modificado por autorización expresa. Maven regeneró artefactos ignorados en `backend/target/`.

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
| `org.springframework.boot:spring-boot-starter-actuator` | compile implícito | Declarada; endpoints/seguridad operativa no verificados |
| `org.springframework.boot:spring-boot-starter-data-jpa` | compile implícito | Declarada; no hay entidades ni repositorios de negocio |
| `org.springframework.boot:spring-boot-starter-flyway` | compile implícito | Declarada; no hay migraciones |
| `org.springframework.boot:spring-boot-starter-security` | compile implícito | Declarada; no hay configuración de usuarios/permisos de negocio |
| `org.springframework.boot:spring-boot-starter-validation` | compile implícito | Declarada; no hay DTOs o reglas de negocio |
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

## Objetivos documentales y componentes pendientes

- **JUnit 5** fue el objetivo documental inicial. AIR-011 comprobó Jupiter/Platform **6.0.3**, gestionados por el parent actual. La propiedad heredada `junit.version=4.13.2` no supone que JUnit 4 participe en las pruebas. El árbol examinado carece de `junit:junit`. Se actualiza la descripción al resultado ejecutado.
- H2 y Testcontainers están ausentes del árbol resuelto. Una herramienta OpenAPI no aparece como dependencia directa. No hay especificación OpenAPI ejecutable ni Javadoc del dominio.
- React, TypeScript, Vite, Tailwind CSS, TanStack Query, Vitest y Playwright: sin proyecto frontend ni `package.json`.
- PostgreSQL como servidor, Docker Compose y GitHub Actions: previstos; no se han configurado ni comprobado servicios.
- Monolito modular y adaptadores de proveedores: diseño aceptado en AIR-010, todavía sin paquetes de negocio ni adaptadores implementados.

## Entorno de agentes observado

Extensión instalada `openai.chatgpt` **26.51106.51428**, con requisito VS Code `^1.96.2`. El ejecutable incluido respondió **codex-cli 0.167.0-alpha.4.1**. La versión de VS Code en ejecución no se ha verificado. Python 3.14.7 está disponible para validación documental; no se incorpora como dependencia del producto.

## Pendientes y tratamiento

AIR-011 valida el contexto demo sin persistencia tras aplicar el cambio autorizado al único archivo de test. El test conserva SpringBootTest y comprueba contexto activo, configuración principal presente como bean y ausencia de DataSources. La configuración de producción conserva su contenido.

PostgreSQL, JPA y Flyway siguen pendientes de pruebas de integración. Una prueba de ese alcance requerirá PostgreSQL aislado y configuración externa cuando se autorice persistencia real. La prueba actual utiliza propiedades locales a la anotación y el perfil Spring predeterminado; no crea un perfil de producción denominado demo.

Actuator, reglas de acceso HTTP, conexiones PostgreSQL, transacciones y migraciones permanecen sin validar en runtime. Maven emitió advertencias HTTP 401 al pedir metadatos de prefijos a dos repositorios adicionales heredados de Flyway. Compilación, árbol y POM efectivo completaron sus operaciones. La propiedad `distributionSha256Sum` del Wrapper está ausente y su incorporación requiere una propuesta autorizada con checksum oficial verificado.
