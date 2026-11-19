# Revisión de navegador de AIR-018

Revisión exploratoria ejecutada el 18/11/2026 por el coordinador con la skill `agent-browser`: CLI 0.39.1, Chrome 155.0.8059.39 headless y axe 4.12.1. Sesión y perfil temporales dedicados, dominio permitido `127.0.0.1`, backend demo y Vite locales. No se usó el perfil personal de Brave, no hubo llamadas a proveedores ni cambios backend o de configuración global.

La matriz local BR-01 a BR-18 registra 18 comprobaciones asistidas satisfactorias sobre React y CSS renderizados, teclado y los tres endpoints reales. Se distingue de las suites unitarias, del cliente HTTP y de App/jsdom documentadas en [VALIDATION](VALIDATION.md). Es una revisión exploratoria acotada, no una suite E2E versionada ni una certificación completa de accesibilidad.

## Comportamientos comprobados

| Área | Observación ejecutada |
|---|---|
| Catálogo y búsqueda | Ida/vuelta OPO/SCQ→MAD, 10–17 de febrero de 2027: dos ofertas completas y observación separada de 45 EUR |
| Comparación | Dos seleccionados, cuatro criterios del servidor. Subtotal sin estimaciones permanece desconocido; con 20 EUR por alternativa produce 120/130 EUR. No se calcula en el navegador |
| Desconocidos | Escenario OPO del 12 de febrero: ocho ofertas con cero, USD y precio/vigencia desconocidos. Comparar 0 EUR con 105 EUR de vigencia desconocida deja esta última sin rango, sin recomendación ante una sola opción evaluable |
| Selección y vistas | Cuatro seleccionados deshabilitan las otras cuatro opciones. `PRICE_ASC`, tamaño 2 y página 2 conservan los IDs y avisan de seleccionados ocultos. Una nueva consulta base limpia la selección |
| Filtros y monedas | USD/precio máximo 100 reduce ocho ofertas a una. Comparación de 95 USD con 0 EUR devuelve dos grupos, sin ganador global |
| Estados | 13 de febrero produce 503 y error visible, 14 de febrero vacío completado; ida/vuelta 11–18 de febrero conserva parcialidad e incidencia de SCQ. Un fallo no se presenta como ausencia de vuelos |
| Teclado y errores | Tab/Enter en salto enfoca H1 con foco visible; Space cambia orígenes/selección; Tab a summary y Enter abre detalles; envío inválido enfoca resumen con errores asociados. Vuelta del 9 de febrero anterior a ida del 10 de febrero se rechaza |
| Consola | Sin excepciones JavaScript observadas. La respuesta 503 esperada y mensajes informativos de Vite no se interpretan como errores de ejecución de la UI |

## Responsive y accesibilidad

Se inspeccionaron capturas de los viewports 1440×1000, 768×1024, 720×900, 390×844 y 320×740. Formulario, resultados, estimaciones y grupos mantuvieron lectura y ajuste de texto; `scrollWidth` coincidió con la anchura del viewport en las comprobaciones realizadas. En escritorio 1440, ampliar el texto por CSS al 200%/32 px no produjo desbordamiento horizontal. Esto es redimensionado CSS de texto, **no una prueba de zoom nativo del navegador**.

Se reprodujo una incidencia de severidad media: **ISSUE-001**, antetítulo «Vuela con criterio» con color efectivo #00685f sobre #173342 y contraste 1,97:1. La paleta calculada previamente no detectaba el efecto real de la cascada. Frontend trasladó la regla `.hero .eyebrow` a la capa de componentes, tras `.eyebrow`, sin `!important`. La reverificación observó #abd7be y contraste 8,31:1. La incidencia quedó corregida y reverificada.

Ocho auditorías axe posteriores a la corrección devolvieron cero violaciones y una regla incompleta que agrupa de 3 a 9 nodos decorativos según el estado (puntos y flechas con `aria-hidden`). Se revisó manualmente su redundancia respecto al contenido textual y los nombres accesibles, incluido «Modo demo». Esto no equivale a conformidad WCAG completa, evaluación con lector de pantalla, cobertura de todos los navegadores o dispositivos físicos.

## Evidencia y límites

Informe de trabajo, auditorías JSON y capturas: `.local/air018/browser-review/report.md`, `.local/air018/browser-review/` y sus archivos asociados. Son evidencias locales ignoradas por Git; no se presentan enlaces que dependan de su presencia en un clon ni se copian datos sensibles a documentación pública. Este informe permanente conserva los hechos verificables y su alcance.

El intento anterior con Computer Use quedó bloqueado incluso después de autorizar Brave porque no podía verificar con confianza la URL de la ventana. La nueva revisión usa otro mecanismo autorizado y operativo; no atribuye éxitos al intento fallido ni elimina ese histórico. Una interrupción de sesión posterior requirió relanzar servicios y navegador, conservando capturas; no se identificó como fallo de la aplicación.

Tras corregir CSS, el coordinador repitió tests ordinarios (53 satisfactorios y una integración opt-in omitida), typecheck, build y lint, todos con código 0 y sin advertencias. Se conservaron los 109 hashes backend. El navegador se cerró mediante la CLI y se verificó la detención de Java/Vite y la ausencia de listeners en 8080/5173.

Security & QA realizó una revisión independiente acotada del informe, ocho JSON de axe, JSON de Vitest, diff CSS y capturas 768/320. Confirmó los resultados registrados y no identificó bloqueos reproducibles en ese alcance; no se le atribuye ejecución de las pruebas del coordinador. Con documentación y planificación sincronizadas, AIR-018 queda **Hecha**. No se inicia otra fase ni se publica una versión.
