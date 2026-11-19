# Política de contratos y proveedores

Airwise implementa catálogo, búsqueda y comparación REST **demo** mediante los tres endpoints descritos en [API_DESIGN.md](API_DESIGN.md), con frontend local de AIR-018. No hay adaptadores reales habilitados ni una especificación OpenAPI generada. La existencia del cliente demo no habilita datos reales.

AIR-010 fijó el diseño y AIR-012 sus contratos; AIR-013/014/015 implementaron el backend demo. [PROVIDERS.md](../architecture/PROVIDERS.md) distingue esa implementación de los límites futuros. AIR-019 añade la [evaluación documental de permisos y presupuesto](PROVIDER_ASSESSMENT.md), fechada y con preguntas pendientes, y [ADR-0007](../decisions/0007-real-provider-admission.md). No sustituye permisos de cuenta ni autoriza integración.

## Categorías de información

| Categoría | Qué representa | Obligación del modelo/UI |
|---|---|---|
| Precio observado | Registro previo de una búsqueda o caché | Identificar fuente y tiempos conocidos; disponibilidad pendiente |
| Resultado reciente de búsqueda | Respuesta obtenida en un contexto de viaje, con caché posible | Conservar parámetros, fase y ámbito; no prometer inventario actual |
| Estimación | Cálculo con fuente o supuestos, p. ej. transporte terrestre | Mostrar esos supuestos y no mezclarlo con precio confirmado |
| Precio anunciado por vendedor | Importe publicado por un vendedor con condiciones conocidas | No equivale a reserva ni confirmación de disponibilidad |
| Precio confirmado en reserva | Confirmación concreta de reserva y condiciones verificadas | Exigir evidencia; fuera del flujo MVP y nunca producido como confirmación real por demo |
| Desconocido | Dato que la fuente no aporta o no permite determinar | Nulo y motivo, nunca cero o servicio incluido por defecto |

Cada adaptador deberá declarar capacidades y límites. Separar DTO externo y modelo normalizado; importe `BigDecimal`, moneda, trayectos cubiertos, datos desconocidos y procedencia explícitos. El demo debe ser sintético y visible.

## Restricciones vigentes

- **Ninguna llamada a Travelpayouts o SerpApi sin autorización específica**, incluidos endpoints de cuenta, continuación y opciones de vendedor. AIR-019 lee solo páginas públicas de documentación/condiciones, realiza cero llamadas API y no repite PoC; fuentes reales deshabilitadas por defecto.
- Presupuesto económico 0 €; no planes de pago, datos bancarios, renovaciones o créditos adicionales.
- Variables previstas: `TRAVELPAYOUTS_API_TOKEN` y `SERPAPI_API_KEY`; custodia exclusivamente en configuración externa del backend. Sus valores nunca deben ir al repositorio, frontend, informes, logs, tests o CI.
- Integración posterior sujeta a cerrar las preguntas aplicables de la evaluación, acreditar permisos de visualización/retención/reutilización y terceros, cuota efectiva y autorización de operaciones/máximo de intentos. No se contactará con proveedores sin petición autorizada.
- El plan de consumo versionado está en la evaluación: separar intentos HTTP de créditos comerciales y reservar conservadoramente de forma atómica/durable antes de enviar. Saldo o consumo incierto no permite enviar ni liberar reservas por inferencia. **Control diseñado, todavía sin implementación para proveedores reales.**
- Caché técnica del servicio, retención en servidores del proveedor y licencia de almacenamiento/publicación de Airwise son condiciones distintas; no copiar sus plazos como permiso implícito.
- No seguir tokens ni enlaces automáticamente, no tratar identificadores temporales como permanentes y no ejecutar mecanismos de compra por inferencia del contexto.
- No asumir equipaje incluido, peso permitido, condiciones de cambios/cancelación ni costes adicionales exactos cuando falten datos fiables por vendedor.
- Clasificación documental actual de ambos: **PENDIENTE DE ACLARACIÓN** para portfolio público multifuente; no es una capacidad operativa ni permiso provisional. La matriz de permitidos/prohibidos/no aclarados y las preguntas concretas están en la evaluación.
- Ninguna petición a proveedores de vuelos desde tests/CI ni al cargar componentes. Se permiten dobles sintéticos/HTTP local sin proveedores reales. Sin fallback silencioso a demo.
- Diseño de presupuesto: topes diarios/mensuales por operación y agregados por proveedor/cuenta, todos0por defecto; también límites comerciales y de concurrencia. Registro o saldo inaccesibles, periodo o coste desconocidos cierran admisión. Reiniciar o renovar una ventana no elimina reservas inciertas; un intento HTTP enviado no se devuelve como si no hubiese ocurrido. No hay política de consumo real implementada.

Los tests futuros de adaptadores serán sintéticos y locales. La evidencia histórica está en [PROVIDER_EVIDENCE.md](../architecture/PROVIDER_EVIDENCE.md); el contrato de dominio y errores pertenece a AIR-012. La revisión documental AIR-019 no altera las capacidades operativas UNVERIFIED ni resuelve las preguntas externas registradas.

## Núcleo sintético AIR-021

El adjunto posterior promueve IMP-008 y autoriza un núcleo Java aislado de admisión y estimación. [PROVIDER_ADMISSION](../architecture/PROVIDER_ADMISSION.md) y [ADR-0008](../decisions/0008-synthetic-admission-ledger.md) delimitan contratos y garantías; implementado y verificado en AIR-021 con42tests nuevos y regresión280 satisfactoria, sin cambiar el demo.

Su ledger en memoria solo sirve para un escenario sintético que comparte una misma instancia: no protege reinicios, dos ledgers, réplicas o procesos. Los alcances no sintéticos se rechazan; no se incorpora transporte ni se habilita un proveedor real por obtener un permiso sintético. La política durable y los límites técnicos minuto/hora de una futura integración siguen pendientes, vinculados a AIR-017 y autorización posterior.

La reserva del máximo multipaso no confirma vuelos ni permisos de uso público. Incertidumbre y fallos conservan compromisos; un intento iniciado no se devuelve automáticamente. Ninguna API o credencial participa en la prueba de este núcleo. Los permisos pendientes de AIR-019 se mantienen.
