package ch.epfl.pop.model.network

object MethodType extends Enumeration {
  type MethodType = Value

  // uninitialized placeholder
  val INVALID: Value = MatchingValue("__INVALID_METHOD__")

  val BROADCAST: Value = MatchingValue("message")
  val PUBLISH: Value = MatchingValue("publish")
  val SUBSCRIBE: Value = MatchingValue("subscribe")
  val UNSUBSCRIBE: Value = MatchingValue("unsubscribe")
  val CATCHUP: Value = MatchingValue("catchup")

  def MatchingValue(v: String): Value with Matching = new Val(nextId, v) with Matching
  def unapply(s: String): Option[Value] = values.find(s == _.toString)
}

