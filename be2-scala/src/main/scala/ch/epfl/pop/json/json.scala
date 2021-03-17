package ch.epfl.pop

import ch.epfl.pop.json.Actions.Actions
import ch.epfl.pop.json.Objects.Objects


/** Collection of types used in Json parsing */
package object json {
  // TODO [unknown extra type?]. Should be removed at some point when we know how to describe an "extra"
  type UNKNOWN = String

  /** Base64 strings (untouched and decoded) */
  type Base64String = String
  type ByteArray = Array[Byte]

  type TimeStamp = Long
  type SignatureJson = ByteArray
  type Key = ByteArray
  type HashJson = ByteArray

  type ChannelName = String
  type ChannelMessage = MessageContent




  sealed trait Matching {
    // See https://stackoverflow.com/questions/3407032/comparing-string-and-enumeration
    def unapply(s: String): Boolean = s == toString
  }

  object Methods extends Enumeration {
    type Methods = Value

    val Subscribe: json.Methods.Value with Matching = MatchingValue("subscribe")
    val Unsubscribe: json.Methods.Value with Matching = MatchingValue("unsubscribe")
    val Broadcast: json.Methods.Value with Matching = MatchingValue("broadcast")
    val Catchup: json.Methods.Value with Matching = MatchingValue("catchup")
    val Publish: json.Methods.Value with Matching = MatchingValue("publish")

    def MatchingValue(v: String): Value with Matching = new Val(nextId, v) with Matching
    def unapply(s: String): Option[Value] = values.find(s == _.toString)
  }


  final case class MessageParameters(channel: ChannelName, message: Option[MessageContent])
  final case class MessageContent(
                                   encodedData: Base64String,
                                   data: MessageContentData,
                                   sender: Key,
                                   signature: SignatureJson,
                                   message_id: HashJson,
                                   witness_signatures: List[KeySignPair]
                                 ) {

    def updateWitnesses(s: KeySignPair): MessageContent =
      MessageContent(encodedData, data, sender, signature, message_id, s :: witness_signatures)
  }

  final case class MessageErrorContent(code: Int, description: String)

  final case class KeySignPair(witness: Key, signature: SignatureJson)

  /* --------------------------------------------------------- */
  /* ---------------------- ADMIN TYPES ---------------------- */
  /* --------------------------------------------------------- */

  object Objects extends Enumeration {
    type Objects = Value

    val Lao: json.Objects.Value with Matching = MatchingValue("lao")
    val Message: json.Objects.Value with Matching = MatchingValue("message")
    val Meeting: json.Objects.Value with Matching = MatchingValue("meeting")
    val RollCall: json.Objects.Value with Matching = MatchingValue("roll_call")

    def MatchingValue(v: String): Value with Matching = new Val(nextId, v) with Matching
    def unapply(s: String): Option[Value] = values.find(s == _.toString)
  }


  object Actions extends Enumeration {
    type Actions = Value

    val Create: json.Actions.Value = MatchingValue("create")
    val UpdateProperties: json.Actions.Value = MatchingValue("update_properties")
    val State: json.Actions.Value = MatchingValue("state")
    val Witness: json.Actions.Value = MatchingValue("witness")
    /* roll call related actions */
    val Open: json.Actions.Value = MatchingValue("open")
    val Reopen: json.Actions.Value = MatchingValue("reopen")
    val Close: json.Actions.Value = MatchingValue("close")

    def MatchingValue(v: String): Value with Matching =  new Val(nextId, v) with Matching
    def unapply(s: String): Option[Value] = values.find(s == _.toString)
  }


  /** Data field of a JSON message */
  final case class MessageContentData(
                                       /* basic common fields */
                                       _object: Objects,
                                       action: Actions,

                                       /* LAO related fields */
                                       id: Array[Byte],
                                       name: String,
                                       creation: TimeStamp,
                                       last_modified: TimeStamp,
                                       organizer: Key,
                                       witnesses: List[Key],

                                       /* state LAO broadcast fields */
                                       modification_id: Array[Byte],
                                       modification_signatures: List[KeySignPair],

                                       /* witness a message related fields */
                                       message_id: Base64String,
                                       signature: SignatureJson,

                                       /* meeting related fields */
                                       location: String,
                                       start: TimeStamp,
                                       end: TimeStamp,
                                       extra: UNKNOWN,

                                       /* roll call related fields */
                                       scheduled: TimeStamp,
                                       roll_call_description: String,
                                       attendees: List[Key],
  )
}
