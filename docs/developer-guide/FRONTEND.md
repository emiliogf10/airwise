# Frontend demo — AIR-018

AIR-018 implementa el cliente React/TypeScript/Vite en `frontend/`, integrado con el backend demo existente. **Estado: Hecha.** Se verificaron automatizaciones, navegación/teclado y responsive en navegador real; la [revisión de navegador](BROWSER_REVIEW.md) registra alcance, incidencia corregida y límites. Evidencia por nivel en [VALIDATION](VALIDATION.md).

## Fuentes de verdad y responsabilidades

El contrato HTTP permanece en [API_DESIGN](../api/API_DESIGN.md), sus tipos e invariantes en [NORMALIZED_CONTRACTS](../architecture/NORMALIZED_CONTRACTS.md) y las reglas de comparación en [TRAVEL_COST_ENGINE](../architecture/TRAVEL_COST_ENGINE.md). El cliente presenta resultados; el servidor valida, filtra, ordena, pagina, reconstituye selecciones y calcula clasificaciones/subtotales. No se duplican cálculos de dinero o recomendaciones en JavaScript.

La interfaz usa únicamente las tres operaciones demo existentes: catálogo, búsqueda y comparación. No introduce endpoints, autenticación, persistencia, proveedores reales, servicios de transporte ni presupuesto de API. El backend conserva sus fuentes, POM, Wrapper y configuración.

La estructura y las dependencias se justifican en [ADR-0006](../decisions/0006-demo-frontend.md): React/TypeScript/Vite, cliente HTTP separado, componentes por responsabilidad, estado con hooks y `fetch`, CSS propio y pruebas de comportamiento. Router, Redux, TanStack Query y Tailwind se aplazan al no ser necesarios para esta página; Playwright previsto no equivale a instalado.

## Conexión local y entorno

El [arranque demo](DEMO_RUNTIME.md) conserva su receta y límites: perfil explícito `demo`, bind en loopback y `-Dspring.devtools.restart.enabled=false` en la JVM antes del main. El servidor anuncia su puerto; no se deduce un puerto disponible de una captura histórica. La receta usa un classpath de verificación local, no un artefacto de producción.

La conexión de desarrollo usa rutas relativas `/api` mediante el proxy Vite de `127.0.0.1:5173` hacia `127.0.0.1:8080`, con puerto frontend estricto. El proxy conserva `Origin`, por lo que la allowlist del arranque backend debe contener exactamente `http://127.0.0.1:5173`; `localhost` es otra procedencia. No se modifica la política ni la configuración versionada del backend, no se permiten comodines o credenciales CORS.

Para conectar ambos procesos, en la receta Java independiente de [DEMO_RUNTIME](DEMO_RUNTIME.md) sustituya el argumento `--server.port=0` por `--server.port=8080` y añada `--airwise.demo.cors.allowed-origins=http://127.0.0.1:5173` a `$airwiseArguments` antes de escribir el archivo de argumentos. Mantenga el perfil `demo` y la propiedad JVM temprana de DevTools. Son opciones de ese proceso, no cambios en archivos backend. La integración HTTP de esta combinación se verificó en AIR-018; la receta presupone las clases y el classpath local ya compilados según la guía, no una instalación Java nueva.

Cualquier variable expuesta al navegador es pública: no admite claves, tokens ni URLs autenticadas. Nunca se solicitan credenciales de Travelpayouts o SerpApi. La conexión fija al proxy evita que la interfaz necesite una URL autenticada o un selector de proveedor.

## Instalación y comandos presentes

El proyecto se generó con create-vite 9.3.4; Node.js 24.21.0 y npm 11.7.0 fueron observados en esta sesión. `package.json` declara Node `^22.22.2 || ^24.15.0 || >=26.0.0`, intersección de requisitos de las herramientas instaladas; solo la versión observada se ha probado. Las dependencias locales y versiones resueltas están en el [inventario](TECHNOLOGY_INVENTORY.md); el lockfile se conserva. Con Node/npm disponibles, desde PowerShell en la raíz:

```powershell
Set-Location frontend
npm ci
npm run dev
```

`npm ci` reproduce el lockfile y puede necesitar red; no instala herramientas globalmente. La instalación inicial de AIR-018 se realizó con npm, sin atribuir todavía una ejecución de `npm ci`. Vite usa `http://127.0.0.1:5173` y rechaza otro puerto ocupado mediante `strictPort`. El backend debe arrancarse aparte en 8080 con el perfil y origen indicados arriba. Ctrl+C detiene cada proceso en su terminal.

Scripts declarados en `frontend/package.json`, ejecutables desde `frontend/`:

| Comando | Función y límite |
|---|---|
| `npm run dev` | Servidor Vite local con proxy `/api` al backend demo |
| `npm run typecheck` | `tsc -b --pretty false`; tipos estrictos |
| `npm run lint` | Oxlint; no ESLint |
| `npm test` | Vitest una vez en jsdom |
| `npm run test:watch` | Vitest en modo interactivo |
| `npm run test:integration` | Cliente TypeScript real contra ambos servicios demo locales; sin mocks y sin navegador |
| `npm run test:integration:ui` | App en jsdom y HTTP real a ambos servicios locales; opt-in, sin navegador |
| `npm run build` | TypeScript y build Vite en `frontend/dist/` |
| `npm run preview` | Servir el build estático en `127.0.0.1:4173`, sin proxy API configurado |

La presencia del script no prueba su resultado. Preview no es la receta de integración ni un despliegue de producción. No se necesitan variables de entorno para la conexión local actual; no se crea `.env` con secretos ni se configura una URL externa seleccionable. Si cambia el puerto del backend, la configuración fija y la allowlist se deben revisar explícitamente, sin abrir CORS.

La estructura presente separa responsabilidades:

```text
frontend/
├── package.json / package-lock.json
├── vite.config.ts / tsconfig*.json / .oxlintrc.json
├── index.html
├── scripts/verify-demo.mjs # Comprobación HTTP local explícita
├── scripts/verify-ui.mjs   # Integración UI/jsdom opt-in con HTTP local
└── src/
    ├── api/             # Tipos y cliente HTTP de las tres operaciones demo
    ├── components/      # Formularios, filtros, tarjetas, resultados, comparación y estados
    ├── lib/             # Formato/decimales y claves de consulta; sin motor monetario
    ├── test/            # Fixtures, cliente y componentes en jsdom
    ├── App.tsx          # Contexto de búsqueda, selección y composición
    ├── main.tsx
    └── styles.css        # Tokens y estilos del producto
```

`SearchForm`, `SearchFilters`, `FlightCard`, `SearchResults`, `ComparisonPanel` y `StatusPanel` contienen las piezas funcionales. `App` coordina catálogo, abortos, contexto/versión, reinicios y anuncios de estado. Los detalles usan controles nativos; no hay rutas vacías, login ni páginas de reserva. Las comprobaciones de esas responsabilidades se registran separadamente de este inventario de fuentes.

`src/test/` contiene cuatro archivos de pruebas (`app`, `client`, `comparison` y `local.integration`) y dos de soporte (`fixtures` y `setup`). `src/lib/` contiene `format.ts` y `search.ts`; no se conservan assets o pantallas del template.

## Estado de búsqueda y comparación

El catálogo publica versión, calendario, reloj simulado, monedas y aerolíneas admitidas. Las fechas se validan contra ese calendario; no se convierten los fixtures en vuelos para el día actual. Las páginas posteriores reutilizan la versión concreta.

Filtros y ordenación se envían al servidor antes de su paginación. La selección de dos a cuatro ofertas pertenece a la consulta canónica y versión: cambiar página, filtro u orden conserva sus IDs; iniciar otra consulta base o cambiar versión los limpia. Una observación parcial se presenta aparte y no se convierte en oferta seleccionable.

La comparación presenta grupos y recomendaciones del servidor, sus motivos y campos ausentes. No hay ganador global entre grupos, conversión monetaria ni total exacto. Los componentes terrestres, si se introducen, son estimaciones explícitas del usuario; el resultado se rotula como subtotal de componentes seleccionados con estimaciones. Desconocido se mantiene distinto de cero.

## Verificación y límites

Desde `frontend/`, con Node y los procesos backend en 8080/Vite en 5173 ya activos según la receta anterior, `npm run test:integration` ejecuta `scripts/verify-demo.mjs`. Carga el cliente TypeScript mediante Vite SSR y usa HTTP real a través del proxy con el `Origin` exacto autorizado. Solo permite las tres rutas demo locales; no usa mocks, credenciales o proveedores. La ejecución del coordinador terminó con código 0: 12 peticiones, cuatro criterios, estimaciones, páginas, cero/desconocido, parcial/vacío y errores 409/503. No ejecuta React ni un navegador y no constituye una prueba E2E de interfaz.

`npm run test:integration:ui`, con esos mismos servicios activos, ejecuta el wrapper portátil `scripts/verify-ui.mjs` y Vitest con modo `local-integration`. Monta App en jsdom, interactúa mediante user-event y hace seis peticiones reales: catálogo, búsqueda y comparación por los cuatro criterios, incluido subtotal. Resultado: una prueba satisfactoria. La suite ordinaria omite intencionadamente ese caso y ejecutó 53 pruebas satisfactorias, sin fallos; typecheck, build y lint también terminaron con código 0. Este segundo nivel sigue sin observar un navegador real ni un layout renderizado. No requiere variables de entorno adicionales.

La evidencia frontend está en [VALIDATION](VALIDATION.md), separada de las 238 pruebas y 24 comprobaciones HTTP backend anteriores. Los tests jsdom siguen teniendo sus límites; adicionalmente se ejecutaron 18 comprobaciones asistidas en Chrome headless real mediante `agent-browser`: flujos con los tres endpoints, teclado, cinco viewports y texto CSS aumentado. Se detectó y corrigió un contraste efectivo de la cascada; ocho auditorías posteriores no notificaron violaciones y conservaron una regla incompleta de nodos decorativos revisados manualmente. Detalle y límites en [BROWSER_REVIEW](BROWSER_REVIEW.md). No equivale a suite E2E versionada completa, zoom nativo, prueba con lector de pantalla o certificación WCAG.

El cliente no valida empaquetado Java, PostgreSQL, TLS/HTTP2, exposición pública, CI o despliegue. Esas capacidades conservan sus tareas y autorizaciones propias. El uso funcional está descrito en el [manual de usuario](../user-manual/README.md) conforme a la interfaz implementada.

Los intentos anteriores de Computer Use se conservaron como etapa fallida; la revisión posterior usó una herramienta operativa y autorizada, con sesión/perfil temporal y dominio local. El navegador y ambos procesos locales se detuvieron al terminar. Las evidencias ignoradas permanecen en `.local/air018/browser-review/`; el informe permanente permite entender los resultados sin depender de ellas en un clon. Cualquier fase posterior necesita autorización propia.
