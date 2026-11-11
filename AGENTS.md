# Acuerdos de trabajo de Airwise

- Lee `planning/BACKLOG.md`, `planning/ROADMAP.md` y el inventario de `docs/developer-guide/TECHNOLOGY_INVENTORY.md` antes de una tarea relevante. Trabaja solo dentro del alcance autorizado y del workspace; pide autorización para actuar fuera.
- Esta entrega es la fase 1 organizativa: no implementes negocio, generes proyectos, configures bases de datos ni llames a Travelpayouts o SerpApi. El presupuesto de API es 0 €. Una fase posterior necesita una petición expresa.
- `backend/` es un proyecto generado existente: conserva código, POM, dependencias, wrapper y configuración. Cualquier cambio de esa base requiere justificación técnica y autorización previa; no vuelvas a ejecutar Initializr.
- El agente principal actúa como Orchestrator: elige una tarea AIR, comprueba dependencias y criterios, delimita archivos y presenta un plan breve para cambios importantes. No conviertas una idea de `IMPROVEMENTS.md` en desarrollo autorizado.
- Delega selectivamente cuando aporte valor y la herramienta lo permita. Asigna una pregunta, límites y entrega concreta; evita escritores simultáneos sobre los mismos archivos. El coordinador integra, revisa y actualiza el backlog. No invoques los seis especialistas en cada petición.
- Los roles especializados están en `.codex/agents/`; los procedimientos en `.agents/skills/`. Si no hay registro ejecutable comprobado, usa sus instrucciones en el agente principal y comunica esa limitación.
- Arquitectura prevista: monolito modular y adaptadores de proveedores separados; capas de dominio, aplicación, infraestructura y API solo cuando aporten valor. No crees capas, módulos o abstracciones vacías.
- Distingue precio observado, resultado reciente, estimación y precio confirmado por un vendedor. Desconocido no equivale a cero; no inventes disponibilidad, equipaje, condiciones ni un viaje completo a partir de un solo trayecto.
- Nunca incorpores ni muestres credenciales, URLs autenticadas, payloads sensibles o datos personales. Usa configuración externa y errores sanitizados; conserva su causa operativa sin convertir fallos en listas vacías o datos demo silenciosos.
- Añade pruebas proporcionales al comportamiento cambiado y ejecuta las pertinentes. No llames a proveedores en tests. No afirmes que algo compila, pasa o está disponible sin evidencia ejecutada; separa estático, ejecutado y pendiente.
- Mantén cambios pequeños y convenciones de `CONTRIBUTING.md`. Respeta el Javadoc existente; añade Javadoc cuando explique contratos, invariantes o decisiones no evidentes.
- Actualiza documentación y ADR afectados de forma proporcional. Tras completar una tarea, registra comando/resultado o evidencia revisable en el backlog y aplica su Definition of Done.
- No cambies configuración global, instales herramientas globales, hagas commits, push, despliegues o publicaciones sin autorización expresa. No reduzcas controles del sandbox ni de aprobación.
