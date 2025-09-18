package com.example.eventnode.domain

import java.time.Instant
import java.util.UUID

/** Domain representation for an event node. */
final case class EventNode(
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
)
