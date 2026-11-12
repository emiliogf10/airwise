# Contratos normalizados de Airwise v1

**Ratificados en AIR-012 el 12/11/2026.** Especificación documental para implementar en tareas posteriores. Se conservan los cuatro límites de AIR-010 y el contexto de test validado en AIR-011.

## Convenciones normativas

`contractVersion` vale `airwise-v1`. Los nombres camelCase de este documento son los nombres JSON. `T?` admite null. En respuestas todos los campos definidos aparecen, incluidos los null explícitos. Las listas son obligatorias, nunca null. Una lista vacía expresa ausencia de elementos en esa colección y su interpretación depende del estado asociado.

En solicitudes solo pueden omitirse campos con un default expreso en API_DESIGN. Campos ajenos, claves JSON duplicadas y tipos estructurales incorrectos producen 400. Las invariantes de un valor bien formado producen 422. Se usan UTF-8 y enums sensibles a mayúsculas.

| Escalar | Tipo Java previsto y forma JSON |
|---|---|
| AirportCode | String ASCII de tres letras mayúsculas, validado contra catálogo para entradas |
| AirlineCode | String de dos caracteres ASCII alfanuméricos mayúsculos; registro del dataset demo |
| Id | String de 1–128 caracteres ASCII, patrón `[A-Za-z0-9][A-Za-z0-9._-]{0,127}` |
| Text | String Unicode de 1–120 caracteres, sin controles |
| Date | LocalDate, `YYYY-MM-DD` válido |
| Instant | Instant UTC, `YYYY-MM-DDTHH:mm:ssZ`, segundos enteros |
| LocalTimeStamp | LocalDateTime con fecha y segundos, sin offset |
| Integer / Seconds | int / long no negativos, JSON entero. Duraciones conocidas hasta 604800 segundos |
| Decimal | BigDecimal, cadena con punto, sin signo, exponente, comas, ceros iniciales o espacios |
| UUID | UUID textual estándar generado por el servidor, también en cabecera X-Correlation-ID |

La estructura mínima se implementará como valores y registros inmutables donde aporte claridad. Los DTO HTTP pertenecen a API, los puertos a aplicación y los DTO de terceros al adaptador. Los contratos de vuelo carecen de identidad o anotaciones JPA.

## DatasetDescriptor y consulta

`DatasetDescriptor` contiene `datasetVersion:Id`, `evaluationInstant:Instant`, `referenceDate:Date`, `calendarStart:Date`, `calendarEnd:Date`, `currencies:CurrencyDefinition[1..16]`, `airlineCodes:AirlineCode[0..16]` y `capabilities:ProviderCapabilities`.

Cada versión fija catálogo, escenarios, reloj y reglas de identidad. Debe cumplirse referenceDate <= calendarStart <= calendarEnd y un calendario de hasta 366 días. Una sola versión publicada es la activa. La primera operación puede resolverla y devuelve siempre la versión concreta. Una versión retirada produce 409.

`CurrencyDefinition` tiene `code:String` de tres letras ISO 4217 y `minorUnits:Integer` de 0–4. El registro versionado se verifica con `java.util.Currency` de Java 21 y admite solo unidades monetarias con fracción conocida. Códigos de unidades especiales sin fracción válida quedan fuera. El demo publica sus monedas admitidas; los códigos numéricos y símbolos monetarios no sustituyen al código alfabético.

`FlightSearchRequest` es la consulta base, con todos estos campos:

| Campo | Tipo | Invariante |
|---|---|---|
| tripType | ONE_WAY / ROUND_TRIP | Enum cerrado |
| origins | AirportCode[1..3] | Distintos tras normalización y ordenados en forma canónica |
| destination | AirportCode | Diferente de todos los orígenes |
| outboundDate | Date | Dentro de calendario inclusivo y no anterior a referenceDate |
| returnDate | Date? | Null en ida, fecha obligatoria >= outboundDate en ida/vuelta y dentro del mismo calendario |
| travellers | objeto con adults:Integer | Exactamente adults=1 |
| cabin | ECONOMY | Cabina fija del MVP |

Normalización de aeropuertos: aceptar solo ASCII, eliminar espacios U+0020 de los extremos y convertir a mayúsculas las letras a–z. Validar catálogo, rechazar duplicados y ordenar orígenes lexicográficamente. No expandir ciudades. Además de normalizar la escritura de códigos, solo se modifica el orden de orígenes al canonicalizar. Fechas, modalidad y viajeros se validan sin correcciones implícitas.

## Referencia y horarios

| Tipo | Campos y nulabilidad |
|---|---|
| Airport | `code:AirportCode, name:Text?, city:Text?, countryCode:String?, timeZone:String?`. País conocido usa dos letras mayúsculas y zona conocida usa IANA. El catálogo demo exige name y timeZone verificadas |
| Airline | `code:AirlineCode?, name:Text?`. Al menos uno conocido; un operador desconocido se representa con el objeto completo null |
| FlightTime | `localDateTime:LocalTimeStamp?, offset:String?, zoneId:String?, instant:Instant?, unknownReason:UnknownReason?` |
| Connection | `afterSegmentIndex:Integer, durationSeconds:Seconds?, airportChange:boolean, selfTransfer:boolean?` |
| FlightSegment | `segmentId:Id, departureAirport:AirportCode, arrivalAirport:AirportCode, departureTime:FlightTime, arrivalTime:FlightTime, marketingAirline:Airline?, operatingAirline:Airline?, flightNumber:Text?, technicalStops:Integer?, durationSeconds:Seconds?` |
| FlightJourney | `direction:OUTBOUND/INBOUND, segments:FlightSegment[1..4], connections:Connection[0..3], durationSeconds:Seconds?` |
| FlightItinerary | `tripType:TripType, journeys:FlightJourney[1..2], durationSeconds:Seconds?, connectionCount:Integer` |

segmentId es único en todo FlightItinerary, incluidos ida y regreso, y estable para datasetVersion/recordKey e índice de segmento de su fixture. Cada coveredSegmentId identifica exactamente un segmento de ese itinerario; IDs duplicados invalidan el registro y el corpus demo se rechaza antes de publicarse.

Los códigos de segmentos referencian aeropuertos. En demo, todos pertenecen al catálogo de la versión, incluidos los intermedios. Una fuente futura puede aportar un código conocido y metadata desconocida, que se conserva sin inventar nombres o zonas.

FlightTime conserva los datos informados y su relación. Offset usa `+HH:mm` o `-HH:mm`. Un instant conocido exige localDateTime y offset coherentes. Si además existe zoneId, sus reglas deben admitir ese offset para esa fecha. Con hora local y zona inequívoca se puede derivar el instante. En un solapamiento DST sin offset no se elige uno arbitrariamente. Si no hay instante fiable, instant es null y unknownReason es NOT_RETURNED, TIME_ZONE_UNKNOWN o AMBIGUOUS_LOCAL_TIME. Con instante conocido, unknownReason es null.

Duración de segmento = llegada − salida cuando hay instantes fiables, y debe ser positiva. Salida/llegada desconocidas producen duración null. Cada conexión une índices i e i+1, una por transición y en orden. Su duración es salida siguiente − llegada previa y debe ser >=0. airportChange deriva de los códigos adyacentes; selfTransfer solo se informa con evidencia. technicalStops conocido pertenece a 0–3 y se mantiene separado de conexiones.

Duración de trayecto = llegada final − salida inicial, incluidas conexiones. Duración de itinerario = suma de trayectos, excluida estancia. Si falta algún dato necesario, el total es null. connectionCount suma las transiciones y queda en 0–6.

Ida exige solo OUTBOUND. Ida/vuelta exige OUTBOUND e INBOUND en ese orden. La primera salida de ida coincide con uno de los orígenes y su llegada final con destino. La vuelta sale de destino y llega al mismo origen de esa alternativa. Cuando se conocen fechas/instantes, deben coincidir con las fechas locales consultadas y el regreso debe seguir a la llegada de ida. Se admiten conexiones con cambio de aeropuerto declarado, sin garantizar el traslado.

## Dinero, cobertura y equipaje

Money tiene `amount:Decimal` y `currency:String` ISO admitida. Su capacidad de representación es 0 <= amount < 1002000000, hasta diez cifras enteras y la fracción admitida por moneda. Los importes cotizados de PriceInformation/AdditionalCost y maxPrice están limitados a amount < 1000000000 y nueve cifras enteras. Solo el subtotal derivado puede superar esa cota, al sumar hasta dos componentes terrestres de como máximo 1000000 cada uno. No se rechaza un subtotal de entradas válidas por reutilizar el límite de la cotización. Se comprueba primero la escala léxica y se rechaza su exceso, incluso con ceros. La normalización añade ceros hasta minorUnits mediante setScale con UNNECESSARY. La salida usa toPlainString con esa escala. Las estimaciones de usuario tienen además máximo de 1000000 unidades por componente.

UnknownReason es uno de NOT_RETURNED, CURRENCY_UNKNOWN, INVALID_SOURCE_VALUE, SCOPE_UNKNOWN, AMBIGUOUS_LOCAL_TIME, TIME_ZONE_UNKNOWN o NOT_ITEMIZED.

| Tipo | Campos |
|---|---|
| PriceScope | `basis:PER_PERSON/GROUP/UNKNOWN, travellerCount:Integer?, cabin:ECONOMY?, coverage:Coverage, coveredSegmentIds:Id[0..8]` |
| AdditionalCost | `kind:TAXES/CABIN_BAGGAGE/CHECKED_BAGGAGE/SELLER_FEE/OTHER, money:Money?, unknownReason:UnknownReason?, includedInQuotedPrice:boolean?` |
| PriceInformation | `money:Money?, unknownReason:UnknownReason?, evidenceKind:EvidenceKind, scope:PriceScope, taxes:INCLUDED/NOT_INCLUDED/PARTIAL/UNKNOWN, additionalCosts:AdditionalCost[0..5], observedAt:Instant?, retrievedAt:Instant, expiresAt:Instant?, freshness:FRESH/EXPIRED/UNKNOWN` |
| BaggageAllowance | `status:INCLUDED/NOT_INCLUDED/UNKNOWN, pieces:Integer?, weightKg:Decimal?` |
| BaggageInformation | `cabin:BaggageAllowance, checked:BaggageAllowance` |

Coverage vale FULL_ITINERARY, OUTBOUND_ONLY, INBOUND_ONLY, SEGMENT_ONLY o UNKNOWN. SEGMENT_ONLY exige IDs conocidos de segmentos de la oferta, distintos y ordenados lexicográficamente. Los demás valores llevan coveredSegmentIds vacío. Una observación sin esos segmentos usa UNKNOWN en lugar de identificadores externos. travellerCount conocido vale 1 en MVP; valores mayores no se convierten por inferencia. En ONE_WAY, una cobertura de toda la ida se normaliza FULL_ITINERARY.

EvidenceKind conserva OBSERVED_CACHE, RECENT_SEARCH, SELLER_ADVERTISED, BOOKING_CONFIRMED, ESTIMATE y UNKNOWN. Es independiente del conocimiento del importe. Una búsqueda reciente puede tener money=null y motivo. Un Money presente exige unknownReason=null. Money null exige motivo. Moneda no verificable produce Money null, nunca moneda adivinada. Cero explícito sigue siendo conocido.

Cada AdditionalCost tiene kind único y cumple la misma regla money/motivo. includedInQuotedPrice requiere evidencia. Los costes adicionales se muestran separados, sin sumar dos veces conceptos incluidos. Una lista vacía expresa falta de detalle adicional reportado y no certifica gratuidad.

BaggageAllowance UNKNOWN lleva pieces/weightKg null. NOT_INCLUDED lleva pieces=0 y weightKg=null y deja pendiente cualquier precio de compra. INCLUDED admite piezas desconocidas o 1–10 y peso desconocido o 0<weightKg<=100, con hasta tres decimales. Una cantidad desconocida no se completa con cero.

observedAt <= retrievedAt cuando ambos existen, y retrievedAt <= evaluationInstant de la respuesta. expiresAt puede ser anterior a retrievedAt, pero no anterior a observedAt conocido. FRESH exige observedAt y expiresAt conocidos, con observedAt <= evaluationInstant < expiresAt. EXPIRED se deriva de expiresAt <= evaluationInstant. Los demás casos son UNKNOWN. No crear TTL por suposición. En demo todos estos tiempos son simulados y fijos por versión, separados del reloj operativo real.

## Ofertas y observaciones

| Tipo | Campos |
|---|---|
| Seller | `id:Id, name:Text`; ambos conocidos, sin URL de compra |
| DataIssue | `code:DataIssueCode, path:String?, message:String` |
| FlightOffer | `offerId:Id, providerId:ProviderId, dataOrigin:DEMO/REAL, itinerary:FlightItinerary, price:PriceInformation, seller:Seller?, baggage:BaggageInformation, comparisonGroupId:Id, warnings:DataIssue[0..20]` |
| PriceObservation | `observationId:Id, providerId:ProviderId, dataOrigin:DEMO/REAL, origin:AirportCode, destination:AirportCode, tripType:TripType?, outboundDate:Date?, returnDate:Date?, price:PriceInformation, warnings:DataIssue[0..20]` |

DataIssueCode es PRICE_UNKNOWN, CURRENCY_UNKNOWN, PRICE_SCOPE_UNKNOWN, TIME_UNKNOWN, TIME_AMBIGUOUS, EXPIRED_DATA, FRESHNESS_UNKNOWN, BAGGAGE_UNKNOWN, ADDITIONAL_COST_UNKNOWN, INVALID_PROVIDER_DATA, CONNECTION_UNVERIFIED o OBSERVATION_ONLY. path es null o una ruta propia conocida como /price/money, /price/scope, /price/additionalCosts, /baggage/cabin, /baggage/checked, /itinerary, /departureTime, /arrivalTime, /observedAt o /expiresAt. Se asocia al tipo de registro y nunca refleja una ruta/valor externo. message procede de un catálogo público de hasta 240 caracteres. Listas de incidencias se deduplican por path/code y se acotan a 20 en orden lexicográfico.

FlightOffer exige el itinerario estructuralmente completo para la modalidad, aunque precio o equipaje puedan ser desconocidos. Un precio parcial no destruye un itinerario conocido y sí limita su ranking monetario. PriceObservation representa ausencia de itinerario completo. No contiene un itinerario ficticio ni puede seleccionarse como oferta. Fechas conocidas de una observación deben corresponder a la consulta; las desconocidas se conservan con aviso.

En demo, dataOrigin es DEMO y providerId es DEMO. Los vuelos/precios y aerolíneas de ejemplo son sintéticos. BOOKING_CONFIRMED queda fuera de sus salidas.

La normalización distingue un atributo opcional de una estructura inválida. Importe o moneda opcionales ausentes/malformados producen money=null, motivo NOT_RETURNED/CURRENCY_UNKNOWN/INVALID_SOURCE_VALUE y aviso, conservando una oferta estructural válida. Lo mismo se aplica a detalle opcional de equipaje/operador. Una hora local legítimamente desconocida o ambigua conserva FlightTime con instante null y su motivo. Una contradicción entre instantes conocidos, aeropuerto obligatorio inválido, ruta incompatible o referencia de segmento duplicada/inexistente invalida el registro; no se borra el valor contradictorio para convertirlo en desconocido. Se puede conservar una PriceObservation si queda una ruta/precio normalizable independiente del itinerario rechazado. Si no queda ningún registro válido, el adaptador devuelve FAILED con INVALID_PROVIDER_DATA; si queda una parte y otra obligación incumplida, PARTIAL con sus unidades. Campos opcionales desconocidos por sí solos no implican fallo ni PARTIAL.

## Identidad estable

La consulta canónica se serializa como JSON compacto UTF-8, sin BOM, espacios ni salto final, con claves en este orden:

```json
{"contractVersion":"airwise-v1","datasetVersion":"demo-v1","query":{"tripType":"ONE_WAY","origins":["OPO"],"destination":"MAD","outboundDate":"2027-02-10","returnDate":null,"travellers":{"adults":1},"cabin":"ECONOMY"}}
```

Se conserva el orden de claves mostrado. Los únicos valores variables de ese payload son ASCII, enums, fechas, enteros y null. Se calcula fingerprint=`q_`+hex64 minúsculo de SHA-256 sobre esos bytes.

Cada fixture declara un recordKey único de tipo Id, inmutable por dataset y distinto para variantes de vendedor/condiciones. offerId=`of_`+SHA256hex(`offer\n`+fingerprint+`\n`+recordKey). observationId usa prefijo `ob_` y texto `observation\n`. Los tokens externos nunca se reutilizan como IDs públicos.

Filtros, orden, página, correlationId y reloj operativo quedan fuera. Orígenes reordenados normalizan igual. Cambiar modalidad, ruta, fechas, viajeros, cabina o versión cambia la identidad. Cambiar fixtures exige nueva versión. El hash identifica contexto y el servidor reconstituye las ofertas para validar pertenencia.

comparisonGroupId=`g_`+SHA256hex del JSON compacto ordenado `{fingerprint,dataOrigin,evidenceKind,currency,scope,taxes,baggage,additionalCostKinds}`. currency es código o null. scope usa el orden de campos de PriceScope. baggage usa cabin y checked, cada uno en orden status,pieces,weightKg; pesos se escriben sin ceros decimales sobrantes. additionalCostKinds se ordena por kind y contiene solo kind,includedInQuotedPrice,currency. No incluye importes, vendedor ni aeropuerto alternativo. Hay grupos con precio desconocido o ámbito parcial, que permiten presentar métricas conocidas y carecen de elegibilidad monetaria.

Un grupo monetariamente elegible exige Money, scope completo de un adulto/ECONOMY con basis conocido y evidencia OBSERVED_CACHE, RECENT_SEARCH o SELLER_ADVERTISED. La comparación no convierte monedas ni mezcla categorías. Extras/bagaje desconocidos limitan la conclusión al importe de vuelo informado. La vigencia de cada fila se evalúa aparte.

## Proveedores

ProviderId es DEMO, TRAVELPAYOUTS o SERPAPI. CapabilityState es SUPPORTED, UNSUPPORTED o UNVERIFIED.

ProviderCapabilities tiene `providerId:ProviderId`; `itineraries`, `priceObservations`, `oneWay`, `roundTrip`, `sellerDetails` y `baggageDetails` son de tipo CapabilityState. Añade `outputKinds:List<OFFER/OBSERVATION>`, `maxOriginsPerCall:1`, `requiresNetwork:boolean` y `requiresCredentials:boolean`. outputKinds es único, vacío si nada está verificado.

ProviderResult tiene `providerId:ProviderId, origin:AirportCode, status:ProviderStatus, requiredUnits:Integer, completedUnits:Integer, offers:FlightOffer[], observations:PriceObservation[], issues:ProviderIssue[]`. requiredUnits pertenece a 1–16. completedUnits está en 0..requiredUnits. Una unidad es una obligación lógica declarada por el adaptador y su mapeo, no una medida de llamadas/cuota. Demo usa una por origen; escenarios parciales internos pueden declarar más de una.

ProviderIssue tiene `code:ProviderIssueCode, message:String` de catálogo público. Códigos cerrados PROVIDER_DISABLED, PROVIDER_OFFLINE, PROVIDER_TIMEOUT, PROVIDER_QUOTA_EXHAUSTED, CAPABILITY_UNSUPPORTED, CAPABILITY_UNVERIFIED, INVALID_PROVIDER_DATA y PROVIDER_FAILURE. Máximo 20 incidencias por resultado.

| ProviderStatus | Reglas |
|---|---|
| COMPLETE | Todas las unidades completadas, al menos una oferta u observación y ninguna incidencia de ejecución |
| EMPTY | Todas completadas, cero ofertas/observaciones e incidencias vacías |
| PARTIAL | 0 < completedUnits < requiredUnits, incidencias no vacías; puede contener cero registros |
| FAILED | completedUnits=0, registros vacíos y al menos una incidencia operativa |
| UNSUPPORTED | completedUnits=0, registros vacíos e incidencia de capacidad, antes de llamada |

PARTIAL conserva solo registros válidos. Si hay registros parcialmente normalizables, el adaptador declara la parte válida y la incumplida en sus unidades lógicas para expresar ese estado. Falta de equipaje o precio reportada como dato opcional no causa PARTIAL por sí misma. Un resultado con todos los registros inválidos es FAILED con INVALID_PROVIDER_DATA, no un vacío válido.

| Solicitud interna | Campos exactos y nulabilidad |
|---|---|
| ProviderSearchRequest | `query:FlightSearchRequest, origin:AirportCode, dataset:DatasetDescriptor?, context:ProviderExecutionContext` |
| ProviderExecutionContext | `correlationId:UUID, deadlineAt:Instant` |

Todos los campos aparecen. query es canónica, origin pertenece a query.origins, dataset es obligatorio y no null para DEMO, y null para futuras fuentes REAL. context y sus dos campos son obligatorios; los fija la aplicación con reloj operativo real, sin admitir valores del cliente. deadlineAt limita la ejecución y no interviene en identidad ni vigencia simulada. El proveedor se determina por el adaptador invocado y no por un selector del cliente. El puerto publica capabilities() y search(request)->ProviderResult. El planificador ejecuta fan-out por origen con adaptadores admitidos. DTO, credenciales y continuaciones permanecen internos. Un fallo de transporte/cuota se traduce en ProviderIssue y ningún fallback automático.

FlightSearchResult contiene `contractVersion, correlationId, context:SearchContext, status:COMPLETE/PARTIAL, offers:FlightOffer[], priceGroups:PriceGroup[], observations:PriceObservation[0..20], observationCount:Integer, observationsTruncated:boolean, pagination:Pagination, providerResults:ProviderSummary[], warnings:DataIssue[], errors:ProviderIssue[]`. Los DTO y tipos auxiliares de agregación se cierran en API_DESIGN. ProviderSummary tiene todos los campos de ProviderResult salvo sus registros, sustituidos por offerCount/observationCount.

La agregación devuelve COMPLETE si todas las unidades de proveedor están COMPLETE/EMPTY. Cualquier resultado PARTIAL o una combinación de completado y FAILED/UNSUPPORTED produce PARTIAL. Todas UNSUPPORTED producen 422 antes de llamar. Sin ninguna unidad completada y con fallo operativo, 503. Una fuente vacía completada junto a una fallida produce PARTIAL vacío. Las observaciones válidas cuentan como datos, nunca como viajes completos.

## Comparación

ComparisonRequestDTO y DTO de respuesta se definen en API_DESIGN. Primarias FLIGHT_PRICE, FLIGHT_DURATION, CONNECTIONS y KNOWN_SUBTOTAL. Dos a cuatro IDs se validan contra el conjunto base reconstruido, con la misma consulta y versión.

GroundTransportEstimate tiene `offerId:Id, component:ORIGIN_GROUND_OUTBOUND/ORIGIN_GROUND_RETURN, money:Money, basis:GROUP, travellerCount:1, source:USER_ESTIMATE, assumption:USER_PROVIDED`. El componente de regreso exige ROUND_TRIP. Máximo ocho estimaciones, sin duplicados de oferta/componente.

KNOWN_SUBTOTAL exige componentes requeridos no vacíos y suma vuelo más esos componentes compatibles en la misma moneda. Con PER_PERSON para un único adulto el importe coincide numéricamente con el grupo de uno, conservando la unidad original del precio. Otros extras de vendedor/equipaje se muestran por separado. El resultado se etiqueta subtotal de componentes seleccionados con estimaciones. Un componente faltante deja la fila sin ranking de subtotal.

Las reglas y desempates de TRAVEL_COST_ENGINE se conservan. Duraciones se comparan en segundos. Los campos ausentes no toman cero. Las filas de un mismo grupo con primaria conocida y datos vigentes son evaluables; una fila sin primaria o vigencia queda con rank=null y motivo. Grupo con menos de dos evaluables carece de recomendación. El algoritmo de bloques por primaria, empate irresoluble y ranking de competición se define en API_DESIGN y TRAVEL_COST_ENGINE: un desempate desconocido nunca crea un ganador por orden de presentación.

## Matriz de pruebas de contrato

Estos casos definen pruebas futuras de componentes y HTTP con fixtures/dobles. En AIR-012 se valida la coherencia documental de los ejemplos y vectores, sin ejecutar endpoints.

| ID | Entrada o escenario | Resultado requerido |
|---|---|---|
| CT-01 | ONE_WAY válido | Un trayecto OUTBOUND, returnDate null y cobertura completa si consta |
| CT-02 | ROUND_TRIP válido | Dos trayectos vinculados, regreso al origen elegido y duración sin estancia |
| CT-03 | Orígenes reordenados y con espacios ASCII | Misma consulta canónica, fingerprint y offerIds |
| CT-04 | Dos orígenes iguales tras normalizar | 422 DUPLICATE |
| CT-05 | Ciudad, código desconocido u origen=destino | 422 UNKNOWN_AIRPORT o INVALID_COMBINATION |
| CT-06 | Regreso en ida, ausente en vuelta o anterior | 422 INVALID_COMBINATION/REQUIRED según campo |
| CT-07 | Fecha inválida o fuera del calendario inclusivo | 422 INVALID_FORMAT/OUT_OF_RANGE |
| CT-08 | Varios segmentos y cambio de aeropuerto | Conexiones derivadas, trasbordo explícito, sin garantía inferida |
| CT-09 | DST ambiguo sin offset | Instante/duración null y TIME_AMBIGUOUS |
| CT-10 | Instantes incoherentes | Registro inválido aislado y causa de normalización conservada |
| CT-11 | EUR y USD | Grupos separados y ninguna suma ni ranking monetario entre monedas |
| CT-12 | Escala excesiva, exponente, negativo o moneda inválida en entrada | 422 INVALID_AMOUNT/INVALID_CURRENCY; sin redondeo |
| CT-13 | Money null frente a cero explícito | Motivo requerido para null; cero sigue siendo evaluable |
| CT-14 | Equipaje UNKNOWN/NOT_INCLUDED | Nulls o cero explícito según estado; sin gratuidad inventada |
| CT-15 | Observación sin itinerario | Permanece fuera de offers/comparisons |
| CT-16 | Itinerario completo y tarifa solo de ida | Se conserva oferta con cobertura parcial; ranking monetario no elegible |
| CT-17 | Cambio de versión/recordKey | Identidad distinta; versión retirada 409 |
| CT-18 | Filtro/orden/página distintos | Identidad de consulta base conservada y páginas estables |
| CT-19 | Página válida fuera del conjunto | 200 con lista vacía y conteos base/filtrado conservados |
| CT-20 | Selección entre páginas o ID ajeno | La selección válida se reconstruye; el ajeno produce 422 UNKNOWN_OFFER_ID |
| CT-21 | expiresAt <= evaluationInstant o frescura desconocida | Sin recomendación vigente |
| CT-22 | Fuente completa vacía junto a fallo | 200 PARTIAL vacío con causa, distinto de vacío completo |
| CT-23 | Todas FAILED / todas UNSUPPORTED | 503 / 422, con datos públicos sanitizados |
| CT-24 | Algún ProviderResult PARTIAL | Agregado PARTIAL con unidades/conteos consistentes |
| CT-25 | Cuota agotada / rate limit local | PROVIDER_QUOTA_EXHAUSTED agregado / 429 RATE_LIMITED |
| CT-26 | Estimación duplicada, moneda incompatible o regreso en ONE_WAY | 422 DUPLICATE/INVALID_COMBINATION |
| CT-27 | KNOWN_SUBTOTAL con componente ausente | Fila sin ranking, motivo y primaria conservada |
| CT-28 | Empate completo y otra fila peor | Rango 1,1,3 y recomendación empatada, ID solo presentación |
| CT-29 | Campo ajeno, JSON duplicado o tipo erróneo | 400 con ruta conocida, sin reflejar valor |
| CT-30 | Más de 65536 bytes o profundidad >12 | 413 / 400 respectivamente |
| CT-31 | Error externo con token/URL autenticada simulados | Código/mensaje propios, sin datos remotos en DTO ni logs públicos |
| CT-32 | Header X-Correlation-ID controlado por cliente | UUID nuevo de servidor, igual en cuerpo/cabecera |
| CT-33 | Marcadores de proveedor real o asOf enviados | 400 y cero llamadas externas |
| CT-34 | Arranque del corpus con IDs duplicados, moneda/horario inválidos | Rechazo explícito de la versión, sin datos corregidos silenciosamente |
| CT-35 | Cotización EUR 999999999.99 y dos componentes de 1000000.00 | Subtotal 1001999999.99 válido, sin desbordar el contrato Money |
| CT-36 | Primaria empatada con algún desempate desconocido | Bloque con mismo rango y TIE_UNRESOLVED; sin recomendación si es el bloque líder |
| CT-37 | segmentId repetido entre ida/vuelta o cobertura ajena | Registro inválido; corpus demo rechazado antes de publicarse |

Los casos negativos de datos externos usan payloads sintéticos dentro del adaptador. Los casos HTTP se ejecutarán cuando existan controladores. El corpus de referencia se documenta en los ejemplos, y sus valores no se presentan como vuelos disponibles.
