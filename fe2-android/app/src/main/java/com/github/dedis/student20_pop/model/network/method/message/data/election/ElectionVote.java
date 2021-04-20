package com.github.dedis.student20_pop.model.network.method.message.data.election;

import androidx.annotation.Nullable;

import com.github.dedis.student20_pop.model.network.method.message.data.Action;
import com.github.dedis.student20_pop.model.network.method.message.data.Data;
import com.github.dedis.student20_pop.model.network.method.message.data.Objects;
import com.github.dedis.student20_pop.model.network.method.message.data.rollcall.CreateRollCall;
import com.github.dedis.student20_pop.utility.security.Hash;
import com.google.gson.annotations.SerializedName;

import java.security.cert.PKIXRevocationChecker;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ElectionVote extends Data {

    private String id;
    @SerializedName(value = "question")
    private String questionId; // id of the question
    private List<Long> votes;
    private Boolean writeIn;

    /**
     * Constructor for a data Question, for the election setup
     */
    public ElectionVote(
            String questionId,
            List<Long> votes,
            Boolean writeIn,
            String electionId) {

        this.questionId = questionId;
        this.writeIn = writeIn;
        this.votes = votes;
        this.id = Hash.hash("Vote", electionId, questionId, votes.toString());
    }


    public String getId() {
        return id;
    }

    public String getQuestionId() {
        return questionId;
    }

    public Boolean getWriteIn() { return writeIn; }

    public List<Long> getVote_results(){ return votes;}

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
        ElectionVote that = (ElectionVote) o;
        return getQuestionId() == that.getQuestionId()
                && getWriteIn() == that.getWriteIn()
                && java.util.Objects.equals(getId(), that.getId())
                && java.util.Objects.equals(getVote_results(), that.getVote_results());

    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(
                getId(),
                getVote_results(),
                getWriteIn(),
                getQuestionId());
    }

    //TODO
    @Override
    public String toString() {
        return null;
    }


}

