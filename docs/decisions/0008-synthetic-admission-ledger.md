# ADR-0008 — Admisión sintética con ledger volátil y alcance cerrado

- Estado: **Aceptado e implementado en AIR-021 dentro del alcance sintético**;42tests nuevos y regresión280 satisfactorios, sin garantía durable.
- Fecha: 2026-11-19.
- Antecedentes: [ADR-0007](0007-real-provider-admission.md), IMP-008 y AIR-019.

## Contexto

El usuario autoriza promover IMP-008 e implementar un núcleo determinista con dobles, sin proveedores reales, dependencias nuevas o infraestructura. Las reservas durables exigidas para consumo real no existen. El buscador demo ya funciona y no necesita acoplarse al componente.

## Decisión

Añadir un paquete Java puro independiente, sin Spring bean, endpoint, transporte o credenciales. Separar estimación monetaria/cuota/intentos, admisión y estados de reserva. Solo se admite coste monetario conocido exactamente0€. El ledger concreto es memoria de un escenario sintético; la atomicidad debe residir en su instancia compartida, no en locks independientes de cada servicio.

Compartir esa misma instancia puede coordinar varios servicios de un proceso. Dos ledgers para una cuenta, aun en la misma JVM, no coordinan sus saldos. Uso real, recuperación tras reinicio o coordinación multiproceso quedan denegados; no basta advertir al llamante. Nueva instancia no demuestra que la deuda anterior haya desaparecido. Inicialización explícita de un escenario nuevo no es recuperación ni puede resetear uno usado.

Reservar máximos de un flujo y verificar cada autorización de paso antes de consumir el doble. Incertidumbre retiene compromisos y no provoca reenvío automático; coste/cuota/periodo/ledger desconocidos cierran admisión. Cambios de ventana no deben borrar deuda pendiente. Handles de otra instancia o proceso no acreditan reservas válidas.

## Garantías fuera de alcance

Memoria no garantiza recuperación ante crash, exactly-once entre ledger y transporte, ni exclusión distribuida. Reinicio y pérdida de memoria no constituyen cancelación. No se exportan snapshots como sustituto de almacenamiento transaccional; ninguna clave real o transporte de vuelo participa en esta tarea.

Una tarea posterior vinculada a AIR-017 deberá justificar durabilidad, identidad/idempotencia y coordinación de cuenta antes de integrar fuentes reales. Exigirá almacenamiento compartido autorizado, reconciliación de estados inciertos y pruebas de crash/múltiples procesos. No se crean tablas, entidades, migraciones, ficheros de persistencia operativa ni infraestructura ahora.

## Consecuencias

La implementación demuestra reglas de presupuesto y atomicidad dentro de una simulación limitada, sin afirmar protección real. Demo, POM, Wrapper y endpoints previos se preservan. Los permisos pendientes de Travelpayouts/SerpApi y la decisión de solo DEMO de ADR-0007 siguen vigentes. Compilación,42tests específicos y regresión280 satisfactorios, con revisiones Database/DevOps/Security & QA; [VALIDATION](../developer-guide/VALIDATION.md) conserva evidencia y límites. La carrera inicial de muestreo del reloj quedó corregida serializando lectura, operación y cierre sobre el ledger.
