package com.github.dedis.student20_pop.model.network.method.message;

import com.github.dedis.student20_pop.model.network.method.message.data.election.ElectionVote;
import com.google.gson.annotations.SerializedName;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ElectionVoteTest {


    private String electionId = "my election id";
    private String questionId = " my question id";
    private List<Long> votes = new ArrayList<Long>(Arrays.asList(2L,1L,0L)); // we vote for ballot option in position 2, then posiion 1 and 0
    private boolean writeIn = false;
    ElectionVote electionVote = new ElectionVote(questionId,votes,writeIn,electionId);

    @Test
    public void electionVoteGetterReturnsCorrectId() {
        assertThat(electionVote.getId(), is(electionId));
    }

    @Test
    public void electionVoteGetterReturnsCorrectQuestionId() {
        assertThat(electionVote.getQuestionId(), is(questionId));
    }

    @Test
    public void electionVoteGetterReturnsCorrectWriteIn() {
        assertThat(electionVote.getWriteIn(), is(writeIn));
    }

    @Test
    public void electionVoteGetterReturnsCorrectVotes() {
        assertThat(electionVote.getVote_results(), is(votes));
    }

}
