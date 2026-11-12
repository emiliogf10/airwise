# ADR-0004 — MVP demo sin cuentas ni persistencia de negocio

- Estado: **Aceptado como diseño en AIR-010**.
- Fecha: 2026-11-11.
- Origen: petición explícita de definir el MVP, integrada con análisis reales de backend, database, frontend y security_qa.

## Contexto

Se necesita demostrar búsquedas ida/ida-vuelta, aeropuertos alternativos, normalización, comparación explicable y estados difíciles sin depender de APIs de pago. JPA/Flyway/PostgreSQL declarados no obligan a usar una BD.

## Decisión

MVP con un adulto/turista, 1–3 orígenes, un destino, filtros/orden/paginación, 2–4 ofertas a comparar y demo sin red/credenciales. Preferencias por operación y estimaciones terrestres opcionales, claramente separadas; sin total exacto cuando falten datos.

Aplazar cuentas, favoritos persistentes, historial, alertas, ML, compra/pagos y proveedores reales. No desarrollar IMP-001 por la presencia de JPA. El valor de portfolio proviene primero del dominio y sus contratos/pruebas/UX, sin añadir identidad/privacidad por apariencia.

Catálogo mínimo y fixtures versionados, sin payloads privados. Reloj de escenario fijo por versión, distinto del reloj operativo. Solicitudes/resultados/selecciones transitorios; ninguna entidad JPA o migración inicial necesaria.

Comparación demo rederiva las ofertas desde consulta canónica + datasetVersion + IDs y valida pertenencia. No confiar importes/fuente enviados por cliente ni introducir un repositorio de búsquedas solo para comparar.

## Consecuencias

La demo puede ejecutarse conceptualmente sin BD de negocio, pero el arranque del generado con JPA/Flyway no está demostrado: AIR-011 deberá comprobarlo sin cambiar su base implícitamente. No se habilita/deshabilita autoconfiguración ni se toca el POM en AIR-010.

PostgreSQL sigue siendo tecnología futura condicionada a un caso durable. AIR-017 justificará esquema/índices/migraciones o ratificará aplazamiento. Retener datos reales exige permisos de AIR-019, también en memoria. Una versión demo retirada obliga a repetir búsqueda, no a usar datos del navegador como autoridad.

## Alternativas consideradas y pendientes

Cuentas/favoritos iniciales aportarían CRUD, pero exigen autorización por propietario, recuperación/borrado y almacenamiento sin mejorar el núcleo. Guardar todas las ofertas o snapshots simplificaría una referencia por ID, pero añade ciclo de vida, privacidad y persistencia evitables con demo determinista. Se descartan ambas opciones para este MVP.

[OVERVIEW.md](../architecture/OVERVIEW.md) contiene requisitos y UX, [MODULES.md](../architecture/MODULES.md) persistencia y [API_DESIGN.md](../api/API_DESIGN.md) los DTO ratificados en AIR-012. El histórico de aceptación inicial se conserva y las tareas de implementación requieren petición propia.
