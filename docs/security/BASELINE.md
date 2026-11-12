# Requisitos de seguridad y calidad del MVP

**Diseño acordado en AIR-010; no controles implementados ni pruebas de producto superadas.** Spring Security/Validation están declarados, pero solo existen arranque y test generados. AIR-011 verificará entorno; AIR-016 detallará configuración y pruebas ejecutables sin iniciarse en AIR-010.

## Superficies y confianza

| Superficie prevista | Acceso y límites |
|---|---|
| Catálogo, búsqueda y comparación demo | Públicos sin cuenta, solo fixtures/capacidades conocidas; entrada/tamaño/tiempo acotados |
| Datos de vuelos del navegador | No autoritativos: reconstruir ofertas desde consulta + versión + IDs; fingerprint no autentica |
| Estimaciones/preferencias del usuario | Aceptar solo campos definidos, importes/ámbitos válidos y procedencia explícita; sin modificar tarifa de vuelo |
| Configuración, adaptadores, presupuesto y diagnóstico | Internos; sin rutas públicas ni activación de fuente real por parámetro |
| Actuator/operación | Política de exposición/acceso separada, denegar por defecto salvo necesidad justificada; dependencia no demuestra protección |

No login, cuentas, recuperación, pagos ni favoritos en MVP. No almacenar búsquedas en servidor o navegador por obligación del diseño. No direcciones personales, geolocalización, perfiles ni texto libre innecesario. Los recursos demo no copian payloads privados de PoC.

## Validación y abuso

Aplicar validación estructural y de dominio en servidor. AIR-012 ratifica campos, normalización y límites en [API_DESIGN.md](../api/API_DESIGN.md) y su contrato normalizado. Orígenes 1–3, un adulto/ECONOMY, 2–4 ofertas, pageSize 1–50, page 0–100000, cuerpo máximo 65536 bytes y profundidad 12. CorrelationId UUID generado por servidor y errores con rutas/códigos propios. La configuración efectiva de seguridad sigue en AIR-016.

Rechazar rangos/fechas incoherentes, origen=destino, IDs repetidos/ajenos, versión retirada, campos de proveedor, importes no válidos y suma duplicada de componentes. Nunca interpretar propiedades/expresiones arbitrarias, URLs o callbacks del cliente como instrucciones de búsqueda. Limitar también tiempo total, resultados y concurrencia de la demo para evitar consumo de CPU/memoria; valores operativos y rate limiting se concretarán antes de publicación.

El demo fija reloj de escenario por versión, distinto del reloj real de limitación operativa. Un `asOf` del cliente no puede rejuvenecer precios. No se diseñan firmas o sesiones innecesarias: datos públicos se revalidan y reconstruyen.

## Dinero, tiempos y errores

BigDecimal, moneda y ámbito explícitos. Desconocido no vale cero; no comparar distintas monedas, ida con ida/vuelta ni caché con anuncio como equivalentes. Precio anunciado no implica reserva. Mostrar campos/equipaje/tasas desconocidos y subtotales estimados sin prometer coste total.

Calcular duración/conexión con instantes fiables y zonas; no restar horas locales de zonas distintas. Ida/vuelta suma trayectos y excluye estancia. No afirmar probabilidad estadística de perder conexión ni conexión garantizada por una heurística.

Vacío válido, fallo total y parcial son estados distintos. Una unidad requerida completada junto a otra fallida o no soportada implica PARTIAL, nunca cobertura completa. Conservar datos útiles y causa sanitizada por fuente/origen. Sin fallback oculto a demo, sin listas vacías ante timeout/cuota. Respuestas/logs no contienen cuerpos remotos, URLs autenticadas, tokens, secretos o trazas internas; correlationId para diagnóstico. No registrar cuerpos completos de búsquedas/comparaciones ni errores con entrada sensible.

## Secretos, consumo y retención futuros

Secretos solo en configuración externa del servidor; nunca Git, React, informes o logs. `.gitignore` previene accidentes, no detecta todos los secretos ni protege un endpoint.

Fuentes reales deshabilitadas y presupuesto de llamadas inicial cero. Antes de autorizarlas: derechos de uso/retención, cuota actual, coste máximo de flujo, reservas atómicas por petición, timeouts, límite de intentos y concurrencia. Continuaciones y reintentos cuentan; consumo incierto se cuenta conservadoramente. Mantener gasto económico máximo 0 € y rechazar si no se puede demostrar admisión. No comprobar cuotas con llamadas de cuenta durante AIR-010.

Retención “solo en memoria” también requiere permisos; no guardar datos reales por defecto. Datos personales futuros exigirían finalidad, acceso, plazo y borrado antes de persistir. Límites y diseño de adaptadores en [PROVIDERS.md](../architecture/PROVIDERS.md).

## Transporte, origen y operación

Preferir misma procedencia para cliente/API inicial. Si desarrollo exige orígenes distintos, lista exacta; no comodín con credenciales. La ausencia de cookies de autenticación simplifica el flujo, pero AIR-016 debe justificar CORS/CSRF, sesiones, headers y exposición efectiva; no desactivar controles globalmente por conveniencia.

No asumir que seguridad por defecto de Spring cubre el contrato o que existe una cuenta por defecto utilizable. Antes de publicar deberán existir pruebas de límites, sanitización y política operativa; no se configura nada en este diseño.

## Estrategia de pruebas futura

| Nivel / tarea | Casos mínimos de calidad |
|---|---|
| Dominio, AIR-012/013 | Ida/ida-vuelta, múltiples segmentos, DST/offset incoherente, ámbito monetario, cero vs desconocido, caducidad y procedencia |
| Motor, AIR-015 | Compatibilidad, monedas/evidencias separadas, ranking/subconjunto evaluable, empates, componentes terrestres ausentes/duplicados y ninguna probabilidad inventada |
| Adaptadores, AIR-012/013 y futura integración | Capacidades no fingidas, normalización, campos ausentes, duplicados, fases incompletas, parcial/total y ausencia de fallback |
| HTTP, AIR-014/016 | Fechas/enums/cotas/cuerpos, errores sanitizados, versión/IDs/contexto/paginación, respuesta desconocida vs cero y ausencia de tokens |
| Integración local, AIR-011/016 y componentes autorizados | Wiring con demo/reloj inyectados, reconstrucción de selección, independencia de BD real, límites y manejo de excepciones |
| UI, AIR-018 | Teclado/foco/zoom/móvil; carga, vacío real/por filtros, parcial, caducado, selección entre páginas y reinicio de consulta |

La matriz de contratos se ejecutará con fixtures/dobles al implementar cada componente. AIR-011 ya comprobó Jupiter/Platform 6.0.3 y el contexto `demoContextLoadsWithoutPersistence`, con un test satisfactorio y exclusión de DataSource limitada a esa prueba. PostgreSQL/JPA/Flyway siguen pendientes de integración. Testcontainers se evaluará si existe persistencia real autorizada.

Criterios para cada entrega futura: invariantes relevantes comprobadas, negativos/errores cubiertos, ningún fallo transformado en demo/vacío, evidencia de comando/resultado y limitaciones. Seguridad/configuración no equivale a auditoría de vulnerabilidades. No se han auditado dependencias resueltas, arrancado Spring, configurado BD o realizado pentest. Reporte de problemas en [SECURITY.md](../../SECURITY.md).
