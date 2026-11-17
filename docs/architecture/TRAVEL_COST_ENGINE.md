# Travel Cost Engine: comparación explicable

**Reglas aceptadas en AIR-010/012 e implementadas/verificadas en AIR-015 el17/11/2026.** Motor determinista sobre datos demo, sin conversiones FX, ML ni puntuación estadística. Las reglas siguientes conservan el contrato aceptado; evidencia de238 pruebas y HTTP real en [VALIDATION](../developer-guide/VALIDATION.md). Entrada y vocabulario en [DOMAIN_MODEL.md](DOMAIN_MODEL.md).

AIR-012 ratifica los tipos, grupos, motivos y DTO de [NORMALIZED_CONTRACTS.md](NORMALIZED_CONTRACTS.md) y [API_DESIGN.md](../api/API_DESIGN.md). Las duraciones del contrato se expresan en segundos y el filtro de minutos convierte su umbral a segundos. El subtotal es de componentes seleccionados con estimaciones.

## Entradas y límite de confianza

Consulta canónica, `datasetVersion`, entre dos y cuatro `offerIds`, preferencias por operación y estimaciones terrestres opcionales. Search reconstruye en el servidor el conjunto demo completo y valida pertenencia/identidad; filtros o página de visualización no alteran esa pertenencia.

Comparison nunca confía en importe, vendedor, evidencia, horarios, caducidad o supuesto `asOf` del navegador. El fingerprint solo ayuda a detectar contexto. Preferencias y componentes terrestres del usuario sí son entradas, etiquetadas como estimaciones. No se llama a proveedores ni se persisten búsquedas.

## Elegibilidad y grupos

1. Validar selección, consulta y versión; quitar duplicados no está permitido silenciosamente: IDs repetidos son error. IDs ajenos, versión retirada o mezcla de búsquedas producen error explícito.
2. Solo ofertas con itinerario estructural completo para la modalidad; observaciones de ruta no se convierten en candidatos completos. Una tarifa de ida no compite con ida/vuelta.
3. Formar grupos por consulta base, origen de datos (demo/real), categoría de evidencia, moneda, pasajeros/cabina y cobertura/condiciones monetarias conocidas. El aeropuerto de origen puede variar: esa es una comparación buscada. No mezclar categorías de caché, búsqueda reciente y anuncio como ofertas equivalentes.
4. Un ámbito o moneda desconocido impide ranking monetario; equipaje u otros extras desconocidos se explican y excluyen del supuesto de coste total. Comparar importe de vuelo declarado no asegura equivalencia de todos los servicios.
5. Datos caducados o de vigencia desconocida se muestran aparte sin recomendación vigente. En demo la vigencia es simulada y se rotula así. Sin autorización de FX, monedas distintas se muestran en grupos separados y no tienen ganador monetario global.

Resultados conservan `reasons`, `missingFields`, `warnings` y posibilidad de clasificación parcial por métricas conocidas. No ocultar una alternativa solo por no poder determinar “más barata”.

La reconstrucción incompleta conserva las causas de ejecución en un warning global de conjunto, sin atribuir invalidez a cada oferta ni ampliar el schema de respuesta. La traducción ratificada para AIR-015 está en [API_DESIGN](../api/API_DESIGN.md); la recomendación solo expresa EVALUABLE_GROUP, nunca cobertura total de los orígenes.

## Reglas iniciales

Preferencia primaria cerrada: FLIGHT_PRICE, FLIGHT_DURATION, CONNECTIONS o KNOWN_SUBTOTAL. Esta última es opcional y exige declarar los componentes terrestres requeridos. Las alternativas comparables usan orden lexicográfico ascendente:

| Primaria | Desempates, por orden |
|---|---|
| FLIGHT_PRICE | duración de vuelo total conocida, número de conexiones conocido, offerId |
| FLIGHT_DURATION | conexiones conocidas, importe de vuelo comparable dentro del grupo, offerId |
| CONNECTIONS | duración conocida, importe de vuelo comparable dentro del grupo, offerId |
| KNOWN_SUBTOTAL | duración conocida, conexiones conocidas, offerId; solo con todos los componentes requeridos compatibles |

Los IDs dan estabilidad de presentación, sin ventaja comercial. Un valor desconocido no se sustituye por cero: queda después de valores conocidos en presentación y se declara no evaluable en esa métrica secundaria. Solo quien tenga la primaria conocida y ámbito válido participa en el ranking de esa métrica. La recomendación se limita explícitamente al subconjunto evaluable; si algún seleccionado no es evaluable o hay grupos incompatibles, no se afirma “mejor de todos”.

La elegibilidad se aplica a la métrica elegida: FLIGHT_PRICE y KNOWN_SUBTOTAL requieren comparabilidad monetaria; FLIGHT_DURATION y CONNECTIONS permiten evaluar su métrica conocida con vigencia FRESH aunque el importe o ámbito monetario sea desconocido. Si ese importe comparable es un desempate requerido y falta dentro de un bloque empatado, se aplica TIE_UNRESOLVED al bloque completo. primaryKnown solo indica conocimiento de la métrica, no vigencia ni elegibilidad final; por eso un importe conocido caducado puede llevar primaryKnown=true y rank=null. La vigencia se explica por fila, sin alterar priceGroup.monetarilyComparable.

AIR-012 concreta el empate por bloques de primaria: si dos o más filas comparten primaria y alguna carece de cualquier desempate comercial requerido, todo ese bloque recibe el mismo rango y TIE_UNRESOLVED. Ningún otro desempate ni ID produce ventaja comercial dentro de él. En presentación se mantienen secundarias conocidas primero e ID. Si el bloque líder es irresoluble no hay recommendationIds. Si todos sus desempates son conocidos, se resuelve lexicográficamente y un empate completo conserva ganadores empatados. Se usa ranking de competición (1,1,3); un bloque inferior irresoluble no borra un líder inequívoco, pero rankingComplete=false. Con menos de dos candidatos evaluables no hay recomendación.

Duración total de vuelo = suma de trayectos, incluidas conexiones y excluida estancia. Escalas de conexión se suman por trayecto; paradas técnicas se muestran aparte. No sumar automáticamente tiempo terrestre ni usar diferencia entre primera salida y última llegada de toda la estancia.

Conexiones muestran su tiempo y advertencias fiables sobre trasbordo/cambio de aeropuerto. No hay probabilidad de pérdida de conexión, conexión garantizada ni índice de riesgo estadístico. Preferencias no incluyen pesos opacos ni perfil guardado.

## Componentes terrestres y subtotales

La primaria opcional KNOWN_SUBTOTAL sustituye a FLIGHT_PRICE en esa operación. Exige un conjunto no vacío de componentes requeridos: por ejemplo ORIGIN_GROUND_OUTBOUND y ORIGIN_GROUND_RETURN para ida/vuelta. Para cada oferta/componente se acepta como máximo una estimación de grupo, con procedencia, moneda y supuestos visibles.

Solo sumar cuando:

- El precio de vuelo tiene importe, moneda y ámbito para todo el viaje solicitado/un adulto.
- Cada componente solicitado está informado una vez para esa oferta, en la misma moneda y sin solaparse con conceptos ya incluidos.
- La estimación tiene ámbito/dirección compatibles; no multiplicar un coste de ida para inventar regreso ni un coste por persona como si fuera de grupo.

La suma usa BigDecimal y se etiqueta **“subtotal de componentes seleccionados con estimaciones”**. El Money derivado admite hasta 1001999999.99 EUR, conservando los límites separados de cotización y componentes; véase NORMALIZED_CONTRACTS. Mostrar también cada componente y los conceptos desconocidos (equipaje, servicios u otros). Un componente requerido ausente deja knownSubtotal=null, missingFields y los componentes conocidos visibles por separado, y excluye esa opción del ranking KNOWN_SUBTOTAL; no se añade cero implícito.

Para comparar subtotales, el conjunto de componentes exigidos debe ser idéntico entre alternativas. Con esta primaria, usar duración, conexiones e ID como desempates, conservando las reglas de desconocidos y empates. El usuario puede elegir FLIGHT_PRICE sin aportar estimaciones. Si elige KNOWN_SUBTOTAL sin componentes suficientemente informados, devolver motivos sin ranking evaluable; nunca cambiar silenciosamente a otra primaria.

Ejemplo sintético: A tiene vuelo 100 EUR y traslado estimado 20 EUR; B, vuelo 110 EUR y traslado desconocido. Por FLIGHT_PRICE puede indicarse que A tiene el menor importe de vuelo conocido. Los 120 EUR de A no permiten declarar ganador por subtotal frente a B; el traslado de B no vale cero.

## Casos límite obligatorios

| Caso | Resultado esperado del diseño |
|---|---|
| 100 USD frente a 110 EUR | Sin orden monetario entre monedas ni ganador global |
| Importe cero explícito frente a desconocido | Cero sigue siendo dato; desconocido es no evaluable |
| Ida de una fuente y solo regreso de otra | No se construye una oferta ida/vuelta |
| Equipaje desconocido | Aviso visible; no equipaje gratis ni coste total exacto |
| Observado en caché frente a anuncio de vendedor | Grupos separados con explicación de evidencia |
| Fuente/origen fallido en una búsqueda parcial | Comparar solo datos disponibles y mantener incidencia; no afirmar cobertura total |
| Hora local ambigua o conexión incompleta | Duración/conexión desconocida, sin corrección inventada |
| Estancia de diez días | Excluida de duración de vuelo de ida/vuelta |
| Fecha/versión diferente o ID manipulado | Rechazo antes del ranking |
| Empates y criterios incompletos | Orden de presentación estable y límites de recomendación explícitos |
| Estimaciones duplicadas/solapadas | Error semántico, sin doble suma |
| Todos caducados/no evaluables | Mostrar motivos; ninguna recomendación vigente |

Cada recomendación devuelve la clave primaria, valores usados, desempates efectivos, grupo, alcance del ranking y advertencias. AIR-012 fija las estructuras serializadas y el ranking de competición en empates. AIR-015 verifica estas reglas con pruebas de dominio, aplicación, DTO/HTTP y peticiones locales sobre las fixtures.

## Implementación presente en AIR-015

`flightcomparison.domain` contiene ComparisonTypes, ComparisonRequest, GroundTransportEstimate y ComparisonResult como valores inmutables. ComparisonEngine aplica elegibilidad, subtotales y ranking; DemoComparisonService reconstruye y verifica la selección mediante `DemoSearchService.reconstruct`, sin filtros, página, persistencia o llamada a fuentes reales. La reconstrucción reutiliza un único fan-out y su límite existente de hasta60 ofertas.

`flightcomparison.api` contiene ComparisonRequestReader, ComparisonViews, ComparisonController y DemoComparisonConfiguration. El lector reutiliza la validación canónica de Search y los límites HTTP existentes; la proyección conserva campos exactos y cadenas monetarias. Compilación, pruebas y HTTP constan en [VALIDATION](../developer-guide/VALIDATION.md); el uso reproducible está en [DEMO_USAGE](../api/DEMO_USAGE.md).
