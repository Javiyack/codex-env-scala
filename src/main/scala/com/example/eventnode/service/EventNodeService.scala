package com.example.eventnode.service

import com.example.eventnode.db.EventNodeRepository
import com.example.eventnode.db.migration.EventNodeMigrations
import com.example.eventnode.domain.EventNode
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}
import java.util.UUID

/** High level API to interact with event nodes stored in the database. */
class EventNodeService(db: Database, repository: EventNodeRepository)(implicit ec: ExecutionContext) {

  def migrateSchema(): Future[Unit] = EventNodeMigrations.migrate(db)

  def create(eventNode: EventNode): Future[Unit] =
    db.run(repository.insert(eventNode))

  def upsert(eventNode: EventNode): Future[Unit] =
    db.run(repository.upsert(eventNode))

  def findById(id: UUID): Future[Option[EventNode]] =
    db.run(repository.findById(id))

  def listAll(): Future[Seq[EventNode]] =
    db.run(repository.listAll)
}
