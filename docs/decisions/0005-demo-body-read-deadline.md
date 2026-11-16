# ADR-0005 — Plazo absoluto de lectura del cuerpo en demo

- Estado: Aceptado en AIR-014 tras pruebas reales y revisión estática independiente de Backend.
- Fecha: 16/11/2026.
- Alcance: HTTP/1.1 NIO local del perfil demo; sin POM, dependencias, proxy o servicios nuevos.

## Contexto

AIR-016 exige leer el cuerpo en un máximo de cinco segundos. Un timeout de socket de cinco segundos limita inactividad, pero permite un goteo continuo más largo. La primera implementación con comprobación posterior a readNBytes produjo400 después de6,06s en un diagnóstico real. Un timeout sobre un Future no acredita cancelación del lector ni evita acceso tardío a una petición servlet reciclada.

## Decisión

Usar puntos de extensión de Tomcat11: Http11NioProtocol, NioEndpoint.createChannel y NioChannel.read. Solo dentro del scope síncrono de lectura, el canal espera en un Selector local hasta el tiempo monotónico restante. Esto incluye el framing chunked y sus trailers, donde no basta comprobar el plazo entre llamadas a ServletInputStream.read. Los demás reads conservan la implementación original de Tomcat.

BodyReadDeadline se instala en ThreadLocal y se retira en finally, sin ejecutores ni timers. El Selector se cierra en el mismo hilo. BodyReadValve deshabilita el swallowing de entrada incompleta y cierra la conexión, para no drenar un cuerpo hostil después de liberar el permiso de aplicación. El rechazo conserva400 JSON sanitizado mientras la respuesta no esté comprometida.

Se configura únicamente bajo demo y se rechazan TLS/HTTP2 no validados. No se abre ASYNC, FORWARD o INCLUDE ni se cambia el modo sin demo. Un proxy externo o una solución asíncrona completa quedan fuera de esta tarea y no se introducen como requisito oculto.

## Consecuencias y validación

La implementación queda acoplada a Tomcat11 y sus APIs; debe revalidarse al actualizar el servidor, habilitar TLS/HTTP2 o publicar. No es un reemplazo general del transporte ni una garantía de scheduling en tiempo real del sistema operativo.

Verificado sobre servidor real: goteo continuo, bloqueo próximo al plazo, cabeceras chunk/trailers incompletos, desconexión, bytes bufferizados, segunda petición y permisos liberados. Suite final: 181 pruebas satisfactorias. Backend contrastó el código y bytecode Tomcat11.0.27 local; el coordinador verificó además Java independiente/curl. Resultados en [VALIDATION](../developer-guide/VALIDATION.md). AIR-014 consta Hecha, sin extender aceptación a otros transportes. Referencias oficiales: [NioEndpoint.createChannel](https://tomcat.apache.org/tomcat-11.0-doc/api/org/apache/tomcat/util/net/NioEndpoint.html) y [NioChannel.read/getIOChannel/reset](https://tomcat.apache.org/tomcat-11.0-doc/api/org/apache/tomcat/util/net/NioChannel.html).
## Identidad de clases en el arranque

DevTools puede separar el estado estático entre el loader del protocolo reflectivo de Tomcat y el de los filtros. Un proceso independiente detectó ese fallo después de180 tests verdes; no se aceptó la evidencia de tests como suficiente. El modo demo exige protocolo/filtro/deadline con identidad de loader compartida y rechaza el arranque antes de bind si no coinciden. Se documenta y verifica la opción JVM temprana `-Dspring.devtools.restart.enabled=false`, conservando dependencia, main y POM. La propiedad en application-demo por sí sola no impide el primer RestartClassLoader. Referencia oficial: [desactivar restart de Spring Boot](https://docs.spring.io/spring-boot/reference/using/devtools.html).
