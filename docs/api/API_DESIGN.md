# API REST del MVP demo — contrato v1

**Ratificado en AIR-012.** Las rutas y DTO siguientes especifican la implementación posterior. Los tipos compartidos, enums e invariantes están definidos en [NORMALIZED_CONTRACTS.md](../architecture/NORMALIZED_CONTRACTS.md). Los ejemplos completos se encuentran en [EXAMPLES.md](EXAMPLES.md).

## Convenciones HTTP

JSON UTF-8, contractVersion=`airwise-v1`. Toda respuesta lleva correlationId UUID nuevo generado por el servidor y cabecera X-Correlation-ID con el mismo valor. Un identificador entrante se ignora para generar el propio. Los mensajes públicos proceden de catálogos internos.

Cuerpo máximo 65536 bytes y profundidad JSON máxima 12. Claves duplicadas, campos desconocidos y tipos incorrectos se rechazan. Los enteros de entrada se escriben sin fracción ni exponente. Los importes se representan mediante cadenas decimales. En salida se incluyen todos los campos declarados, con null explícito y listas siempre presentes.

| Método y ruta | Entrada | Respuesta | Acceso |
|---|---|---|---|
| GET `/api/v1/demo/airports` | q, datasetVersion, page, pageSize | 200 AirportCatalogResponseDTO | Público previsto |
| POST `/api/v1/demo/flight-searches` | FlightSearchRequestDTO | 200 FlightSearchResult, COMPLETE/PARTIAL | Público previsto |
| POST `/api/v1/demo/comparisons` | ComparisonRequestDTO | 200 ComparisonResponseDTO | Público previsto |

POST ejecuta operaciones de consulta y devuelve 200. Se mantiene estado transitorio. Fuentes reales, autenticación de cuentas y creación de reservas quedan fuera del contrato público MVP. El registro de proveedores y sus secretos pertenecen al servidor. Política efectiva de acceso, CORS/CSRF y límites operativos se concretan en AIR-016.

## Catálogo

Parámetros admitidos, únicos en query string:

| Campo | Tipo y default | Validación |
|---|---|---|
| q | String, default vacío | Hasta 64 caracteres sin controles. Trim, NFKD, retirar marcas diacríticas y mayúsculas para búsqueda por subcadena en código/nombre/ciudad |
| datasetVersion | Id opcional | Ausente resuelve activa solo en página 0; posteriores requieren versión |
| page | Integer, default 0 | 0–100000 |
| pageSize | Integer, default 20 | 1–50 |

AirportCatalogResponseDTO contiene `contractVersion, correlationId, dataset:DatasetDescriptor, items:Airport[], pagination:CatalogPagination`. CatalogPagination tiene `page, pageSize, totalCount, hasMore`. totalCount cuenta coincidencias tras q. Orden siempre por código de aeropuerto ascendente. Una página fuera del conjunto devuelve items vacío.

## Búsqueda

FlightSearchRequestDTO contiene estos campos y ningún otro:

| Campo | Tipo / omisión | Regla |
|---|---|---|
| query | FlightSearchRequest requerido | Campos e invariantes normativos en el contrato normalizado |
| datasetVersion | Id?, default null | En primera página null selecciona activa. page>0 exige versión concreta |
| filters | SearchFilters, default objeto con sus defaults | Null no admitido |
| sort | DEFAULT / PRICE_ASC / DURATION_ASC / CONNECTIONS_ASC, default DEFAULT | Enum cerrado |
| page | Integer, default 0 | 0–100000, offset calculado en long |
| pageSize | Integer, default 20 | 1–50 |

query contiene tripType, origins, destination, outboundDate, returnDate, travellers y cabin. En ONE_WAY, returnDate omitido se normaliza a null. ROUND_TRIP exige valor presente. Los demás campos son requeridos.

SearchFilters tiene `origins:AirportCode[]=[]`, `airlineCodes:AirlineCode[]=[]`, `maxConnections:Integer?=null`, `maxDurationMinutes:Integer?=null`, `currency:String?=null` y `maxPrice:Decimal?=null`.

Orígenes de filtro pertenecen a la consulta base y tienen como máximo tres valores. Aerolíneas pertenecen al registro de la versión y tienen como máximo 16. Se normalizan códigos y se rechazan duplicados. maxConnections pertenece a 0–6 y maxDurationMinutes a 1–10080. maxPrice exige currency admitida y cumple reglas de cotización Money (importe <1000000000). currency sola filtra por esa moneda.

Se aplica OR dentro de cada lista y AND entre clases de filtro. Aerolínea coincide si algún segmento tiene marketingAirline u operatingAirline con ese código conocido. El filtro de duración compara segundos <= minutos*60. El de precio exige importe/ámbito comparables de viaje completo. Un filtro que exige dato ausente excluye esa oferta y suma una vez al conteo excludedUnknownCount. Otras monedas se excluyen por currency sin compararlas numéricamente.

### Orden determinista

Todos los órdenes se aplican al conjunto completo tras filtros y antes de paginar.

- DEFAULT ordena por offerId ascendente.
- DURATION_ASC usa duración conocida ascendente, conexiones e ID. Duración desconocida queda al final.
- CONNECTIONS_ASC usa conexiones, duración conocida e ID, con duración desconocida al final del desempate.
- PRICE_ASC sitúa grupos monetariamente elegibles primero, ordena grupos por groupId y dentro de ellos por importe ascendente e ID. Grupos no elegibles se presentan después por groupId/offerId, sin ranking de sus importes.

El orden de grupos no representa superioridad comercial. La UI muestra sus límites incluso entre páginas. Observaciones se mantienen en un bloque separado, ordenado por observationId, sin filtros ni paginación de ofertas.

### Respuesta normalizada

FlightSearchResult contiene `contractVersion, correlationId, context:SearchContext, status, offers, priceGroups, observations, observationCount, observationsTruncated, pagination, providerResults, warnings, errors`.

| Tipo | Campos exactos |
|---|---|
| SearchContext | datasetVersion:Id, fingerprint:Id, query:FlightSearchRequest canónica, dataOrigin:DEMO, evaluationInstant:Instant |
| Pagination | page:Integer, pageSize:Integer, baseCount:Integer, filteredCount:Integer, excludedUnknownCount:Integer, hasMore:boolean |
| PriceGroup | groupId:Id, dataOrigin:DEMO/REAL, evidenceKind:EvidenceKind, currency:String?, scope:PriceScope, taxes, baggage:BaggageInformation, additionalCostKinds:AdditionalCostKind[], monetarilyComparable:boolean, reasons:ReasonCode[] |
| AdditionalCostKind | kind, includedInQuotedPrice:boolean?, currency:String? |
| ProviderSummary | providerId, origin, status, requiredUnits, completedUnits, offerCount:Integer, observationCount:Integer, issues:ProviderIssue[] |

Los otros tipos están definidos en NORMALIZED_CONTRACTS. Los campos taxes y kind reutilizan sus enums. baseCount cuenta ofertas normalizadas/deduplicadas antes de filtros, filteredCount las restantes y excludedUnknownCount las que incumplen un filtro por datos ausentes. Son conteos del conjunto base, independientes de la página. hasMore equivale a offset+cantidadDevuelta < filteredCount.

El demo admite hasta 20 ofertas y 20 observaciones por origen, con 60 de cada una por consulta base. El registro de fixtures debe respetar esas cotas. Se devuelven como máximo 20 observaciones, con conteo total y truncamiento explícito. Cada página incluye descriptores priceGroups de los grupos presentes en esa página y grupos en orden de groupId. Una comparación reconstruye el conjunto base completo.

warnings contiene DataIssue globales acotadas a 20, deduplicadas y ordenadas por path/code. errors contiene las incidencias de ejecución agregadas: ordenar las entradas de proveedor por providerId/origin/code, deduplicar por esa triple clave y conservar las primeras 20. El DTO ProviderIssue conserva code/message; la procedencia y las listas completas acotadas permanecen en providerResults. No se deduplica entre orígenes diferentes. providerResults contiene una entrada por origen solicitado en el demo, en orden de código, con todos sus estados. COMPLETE puede incluir cero datos válidos. PARTIAL conserva causas de obligaciones incumplidas aunque los resultados útiles estén vacíos.

| Resultados por proveedor/origen | HTTP y status |
|---|---|
| Todos COMPLETE/EMPTY | 200 COMPLETE |
| Algún PARTIAL, o unidades completadas junto a FAILED/UNSUPPORTED | 200 PARTIAL |
| Todos UNSUPPORTED | 422 CAPABILITY_UNAVAILABLE, antes de llamar al adaptador |
| Ninguna unidad completada y algún FAILED | 503 PROVIDERS_FAILED |
| Vacío completado junto a fallo | 200 PARTIAL con ofertas vacías |
| Todos vacíos completados | 200 COMPLETE con ofertas vacías |

Una observación es dato útil, pero conserva su condición de precio parcial. No se suma al número de viajes ni se acepta como offerId.

## Comparación

ComparisonRequestDTO contiene `query:FlightSearchRequest`, `datasetVersion:Id`, `offerIds:Id[2..4]`, `primary:ComparisonPrimary=FLIGHT_PRICE`, `requiredComponents:GroundComponent[]=[]` y `groundEstimates:GroundTransportEstimate[0..8]=[]`.

query y versión son requeridas. IDs se escriben exactamente como of_ seguido de 64 hexadecimales minúsculos y son distintos. El servidor normaliza la consulta y rederiva el conjunto de esa versión. Un ID fuera del conjunto produce 422 UNKNOWN_OFFER_ID. Un fingerprint remitido, precio, fuente, reloj, filtro o página ajenos al esquema producen 400.

primary es FLIGHT_PRICE, FLIGHT_DURATION, CONNECTIONS o KNOWN_SUBTOTAL. requiredComponents es un conjunto ordenado lexicográficamente sin duplicados, con ORIGIN_GROUND_OUTBOUND y/o ORIGIN_GROUND_RETURN. El regreso exige ROUND_TRIP. Para KNOWN_SUBTOTAL se requiere conjunto no vacío; para las otras primarias debe estar vacío. groundEstimates solo se admite con KNOWN_SUBTOTAL, con IDs seleccionados y sin duplicar oferta/componente. La estimación tiene máximo de 1000000 unidades y moneda compatible con su oferta.

ComparisonResponseDTO contiene `contractVersion, correlationId, context:SearchContext, primary, requiredComponents, groups:ComparisonGroup[], warnings:DataIssue[]`.

| Tipo | Campos exactos |
|---|---|
| ComparisonGroup | groupId:Id, priceGroup:PriceGroup, rows:ComparisonRow[], rankingComplete:boolean, recommendationIds:Id[], recommendationScope:EVALUABLE_GROUP |
| ComparisonRow | offerId:Id, rank:Integer?, primaryKnown:boolean, flightPrice:Money?, durationSeconds:Seconds?, connectionCount:Integer, knownSubtotal:Money?, groundEstimates:GroundTransportEstimate[], missingFields:MissingField[], reasons:ReasonCode[] |

Cada seleccionado aparece una vez en rows de su grupo. priceGroup aporta moneda, evidencia, ámbito y condiciones, con priceGroup.groupId=groupId. Los grupos se ordenan por groupId.

Las filas evaluables se dividen en bloques de igual valor de primaria, en orden ascendente. Si dentro de un bloque de dos o más filas falta cualquier desempate comercial requerido por TRAVEL_COST_ENGINE, todo ese bloque conserva el mismo rank y motivo TIE_UNRESOLVED: no se ordena comercialmente por los demás desempates. Su presentación sigue usando secundarias conocidas primero y después offerId. Si todos los desempates se conocen, se resuelve lexicográficamente; un empate completo conserva rango común y TIE. rank usa competición (1,1,3): uno más que el número de filas evaluables en bloques o subbloques comerciales anteriores. Las no evaluables llevan rank=null y se presentan al final por ID. offerId solo decide presentación.

missingFields permite PRICE, PRICE_SCOPE, DURATION, FRESHNESS, ORIGIN_GROUND_OUTBOUND y ORIGIN_GROUND_RETURN. ReasonCode permite PRICE_UNKNOWN, PRICE_SCOPE_UNKNOWN, EVIDENCE_UNSUPPORTED, FRESHNESS_UNKNOWN, EXPIRED_DATA, DURATION_UNKNOWN, GROUND_COMPONENT_MISSING, INCOMPATIBLE_CURRENCY, ADDITIONAL_COSTS_NOT_INCLUDED, SINGLE_EVALUABLE_OPTION, TIE y TIE_UNRESOLVED. PriceGroup.reasons utiliza los códigos de precio/ámbito/evidencia. Los motivos derivados de la primaria se calculan por fila.

rankingComplete exige todas las filas evaluables y ningún bloque TIE_UNRESOLVED. recommendationIds contiene los IDs de rango 1, ordenados por ID, solo si hay al menos dos evaluables y el bloque líder está resuelto. Si el bloque líder es TIE_UNRESOLVED, recommendationIds=[]; un empate completo conocido sí admite ganadores empatados. Un bloque inferior irresoluble impide rankingComplete, aunque puede conservarse una recomendación líder inequívoca. Con menos de dos evaluables se devuelve recommendationIds vacío y SINGLE_EVALUABLE_OPTION para la fila evaluable. No existe ganador global entre grupos. primaryKnown refleja solo si se conoce la métrica primaria; un precio conocido caducado puede tener primaryKnown=true y rank=null con motivo EXPIRED_DATA. Una primaria desconocida produce rank=null y primaryKnown=false. KNOWN_SUBTOTAL incompleto no cambia automáticamente de primaria.

El subtotal suma exclusivamente el importe cotizado de vuelo y los componentes terrestres requeridos, en la misma moneda. Equipaje y otros servicios se muestran con sus condiciones conocidas/desconocidas. El rótulo público es “subtotal de componentes seleccionados con estimaciones”. El coste total exacto no forma parte del contrato.

## Errores y límites de confianza

ErrorResponseDTO contiene `contractVersion, correlationId, code, message, fieldErrors:FieldError[], fieldErrorsTruncated:boolean, providerResults:ProviderSummary[]`. message tiene hasta 240 caracteres de catálogo público. FieldError contiene `path:String, code:FieldErrorCode`. Máximo 20, ordenados por path/code; si exceden se marca fieldErrorsTruncated=true.

path es un JSON Pointer conocido del esquema, con índices acotados. Para claves ajenas o errores de parseo se usa `/`. Nunca se refleja un valor rechazado, nombre arbitrario de clave, URL o excepción remota.

FieldErrorCode permite REQUIRED, INVALID_FORMAT, OUT_OF_RANGE, UNKNOWN_AIRPORT, DUPLICATE, INVALID_COMBINATION, UNSUPPORTED_VALUE, UNKNOWN_OFFER_ID, INVALID_AMOUNT, INVALID_CURRENCY, INVALID_ID, UNEXPECTED_FIELD y TYPE_MISMATCH.

| HTTP | code | Casos |
|---|---|---|
| 400 | INVALID_REQUEST | JSON/estructura/tipo/campo ajeno/duplicado |
| 413 | PAYLOAD_TOO_LARGE | Más de 65536 bytes |
| 415 | UNSUPPORTED_MEDIA_TYPE | Contenido que no sea JSON UTF-8 |
| 422 | VALIDATION_FAILED | Valores, fechas, catálogo, selección, dinero o cotas semánticas |
| 422 | CAPABILITY_UNAVAILABLE | Todas las obligaciones no soportadas o no verificadas |
| 409 | DATASET_VERSION_UNAVAILABLE | Versión retirada o desconocida |
| 429 | RATE_LIMITED | Límite operativo de Airwise; Retry-After entero cuando se conozca |
| 503 | PROVIDERS_FAILED | Fallo global, incluidos offline, timeout o cuota de proveedor |
| 500 | INTERNAL_ERROR | Fallo inesperado sanitizado |

Una cuota agotada del proveedor se conserva como PROVIDER_QUOTA_EXHAUSTED en su incidencia. El estado global depende de la agregación, mientras RATE_LIMITED identifica el límite local de Airwise. providerResults es vacío si no intervino un proveedor.

Ninguna ruta admite host, credencial, callback, token de continuación o selector de fuente real. Los DTO externos se traducen dentro del adaptador. La protección del registro/caché, el presupuesto futuro y la sanitización respetan [PROVIDERS.md](../architecture/PROVIDERS.md) y [BASELINE.md](../security/BASELINE.md).
