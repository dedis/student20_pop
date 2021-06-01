package ch.epfl.pop.model.network.method.message.data.election

import ch.epfl.pop.model.objects.{Hash, Timestamp,PublicKey}
import java.sql.Time
import java.security.PublicKey
import scala.collection.script.Message
import java.security.Timestamp

case class ElectionEnd(
                            laoId: Hash,
                            electionId: Hash,
                            createdAt: Timestamp,
                            registeredVotes: Hash

)extends MessageData{
    override val _object: ObjectType = Object.ELECTION
    override val action: ActionType = ActionType.END
}

object ElectionEnd extends Parsable{
    def apply(laoId: Hash, electionId: Hash, createdAt: Timestamp, registeredVotes: Hash):ElectionEnd = {
        new ElectionEnd(laoId, electionId, createdAt, registeredVotes) 
    }
}
