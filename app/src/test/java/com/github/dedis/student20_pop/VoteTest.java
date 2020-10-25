package com.github.dedis.student20_pop;

import com.github.dedis.student20_pop.model.Vote;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;

public class VoteTest {

    private final String person1 = "Person 1";
    private final String person2 = "Person 2";
    private final String election = "0x3434";
    private final String vote = "Encrypted Vote";
    private final Vote vote1 = new Vote(person1, election, vote);
    private final Vote vote2 = new Vote(person2, election, vote);

    @Test
    public void createVoteNullParametersTest() {
        assertThrows(IllegalArgumentException.class, () -> new Vote(null, election, vote));
        assertThrows(IllegalArgumentException.class, () -> new Vote(person1, null, vote));
        assertThrows(IllegalArgumentException.class, () -> new Vote(person1, election, null));
    }

    @Test
    public void getPersonTest() {
        assertThat(vote1.getPerson(), is(person1));
    }

    @Test
    public void getElectionTest() {
        assertThat(vote1.getElection(), is(election));
    }

    @Test
    public void getVoteTest() {
        assertThat(vote1.getVote(), is(vote));
    }

    @Test
    public void getAttestationTest() {
        assertThat(vote1.getAttestation(), is(election + vote));
    }

    @Test
    public void equalsTest() {
        assertEquals(vote1, vote1);
        assertNotEquals(vote1, vote2);
    }

    @Test
    public void hashCodeTest() {
        assertEquals(vote1.hashCode(), vote1.hashCode());
        assertNotEquals(vote1.hashCode(), vote2.hashCode());
    }
}
