package com.example.eventnode.domain

/** Represents a set of localized strings keyed by locale identifier (e.g. "en_US"). */
final case class LocalizedText(values: Map[String, String]) {
  def get(locale: String): Option[String] = values.get(locale)
}

object LocalizedText {
  val empty: LocalizedText = LocalizedText(Map.empty)
}
