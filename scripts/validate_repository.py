"""Validate repository sources locally and report the historical phase-one snapshot."""

from __future__ import annotations

import hashlib
import json
import os
import re
import sys
import tomllib
from pathlib import Path
from urllib.parse import unquote, urlsplit
import xml.etree.ElementTree as ET


ROOT = Path(__file__).resolve().parents[1]
IGNORED = {".git", ".local", "node_modules", "__pycache__"}
SKILLS = {"task-planning", "api-integration", "database-migrations", "testing-and-review", "documentation-sync"}
AGENTS = {"backend", "frontend", "database", "security_qa", "devops", "documentation"}
ERRORS: list[str] = []


def check(condition: bool, message: str) -> None:
    if not condition:
        ERRORS.append(message)


def is_ignored(path: Path) -> bool:
    """Exclude tool state and backend build output while retaining source directories."""
    parts = path.relative_to(ROOT).parts
    return bool(IGNORED.intersection(parts)) or parts[:2] == ("backend", "target")


def files() -> list[Path]:
    return sorted(p for p in ROOT.rglob("*") if p.is_file() and not is_ignored(p))


def metadata(path: Path) -> dict[str, str]:
    """Parse only the documented scalar subset used here, not arbitrary YAML."""
    text = path.read_text(encoding="utf-8")
    match = re.match(r"\A---\n(.*?)\n---(?:\n|$)", text, re.S)
    if not match:
        raise ValueError("falta frontmatter delimitado")
    result: dict[str, str] = {}
    for line in match.group(1).splitlines():
        key, sep, value = line.partition(":")
        if not sep or not re.fullmatch(r"[a-z-]+", key) or key in result:
            raise ValueError("clave de frontmatter inválida o duplicada")
        value = value.strip()
        if value.startswith('"'):
            parsed = json.loads(value)  # JSON quoted strings are valid YAML scalars.
            if not isinstance(parsed, str):
                raise ValueError("el metadato debe ser texto")
            result[key] = parsed
        elif re.fullmatch(r"[a-z0-9]+(?:-[a-z0-9]+)*", value):
            result[key] = value
        else:
            raise ValueError("fuera del subconjunto YAML escalar admitido por este validador")
    return result


def validate_config() -> None:
    config = tomllib.loads((ROOT / ".codex/config.toml").read_text(encoding="utf-8"))
    check(set(config) == {"agents"}, "Config Codex: configuración inesperada")
    check(config["agents"] == {"enabled": True, "max_concurrent_threads_per_session": 3}, "Config Codex: claves/valores de agentes inesperados")
    names = []
    for path in (ROOT / ".codex/agents").glob("*.toml"):
        agent = tomllib.loads(path.read_text(encoding="utf-8"))
        check(set(agent) == {"name", "description", "developer_instructions"}, f"Campos de agente no previstos: {path.name}")
        check(all(isinstance(value, str) and value.strip() for value in agent.values()), f"Agente sin instrucciones: {path.name}")
        check("AGENTS.md" in agent.get("developer_instructions", ""), f"Agente sin acuerdo compartido: {path.name}")
        names.append(agent.get("name"))
    check(len(names) == 6 and set(names) == AGENTS, "Deben existir seis nombres de especialistas únicos")
    print(f"Codex TOML: {len(names)} especialistas; sintaxis/campos verificados, invocación no inferida")


def validate_skills() -> None:
    paths = list((ROOT / ".agents/skills").glob("*/SKILL.md"))
    names = []
    for path in paths:
        data = metadata(path)
        check(set(data) == {"name", "description"}, f"Metadatos de skill inesperados: {path.parent.name}")
        name = data.get("name", "")
        check(name == path.parent.name and len(name) < 64 and bool(re.fullmatch(r"[a-z0-9]+(?:-[a-z0-9]+)*", name)), f"Nombre de skill inválido: {path.parent.name}")
        description = data.get("description", "")
        check(0 < len(description) <= 1024 and "<" not in description and ">" not in description, f"Descripción inválida: {path.parent.name}")
        check("[TODO:" not in path.read_text(encoding="utf-8"), f"Scaffold sin terminar: {path.parent.name}")
        names.append(name)
    check(len(paths) == 5 and set(names) == SKILLS, "Deben existir exactamente las cinco skills previstas")
    for path in (ROOT / ".github/ISSUE_TEMPLATE").glob("*.md"):
        data = metadata(path)
        check(bool(data.get("name")) and bool(data.get("about")), f"Plantilla sin identificación: {path.name}")
    print(f"Skills: {len(paths)}; subconjunto YAML y metadatos verificados sin dependencias")


def validate_links(repository_files: list[Path]) -> None:
    count = 0
    for path in repository_files:
        if path.suffix.lower() != ".md" or "backend" in path.relative_to(ROOT).parts:
            continue
        text = re.sub(r"```.*?```", "", path.read_text(encoding="utf-8"), flags=re.S)
        for target in re.findall(r"(?<!!)\[[^\]]+\]\(([^)]+)\)", text):
            target = target.strip().split(' "', 1)[0].strip("<>")
            url = urlsplit(target)
            if url.scheme or not url.path:
                continue
            destination = (path.parent / unquote(url.path)).resolve()
            check(destination.is_relative_to(ROOT) and destination.exists(), f"Enlace roto/fuera del repo: {path.relative_to(ROOT)} -> {target}")
            count += 1
    print(f"Enlaces locales: {count} destinos comprobados (sin consultar URLs externas)")


def validate_backlog() -> None:
    text = (ROOT / "planning/BACKLOG.md").read_text(encoding="utf-8")
    tasks = re.findall(r"^### (AIR-\d{3}) — ([^\n]+)\n(.*?)(?=^### |\Z)", text, re.M | re.S)
    ids = [task[0] for task in tasks]
    check(len(ids) == len(set(ids)) and bool(ids), "IDs AIR ausentes o duplicados")
    states = {"Propuesta", "Pendiente", "En curso", "En revisión", "Bloqueada", "Hecha", "Cancelada"}
    for task_id, _, body in tasks:
        for field in ("Descripción", "Prioridad", "Estado", "Responsable", "Dependencias", "Aceptación", "Evidencia"):
            check(bool(re.search(rf"^- {field}: .+", body, re.M)), f"{task_id}: falta {field}")
        state = re.search(r"^- Estado: (.+)\.", body, re.M)
        check(state is not None and state.group(1) in states, f"{task_id}: estado inválido")
        deps = re.search(r"^- Dependencias: (.+)", body, re.M)
        if deps:
            for dep in re.findall(r"AIR-\d{3}", deps.group(1)):
                check(dep in ids and dep != task_id, f"{task_id}: dependencia desconocida o circular directa")
        if state and state.group(1) == "Hecha":
            evidence = re.search(r"^- Evidencia: (.+)", body, re.M)
            check(evidence is not None and "pendiente de cerrar" not in evidence.group(1).lower(), f"{task_id}: evidencia de cierre pendiente")
    print(f"Backlog: {len(tasks)} tareas con campos, estados y dependencias comprobados")


def validate_backend() -> None:
    required_sources = (
        "pom.xml", "mvnw", "mvnw.cmd", ".mvn/wrapper/maven-wrapper.properties",
        "src/main/java/com/airwise/backend/BackendApplication.java",
        "src/main/resources/application.properties",
        "src/test/java/com/airwise/backend/BackendApplicationTests.java",
    )
    for relative in required_sources:
        check((ROOT / "backend" / relative).is_file(), f"Falta archivo esencial del backend: {relative}")
    ns = {"m": "http://maven.apache.org/POM/4.0.0"}
    pom = ET.parse(ROOT / "backend/pom.xml").getroot()
    check(pom.findtext("m:parent/m:version", namespaces=ns) == "4.1.2", "El parent del backend difiere del inventario inicial")
    check(pom.findtext("m:properties/m:java.version", namespaces=ns) == "21", "Java del backend difiere del inventario inicial")
    check(len(pom.findall("m:dependencies/m:dependency", ns)) == 15, "Número de dependencias directas distinto del inventario")
    actual = {
        p.relative_to(ROOT).as_posix(): hashlib.sha256(p.read_bytes()).hexdigest()
        for p in (ROOT / "backend").rglob("*")
        if p.is_file() and not is_ignored(p)
    }
    print(f"Backend: {len(actual)} archivos fuente/configuración enumerados; POM inspeccionado")
    baseline = ROOT / ".local/backend-phase1-baseline.json"
    if not baseline.exists():
        print("Histórico fase 1: contraste NO COMPROBADO (sin snapshot local)")
        return
    previous = json.loads(baseline.read_text(encoding="utf-8-sig"))
    expected = {row["path"]: row["sha256"] for row in previous}
    changed = sorted(path for path in actual.keys() & expected.keys() if actual[path] != expected[path])
    added = sorted(actual.keys() - expected.keys())
    removed = sorted(expected.keys() - actual.keys())
    unchanged = len(actual.keys() & expected.keys()) - len(changed)
    print(f"Histórico fase 1: {len(expected)} archivos registrados; {unchanged} idénticos, "
          f"{len(changed)} modificados, {len(added)} nuevos y {len(removed)} ausentes")
    for status, paths in (("modificado", changed), ("nuevo", added), ("ausente", removed)):
        for path in paths:
            print(f"Histórico informativo ({status}): {path}")
    print("Las diferencias históricas requieren revisión del alcance autorizado y su evidencia en el backlog")


def validate_safety(repository_files: list[Path]) -> None:
    known_secrets = [value for name in ("TRAVELPAYOUTS_API_TOKEN", "SERPAPI_API_KEY") if (value := os.environ.get(name))]
    for path in repository_files:
        try:
            text = path.read_text(encoding="utf-8-sig")
        except (UnicodeError, OSError):
            continue
        check(not any(secret in text for secret in known_secrets), f"Posible credencial del entorno en {path.relative_to(ROOT)}; valor omitido")
        private_markers = ("-----BEGIN " + "PRIVATE KEY-----", "-----BEGIN " + "RSA PRIVATE KEY-----")
        check(not any(marker in text for marker in private_markers), f"Posible clave privada en {path.relative_to(ROOT)}; contenido omitido")
    preserved_empty = 0
    for path in ROOT.rglob("*"):
        if path.is_dir() and not is_ignored(path):
            if not any(path.iterdir()):
                if path.relative_to(ROOT).parts[0] == "backend":
                    preserved_empty += 1
                else:
                    check(False, f"Directorio vacío creado fuera de la base preservada: {path.relative_to(ROOT)}")
    print(f"Directorios vacíos del backend generado conservados: {preserved_empty}; no se exige eliminarlos")
    print(f"Seguridad: búsqueda de valores de proveedor disponibles en entorno ({len(known_secrets)}), marcadores de claves privadas y directorios vacíos")
    print("Límite: no es un escáner general de secretos ni valida configuración de seguridad de producto")


def main() -> int:
    try:
        required = ["AGENTS.md", "README.md", "CONTRIBUTING.md", "SECURITY.md", ".gitignore", ".codex/config.toml",
                    "planning/ROADMAP.md", "planning/BACKLOG.md", "planning/IMPROVEMENTS.md", "planning/CHANGELOG.md",
                    "docs/developer-guide/VALIDATION.md", "docs/developer-guide/CODEX_SETUP.md"]
        for name in required:
            check((ROOT / name).is_file(), f"Falta {name}")
        repository_files = files()
        validate_config()
        validate_skills()
        validate_links(repository_files)
        validate_backlog()
        validate_backend()
        validate_safety(repository_files)
    except (OSError, ValueError, ET.ParseError, KeyError, TypeError) as error:
        ERRORS.append(f"Validación interrumpida: {type(error).__name__}; revise los formatos locales sin publicar datos sensibles")
    for error in ERRORS:
        print("ERROR: " + error)
    print("RESULTADO: " + ("CORRECTO" if not ERRORS else f"{len(ERRORS)} error(es)"))
    return 1 if ERRORS else 0


if __name__ == "__main__":
    sys.exit(main())
