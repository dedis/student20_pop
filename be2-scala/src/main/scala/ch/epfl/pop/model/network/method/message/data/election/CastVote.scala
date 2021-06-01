package ch.epfl.pop.model.network.method.message.data.election

import ch.epfl.pop.model.objects.{Hash, Timestamp,PublicKey}
import java.sql.Time
import java.security.PublicKey
import scala.collection.script.Message
import java.security.Timestamp

case class Vote(
                        id: PublicKey,
                        questionId: Hash,
                        voteIndexes: List[Int],
                        writeIn: String
)

case class CastVote(
                        laoId: Hash,
                        electionId: Hash,
                        createdAt: Timestamp,
                        votes: List[Vote]
)extends MessageData{
    override val _object: ObjectType = Object.ELECTION
    override val action: ActionType = ActionType.CAST_VOTE
}

object Vote extends Parsable{
    def apply(id: PublicKey, questionId: Hash, voteIndexes: List[Int], writeIn: String): Vote ={
        new Vote(id, questionId, voteIndexes, writeIn)
    }
}

object CastVote extends Parsable{
    def apply(laoId: Hash, electionId: Hash, createdAt: Timestamp, votes: List[Vote]):CastVote ={
        new CastVote(laoId, electionId, createdAt, votes)
    }
}