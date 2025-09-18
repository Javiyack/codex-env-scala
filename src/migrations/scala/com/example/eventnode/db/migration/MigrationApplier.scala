package com.example.eventnode.db.migration

import slick.jdbc.PostgresProfile.api._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

/**
  * Entry point for applying database migrations via sbt.
  *
  * Usage (example):
  * {{
  *   sbt migrations/runMain com.example.eventnode.db.migration.MigrationApplier
  * }}
  *
  * Optionally, the database configuration path can be provided using the
  * `eventnode.migrations.dbConfig` system property. By default it looks for the
  * `event-node.db` key in the Typesafe configuration.
  */
object MigrationApplier extends App {

  private val configPath =
    sys.props.getOrElse("eventnode.migrations.dbConfig", "event-node.db")

  implicit val ec: ExecutionContext = ExecutionContext.global

  val db = Database.forConfig(configPath)

  try {
    Await.result(EventNodeMigrations.migrate(db), Duration.Inf)
    println(s"Successfully applied migrations using config '$configPath'")
  } finally db.close()
}
