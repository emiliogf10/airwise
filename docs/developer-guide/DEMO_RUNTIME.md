# Ejecución demo sin persistencia — AIR-014/015

**Actualizada el17/11/2026 con AIR-015.** Perfil explícito, catálogo/búsqueda/comparación, proveedor DEMO único y controles HTTP verificados. La suite integral pasa238 pruebas; resultados en [VALIDATION](VALIDATION.md), política en [BASELINE](../security/BASELINE.md), uso en [DEMO_USAGE](../api/DEMO_USAGE.md) y decisión de transporte en [ADR-0005](../decisions/0005-demo-body-read-deadline.md).

## Perfil y componentes reales

- `application-demo.properties` contiene exclusivamente configuración del modo demo. No se ha tocado application.properties, aplicación principal, POM, Wrapper ni el test original.
- `config.demo.DemoRuntimeConfiguration` registra Clock operativo UTC y DemoFlightProvider solo bajo perfil demo activo explícito. No se acepta demo como perfil por defecto ni combinado con otro perfil.
- Se excluyen únicamente `org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration` y `org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration`. No hay exclusiones preventivas JPA/Flyway.
- `DemoStartupGuard`, BeanFactoryPostProcessor estático, rechaza todas las propiedades spring.datasource.*, spring.flyway.url/user/password y definiciones DataSource, XADataSource, JdbcConnectionDetails, EntityManagerFactory o Flyway antes de instanciar singletons. No imprime valores ni inicia conexiones. No es un sandbox contra código arbitrario.
- `flightsearch.api.DemoSearchConfiguration` compone catálogo y caso de uso con el adaptador demo existente. No se registra ninguna fuente REAL.
- `flightcomparison.api.DemoComparisonConfiguration` añade motor y servicio de comparación sobre `DemoSearchService.reconstruct`, sin almacenamiento o llamadas nuevas de proveedor.
- `http.DemoHttpConfiguration` registra Security, límite de bytes/JSON, admisión, errores y cabeceras. Cobertura de los cinco dispatchers; FORWARD/INCLUDE/ASYNC no ejecutan negocio. ERROR interno solo escribe un error sanitizado, sin CSRF, nueva cuota o nuevo UUID.

Bind predeterminado127.0.0.1, sin confiar en Forwarded/X-Forwarded-*. Actuator web/JMX cerrado, sin usuario/password generado ni sesión. CORS externo vacío. Perfil desactiva LiveReload/defaults de desarrollo, pero la propiedad de perfil restart.enabled=false llega tarde para impedir el primer RestartClassLoader: se exige también `-Dspring.devtools.restart.enabled=false` en la JVM antes del main. Una guarda compara la identidad real del protocolo y los loaders de filtro/deadline y aborta antes de bind ante separación. DevTools permanece como dependencia; no se modifica el main ni se presenta este classpath como distribución de producción.

## Compilar y probar en el entorno comprobado

Se reutilizó únicamente la distribución Maven 3.10.0 y la caché AIR-011 del workspace. Java Temurin 21.0.12+8. Desde una sesión PowerShell dedicada en la raíz, sin variables externas de proveedores/Java/Maven/Spring ni DEBUG/TRACE/LOGGING_LEVEL_* heredadas:

```powershell
$airwiseRoot = (Get-Location).Path
$airwiseCache = Join-Path $airwiseRoot '.local/air011'
$env:MAVEN_USER_HOME = Join-Path $airwiseCache 'maven-home'
Push-Location backend
try {
    .\mvnw.cmd -o -B -ntp "-Dmaven.repo.local=$airwiseCache/repository" clean test
} finally { Pop-Location }
```

Resultado actual ejecutado:74 fuentes principales,25 fuentes de tests,238 pruebas en24 clases JUnit; cero fallos/errores/omitidas. Las asignaciones son de la sesión dedicada; no se modifica configuración global. Offline necesita caché previa y no acredita un clon sin ella. Los resultados181 de AIR-014 se conservan en el histórico de VALIDATION.

En AIR-014, `mvnw.cmd -o ... package` falló por maven-jar-plugin3.5.1 ausente de caché, sin descargar nada o alterar el POM. AIR-015 no repitió ni acredita empaquetado. Se valida arranque Java directo; spring-boot:run y un paquete de producción siguen sin acreditar.

## Arranque Java independiente comprobado

Además de SpringBootTest con servidor real, se lanzó la aplicación principal en un proceso Java separado, perfil demo y puerto aleatorio. Al no disponer del plugin de empaquetado, se reutilizó el classpath resuelto de Surefire, excluyendo test-classes. Esta es una receta local de verificación, no un formato de distribución:

```powershell
$airwiseReport = Get-ChildItem backend/target/surefire-reports/TEST-*.xml | Select-Object -First 1
[xml]$airwiseXml = Get-Content -Raw -LiteralPath $airwiseReport.FullName
$airwiseClasspath = ($airwiseXml.testsuite.properties.property | Where-Object name -eq 'java.class.path').value
$airwiseClasspath = (($airwiseClasspath -split ';' | Where-Object { $_ -notlike '*test-classes*' }) -join ';').Replace('\', '/')
New-Item -ItemType Directory -Force .local/air015 | Out-Null
$airwiseArguments = @('-Dspring.devtools.restart.enabled=false', '-cp', ('"' + $airwiseClasspath + '"'), 'com.airwise.backend.BackendApplication', '--spring.profiles.active=demo', '--server.port=0')
[System.IO.File]::WriteAllLines((Join-Path (Get-Location).Path '.local/air015/demo-java.args'), $airwiseArguments, [System.Text.UTF8Encoding]::new($false))
java '@.local/air015/demo-java.args'
```

Se ejecutó `java @.local/air015/demo-java.args` con esas opciones desde la raíz, entorno hijo saneado y caché existente. En la verificación final AIR-015 Tomcat anunció el puerto temporal63505; curl verificó24 comprobaciones, incluidos catálogo/búsqueda anteriores, comparación200 por las cuatro primarias y errores400/409/422/503. El proceso fue detenido al terminar. Use Ctrl+C cuando lo arranque manualmente. El classpath contiene librerías de pruebas, aunque no sus clases: no se presenta como distribución de producción.

## Relojes, cotas y límites de transporte

El dataset demo-v1 conserva evaluación fija `2027-01-01T00:00:00Z`, calendario 2027 y sus fixtures. Se validan fechas contra ese calendario, independientemente del día real. El reloj operativo del servidor fija UUID/deadline; los buckets usan nanoTime. Ninguno cambia identidades o precios sintéticos.

Fan-out secuencial de hasta tres orígenes, sin reintentos y plazo compartido de hasta dos segundos, truncado a segundos. Chequeos cooperativos antes/después del adaptador demo en memoria; no se promete cancelación dura de un futuro adaptador arbitrario.

Bytes efectivos 65536, lectura máxima65537, incluidos chunked/sin Content-Length; parser profundidad12/tokens10000/cadenas1024/nombres128/números32. Cabeceras del conector8192 bytes y query4096 bytes. Buckets60/IP y300global con recargas1/s y5/s, concurrencia2/IP y8global, mapa10000 claves/TTL10min. Límites observados en pruebas; no defensa distribuida o DDoS completa.

**Lectura absoluta5s comprobada:** BodyReadDeadline delimita el scope síncrono y DemoDeadlineProtocol espera con Selector local únicamente por el tiempo monotónico restante, incluido framing/trailers chunked. BodyReadValve evita drenaje posterior de cuerpos incompletos. Sin tareas lectoras diferidas; recursos retirados en finally. Goteo continuo, bloqueo a4,2s, chunk-header/trailers y desconexión probados; el proceso Java independiente rechazó goteo en5.01s con400 JSON. El diagnóstico anterior6,06s se conserva como fallo corregido, no como garantía actual. Este transporte se configura solo en demo, HTTP/1.1 NIO; TLS/HTTP2 fallan antes del bind y requieren validación propia. No se promete scheduling en tiempo real del SO. [ADR-0005](../decisions/0005-demo-body-read-deadline.md) registra mantenimiento y alcance.

Sin demo no se filtran estos beans/exclusiones. El test original sigue con su exclusión local. Persistencia/producción requerirán tarea autorizada y pruebas reales de PostgreSQL/JPA/Flyway. AIR-015 incorpora comparación manteniendo estas restricciones. El cliente demo AIR-018 está En revisión, sin modificar archivos de este arranque; consulte la [conexión frontend](FRONTEND.md) y sus opciones locales de puerto/allowlist. Los proveedores externos siguen sin implementar.

## Verificación de DevTools y classloaders

El primer arranque independiente posterior a180 tests verdes todavía tardó8,09s: DevTools separó el ThreadLocal en dos loaders. Se confirmó con traza de carga de clases. El arranque temprano con la opción JVM indicada rechazó goteo en5,01s, conservando DevTools en classpath. Un segundo arranque con restart habilitado fue rechazado por la guarda antes de abrir el puerto. RestartLauncher devolvió código0 pese a `Application run failed`; en ese modo el código de proceso no basta para declarar arranque correcto. La receta válida desactiva restart antes del main, y las pruebas verifican respuesta HTTP real. Evidencia en VALIDATION.
