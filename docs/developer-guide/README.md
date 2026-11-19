# Guía de desarrollo inicial

La raíz contiene instrucciones, planificación, agentes y documentación. `backend/` contiene el proveedor Java AIR-013 y catálogo/búsqueda REST AIR-014 completada. Perfil demo y [arranque](DEMO_RUNTIME.md) comprobados; consulte el [inventario](TECHNOLOGY_INVENTORY.md) y los [ejemplos HTTP](../api/DEMO_USAGE.md). El cliente React/TypeScript/Vite de AIR-018 está implementado y verificado en `frontend/`; su uso y comandos constan en la [guía frontend](FRONTEND.md), con [revisión asistida de navegador](BROWSER_REVIEW.md) y límites explícitos.

AIR-015 añade el módulo flightcomparison, reconstrucción completa mediante Search y POST /api/v1/demo/comparisons, verificados el17/11/2026. Wrapper clean test pasa238 pruebas; Java independiente/curl confirma24 comprobaciones, incluidos ambos JSON publicados, sin APIs externas. Entradas y resultados en [DEMO_USAGE](../api/DEMO_USAGE.md) y [VALIDATION](VALIDATION.md); los181 tests de AIR-014 se conservan como histórico.

## Empezar una tarea

Use [CONTRIBUTING.md](../../CONTRIBUTING.md), seleccione una tarea del [backlog](../../planning/BACKLOG.md) y lea el ADR afectado. AIR-011 autoriza ejecutar Maven, diagnosticar y documentar. Las correcciones de código o configuración requieren autorización específica.

El [roadmap](../../planning/ROADMAP.md) ordena dependencias. `IMPROVEMENTS.md` conserva ideas no aprobadas. Un plan no es autorización para tocar la base del backend o ejecutar proveedores.

## Seguimiento recuperable y selección de skills

Antes de iniciar o retomar trabajo, lea `.local/ACTIVE_TASK.md` si existe y contraste sus notas con el estado de Git y las evidencias disponibles. El coordinador crea ese archivo al comenzar y mantiene un único seguimiento, también cuando delega. `.local/` está ignorado por Git; el archivo permite reanudar el trabajo en este workspace y no sustituye al backlog ni al registro permanente de validación.

Registre petición y alcance autorizado, plan, archivos y responsables, avances, comandos con resultados comprobados, pendientes o bloqueos y siguiente paso. Actualícelo después de cada hito y antes de una interrupción prevista, sin credenciales ni payloads sensibles. Si la tarea queda incompleta, consérvelo; una vez terminadas la tarea y sus verificaciones, elimine únicamente `.local/ACTIVE_TASK.md`, conservando las evidencias necesarias en su ubicación correspondiente.

Descubra las skills del catálogo actual y las instaladas globalmente y en `.agents/skills/`. Un `SKILL.md` presente en disco no acredita por sí solo que esté registrado en la sesión. Lea las instrucciones de las pertinentes y elija el procedimiento más ajustado: una skill local puede concretar mejor las reglas de Airwise que una global similar. Justifique solapamientos, evite procedimientos duplicados y registre cuáles se utilizaron realmente. Descubrir una skill no autoriza instalarla ni cambiar configuración global. Las reglas persistentes están en [AGENTS.md](../../AGENTS.md).

## Comandos disponibles y comprobados

Desde PowerShell en la raíz:

```powershell
java -version
mvn -version
py -3.14 scripts/validate_repository.py
git status --short
```

Python 3.11+ permite analizar TOML con biblioteca estándar. El script verifica documentación, metadatos, enlaces, backlog, POM, presencia de archivos esenciales y secretos en fuentes. Excluye exactamente `backend/target/` y conserva como información el contraste con la instantánea histórica si existe. Una carpeta llamada target dentro de fuentes sigue analizándose.

## Ejecución local comprobada en AIR-011

El 12/11/2026, el Wrapper ejecutó Maven 3.10.0 con Temurin 21.0.12. `clean compile` terminó con código 0. El primer test falló por DataSource. Tras autorizar y aplicar la exclusión local al test, `test` terminó con código 0, un test ejecutado, cero errores y cero omitidos. El POM efectivo y el árbol resuelto se exportaron correctamente. El registro de validación conserva las dos etapas.

Los comandos siguientes reproducen los goals desde PowerShell. Se empleó un proceso hijo con variables de proveedores y opciones externas Java/Maven/Spring excluidas, conservando JAVA_HOME y transporte. Las asignaciones de caché pertenecen al proceso de ejecución.

```powershell
# Desde la raíz, en una sesión dedicada
$airwiseRoot = (Get-Location).Path
$airwiseCache = Join-Path $airwiseRoot '.local/air011'
New-Item -ItemType Directory -Force -Path "$airwiseCache/maven-home", "$airwiseCache/repository", "$airwiseCache/tmp" | Out-Null
$env:MAVEN_USER_HOME = "$airwiseCache/maven-home"
$env:TEMP = "$airwiseCache/tmp"
$env:TMP = "$airwiseCache/tmp"
Push-Location backend
try {
    .\mvnw.cmd --version
    .\mvnw.cmd clean compile "-Dmaven.repo.local=$airwiseCache/repository" -B -ntp
    .\mvnw.cmd test "-Dmaven.repo.local=$airwiseCache/repository" -B -ntp
    .\mvnw.cmd help:effective-pom -Dverbose "-Doutput=../.local/air011/effective-pom.xml" "-Dmaven.repo.local=$airwiseCache/repository" -B -ntp
    .\mvnw.cmd dependency:tree -Dverbose "-DoutputFile=../.local/air011/dependency-tree.txt" "-Dmaven.repo.local=$airwiseCache/repository" -B -ntp
} finally {
    Pop-Location
}
```

Revisar el código de salida de cada comando mediante `$LASTEXITCODE`. Los resultados de las operaciones diagnósticas posteriores se evalúan individualmente. Los logs y reportes permanecen en rutas ignoradas y se sanitizan antes de compartirlos. Cargar el contexto de test y arrancar la aplicación con su configuración principal son comprobaciones de alcance diferente.

No hay un proyecto Maven raíz, Compose ni pipeline que ejecutar. AIR-018 incorpora scripts npm dentro de `frontend/`, documentados en [FRONTEND](FRONTEND.md); su presencia se distingue del resultado ejecutado. No se debe inventar un usuario por defecto o un flujo de login para Airwise.

## Histórico: proveedor demo comprobado en AIR-013

Los paquetes airport.domain, flightsearch.domain/application y flightprovider.demo contienen el primer componente de negocio. [PROVIDERS.md](../architecture/PROVIDERS.md) documenta escenarios, admisión, recurso versionado y límites. Se instancia DemoFlightProvider directamente y se invoca el puerto Java; en aquel incremento aún no había wiring Spring ni controladores de negocio. AIR-014 los añade bajo perfil explícito demo.

El 13/11/2026 se ejecutaron Wrapper clean compile y test con -o -B -ntp y las cachés existentes de AIR-011: 33 fuentes principales compiladas y 62 tests satisfactorios, incluido el contexto conservado. Offline evita descargar/instalar dependencias. Ese modo requiere que distribución y artefactos ya existan en caché; no prueba la reproducibilidad de un clon sin ella.

En una sesión dedicada, con el mismo aislamiento de entorno descrito arriba, los goals usados fueron:

```powershell
# Desde backend, con $airwiseCache apuntando a la caché existente de AIR-011
.\mvnw.cmd -o -B -ntp "-Dmaven.repo.local=$airwiseCache/repository" clean compile
.\mvnw.cmd -o -B -ntp "-Dmaven.repo.local=$airwiseCache/repository" test
```

No se sustituyó la configuración de producción ni se omitieron tests. La prueba de contexto conserva su exclusión local a DataSource; las pruebas nuevas del componente son puras y sin servicios externos.

## Núcleo de admisión sintético — AIR-021

IMP-008 se promueve a AIR-021 con autorización de implementación; tarea Hecha tras compilación,42tests específicos y regresión280 satisfactoria. El paquete Java puro `flightprovider.admission` separa estimación, admisión y ledger, sin registrarse en Spring ni conectarse al proveedor demo, endpoints o transporte externo. No incorpora dependencias ni persistencia.

El [contrato de admisión](../architecture/PROVIDER_ADMISSION.md) concentra las reglas de presupuesto, reservas y consumo incierto; [ADR-0008](../decisions/0008-synthetic-admission-ledger.md) limita la garantía a un escenario sintético con una misma instancia de ledger compartida. Memoria no ofrece recuperación tras reinicio ni coordinación multiproceso: esos alcances y la ejecución real se rechazan. La inicialización de un escenario nuevo no acredita recuperación de deuda anterior. Resultados ejecutados y pendientes se registran en [VALIDATION](VALIDATION.md); los resultados históricos de AIR-015 no verifican este nuevo paquete.

## Configuración y secretos

La aplicación generada solo declara su nombre. No añada conexiones, perfiles o credenciales a esa base sin autorización. Las variables de proveedores se documentan como nombres en la [política de API](../api/PROVIDER_POLICY.md); no se usan para autenticar consultas durante AIR-010. El validador local puede comparar valores disponibles para detectar filtraciones, sin mostrarlos.

## Evolución del validador

El validador conserva las comprobaciones de POM y la existencia de sus puntos de entrada actuales, recursos y test. En AIR-011 se corrigió el alcance para excluir `backend/target/` de la enumeración, del escaneo y del contraste histórico. Los archivos fuente/configuración relevantes se siguen analizando.

El snapshot inicial de diez archivos se conserva sin regenerarlo. Las diferencias de contenido y de conjunto se informan como histórico, con rutas y cantidades. La revisión del diff, el alcance autorizado y los tests pertinentes acreditan las modificaciones posteriores. El histórico deja de exigir hashes iniciales inmutables para cada versión del proyecto. Un clon sin snapshot declara el contraste histórico no disponible y sigue comprobando fuentes, metadatos y archivos esenciales.

## Diseño de seguridad y ejecución AIR-016

AIR-014 implementó el perfil demo y guardas diseñados en AIR-016: [DEMO_RUNTIME](DEMO_RUNTIME.md). AIR-015 conserva arranque, configuración común y test original; amplía solo las rutas/mapeos necesarios y el módulo de comparación. `clean test` offline actual pasa238 pruebas. El fallo de `package` offline por maven-jar-plugin3.5.1 pertenece a AIR-014; AIR-015 no acredita empaquetado ni descarga dependencias.

[BASELINE](../security/BASELINE.md) fija permisos, gate anterior a CSRF, CORS, Actuator, errores y límites. Catálogo/búsqueda y comparación se verifican con esos controles, incluidos tipos/campos/selección inválidos y regresiones del transporte. [TESTING_STRATEGY](TESTING_STRATEGY.md) y [VALIDATION](VALIDATION.md) distinguen pruebas ejecutadas de persistencia, TLS/HTTP2 y publicación futuras.

O-01 y el plazo total de lectura están corregidos y probados en AIR-014. El transporte demo está limitado a HTTP/1.1 NIO/Tomcat11, sin TLS/HTTP2; deberá revalidarse al cambiar el servidor o publicar. AIR-015 conserva estos controles, sin cambiar el arranque ni activar otra fase. Decisión y consecuencias en [ADR-0005](../decisions/0005-demo-body-read-deadline.md).

## Agentes y verificación

AIR-020 añade el [workflow CI local y su guía](CI.md): documentación, backend y frontend con versiones fijadas, mínimos permisos y sin proveedores. Comandos equivalentes comprobados en Windows; ejecución de acciones y runner Linux pendientes. La guía distingue instalación, tests, integraciones opt-in y publicación; no se ha hecho push ni activado ajustes remotos.

El [manual de Codex](CODEX_SETUP.md) explica los siete roles, las cinco skills, la activación y la diferencia entre archivo configurado y agente invocado. Consulte [VALIDATION.md](VALIDATION.md) para las comprobaciones reales y sus límites. La sesión principal conserva la responsabilidad de revisar y cerrar una tarea con evidencia.
