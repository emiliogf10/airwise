# Configuración local de Codex

Comprobado el **11/11/2026**, incluida la validación real de AIR-006 descrita más abajo. No se modificó la configuración global ni se fijaron modelo, proveedor, sandbox o política de aprobación.

## Fuentes y entorno real

Durante la preparación inicial se consultó la documentación oficial de [subagentes](https://learn.chatgpt.com/docs/agent-configuration/subagents), [skills](https://learn.chatgpt.com/docs/build-skills), [AGENTS.md](https://learn.chatgpt.com/docs/agent-configuration/agents-md), [configuración](https://learn.chatgpt.com/docs/config-file/config-basic) y [referencia](https://learn.chatgpt.com/docs/config-file/config-reference). AIR-006 se verificó con archivos y herramientas de esta sesión, sin nuevas consultas externas.

Metadatos locales: extensión `openai.chatgpt` **26.51106.51428**; ejecutable incluido **codex-cli 0.167.0-alpha.4.1**. El manifiesto declara VS Code `^1.96.2`; la versión del editor en ejecución no se verificó. No se instaló ni actualizó Codex.

La documentación actual admite archivos TOML independientes en `.codex/agents/`, con `name`, `description` y `developer_instructions`. No se inventaron formatos de otros asistentes ni comandos `/backend`. Los permisos se heredan de la sesión principal; las instrucciones de rol no crean un sandbox separado.

## Siete roles y su mecanismo

| Rol | Configuración | Uso previsto y estado comprobado |
|---|---|---|
| Orchestrator Agent | [AGENTS.md](../../AGENTS.md) | Agente principal: planificación, coordinación, revisión y cierre. Sus instrucciones aparecieron en el renderizador local de Codex; no es otro subproceso |
| Backend Agent | `.codex/agents/backend.toml`, nombre `backend` | Invocaciones reales completadas en AIR-006 y análisis de AIR-010 |
| Frontend Agent | `.codex/agents/frontend.toml`, nombre `frontend` | Invocación real de solo lectura completada en AIR-010 |
| Database Agent | `.codex/agents/database.toml`, nombre `database` | Invocación real de solo lectura completada en AIR-010 |
| Security & QA Agent | `.codex/agents/security-qa.toml`, nombre `security_qa` | Invocaciones reales en revisión previa al commit y análisis de AIR-010 |
| DevOps Agent | `.codex/agents/devops.toml`, nombre `devops` | Expuesto en `spawn_agent.agent_type`; ejecución individual no probada |
| Documentation Agent | `.codex/agents/documentation.toml`, nombre `documentation` | Expuesto en `spawn_agent.agent_type`; ejecución individual no probada |

Se omiten modelo y esfuerzo para heredar los elegidos en la sesión. `.codex/config.toml` habilita agentes y limita a tres hilos secundarios concurrentes; no lanza especialistas por sí mismo. Es una capacidad máxima, no una obligación de delegar ni un límite global que pueda superar requisitos del entorno.

Para utilizarlos en una sesión nueva del proyecto confiable, pida explícitamente, por ejemplo: «Delega una revisión de AIR-010 al agente personalizado `backend`; solo lectura, sin tocar el backend. Integra sus observaciones antes de cerrar». Sustituya el nombre por `frontend`, `database`, `security_qa`, `devops` o `documentation` según la tarea. El coordinador define límites y archivos; no invoca los seis para cualquier solicitud.

**Los seis especialistas están disponibles para selección en el catálogo ejecutable de esta sesión.** Tras AIR-010 hay ejecución real comprobada de `backend`, `database`, `frontend` y `security_qa`; `devops` y `documentation` siguen sin probar individualmente. AIR-006 solo probó backend; el historial se conserva abajo. En un cliente sin selector, aplicar manualmente un TOML sigue siendo uso de instrucciones de rol y no demuestra delegación real.

## Skills del repositorio

| Skill | Cuándo usarla | Invocación explícita en Codex |
|---|---|---|
| task-planning | Plan de tarea AIR o cambio importante | `$task-planning` |
| api-integration | Contrato, coste y semántica de un adaptador autorizado | `$api-integration` |
| database-migrations | Diseño/revisión de persistencia y Flyway autorizado | `$database-migrations` |
| testing-and-review | Verificación proporcional y revisión con evidencia | `$testing-and-review` |
| documentation-sync | Sincronización de documentación con decisiones/código | `$documentation-sync` |

Ubicación: `.agents/skills/<nombre>/SKILL.md`. Cada archivo tiene `name`, `description` y procedimiento; no se generaron carpetas de recursos vacías ni se copiaron instrucciones completas de roles. Se dejó la selección implícita predeterminada según descripción. Las skills no autorizan automáticamente acciones externas ni cambios de fase.

La documentación de Codex permite mencionarlas con `$` y consultarlas mediante `/skills`. El renderizador local mostró las cinco en su catálogo con nombre y descripción y rutas abreviadas mediante alias. En AIR-006, el catálogo de skills recibido por el coordinador volvió a incluir exactamente las cinco, con rutas bajo el alias `r8` del workspace; además se leyeron `task-planning`, `testing-and-review` y `documentation-sync`. El validador local verificó los cinco archivos y sus metadatos. Esto acredita descubrimiento en esta sesión; no se verificó visualmente el selector de la extensión ni se ejecutaron los procedimientos de API o migraciones. No se modificó la confianza del proyecto.

## Evidencia inicial, anterior a AIR-006

- Se inspeccionaron manifiesto de extensión, versión del binario y su ayuda real.
- `codex --strict-config app-server --print-startup-config` devolvió código 0 sin iniciar el servidor. Es evidencia de aceptación de configuración de arranque; no demuestra por sí sola que se cargue cada capa del proyecto confiable ni un registro de agentes.
- `codex debug prompt-input` falló primero por acceso denegado del sandbox a temporales de Codex. Tras autorización específica, devolvió código 0; se comprobó presencia del AGENTS raíz y de las cinco skills sin ejecutar una tarea de modelo. La salida íntegra se conservó solo en `.local/`, ignorada por Git; no se publicó contexto personal.
- Buscar nombres comunes en ese renderizado no verifica un agente personalizado. No aparecieron las descripciones exactas de los seis roles, y el renderizado no incluye un selector `agent_type`; por eso no se presenta como prueba de registro o invocación.
- El análisis TOML y la comprobación de los campos de cada rol se realizan con `scripts/validate_repository.py` y la biblioteca estándar de Python.
- El validador de skills incluido con `skill-creator` se intentó ejecutar, pero falta PyYAML en los Python inspeccionados. No se instaló nada. El validador local comprueba un subconjunto YAML deliberadamente simple: claves conocidas con nombres/valores de texto y descripciones JSON entrecomilladas, válidas como YAML. No se lo presenta como un parser YAML general ni como el validador oficial.

En aquella comprobación inicial no se inició un servidor Codex, sesión de inferencia adicional ni llamada a Travelpayouts o SerpApi. La limitación de agentes documentada en el informe inicial de fase 1 corresponde a ese momento; AIR-006 la actualiza con la siguiente evidencia.

## AIR-006: selección y delegación reales

Prueba realizada el **11/11/2026** desde la sesión coordinadora del workspace, con AIR-003 como dependencia satisfecha. Se conservaron `.codex/config.toml`, los seis TOML de roles y las cinco skills. No se crearon definiciones de agentes ni se inició otra tarea AIR.

### Catálogo, mecanismo y trazabilidad

El esquema efectivo de la herramienta `collaboration.spawn_agent` ofreció `backend`, `frontend`, `database`, `security_qa`, `devops` y `documentation`. Sus seis descripciones coinciden literalmente con los TOML inspeccionados; no se dedujo disponibilidad solo de la presencia de archivos.

El coordinador ejecutó `collaboration.spawn_agent` con `agent_type="backend"` y `task_name="air006_backend_readonly"`. Se heredaron modelo y esfuerzo, sin sobrescribirlos. La herramienta devolvió el identificador **`/root/air006_backend_readonly`**. Se delegó únicamente leer las fuentes de contexto exigidas y `backend/pom.xml`, con prohibición de escribir archivos, ejecutar Maven, compilar, instalar o llamar al exterior.

Evidencia observada en las herramientas de esta conversación:

- `spawn_agent` aceptó el rol personalizado y devolvió el hilo anterior.
- `collaboration.list_agents` mostró ese hilo primero en estado `running` y, después de su informe final, en estado `completed`.
- El especialista informó haber recibido instrucciones específicas de Backend Agent en un mensaje `developer`, incluida «No presupongas Spring Boot 3 ni una versión de JUnit no resuelta». Esa instrucción coincide con el TOML y no se incluyó en el encargo. El especialista no leyó `.codex/agents/backend.toml`.
- El hilo devolvió un informe final con el resultado de dos llamadas reales a `exec_command`, ambas con código 0: `chunk_id=3165b7` para AGENTS/backlog/roadmap y `chunk_id=c16188` para inventario/POM.

Comandos de solo lectura registrados por el especialista:

```powershell
Get-Content -Encoding UTF8 AGENTS.md; Get-Content -Encoding UTF8 planning/BACKLOG.md; Get-Content -Encoding UTF8 planning/ROADMAP.md
Get-Content -Encoding UTF8 docs/developer-guide/TECHNOLOGY_INVENTORY.md; Get-Content -Encoding UTF8 backend/pom.xml
```

Los identificadores permiten relacionar este resumen con la traza de la conversación; no son archivos ni enlaces públicos. La evidencia incluye un hilo independiente completado, su informe y los resultados de herramienta comunicados por él. No se presentó una lectura del POM por el coordinador como ejecución del especialista.

### Resultado de la inspección delegada

El especialista confirmó **Java 21**, parent **`org.springframework.boot:spring-boot-starter-parent:4.1.2`** y **15 dependencias directas**, coherentes con el [inventario del POM](TECHNOLOGY_INVENTORY.md):

- Siete con scope compile implícito: los starters Boot `actuator`, `data-jpa`, `flyway`, `security`, `validation`, `webmvc`, y `org.flywaydb:flyway-database-postgresql`.
- Dos de runtime: `org.springframework.boot:spring-boot-devtools` con `optional=true`, y `org.postgresql:postgresql`.
- Seis de test: los starters Boot `actuator-test`, `data-jpa-test`, `flyway-test`, `security-test`, `validation-test` y `webmvc-test`.

Plugin explícito: `org.springframework.boot:spring-boot-maven-plugin`. No hay versiones locales independientes de las dependencias ni del plugin; sus versiones efectivas no se resolvieron.

Antes de compilar, en una tarea posterior autorizada, habrá que comprobar JDK seleccionado, distribución y compatibilidad del wrapper, resolución del parent y dependencias, versión efectiva de JUnit Jupiter y requisitos de persistencia del test de contexto JPA/Flyway. Son comprobaciones pendientes, sin afirmar fallo ni éxito. No se ejecutó AIR-011.

### Verificación y cierre

El coordinador ejecutó `py -3.14 scripts/validate_repository.py`: código 0, **RESULTADO: CORRECTO**. Verificó sintaxis/campos de seis especialistas TOML, metadatos de cinco skills, enlaces locales, estados/dependencias de las 17 tareas y preservación de los diez archivos de `backend/` frente a la instantánea existente mediante SHA-256. Este chequeo estático complementa la prueba de ejecución; por sí solo no invoca agentes.

AIR-006 satisfizo su aceptación mediante catálogo efectivo, prueba acotada real de un especialista y descubrimiento de las cinco skills. No se observaron bloqueos de configuración, cliente ni entorno para esa prueba. Al cerrar AIR-006 quedaron sin probar los otros cinco; AIR-010 actualiza el estado de tres de ellos. La interfaz visual y otras sesiones/clientes siguen sin verificar.

No se modificó `backend/`, no se instalaron dependencias ni se ejecutaron compilaciones, tests de producto, APIs externas, cambios globales, commits o push. La única delegación fue el hilo de `backend` autorizado para AIR-006.

## Seguimiento AIR-010

Invocaciones reales mediante `collaboration.spawn_agent` con `agent_type` específico, sin sobrescribir modelo/esfuerzo. Los cuatro hilos devolvieron informes finales de solo lectura: `/root/air010_backend_design`, `/root/air010_database_design`, `/root/air010_frontend_design` y `/root/air010_security_design`. El coordinador fue el único escritor. No se invocaron DevOps ni Documentation por no necesitar otra especialidad. La trazabilidad y revisión integrada se registran en [VALIDATION.md](VALIDATION.md).
