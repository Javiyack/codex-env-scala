package com.example.eventnode.db

import com.example.eventnode.domain.EventNode
import slick.jdbc.PostgresProfile.api._

import java.util.UUID

class EventNodeRepository {
  import Tables._

  def insert(eventNode: EventNode): DBIO[Unit] = {
    val row = EventNodeRow.fromDomain(eventNode)
    (eventNodes += row).map(_ => ())
  }

  def upsert(eventNode: EventNode): DBIO[Unit] = {
    eventNodes.insertOrUpdate(EventNodeRow.fromDomain(eventNode)).map(_ => ())
  }

  def findById(id: UUID): DBIO[Option[EventNode]] = {
    eventNodes.filter(_.id === id).result.headOption.map(_.map(_.toDomain))
  }

  def listAll: DBIO[Seq[EventNode]] = {
    eventNodes.result.map(_.map(_.toDomain))
  }
}
