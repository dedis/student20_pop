package com.github.dedis.student20_pop.model.network.method.message.data.meeting;

import com.github.dedis.student20_pop.model.network.method.message.data.Action;
import com.github.dedis.student20_pop.model.network.method.message.data.Data;
import com.github.dedis.student20_pop.model.network.method.message.data.Objects;
import com.github.dedis.student20_pop.utility.network.IdGenerator;

/** Data sent to create a new meeting */
public class CreateMeeting extends Data {

  private final String id;
  private final String name;
  private final long creation;
  private final String location;
  private final long start;
  private final long end;

  /**
   * Constructor for a data Create Meeting Event
   *
   * @param laoId id of the LAO
   * @param id of the Meeting creation message, Hash("M"||laoId||creation||name)
   * @param name name of the Meeting
   * @param creation time of creation
   * @param location location of the Meeting
   * @param start of the Meeting
   * @param end of the Meeting
   * @throws IllegalArgumentException if the id is invalid
   */
  public CreateMeeting(
      String laoId, String id, String name, long creation, String location, long start, long end) {
    if(!id.equals(IdGenerator.generateCreateMeetingId(laoId, creation, name))) {
      throw new IllegalArgumentException("CreateMeeting id must be Hash(\"M\"||laoId||creation||name)");
    }
    this.id = id;
    this.name = name;
    this.creation = creation;
    this.location = location;
    this.start = start;
    this.end = end;
  }

  public CreateMeeting(
          String laoId, String name, long creation, String location, long start, long end) {
    this.id = IdGenerator.generateCreateMeetingId(laoId, creation, name);
    this.name = name;
    this.creation = creation;
    this.location = location;
    this.start = start;
    this.end = end;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public long getCreation() {
    return creation;
  }

  public String getLocation() {
    return location;
  }

  public long getStart() {
    return start;
  }

  public long getEnd() {
    return end;
  }

  @Override
  public String getObject() {
    return Objects.MEETING.getObject();
  }

  @Override
  public String getAction() {
    return Action.CREATE.getAction();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CreateMeeting that = (CreateMeeting) o;
    return getCreation() == that.getCreation()
        && getStart() == that.getStart()
        && getEnd() == that.getEnd()
        && java.util.Objects.equals(getId(), that.getId())
        && java.util.Objects.equals(getName(), that.getName())
        && java.util.Objects.equals(getLocation(), that.getLocation());
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(
        getId(), getName(), getCreation(), getLocation(), getStart(), getEnd());
  }

  @Override
  public String toString() {
    return "CreateMeeting{"
        + "id='"
        + id
        + '\''
        + ", name='"
        + name
        + '\''
        + ", creation="
        + creation
        + ", location='"
        + location
        + '\''
        + ", start="
        + start
        + ", end="
        + end
        + '}';
  }
}
