package com.example.eventnode.db

import com.example.eventnode.domain.{EventNode, LocalizedText, RequestedTarget}
import com.example.eventnode.json.EventNodeJson._
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

  implicit val requestedTargetsColumnType: BaseColumnType[Seq[RequestedTarget]] = MappedColumnType.base[Seq[RequestedTarget], String](
    targets => targets.asJson.noSpaces,
    json =>
      parse(json)
        .flatMap(_.as[Seq[RequestedTarget]])
        .fold(error => throw new IllegalArgumentException(s"Invalid RequestedTarget JSON: ${error.message}"), identity)
  )

  final case class EventNodeRow(
    id: UUID,
    siteDisplayLabel: LocalizedText,
    start: Instant,
    end: Instant,
    expectedCapacity: BigDecimal,
    deleted: Boolean,
    organizationName: LocalizedText,
    requestedTargets: Seq[RequestedTarget],
    createdAt: Instant,
    updatedAt: Instant
  ) {
    def toDomain: EventNode = EventNode(
      id = id,
      siteDisplayLabel = siteDisplayLabel,
      start = start,
      end = end,
      expectedCapacity = expectedCapacity,
      deleted = deleted,
      organizationName = organizationName,
      requestedTargets = requestedTargets.sortBy(_.start),
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
      requestedTargets = domain.requestedTargets,
      createdAt = domain.createdAt,
      updatedAt = domain.updatedAt
    )
  }

  class EventNodesTable(tag: Tag) extends Table[EventNodeRow](tag, "event_nodes") {
    def id = column[UUID]("event_node_id", O.PrimaryKey)
    def siteDisplayLabel = column[LocalizedText]("site_display_label", O.SqlType("TEXT"))
    def start = column[Instant]("event_node_start_dttm_utc", O.SqlType("TIMESTAMPTZ"))
    def end = column[Instant]("event_node_end_dttm_utc", O.SqlType("TIMESTAMPTZ"))
    def expectedCapacity = column[BigDecimal]("expected_capacity_value", O.SqlType("NUMERIC(18,4)"))
    def deleted = column[Boolean]("deleted", O.SqlType("BOOLEAN"))
    def organizationName = column[LocalizedText]("organization_name", O.SqlType("TEXT"))
    def requestedTargets = column[Seq[RequestedTarget]]("requested_targets", O.SqlType("JSONB"))
    def createdAt = column[Instant]("created_dttm", O.SqlType("TIMESTAMPTZ"))
    def updatedAt = column[Instant]("last_updated_dttm", O.SqlType("TIMESTAMPTZ"))

    override def * = (id, siteDisplayLabel, start, end, expectedCapacity, deleted, organizationName, requestedTargets, createdAt, updatedAt).mapTo[EventNodeRow]
  }

  val eventNodes = TableQuery[EventNodesTable]
}
