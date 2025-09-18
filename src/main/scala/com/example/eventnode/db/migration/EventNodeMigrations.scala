package com.example.eventnode.db.migration

import com.example.eventnode.db.Tables
import slick.jdbc.PostgresProfile.api._
import slick.migration.api.PostgresDialect

import scala.annotation.unused
import scala.concurrent.{ExecutionContext, Future}

/**
  * Define y ejecuta las migraciones de base de datos utilizando la función `.schema`
  * de Slick para mantener la definición de la tabla sincronizada con el modelo.
  */
object EventNodeMigrations {

  @unused implicit val postgresDialect: PostgresDialect = new PostgresDialect

  private val eventNodesSchema = Tables.eventNodes.schema

  private val schemaMigrations: List[SchemaDescription] = List(eventNodesSchema)

  def schemaStatements: Seq[String] = eventNodesSchema.createStatements.toSeq

  def migrate(db: Database)(implicit ec: ExecutionContext): Future[Unit] =
    schemaMigrations.foldLeft(Future.successful(())) { (acc, schema) =>
      acc.flatMap(_ => db.run(schema.createIfNotExists))
    }
}
