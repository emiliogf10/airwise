# AIR-019 — Permisos y presupuesto de proveedores reales

Revisión documental: **19/11/2026**. Fuentes públicas oficiales, sin iniciar sesión, contactar proveedores ni ejecutar endpoints de vuelos, cuenta, archivo o continuación. Presupuesto autorizado: **0 peticiones reales y 0 €**. No se implementan adaptadores ni controles nuevos.

**Decisión operativa: solo DEMO habilitado.** La oferta gratuita publicada hace viable estudiar un prototipo limitado, pero no acredita saldo, permisos de la cuenta ni todos los derechos de publicación de Airwise. Las dudas siguientes bloquean una futura habilitación; AIR-019 puede cerrar su investigación dejándolas registradas. [ADR-0007](../decisions/0007-real-provider-admission.md) fija esta decisión; [PROVIDER_POLICY](PROVIDER_POLICY.md) mantiene las reglas comunes.

## Cómo interpretar la evidencia

- **Confirmado documentalmente:** la fuente oficial publica la condición citada; no prueba que una cuenta concreta la tenga ni que un adaptador funcione.
- **Inferencia de diseño:** consecuencia conservadora adoptada por Airwise, identificada como tal; no condición impuesta por el proveedor.
- **Pendiente:** no acreditado con las fuentes accesibles. Ausencia de prohibición encontrada no equivale a licencia.

Las [PoC históricas](../architecture/PROVIDER_EVIDENCE.md) siguen separadas: sus respuestas y cuotas observadas no se han reproducido ni sirven como autorización vigente. Los permisos de datos normalizados también deben resolverse; eliminar el JSON original no concede derechos nuevos. El adjunto detallado de AIR-019 amplía esta evaluación el mismo 19/11/2026 con clasificación, comparación técnica y topes diarios/mensuales; se conserva la revisión anterior.

## Clasificación para el uso previsto

Ámbito evaluado: portfolio web público, comparación multifuente, backend propio y presupuesto de APIs de 0 €. Clasificación documental de Airwise, no dictamen jurídico ni certificado del proveedor. APTO exige acreditar viabilidad del uso; APTO CON CONDICIONES requiere permisos del uso acreditados y requisitos restantes delimitados; NO APTO exige incompatibilidad demostrada; PENDIENTE DE ACLARACIÓN significa que falta evidencia decisiva.

| Proveedor | Clasificación global | Técnica y economía | Permisos y riesgo que impiden habilitarlo |
|---|---|---|---|
| Travelpayouts Data API | **PENDIENTE DE ACLARACIÓN** | Candidato a observaciones en caché; gratuidad y límites publicados, sin comprobar cuenta. | Reglas aplicables Aviasales, comparación/publicación normalizada, retención y atribución pendientes Q-T1–Q-T4. Prioridad alta de aclaración antes de integrar. |
| SerpApi Google Flights Free | **PENDIENTE DE ACLARACIÓN** | Candidato a detalle de vuelos mediante fases; cuota gratuita publicada finita, sin saldo efectivo verificado. | Uso público/afiliado, reutilización, derechos subyacentes y sobrecuota pendientes Q-S1–Q-S3. Prioridad alta; no hay Legal Shield en Free [S4]. |

No se declara APTO solo porque exista una clave, un plan gratis o una PoC exitosa. Tampoco se concluye NO APTO sin una incompatibilidad acreditada. Ambos permanecen excluidos del flujo operativo.

### Permitido, prohibido y no aclarado

| Proveedor | Expresamente previsto o permitido bajo sus condiciones | Expresamente restringido/prohibido | No aclarado para este uso |
|---|---|---|---|
| Travelpayouts Data API | Contenido informativo, integración en recursos propios afiliados y almacenamiento técnico recomendado [T1,T4–T6]. | Usos no autorizados o ajenos a la finalidad de la red, vulneración de derechos y cesión contractual sin consentimiento [T6,T8]. | Portfolio multifuente, API de redistribución, históricos/derivados y conservación tras afiliación; reglas concretas de programa. |
| SerpApi | Servicio sujeto a términos y Free publicado; caché del proveedor documentada [S1–S3]. | Uso ilegal/no autorizado, infracción de terceros y reventa/explotación del servicio sin permiso escrito [S3]. | Visualización normalizada, elegibilidad pública/comercial/afiliada de Free, retención local y derechos de datos/imágenes/marcas. No extender la restricción sobre reventa del servicio a una prohibición automática de toda visualización. |

Las obligaciones de atribución y enlaces no reemplazan permisos. El uso público no se presume autorizado mientras subsistan estas preguntas.

## Travelpayouts: Aviasales Data API

Se evalúa **Data API**, no Search API de búsqueda en vivo. El catálogo diferencia Data API sin aprobación de marca de Search API con aprobación [T2]. Las restricciones específicas de Search API no se trasladan automáticamente a Data API.

| Materia | Confirmado documentalmente | Pendiente o consecuencia para Airwise |
|---|---|---|
| Acceso | Registro/token; herramientas sujetas al programa. FAQ de Data API y documentación GraphQL piden conexión a Aviasales [T1–T3,T9]. | Las páginas no describen uniformemente el acceso; cuenta, adhesión y entidad contractual aplicables no verificadas. Clave configurada no prueba esos requisitos. |
| Naturaleza | Datos de búsquedas previas en caché, destinados a contenido informativo [T1,T4]. | Etiquetar precio observado; no anunciar inventario actual ni confirmar reserva. |
| Coste | Plataforma anunciada como gratuita; oferta con licencia funcional gratuita [T6,T7]. | No se identificó tarifa contractual específica de Data API. Viabilidad documental favorable a 0 €, sin habilitar consumo ni servicios adicionales. |
| Límites | Tabla desde 14/06/2024: `prices_for_dates` y `grouped_prices` 600/min; `get_latest_prices` 300/min; GraphQL 60/min. Documenta 429 y cabeceras de límite [T5]. | Límites por método, no saldo mensual ni autorización. Agregación exacta por cuenta/token/IP y cuota efectiva sin verificar. |
| Publicación | Documentación propone contenido web/blog; oferta contempla API en recursos propios. Uso sujeto a programa y finalidad de afiliación [T1,T4,T6,T8]. | No acredita API abierta de redistribución, suministro masivo a terceros ni comparación multifuente de Airwise. Resolver Q-T1/Q-T2. |
| Caché | El artículo de límites recomienda caché y almacenamiento técnico; FAQ recomienda 24 h [T5,T9]. | Recomendación técnica no determina retención contractual de históricos ni permiso tras finalizar afiliación. Resolver Q-T3. |
| Atribución | ID/marker y enlaces de afiliado mediante herramientas oficiales [T4,T6,T8,T10]. | Falta regla específica vigente Aviasales para texto/logos/disclosure en Airwise. Resolver Q-T4. |
| Reutilización/terceros | Licencia funcional no transferible y derechos de titulares/terceros preservados; transmisión de derechos sujeta a consentimiento [T6,T8]. | No interpretar esto como licencia general de base de datos, imágenes, marcas o exportación. |

T8 §3.2 contempla adhesión por defecto a Aviasales/JetRadar y Hotellook desde la primera autorización; esto matiza las distintas descripciones de acceso, pero no comprueba adhesión ni condiciones efectivas de la cuenta de Airwise. T4 recomienda descartar precios vencidos según `expires_at` cuando esté presente; no se presenta esa fecha aproximada como garantía comercial.

### Tres ventanas diferentes

T4 describe caché general de siete días y, para métodos concretos de fechas/agrupación, resultados encontrados en 48 h; T1 habla de dos a siete días. T9 recomienda caché local de 24 h. Son descripciones de origen y recomendaciones con ámbitos distintos, con discrepancia general entre páginas: **ninguna garantiza frescura de cada tarifa ni concede retención histórica indefinida**. Respetar caducidad conocida y declarar incertidumbre; no fijar un TTL universal copiando uno de esos números.

La frase amplia de la FAQ sobre ausencia de restricciones no anula la tabla específica de rate limits ni las condiciones del programa. Las reglas completas actuales de Aviasales remiten a su ficha de programa [T11], no recuperada íntegramente en esta revisión pública.

## SerpApi Google Flights

Es un servicio de extracción de Google Flights [S2]; no se ha acreditado que sea una API oficial de Google o una licencia de todos los datos subyacentes.

| Materia | Confirmado documentalmente | Pendiente o consecuencia para Airwise |
|---|---|---|
| Plan gratuito | Free publicado: 0 USD/mes, 250 búsquedas/mes y 50/h [S1]. | Cuenta, elegibilidad, saldo y renovación efectivos sin comprobar. No significa 250 viajes completos ni capacidad suficiente para servicio público. |
| Cómputo | Búsqueda exitosa incluso vacía consume crédito; caché de SerpApi, error o fallo no consumen cuota mensual [S1]. | Reservar conservadoramente antes de enviar; no presuponer acierto de caché ni liberar un timeout incierto. |
| Fases | `departure_token` permite fase de regreso/siguiente tramo; `booking_token`, opciones de reserva/vendedor [S2]. | Cada consulta posterior exige admisión propia. Opciones de vendedor no confirman una compra. |
| Caché técnica | Idénticos parámetros permiten caché del proveedor durante una hora; `no_cache` puede forzar consulta nueva [S2]. | No es permiso de caché local de Airwise ni TTL contractual de republicación. |
| Retención del proveedor | Política de privacidad publica conservación de datos de búsqueda durante 31 días y posterior borrado [S3, Privacy §10]. ZeroTrace se documenta para Enterprise [S2]. | No atribuir esos 31 días como derecho de Airwise. No contar con ZeroTrace en presupuesto gratuito. |
| Licencia/publicación | Condiciones §1–2 limitan usos no autorizados y explotación/reventa del servicio; §8/12 preservan derechos de terceros [S3]. | El alcance sobre mostrar resultados normalizados, comparación pública, afiliación y reutilización no queda acreditado para este caso. Resolver Q-S1/Q-S2; no afirmar prohibición general de toda visualización. |
| Protección jurídica | U.S. Legal Shield excluye Free/Starter/Developer, aplica desde Production; cubre recogida/procesamiento bajo condiciones, no uso posterior, y solo reclamaciones bajo ley estadounidense ante tribunal estadounidense [S4]. | Free no incorpora esa protección. Contratar un plan de pago tampoco resolvería derechos de uso y está fuera de presupuesto. |
| Cuenta | Account API se documenta gratuita y fuera del cómputo mensual [S5]. | **No ejecutada ni autorizada ahora.** Sigue siendo una llamada externa con credenciales. |
| Atribución y terceros | No se encontró fórmula universal de atribución que por sí sola habilite el caso de Airwise. Google distingue contenido propio y de terceros [S6]. | Resolver marcas/logos, enlaces, fuente visible, derechos de datos y condiciones aplicables; atribuir no sustituye una licencia. |

S3 publica actualización de 27/08/2026. No se deduce la elegibilidad comercial actual de Free desde entradas antiguas del foro ni desde una PoC. S6 se mostró en versión de Estados Unidos, efectiva 30/07/2026: sirve para identificar derechos distintos, **no acredita jurisdicción aplicable a Airwise** ni resuelve su relación con un intermediario. No se emite dictamen jurídico.

S1 describe renovación anticipada manual y opción automática, con reposición/cambio de ciclo. No se ha acreditado el comportamiento específico de Free al agotar cuota ni la ausencia universal de cargos; no deducirlos del precio anunciado. Airwise prohíbe activar esa opción o pagos y mantiene la admisión cerrada mientras Q-S3/Q-B1 estén pendientes.

## Matriz técnica comparativa

**P** = PoC histórica aportada por el usuario, sin reproducir; **D** = documentación oficial del registro de fuentes. Ninguna celda acredita un adaptador operativo. Comparación de capacidades potenciales, no permisos de publicación.

| Capacidad | Travelpayouts Data API | SerpApi Google Flights | Encaje e incertidumbre en Airwise |
|---|---|---|---|
| Precios observados | P:25 registros/16HTTP200; D: datos de búsquedas previas [T4]. | P: resultados de detalle; D: búsqueda con caché posible [S2]. | `PriceObservation` para datos sin itinerario; no convertir recepción reciente en observación reciente. |
| Itinerarios completos | P: no acreditados; no exigirlos a precios por ruta. | P: segmentos entre61entradas con fases/solapamientos; no61viajes únicos. | `FlightOffer` exige itinerario estructural válido; no inventar tramos ausentes. |
| Ida/vuelta | P: cobertura irregular; tarifa de ida no acredita viaje completo. | P: regreso observado; D: continuación de fase [S2]. | `ROUND_TRIP` exige2trayectos vinculados. Itinerario completo con precio `OUTBOUND_ONLY` no tiene precio completo comparable. |
| Múltiples orígenes | P: irregularidad por ruta; operación múltiple nativa no acreditada. | P: OPO/SCQ/VGO; D: varios aeropuertos por parámetro [S2]. | Consulta acepta1–3orígenes, puerto obliga1por llamada. No inferir equivalencia del parámetro externo con el fan-out actual. |
| Segmentos/escalas | No acreditados para todas las observaciones Data API. | P: segmentos/escalas; cobertura actual sin prueba nueva. | Trayecto admite1–4segmentos coherentes; conexión descrita no certifica viabilidad comercial. |
| Vendedores | No inferir detalle desde enlaces afiliados. | P: opciones observadas; D: fase adicional con token [S2]. | Anuncio de vendedor separado de reserva; tokens internos, no identidad pública. |
| Equipaje | P: insuficiente. | P: parcial. | Desconocido nunca significa incluido/coste0; ausencia opcional no fuerza `PARTIAL`. |
| Frescura | D: ventanas de origen/local diferentes; ver sección Travelpayouts. | D: caché técnica del proveedor; ver sección SerpApi. | Separar observado/recuperado/caducidad; `UNKNOWN` si falta evidencia, sin TTL universal. |
| Disponibilidad | Observación no acredita inventario actual. | Búsqueda/vendedor no confirman compra. | Elegibilidad de comparación no equivale a disponibilidad; no emitir confirmación de reserva. |
| Coste de consultas | D: límites por método, condiciones económicas anteriores. | D: cuota/cómputo anteriores; varias fases posibles. | Intentos HTTP, créditos y `requiredUnits` son contadores distintos. |
| Uso público | Permisos específicos abiertos Q-T. | Permisos y derechos abiertos Q-S. | Ambos PENDIENTE DE ACLARACIÓN; solo demo operativo. |

Inspección estática del checkout: [FlightProvider](../../backend/src/main/java/com/airwise/backend/flightsearch/application/FlightProvider.java), [ProviderCapabilities](../../backend/src/main/java/com/airwise/backend/flightsearch/application/ProviderCapabilities.java), [ProviderSearchRequest](../../backend/src/main/java/com/airwise/backend/flightsearch/application/ProviderSearchRequest.java) y [ProviderResult](../../backend/src/main/java/com/airwise/backend/flightsearch/application/ProviderResult.java). `requiredUnits` representa obligaciones lógicas (1–16), no llamadas ni créditos. `DemoSearchService` rechaza proveedores distintos de DEMO o que requieran red/credenciales; no existe un coordinador genérico real habilitable cambiando un flag. Los identificadores de fuentes reales en enums y la clasificación documental UNVERIFIED no son instancias operativas verificadas. Falta diseñar su coordinación y normalización antes de una integración autorizada.

## Preguntas que bloquean una futura habilitación

Son condiciones pendientes, no solicitudes enviadas ni nuevas tareas implementadas. Solo contactar con autorización expresa; una respuesta deberá identificar producto, versión contractual, cuenta/proyecto, ámbito y fecha, sin guardar datos privados en el repositorio.

| ID | Duda concreta y evidencia necesaria | Responsable funcional |
|---|---|---|
| Q-T1 | Recuperar reglas vigentes del programa Aviasales aplicables a Data API, adhesión y entidad contractual. Acreditar el uso de un portfolio público con posible afiliación. | Orchestrator / revisión de condiciones |
| Q-T2 | Aclarar visualización en frontend mediante backend propio, comparación multifuente, salida normalizada y diferencia con redistribución/API abierta/exportación. | Orchestrator + Backend |
| Q-T3 | Acreditar caché local, retención normalizada, históricos/precios derivados, eliminación y obligaciones tras terminación. | Orchestrator + Database |
| Q-T4 | Precisar marker/enlaces, atribución/disclosure, logotipos y otros derechos de terceros. | Orchestrator + Documentation |
| Q-S1 | Confirmar elegibilidad Free para el uso público/afiliado descrito y alcance contractual de mostrar/comparar resultados normalizados frente a reventa del servicio. | Orchestrator / revisión de condiciones |
| Q-S2 | Acreditar almacenamiento local, histórico/derivados, redistribución, atribución, imágenes/marcas y derechos del contenido de Google/aerolíneas/vendedores. No basta permiso del intermediario si no cubre esos derechos. | Orchestrator + Security & QA |
| Q-S3 | Acreditar comportamiento específico de Free al agotar cuota, reposición/ciclo, bloqueo, renovaciones y cualquier cargo automático; verificar configuración concreta solo mediante actuación posterior autorizada. | Orchestrator / revisión de condiciones |
| Q-B1 | Verificar plan y límites de cuenta, periodo/renovación, remanente y ausencia de recarga/renovación anticipada de pago; definir tope autorizado de intentos. | Orchestrator, con autorización específica |
| Q-B2 | Antes de consumo, implementar y probar con dobles el registro durable de reservas, concurrencia, fallos inciertos y bloqueo por saldo desconocido. | Backend + Security & QA, tarea posterior autorizada |

## Plan persistente de consumo de 0 €

**Política de diseño, no implementación.** Se conserva en este documento versionado. Una futura ejecución necesitará además registro operativo durable; no crear BD/ledger en AIR-019.

1. Estado actual cerrado: reales deshabilitados, cero intentos autorizados. Sin planes pagados, tarjetas, recargas, renovación anticipada ni activaciones automáticas. No incorporar claves al frontend/logs/fixtures.
2. Antes de habilitar: cerrar dudas aplicables, aprobar proveedor/operaciones/entorno y máximo de intentos por periodo y sesión, confirmar presupuesto económico 0 € y cuota suficiente con evidencia autorizada. Si el saldo, permisos o periodo son desconocidos, rechazar. La aprobación de la investigación no cumple este requisito.
3. Separar **intentos HTTP autorizados** de **créditos facturados por el proveedor**. Todo intento externo cuenta para el primer límite: cuenta, caché, continuación, archivo, polling, reintento o búsqueda. Que Account API o caché no gasten crédito mensual no les concede autorización.
4. Estimar todas las fases antes del flujo. Reservas por intento y crédito potencial, atómicas y durables entre procesos/reinicios, previas al envío; topes propios inferiores a cuotas disponibles. Viajes de varias fases no se admiten con presupuesto de una sola búsqueda. No equiparar `requiredUnits` del dominio con llamadas/créditos.
5. Para SerpApi reservar un crédito potencial por nueva consulta de búsqueda aunque pueda resolverse de caché. Conciliar únicamente con evidencia fiable del proveedor; timeout o consumo ambiguo permanecen reservados/consumidos conservadoramente. Para Travelpayouts respetar límites por método y tope propio aprobado aun sin cuota mensual documentada.
6. Sin continuaciones, fan-out, reintentos, polling o fallback a otra fuente automáticos por render/filtro/paginación. Cualquier intento adicional requiere admisión; 429 no habilita un reintento. Cancelar no demuestra que el proveedor no consumió crédito.
7. Registrar cantidades/estado/fecha y motivo sanitizado, sin claves, tokens, URLs autenticadas o payloads sensibles. Preservar causas operativas; no convertir cuota agotada en vuelos vacíos ni demo silencioso. Probar límites con dobles locales antes de cualquier autorización real.

### Topes configurables por proveedor y operación

Diseño de configuración **solo de backend**, no propiedades/variables implementadas. Todos los máximos siguientes arrancan en **0 intentos** y todas las operaciones reales deshabilitadas. El frontend no puede sustituirlos. Credenciales exclusivamente en configuración externa del backend; ninguna clave real en tests/CI. No llamar a proveedores al montar componentes, ejecutar tests, CI, paginar o filtrar; los dobles/servidores locales sintéticos sí permiten verificación sin cuota real.

| Proveedor / ámbito | Operaciones a presupuestar separadamente | Máximo diario propio inicial | Máximo mensual propio inicial |
|---|---|---:|---:|
| Travelpayouts, agregado de proveedor/cuenta | Suma de todos sus métodos y recursos, aunque estén separados internamente | 0 | 0 |
| Travelpayouts, cada operación | `prices_for_dates`, `get_latest_prices`, `grouped_prices`, GraphQL u otro método aprobado; cuenta/otros si existieran, nunca presupuestos implícitos | 0 cada una | 0 cada una |
| SerpApi, agregado de proveedor/cuenta | Suma de iniciales, regresos, vendedores y cualquier otro intento | 0 | 0 |
| SerpApi, cada operación | Inicial, regreso/siguiente tramo, vendedor, cuenta, archivo, polling y reintento, cada uno con admisión independiente | 0 cada una | 0 cada una |

Al autorizar un incremento, establecer máximos por operación **y agregados** sin multiplicar el saldo compartido. La admisión exige satisfacer simultáneamente ambos, el tope de sesión/flujo, el límite horario/minuto aplicable y la cuota comercial acreditada. Ningún máximo propio se deduce automáticamente de250/mes o600/min publicados. Coste comercial no conocido para una operación implica operación bloqueada, nunca coste0 por omisión.

- Ventanas propias: día00:00–24:00UTC y mes natural UTC según reloj de servidor. Además, aplicar el periodo comercial real del proveedor, que puede tener otra renovación. Sin periodo/saldo/reloj fiable, cerrar admisión. Definir vigencia máxima de la evidencia de saldo antes de habilitar; sin vigencia aprobada no es utilizable.
- Reservar atómicamente intentos/créditos potenciales y cupo de concurrencia antes del envío. Propuesta conservadora para una futura autorización: como máximo1petición real simultánea por cuenta, además del tope global aprobado; no es un servicio implementado ni un permiso para enviar ahora. Reintentos automáticos iniciales0.
- Si falla o es inaccesible el registro durable, no enviar. Crash entre reserva y envío deja estado incierto, sin reenvío automático. Reinicio, redeploy, cambio de límites o de ventana no borran deuda/reservas inciertas; la conciliación necesita evidencia fiable y autorización para cualquier consulta externa.
- Un intento HTTP enviado no se devuelve por caché, error o cancelación. Solo el crédito comercial potencial puede conciliarse si existe evidencia fiable de no consumo; sin ella permanece retenido conservadoramente. Una nueva muestra de saldo no repone unidades ya comprometidas ni elimina reservas concurrentes.
- Retener por separado auditoría mínima de consumo y datos de vuelos, con acceso y plazo acordados antes de implementarla; el control de presupuesto no concede permiso para archivar payloads o históricos. No se crea almacenamiento en esta tarea.

### Ejemplos de planificación, no ejecutados

Con SerpApi, una búsqueda inicial, una selección de regreso y una consulta de vendedor serían **tres peticiones de búsqueda potencialmente facturables**. Es un ejemplo, no una constante de todos los flujos. En el puerto actual de Airwise, cada llamada lógica admite un origen: tres búsquedas iniciales y un regreso/vendedor elegidos serían cinco; repetir el flujo completo para tres orígenes serían nueve. La API externa documenta también múltiples aeropuertos en un parámetro [S2], pero no se cambia el contrato ni se presume que una llamada cubre cualquier comparación.

Fórmula conservadora: `N_iniciales + N_regresos + N_vendedores + N_reintentos + N_polling_de_búsqueda`; añadir archivo/cuenta/otros intentos al presupuesto HTTP aunque su cómputo comercial difiera. Ninguno está autorizado en esta tarea. Un mes gratuito publicado no garantiza un número fijo de sesiones de usuario ni un despliegue público sostenible.

| Flujo SerpApi de ejemplo | 1 origen | 3 orígenes según el puerto actual |
|---|---:|---:|
| Solo iniciales | 1 | 3 |
| Iniciales + regreso de una alternativa elegida | 2 | 4 |
| Iniciales + regreso y vendedor de una alternativa elegida | 3 | 5 |
| Inicial + regreso para cada origen | 2 | 6 |
| Inicial + regreso + vendedor para cada origen | 3 | 9 |

Son peticiones de búsqueda potencialmente facturables sin descontar caché, para una alternativa por fase, sin reintentos ni polling; otras elecciones aumentan el coste. No se ejecutaron estos flujos ni se confunden tres orígenes con tres créditos de viaje completo.

## Propuesta siguiente y promoción posterior a AIR-021

La propuesta original [IMP-008](../../planning/IMPROVEMENTS.md) de AIR-019 recomendó un núcleo con **dobles sintéticos y sin transporte real**. El adjunto posterior la promueve a AIR-021 y autoriza implementar ese alcance aislado. Debe demostrar topes compartidos, concurrencia, ventanas, cuota desconocida y consumo incierto dentro de sus garantías; **no demuestra reserva durable ni recuperación de crash con memoria**. Esos usos se bloquean y conservan una decisión posterior vinculada a AIR-017, sin BD implícita.

Entrega autorizada de AIR-021: contrato/invariantes, componente aislado y pruebas de carreras/fallos/rechazo de recuperación no soportada, sin adaptadores, credenciales, consultas, dependencias nuevas o UI. [ADR-0008](../decisions/0008-synthetic-admission-ledger.md) y [PROVIDER_ADMISSION](../architecture/PROVIDER_ADMISSION.md) concretan la separación entre lo implementable en memoria y lo pendiente. No se ejecuta otra tarea. Las preguntas Q-T/Q-S no se envían sin autorización; ninguno de los proveedores queda elegido para publicación mientras su clasificación siga pendiente.

## Registro de fuentes oficiales

Todas consultadas el **19/11/2026** mediante páginas públicas. Fechas de versión solo donde fueron identificadas. Fuentes técnicas explican comportamiento; las contractuales y del programa delimitan permisos. No se archivaron páginas completas ni resultados de búsqueda de vuelos.

| ID | Fuente / sección útil |
|---|---|
| T1 | [Requirements for Aviasales data API access](https://support.travelpayouts.com/hc/en-us/articles/203956083-Requirements-for-Aviasales-data-API-access): requisitos y finalidad informativa. |
| T2 | [Catálogo de APIs y data feeds](https://support.travelpayouts.com/hc/en-us/articles/20384016664594-Brands-that-provide-access-to-APIs-and-data-feeds-for-Travelpayouts-partners): fila Aviasales Data/Search. |
| T3 | [Aviasales GraphQL](https://support.travelpayouts.com/hc/en-us/articles/4417975783314-Aviasales-GraphQL-for-access-to-Flights-Data-API): conexión al programa. |
| T4 | [Aviasales Data API](https://support.travelpayouts.com/hc/en-us/articles/203956163-Aviasales-Data-API): Overview, métodos de fechas/agrupación, caducidad y enlaces. |
| T5 | [API rate limits](https://support.travelpayouts.com/hc/en-us/articles/4402565416594-API-rate-limits): tabla efectiva desde 14/06/2024, 429, cabeceras y almacenamiento técnico. |
| T6 | [Affiliate Agreement — public offer](https://support.travelpayouts.com/hc/en-us/articles/203956053-Travelpayouts-Affiliate-Agreement-The-public-offer): versión 08/01/2026, §§1.2,2.1.1,2.2,2.4,6.4. |
| T7 | [Travelpayouts](https://www.travelpayouts.com/): FAQ sobre gratuidad de la plataforma. No sustituye condiciones específicas. |
| T8 | [Terms of the Travelpayouts Travel Affiliate Network](https://support.travelpayouts.com/hc/en-us/articles/360004162111-Terms-of-the-Travelpayouts-Travel-Affiliate-Network): versión 28/11/2025, §§3,5–7. |
| T9 | [FAQ about Aviasales API](https://support.travelpayouts.com/hc/en-us/articles/204529267-FAQ-about-Aviasales-API): Data frente a Search y recomendación de caché. |
| T10 | [Crear enlaces afiliados](https://support.travelpayouts.com/hc/en-us/articles/360027634052-How-to-create-and-use-affiliate-links): generador/ID. |
| T11 | [Traffic types](https://support.travelpayouts.com/hc/en-us/articles/203955673-Traffic-types) y [About programs](https://support.travelpayouts.com/hc/en-us/articles/203955763-About-programs-on-the-Travelpayouts-platform): localización de reglas del programa. |
| S1 | [SerpApi Pricing](https://serpapi.com/pricing): Free, cómputo y renovación anticipada opcional. |
| S2 | [Google Flights API — documentación](https://serpapi.com/google-flights-api): múltiples aeropuertos, tokens, caché, async y ZeroTrace. Se leyó la documentación, no se ejecutaron sus ejemplos. |
| S3 | [SerpApi Terms / Privacy](https://serpapi.com/legal): términos actualizados 27/08/2026, §§1–2,8,12 y Privacy §10. |
| S4 | [U.S. Legal Shield](https://serpapi.com/us-legal-shield): planes excluidos, recogida frente a uso posterior y jurisdicción. |
| S5 | [Account API — documentación](https://serpapi.com/account-api): operaciones y cómputo, sin consultar cuenta. |
| S6 | [Google Terms of Service](https://policies.google.com/terms?hl=en): versión mostrada US, efectiva 30/07/2026, Google content / Other content. Aplicabilidad concreta pendiente. |

## Límites de esta revisión y mantenimiento

Travelpayouts: enlaces antiguos de términos/programa devolvieron 404; se recuperaron términos/oferta por Help Center oficial. El catálogo público del programa exigía JavaScript y sus reglas completas remiten al panel; no se accedió a sesión privada. SerpApi: algunas páginas FAQ no entregaron cuerpo legible; para cómputo/retención se usaron Pricing y Terms/Privacy accesibles. No se presupone contenido no recuperado.

No se han comprobado cuenta, cuotas efectivas, elegibilidad, comportamiento de adaptadores o respuestas de proveedores. No se contactó con terceros ni se efectuaron compras. Revalidar condiciones antes de una integración/publicación o si cambian plan, programa, finalidad, destinatarios, retención o términos. La revisión documental no altera las capacidades operativas **UNVERIFIED** de las fuentes reales.
