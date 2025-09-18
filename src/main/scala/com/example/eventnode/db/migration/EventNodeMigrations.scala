package com.example.eventnode.db.migration

import com.example.eventnode.db.Tables
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.api._
import slick.migration.api.{Dialect, Migration, MigrationSeq, PostgresDialect, TableMigration}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Define y ejecuta las migraciones de base de datos empleando slick-migration-api
  * y el dialecto de PostgreSQL. Permite generar las sentencias SQL de creación
  * de esquema directamente desde las definiciones de tabla de Slick para mantener
  * consistencia entre el dominio y la base de datos.
  */
object EventNodeMigrations {

  implicit val postgresDialect: Dialect[PostgresProfile] = PostgresDialect

  val createEventNodesTable: Migration =
    TableMigration(Tables.eventNodes.baseTableRow)
      .create
      .addColumns(
        _.id,
        _.siteDisplayLabel,
        _.start,
        _.end,
        _.expectedCapacity,
        _.deleted,
        _.organizationName,
        _.requestedTargets,
        _.createdAt,
        _.updatedAt
      )

  val all: MigrationSeq = MigrationSeq(createEventNodesTable)

  def schemaStatements: Seq[String] = all.statements.toSeq

  def schema: DBIO[Unit] = DBIO.sequence(schemaStatements.map(stmt => sqlu"#$stmt")).map(_ => ())

  def migrate(db: Database)(implicit ec: ExecutionContext): Future[Unit] =
    db.run(schema)
}
