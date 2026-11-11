---
name: testing-and-review
description: "Verificar y revisar una tarea Airwise según su riesgo, seleccionando pruebas reales y registrando resultados sin consumir APIs de proveedores."
---

# Verificar lo que cambia

Lee los criterios de la tarea y `CONTRIBUTING.md`. Distingue comprobación estática, prueba ejecutada y resultado pendiente.

1. Identifica comportamiento afectado, regresiones plausibles y herramientas realmente presentes. No inventes scripts npm, versiones transitivas ni servicios de pruebas.
2. Elige la comprobación mínima que observe el comportamiento relevante: análisis de archivos para documentación; tests unitarios, de contrato o integración para lógica; pruebas de UI cuando exista frontend. No añadas tests que solo repitan la implementación.
3. Usa fixtures sintéticas y servidor local para proveedores; no consuma créditos ni requiera claves reales. En fase 1 no arranques Spring ni modifiques backend/ para resolver fallos.
4. Ejecuta los comandos aplicables dentro del alcance y registra salida/código de retorno. Si faltan dependencias o servicios, documenta la limitación; no conviertas el fallo en éxito ni alteres configuración para ocultarlo.
5. Revisa diff, secretos, errores, documentación y criterios. Delegar una revisión independiente puede ayudar si hay una pregunta concreta; no requiere invocar siempre a todos los roles.

Entrega: hallazgos priorizados, comprobaciones con resultados, límites y criterio de aceptación. Solo el coordinador integra la evidencia para cerrar la tarea AIR.
