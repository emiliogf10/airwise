---
name: api-integration
description: "Investigar o preparar un adaptador de proveedor de vuelos de Airwise, verificando contrato, coste, semántica y permisos antes de una integración autorizada."
---

# Validar un proveedor antes de integrarlo

Consulta `docs/api/PROVIDER_POLICY.md`, el contrato aprobado y la tarea AIR. En la fase organizativa solo se permite documentar: no enviar solicitudes reales ni incorporar adaptadores.

Para una integración posterior autorizada:

1. Verifica documentación oficial vigente, autenticación, cuota, coste, caché, retención y publicación. Separa documentado, observado, parcial y pendiente; una clave no demuestra permiso de redistribución.
2. Define capacidades y ámbito del precio: observado, búsqueda reciente, estimación o confirmación; ida frente a viaje completo; moneda y servicios desconocidos. Modela datos ausentes explícitamente.
3. Prepara mapeo de DTO externo a modelo del dominio y tests sintéticos para errores HTTP, semánticos, vacíos, parcialidad, precisión monetaria y secretos. Evita acoplar el dominio al token temporal del proveedor.
4. Solo con autorización concreta de prueba real y presupuesto verificable, fija un máximo persistente antes del envío. Una continuación consume otra petición posible; no sigas tokens, reintentes ni actives pagos automáticamente.
5. Registra evidencia sanitizada sin credenciales ni URLs autenticadas. Una opción de vendedor no equivale a una reserva confirmada.

Entrega: contrato y capacidades, cambios autorizados, evidencia, consumo conocido/desconocido y limitaciones. No uses datos demo silenciosamente ante un fallo real.
