# Changelog

## Unreleased

### 2026-11-12 — contratos AIR-012

- Ratificados contratos airwise-v1 de búsqueda, vuelos/precios, proveedores y tres operaciones HTTP, con 18 ejemplos sintéticos y 37 casos de contrato previstos. Separados itinerario completo, observación parcial y desconocidos; definidos identidad estable, estados/errores, cotas, grupos y desempates explicables.
- Revisiones reales de backend y security_qa conformes tras integrar precisiones. Validador y comprobación documental de ejemplos satisfactorios; backend y script validador preservados. Paquetes e incremento vertical propuestos; opciones de arranque demo documentadas sin configurar ni implementar componentes.

### 2026-11-12 — diagnóstico AIR-011

- Tras autorización expresa, ajustado únicamente el test de contexto para el demo sin persistencia y corregido el alcance del validador. Maven Wrapper test y validador completados con código 0; cinco regresiones aisladas satisfactorias. AIR-011 Hecha para ese alcance. Se conserva el diagnóstico fallido inicial y el snapshot histórico de los diez archivos.

- En el diagnóstico inicial se verificaron Temurin 21.0.12, Wrapper Maven 3.10.0, Boot 4.1.2 y árbol resuelto con Jupiter 6.0.3. Compilación correcta y test de contexto reproducido con error DataSource. En esa etapa AIR-011 quedó Bloqueada hasta autorizar su corrección.
- Registrado el límite del validador histórico ante salidas de target y contrastada por separado la integridad de los archivos originales.

### 2026-11-11 — organización y diseño

- AIR-010: acordado MVP demo ida/ida-vuelta y cuatro módulos, dominio, proveedores, comparación, HTTP conceptual, UX, seguridad y persistencia aplazada; ADR-0002/0003 aceptados con historial y ADR-0004 añadido. Análisis reales de backend, database, frontend y security_qa; sin implementación.
- Generalizadas las restricciones de AGENTS por autorización de tarea; backend y validador preservados, con propuesta documentada de evolución del chequeo histórico.

- Inventariado el backend Initializr existente sin cambiar sus archivos.
- Añadidas instrucciones raíz, configuración local de seis especialistas y cinco skills de procedimiento.
- Preparados roadmap, backlog con Definition of Done, mejoras propuestas y documentación inicial con ADR.
- Añadidas plantillas GitHub y proceso de contribución/seguridad, sin pipelines.
- Preparada validación local de metadatos, enlaces, planificación y preservación; resultados en [VALIDATION.md](../docs/developer-guide/VALIDATION.md).
- Aplicadas diez exclusiones preventivas para temporales, nombres de credenciales e informes privados antes del primer commit; actualizado el informe con AIR-006 y la revisión real de `security_qa`, conservando la evidencia histórica y el backend original.

No hay versión publicada. `backend:0.0.1-SNAPSHOT` es la coordenada generada, no una release de Airwise. Existe el primer commit local `439826c`; AIR-010 no hace commits, push, despliegues ni llamadas a las APIs de vuelos.
