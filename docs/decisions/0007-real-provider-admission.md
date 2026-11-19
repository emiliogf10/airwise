# ADR-0007 — Admisión de proveedores reales con presupuesto de 0 €

- Estado: **Aceptado para el alcance documental de AIR-019**; no autoriza integración, publicación de datos reales ni llamadas.
- Fecha: 2026-11-19.
- Ampliación del mismo día según adjunto AIR-019: clasificación explícita, matriz técnica y topes diarios/mensuales; conserva la decisión de no habilitar fuentes reales.
- Antecedente: [ADR-0003](0003-provider-boundaries.md).

## Contexto

El demo ya ofrece búsqueda y comparación local. Las PoC previas acreditan observaciones históricas; no acreditan permisos vigentes de reutilización ni cuota de cuenta. AIR-019 autoriza investigar fuentes oficiales públicas. La [evaluación](../api/PROVIDER_ASSESSMENT.md) separa condiciones documentadas, inferencias de Airwise y dudas pendientes, con fecha y fuentes.

## Decisión

Mantener DEMO como única fuente habilitada. Travelpayouts Data API y SerpApi Google Flights conservan capacidades operativas UNVERIFIED y ningún adaptador real implementado. La viabilidad documental de un plan gratuito no habilita por sí sola un servicio público.

Clasificar ambos como **PENDIENTE DE ACLARACIÓN** para el portfolio público multifuente. La evidencia técnica permite evaluar un encaje futuro, pero faltan permisos decisivos de uso/retención y datos de cuenta. No equivale a APTO CON CONDICIONES operativas ni a NO APTO definitivo; las preguntas Q-T/Q-S/Q-B determinan qué evidencia falta.

Antes de una tarea posterior de integración deben acreditarse los permisos aplicables al uso concreto (comparación, visualización vía backend/frontend, retención, atribución y terceros), el plan/cuota y una autorización expresa de proveedor, operaciones y máximo de intentos. Las dudas Q-T/Q-S/Q-B de la evaluación son puertas de entrada; no se presume permiso por silencio. No contactar con proveedores sin autorización.

Adoptar el plan persistente de consumo de la evaluación: gasto máximo 0 €, sin pagos/recargas; distinguir intentos HTTP de créditos comerciales, estimar todas las fases y reservar de forma atómica/durable antes de enviar. Saldo o consumo desconocidos cierran admisión. Este control deberá implementarse y probarse con dobles en una tarea posterior autorizada; no existe ahora un ledger de consumo real.

Topes configurables diarios/mensuales por operación y agregados por proveedor/cuenta, inicialmente0, con ventanas propias separadas del ciclo comercial. Fallo de ledger, reloj/periodo o saldo sin evidencia vigente impiden enviar; reinicio no borra reservas inciertas. Sin peticiones de proveedor en tests/CI o por carga de UI. La propuesta IMP-008 solo contempla un futuro núcleo con dobles, sin autorizar implementación ni almacenamiento ahora.

## Consecuencias

AIR-019 puede completarse con preguntas externas explícitas y evidencia documental revisada. El cierre no resuelve esas preguntas ni inicia adaptadores. Caché técnica del proveedor, retención en sus servidores y licencia de almacenamiento/publicación de Airwise se gestionan por separado. Se evita atribuir a Free una protección o capacidad de un plan pagado.

La arquitectura de adaptadores y el contrato airwise-v1 permanecen intactos. No se modifica backend, frontend, dependencias, persistencia ni configuración. El siguiente trabajo de producto u operación requiere petición propia; el demo sigue siendo utilizable sin proveedores reales.

Evolución posterior: el adjunto de promoción IMP-008 autoriza AIR-021, exclusivamente núcleo sintético y tests nuevos. [ADR-0008](0008-synthetic-admission-ledger.md) complementa esta decisión: memoria no satisface la durabilidad exigida aquí ni habilita datos reales. Las frases de ausencia de código/implementación de este ADR conservan su alcance histórico AIR-019.
