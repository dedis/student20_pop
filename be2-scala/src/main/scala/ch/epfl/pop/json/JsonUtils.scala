package ch.epfl.pop.json

import java.util.Base64

import ch.epfl.pop.json.Actions.Actions
import ch.epfl.pop.json.JsonUtils.ErrorCodes.ErrorCodes
import ch.epfl.pop.json.Objects.Objects


object JsonUtils {

  /** JSON-RPC protocol version */
  val JSON_RPC_VERSION: String = "2.0"

  /** Static references to Base64 encoder/decoder */
  val ENCODER: Base64.Encoder = Base64.getEncoder
  val DECODER: Base64.Decoder = Base64.getDecoder

  /** Parsing exception, always caught by the parser */
  final case class JsonMessageParserException(
                                               description: String,
                                               id: Option[Int] = None,
                                               errorCode: ErrorCodes = ErrorCodes.InvalidData
                                             ) extends Exception(description) {}

  /** Object sent back to PubSub if a parsing error occurred */
  final case class JsonMessageParserError(
                                           description: String,
                                           id: Option[Int] = None,
                                           errorCode: ErrorCodes = ErrorCodes.InvalidData
                                         )

  object ErrorCodes extends Enumeration {
    type ErrorCodes = Value

    // operation was successful (should never be used)
    val Success: JsonUtils.ErrorCodes.Value = Value(0, "operation was successful")
    // invalid action
    val InvalidAction: JsonUtils.ErrorCodes.Value = Value(-1, "invalid action")
    // invalid resource (e.g. channel does not exist, channel was not subscribed to, etc.)
    val InvalidResource: JsonUtils.ErrorCodes.Value = Value(-2, "invalid resource")
    // resource already exists (e.g. lao already exists, channel already exists, etc.)
    val AlreadyExists: JsonUtils.ErrorCodes.Value = Value(-3, "resource already exists")
    // request data is invalid (e.g. message is invalid)
    val InvalidData: JsonUtils.ErrorCodes.Value = Value(-4, "request data is invalid")
    // access denied (e.g. subscribing to a “restricted” channel)
    val AccessDenied: JsonUtils.ErrorCodes.Value = Value(-5, "access denied")
  }



  /** Builder for MessageContentData */
  final class MessageContentDataBuilder() {
    /* basic common fields */
    var _object: Objects = _
    var action: Actions = _

    /* LAO related fields */
    var id: ByteArray = Array[Byte]()
    var name: String = ""
    var creation: TimeStamp = -1L
    var last_modified: TimeStamp = -1L
    var organizer: Key = Array[Byte]()
    var witnesses: List[Key] = List()

    /* state LAO broadcast fields */
    var modification_id: ByteArray = Array[Byte]()
    var modification_signatures: List[KeySignPair] = List()

    /* witness a message related fields */
    var message_id: Base64String = ""
    var signature: SignatureJson = Array[Byte]()

    /* meeting related fields */
    var location: String = ""
    var start: TimeStamp = -1L
    var end: TimeStamp = -1L
    var extra: UNKNOWN = ""

    /* roll call related fields */
    var scheduled: TimeStamp = -1L
    var roll_call_description: String = ""
    var attendees: List[Key] = List()


    def build(): MessageContentData = {
      MessageContentData(
        _object, action,
        id, name, creation, last_modified, organizer, witnesses,
        modification_id, modification_signatures,
        message_id, signature,
        location, start, end, extra,
        scheduled, roll_call_description, attendees
      )
    }

    def setHeader(obj: Objects, act: Actions): MessageContentDataBuilder = {setObject(obj); setAction(act); this }

    def setObject(obj: Objects): MessageContentDataBuilder = { this._object = obj; this }
    def setAction(action: Actions): MessageContentDataBuilder = { this.action = action; this }
    def setId(id: ByteArray): MessageContentDataBuilder = { this.id = id; this }
    def setName(name: String): MessageContentDataBuilder = { this.name = name; this }
    def setCreation(creation: TimeStamp): MessageContentDataBuilder = { this.creation = creation; this }
    def setLastModified(lastModified: TimeStamp): MessageContentDataBuilder = { this.last_modified = lastModified; this }
    def setOrganizer(organizer: Key): MessageContentDataBuilder = { this.organizer = organizer; this }
    def setWitnesses(witnesses: List[Key]): MessageContentDataBuilder = { this.witnesses = witnesses; this }
    def setModificationId(modification_id: ByteArray): MessageContentDataBuilder = { this.modification_id = modification_id; this }
    def setMessageId(id: Base64String): MessageContentDataBuilder = { this.message_id = id; this }
    def setModificationSignatures(modification_sig: List[KeySignPair]): MessageContentDataBuilder = { this.modification_signatures = modification_sig; this}
    def setSignature(signature: SignatureJson): MessageContentDataBuilder = { this.signature = signature; this }
    def setLocation(location: String): MessageContentDataBuilder = { this.location = location; this }
    def setStart(start: TimeStamp): MessageContentDataBuilder = { this.start = start; this }
    def setEnd(end: TimeStamp): MessageContentDataBuilder = { this.end = end; this }
    def setExtra(extra: UNKNOWN): MessageContentDataBuilder = { this.extra = extra; this }
    def setScheduled(scheduled: TimeStamp): MessageContentDataBuilder = { this.scheduled = scheduled; this }
    def setRollCallDescription(description: String): MessageContentDataBuilder = { this.roll_call_description = description; this }
    def setAttendees(attendees: List[Key]): MessageContentDataBuilder = { this.attendees = attendees; this }
  }
}
