# Modelado de la estructura Event Node

Este documento describe las decisiones de modelado adoptadas para la aplicación de ejemplo. El objetivo principal es
representar y persistir el payload JSON proporcionado utilizando **Scala 2.13**, **Slick** y **PostgreSQL** con serialización
**Circe**.

## Modelo de dominio en Scala

| Atributo JSON | Tipo Scala | Justificación |
|---------------|------------|---------------|
| `event_node_id` | `java.util.UUID` | El identificador es un UUID canónico; se utiliza el tipo estándar de Java para aprovechar la validación automática de formato. |
| `site_display_label` | `LocalizedText` (envoltorio de `Map[String, String]`) | El payload contiene un conjunto de etiquetas por locale. Se modela como mapa para permitir múltiples idiomas. |
| `event_node_start_dttm_utc`, `event_node_end_dttm_utc`, `created_dttm`, `last_updated_dttm` | `java.time.Instant` | Las marcas de tiempo están en UTC con formato ISO-8601. `Instant` ofrece precisión de nanosegundos y semántica temporal explícita. |
| `expected_capacity_value` | `BigDecimal` | Se necesita representar valores decimales de alta precisión (no flotantes). `BigDecimal` evita errores de redondeo. |
| `deleted` | `Boolean` | Se conserva el tipo booleano nativo. |
| `organization_name` | `LocalizedText` | El JSON incluye un campo `value` con un documento JSON serializado. Se aprovecha el mismo tipo `LocalizedText` para representar los valores deserializados. |
| `requested_targets` | `Seq[RequestedTarget]` donde `RequestedTarget` contiene `Instant` y `BigDecimal` | Cada target representa un intervalo de tiempo con valor cuantitativo. Usamos `Seq` para mantener el orden original y `BigDecimal` para permitir valores enteros o fraccionarios. |

## Persistencia con Slick y PostgreSQL

Se definieron dos tablas normalizadas:

- `event_nodes` almacena la cabecera del evento.
- `requested_targets` modela la colección homónima como filas hijas (relación 1-N). Esto evita arreglos JSON y permite
  consultas y restricciones SQL nativas sobre cada objetivo temporal.

### Tipos de columna

| Columna | Tipo PostgreSQL | Motivo |
|---------|-----------------|--------|
| `event_node_id` | `UUID` | coincide con la semántica del identificador. |
| `site_display_label`, `organization_name` | `TEXT` | Contienen representaciones JSON compactas. Almacenar el JSON como texto evita dependencias adicionales (`slick-pg`). En el acceso se usan codecs Circe para mapear `LocalizedText` ⇔ `String`. |
| Timestamps | `TIMESTAMPTZ` | Mantiene la zona horaria UTC sin pérdida de información. |
| `expected_capacity_value`, `target_value` | `NUMERIC(18,4)` | Proporciona precisión para cifras grandes con hasta cuatro decimales. La elección equilibra precisión financiera/energética y compatibilidad con `BigDecimal`. |
| `deleted` | `BOOLEAN` | Representación directa del flag lógico. |

### Estrategias de codificación/decodificación

1. **Slick ⇔ PostgreSQL**
   - Se declararon `MappedColumnType` personalizados para `Instant` y `LocalizedText`.
   - `Instant` se convierte a `java.sql.Timestamp` para permitir que Slick lo envíe como `TIMESTAMPTZ`.
   - `LocalizedText` se serializa como `String` mediante `asJson.noSpaces`; durante la lectura se parsea con Circe. Cualquier
     error de parseo produce una excepción descriptiva, evitando datos corruptos.
   - `requested_targets` se proyecta en una tabla secundaria. El repositorio combina las filas hijas agrupándolas por
     `event_node_id` al reconstruir el agregado `EventNode`.

2. **Circe ⇔ JSON de API**
   - Se implementaron encoders/decoders implícitos (`EventNodeJson`) coherentes con la estructura de llaves solicitada.
   - `organization_name` requiere un tratamiento especial: el campo `value` contiene JSON serializado como `String`. Se
     parsea con Circe (`parseLocalizedText`) durante la lectura y, al serializar, se vuelve a producir la cadena JSON
     embebida.
   - Las fechas se manejan como `Instant` en ISO-8601. Cualquier error de `parse` devuelve un `DecodingFailure` con el
     mensaje específico.

## Scripts de migración

El archivo [`src/main/resources/db/migration/V1__create_event_node_tables.sql`](../src/main/resources/db/migration/V1__create_event_node_tables.sql)
crea ambas tablas y los índices necesarios para consultas por `event_node_id`. Puede utilizarse con herramientas como
Flyway o ejecutarse manualmente.

## Flujo de datos

1. **Entrada JSON** → `EventNode` (Circe) → operaciones de dominio.
2. **Persistencia**: el servicio llama al `EventNodeRepository`, que inserta/actualiza `event_nodes` y `requested_targets`
   en una transacción (`DBIO.transactionally`).
3. **Lectura**: el repositorio obtiene las filas de ambas tablas, agrupa `requested_targets` y crea un `EventNode` de
   dominio, listo para serializarse nuevamente con Circe cuando se necesite exponerlo.

Esta arquitectura facilita validar y transformar los datos en Scala, aprovechar restricciones relacionales en PostgreSQL
y mantener la compatibilidad exacta con el payload JSON original.
