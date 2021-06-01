package com.github.dedis.student20_pop.model.network.method.message.data;

import com.github.dedis.student20_pop.model.network.method.message.data.election.ElectionSetup;
import com.github.dedis.student20_pop.model.network.method.message.data.lao.CreateLao;
import com.github.dedis.student20_pop.model.network.method.message.data.lao.StateLao;
import com.github.dedis.student20_pop.model.network.method.message.data.lao.UpdateLao;
import com.github.dedis.student20_pop.model.network.method.message.data.meeting.CreateMeeting;
import com.github.dedis.student20_pop.model.network.method.message.data.meeting.StateMeeting;
import com.github.dedis.student20_pop.model.network.method.message.data.message.WitnessMessage;
import com.github.dedis.student20_pop.model.network.method.message.data.rollcall.CloseRollCall;
import com.github.dedis.student20_pop.model.network.method.message.data.rollcall.CreateRollCall;
import com.github.dedis.student20_pop.model.network.method.message.data.rollcall.OpenRollCall;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.github.dedis.student20_pop.model.network.method.message.data.Action.CLOSE;
import static com.github.dedis.student20_pop.model.network.method.message.data.Action.CREATE;
import static com.github.dedis.student20_pop.model.network.method.message.data.Action.OPEN;
import static com.github.dedis.student20_pop.model.network.method.message.data.Action.REOPEN;
import static com.github.dedis.student20_pop.model.network.method.message.data.Action.SETUP;
import static com.github.dedis.student20_pop.model.network.method.message.data.Action.STATE;
import static com.github.dedis.student20_pop.model.network.method.message.data.Action.UPDATE;
import static com.github.dedis.student20_pop.model.network.method.message.data.Action.WITNESS;
import static com.github.dedis.student20_pop.model.network.method.message.data.Objects.ELECTION;
import static com.github.dedis.student20_pop.model.network.method.message.data.Objects.LAO;
import static com.github.dedis.student20_pop.model.network.method.message.data.Objects.MEETING;
import static com.github.dedis.student20_pop.model.network.method.message.data.Objects.MESSAGE;
import static com.github.dedis.student20_pop.model.network.method.message.data.Objects.ROLL_CALL;

/** An abstract high level message */
public abstract class Data {

  /** A mapping of (object, action) -> class */
  private static final Map<EntryPair, Class<? extends Data>> messages = buildMessagesMap();

  /**
   * Create an entry pair given obj and action
   *
   * @param obj of the pair
   * @param action of the pair
   * @return the pair
   */
  private static EntryPair pair(Objects obj, Action action) {
    return new EntryPair(obj, action);
  }

  /**
   * Build the protocol messages map
   *
   * @return the built map (Unmodifiable)
   */
  private static Map<EntryPair, Class<? extends Data>> buildMessagesMap() {
    Map<EntryPair, Class<? extends Data>> messagesMap = new HashMap<>();

    // Lao
    messagesMap.put(pair(LAO, CREATE), CreateLao.class);
    messagesMap.put(pair(LAO, UPDATE), UpdateLao.class);
    messagesMap.put(pair(LAO, STATE), StateLao.class);

    // Meeting
    messagesMap.put(pair(MEETING, CREATE), CreateMeeting.class);
    messagesMap.put(pair(MEETING, STATE), StateMeeting.class);

    // Message
    messagesMap.put(pair(MESSAGE, WITNESS), WitnessMessage.class);

    // Roll Call
    messagesMap.put(pair(ROLL_CALL, CREATE), CreateRollCall.class);
    messagesMap.put(pair(ROLL_CALL, OPEN), OpenRollCall.class);
    messagesMap.put(pair(ROLL_CALL, REOPEN), OpenRollCall.class);
    messagesMap.put(pair(ROLL_CALL, CLOSE), CloseRollCall.class);

    // Election
    messagesMap.put(pair(ELECTION,SETUP), ElectionSetup.class);


    return Collections.unmodifiableMap(messagesMap);
  }

  /**
   * Return the class assigned to the pair (obj, action)
   *
   * @param obj of the entry
   * @param action of the entry
   * @return the class assigned to the pair of empty if none are defined
   */
  public static Optional<Class<? extends Data>> getType(Objects obj, Action action) {
    return Optional.ofNullable(messages.get(pair(obj, action)));
  }

  /** Returns the object the message is referring to. */
  public abstract String getObject();

  /** Returns the action the message is handling. */
  public abstract String getAction();

  /** Entry of the messages map. A pair of (Objects, Action) */
  private static final class EntryPair {

    private final Objects object;
    private final Action action;

    /**
     * Constructor for the EntryPair
     *
     * @param object of the pair
     * @param action of the pair
     */
    private EntryPair(Objects object, Action action) {
      this.object = object;
      this.action = action;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      EntryPair entryPair = (EntryPair) o;
      return object == entryPair.object && action == entryPair.action;
    }

    @Override
    public int hashCode() {
      return java.util.Objects.hash(object, action);
    }
  }
}
