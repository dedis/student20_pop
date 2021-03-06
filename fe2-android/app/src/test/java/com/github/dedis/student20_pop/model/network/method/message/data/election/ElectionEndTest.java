package com.github.dedis.student20_pop.model.network.method.message.data.election;

import com.github.dedis.student20_pop.model.network.method.message.data.Action;
import com.github.dedis.student20_pop.model.network.method.message.data.Objects;

import org.junit.Test;

import java.time.Instant;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

public class ElectionEndTest {
    private String electionId = "electionId";
    private String laoId = "laoId";
    private String registeredVotes = "hashed";
    private ElectionEnd electionEnd = new ElectionEnd(electionId, laoId, registeredVotes);

    @Test
    public void electionEndGetterReturnsCorrectElectionId() {
        assertThat(electionEnd.getElectionId(), is(electionId));
    }

    @Test
    public void electionEndGetterReturnsCorrectLaoId() {
        assertThat(electionEnd.getLaoId(), is(laoId));
    }

    @Test
    public void electionEndGetterReturnsCorrectRegisteredVotes() {
        assertThat(electionEnd.getRegisteredVotes(), is(registeredVotes));
    }

    @Test
    public void electionEndGetterReturnsCorrectObject() {
        assertThat(electionEnd.getObject(), is(Objects.ELECTION.getObject()));
    }

    @Test
    public void electionEndGetterReturnsCorrectAction() {
        assertThat(electionEnd.getAction(), is(Action.END.getAction()));
    }

    @Test
    public void fieldsCantBeNull() {
        assertThrows(IllegalArgumentException.class, () -> new ElectionEnd(null, laoId, registeredVotes));
        assertThrows(IllegalArgumentException.class, () -> new ElectionEnd(electionId, null, registeredVotes));
        assertThrows(IllegalArgumentException.class, () -> new ElectionEnd(electionId, laoId, null));
    }

}
