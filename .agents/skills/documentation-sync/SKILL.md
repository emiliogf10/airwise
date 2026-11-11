---
name: documentation-sync
description: "Actualizar documentación de Airwise tras una decisión o cambio autorizado, manteniendo coherencia entre implementación, README, ADR, manuales y planificación."
---

# Sincronizar documentación desde la evidencia

1. Identifica la fuente de verdad: POM para dependencias declaradas, código/contrato para comportamiento, pruebas para resultados y ADR para decisiones. Una propuesta no sustituye a una implementación.
2. Revisa solo las secciones afectadas: README, guía de desarrollo, manual de usuario, arquitectura, API, seguridad y planificación. Mantén cada dato en un lugar principal y enlázalo desde el resto.
3. Distingue presente, previsto y pendiente. No documentes pantallas o comandos de componentes inexistentes ni hagas pasar una muestra externa por una prueba del checkout actual.
4. Conserva Javadoc existente; modificarlo exige que la tarea autorice editar ese código. Documenta contratos e invariantes útiles sin repetir getters o código obvio.
5. Verifica rutas, enlaces y consistencia de estados AIR. Añade evidencia al cerrar tareas; usa `Unreleased` en el changelog mientras no exista una versión publicada.

Entrega: documentos actualizados, fuente utilizada, enlaces comprobados y cuestiones pendientes, sin duplicar manuales extensos ni exponer secretos.
