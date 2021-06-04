package ch.epfl.pop.model.network.method.message.data

trait MessageData {
  val _object: ObjectType.ObjectType
  val action: ActionType.ActionType
}
