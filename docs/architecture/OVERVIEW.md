# Visión arquitectónica inicial

**Estado: propuesta para validar en AIR-010.** El único código actual es el backend Initializr. No se han creado módulos, capas o adaptadores de negocio.

## Contexto y límites

Airwise comparará alternativas de viaje con datos de proveedores y estimaciones explícitas. Backend previsto: monolito modular Java/Spring; frontend previsto: React/TypeScript. La incorporación de PostgreSQL, infraestructura y proveedores reales debe responder a casos de uso aprobados, no a una lista de tecnologías.

Límites candidatos: exploración/búsqueda, comparación, catálogo de lugares/aeropuertos y adaptadores. Usuarios/preferencias/favoritos y alertas/histórico se revisarán después. No son paquetes existentes ni una lista cerrada de módulos obligatorios.

## Dependencias y modelo

El dominio no debería depender de DTOs, tokens o códigos de error de un proveedor. La aplicación coordinaría casos de uso y resultados parciales; infraestructura adaptaría fuentes; API expondría contratos coherentes. Se separarán estas responsabilidades donde exista comportamiento que lo justifique, evitando interfaces y directorios vacíos.

El modelo deberá diferenciar trayecto, segmento, itinerario de ida/vuelta, moneda y ámbito del importe. Conservará fuente, recepción, observación/caducidad cuando se conozcan y campos desconocidos. Una opción de vendedor no demuestra una reserva confirmada. Las estimaciones terrestres tendrán procedencia o supuestos visibles.

## Proveedores y fallos

Travelpayouts aportaría precios observados; SerpApi, resultados de búsqueda con detalle y posibles continuaciones; demo, datos sintéticos etiquetados. No todas las fuentes tienen capacidades equivalentes. El coordinador de búsqueda conservaría los resultados parciales y errores por fuente, sin convertir errores reales en vacío ni activar demo silenciosamente.

Permisos, retención y consumo deben comprobarse antes de integrar o publicar. Esta fase realiza cero llamadas reales. Véanse la [política](../api/PROVIDER_POLICY.md), la [evidencia previa](PROVIDER_EVIDENCE.md) y los [ADR](../decisions/README.md).

## Decisiones pendientes

Delimitar el MVP, contratos de comparación, necesidad de persistencia inicial, estrategia de seguridad, versiones resueltas, pruebas del backend generado y frontend. No hay API pública, base creada ni despliegue que describir todavía.
