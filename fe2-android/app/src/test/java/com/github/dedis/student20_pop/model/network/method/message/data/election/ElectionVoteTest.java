package com.github.dedis.student20_pop.model.network.method.message.data.election;

import com.github.dedis.student20_pop.model.network.method.message.data.ElectionVote;
import com.github.dedis.student20_pop.utility.network.IdGenerator;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ElectionVoteTest {
    private String electionId = "my election id";
    private String questionId = " my question id";
    private List<Integer> votes = new ArrayList<>(Arrays.asList(2,1,0)); // we vote for ballot option in position 2, then posiion 1 and 0
    private String writeIn = "My write in ballot option";
    ElectionVote electionVote1 = new ElectionVote(questionId,votes,false,writeIn,electionId);
    ElectionVote electionVote2 = new ElectionVote(questionId,votes,true,writeIn,electionId);

    @Test
    public void electionVoteGetterReturnsCorrectId() {
        assertThat(electionVote1.getId(), is(IdGenerator.generateElectionVoteId(electionId, questionId, electionVote1.getVotes(), electionVote1.getWriteIn(), false)));
    }

    @Test
    public void electionVoteGetterReturnsCorrectQuestionId() {
        assertThat(electionVote1.getQuestionId(), is(questionId));
    }

    @Test
    public void attributesIsNull() {
        assertNull(electionVote1.getWriteIn());
        assertNull(electionVote2.getVotes());
        assertNotNull(electionVote1.getVotes());
        assertNotNull(electionVote2.getWriteIn());
    }

    @Test
    public void electionVoteGetterReturnsCorrectWriteIn() {
        assertThat(electionVote2.getWriteIn(),is(writeIn));
    }


    @Test
    public void electionVoteGetterReturnsCorrectVotes() {
        assertThat(electionVote1.getVotes(), is(votes));
    }

    @Test
    public void isEqual() {
        assertNotEquals(electionVote1,electionVote2);
        assertEquals(electionVote1,new ElectionVote(questionId,votes,false,writeIn,electionId));
        assertNotEquals(electionVote1,new ElectionVote("random",votes,false,writeIn,electionId));
        assertNotEquals(electionVote1,new ElectionVote(questionId,new ArrayList<>(Arrays.asList(0,1,2)),false,writeIn,electionId));
        assertNotEquals(electionVote1,new ElectionVote(questionId,votes,false,writeIn,"random"));

        // here because writeInEnabled is false it will be computed as null making both elections the same even though we don't give them the same constructor
        assertEquals(electionVote1,new ElectionVote(questionId,votes,false,"random",electionId));

        // here because writeInEnabled is true the list of votes should be computed as null making both election the same
        assertEquals(electionVote2,new ElectionVote(questionId,new ArrayList<>(Arrays.asList(0,1,2)),true,writeIn,electionId));
    }
}
