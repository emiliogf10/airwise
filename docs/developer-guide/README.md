# Guía de desarrollo inicial

La raíz del repositorio contiene instrucciones, planificación, configuración de agentes y documentación. El único proyecto de aplicación presente es `backend/`, generado y con un ajuste de prueba acotado en AIR-011. Consulte el [inventario](TECHNOLOGY_INVENTORY.md) antes de elegir versiones o comandos.

## Empezar una tarea

Use [CONTRIBUTING.md](../../CONTRIBUTING.md), seleccione una tarea del [backlog](../../planning/BACKLOG.md) y lea el ADR afectado. AIR-011 autoriza ejecutar Maven, diagnosticar y documentar. Las correcciones de código o configuración requieren autorización específica.

El [roadmap](../../planning/ROADMAP.md) ordena dependencias. `IMPROVEMENTS.md` conserva ideas no aprobadas. Un plan no es autorización para tocar la base del backend o ejecutar proveedores.

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

No hay un comando raíz `mvn verify`, scripts npm, servidor de frontend, Compose ni pipeline que ejecutar. No se debe inventar una URL local, un usuario por defecto o un flujo de login para Airwise.

## Configuración y secretos

La aplicación generada solo declara su nombre. No añada conexiones, perfiles o credenciales a esa base sin autorización. Las variables de proveedores se documentan como nombres en la [política de API](../api/PROVIDER_POLICY.md); no se usan para autenticar consultas durante AIR-010. El validador local puede comparar valores disponibles para detectar filtraciones, sin mostrarlos.

## Evolución del validador

El validador conserva las comprobaciones de POM y la existencia de sus puntos de entrada actuales, recursos y test. En AIR-011 se corrigió el alcance para excluir `backend/target/` de la enumeración, del escaneo y del contraste histórico. Los archivos fuente/configuración relevantes se siguen analizando.

El snapshot inicial de diez archivos se conserva sin regenerarlo. Las diferencias de contenido y de conjunto se informan como histórico, con rutas y cantidades. La revisión del diff, el alcance autorizado y los tests pertinentes acreditan las modificaciones posteriores. El histórico deja de exigir hashes iniciales inmutables para cada versión del proyecto. Un clon sin snapshot declara el contraste histórico no disponible y sigue comprobando fuentes, metadatos y archivos esenciales.

## Opciones de arranque del demo acordadas en AIR-012

| Contexto | Opción técnica y cobertura |
|---|---|
| Test existente | La propiedad de SpringBootTest excluye DataSourceAutoConfiguration solo en su contexto. AIR-011 verificó inicialización real, bean principal y ausencia de DataSources |
| Ejecución demo futura | Preferencia por perfil explícito demo, con exclusión DataSourceAutoConfiguration limitada a ese perfil y proveedores demo. Requerirá autorizar su archivo de configuración y validar el contexto/HTTP completo |
| Sesión demo de diagnóstico | Argumento de arranque limitado al proceso que excluya esa autoconfiguración, con contexto de fuentes demo. Debe declararse su alcance y comprobarse, sin convertirlo en configuración predeterminada |
| Persistencia futura | Perfil con PostgreSQL/configuración externa y pruebas de JPA/Flyway cuando haya caso durable aprobado |

AIR-012 documenta estas opciones. El test actual conserva su perfil Spring predeterminado y una propiedad local de prueba. El perfil de ejecución demo y sus archivos se crearán solo en una tarea autorizada. La política pública de endpoints necesita la preparación de AIR-016; el arranque actual por sí solo no acredita esa política.

## Agentes y verificación

El [manual de Codex](CODEX_SETUP.md) explica los siete roles, las cinco skills, la activación y la diferencia entre archivo configurado y agente invocado. Consulte [VALIDATION.md](VALIDATION.md) para las comprobaciones reales y sus límites. La sesión principal conserva la responsabilidad de revisar y cerrar una tarea con evidencia.
