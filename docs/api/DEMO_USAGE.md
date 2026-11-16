# Uso HTTP demo — AIR-014

Implementado el 16/11/2026. Solo existen catálogo y búsqueda; POST `/api/v1/demo/comparisons` devuelve 403. Todo vuelo/precio es sintético y no acredita disponibilidad, reserva o equipaje real. Contrato completo en [API_DESIGN](API_DESIGN.md); arranque y limitaciones en [DEMO_RUNTIME](../developer-guide/DEMO_RUNTIME.md).

Con el proceso demo iniciado, sustituya el puerto por el anunciado por Tomcat:

```powershell
$airwiseBase = 'http://127.0.0.1:8080' # Puerto elegido al arrancar; no es un servidor permanente
curl.exe --noproxy '*' "$airwiseBase/api/v1/demo/airports?q=Porto"
curl.exe --noproxy '*' --header 'Content-Type: application/json' --data-binary '@docs/api/demo-search.json' "$airwiseBase/api/v1/demo/flight-searches"
```

Estos métodos, ruta, parámetro y cuerpo se verificaron con curl.exe contra un proceso Java independiente en puerto aleatorio, además de tests con Tomcat real. El catálogo filtrado devuelve OPO. La [solicitud de ejemplo](demo-search.json) produce 200 COMPLETE, una oferta DEMO de `100.00` EUR y equipaje desconocido; null no se convierte en cero. UUID y puerto varían en cada ejecución.

Escenarios verificados mediante pruebas HTTP:

| Consulta | Resultado |
|---|---|
| ONE_WAY OPO–MAD, 2027-02-12 | Ocho ofertas; EUR/USD, precio desconocido, cero, escalas y equipaje diferente |
| ROUND_TRIP OPO/SCQ–MAD, 2027-02-10 a 2027-02-17 | Dos itinerarios completos y una observación de solo ida separada |
| ROUND_TRIP OPO/SCQ–MAD, 2027-02-11 a 2027-02-18 | 200 PARTIAL, oferta OPO y fallo simulado SCQ |
| ONE_WAY OPO–MAD, 2027-02-13 | 503 PROVIDERS_FAILED, causa PROVIDER_TIMEOUT |
| ONE_WAY OPO–MAD, 2027-02-14 | 200 COMPLETE, listas vacías |
| ONE_WAY OPO–MAD, 2027-02-19 | 200 PARTIAL, una unidad de dos completada |

Los filtros, sort, page y pageSize se añaden al nivel raíz del JSON. Ejemplo: `"filters":{"currency":"EUR","maxPrice":"100.00"},"sort":"PRICE_ASC","pageSize":2`. En el escenario del día 12 quedan cuatro ofertas, una exclusión por precio desconocido y dos páginas. Desde page=1 se exige `"datasetVersion":"demo-v1"`. Ordenar grupos por ID no expresa superioridad comercial entre monedas o condiciones.

No se admiten parámetros de query en POST, selector de proveedor, credenciales ni reloj del cliente. CORS empieza vacío; un cliente curl sin Origin no necesita habilitarlo. Para un origen concreto, configure `airwise.demo.cors.allowed-origins`, sin comodines ni credenciales. No hay login ni token requerido.
