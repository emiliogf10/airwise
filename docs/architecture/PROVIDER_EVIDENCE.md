# Evidencia previa de proveedores

Fuente de esta sección: resultados verificados **aportados por el usuario** al comenzar la fase 1. No son pruebas ejecutadas en este checkout durante esta entrega. Las PoC completas y sus archivos de evidencia no están en la estructura inicial inspeccionada; no se copiaron ni recrearon.

| Proveedor | Evidencia aportada | Límites conocidos de esa evidencia |
|---|---|---|
| Travelpayouts Data API | 16 consultas HTTP 200, 25 registros observados; destinos económicos y tarifas de caché | Cobertura irregular por ruta/fecha; disponibilidad actual no garantizada; equipaje insuficiente |
| SerpApi Google Flights | 6 peticiones HTTP 200, una de cuenta y cinco de vuelos; 61 entradas con fases y solapamientos; OPO/SCQ/VGO, segmentos, regreso y vendedor comprobados | Equipaje parcial; plan observado de 250 búsquedas/mes; un flujo completo puede requerir varias búsquedas |

No se presentan las 61 entradas como 61 viajes únicos, ni una opción de vendedor como compra. Tampoco se deduce la cuota actual de la cuenta a partir de estos datos históricos.

Presupuesto de API: 0 €. Esta evidencia orienta el diseño de adaptadores y capacidades; no autoriza nuevas peticiones ni acredita permisos vigentes.

## Revisión documental posterior

AIR-019 revisa fuentes públicas oficiales el 19/11/2026, sin repetir las PoC ni acceder a cuentas. La [evaluación de permisos y presupuesto](../api/PROVIDER_ASSESSMENT.md) registra condiciones documentadas, preguntas de publicación/retención/reutilización todavía abiertas y un plan de consumo de 0 €. No modifica los resultados históricos de esta tabla ni habilita fuentes reales; [ADR-0007](../decisions/0007-real-provider-admission.md) mantiene solo DEMO operativo.

La ampliación del adjunto detallado, también el19/11/2026, agrega matriz técnica de11capacidades y clasificación **PENDIENTE DE ACLARACIÓN** de ambos para portfolio público multifuente. Backend inspeccionó el puerto y las restricciones demo en solo lectura; Documentation revalidó fuentes públicas; Security & QA revisó política/retención/cuotas. No hay nuevas respuestas de APIs, saldo de cuenta o condiciones privadas verificadas. Los escenarios de1/3orígenes y fases son estimaciones conservadoras, no peticiones ejecutadas.
