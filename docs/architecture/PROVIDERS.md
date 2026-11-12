# Adaptadores y capacidades de proveedores

**Principios AIR-010 y puerto ratificado en AIR-012.** Campos y estados normalizados en [NORMALIZED_CONTRACTS.md](NORMALIZED_CONTRACTS.md). La implementación corresponde a tareas posteriores. Los hechos externos se limitan a [PROVIDER_EVIDENCE.md](PROVIDER_EVIDENCE.md), aportados por el usuario. La [política](../api/PROVIDER_POLICY.md) sigue siendo obligatoria.

## Puerto y normalización

Search declara `FlightProvider`: capacidades, admisión de consulta, ejecución acotada y resultado por fuente/origen. Demo y futuros adaptadores lo implementan. DTO externos, excepciones, tokens de continuación y credenciales pertenecen al adaptador, nunca a entidades/DTO públicos o logs.

Cada capacidad distingue SUPPORTED, UNSUPPORTED y UNVERIFIED; “parcial” describe cobertura de campos/flujo, no una promesa total. No rellenar un campo inexistente para satisfacer una interfaz. Una fuente puede devolver observaciones en vez de ofertas completas.

### Puerto y solicitudes internas ratificadas

Firma conceptual del puerto de aplicación, documentada sin crear una interfaz Java:

```text
FlightProvider.capabilities() -> ProviderCapabilities
FlightProvider.search(ProviderSearchRequest) -> ProviderResult
```

ProviderSearchRequest tiene los campos exactos query, origin, dataset y context, definidos con sus tipos/nulabilidad en NORMALIZED_CONTRACTS. query es canónica y origin pertenece a query.origins. dataset es obligatorio para DEMO y null para futuras fuentes REAL. ProviderExecutionContext contiene correlationId UUID y deadlineAt del reloj operativo, fijados por la aplicación. La versión y el reloj simulados pertenecen al descriptor y nunca sustituyen al deadline real.

El puerto trabaja con un origen por llamada lógica. Search coordina 1–3 orígenes. El registro interno exige providerId único y resultado con el mismo providerId/origin de su solicitud. Datos de otro contexto se rechazan como INVALID_PROVIDER_DATA. DTO externos, credenciales y excepciones permanecen dentro del adaptador.

El contrato previsto de DEMO declara SUPPORTED para itineraries, priceObservations, oneWay, roundTrip y baggageDetails, y UNSUPPORTED para sellerDetails. Requiere cero red y credenciales, outputKinds=[OFFER,OBSERVATION] y maxOriginsPerCall=1. Se comprobará al implementar AIR-013. Travelpayouts y SerpApi permanecen deshabilitados y sus capacidades operativas son UNVERIFIED hasta una verificación autorizada. Las observaciones históricas no equivalen a capacidades actuales habilitadas.

### Normalización y fallos

1. Resolver admisión y capacidades. UNSUPPORTED o UNVERIFIED genera incidencia de capacidad antes de llamar. Un proveedor deshabilitado, desconectado o sin cuota devuelve FAILED con su código propio.
2. Mapear datos externos a los tipos normalizados. Validar aeropuertos, relación de trayectos, tiempos, moneda y cobertura. Un campo opcional ausente o malformado se trata según la frontera normativa de NORMALIZED_CONTRACTS: null/motivo/aviso cuando es normalizable, aislamiento si afecta a estructura o contradice datos conocidos. Un registro con itinerario incompleto puede conservarse como PriceObservation si sus datos de ruta/precio son válidos.
3. Aislar registros inválidos. Un resultado enteramente inválido devuelve FAILED con INVALID_PROVIDER_DATA. Una parte válida junto a una obligación incumplida devuelve PARTIAL y declara completedUnits/requiredUnits coherentes. La ausencia de equipaje por limitación de capacidad mantiene un resultado completo con dato desconocido.
4. Generar identidad pública propia y conservar procedencia. Deduplicar solo duplicados demostrables con las mismas condiciones. No fusionar anuncios/vendedores o sumar segmentos entre fuentes.
5. Agregar los resultados por origen conservando incidencias sanitizadas. Paginación y comparación demo trabajan con las fixtures reconstruidas y nunca abren continuaciones externas.

ProviderStatus incluye COMPLETE, EMPTY, PARTIAL, FAILED y UNSUPPORTED. Sus invariantes de unidades, registros y códigos están en el contrato normativo. Timeout, offline, falta de cuota y capacidad no admitida tienen códigos distintos. Cuotas, payloads privados y enlaces autenticados quedan fuera del DTO público.

| Capacidad / límite | Demo previsto | Travelpayouts: evidencia histórica | SerpApi: evidencia histórica |
|---|---|---|---|
| Precios observados/exploración económica | Escenarios sintéticos; sin explorador público adicional en MVP | Caché y destinos económicos observados | No inferir capacidad de exploración desde detalle de vuelos |
| Itinerarios con segmentos | Sí, fixtures ida/ida-vuelta | No exigirlos a sus observaciones de ruta | Detalle observado, cobertura/contrato por verificar |
| Varios orígenes | Sí, plan por aeropuerto explícito | Cobertura irregular; no asumir consulta nativa múltiple | Alternativas observadas; no asumir una llamada para todo |
| Regreso | Fixture completa vinculada a ida | No construirlo desde tarifa de ida | Regreso observado; fases/continuaciones por confirmar |
| Vendedores | Información sintética opcional, sin compra | No asumir detalle suficiente | Opciones observadas, no reserva confirmada |
| Equipaje/condiciones | Conocido o desconocido, casos explícitos | Insuficiente en evidencia aportada | Parcial; no garantía de franquicia o coste |
| Red/cuota | Cero red, claves y cuota | Integración no autorizada | Integración no autorizada; 250/mes fue dato histórico, no cuota actual |

La disponibilidad real de cada capacidad se verificará antes de implementar el adaptador. Que Airwise soporte tres orígenes no significa que el proveedor tenga una operación nativa equivalente: el planificador deberá contabilizar todo el fan-out.

## Demo determinista

Cada `datasetVersion` identifica inmutablemente fixtures, catálogo, calendario admitido, reglas de generación e `evaluationInstant` simulado. Publicar esa metadata para la UI. No cambiar contenido bajo una misma versión ni permitir al cliente sustituir el reloj. Un cambio exige otra versión; una versión retirada devuelve conflicto explícito.

Misma consulta canónica y versión producen ofertas/observaciones/IDs/orden iguales. correlationId y metadatos operativos no forman parte de esa identidad. El reloj operativo real para límites no se congela con el escenario.

Los escenarios versionados deben cubrir: directo y conexiones, ida y vuelta, varios orígenes, moneda distinta, empate, equipaje desconocido, tarifa parcial, caducidad simulada, vacío, fallo total y parcial. Los casos de fallo se asignan a combinaciones de fixtures documentadas; no a un parámetro público arbitrario para forzar errores o cambiar de proveedor. Mostrar siempre DEMO y “datos sintéticos”.

La API inicial solo expone demo. Comparar/paginar rederiva fixtures, sin búsqueda guardada, credenciales, llamada oculta o uso de un snapshot real. No reutilizar este mecanismo para reconsultar proveedores reales de forma inadvertida.

## Resultados y errores

Una unidad planificada identifica fuente, origen y obligaciones de modalidad. Su resultado distingue datos completos, vacío válido, fallo y capacidad no admitida. Una ida/vuelta de varias fases no está completa por tener solo la ida. Información parcial de una tarifa se conserva como observación/aviso, no como viaje completo.

La agregación mantiene resultados útiles y causas sanitizadas. Vacío solo significa ejecución válida sin coincidencias; un timeout o cuota agotada nunca significa “cero vuelos”. Si alguna unidad requerida completa (con datos o vacía) y otra no completa por fallo o capacidad no admitida, responder PARTIAL. COMPLETE exige todas las unidades requeridas completadas, sin omitir cobertura solicitada. Si todas son no soportadas, error semántico antes de llamar. Si ninguna completa y existe fallo operativo, error global incluso si otras eran no soportadas. Capacidad no soportada se detecta en planificación antes de llamar.

No fallback silencioso de real a demo ni cascada automática a otro proveedor que consuma cuota. No fusionar precios de fuentes/vendedores distintos. Agrupar itinerarios visualmente conserva sus alternativas y procedencias separadas.

## Admisión y presupuesto futuros

Diseño para AIR-019 y una integración posterior expresamente autorizada, no implementación de AIR-010:

1. Fuentes reales deshabilitadas por defecto. Autorizar proveedor, operación, capacidades, permisos de visualización/retención y presupuesto de peticiones antes de habilitarlos.
2. Estimar coste máximo del flujo completo: orígenes, ida/regreso, opciones de vendedor, continuaciones y reintentos. No planificar con el coste de una sola llamada.
3. Reservar presupuesto de forma atómica antes de cada petición. Límites de cuenta/periodo, concurrencia y sesión operativa coherentes; con saldo desconocido o agotado, rechazar. Gasto económico máximo 0 €, sin facturación automática ni planes de pago.
4. Definir timeouts, máximo de intentos y cancelación. Una petición cuyo consumo sea incierto cuenta conservadoramente como consumida; no liberarla como si nada hubiese ocurrido.
5. No disparar continuaciones/reintentos por render, página o filtro sin admisión. Tokens permanecen internos y no se siguen URLs arbitrarias del cliente.
6. Reutilizar resultados solo si permisos/retención lo permiten; clave incluye consulta canónica y semántica. No cachear para eludir condiciones ni convertir recepción reciente en observación reciente.

La cuota gratuita vigente, coste real por operación y control de sobreconsumo son desconocidos hoy. En despliegues futuros con varios procesos podría requerirse coordinación durable de reservas; no se crea infraestructura ahora.

## Conservación y publicación

El MVP conserva solo recursos sintéticos y referencia mínima lícita. Ninguna autorización de retención de payloads reales está implícita, ni siquiera “solo en memoria”. Resolver licencia del catálogo, datos personales, duración de conservación y derechos de reutilización/publicación antes de integrar. No guardar tokens como identidad permanente o valores de negocio.

La matriz ratificada en AIR-012 cubre capacidades, campos ausentes, ámbito/moneda/tiempo, fases incompletas, duplicados, fallo total/parcial y presupuesto agotado. Se ejecutará contra fixtures y dobles al implementar componentes. AIR-013 materializará el proveedor demo y sus escenarios.
