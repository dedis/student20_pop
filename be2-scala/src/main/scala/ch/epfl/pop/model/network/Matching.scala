package ch.epfl.pop.model.network

trait Matching {
  // See https://stackoverflow.com/questions/3407032/comparing-string-and-enumeration
  def unapply(s: String): Boolean = s == toString
}
