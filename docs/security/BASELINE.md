# Criterios iniciales de seguridad y calidad

Estas son condiciones de diseño para tareas futuras, no controles de negocio ya implementados. El backend declara Spring Security y Validation; solo tiene arranque y test generados. Su comportamiento en ejecución no se comprobó.

## Criterios del MVP que deben acordarse en AIR-016

- Delimitar superficies públicas y privadas. Una demo puede no necesitar cuentas; no introducir autenticación de usuarios sin caso de uso aprobado.
- Validar entradas y expresar errores de manera estable, sin credenciales, trazas sensibles ni cuerpos remotos completos. Fallo y vacío son estados distintos.
- Configuración externa para secretos y permisos mínimos. No exponer claves a clientes ni registrar URLs autenticadas. `.gitignore` ayuda a evitar accidentes; no es un escáner de secretos ni una frontera de seguridad.
- Identificar datos personales y justificar conservación, acceso y eliminación antes de persistirlos. No usar datos de producción en tests.
- Probar comportamiento negativo, autorización cuando exista y aislamiento de proveedores. No consumir APIs en tests.
- Revisar dependencias resueltas y compatibilidad con la versión real del backend. No afirmar ausencia de vulnerabilidades por una revisión documental.
- Configurar seguridad de observabilidad, CORS/CSRF y sesiones/tokens según el flujo elegido, sin desactivar controles por comodidad ni aplicar recetas ajenas sin contexto.

## Límites de esta entrega

No se han hecho auditorías de dependencias resueltas, pruebas de penetración, arranque de Spring, configuración de BD, contenedores ni despliegue. No se ha afirmado que la seguridad por defecto cubra las necesidades de Airwise. Para comunicar un problema utilice [SECURITY.md](../../SECURITY.md).
