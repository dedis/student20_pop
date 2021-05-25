package com.github.dedis.student20_pop.model.network.method.message;


import com.github.dedis.student20_pop.utility.security.Hash;
import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.List;

public class  ElectionQuestion {

    private String id;
    private String question;
    @SerializedName(value = "voting_method")
    private String votingMethod;
    @SerializedName(value = "ballot_options")
    private List<String> ballotOptions;
    @SerializedName(value = "write_in")
    private boolean writeIn;

    /**
     * Constructor for a data Question, for the election setup
     */
    public ElectionQuestion(
            String question,
            String votingMethod,
            boolean writeIn,
            List<String> ballotOptions,
            String electionId) {

        this.question = question;
        this.ballotOptions = ballotOptions;
        this.writeIn = writeIn;
        this.votingMethod = votingMethod;
        this.id = Hash.hash("Question", electionId, question);
    }


    public String getId() {
        return id;
    }

    public String getQuestion() {
        return question;
    }

    public boolean getWriteIn() { return writeIn; }

    public List<String> getBallotOptions() { return Collections.unmodifiableList(ballotOptions); }

    public String getVotingMethod() { return votingMethod; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ElectionQuestion that = (ElectionQuestion) o;
        return getQuestion() == that.getQuestion()
                && getWriteIn() == that.getWriteIn()
                && java.util.Objects.equals(getId(), that.getId())
                && java.util.Objects.equals(getBallotOptions(), that.getBallotOptions())
                && getVotingMethod() == that.getVotingMethod();
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(
                getId(),
                getVotingMethod(),
                getWriteIn(),
                getBallotOptions(),
                getQuestion());
    }

    @Override
    public String toString() {

        return "ElectionQuestion{"
                + "id='"
                + id
                + '\''
                + ", question='"
                + question
                + '\''
                + ", voting method='"
                + votingMethod
                + '\''
                + ", ballot options='"
                + ballotOptions
                + '\''
                + ", write in ='"
                + '}';
    }


}

