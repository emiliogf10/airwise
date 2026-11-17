# Estrategia de pruebas del MVP — AIR-016

**Diseño AIR-016 actualizado con AIR-015 el17/11/2026.** La matriz conserva estados históricos P salvo actualización explícita. Evidencia en [VALIDATION](VALIDATION.md):238 pruebas satisfactorias, incluidas comparación, DTO/JSON, regresiones catálogo/búsqueda, Security/CORS/admisión, Tomcat y plazo absoluto5s en HTTP/1.1 NIO. Persistencia, TLS/HTTP2 y publicación no se acreditan; un caso de matriz puede contener más condiciones que las cubiertas por un test.

## Base y herramientas reales

AIR-013 documenta 62 invocaciones satisfactorias: contexto 1, proveedor 18, corpus 6, aislamiento 2, aplicación 6 y dominio 29. AIR-016 inspecciona fuentes/reportes históricos; no acredita una nueva ejecución Maven.

Las dependencias ya resuelven Jupiter/Platform 6.0.3, parametrizadas, Mockito, AssertJ, MockMvc y soporte Security. No añadir H2, Testcontainers, WireMock, starter genérico o cliente de proveedor para estos casos.

| Nivel | Qué observa / límite |
|---|---|
| Unitario | Invariantes/aplicación/admisión con reloj y dobles; no acredita cadena servlet |
| Contrato | Vectores, JSON/DTO, estados y ejemplos versionados; no acredita transporte |
| Web slice | Controlador/mapeo/parser/advice y filtros reales; no acredita conector ni ERROR real |
| Integración demo | Contexto completo, servidor embebido en puerto aleatorio y cliente local; sin PostgreSQL/terceros |
| Persistencia futura | PostgreSQL/JPA/Flyway aislados bajo otra autorización; no cubiertos por demo |

Imports Boot 4 comprobados: org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest y AutoConfigureMockMvc; SpringBootTest sigue en org.springframework.boot.test.context. Mantener addFilters=true e importar configuración real. MockMvc manual usa SecurityMockMvcConfigurers.springSecurity(). No excluir filtros ni inventar un usuario autenticado para hacer pasar una ruta pública.

## Matriz verificable

E13: ejecución histórica AIR-013. P: diseñado y pendiente. Los casos deben observar comportamiento, no solo que un DTO se construya.

| ID | Nivel / componente | Entrada o escenario | Resultado exigido / estado |
|---|---|---|---|
| T-01 | Unitario dominio | Negativo/exponente/escala/moneda/cero | Rechazo sin redondeo, cero conocido / E13 |
| T-02 | Unitario dominio | Ida/vuelta, DST, segmentos repetidos y cronología | Invariantes y desconocidos conservados / E13 |
| T-03 | Unitario proveedor | Consulta/version equivalentes, orígenes normalizados y contextos distintos | Mismos resultados/IDs con deadline vigente / E13 |
| T-04 | Contrato proveedor | Vectores q_/of_/g_, OPO/SCQ y observación | Identidad aprobada y separación de tipos / E13 |
| T-05 | Unitario corpus | UTF-8/tamaño/duplicados/referencias/más de20 | Construcción falla explícitamente, sin fallback / E13 |
| T-06 | Unitario proveedor | Vacío/fallo/parcial/descriptor forjado | Unidades/causas y reloj correctos / E13 |
| T-07 | Contrato aplicación | All-UNVERIFIED con outputKinds no vacío | Regresión O-01 negativa y positivo vacío / ejecutado AIR-014 |
| T-08 | Web slice AIR-014 | GET catálogo y POST búsqueda sin token/cookie | Caso de uso público accesible sin principal / P |
| T-09 | Web slice AIR-015 | POST comparación | Público exacto, respuestas y errores normalizados / ejecutado AIR-015 |
| T-10 | Web slice permiso | HEAD, método distinto, barra final, sufijo, subruta, /login,/actuator,/error | 403 y cero negocio / P |
| T-11 | Web slice firewall | URI ambigua/separador codificado rechazado | 400 sanitizado sin eco, cero negocio / P |
| T-12 | Web slice sesión | POST no público, Cookie/JSESSIONID y Basic sintéticos | Gate antes de CSRF; 403 sin sesión/Set-Cookie/redirect/WWW-Authenticate / P |
| T-13 | Web slice CSRF | POST búsqueda exacto sin token y filtro CSRF de producción sobre ruta unsafe de prueba | Exención solo exacta; CSRF sigue instalado y protege la ruta no exenta / ejecutado AIR-014 |
| T-14 | Web slice dispatcher | FORWARD/INCLUDE/ASYNC a ruta pública | Ningún bypass ni negocio / P |
| T-15 | Web slice CORS | Preflight origen/ruta/método/Content-Type admitidos | 200 sin body de negocio, ACAO exacto y sin credenciales / P |
| T-16 | Web slice CORS | Origin null/ajeno, header/método/ruta extra u OPTIONS ordinario | 403 seguro, sin ACAO al origen rechazado / P |
| T-17 | Web slice CORS | Sin Origin; error de origen admitido | Cliente público válido; error con CORS aplicable / P |
| T-18 | Contrato JSON | Duplicadas/extra/segundo documento/UTF-8 inválido | 400 con ruta propia o "/", sin entrada reflejada / P |
| T-19 | Contrato JSON | Número/boolean→texto, string→número/boolean, enum numérico, array→valor | 400 sin coerción / P |
| T-20 | Contrato JSON | page 1.0/1e0; fecha/enum bien tipados pero inválidos | Entero no léxico→400; valor semántico inválido→422 / P |
| T-21 | Contrato JSON | Omisión/default, null explícito y returnDate RT null | Default solo de omisión; null no ocultado; retorno requerido→422 / P |
| T-22 | Web slice parser | Profundidad12/13, 10000/10001 tokens y nombres/cadenas/números N/N+1 | Dentro llega a validación; exceso parser→400 / P |
| T-23 | Integración bytes | 65536/65537, con/sin Content-Length y chunked | Exceso→413 antes de parser/proveedor, lectura acotada / P |
| T-24 | Web slice formato | Media type/charset/Content-Encoding ajenos | 415, no descompresión ni negocio / P |
| T-25 | Web slice valores | Catálogo/duplicados/fechas/IDs/cotas de dinero | 422 con códigos/rutas propios / P |
| T-26 | Web slice versión | Activa omitida, concreta, retirada/desconocida | Resolver donde permitido; 409 donde corresponde sin buscar / P |
| T-27 | Unitario/Web búsqueda | Filtros AND/OR, dato requerido unknown y otra moneda | Conjunto/conteos normativos; sin comparación de monedas / P |
| T-28 | Contrato búsqueda | Orden previo a página, empate y página fuera | Orden estable, hasMore/conteos y 200 vacío correcto / P |
| T-29 | Contrato identidad | Cambiar vista frente a consulta/versión | IDs permanecen en vista y cambian con contexto / P |
| T-30 | Contrato agregación | Completo+fallo, vacío+fallo y ProviderResult PARTIAL | 200 PARTIAL incluso vacío con causas / P |
| T-31 | Contrato agregación | Todos vacíos/fallidos/no soportados | 200 COMPLETE vacío /503 /422, sin fallback / P |
| T-32 | Unitario admisión | Buckets/recarga en bordes con ticker falso | 429/Retry-After correcto, independiente de reloj demo / P |
| T-33 | Integración admisión | 2/IP,8global; cancelación y excepción | Barreras de concurrencia, permisos liberados, sin cola/trabajo posterior / P |
| T-34 | Unitario admisión | 10000 claves, nueva IP, TTL y XFF arbitrario | Memoria acotada, sin reset/trust del header / P |
| T-35 | Web slice errores | Excepción con token/URL/path sintéticos | 500 catálogo; marcadores ausentes body/headers/log / P |
| T-36 | Web slice errores | CORS/firewall/413/429/503/MVC | Mismo ErrorResponseDTO/UUID, <=20 fieldErrors / P |
| T-37 | Integración ERROR | Error real del contenedor desde ruta admitida | Solo ERROR+/error interno, status/headers/CORS seguros, sin bucle/sesión; no duplicar cuota/permisos/plazo ni sustituir por429 / P |
| T-38 | Web slice headers | Éxito/4xx/5xx; Accept omitido/JSON/text/html/q=0 y Basic | Accept incompatible→406 NOT_ACCEPTABLE JSON; permitido→representación normal; denegación previa→403; headers sin HTML/redirect/detalles / P |
| T-39 | Integración transporte | Cabeceras/query, fragmentación, lector lento/plazos | Rechazo acotado, distinguir conector de JSON Airwise / P |
| T-40 | Integración demo | Perfil explícito y puerto aleatorio, entorno limpio | DEMO único, sin DataSource/EMF/Flyway/usuario/password generado / P |
| T-41 | Integración guardas | URL/JNDI/XA/JdbcConnectionDetails/bean o perfil incompatible | Fallo antes de conectar, sin valores en logs / P |
| T-42 | Integración perfiles | Sin demo y test con exclusión local actual | No filtración de beans/exclusiones a otro modo / P |
| T-43 | Integración Actuator | Web/JMX incluidos health/env/mappings | Ninguna exposición demo; ruta HTTP denegada / P |
| T-44 | Contrato salida | Money/null/listas/instantes/enums/metadata DEMO | Cadenas monetarias/escala/campos exactos, sin beans/excepciones / P |
| T-45 | Contrato AIR-015 | Null/cero, parcial, EUR/USD, evidencia/caducidad | Sin total/gratuidad/ganador monetario inventados / ejecutado AIR-015; parcialidad mediante dobles |
| T-46 | Contrato AIR-015 | ID entre páginas/ajeno, componentes duplicados/ausentes | Reconstrucción, rechazo o ranking parcial explicado / ejecutado AIR-015 |
| T-47 | Contrato AIR-015 | Empate completo/secundarias desconocidas | Competición y TIE_UNRESOLVED, no ganador por ID / ejecutado AIR-015 |
| T-48 | Estático/integración | provider/host/token/asOf y bean real accidental | Registro DEMO y rechazo, sin transporte/credenciales / E13 parcial, P HTTP |
| T-49 | Publicación futura | Artefacto, debug/DevTools, TLS/proxy/CORS | Sin defaults de desarrollo, borde probado; no acreditado por unitarias / P |

## JSON y errores compatibles

Jackson Core/Databind 3.1.7 usa tools.jackson.*; annotations sigue com.fasterxml.jackson.annotation. Existen JsonFactoryBuilderCustomizer y JsonMapperBuilderCustomizer en org.springframework.boot.jackson.autoconfigure. Configurar y probar el conversor efectivamente usado por los DTO demo.

APIs verificadas: StreamReadConstraints para profundidad/documento/tokens/nombres/cadenas/números —maxTokenCount=10000—; StreamReadFeature.STRICT_DUPLICATE_DETECTION; DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES y FAIL_ON_TRAILING_TOKENS. Desactivar ACCEPT_FLOAT_AS_INT y coerciones escalares; LogicalType.Textual Integer/Float/Boolean como CoercionAction.Fail. FAIL_ON_NUMBERS_FOR_ENUMS pertenece a tools.jackson.databind.cfg.EnumFeature, no DeserializationFeature de Jackson 2.

Preservar presencia/default/null separando lectura y semántica. No activar globalmente FAIL_ON_MISSING_CREATOR_PROPERTIES/FAIL_ON_NULL_CREATOR_PROPERTIES: rompen omisiones/nulls lícitos. Comprobar tipo de token para importes/enteros; BigDecimal no conserva toda forma léxica de origen.

## Datos y aislamiento

Fixtures sintéticas y relojes/dobles inyectados. El doble del puerto registra invocaciones para probar que validación/403/413/429 no llegan al proveedor. No servicios externos, credenciales reales o PoC como fixtures.

Conservar regresión de bytecode y revisión de imports; complementarlas con ejecución local con salida de red impedida al probar el borde. Buscar transportes conocidos no equivale a captura exhaustiva; el cliente de integración solo apunta al servidor local. No añadir cliente de vuelos para probar que no se usa.

Observar errores por estado/código/forma/headers y ausencia de marcadores sintéticos en logs. No comparar exception.getMessage() completo ni registrar datos sensibles para depurar. Mantener causa operativa sanitizada/correlation y no convertir fallo en vacío.

## Gates posteriores

AIR-014 completó perfil/cadena/mapper/admisión y catálogo/búsqueda con filtros reales y Tomcat. AIR-015 está autorizada para comparación y conserva esos controles; su habilitación pública exacta, motor y regresiones se verifican por separado. La denegación403 anterior de /comparisons pertenece al histórico AIR-014, no al contrato final de AIR-015.

O-01 y el plazo absoluto de lectura fueron corregidos y probados en AIR-014; se conserva su regresión. Antes de publicar deben pasar casos HTTP/runtime pertinentes, resolver riesgos P0/P1 y contrastar valores operativos. Una validación demo no acredita por sí sola persistencia, TLS/HTTP2 o despliegue.

Cuando exista cambio ejecutable: Wrapper compile/test sin omitir pruebas, caché/offline si disponible, validación documental y diff. No repetir Maven indiscriminadamente por edición documental. Evidencia actual/histórica en [VALIDATION](VALIDATION.md).
