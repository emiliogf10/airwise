# Uso HTTP demo — AIR-014/015

Catálogo/búsqueda implementados en AIR-014 y comparación AIR-015 verificada mediante HTTP local el17/11/2026. La suite integral actual pasa238 pruebas. Todo vuelo/precio es sintético y no acredita disponibilidad, reserva o equipaje real. Contrato completo en [API_DESIGN](API_DESIGN.md); arranque y limitaciones en [DEMO_RUNTIME](../developer-guide/DEMO_RUNTIME.md).

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

## Comparación AIR-015: ejemplos HTTP ejecutados

El coordinador ejecutó estas solicitudes con curl contra Java independiente, perfil demo y DevTools restart desactivado desde la JVM, el17/11/2026. El puerto final observado63505 fue temporal y el proceso se detuvo al terminar. Las entradas proceden de fixtures demo-v1 y vectores AIR-012:

- [demo-comparison.json](demo-comparison.json): dos ofertas completas de OPO/SCQ–MAD, ida/vuelta del 10 al 17 de febrero de 2027, primaria FLIGHT_PRICE. HTTP200; rango1 OPO100EUR y rango2 SCQ110EUR dentro del mismo grupo compatible.
- [demo-subtotal-comparison.json](demo-subtotal-comparison.json): la misma selección y cuatro estimaciones USER_ESTIMATE/USER_PROVIDED, para grupo de un adulto, con ambos traslados requeridos. Los valores de30/30EUR y10/10EUR son supuestos sintéticos aportados, no precios investigados de transporte. HTTP200; subtotal130EUR de SCQ antes de160EUR de OPO. La [respuesta capturada](EXAMPLES.md) conserva el UUID variable de esa ejecución.

Con el proceso iniciado y `$airwiseBase` apuntando a su puerto:

```powershell
curl.exe --noproxy '*' --header 'Content-Type: application/json' --data-binary '@docs/api/demo-comparison.json' "$airwiseBase/api/v1/demo/comparisons"
curl.exe --noproxy '*' --header 'Content-Type: application/json' --data-binary '@docs/api/demo-subtotal-comparison.json' "$airwiseBase/api/v1/demo/comparisons"
```

La ejecución final verificó24 comprobaciones, incluyendo catálogo/búsqueda anteriores, las cuatro primarias, cuatro ofertas con rangos1,2,3,3, IDs de páginas diferentes, invariancia al invertir la selección, separación de grupos, desconocidos/vigencia, subtotal incompleto, CORS y errores400/409/422/503. El fallo total de reconstrucción devuelve PROVIDERS_FAILED con causa PROVIDER_TIMEOUT. El detalle y límites constan en [VALIDATION](../developer-guide/VALIDATION.md). El subtotal usa solo componentes seleccionados con estimaciones; equipaje desconocido y otros servicios no pasan a ser gratuitos. No existe ganador global entre grupos incompatibles.
