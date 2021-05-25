package com.github.dedis.student20_pop.model;

import com.github.dedis.student20_pop.model.event.Event;
import org.junit.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

public class LaoTest {

    private static final String LAO_1_ID = "lao1Id";
    private static final String LAO_2_ID= "lao2Id";
    private static final String LAO_NAME_1 = "LAO name 1";
    private static final String LAO_NAME_2 = "LAO name 2";
    private static final String ORGANIZER = "0x2365";
    private static final String rollCallId1 = "rollCallId1";
    private static final String rollCallId2 = "rollCallId2";
    private static final String rollCallId3 = "rollCallId3";
    private static final String electionId1 = "electionId1";
    private static final String electionId2 = "rollCallId2";
    private static final String electionId3 = "rollCallId3";
    private static final String WITNESS = "0x3435";
    private static final List<String> WITNESSES = Arrays.asList("0x3434", "0x4747");
    private static final List<String> WITNESSES_WITH_NULL = Arrays.asList("0x3939", null,
   "0x4747");

    private static final Lao LAO_1 = new Lao(LAO_NAME_1, ORGANIZER);
    private static final Lao LAO_2 = new Lao(LAO_NAME_2, ORGANIZER);
    private static final List<Event> EVENTS =
        Arrays.asList(
            new Election(),
            new RollCall());
    private static final List<Event> EVENTS_WITH_NULL =
        Arrays.asList(
            new Election(),
            null,
            new RollCall());

    private static final List<Lao> LAOS = new ArrayList<>(Arrays.asList(LAO_1, LAO_2));
    private static final List<Lao> LAOS_WITH_NULL =
        new ArrayList<>(Arrays.asList(LAO_1, null, LAO_2));

    @Test
    public void removeRollCallTest() {
        LAO_1.setRollCalls(new HashMap<String,RollCall>() {{
            put(rollCallId1,new RollCall() );
            put(rollCallId2, new RollCall());
            put(rollCallId3, new RollCall());
        }}
        );
        assert(LAO_1.removeRollCall(rollCallId3)); // we want to assert that we can remove rollCallId3 successfully
        assert(LAO_1.getRollCalls().size() == 2);
        assert(LAO_1.getRollCalls().containsKey(rollCallId1));
        assert(LAO_1.getRollCalls().containsKey(rollCallId2));
        assert(! LAO_1.getRollCalls().containsKey(rollCallId3));

        LAO_1.setRollCalls(new HashMap<String,RollCall>() {{
                               put(rollCallId1,new RollCall() );
                               put(null, null);
                               put(rollCallId3, new RollCall());
                           }}
        );
        assert(!LAO_1.removeRollCall(rollCallId2));
    }

    @Test
    public void removeElectionTest() {
        LAO_1.setElections(new HashMap<String,Election>() {{
                               put(electionId1,new Election() );
                               put(electionId2, new Election());
                               put(electionId3, new Election());
                           }}
        );
        assert(LAO_1.removeElection(electionId3)); // we want to assert that we can remove electionId3 successfully
        assert(LAO_1.getElections().size() == 2);
        assert(LAO_1.getElections().containsKey(electionId1));
        assert(LAO_1.getElections().containsKey(electionId2));
        assert(! LAO_1.getElections().containsKey(electionId3));

        LAO_1.setElections(new HashMap<String,Election>() {{
                               put(electionId1,new Election() );
                               put(null, null);
                               put(electionId3, new Election());
                           }}
        );
        assert(!LAO_1.removeElection(electionId2));
    }

    @Test
    public void updateRollCalls(){

        LAO_1.setRollCalls(new HashMap<String,RollCall>() {{
                               put(rollCallId1,new RollCall() );
                               put(rollCallId2, new RollCall());
                               put(rollCallId3, new RollCall());
                           }}
        );
        RollCall r1 = new RollCall();
        assert(LAO_1.getRollCalls().get(rollCallId1) != r1);
        LAO_1.updateRollCall(rollCallId1,r1);
        LAO_1.updateRollCall(rollCallId3,r1);
        assert(LAO_1.getRollCalls().containsKey(rollCallId1));
        assert(LAO_1.getRollCalls().containsKey(rollCallId2));
        assert(LAO_1.getRollCalls().containsKey(rollCallId3));
        assert(LAO_1.getRollCalls().get(rollCallId1) == r1);
        assert(LAO_1.getRollCalls().get(rollCallId3) == r1);

    }

    @Test
    public void updateElections(){

        LAO_1.setElections(new HashMap<String,Election>() {{
                               put(electionId1,new Election() );
                               put(electionId2, new Election());
                               put(electionId3, new Election());
                           }}
        );
        Election e1 = new Election();
        assert(LAO_1.getElections().get(electionId1) != e1);
        LAO_1.updateElection(electionId1,e1);
        LAO_1.updateElection(electionId3,e1);
        assert(LAO_1.getElections().containsKey(electionId1));
        assert(LAO_1.getElections().containsKey(electionId2));
        assert(LAO_1.getElections().containsKey(electionId3));
        assert(LAO_1.getElections().get(rollCallId1) == e1);
        assert(LAO_1.getElections().get(rollCallId3) == e1);

    }


    @Test
    public void createLaoNullParametersTest() {
      assertThrows(IllegalArgumentException.class, () -> new Lao(null, LAO_NAME_1));
      assertThrows(IllegalArgumentException.class, () -> new Lao(LAO_1_ID, null));
      assertThrows(IllegalArgumentException.class, () -> new Lao(null));
    }

    @Test
    public void createLaoEmptyNameTest() {
          assertThrows(IllegalArgumentException.class, () -> new Lao(LAO_1_ID, ""));
      assertThrows(IllegalArgumentException.class, () -> new Lao(LAO_1_ID, "     "));
    }

    @Test
    public void setAndGetNameTest() {
      Lao lao = new Lao(LAO_1_ID, LAO_NAME_1);
      assertThat(lao.getName(), is(LAO_NAME_1));
      lao.setName(LAO_NAME_2);
      assertThat(lao.getName(), is(LAO_NAME_2));
    }

    @Test
    public void getOrganizerTest() {
      LAO_1.setOrganizer(ORGANIZER);
      assertThat(LAO_1.getOrganizer(), is(ORGANIZER));
    }

  //  @Test
  //  public void getHostTest() {
  //    assertThat(LAO_1.getHost(), is(HOST));
  //  }
  //
  //  @Test
  //  public void setAndGetWitnessesTest() {
  //    Lao lao = new Lao(LAO_NAME_1, ORGANIZER, HOST);
  //    lao.setWitnesses(WITNESSES);
  //    assertThat(lao.getWitnesses(), is(WITNESSES));
  //  }
  //
  //  @Test
  //  public void addWitnessTest() {
  //    Lao lao = new Lao(LAO_NAME_1, ORGANIZER, HOST);
  //    lao.addWitness(WITNESS);
  //    assertThat(lao.getWitnesses(), hasItem(WITNESS));
  //  }
  //
  //  @Test
  //  public void setAndGetMembersTest() {
  //    Lao lao = new Lao(LAO_NAME_1, ORGANIZER, HOST);
  //    lao.setMembers(WITNESSES);
  //    assertThat(lao.getMembers(), is(WITNESSES));
  //  }
  //
  //  @Test
  //  public void setAndGetEventsTest() {
  //    LAO_FOR_EVENTS.setEvents(EVENTS);
  //    assertThat(LAO_FOR_EVENTS.getEvents(), is(EVENTS));
  //  }
  //
  //  @Test
  //  public void addEventTest() {
  //    Lao lao = new Lao(LAO_NAME_1, ORGANIZER, HOST);
  //    Event event = new PollEvent("question", 0, 0, lao.getId(), "loc", new ArrayList<>(), false);
  //    lao.addEvent(event);
  //    assertThat(lao.getEvents(), hasItem(event));
  //  }
  //
  //  @Test
  //  public void setNullNameTest() {
  //    assertThrows(IllegalArgumentException.class, () -> LAO_1.setName(null));
  //  }
  //
  //  @Test
  //  public void setEmptyNameTest() {
  //    assertThrows(IllegalArgumentException.class, () -> LAO_1.setName(""));
  //  }
  //
  //  @Test
  //  public void setNullWitnessesTest() {
  //    assertThrows(IllegalArgumentException.class, () -> LAO_1.setWitnesses(null));
  //    assertThrows(IllegalArgumentException.class, () -> LAO_1.setWitnesses(WITNESSES_WITH_NULL));
  //  }
  //
  //  @Test
  //  public void setNullMembersTest() {
  //    assertThrows(IllegalArgumentException.class, () -> LAO_1.setMembers(null));
  //    assertThrows(IllegalArgumentException.class, () -> LAO_1.setMembers(WITNESSES_WITH_NULL));
  //  }
  //
  //  @Test
  //  public void setNullEventsTest() {
  //    assertThrows(IllegalArgumentException.class, () -> LAO_1.addEvent(null));
  //    assertThrows(IllegalArgumentException.class, () -> LAO_1.setEvents(null));
  //    assertThrows(IllegalArgumentException.class, () -> LAO_1.setEvents(EVENTS_WITH_NULL));
  //  }
  //
  //  @Test
  //  public void getNullIdsTest() {
  //    assertThrows(IllegalArgumentException.class, () -> Lao.getIds(null));
  //    assertThrows(IllegalArgumentException.class, () -> Lao.getIds(LAOS_WITH_NULL));
  //  }
  //
  //  @Test
  //  public void getIdsTest() {
  //    assertThat(
  //        Lao.getIds(LAOS),
  //        is(new ArrayList<>(Arrays.asList(LAO_1.getId(), LAO_FOR_EVENTS.getId()))));
  //  }
  //
  //  @Test
  //  public void equalsTest() {
  //    Lao lao1 = new Lao(LAO_NAME_1, ORGANIZER, HOST);
  //    Lao lao2 = new Lao(LAO_NAME_1, ORGANIZER, HOST);
  //    Lao lao3 = new Lao(LAO_NAME_2, ORGANIZER, HOST);
  //
  //    assertEquals(lao1, lao2);
  //    assertNotEquals(lao1, lao3);
  //  }
  //
  //  @Test
  //  public void hashCodeTest() {
  //    Lao lao1 = new Lao(LAO_NAME_1, ORGANIZER, HOST);
  //    Lao lao2 = new Lao(LAO_NAME_1, ORGANIZER, HOST);
  //    Lao lao3 = new Lao(LAO_NAME_2, ORGANIZER, HOST);
  //
  //    assertEquals(lao1.hashCode(), lao2.hashCode());
  //    assertNotEquals(lao1.hashCode(), lao3.hashCode());
  //  }
}
