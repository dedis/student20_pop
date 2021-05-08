package com.github.dedis.student20_pop.model.network.method.message.data.election;

import com.github.dedis.student20_pop.model.network.method.message.data.election.ElectionVote;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class CastVoteTest {

    private String questionId = " myQuestion";
    private String laoId = "myLao";
    private String electionId = " myElection";
    private Boolean writeIn = false ;
    private ElectionVote electionVote = new ElectionVote(questionId,new ArrayList<Long>(Arrays.asList(2L, 1L, 0L)),
            writeIn,electionId);
    private List<ElectionVote> electionVotes  = new ArrayList<>(Arrays.asList(electionVote));
List<List<Long>> votes = Collections.singletonList(new ArrayList<>(Arrays.asList(2L, 1L, 0L)));
CastVote castVote = new CastVote(writeIn,votes,questionId,electionId,laoId);
    @Test
    public void castVoteGetterReturnsCorrectLaoId() {
        assertThat(castVote.getLaoId(), is(laoId));
    }

    @Test
    public void castVoteGetterReturnsElectionId() {
        assertThat(castVote.getElectionId(), is(electionId));
    }

    @Test
    public void castVoteGetterReturnsVotes() {
        assertThat(castVote.getVotes(), is(electionVotes));
    }

}
