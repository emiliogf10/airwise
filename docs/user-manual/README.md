# Manual de usuario — demo de Airwise

El cliente AIR-018 incorpora búsqueda, resultados y comparación en una página, con navegación **Buscar**, **Resultados** y **Comparar**. La tarea está **Hecha**, con flujos, teclado y tamaños de pantalla revisados en navegador real dentro del alcance del [informe](../developer-guide/BROWSER_REVIEW.md). Para instalar y conectar los dos procesos locales, consulte la [guía frontend](../developer-guide/FRONTEND.md).

**Todo el contenido de vuelos es sintético.** El modo demo no confirma disponibilidad, reserva, precios comerciales ni equipaje real. No hay registro, compra, favoritos persistentes, historial o alertas. Los datos y la selección pertenecen a esta sesión de página; no son una búsqueda guardada.

## Buscar alternativas

1. Espere a que se carguen los aeropuertos del backend. Marque de uno a tres orígenes concretos y elija un destino diferente de todos ellos.
2. Seleccione **Solo ida** o **Ida y vuelta**. Introduzca fechas dentro del calendario simulado que aparece junto al formulario; la vuelta no puede ser anterior a la ida. El alcance es un adulto en turista.
3. Pulse **Buscar vuelos**. **Cargar ejemplo demo** prepara OPO y SCQ hacia MAD, ida/vuelta del 10 al 17 de febrero de 2027, cuando está disponible `demo-v1`; después debe enviar la búsqueda.

No todas las rutas/fechas del calendario tienen fixtures. Una búsqueda completada puede no tener alternativas. Si falta un criterio o una fecha no es válida, revise el resumen de errores y los mensajes asociados a los campos.

## Leer resultados y filtrar

Cada tarjeta muestra ida y, si corresponde, vuelta; horarios locales con su contexto, duración, conexiones, importe/moneda y evidencia. **Ver itinerario y condiciones** despliega segmentos, paradas técnicas, equipaje, cobertura, impuestos, procedencia y vigencia simulada. La duración de vuelo excluye estancia y transporte terrestre.

**Desconocido** no significa cero ni gratuito. Un precio observado, reciente o anunciado ilustra una clase de evidencia; en demo sigue siendo sintético. Las observaciones sin itinerario completo aparecen aparte y no pueden seleccionarse para comparar.

En **Filtrar y ordenar** puede limitar origen, aerolínea, conexiones, duración, moneda y precio máximo. El precio exige una moneda y decimal con punto. Pulse **Aplicar filtros**; el servidor aplica el cambio al conjunto antes de paginar. **Restablecer filtros** elimina las restricciones. **Anterior** y **Siguiente** recorren las páginas. El orden por precio compara dentro de grupos compatibles: el primer grupo no es un ganador comercial global.

## Comparar de dos a cuatro ofertas

Marque **Seleccionar** en las tarjetas y vaya a **Comparar**. La selección se conserva al cambiar filtros, orden o página; se avisa si algún seleccionado no está visible. **Quitar** elimina una alternativa. Una nueva consulta base o versión inicia otra selección.

Elija precio de vuelo, duración, conexiones o subtotal conocido con estimaciones y pulse **Comparar selección**. Las clasificaciones y recomendaciones proceden del servidor, separadas por grupos compatibles. **Sin clasificar**, clasificación parcial, motivos y valores ausentes explican sus límites. No hay ganador global entre monedas, evidencias o condiciones diferentes.

Para el subtotal, marque los componentes terrestres requeridos e introduzca voluntariamente sus estimaciones por oferta/componente. Son importes para el grupo de un adulto y deben usar la misma moneda del vuelo cuando se conoce. Use punto decimal; un campo vacío sigue siendo desconocido y cero es una estimación explícita. El servidor calcula el **subtotal de componentes seleccionados con estimaciones**; no es el coste total exacto del viaje ni añade equipaje desconocido.

## Estados y problemas de conexión

La interfaz distingue carga, vacío del escenario, vacío por filtros, resultado parcial y fallo total. Un resultado parcial puede estar vacío y conserva sus incidencias por origen/fuente. Un fallo no se transforma en vuelos demo inventados ni en una búsqueda vacía.

Ante un error de conexión, compruebe el backend local y use **Reintentar** cuando se ofrece. Si la versión deja de estar disponible, **Reiniciar catálogo y búsqueda** elimina el contexto anterior. La referencia de error sirve para correlacionar el diagnóstico sin mostrar detalles internos. La navegación incluye **Saltar al contenido**, controles nativos y mensajes de estado; las comprobaciones ejecutadas de accesibilidad y tamaños se detallan en el registro de validación.

El [README](../../README.md) y el [roadmap](../../planning/ROADMAP.md) indican el estado real y los hitos siguientes. El uso técnico directo de la API permanece en [DEMO_USAGE](../api/DEMO_USAGE.md).
