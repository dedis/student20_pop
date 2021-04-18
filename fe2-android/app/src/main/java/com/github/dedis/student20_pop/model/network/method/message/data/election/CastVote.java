package com.github.dedis.student20_pop.model.network.method.message.data.election;

import com.github.dedis.student20_pop.model.network.method.message.data.Action;
import com.github.dedis.student20_pop.model.network.method.message.data.Data;
import com.github.dedis.student20_pop.model.network.method.message.data.Objects;
import com.google.gson.annotations.SerializedName;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CastVote extends Data {

    @SerializedName(value = "created_at")
    private long createdAt; // time the votes were submited
    private String lao;
    private String election;
    private List<ElectionVote> votes;

    /**
     * Constructor for a data Cast Vote Election Event
     *
     * @param laoId id of the LAO
     * @param question  id of the question
     * @param votes list of vote indexes corresponding to the the ballot_options
     */
    public CastVote(
            boolean writeIn,
            List<List<Long>> votes,
            String question,
            String electionId,
            String laoId) {
        this.createdAt = Instant.now().toEpochMilli();
        this.votes = new ArrayList<>();
        this.lao = laoId;
        this.election = electionId;
        for (int i = 0; i < votes.size(); i++) {
            ElectionVote vote = new ElectionVote(question, votes.get(i), writeIn, electionId);
            this.votes.add(vote);
        }
    }


    public String getLaoId() {
        return lao;
    }
    public String getElectionId(){return election;}

    public long getCreation() {
        return createdAt;
    }

    public List<ElectionVote> getVotes() { return Collections.unmodifiableList(votes); }


    @Override
    public String getObject() {
        return Objects.ELECTION.getObject();
    }

    @Override
    public String getAction() {
        return Action.CAST_VOTE.getAction();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CastVote that = (CastVote) o;
        return java.util.Objects.equals(getLaoId(), that.getLaoId())
                && createdAt == that.getCreation()
                && java.util.Objects.equals(votes, that.getVotes());
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(
                getLaoId(),
                getCreation(),
                getVotes());
    }

    @Override
    public String toString() {
        StringBuilder builder = null;
        for (ElectionVote vote: votes) {
            builder.append(vote.toString());
        }
        return "CastVote{"
                + "lao='"
                + lao
                + '\''
                + ", creation='"
                + createdAt
                + '\''
                + ", election='"
                + election
                + '\''
                + ", votes = { '"
                + builder
                + '\''
                + '}'
                + '\''
                + '}';

    }

}
