package com.example.eventnode

import com.example.eventnode.domain.EventNode
import com.example.eventnode.json.EventNodeJson._
import io.circe.parser.decode
import io.circe.syntax._

/** Simple entry point that demonstrates JSON decoding/encoding. */
object App extends scala.App {
  private val sampleJson =
    """{
       |  \"event_node_id\": \"12152850-94c5-11f0-bb2b-53f722bbf2d6\",
       |  \"site_display_label\": {
       |    \"en_US\": \"EPS QAA Event Nodes Site 1\"
       |  },
       |  \"event_node_start_dttm_utc\": \"2025-09-18T19:10:00.000Z\",
       |  \"event_node_end_dttm_utc\": \"2025-09-18T19:20:00.000Z\",
       |  \"expected_capacity_value\": 2000.7,
       |  \"deleted\": false,
       |  \"organization_name\": {
       |    \"value\": \"{\\\"en_US\\\":\\\"EPS QAA Event Nodes Org 1 jllach\\\"}\"
       |  },
       |  \"requested_targets\": [
       |    {
       |      \"end_dttm\": \"2025-09-18T19:15:00.000Z\",
       |      \"start_dttm\": \"2025-09-18T19:10:00.000Z\",
       |      \"target_value\": 7000
       |    },
       |    {
       |      \"end_dttm\": \"2025-09-18T19:20:00.000Z\",
       |      \"start_dttm\": \"2025-09-18T19:15:00.000Z\",
       |      \"target_value\": 7500
       |    }
       |  ],
       |  \"created_dttm\": \"2025-09-18T19:24:18.132Z\",
       |  \"last_updated_dttm\": \"2025-09-18T19:24:18.133Z\"
       |}""".stripMargin

  decode[EventNode](sampleJson) match {
    case Left(error) =>
      println(s"Decoding failed: ${error.getMessage}")
    case Right(eventNode) =>
      println("Decoded EventNode domain model:")
      println(eventNode)
      println()
      println("Re-encoded JSON:")
      println(eventNode.asJson.spaces2)
  }
}
