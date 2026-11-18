# Arquitectura y alcance del MVP

**Diseño AIR-010 y contratos AIR-012, materializados en el backend demo AIR-013/014/015.** Catálogo, búsqueda y comparación REST, arranque aislado y seguridad HTTP están implementados y verificados; la evidencia y sus límites se conservan en [VALIDATION](../developer-guide/VALIDATION.md). PostgreSQL/JPA/Flyway siguen sin integración de negocio. AIR-018 tiene autorización expresa para implementar el cliente React/TypeScript/Vite; este diseño por sí solo no autoriza otros cambios ni llamadas externas.

## MVP acordado

El primer incremento demostrable será una búsqueda y comparación con proveedor demo determinista, sin Internet, credenciales ni cuota. Los requisitos siguientes se implementarán en tareas posteriores, no durante AIR-010.

| ID | Requisito confirmado | Criterio funcional para la implementación futura |
|---|---|---|
| MVP-01 | Buscar por aeropuertos y fechas | Entre uno y tres orígenes explícitos y un destino del catálogo; una ciudad no amplía la consulta automáticamente |
| MVP-02 | Ida e ida/vuelta | Un adulto, cabina turista; ida/vuelta exige ambos trayectos y ámbito del precio explícito. Sin multicity, open-jaw ni combinación automática de billetes |
| MVP-03 | Itinerarios comprensibles | Segmentos, conexiones, aeropuertos, fechas/horas, aerolíneas y duraciones cuando existan; campos desconocidos visibles |
| MVP-04 | Filtrar y ordenar | Origen, aerolínea, escalas, duración y precio en una moneda; orden estable y paginación ligada a consulta y versión |
| MVP-05 | Comparar aeropuertos alternativos | Seleccionar de dos a cuatro ofertas de la misma búsqueda, incluso entre páginas; explicación por precio, duración o escalas sin mezclar monedas ni ámbitos |
| MVP-06 | Procedencia e incertidumbre | Demo siempre identificado; caché, búsqueda reciente, anuncio, confirmación de reserva, estimación y desconocido diferenciados; nada garantiza disponibilidad |
| MVP-07 | Vacíos, errores y parciales | Separar consulta vacía, filtros sin coincidencias y fallo total; conservar éxitos parciales y sus incidencias por fuente/origen |
| MVP-08 | Reproducibilidad | Fixtures, catálogo, calendario e instante simulado fijos por `datasetVersion`; misma entrada canónica produce los mismos datos/IDs/orden, salvo metadatos operativos |
| MVP-09 | Comparación explicable | Reglas deterministas del motor, preferencias por operación y subtotales separados. Una estimación terrestre manual es opcional y nunca un total exacto |

Para el MVP demo, el calendario admitido y su fecha de referencia se publicarán con cada versión del escenario; no se prometerán vuelos reales para fechas arbitrarias. El catálogo usa códigos de aeropuertos reales con metadatos mínimos verificados antes de incorporarlos; los vuelos y precios son sintéticos y las PoC privadas no se copian.

## Diferenciación y aplazamientos

Se aprueba un núcleo del Travel Cost Engine por reglas y preferencias transitorias; AIR-015 implementa su comparación. El mismo diseño admite estimaciones terrestres aportadas voluntariamente por el usuario, sin geocodificación ni direcciones privadas. Es posible comparar solo vuelos sin estimaciones. Fuentes automáticas, conversión monetaria, exploración económica con Travelpayouts y consultas detalladas con SerpApi se incorporarán solo tras tareas y permisos propios.

**Fuera del MVP inicial:** compra/emisión/reserva, pagos, chatbot, ML predictivo, alertas, suscripciones, infraestructura distribuida, APIs de pago obligatorias, cuentas, favoritos persistentes, historial, perfiles y recomendaciones personalizadas persistidas. Tampoco se aprueba búsqueda multicity o calendario flexible avanzado.

Posponer cuentas y favoritos evita autenticación, autorización por propietario, recuperación y eliminación de datos sin debilitar la demostración de portfolio: normalización, dinero, tiempos, adaptadores, accesibilidad y pruebas ya aportan complejidad útil. Las preferencias por operación sí entran. Las ideas de IMP-001 a IMP-007 permanecen propuestas; este diseño no las convierte en desarrollo autorizado.

## Monolito modular

Un backend desplegable como unidad con cuatro límites: **Airport, Flight Search, Flight Provider y Flight Comparison**. Son fronteras funcionales implementadas en paquetes del backend, sin módulos Maven ni servicios independientes. React/TypeScript es el cliente autorizado en AIR-018; PostgreSQL/JPA/Flyway están previstos/declarados, pero no hay caso de persistencia de negocio inicial.

Las dependencias, interfaces, riesgos y decisión de persistencia están en [MODULES.md](MODULES.md). El dominio se define en [DOMAIN_MODEL.md](DOMAIN_MODEL.md); los adaptadores en [PROVIDERS.md](PROVIDERS.md); las reglas en [TRAVEL_COST_ENGINE.md](TRAVEL_COST_ENGINE.md). Separar dominio, aplicación, infraestructura y API solo cuando haya comportamiento o fronteras concretos. No crear capas, librerías comunes ni módulos de seguridad vacíos.

## Experiencia de usuario prevista

El diseño AIR-010 acotó formulario y resultados con detalle/comparador, sin crear componentes o prototipos en esa tarea. AIR-018 los implementa en una página con secciones Buscar, Resultados y Comparar; [ADR-0006](../decisions/0006-demo-frontend.md) justifica navegación y herramientas. La tarea está En revisión por comprobaciones visuales pendientes.

1. Elegir modo demo visible, aeropuertos concretos y fechas; mostrar la condición un adulto/turista y el calendario simulado.
2. Buscar; anunciar carga y evitar envíos duplicados. Validar campos con mensajes asociados y resumen de errores enfocable.
3. Revisar tarjetas, detalle de ida/regreso, escalas y horarios locales con fecha y zona/desfase. Mostrar aeropuerto alternativo incluso si comparte ciudad.
4. Filtrar/ordenar y seleccionar ofertas. Al cambiar página se mantiene la selección de la consulta base; al cambiar consulta o versión se limpia. Si un filtro oculta una oferta elegida, avisar.
5. Comparar grupos compatibles y explicar claves, datos ausentes y componentes estimados; sin botones de compra ni supuesta reserva.

| Estado UX | Representación exigida |
|---|---|
| Vacío real | Consulta completada sin datos; permitir editar criterios |
| Vacío por filtros | Indicar que hay resultados fuera del filtro y permitir restablecerlo |
| Fallo total | Error y reintento explícito; nunca “no hay vuelos” como sustitución |
| Parcial | Mostrar lo útil y qué orígenes/fuentes fallaron; parcial vacío sigue avisando del fallo |
| Incompleto | Identificar precio/equipaje/horario desconocido y su efecto en comparación |
| Caducado | Marcar antigüedad o simulación; sin recomendación vigente; repetir búsqueda explícitamente |
| Versión retirada | Explicar conflicto y reiniciar el contexto; no reutilizar IDs con otra versión |

Teclado, foco visible y retorno previsible al cerrar detalles/comparador; etiquetas y estructura semántica, mensajes breves de estado, sin depender solo de color. En móvil, tarjetas y comparación por atributos con lectura lineal, zoom y sin tabla ancha como único acceso. Componentes, pruebas y límites de AIR-018 se documentan en [FRONTEND](../developer-guide/FRONTEND.md): los tests automatizados no sustituyen la revisión pendiente de teclado real y layout en navegador.

## Contratos, seguridad y decisiones

[API_DESIGN.md](../api/API_DESIGN.md) ratifica en AIR-012 las tres rutas demo y sus DTO, implementados en AIR-014/015. [NORMALIZED_CONTRACTS.md](NORMALIZED_CONTRACTS.md) fija campos/invariantes y [BASELINE.md](../security/BASELINE.md) la política de seguridad concretada en AIR-016 e implementada en el backend demo.

Se aceptan ADR-0002 y ADR-0003, con contratos ratificados en AIR-012 e historial conservado. [ADR-0004](../decisions/0004-demo-mvp-and-persistence.md) registra alcance demo y ausencia de BD/cuentas. ADR-0001 conserva el historial de la base generada. Permisos y coste permanecen en AIR-019, según la [evidencia histórica](PROVIDER_EVIDENCE.md).

AIR-011 comprobó base Java/Wrapper y contexto demo de prueba. AIR-012 ratifica enums, payloads, cotas de entrada, reglas y ejemplos. AIR-013 materializa fixtures sintéticas y el proveedor demo, con referencias mínimas manuales. AIR-014/015 completan ejecución demo, seguridad HTTP, endpoints y comparación. Permanecen pendientes un catálogo externo verificado/licenciado si se incorpora y cualquier esquema de persistencia futuro. Cada tarea posterior necesita autorización propia.
