# Evidencia previa de proveedores

Fuente de esta sección: resultados verificados **aportados por el usuario** al comenzar la fase 1. No son pruebas ejecutadas en este checkout durante esta entrega. Las PoC completas y sus archivos de evidencia no están en la estructura inicial inspeccionada; no se copiaron ni recrearon.

| Proveedor | Evidencia aportada | Límites conocidos de esa evidencia |
|---|---|---|
| Travelpayouts Data API | 16 consultas HTTP 200, 25 registros observados; destinos económicos y tarifas de caché | Cobertura irregular por ruta/fecha; disponibilidad actual no garantizada; equipaje insuficiente |
| SerpApi Google Flights | 6 peticiones HTTP 200, una de cuenta y cinco de vuelos; 61 entradas con fases y solapamientos; OPO/SCQ/VGO, segmentos, regreso y vendedor comprobados | Equipaje parcial; plan observado de 250 búsquedas/mes; un flujo completo puede requerir varias búsquedas |

No se presentan las 61 entradas como 61 viajes únicos, ni una opción de vendedor como compra. Tampoco se deduce la cuota actual de la cuenta a partir de estos datos históricos.

Presupuesto de API: 0 €. Publicación, conservación y reutilización siguen pendientes de comprobar para ambos. Solo se usará esta evidencia para orientar el diseño de adaptadores y capacidades; no autoriza nuevas peticiones.
