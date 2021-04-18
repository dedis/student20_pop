package com.github.dedis.student20_pop.model;

import com.github.dedis.student20_pop.model.event.Event;
import com.github.dedis.student20_pop.model.event.EventType;

import java.util.HashSet;
import java.util.Set;

public class RollCall extends Event {

  private String id;
  private String name;
  private long creation;
  private long start;
  private long scheduled;
  private long end;
  private Set<String> attendees;

  private String location;
  private String description;

  public RollCall() {
    this.attendees = new HashSet<>();
    type = EventType.ROLL_CALL;
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

  public long getCreation() {
    return creation;
  }

  public void setCreation(long creation) {
    this.creation = creation;
  }

  public long getStart() {
    return start;
  }

  public void setStart(long start) {
    this.start = start;
  }

  public long getScheduled() {
    return scheduled;
  }

  public void setScheduled(long scheduled) {
    this.scheduled = scheduled;
  }

  public long getEnd() {
    return end;
  }

  public void setEnd(long end) {
    this.end = end;
  }

  public Set<String> getAttendees() {
    return attendees;
  }

  public void setAttendees(Set<String> attendees) {
    this.attendees = attendees;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public long getStartTimestamp() {
    if (start != 0) {
      return start;
    }
    return scheduled;
  }

  @Override
  public long getEndTimestamp() {
    if (end == 0) {
      return Long.MAX_VALUE;
    }
    return end;
  }
}
