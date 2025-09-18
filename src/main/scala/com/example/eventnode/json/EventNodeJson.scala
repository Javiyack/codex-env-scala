package com.example.eventnode.json

import com.example.eventnode.domain.{EventNode, LocalizedText, RequestedTarget}
import io.circe.{Decoder, DecodingFailure, Encoder, HCursor, Json}
import io.circe.parser.parse
import io.circe.syntax._

import java.time.Instant
import java.time.format.DateTimeParseException
import java.util.UUID
import scala.util.{Failure, Success, Try}

/** Provides Circe encoders and decoders for the domain model. */
object EventNodeJson {

  private def parseLocalizedText(jsonString: String, fieldName: String): Either[DecodingFailure, LocalizedText] = {
    parse(jsonString)
      .flatMap(_.as[Map[String, String]])
      .left.map(err => DecodingFailure(s"Invalid JSON in '$fieldName': ${err.message}", List.empty))
      .map(LocalizedText.apply)
  }

  implicit val instantEncoder: Encoder[Instant] = Encoder.encodeString.contramap(_.toString)
  implicit val instantDecoder: Decoder[Instant] = Decoder.decodeString.emap { str =>
    Try(Instant.parse(str)) match {
      case Success(value) => Right(value)
      case Failure(error: DateTimeParseException) => Left(error.getMessage)
      case Failure(other) => Left(other.getMessage)
    }
  }

  implicit val localizedTextEncoder: Encoder[LocalizedText] = Encoder.instance { localizedText =>
    Json.obj(localizedText.values.toSeq.map { case (key, value) =>
      (key, Json.fromString(value))
    }: _*)
  }

  implicit val localizedTextDecoder: Decoder[LocalizedText] = Decoder.instance { cursor: HCursor =>
    cursor.as[Map[String, String]].map(LocalizedText.apply)
  }

  val organizationNameEncoder: Encoder[LocalizedText] = Encoder.instance { localizedText =>
    Json.obj(
      "value" -> Json.fromString(localizedText.values.asJson.noSpaces)
    )
  }

  val organizationNameDecoder: Decoder[LocalizedText] = Decoder.instance { cursor: HCursor =>
    cursor.get[String]("value").flatMap { rawJson =>
      parseLocalizedText(rawJson, "organization_name.value")
    }
  }

  implicit val requestedTargetEncoder: Encoder[RequestedTarget] = Encoder.instance { target =>
    Json.obj(
      "start_dttm" -> target.start.asJson,
      "end_dttm" -> target.end.asJson,
      "target_value" -> Json.fromBigDecimal(target.targetValue)
    )
  }

  implicit val requestedTargetDecoder: Decoder[RequestedTarget] = Decoder.instance { cursor =>
    for {
      start <- cursor.get[Instant]("start_dttm")
      end <- cursor.get[Instant]("end_dttm")
      value <- cursor.get[BigDecimal]("target_value")
    } yield RequestedTarget(start, end, value)
  }

  implicit val eventNodeEncoder: Encoder[EventNode] = Encoder.instance { node =>
    Json.obj(
      "event_node_id" -> Json.fromString(node.id.toString),
      "site_display_label" -> localizedTextEncoder(node.siteDisplayLabel),
      "event_node_start_dttm_utc" -> node.start.asJson,
      "event_node_end_dttm_utc" -> node.end.asJson,
      "expected_capacity_value" -> Json.fromBigDecimal(node.expectedCapacity),
      "deleted" -> Json.fromBoolean(node.deleted),
      "organization_name" -> organizationNameEncoder(node.organizationName),
      "requested_targets" -> Json.fromValues(node.requestedTargets.map(_.asJson)),
      "created_dttm" -> node.createdAt.asJson,
      "last_updated_dttm" -> node.updatedAt.asJson
    )
  }

  implicit val eventNodeDecoder: Decoder[EventNode] = Decoder.instance { cursor =>
    for {
      id <- cursor.get[UUID]("event_node_id")
      siteDisplayLabel <- cursor.downField("site_display_label").as[LocalizedText]
      start <- cursor.get[Instant]("event_node_start_dttm_utc")
      end <- cursor.get[Instant]("event_node_end_dttm_utc")
      expectedCapacity <- cursor.get[BigDecimal]("expected_capacity_value")
      deleted <- cursor.get[Boolean]("deleted")
      organizationName <- cursor.downField("organization_name").as[LocalizedText](organizationNameDecoder)
      requestedTargets <- cursor.get[Seq[RequestedTarget]]("requested_targets")
      created <- cursor.get[Instant]("created_dttm")
      updated <- cursor.get[Instant]("last_updated_dttm")
    } yield EventNode(id, siteDisplayLabel, start, end, expectedCapacity, deleted, organizationName, requestedTargets, created, updated)
  }
}
