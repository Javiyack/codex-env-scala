package com.example.eventnode.db

import com.example.eventnode.domain.{EventNode, LocalizedText, RequestedTarget}
import io.circe.parser.parse
import io.circe.syntax._
import slick.jdbc.PostgresProfile.api._

import java.sql.Timestamp
import java.time.Instant
import java.util.UUID

object Tables {

  implicit val instantColumnType: BaseColumnType[Instant] = MappedColumnType.base[Instant, Timestamp](
    instant => Timestamp.from(instant),
    timestamp => timestamp.toInstant
  )

  implicit val localizedTextColumnType: BaseColumnType[LocalizedText] = MappedColumnType.base[LocalizedText, String](
    localizedText => localizedText.values.asJson.noSpaces,
    json =>
      parse(json)
        .flatMap(_.as[Map[String, String]])
        .fold(error => throw new IllegalArgumentException(s"Invalid LocalizedText JSON: ${error.message}"), LocalizedText.apply)
  )

  final case class EventNodeRow(
    id: UUID,
    siteDisplayLabel: LocalizedText,
    start: Instant,
    end: Instant,
    expectedCapacity: BigDecimal,
    deleted: Boolean,
    organizationName: LocalizedText,
    createdAt: Instant,
    updatedAt: Instant
  ) {
    def toDomain(targets: Seq[RequestedTargetRow]): EventNode = EventNode(
      id = id,
      siteDisplayLabel = siteDisplayLabel,
      start = start,
      end = end,
      expectedCapacity = expectedCapacity,
      deleted = deleted,
      organizationName = organizationName,
      requestedTargets = targets.map(_.toDomain).sortBy(_.start),
      createdAt = createdAt,
      updatedAt = updatedAt
    )
  }

  object EventNodeRow {
    def fromDomain(domain: EventNode): EventNodeRow = EventNodeRow(
      id = domain.id,
      siteDisplayLabel = domain.siteDisplayLabel,
      start = domain.start,
      end = domain.end,
      expectedCapacity = domain.expectedCapacity,
      deleted = domain.deleted,
      organizationName = domain.organizationName,
      createdAt = domain.createdAt,
      updatedAt = domain.updatedAt
    )
  }

  final case class RequestedTargetRow(
    eventNodeId: UUID,
    start: Instant,
    end: Instant,
    targetValue: BigDecimal
  ) {
    def toDomain: RequestedTarget = RequestedTarget(start, end, targetValue)
  }

  object RequestedTargetRow {
    def fromDomain(eventNodeId: UUID, target: RequestedTarget): RequestedTargetRow = RequestedTargetRow(
      eventNodeId = eventNodeId,
      start = target.start,
      end = target.end,
      targetValue = target.targetValue
    )
  }

  class EventNodesTable(tag: Tag) extends Table[EventNodeRow](tag, "event_nodes") {
    def id = column[UUID]("event_node_id", O.PrimaryKey)
    def siteDisplayLabel = column[LocalizedText]("site_display_label")
    def start = column[Instant]("event_node_start_dttm_utc")
    def end = column[Instant]("event_node_end_dttm_utc")
    def expectedCapacity = column[BigDecimal]("expected_capacity_value")
    def deleted = column[Boolean]("deleted")
    def organizationName = column[LocalizedText]("organization_name")
    def createdAt = column[Instant]("created_dttm")
    def updatedAt = column[Instant]("last_updated_dttm")

    override def * = (id, siteDisplayLabel, start, end, expectedCapacity, deleted, organizationName, createdAt, updatedAt).mapTo[EventNodeRow]
  }

  class RequestedTargetsTable(tag: Tag) extends Table[RequestedTargetRow](tag, "requested_targets") {
    def eventNodeId = column[UUID]("event_node_id")
    def start = column[Instant]("start_dttm")
    def end = column[Instant]("end_dttm")
    def targetValue = column[BigDecimal]("target_value")

    def pk = primaryKey("requested_targets_pkey", (eventNodeId, start))
    def eventNodeFk = foreignKey("requested_targets_event_node_id_fkey", eventNodeId, eventNodes)(_.id, onDelete = ForeignKeyAction.Cascade)

    override def * = (eventNodeId, start, end, targetValue).mapTo[RequestedTargetRow]
  }

  val eventNodes = TableQuery[EventNodesTable]
  val requestedTargets = TableQuery[RequestedTargetsTable]
}
