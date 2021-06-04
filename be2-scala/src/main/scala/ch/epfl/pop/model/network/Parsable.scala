package ch.epfl.pop.model.network

trait Parsable {
  /**
   * Builds an instance of the class implementing Parsable out of its json representation
   *
   * @param payload json string representation
   * @return an instance of the class implementing Parsable
   */
  def buildFromJson(payload: String): Any
}
