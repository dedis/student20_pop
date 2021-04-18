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

  private Set<PendingUpdate> pendingUpdates;

  private Map<String, RollCall> rollCalls;
  private Map<String,Election> elections;
  public Lao(String id) {
    this.channel = id;
    this.id = id;
    this.rollCalls = new HashMap<>();
    this.elections = new HashMap<>();
    this.witnesses = new HashSet<>();
    this.pendingUpdates = new HashSet<>();
  }

  public Lao(String id, String name) {
    this(id);
    this.name = name;
  }

  public void updateRollCall(String prevId, RollCall rollCall) {
    if (rollCalls.containsKey(prevId)) {
      rollCalls.remove(prevId);
    }
    String newId = rollCall.getId();
    rollCalls.put(newId, rollCall);
  }

  public void updateElections(String prevId, Election election) {
    if (elections.containsKey(prevId)) {
      elections.remove(prevId);
    }
    String newId = election.getId();
    elections.put(newId, election);
  }

  public Optional<RollCall> getRollCall(String id) {
    return Optional.ofNullable(rollCalls.get(id));
  }

  public Optional<Election> getElection(String id) {
    return Optional.ofNullable(elections.get(id));
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
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
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
    this.witnesses = witnesses;
  }

  public void setPendingUpdates(Set<PendingUpdate> pendingUpdates) {
    this.pendingUpdates = pendingUpdates;
  }

  public Map<String, Election> getElections() {return elections;}

  public Map<String, RollCall> getRollCalls() {
    return rollCalls;
  }

  public void setRollCalls(Map<String, RollCall> rollCalls) {
    this.rollCalls = rollCalls;
  }

}
