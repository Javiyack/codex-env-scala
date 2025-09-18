# Event Node Modeling (Scala + Slick + Circe)

Este proyecto implementa un modelo de dominio y capa de persistencia para manejar el payload JSON de un *event node*.
Incluye:

- Modelos de dominio en Scala 2.13.
- Encoders/decoders implícitos de Circe que mantienen la forma exacta del JSON original.
- Tablas y repositorios Slick para PostgreSQL.
- Script SQL de migración (`V1__create_event_node_tables.sql`).
- Documentación detallada en [`docs/modeling.md`](docs/modeling.md) sobre las elecciones de tipos y la estrategia de
  serialización.

## Ejecutar el ejemplo JSON

```bash
sbt "runMain com.example.eventnode.App"
```

El `Main` decodifica el JSON proporcionado, reconstruye el modelo `EventNode` y lo vuelve a serializar mostrando que la
estructura se mantiene consistente.

## Conexión a base de datos

La clase [`EventNodeService`](src/main/scala/com/example/eventnode/service/EventNodeService.scala) expone operaciones
asincrónicas para crear, actualizar y consultar registros. Puede inicializarse con un `Database` configurado mediante
HikariCP o `Database.forConfig`.
