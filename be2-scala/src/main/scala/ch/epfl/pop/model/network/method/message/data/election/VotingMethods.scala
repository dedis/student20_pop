package ch.epfl.pop.model.network.method.message.data.election

object  VotingMethod extends Enumeration{
    type VotingMethod = String
    type PluralityVotingMethod = VotingMethod
    type ApprovalVotingMethod = VotingMethod

    val VotingMethod : PluralityVotingMethod = "Plurality"

    val VotingMethod : ApprovalVotingMethod = "Approval"
}