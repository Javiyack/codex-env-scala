package com.example.eventnode.domain

import java.time.Instant

/** Represents a requested target interval for a given event node. */
final case class RequestedTarget(
  start: Instant,
  end: Instant,
  targetValue: BigDecimal
)
