# Modelo de dominio y contratos normalizados

**Diseño aceptado en AIR-010 y contratos ratificados en AIR-012.** Campos, tipos, nulabilidad, enums, identidad y relaciones están fijados en [NORMALIZED_CONTRACTS.md](NORMALIZED_CONTRACTS.md). Este documento conserva el vocabulario y sus fundamentos. La implementación se desarrollará en tareas posteriores.

## Conceptos y propiedad

| Concepto | Contenido e invariantes principales | Naturaleza / propietario |
|---|---|---|
| Airport | Código de aeropuerto real normalizado, nombre/ciudad informativa, país y zona IANA conocidos; ciudad ≠ aeropuerto | Referencia, Airport; recurso mínimo sin JPA |
| Airline | Código/nombre disponibles; desconocido permitido sin inventar operador | Valor de Search; sin catálogo/módulo propio obligatorio |
| FlightSegment | Un vuelo entre aeropuertos; horarios y aerolínea cuando existan, número opcional, paradas técnicas explícitas | Valor inmutable de Search |
| FlightJourney | Ida o regreso con segmentos ordenados, conexiones y duración conocida | Valor de Search |
| FlightItinerary | Una ida o una ida y un regreso vinculados; jamás construir regreso desde tarifa de ida | Valor de Search |
| FlightOffer | Itinerario estructuralmente completo para la modalidad, precio/condiciones conocidos o desconocidos, vendedor opcional y procedencia | Dominio Search; no garantiza compra/disponibilidad |
| PriceObservation | Evidencia parcial de precio para ruta/fechas/ámbito sin itinerario completo | Dominio Search; lista separada, no oferta ficticia |
| FlightSearchRequest | Modalidad, orígenes distintos, destino, fechas y un adulto/turista; el DTO contenedor aporta datasetVersion | Entrada de aplicación; filtros/orden/página son una vista del conjunto base |
| FlightSearchResult | Consulta canónica, versión/fingerprint, ofertas, observaciones, páginas y estados/incidencias por fuente/origen | Resultado de aplicación, no snapshot persistido |
| Money / PriceInformation | Importe decimal, moneda, evidencia, cobertura, conceptos incluidos y tiempos | Valores públicos Search; no `double` |
| FlightProvider | Puerto de búsqueda/capacidades definido por Search | Contrato, no entidad ni tabla |
| GroundTransportEstimate | Importe, moneda, dirección/ámbito lógico cubierto, procedencia y supuestos; sin dirección personal | Valor transitorio de Comparison |
| ComparisonResult | Grupos, rankings parciales, motivos, exclusiones, valores ausentes y componentes conocidos | Resultado de Comparison |

Los DTO HTTP proyectan este modelo sin anotaciones de persistencia ni DTO de terceros en dominio. Ninguno de estos conceptos requiere por sí mismo una tabla. Datos guardables y condiciones futuras en [MODULES.md](MODULES.md).

## Modalidad, identidad y estructura

Solo ida contiene un trayecto OUTBOUND. Ida/vuelta contiene OUTBOUND e INBOUND: destino de ida como origen de vuelta, regreso al mismo aeropuerto de origen de esa alternativa. Varios orígenes en la solicitud producen alternativas independientes; no un único viaje que salga de todos. No open-jaw, multicity ni billetes autocompuestos en el MVP.

Cada trayecto contiene uno o varios segmentos ordenados. Una escala de conexión entre segmentos no es lo mismo que una parada técnica durante un segmento. Número de conexiones conocido = suma de transiciones entre segmentos de cada trayecto; paradas técnicas se muestran aparte. Cambios de aeropuerto, billetes separados o self-transfer deben ser explícitos si se conocen y nunca inferirse seguros.

La clasificación depende de la estructura del itinerario. Una oferta con todos los trayectos puede tener precio de cobertura parcial o desconocido y conserva su itinerario conocido. Esa cobertura limita su ranking monetario. Una observación carece del itinerario completo requerido y se mantiene fuera del comparador de ofertas. Un solo trayecto nunca permite fabricar el regreso.

`offerId` es estable dentro de consulta canónica + datasetVersion y distingue fuente, itinerario y condiciones/vendedor. Reordenar orígenes o variar filtros/página no cambia la consulta base ni sus IDs. Un cambio de fecha, modalidad, origen, destino o versión sí cambia el contexto. El fingerprint ayuda a detectar mezclas, pero no autentica ni sustituye validación en servidor.

Deduplicar solo duplicados demostrables de la misma fuente/versión/condiciones. El mismo itinerario de dos vendedores o evidencias distintas conserva precios separados. No sumar fragmentos externos para fabricar ida/vuelta.

## Dinero, evidencia y desconocidos

Backend usa `BigDecimal` y moneda explícita. HTTP representa importes como cadenas decimales. Escala, normalización y límites están cerrados en NORMALIZED_CONTRACTS. Cero es un dato explícito de fuente/estimación y la falta de dato se representa con null y motivo. La categoría de evidencia y el conocimiento del importe son dimensiones independientes.

| Categoría conceptual | Significado |
|---|---|
| OBSERVED_CACHE | Precio observado previamente en caché; no disponibilidad actual |
| RECENT_SEARCH | Respuesta de búsqueda reciente, con caché posible; sin garantía |
| SELLER_ADVERTISED | Importe anunciado por un vendedor con condiciones conocidas; no reserva |
| BOOKING_CONFIRMED | Confirmación concreta asociada a reserva y condiciones verificadas; fuera del flujo MVP |
| ESTIMATE | Componente calculado o aportado con fuente/supuestos, separado del vuelo |
| UNKNOWN | Categoría de evidencia no determinada; el desconocimiento del importe se declara por separado mediante money=null y motivo |

La procedencia `DEMO` es otra dimensión: datos sintéticos incluso si ilustran categorías anteriores. El demo no produce confirmación real de vendedor/reserva ni BOOKING_CONFIRMED. Una respuesta reciente de transporte HTTP no convierte un registro viejo en precio reciente.

`PriceInformation` conserva pasajeros/cabina, trayectos/personas cubiertos, si el precio es por persona o grupo y conceptos incluidos, parciales o desconocidos. Distinguir importe de vuelo, impuestos, equipaje y estimaciones. Un importe de ida no es total de ida/vuelta; equipaje desconocido no está incluido ni es gratuito. Si falta moneda verificada, no crear Money ni inventarla: representar precio desconocido y su motivo. Si solo falta ámbito, puede mostrarse el importe con moneda y advertencia, pero queda excluida la comparación monetaria afectada.

## Horarios y duraciones

Fechas de búsqueda son locales del aeropuerto de salida de cada trayecto. En una alternativa de ida/vuelta, la vuelta se interpreta en el aeropuerto destino de la ida. Conservar fecha/hora local, offset e IANA cuando se conozcan; si hay instante fiable, usarlo para calcular duración.

No inferir instantes de una hora local ambigua por DST ni inventar zona/offset. Si faltan datos fiables, duración/conexión quedan desconocidas con motivo. Rechazar secuencias imposibles o discrepancias de zona/offset en fixtures; un registro externo incoherente se aislará como incidencia, sin esconder toda la causa.

Duración de un trayecto = llegada final − salida inicial, incluyendo conexiones. Duración de ida/vuelta = suma de trayectos, **excluye estancia en destino**. No es tiempo puerta a puerta. Un regreso con fecha local igual a la ida solo se admite si su secuencia de instantes es coherente; no se exige artificialmente otra fecha.

Tiempo de conexión = salida siguiente − llegada previa cuando ambos instantes son fiables. No declarar conexión garantizada o imposible por un umbral heurístico sin reglas fiables de traslado/conexión; mostrar advertencias cualitativas.

## Procedencia, frescura y parcialidad

Origen de datos, proveedor y vendedor son dimensiones distintas. Guardar `observedAt`, `retrievedAt`, `expiresAt` solo cuando se conozcan y su semántica sea válida. Desconocer observación/caducidad no permite inventarlas a partir de la recepción.

Demo fija un `evaluationInstant` por versión, calendario y reloj simulado. El cliente no lo decide mediante un `asOf`. Repetibilidad excluye correlationId, tiempos operativos y límites reales. Un futuro proveedor real usará reloj real y políticas de frescura propias, pendientes de permisos/contrato.

Separar ejecución por fuente/origen, integridad de cada dato y vigencia comercial. Una fuente puede completar con precio/equipaje desconocidos; eso no es automáticamente fallo técnico. Fuentes fallidas se conservan como incidencias; éxitos parciales no se convierten en fallo total ni fallos en resultados vacíos. Datos expirados o con frescura desconocida se etiquetan y no reciben recomendación vigente.

## Estimaciones terrestres

El usuario puede aportar componentes lógicos como traslado al aeropuerto en ida o vuelta, sin geolocalización ni datos privados. Cada estimación declara ID de oferta, componente, importe/moneda, ámbito (grupo de un adulto), procedencia USER_ESTIMATE y supuestos acotados mediante opciones, sin texto libre obligatorio. No modifica precio de vuelo.

Futuras fuentes autorizadas requerirán licencia, fecha, cobertura y conversión fiable si procede. No crear un coste total exacto ni añadir ausencias como cero. Criterios de suma y comparación en [TRAVEL_COST_ENGINE.md](TRAVEL_COST_ENGINE.md).
