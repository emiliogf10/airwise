# Integración continua — AIR-020

El workflow local [ci.yml](../../.github/workflows/ci.yml) valida los componentes existentes. Preparado el **19/11/2026**, revisado estáticamente y con comandos equivalentes ejecutados en Windows. **No se ha subido ni ejecutado en GitHub**: resolución de herramientas y ejecución en Linux pendientes. No se afirma una CI remota operativa ni se configura despliegue.

## Alcance y comandos

Tres jobs independientes sobre `ubuntu-24.04`; un fallo de instalación o comprobación falla su job. Sin cachés compartidas, artefactos subidos, servicios externos, credenciales de proveedores ni publicación de paquetes.

| Job | Entorno fijado | Comandos | Límite |
|---|---|---|---|
| Documentation | Python 3.14.7 | `python scripts/validate_repository.py` desde raíz | 5 min; formatos, enlaces y comprobaciones locales, sin APIs |
| Backend tests | Temurin 21.0.12+8 y Maven 3.10.0 del Wrapper existente | `bash ./mvnw --batch-mode --no-transfer-progress -Dlogging.level.org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration=ERROR clean test` desde backend | 15 min; compilación/tests, sin empaquetado ni persistencia real |
| Frontend checks | Node 24.21.0 | `npm ci --no-audit --no-fund`, `npm run typecheck`, `npm run lint`, `npm test`, `npm run build` desde frontend | 10 min; lockfile, tipos, lint, jsdom y build estático |

El Wrapper está versionado con modo 100644: invocarlo mediante Bash evita modificarlo o cambiar sus permisos. El argumento de logging afecta solo al proceso de pruebas y evita imprimir la contraseña temporal del contexto Spring predeterminado. La ejecución local confirmó que no apareció el mensaje antes de sanitizar el log; no altera autenticación ni configuración versionada del backend.

Las versiones de Java/Node/Python corresponden al entorno local observado, no a una matriz probada de plataformas. En una VM limpia se descargarán herramientas y dependencias; los tests de aplicación usan datos demo/locales y no necesitan claves. No se añade `--if-present`, `continue-on-error` ni se omiten comprobaciones requeridas para esconder fallos.

### Pruebas incluidas y excluidas

La suite backend actual verifica 238 casos, incluidos tests HTTP locales. La suite frontend ordinaria ejecuta 53 casos y omite intencionadamente 1 integración opt-in. No es una nueva omisión introducida por CI.

`test:integration` y `test:integration:ui` necesitan backend y proxy Vite activos según [FRONTEND](FRONTEND.md). Se mantienen como verificaciones locales separadas; este workflow mínimo no arranca ambos servicios ni acredita esos flujos en Linux. La revisión asistida de navegador de AIR-018 tampoco se convierte en E2E automatizado. Empaquetado Java, BD, TLS/HTTP2 y despliegue siguen fuera del alcance.

## Eventos, permisos y dependencias del workflow

- Declarados `pull_request`, `push` a `main` y `workflow_dispatch`; sin filtros de rutas. Al publicarlo en un repositorio con Actions habilitado, esos eventos podrán ejecutarlo. El evento manual requiere que el workflow esté disponible en la rama predeterminada conforme a GitHub.
- `contents: read` y el resto de permisos sin conceder; no se usa `pull_request_target`, ambientes de despliegue o secretos configurados. El token efímero de lectura de GitHub sigue siendo parte del funcionamiento de las acciones oficiales.
- Checkout sin persistir credenciales ni modificar `safe.directory` global. Configuración Maven generada por setup-java aislada en `runner.temp`, sin credenciales de publicación exportadas. Caché automática de setup-node desactivada.
- Grupo de concurrencia por workflow/ref y cancelación de ejecuciones anteriores de esa referencia; topes temporales por job. Estos límites reducen trabajo redundante, **no son un control de facturación**.
- Acciones oficiales fijadas por SHA completo. Renovar un pin requiere revisar release, procedencia, entradas y runtime; no sustituirlo automáticamente por una etiqueta móvil.

| Acción | Release consultada / origen del SHA del workflow |
|---|---|
| checkout | [v7.0.1](https://github.com/actions/checkout/releases/tag/v7.0.1) |
| setup-python | [v7.1.0](https://github.com/actions/setup-python/releases/tag/v7.1.0) |
| setup-java | [v6.3.0](https://github.com/actions/setup-java/releases/tag/v6.3.0) |
| setup-node | [v7.1.0](https://github.com/actions/setup-node/releases/tag/v7.1.0) |

Las acciones seleccionadas usan Node24 internamente y requieren runner compatible; esto es distinto de la versión Node que compila el frontend. No se configura un runner autogestionado. Se conserva el límite previo del Wrapper: `distributionSha256Sum` no está definido; AIR-020 no modifica esa base protegida ni presenta el pin de acciones como verificación de todas las descargas Maven/npm.

## Comprobaciones locales y diferencias con GitHub

La evidencia ejecutada está en [VALIDATION](VALIDATION.md). Se comprobó el YAML con el parser seguro SnakeYAML2.6 ya disponible y assertions de política sobre eventos, jobs, permisos y SHA. No es un validador completo del schema de GitHub ni una ejecución de acciones.

El backend se ejecutó mediante `mvnw.cmd` con los mismos goals y propiedad de logging, añadiendo `-o` y las cachés existentes bajo `.local/air011`. El workflow Linux usa Bash y resolverá dependencias por red. No se afirma un clon limpio Maven ni descarga Linux comprobados.

`npm ci` se intentó primero offline; faltaba un tarball en caché. Se completó después con descarga autorizada y caché en `.local/air020/npm-cache`, conservando package.json y lockfile. Typecheck, lint, tests y build usan los scripts exactos del workflow. No se instaló ninguna herramienta global.

En un clon sin `.local`, el validador indica correctamente que el contraste histórico de fase1 no está disponible; sigue validando fuentes, enlaces y configuración. Se ejecuta en un job independiente que no construye componentes y no necesita snapshots privados ni otros jobs. Los tres jobs pueden ejecutarse en paralelo.

## Antes de usarlo en GitHub

No se han consultado ni cambiado ajustes del repositorio remoto. La revisión/subida del workflow y su activación necesitan la autorización correspondiente; no hacer push desde esta tarea.

1. Comprobar visibilidad, disponibilidad de Actions, políticas permitidas y compatibilidad del runner. No asumir que la preparación local registra el workflow o establece reglas de rama protegida.
2. Mantener gasto 0 €: GitHub documenta runners estándar gratuitos para repositorios públicos; los privados pueden consumir minutos incluidos y generar cargos. Verificar plan, remanente y bloqueo de gasto antes de activar; no contratar recursos ni ampliar presupuesto automáticamente. Los presupuestos de APIs de vuelos siguen siendo 0 € y cero llamadas autorizadas.
3. Revisar la PR y comprobar la primera ejecución real: tres jobs verdes, herramientas/versiones resueltas y logs sin secretos. Si falla, conservar causa y corregir mediante una tarea autorizada; no retirar tests o ampliar permisos para obtener verde.
4. Solo después de esa evidencia, decidir si estos checks serán obligatorios para merge. No se ha aplicado esa configuración remota ni se garantiza ejecución de PR de forks sin la aprobación que exijan sus políticas.

## Fuentes oficiales de configuración

Consultadas el 19/11/2026 por DevOps/Orchestrator, solo páginas públicas:

- [Sintaxis y permisos](https://docs.github.com/en/actions/reference/workflows-and-actions/workflow-syntax#permissions), [eventos](https://docs.github.com/en/actions/reference/workflows-and-actions/events-that-trigger-workflows) y [concurrencia](https://docs.github.com/en/actions/how-tos/write-workflows/choose-when-workflows-run/control-workflow-concurrency).
- [Uso seguro de acciones](https://docs.github.com/en/actions/reference/security/secure-use), [build/test Node](https://docs.github.com/en/actions/tutorials/build-and-test-code/nodejs) y [facturación](https://docs.github.com/en/actions/concepts/billing-and-usage).
- [Resolución Temurin de setup-java](https://github.com/actions/setup-java/blob/v6.3.0/src/distributions/temurin/installer.ts) y [propiedades de Surefire](https://maven.apache.org/surefire/maven-surefire-plugin/test-mojo.html#promoteUserPropertiesToSystemProperties).

Esta guía describe un workflow local revisable, con ejecución remota pendiente. Las políticas de proveedor y [ADR-0007](../decisions/0007-real-provider-admission.md) siguen vigentes.
