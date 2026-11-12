# Contribuir a Airwise

Antes de editar, consulta [AGENTS.md](AGENTS.md), el [backlog](planning/BACKLOG.md), el [roadmap](planning/ROADMAP.md) y los ADR relevantes. Cada tarea necesita alcance autorizado; aceptar un diseño no autoriza implementar negocio ni cambiar la base generada de `backend/`. AIR-010 es exclusivamente documental.

## Flujo de trabajo

1. Identifica una tarea AIR con alcance autorizado, dependencias y criterios observables. Una idea en mejoras todavía no es una tarea aprobada.
2. Para cambios importantes, redacta un plan breve y acuerda propiedad de archivos si hay delegación. Separa propuestas y decisiones aceptadas.
3. Realiza un cambio pequeño. Respeta las convenciones existentes y no añadas dependencias o arquitectura por costumbre. POM, wrapper y configuración base requieren la autorización indicada en AGENTS.md.
4. Ejecuta verificaciones proporcionales; registra comando, resultado y limitaciones. Para proveedores usa fixtures sintéticas y HTTP local. No conviertas una inspección en una afirmación de compilación o tests ejecutados.
5. Sincroniza documentos afectados, backlog y changelog. Aplica la Definition of Done antes de proponer el cierre.

## Convenciones

- Documentación y mensajes de producto: español claro; código y símbolos: nombres coherentes con su módulo. Conserva el formato del código existente.
- Commits previstos: `tipo(ámbito): descripción`, con tipos `docs`, `feat`, `fix`, `test`, `refactor`, `build` o `chore`. Incluye el AIR cuando aporte trazabilidad. Ejemplo de formato: `docs(planning): delimitar AIR-010`.
- No hagas commits, push o publicación sin petición expresa. No hay una política de ramas protegidas aplicada ni CI configurada; se definirán cuando exista un repositorio remoto autorizado.
- Conserva Javadoc existente. Documenta invariantes, contratos y semántica no evidente; no repitas getters o código obvio.
- Desconocido no equivale a cero. No mezcles un precio observado, una estimación y un resultado reciente como si fueran una oferta confirmada.

## Revisión y aceptación

La PR debe explicar problema, cambio, AIR, evidencia y riesgos. Usa la [plantilla](.github/pull_request_template.md). Revisa alcance, corrección, seguridad, pruebas, errores y documentación; las comprobaciones automáticas futuras no sustituyen la revisión.

Un cambio se acepta cuando satisface sus criterios y DoD, no expone secretos, no amplía silenciosamente el alcance y declara lo no comprobado. Las tareas exclusivamente documentales se verifican con enlaces, formatos y coherencia; no requieren arrancar una aplicación que no se cambió.

La validación local de esta fase es `py -3.14 scripts/validate_repository.py` con Python 3.11+. No crea servicios ni llama a APIs. Los comandos de backend y frontend pendientes se detallan en la [guía](docs/developer-guide/README.md).
