package com.github.dedis.student20_pop.model.network.method.message.data;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Enumerates all possible messages objects */
public enum Objects {
  LAO("lao"),
  MEETING("meeting"),
  MESSAGE("message"),
  ROLL_CALL("roll_call"),
  ELECTION_QUESTION("question"),
  ELECTION_VOTE("vote"),
  ELECTION("election");

  private static final List<Objects> ALL = Collections.unmodifiableList(Arrays.asList(values()));
  private final String object;

  /**
   * Constructor for a message Object
   *
   * @param object name of the object
   */
  Objects(String object) {
    this.object = object;
  }

  /** Returns the name of the Object. */
  public String getObject() {
    return object;
  }

  /**
   * Find a given Object
   *
   * @param searched the searched object
   * @return the corresponding enum object
   */
  public static Objects find(String searched) {
    for (Objects object : ALL) if (object.getObject().equals(searched)) return object;

    return null;
  }
}
