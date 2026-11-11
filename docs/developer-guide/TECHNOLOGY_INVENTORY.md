# Inventario tecnológico observado

Inspección del checkout: **11/11/2026**. Fuentes: [POM](../../backend/pom.xml), clases generadas, `application.properties`, wrapper y archivos de herramientas. No se modificó ningún archivo de `backend/` ni se resolvieron sus dependencias.

## Base existente

| Elemento | Evidencia observada | Límite de la afirmación |
|---|---|---|
| Coordenadas Maven | `com.airwise:backend:0.0.1-SNAPSHOT` | Versión de artefacto generada; no es una release publicada |
| Java | Propiedad `java.version=21` | JDK instalado Temurin 21.0.12; no se ejecutó compilación backend |
| Spring Boot | Parent `spring-boot-starter-parent:4.1.2` | Versión declarada; no se ha resuelto el árbol efectivo |
| Maven instalado | `mvn -version`: 3.9.16 | Distinto de la distribución declarada por el wrapper |
| Maven Wrapper | Wrapper 3.3.4, `only-script`; distribución Maven 3.10.0 | `mvnw`/`mvnw.cmd` presentes; descarga/ejecución no comprobadas |
| Arranque | `com.airwise.backend.BackendApplication`, `@SpringBootApplication` | Solo arranque generado; sin funcionalidad de negocio |
| Configuración | `spring.application.name=backend` | No hay conexión de BD, perfiles o claves de proveedor configurados en ese archivo |
| Test | `BackendApplicationTests.contextLoads`, `@SpringBootTest`, import JUnit Jupiter | Existe; no ejecutado. El import no demuestra una versión mayor concreta de JUnit |

## Dependencias directas declaradas: 15

Las versiones se heredan del parent o de su gestión transitiva; no se atribuye una versión resuelta sin ejecutar Maven.

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

Plugin explícito: `spring-boot-maven-plugin`, sin versión local independiente. No hay POM agregador en la raíz.

El generado incluye directorios vacíos `src/main/resources/static`, `templates` y `db/migration`. Se conservan por la instrucción de preservar el backend; no indican frontend ni migraciones implementadas. No se crearon nuevos directorios vacíos fuera de esa base.

## Tecnologías previstas que no se confirman como instaladas/integradas

- **JUnit 5** es el objetivo del proyecto; la versión resuelta de JUnit Jupiter no se ha comprobado. Debe revisarse su compatibilidad con el parent existente, sin degradar ni sustituir dependencias automáticamente.
- Testcontainers y una herramienta OpenAPI no aparecen como dependencias directas; no se afirma su disponibilidad transitiva. No hay especificación OpenAPI ni Javadoc del dominio.
- React, TypeScript, Vite, Tailwind CSS, TanStack Query, Vitest y Playwright: sin proyecto frontend ni `package.json`.
- PostgreSQL como servidor, Docker Compose y GitHub Actions: previstos; no se han configurado ni comprobado servicios.
- Monolito modular y adaptadores de proveedores: propuesta arquitectónica, todavía sin paquetes de negocio.

## Entorno de agentes observado

Extensión instalada `openai.chatgpt` **26.51106.51428**, con requisito VS Code `^1.96.2`. El ejecutable incluido respondió **codex-cli 0.167.0-alpha.4.1**. La versión de VS Code en ejecución no se ha verificado. Python 3.14.7 está disponible para validación documental; no se incorpora como dependencia del producto.

## Pendientes y tratamiento

No se arrancó Spring ni se ejecutó `contextLoads`: JPA/Flyway pueden requerir un entorno de persistencia y hay que comprobarlo, no asumir éxito o fallo. Tampoco se descargó el Maven del wrapper. Resolver esas comprobaciones corresponde a AIR-011; cualquier corrección del POM, wrapper, código o configuración necesitará autorización previa y evidencia del problema.
