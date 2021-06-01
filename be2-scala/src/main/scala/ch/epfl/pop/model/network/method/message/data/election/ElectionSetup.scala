package ch.epfl.pop.model.network.method.message.data.election

import ch.epfl.pop.model.objects.{Hash, Timestamp,PublicKey}
import java.sql.Time
import java.security.PublicKey

object  BallotOption{
    type BallotOption = String
}

case class Question(
                        id : PublicKey,
                        questionAsked : String,
                        votingMethod : VotingMethod,
                        ballotOptions : List[BallotOption],
                        writeIn : Boolean

)

case class ElectionSetup(
                            id: Hash,
                            laoId : Hash,
                            name: String,
                            version: String,
                            createdAt: Timestamp,
                            startTime : Timestamp,
                            endTime : Timestamp,
                            questions : List[Question]
                            

                        )extends MessageData{
  override val _object: ObjectType = ObjectType.ELECTION
  override val action: ActionType = ActionType.SETUP
}

object Question extends Parsable{
    def apply(id: PublicKey, questionAsked: String, votingMethod: VotingMethods, ballotOptions: List[BallotOption], writeIn: Boolean): Question = {
        new Question(id, questionAsked, votingMethod, ballotOptions, writeIn)
    }
}

object ElectionSetup extends Parsable{
    def apply(id: Hash, laoId: String, name: String, version : String, createdAt: Timestamp, startTime: Timestamp, endTime: Timestamp, questions: List[Questions]) : ElectionSetup = {
        new ElectionSetup(id, laoId, name, version, createdAt, startTime,endTime,questions)
    }
 
    override def buildFromJson(payload: String): CreateLao = payload.parseJson.asJsObject.convertTo[ElectionSetup]
}