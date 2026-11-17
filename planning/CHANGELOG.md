# Changelog

## Unreleased

### 2026-11-17 — motor de comparación demo AIR-015

- Implementado POST /api/v1/demo/comparisons: selección servidor de2–4 ofertas sin depender de filtros/páginas, cuatro primarias, grupos compatibles, rankings de competición y recomendaciones limitadas a EVALUABLE_GROUP.
- Estimaciones terrestres explícitas, subtotales de componentes seleccionados, null/cero diferenciados y vigencia/desempates explicados; sin FX, puntuaciones opacas o ganador global entre grupos. Parcialidad operativa conserva causas mediante warning seguro sin ampliar schema.
- Security habilita solo la nueva combinación POST exacta y preflight, conservando CSRF específico, CORS, cuotas, JSON, errores y plazo de lectura. Moneda admitida pero incompatible produce INVALID_COMBINATION conforme a CT-26.
- Wrapper offline clean test:238 pruebas, cero fallos/errores/omitidas;24 comprobaciones con Java independiente/curl y ejemplos capturados. Sin dependencias nuevas, cambios del POM/Wrapper/base protegida o corpus, persistencia, APIs externas, commits o push. No se publica versión ni se inicia AIR-018.

### 2026-11-16 — buscador REST demo AIR-014 completado

- Perfil demo explícito, guardas antes de conexiones, catálogo GET y búsqueda POST sobre DEMO; corregida O-01. DTO HTTP separados, validación estricta, filtros, orden, páginas y causas parciales conservadas.
- Security restrictiva para dos rutas, sin sesión/login, CORS por allowlist, CSRF específico, Actuator cerrado, errores/cabeceras uniformes y admisión acotada. Comparación sigue denegada y AIR-015 sin iniciar.
- Wrapper offline `clean test`: 181 pruebas satisfactorias; HTTP real, chunked, bytes/concurrencia/ERROR GET y POST, proceso Java independiente y curl comprobados. POM/Wrapper/base generada intactos en esta tarea; sin APIs ni nuevas dependencias.
- Corregido el plazo total de lectura lenta mediante NIO demo; goteo, framing incompleto y desconexión probados. HTTP/1.1 local únicamente, TLS/HTTP2 pendientes; ADR-0005 registra decisión y consecuencias. `package` offline no generó JAR por plugin ausente en caché; no se descargó ni alteró configuración para eludirlo.

### 2026-11-13 — diseño de seguridad y pruebas AIR-016

- Definidos permisos exactos, denegación por defecto, gate anterior a CSRF, CORS, errores/cabeceras, cierre Actuator y cotas operativas. Perfil demo y guardas de persistencia diseñados con APIs reales Boot 4.1.2/Security 7/Jackson 3.
- Revisiones reales de security_qa y backend; documentada O-01 de capacidades como desviación funcional interna de prioridad baja. Matriz de 49 casos distingue pruebas históricas de controles HTTP/runtime pendientes.
- Solo documentación y comprobaciones estáticas, sin modificar backend/configuración ni repetir los tests de AIR-013. AIR-014/015 no iniciadas.

### 2026-11-13 — proveedor demo AIR-013

- Implementados modelos inmutables, puerto independiente y proveedor demo sin red, con identidad SHA-256 y corpus sintético versionado: 11 escenarios/16 registros, incluidas escalas, empates, monedas, desconocidos, caducidad y estados vacíos/fallidos/parciales.
- Añadidos 61 casos nuevos automatizados; el total con el contexto conservado es 62, todos satisfactorios. Compilación y tests mediante Wrapper offline; validador documental satisfactorio. Revisión real de backend/security_qa integrada.
- Preservados todos los archivos backend anteriores, POM, Wrapper, configuración y script validador. HTTP, frontend, comparación, wiring demo y persistencia siguen pendientes.

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
