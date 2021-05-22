package ch.epfl.pop.model.network.method.message.data

import ch.epfl.pop.model.network.Matching

object ActionType extends Enumeration {
  type ActionType = Value

  // uninitialized placeholder
  val INVALID: Value = MatchingValue("__INVALID_ACTION__")

  val CREATE: Value = MatchingValue("create")
  val UPDATE_PROPERTIES: Value = MatchingValue("update_properties")
  val STATE: Value = MatchingValue("state")
  val WITNESS: Value = MatchingValue("witness")
  val OPEN: Value = MatchingValue("open")
  val REOPEN: Value = MatchingValue("reopen")
  val CLOSE: Value = MatchingValue("close")
  vale SETUP : Value = MatchingValue("setup")

  def MatchingValue(v: String): Value with Matching = new Val(nextId, v) with Matching
  def unapply(s: String): Option[Value] = values.find(s == _.toString)
}
