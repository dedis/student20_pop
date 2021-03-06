package com.github.dedis.student20_pop.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Class modeling a Local Autonomous Organization (LAO) */
public final class Lao {

  private String channel;
  private String id;
  private String name;
  private Long lastModified;
  private Long creation;
  private String organizer;
  private String modificationId;
  private Set<String> witnesses;
  private Map<String,WitnessMessage> witnessMessages; /** map between a messages ID and the corresponding object WitnessMessage that has to be signed by witnesses */

  private Set<PendingUpdate> pendingUpdates;

  private Map<String, RollCall> rollCalls;
  private Map<String,Election> elections;
  public Lao(String id) {
    if(id == null) {
      throw new IllegalArgumentException(" The id is null");
    }
    else if(id.isEmpty()) {
      throw new IllegalArgumentException(" The id of the Lao is empty");
    }
    this.channel = id;
    this.id = id;
    this.rollCalls = new HashMap<>();
    this.elections = new HashMap<>();
    this.witnessMessages = new HashMap<>();
    this.witnesses = new HashSet<>();
    this.pendingUpdates = new HashSet<>();
  }

  public Lao(String id, String name) {
    this(id);
    if(name == null) {
      throw new IllegalArgumentException(" The name of the Lao is null");
    }
    else if(name.isEmpty()) {
      throw new IllegalArgumentException(" The name of the Lao is empty");
    }
    else {
      this.name = name;
    }
  }

  public void updateRollCall(String prevId, RollCall rollCall) {

    if(rollCall == null) {
      throw new IllegalArgumentException(" The roll call is null");
    }

    if (rollCalls.containsKey(prevId)) {
      rollCalls.remove(prevId);
    }
    String newId = rollCall.getId();
    rollCalls.put(newId, rollCall);
  }

  public void updateElection(String prevId, Election election) {
    if(election == null) {
      throw new IllegalArgumentException(" The election is null");
    }
    if (elections.containsKey(prevId)) {
      elections.remove(prevId);
    }
    String newId = election.getId();
    elections.put(newId, election);
  }


  /**
   * Update the list of messages that have to be signed by witnesses.
   * If the list of messages contain the message with  Id prevId , it will
   * remove this message from the list. Then it will add the new message to the list with the corresponding newId
   *
   * @param prevId  the previous id of a message that needs to be signed
   * @param witnessMessage  the object representing the message needing to be signed
   */
  public void updateWitnessMessage(String prevId, WitnessMessage witnessMessage) {
    if (witnessMessages.containsKey(prevId)) {
      witnessMessages.remove(prevId);
    }
    String newId = witnessMessage.getMessageId();
    witnessMessages.put(newId, witnessMessage);
  }

  public Optional<RollCall> getRollCall(String id) {
    return Optional.ofNullable(rollCalls.get(id));
  }

  public Optional<Election> getElection(String id) {
    return Optional.ofNullable(elections.get(id));
  }

  public Optional<WitnessMessage> getWitnessMessage(String id) {
    return Optional.ofNullable(witnessMessages.get(id));
  }


  /**
   * Removes an election from the list of elections.
   *
   * @param id       the id of the Election
   * @return true if the election was deleted
   */
  public boolean removeElection(String id) {
    return (elections.remove(id) != null ) ;

  }

  /**
   * Removes a roll call from the list of roll calls.
   *
   * @param id       the id of the Roll Call
   * @return true if the roll call was deleted
   */
  public boolean removeRollCall(String id) {
    return (rollCalls.remove(id) != null ) ;

  }

  public Long getLastModified() {
    return lastModified;
  }

  public Set<String> getWitnesses() {
    return witnesses;
  }

  public Set<PendingUpdate> getPendingUpdates() {
    return pendingUpdates;
  }

  public String getOrganizer() {
    return organizer;
  }

  public String getChannel() {
    return channel;
  }

  public void setChannel(String channel) {
    this.channel = channel;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    if(id == null) {
      throw new IllegalArgumentException(" The Id of the Lao is null");
    }
    else if(id.isEmpty()) {
      throw new IllegalArgumentException(" The id of the Lao is empty");
    }

    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {

    if(name == null) {
      throw new IllegalArgumentException(" The name of the Lao is null");
    }
    else if(name.isEmpty()) {
      throw new IllegalArgumentException(" The name of the Lao is empty");
    }

    this.name = name;
  }

  public void setLastModified(Long lastModified) {
    this.lastModified = lastModified;
  }

  public Long getCreation() {
    return creation;
  }

  public void setCreation(Long creation) {
    this.creation = creation;
  }

  public void setOrganizer(String organizer) {
    this.organizer = organizer;
  }

  public String getModificationId() {
    return modificationId;
  }

  public void setModificationId(String modificationId) {
    this.modificationId = modificationId;
  }

  public void setWitnesses(Set<String> witnesses) {

    if(witnesses == null) {
      throw new IllegalArgumentException(" The witnesses set is null");
    }
    for (String witness : witnesses) {
      if (witness == null) throw new IllegalArgumentException("One of the witnesses in the set is null");
    }
    this.witnesses = witnesses;
  }

  public void setPendingUpdates(Set<PendingUpdate> pendingUpdates) {
    this.pendingUpdates = pendingUpdates;
  }

  public Map<String, Election> getElections() {return elections;}

  public Map<String, RollCall> getRollCalls() {
    return rollCalls;
  }

  public Map<String,WitnessMessage> getWitnessMessages() {return witnessMessages;}

  public void setRollCalls(Map<String, RollCall> rollCalls) {
    this.rollCalls = rollCalls;
  }

  public void setElections(Map<String, Election> elections) {
    this.elections = elections;
  }

}
