---
name: task-planning
description: "Planificar una tarea AIR o un cambio importante de Airwise, con dependencias, alcance, criterios y evidencia; no para cambios mecánicos triviales."
---

# Planificar trabajo de Airwise

Lee `AGENTS.md`, `planning/ROADMAP.md`, la tarea de `planning/BACKLOG.md` y los ADR pertinentes desde la raíz del repositorio.

1. Contrasta el objetivo con el estado real y la fase autorizada. Si es una mejora no aprobada, regístrala en `planning/IMPROVEMENTS.md` sin comenzar su implementación.
2. Identifica dependencias, límites, responsable funcional y archivos. Para cambios importantes redacta un plan breve con verificaciones observables y riesgos; una incertidumbre no se presenta como decisión aprobada.
3. Elige ejecución directa o delegación selectiva. Si delegas, define entradas, salida esperada y propiedad de archivos; espera e integra las partes dependientes antes de revisarlas.
4. Aplica las transiciones del backlog. Una tarea bloqueada debe explicar qué falta; no declares completado lo que carece de evidencia.

Entrega: objetivo, AIR, alcance, pasos, validación y criterio de cierre. La planificación no autoriza cambios del backend, llamadas externas ni pasar de fase por sí sola.
