# Guía de desarrollo inicial

La raíz del repositorio contiene instrucciones, planificación, configuración de agentes y documentación. El único proyecto de aplicación presente es `backend/`, generado e intacto. Consulte el [inventario](TECHNOLOGY_INVENTORY.md) antes de elegir versiones o comandos.

## Empezar una tarea

Use [CONTRIBUTING.md](../../CONTRIBUTING.md), seleccione una tarea del [backlog](../../planning/BACKLOG.md) y lea el ADR afectado. La tarea actual termina en organización; no se inicia la siguiente fase automáticamente.

El [roadmap](../../planning/ROADMAP.md) ordena dependencias. `IMPROVEMENTS.md` conserva ideas no aprobadas. Un plan no es autorización para tocar la base del backend o ejecutar proveedores.

## Comandos disponibles y comprobados

Desde PowerShell en la raíz:

```powershell
java -version
mvn -version
py -3.14 scripts/validate_repository.py
git status --short
```

Python 3.11+ permite analizar TOML con biblioteca estándar. El script verifica documentación, metadatos, enlaces, backlog y preservación de la base si existe la instantánea local. No instala dependencias ni accede a servicios.

## Comandos existentes pero todavía no validados

`backend/mvnw.cmd` existe y declara Maven 3.10.0. En una tarea futura autorizada, desde `backend/`, se podrá comprobar primero `./mvnw.cmd -version`, resolver las dependencias y evaluar `./mvnw.cmd test`. Se documentan como **pasos pendientes**, no como instrucciones ya verificadas ni como parte ejecutada de esta fase. Pueden descargar herramientas y necesitar servicios/configuración de test.

No hay un comando raíz `mvn verify`, scripts npm, servidor de frontend, Compose ni pipeline que ejecutar. No se debe inventar una URL local, un usuario por defecto o un flujo de login para Airwise.

## Configuración y secretos

La aplicación generada solo declara su nombre. No añada conexiones, perfiles o credenciales a esa base sin la autorización indicada. Las variables de proveedores se documentan como nombres en la [política de API](../api/PROVIDER_POLICY.md); no se leen ni se usan en esta fase.

## Agentes y verificación

El [manual de Codex](CODEX_SETUP.md) explica los siete roles, las cinco skills, la activación y la diferencia entre archivo configurado y agente invocado. Consulte [VALIDATION.md](VALIDATION.md) para las comprobaciones reales y sus límites. La sesión principal conserva la responsabilidad de revisar y cerrar una tarea con evidencia.
