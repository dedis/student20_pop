package ch.epfl.pop.model.network

import ch.epfl.pop.pubsub.graph.PipelineError

trait Validatable {
  def validateContent(): Option[PipelineError]
}
