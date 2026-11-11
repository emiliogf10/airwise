# Validación de la fase 1

Fecha: **2026-11-11**. Alcance: organización, instrucciones y documentación. No se implementa producto, no se modifica backend/ ni se llama a proveedores.

## Estado actualizado antes del primer commit

Actualización del **2026-11-11**, posterior al cierre inicial de AIR-001 a AIR-005. Se conservan más abajo sus comandos, resultados y limitaciones históricos.

| Comprobación | Resultado verificado |
|---|---|
| AIR-006 | Completada y marcada Hecha en el backlog; catálogo efectivo y prueba acotada de delegación real |
| Backend Agent | Invocación mediante `collaboration.spawn_agent` con `agent_type="backend"`; hilo `/root/air006_backend_readonly` completado, informe propio y dos lecturas con código 0. Mecanismo y trazabilidad en [CODEX_SETUP.md](CODEX_SETUP.md) |
| Otros cinco especialistas | `frontend`, `database`, `security_qa`, `devops` y `documentation` detectados en el selector con descripciones coincidentes con sus TOML. AIR-006 no los invocó todos; `security_qa` se ejecutó después para la revisión previa al commit |
| Skills | Las cinco disponibles en el catálogo de la sesión; sus archivos y metadatos comprobados por el validador local |
| Revisión de seguridad previa al commit | Completada por `security_qa` en `/root/precommit_security_readonly`: 47 candidatos, sin secretos, credenciales ni datos privados detectados; PoC limitadas a resúmenes agregados. Escaneo y exclusiones con código 0 (`chunk_id=c47667`, `f67906`, `1030ea`, `8828d5`). Revisión acotada, sin garantía absoluta de ausencia de secretos |
| Correcciones preventivas | Añadidas diez exclusiones al [.gitignore raíz](../../.gitignore), conservando todas las anteriores; contrastadas mediante rutas sintéticas sin crear archivos |
| Validación del repositorio | `py -3.14 scripts/validate_repository.py`: código 0 y RESULTADO CORRECTO; seis especialistas TOML, cinco skills, enlaces internos y 17 tareas comprobados; diez archivos backend preservados mediante SHA-256 |
| Construcción del backend | Compilación, tests y arranque todavía NO EJECUTADOS. Continúan pendientes en AIR-011; no se modifica POM, Java, Spring, wrapper ni `backend/.gitignore` |

Las nuevas reglas `*.tmp`, `*.temp`, `*.bak`, `*~`, `*.swp`, `credentials.json`, `secrets.json`, `id_rsa` e `id_ed25519` se aplican a esos nombres o sufijos en cualquier nivel. `reports/private/` solo protege esa ruta desde la raíz; no excluye todos los informes ni carpetas homónimas dentro de otros directorios. Los documentos privados deben guardarse en `.local/` o en esa ubicación protegida. `.env.example` sigue siendo una excepción deliberada y debe contener solo ejemplos sin secretos.

Se comprobaron exclusiones mediante `git check-ignore -z --no-index --verbose --non-matching --stdin`, con rutas sintéticas ignorables y controles de archivos legítimos. Los 47 candidatos originales siguen visibles, incluidos los nueve archivos versionables de Initializr, configuración compartida de Codex y cinco skills. `backend/HELP.md` conserva su exclusión generada; no existe un repositorio Git anidado en backend. Las excepciones de `target` bajo fuentes de Initializr se mantienen expresamente.

`git status --short --untracked-files=all` confirma los candidatos sin seguimiento y el índice vacío; `git diff --check` termina sin errores. Como ese diff no cubre archivos sin seguimiento, se revisaron también los cambios contra el contenido capturado al inicio y los espacios finales de los documentos modificados. Los enlaces internos se comprobaron además contra el conjunto candidato, para evitar destinos presentes solo en archivos ignorados. No se consultaron URLs externas.

No se ejecutaron `git add`, commits ni push, instalaciones, cambios globales, desarrollo de producto o llamadas a proveedores. Los cambios son preventivos y documentales; no cierran ninguna tarea de producto.

## Evidencia histórica inicial de AIR-001 a AIR-005

La tabla y los párrafos de este apartado describen el cierre inicial, anterior a AIR-006 y a la revisión previa al commit; su estado de agentes y sus recuentos de enlaces son históricos.

| Comprobación | Estado y evidencia |
|---|---|
| Inspección del backend | Ejecutada: POM, estructura, clase de arranque, test, configuración y wrapper; inventario en TECHNOLOGY_INVENTORY.md |
| Conservación del backend | Ejecutada: los mismos 10 archivos, hashes SHA-256 idénticos y sin construcción dentro de backend/ |
| Herramientas | Java Temurin 21.0.12, Maven 3.9.16, Python 3.14.7; wrapper declara otra distribución, no ejecutada |
| Extensión / binario | Metadatos locales de openai.chatgpt 26.51106.51428; codex-cli 0.167.0-alpha.4.1 |
| Documentación oficial Codex | Consultada; fuentes y mecanismo en CODEX_SETUP.md |
| Configuración de arranque Codex | Comando nativo estricto terminó con código 0; no prueba registro de cada especialista |
| Instrucciones y skills visibles | Renderizador local terminó con código 0 tras autorización de acceso temporal; AGENTS raíz y catálogo con nombre/descripción de las cinco skills presentes; rutas expuestas mediante alias del catálogo |
| Agentes ejecutables | Seis archivos configurados; selección/invocación de esos agentes no comprobada. Coordinador es la sesión principal |
| Validador oficial de skills | Intentado: falta PyYAML; no instalado. Validación local del subconjunto de metadatos preparada |
| Metadatos, enlaces y backlog | Ejecutado `scripts/validate_repository.py`: RESULTADO CORRECTO; 6 especialistas TOML, 5 skills, 59 enlaces locales y 17 tareas AIR |
| Secretos y directorios vacíos | Sin coincidencias de los 2 valores de proveedores disponibles en el entorno ni marcadores de claves privadas; sin nuevos directorios vacíos. Se preservaron los 3 vacíos de backend/. No es auditoría general |
| Compilación/tests/arranque Spring | NO EJECUTADOS: fuera del alcance organizativo, backend conservado; AIR-011 |
| Frontend, BD, Docker y CI | No generados; no se presentan comandos o servicios como disponibles |
| APIs de vuelos | Cero llamadas durante esta fase; sin consumo de créditos |
| Publicación y Git | Sin commits, push ni publicación realizados por esta tarea; rama inicial sin commits al inspeccionar |

Los temporales de evidencia se guardan en `.local/`, excluida de Git. El renderizador nativo falló inicialmente por acceso denegado a temporales fuera del workspace; se repitió únicamente tras autorización específica. No se modificó configuración global ni se ejecutó una tarea adicional de inferencia.

La validación local se ejecutó sobre archivos aún no añadidos a Git. `git diff --check` terminó sin errores, pero no verifica por sí solo archivos nuevos sin seguimiento; sus rutas, metadatos, enlaces y coherencia se comprobaron con el script. No se crearon commits. En aquel cierre, AIR-001 a AIR-005 estaban cerradas con evidencia y AIR-006 y las tareas de producto permanecían pendientes. El estado posterior de AIR-006 se recoge en la actualización anterior; las tareas de producto siguen pendientes.

## Reproducir las comprobaciones documentales

```powershell
py -3.14 scripts/validate_repository.py
git diff --check
git status --short
```

Requiere Python 3.11+ para TOML. El script no usa red, no inicia Spring y no instala paquetes. Si la instantánea local del backend no existe en otro clon, indica que no puede comprobar la preservación histórica; no la inventa. No se debe regenerar para ocultar cambios. Este snapshot pertenece al cierre de la fase 1; una fase futura que autorice cambios de backend deberá revisar ese criterio explícitamente.

## Árbol de entrega

```text
airwise/
├── AGENTS.md
├── README.md
├── CONTRIBUTING.md
├── SECURITY.md
├── .gitignore
├── .codex/
│   ├── config.toml
│   └── agents/
│       ├── backend.toml
│       ├── frontend.toml
│       ├── database.toml
│       ├── security-qa.toml
│       ├── devops.toml
│       └── documentation.toml
├── .agents/skills/
│   ├── task-planning/SKILL.md
│   ├── api-integration/SKILL.md
│   ├── database-migrations/SKILL.md
│   ├── testing-and-review/SKILL.md
│   └── documentation-sync/SKILL.md
├── .github/
│   ├── ISSUE_TEMPLATE/bug_report.md
│   ├── ISSUE_TEMPLATE/feature_request.md
│   └── pull_request_template.md
├── backend/                         # Los 10 archivos iniciales, sin modificar
├── planning/
│   ├── ROADMAP.md
│   ├── BACKLOG.md
│   ├── IMPROVEMENTS.md
│   └── CHANGELOG.md
├── docs/
│   ├── user-manual/README.md
│   ├── developer-guide/
│   │   ├── README.md
│   │   ├── TECHNOLOGY_INVENTORY.md
│   │   ├── CODEX_SETUP.md
│   │   └── VALIDATION.md
│   ├── architecture/
│   │   ├── OVERVIEW.md
│   │   └── PROVIDER_EVIDENCE.md
│   ├── api/PROVIDER_POLICY.md
│   ├── security/BASELINE.md
│   └── decisions/
│       ├── README.md
│       ├── 0001-preserve-generated-backend.md
│       ├── 0002-modular-monolith.md
│       └── 0003-provider-boundaries.md
└── scripts/validate_repository.py
```

`.git/` y `.local/` no se incluyen en el árbol de archivos de entrega. No se crearon directorios vacíos ni un proyecto de aplicación nuevo. Se conservaron los tres directorios vacíos del backend generado (`static`, `templates` y `db/migration`) por la instrucción de mantener esa base intacta.
