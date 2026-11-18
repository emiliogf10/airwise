# Seguridad HTTP y riesgos del MVP

**Diseño ratificado en AIR-016 e implementación demo AIR-014/015 verificada el17/11/2026.** Tres rutas públicas exactas, Security restrictiva, JSON, errores y admisión;238 pruebas satisfactorias. La comparación conserva el plazo absoluto de lectura HTTP/1.1 NIO, guardas de classloaders, ausencia de sesiones y cierre Actuator. [ADR-0005](../decisions/0005-demo-body-read-deadline.md) delimita Tomcat y exclusión de TLS/HTTP2. Evidencia y controles de publicación pendientes en [VALIDATION](../developer-guide/VALIDATION.md).

Campos y semántica siguen en [API_DESIGN](../api/API_DESIGN.md) y [NORMALIZED_CONTRACTS](../architecture/NORMALIZED_CONTRACTS.md). El [arranque demo](../developer-guide/DEMO_RUNTIME.md) y la [matriz de pruebas](../developer-guide/TESTING_STRATEGY.md) concretan la implementación posterior. AIR-016 no modifica producción.

## Frontera y autorización

Una SecurityFilterChain del perfil demo cubre todas las solicitudes; no limitarla con securityMatcher("/api/**") ni excluir rutas mediante web.ignoring. Compartir una única definición de matchers entre admisión temprana y autorización.

| Dispatcher / método / ruta exacta | Decisión prevista |
|---|---|
| REQUEST GET /api/v1/demo/airports | Público |
| REQUEST POST /api/v1/demo/flight-searches | Público |
| REQUEST POST /api/v1/demo/comparisons | Público exacto en AIR-015; verificado por HTTP local17/11/2026 |
| Preflight CORS válido a esas rutas | Procesado por CORS, sin ejecutar negocio |
| ERROR interno y /error | Solo fallback sanitizado, bajo las condiciones descritas abajo |
| Cualquier otra combinación | 403 ACCESS_DENIED y cero invocaciones de negocio |

HEAD no hereda GET. Métodos equivocados, barra final, sufijos, subrutas, /api/**, /login, /logout, /actuator/**, documentación, estáticos y REQUEST /error quedan denegados. Mantener StrictHttpFirewall sin relajar separadores codificados, barras ambiguas, métodos peligrosos o parámetros de ruta. Su rechazo corresponde a 400 sanitizado; una ruta normalizada válida no admitida corresponde a 403. Query string no forma parte del permiso de ruta.

Usar PathPatternRequestMatcher con método/ruta y conservar anyRequest().denyAll(). Security debe cubrir REQUEST, ERROR, ASYNC, FORWARD e INCLUDE; los tres últimos no tienen permiso de negocio. Los cinco dispatchers constan en la metadata de Boot 4.1.2.

Desactivar formLogin, httpBasic, logout y caché de solicitudes; SessionCreationPolicy.STATELESS y contexto no persistido. Mantener Anonymous para evaluar reglas públicas. AuthenticationEntryPoint y AccessDeniedHandler escriben 403 JSON sin redirect, desafío Basic ni usuario generado. No cuentas, JWT, cookies de autenticación o sesiones de negocio.

## CSRF y ausencia de sesión

Los POST implementados, búsqueda y comparación, son consultas públicas sintéticas sin autoridad ambiental ni mutación privada. CSRF se exime solo en los REQUEST POST exactos a flight-searches y comparisons, sin deshabilitarlo globalmente. Las demás combinaciones se rechazan antes de acceder al token. Una futura sesión, cookie de autenticación u operación privada obliga a revisar estas excepciones.

STATELESS por sí solo no garantiza ausencia de JSESSIONID: el repositorio CSRF predeterminado puede crear HttpSession al comprobar un POST no exento. Introducir un filtro de admisión antes de CsrfFilter, después de CorsFilter y del firewall. Rechaza ruta/método/dispatcher no público con 403 antes de acceder al token. Comparte matchers con authorizeHttpRequests y no sustituye anyRequest().denyAll(). No registrarlo simultáneamente como filtro servlet y dentro de Security.

Solo llegan como REQUEST a CSRF el GET público y los dos POST exentos. Los controladores JSON no leen atributos de token, crean sesiones ni emiten Set-Cookie. Demostrar con getSession(false), cabeceras y Cookie/JSESSIONID sintética que no se usa sesión. No hace falta repositorio CSRF adicional ni cookie CSRF.

ERROR del contenedor: dispatcher ERROR, destino /error, atributo jakarta.servlet.error.request_uri y marca interna de admisión de la petición pública original. Autorizar solo esa conjunción hacia el escritor; nunca permitir todos los ERROR. CsrfFilter hereda la omisión de ERROR de OncePerRequestFilter, que también comprueba el atributo del contenedor. Probar un ERROR real, no solo cambiar el dispatcher en MockMvc. Headers no crean atributos servlet confiables. Evitar sendError en rechazos controlados, escribirlos directamente y no redisparar negocio ni entrar en bucle.

## CORS

Preferir misma procedencia. Allowlist externa vacía por defecto; configurarla cuando se apruebe un cliente. Coincidencia exacta scheme/host/puerto, sin comodines, patrones, eco arbitrario ni Origin null. localhost y 127.0.0.1 son orígenes distintos; citar Vite no autoriza un origen ni crea frontend.

allowCredentials=false; GET para catálogo y POST para cada operación. Único header de solicitud adicional permitido: Content-Type. Exponer X-Correlation-ID y Retry-After; maxAge 600 segundos. Conservar Vary de origen/preflight. No admitir Authorization ni X-Correlation-ID en preflight; un identificador remitido en una solicitud admitida se ignora.

Preflight válido para origen/ruta/método/headers devuelve 200 sin body de negocio. OPTIONS ordinario, ruta desconocida o valor no autorizado devuelve 403. No abrir OPTIONS /**. Sin Origin es válido para clientes no navegador; CORS no autentica ni evita abuso. Rechazo CORS necesita escritor JSON propio: el predeterminado no garantiza ErrorResponseDTO. No enviar ACAO al origen rechazado; errores de un origen admitido conservan CORS aplicable.

## Orden y límites

Valores iniciales para una instancia, sin benchmark ni SLO acreditados. No son parámetros del cliente.

| Recurso | Cota y aplicación prevista |
|---|---|
| Body | 65536 bytes efectivos; leer como máximo 65537 y rechazar exceso antes del parser |
| JSON | Profundidad 12, raíz como primer contenedor; hasta 10000 tokens totales; cadenas 1024 y nombres 128 unidades del parser; números 32 caracteres |
| Tomcat local | Query hasta 4096 bytes; línea de petición más cabeceras hasta 8192 bytes en conjunto |
| Formato | application/json UTF-8; charset ausente o UTF-8; Content-Encoding ausente o identity |
| Dominio/salida | Cotas de API_DESIGN: 1–3 orígenes, 2–4 seleccionados, hasta 8 estimaciones, page 0–100000/pageSize 1–50; offset en long; 20 ofertas/20 observaciones por origen |
| Plan demo | Hasta 3 ejecuciones secuenciales, cero reintentos, continuaciones o fallback |
| Bucket por IP | Capacidad 60; recarga 1 token/segundo |
| Bucket global | Capacidad 300; recarga 5 tokens/segundo |
| Concurrencia | 2 por IP y 8 globales; cola cero; adquisición atómica y liberación en finally |
| Memoria del limitador | Hasta 10000 claves, TTL de inactividad 10 minutos; no expulsar activas para reiniciar cuota; rechazar nueva clave si está lleno |
| Tiempo | Ejecución demo hasta 2 segundos reales compartidos; lectura body hasta 5 segundos; conexión hasta 10 segundos, enforcement por verificar |

IP del socket remoteAddr, sin DNS ni confianza en Forwarded/X-Forwarded-For/X-Real-IP; forward-headers-strategy=none. Proxy futuro exige topología/direcciones confiables y bloqueo de acceso directo aprobados; sin ello sus clientes comparten bucket. Claves solo en memoria con TTL, sin registrar IP/solicitudes completas. Buckets usan tiempo monotónico, nunca reloj simulado.

Admisión cuenta errores y preflights. Tasa/concurrencia/capacidad agotada devuelve 429 RATE_LIMITED antes del negocio, con Retry-After entero conservador: recarga conocida, o 1 segundo para saturación transitoria sin prometer admisión posterior. No etiquetar sobrecarga local como PROVIDERS_FAILED. Una instancia no acredita cuota distribuida ni defensa DDoS completa.

Cuota y permisos se adquieren una sola vez por REQUEST/preflight externo, independientemente de su resultado. ERROR interno reutiliza marca, correlationId y deadline; no consume tokens, adquiere/libera permisos otra vez ni reemplaza el error original por 429. La liberación original ocurre exactamente una vez en finally. La autorización final también exige dispatcher REQUEST en los matchers públicos: no depender de que un OncePerRequestFilter ejecute su gate en ASYNC/FORWARD/INCLUDE.

~~~mermaid
flowchart TD
  A[Conector: formato y tiempos] --> B[CorrelationId y cabeceras]
  B --> C[Admision y cuota local]
  C --> D[Firewall y CORS]
  D --> E[Gate anterior a CSRF]
  E --> F[CSRF y autorizacion final]
  F --> G[Media type y bytes]
  G --> H[JSON estricto y semantica]
  H --> I[Version y capacidades]
  I --> J[Proveedor y agregacion]
~~~

Primer rechazo aplicable gana. Comprobar bytes reales, incluido chunked/sin Content-Length; ese header solo permite rechazo temprano. No descomprimir antes de comprobar política. El límite de formularios no limita por sí solo JSON. Probar timeouts en conector/proxy: un timeout asíncrono no cancela cómputo ni impide lector lento. Con respuesta comprometida, no escribir otro error ni seguir invocando proveedor.

## JSON y errores

DTO HTTP cerrados, separados de dominio y contratos internos. El servidor elige descriptor, reloj, proveedor, UUID y deadline; reconstituye selecciones. Un ID/hash no autentica ni acredita pertenencia.

Jackson 3 rechazará duplicadas, campos extra, tokens posteriores, coerciones número/boolean a texto, string a número/boolean, arrays/valores intercambiables y enteros con fracción/exponente. Sin tipado polimórfico o clases del cliente. Defaults solo para omisiones admitidas; no convertir null en default. Fechas/enums/importes bien tipados se validan semánticamente para 422. No mapear toda IllegalArgumentException a 422; un fallo interno inesperado es 500.

Salida mediante DTO: Money.amount cadena decimal con escala, nulls/listas presentes, enums e instantes según contrato; sin beans, excepciones o payloads remotos.

Negociación explícita tras autorización y comprobación de formato: Accept ausente o que admita application/json permite la representación JSON; un Accept incompatible —por ejemplo text/html o application/json;q=0 sin alternativa que lo permita— produce 406 NOT_ACCEPTABLE. Respetar especificidad y q; no ignorar el header. Los errores, incluido 406, usan siempre el formato seguro JSON como política de protocolo, sin página HTML. CORS preflight sin body queda fuera de esta negociación. Los rechazos de autorización anteriores conservan 403 JSON aunque Accept sea incompatible.

| HTTP / code | Responsable previsto |
|---|---|
| 400 INVALID_REQUEST | Firewall, UTF-8, parser, estructura/tipos/campos o límites del parser |
| 403 ACCESS_DENIED | Gate, Security o CORS |
| 404 RESOURCE_NOT_FOUND | Ruta pública admitida aún sin controlador; sin abrir rutas nuevas |
| 406 NOT_ACCEPTABLE | Accept incompatible con la representación JSON |
| 409 DATASET_VERSION_UNAVAILABLE | Versión desconocida/retirada |
| 413 PAYLOAD_TOO_LARGE | Bytes efectivos |
| 415 UNSUPPORTED_MEDIA_TYPE | Tipo, charset o Content-Encoding no admitidos |
| 422 VALIDATION_FAILED / CAPABILITY_UNAVAILABLE | Semántica o todas las obligaciones no soportadas |
| 429 RATE_LIMITED | Admisión local |
| 500 INTERNAL_ERROR | Inesperado sanitizado |
| 503 PROVIDERS_FAILED | Ninguna unidad completada y fallo operativo; causas conservadas |

403/404/406 completan seguridad sin cambiar la forma de ErrorResponseDTO. Métodos no autorizados se deniegan antes de MVC; no se promete 405. Datos útiles junto a fallo conservan 200 PARTIAL; vacío válido no equivale a 503.

Escritor compartido con contractVersion, UUID servidor, code/message de catálogo, fieldErrors<=20 y providerResults seguros. Rutas de error conocidas o "/", sin claves/valores rechazados arbitrarios. UUID generado una vez antes de filtros, reutilizado en cabecera/cuerpo/ERROR; ignorar entrada del navegador.

ControllerAdvice no cubre firewall, Security, CORS o limitadores. Compartir escritor con RequestRejectedHandler, handlers Security, 413/429 y fallback ERROR. Este último restaura headers y CORS ya validado de la petición original. Rechazos anteriores al proceso —conector/TLS/proxy— pueden tener formato de transporte sin UUID/JSON Airwise; exigir ausencia de detalles sensibles, no garantías fuera del proceso.

## Cabeceras, Actuator y entornos

Aplicación y errores: Content-Type JSON UTF-8 cuando haya body, nosniff, Cache-Control: no-store, X-Frame-Options: DENY y Referrer-Policy: no-referrer; Pragma/Expires compatibles con Security. CSP: default-src 'none'; frame-ancestors 'none'; base-uri 'none'. Permissions-Policy: geolocation=(), camera=(), microphone=(). Estas cabeceras de API no sustituyen la política del futuro frontend.

HSTS solo en HTTPS público comprobado: max-age 31536000, sin includeSubDomains/preload inicialmente. HTTP local no lo anuncia. No confiar en X-Forwarded-Proto arbitrario. Sin Server/X-Powered-By ni detalles del servidor; comprobar también terminación TLS.

Actuator demo: exposure.exclude=* web/JMX, JMX deshabilitado y denegación de rutas adicional; sin excepción pública health. Sondas futuras requieren canal/ruta/autorización propios. Sin env, configprops, beans, mappings, loggers, heapdump o detalles de health.

Desarrollo: demo explícito, loopback y allowlist vacía salvo configuración concreta. Publicación futura: HTTPS, borde verificado, sin debug/DevTools en artefacto, CORS/límites probados. AIR-018 implementa el cliente demo sin alterar esta política backend; sus variables públicas no contendrán tokens, secretos ni URLs autenticadas. La conexión de desarrollo y sus límites se documentan en la [guía frontend](../developer-guide/FRONTEND.md).

## Riesgos priorizados

P0/P1/P2 son prioridad de implementación, no severidad de vulnerabilidad demostrada.

| Riesgo | Prioridad / impacto | Mitigación prevista y prueba |
|---|---|---|
| R-01 Rutas internas | P0: exposición operativa | Allowlist método/ruta/dispatcher y denyAll; negativos HEAD, Actuator y /error |
| R-02 Abuso | P0: hilos/CPU/memoria | Bytes/parser/buckets/concurrencia/plazos; N/N+1, lector lento, barreras, claves acotadas |
| R-03 Validación | P0: datos incoherentes | DTO cerrado/dominio; duplicadas/coerciones/fechas/IDs/contexto forjado |
| R-04 Cuota involuntaria | P0: tráfico/gasto | Solo DEMO, sin selector/fallback; host/provider/token/asOf rechazados sin invocación |
| R-05 Precios incompletos | P0: conclusión engañosa | Separar moneda/ámbito/evidencia/vigencia; null vs cero, parcial/caducado |
| R-06 Credenciales | P1: fuga a cliente/log | Configuración externa, catálogo, sin usuario generado; marcadores sintéticos ausentes |
| R-07 Logging | P1: privacidad/secretos | No body/query/IP/headers sensibles; correlation/códigos; captura con dobles |
| R-08 Errores detallados | P1: información interna | Escritor común/Actuator cerrado; filtro/MVC/firewall/ERROR sin stack/ruta física |
| R-09 Demo/real | P1: decisión incorrecta | DEMO/sintéticos explícitos, nunca BOOKING_CONFIRMED; procedencia/no fallback |
| R-10 CORS/CSRF | P1: autoridad ambiental futura | Allowlist, gate previo, excepción limitada; preflight/Cookie/POST sin sesión |
| R-11 Arranque/BD | P1: conexión accidental | Perfil/guardas previas; cero beans/conexiones ante configuración incompatible |
| R-12 Adaptadores futuros | P2: SSRF/fugas/cuota | Permisos/egress/redirects/tamaños/plazos antes de integrar; dobles locales |

No registrar exception.getMessage() o payload remoto sin sanitización: conservar causa operativa con código/clase permitida y correlation. Secretos fuera de Git/frontend/informes. API 0 €, sin peticiones de cuenta, reintentos ni retención de PoC autorizados. Aplicar [política](../api/PROVIDER_POLICY.md) y permisos antes de cualquier integración/caché.

## Revisión acotada de AIR-013

Revisados 32 archivos Java nuevos, recurso y seis clases de tests, incluida contexto. Inspección estática, sin ejecutar backend ni investigar CVE actuales. No se confirmó vulnerabilidad explotable en superficie existente; no hay HTTP propio ni adaptadores reales. No acredita seguridad de despliegue/dependencias completas.

**O-01: desviación interna identificada en AIR-016 y corregida en AIR-014.** La versión anterior de [ProviderCapabilities](../../backend/src/main/java/com/airwise/backend/flightsearch/application/ProviderCapabilities.java) admitía outputKinds no vacío con todas las capacidades UNVERIFIED. Actualmente lo rechaza; tres regresiones verifican negativos y positivo vacío. Se conservan las implicaciones SUPPORTED ⇒ tipo presente. No se identificó una vulnerabilidad remota en el componente demo.

Corrección mínima: rechazar lista no vacía sin capacidades verificadas y probar negativo/positivo vacío. La correspondencia exacta OFFER/itineraries y OBSERVATION/priceObservations puede reforzarse tras ratificarla; no se redefine ahora. En ese caso, adaptar el fixture de [ProviderContractTests](../../backend/src/test/java/com/airwise/backend/flightsearch/application/ProviderContractTests.java) que cambia itineraries conservando OFFER. Producción no se corrigió en AIR-016. AIR-014 aplica la corrección mínima con tres regresiones de capacidades, manteniendo las implicaciones existentes.

Observaciones sin elevación a vulnerabilidad:

- ProviderIssue/DataIssue aceptan texto sintácticamente válido; demo usa catálogo/literales. Un adaptador futuro no debe copiar texto remoto.
- PriceInformation necesita evaluationInstant; DemoCorpus recalcula correctamente. Preservar esa derivación en el borde.
- Regresión de bytecode detecta transportes conocidos; no prueba ausencia exhaustiva de red por reflexión/dependencias/configuración.

Los controles HTTP/runtime permanecen diseñados, pendientes de implementar/probar bajo tarea autorizada. Matriz en [TESTING_STRATEGY](../developer-guide/TESTING_STRATEGY.md); reporte responsable en [SECURITY.md](../../SECURITY.md).
