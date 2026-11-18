# ADR-0006 — Cliente demo React con estado transitorio

- Estado: Aceptado e implementado en AIR-018, En revisión por verificación visual/teclado real/viewports pendiente.
- Fecha: 18/11/2026.
- Alcance: `frontend/`, sin modificar fuentes ni configuración versionada del backend.

## Contexto

El backend demo ya expone catálogo, búsqueda y comparación con contratos ratificados. AIR-018 autoriza la primera interfaz funcional, con una sola página, selección entre páginas de resultados y comparaciones cuya autoridad pertenece al servidor. El stack previsto incluía varias herramientas opcionales; su mención inicial no las convierte en dependencias necesarias.

## Decisión

Usar React, TypeScript y Vite con el plugin de React. Separar `src/api/` para tipos y cliente HTTP, componentes de presentación/formulario/comparación, utilidades pequeñas en `src/lib/`, composición en `App` y pruebas en `src/test/`. No crear módulos vacíos ni otro repositorio.

La navegación mediante anclas cubre las secciones de una sola página. Hooks de React y `fetch` con `AbortController` gestionan el estado transitorio y las peticiones; la cancelación y las respuestas tardías deben comprobarse. No se introduce React Router, Redux ni TanStack Query sin una necesidad de rutas, caché o coordinación adicional.

CSS propio con tokens de color marfil, navy y teal, espaciado y foco visible cubre el sistema visual inicial; Tailwind no aporta suficiente valor para justificar otra dependencia en este alcance. No se descargan tipografías, fotografías ni imágenes externas. Componentes semánticos y controles nativos reducen el coste de interacción accesible.

Vitest, React Testing Library, user-event, jest-dom y jsdom permiten verificar el comportamiento del cliente. TypeScript comprueba tipos y Oxlint convenciones. El generador oficial create-vite 9.3.4 trajo Oxlint; se conserva en lugar de añadir ESLint redundante. Las pruebas con respuestas simuladas se distinguen de la integración y navegación real; Playwright no se considera instalado ni ejecutado por estar previsto en el stack.

El navegador usa rutas relativas `/api` a través del proxy Vite local: frontend `127.0.0.1:5173` con puerto estricto, destino backend `127.0.0.1:8080`. Si el proxy conserva `Origin`, el arranque backend permite únicamente `http://127.0.0.1:5173` en su allowlist local. Esto no abre comodines, credenciales ni CORS global, y no modifica archivos backend. No se añaden claves ni URL externa seleccionable desde el navegador.

## Consecuencias y límites

El cliente conserva consulta canónica, versión y selección; no recalcula rankings, dinero ni grupos del motor. La complejidad de estado sigue siendo local y debe revisarse si aparecen nuevas rutas o casos reales de caché. Las versiones realmente instaladas y los comandos ejecutados se registran en el [inventario](../developer-guide/TECHNOLOGY_INVENTORY.md) y [VALIDATION](../developer-guide/VALIDATION.md), no se deducen de esta decisión. La evidencia automatizada incluye componentes y cliente, integración HTTP real y App montada en jsdom con HTTP real. Ningún nivel se declara E2E de navegador; el bloqueo técnico de la herramienta deja AIR-018 En revisión y mantiene su seguimiento.

Vite y su proxy son herramientas locales de desarrollo. Este ADR no define publicación, cabeceras de producción, TLS, despliegue, usuarios, persistencia ni proveedores reales. La guía operativa es [FRONTEND](../developer-guide/FRONTEND.md).
