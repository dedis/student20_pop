package ch.epfl.pop.model.network.method.message.data

trait MessageData {
  def _object: ObjectType.ObjectType
  def action: ActionType.ActionType
}
