package com.example.eventnode.db

import com.example.eventnode.domain.EventNode
import slick.jdbc.PostgresProfile.api._

import java.util.UUID

class EventNodeRepository {
  import Tables._

  def insert(eventNode: EventNode): DBIO[Unit] = {
    val row = EventNodeRow.fromDomain(eventNode)
    val targetRows = eventNode.requestedTargets.map(RequestedTargetRow.fromDomain(eventNode.id, _))

    val action = for {
      _ <- eventNodes += row
      _ <- requestedTargets ++= targetRows
    } yield ()

    action.transactionally
  }

  def upsert(eventNode: EventNode): DBIO[Unit] = {
    val deleteExistingTargets = requestedTargets.filter(_.eventNodeId === eventNode.id).delete
    val insertTargets = requestedTargets ++= eventNode.requestedTargets.map(RequestedTargetRow.fromDomain(eventNode.id, _))

    val action = for {
      _ <- eventNodes.insertOrUpdate(EventNodeRow.fromDomain(eventNode))
      _ <- deleteExistingTargets
      _ <- insertTargets
    } yield ()

    action.transactionally
  }

  def findById(id: UUID): DBIO[Option[EventNode]] = {
    val fetchEventNode = eventNodes.filter(_.id === id).result.headOption
    val fetchTargets = requestedTargets.filter(_.eventNodeId === id).result

    for {
      maybeRow <- fetchEventNode
      targets <- fetchTargets
    } yield maybeRow.map(_.toDomain(targets))
  }

  def listAll: DBIO[Seq[EventNode]] = {
    eventNodes.result.flatMap { rows =>
      val ids = rows.map(_.id)
      if (ids.isEmpty) DBIO.successful(Seq.empty)
      else {
        requestedTargets.filter(_.eventNodeId.inSet(ids)).result.map { targetRows =>
          val grouped = targetRows.groupBy(_.eventNodeId)
          rows.map(row => row.toDomain(grouped.getOrElse(row.id, Seq.empty)))
        }
      }
    }
  }
}
