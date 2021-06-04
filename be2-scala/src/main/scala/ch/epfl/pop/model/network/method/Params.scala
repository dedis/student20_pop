package ch.epfl.pop.model.network.method

import ch.epfl.pop.model.objects.Channel

class Params(val channel: Channel) {
  def hasMessage: Boolean = false
}
