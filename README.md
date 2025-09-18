# Event Node Modeling (Scala + Slick + Circe)

Este proyecto implementa un modelo de dominio y capa de persistencia para manejar el payload JSON de un *event node*.
Incluye:

- Modelos de dominio en Scala 2.13.
- Encoders/decoders implícitos de Circe que mantienen la forma exacta del JSON original.
- Tablas y repositorios Slick para PostgreSQL.
- Migraciones programáticas usando la API `.schema` de Slick junto con el `PostgresDialect` (`EventNodeMigrations`), ejecutables a través de un `runMain` dedicado.
- Documentación detallada en [`docs/modeling.md`](docs/modeling.md) sobre las elecciones de tipos y la estrategia de
  serialización.

## Ejecutar el ejemplo JSON

```bash
sbt "runMain com.example.eventnode.App"
```

El `Main` decodifica el JSON proporcionado, reconstruye el modelo `EventNode` y lo vuelve a serializar mostrando que la
estructura se mantiene consistente.

## Migraciones de base de datos

Las migraciones se ejecutan fuera de la capa de servicio mediante un comando sbt dedicado:

```bash
sbt "migrations/runMain com.example.eventnode.db.migration.MigrationApplier"
```

El `MigrationApplier` utiliza por defecto la configuración `event-node.db` (Typesafe Config). Se puede sobreescribir
con el parámetro `-Deventnode.migrations.dbConfig=<path>` para apuntar a otro bloque de configuración.

## Conexión a base de datos

La clase [`EventNodeService`](src/main/scala/com/example/eventnode/service/EventNodeService.scala) expone operaciones
asincrónicas para crear, actualizar y consultar registros; asume que el esquema ya fue creado por las migraciones. Puede
inicializarse con un `Database` configurado mediante HikariCP o `Database.forConfig`.
