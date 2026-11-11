# Seguridad de Airwise

Airwise está en preparación y no tiene versiones publicadas ni servicio público desplegado. La dependencia de Spring Security está declarada; la autenticación, autorización y el endurecimiento de negocio aún no se han implementado ni verificado.

## Notificar un problema

No publiques credenciales, datos personales ni detalles de explotación en una incidencia pública. Utiliza el canal privado del mantenedor cuando exista; GitHub Private Vulnerability Reporting solo si el repositorio lo tiene habilitado. Actualmente no se ha verificado un repositorio remoto, un correo público de seguridad ni ese mecanismo. Solicita un canal privado sin adjuntar información sensible si no está disponible. No se promete un plazo de respuesta no acordado.

## Secretos y uso de proveedores

Las claves de Travelpayouts y SerpApi deben permanecer fuera del repositorio y del frontend, en el entorno o un gestor autorizado. No se deben imprimir valores, URLs autenticadas, payloads de reserva ni el entorno completo. Los nombres de variables pueden documentarse; sus valores no.

El presupuesto de APIs es 0 €. Esta fase prohíbe llamadas reales, incluso a endpoints de cuenta que no cobren búsquedas. No actives pagos, renovaciones o créditos ni ejecutes las PoC anteriores. Los datos demo deben identificarse; no son sustitutos silenciosos de errores.

Si se descubre un secreto, no lo copies a informes. Informa de la ubicación y coordina su revocación por el titular mediante un canal privado; no reescribas historial Git o cambies credenciales sin autorización.

Los criterios técnicos iniciales y las tareas futuras están en [docs/security/BASELINE.md](docs/security/BASELINE.md) y el [backlog](planning/BACKLOG.md).
