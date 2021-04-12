package com.github.dedis.student20_pop.model.network.method.message.data.rollcall;

import androidx.annotation.Nullable;
import com.github.dedis.student20_pop.model.network.method.message.data.Action;
import com.github.dedis.student20_pop.model.network.method.message.data.Data;
import com.github.dedis.student20_pop.model.network.method.message.data.Objects;
import com.github.dedis.student20_pop.utility.security.Hash;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/** Data sent to create a Roll-Call */
public class CreateRollCall extends Data {

  private String id;
  private String name;
  private long creation;
  //private transient long start;
  //private transient StartType startType;
  private long proposedStart;
  private long proposedEnd;
  private String location;

  @Nullable private transient String description;

  /**
   * Constructor for a data Create Roll-Call Event
   *
   * @param name name of the Roll-Call
   * @param proposedStart of the Roll-Call
   * @param proposedEnd of the Roll-Call
   * @param location location of the Roll-Call
   * @param description can be null
   */
  public CreateRollCall(
      String name,
      long proposedStart,
      long proposedEnd,
      String location,
      @Nullable String description,
      String laoId) {
    this.name = name;
    this.creation = Instant.now().toEpochMilli();
    this.proposedStart= proposedStart;
    this.proposedEnd = proposedEnd;
    this.location = location;
    this.description = description;
    this.id = Hash.hash("R", laoId, Long.toString(creation), name);
  }

  public CreateRollCall(
      String id,
      String name,
      long creation,
      long proposedStart,
      long proposedEnd,
      String location,
      String description) {
    this.id = id;
    this.name = name;
    this.creation = creation;
    this.proposedStart= proposedStart;
    this.proposedEnd = proposedEnd;
    this.location = location;
    this.description = description;
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

  public long getProposedStart() {
    return proposedStart;
  }

  public long getProposedEnd() {
    return proposedEnd;
  }

  public String getLocation() {
    return location;
  }

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }

  @Override
  public String getObject() {
    return Objects.ROLL_CALL.getObject();
  }

  @Override
  public String getAction() {
    return Action.CREATE.getAction();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CreateRollCall that = (CreateRollCall) o;
    return getCreation() == that.getCreation()
        && proposedStart == that.proposedStart
        && proposedEnd == that.proposedEnd
        && java.util.Objects.equals(getId(), that.getId())
        && java.util.Objects.equals(getName(), that.getName())
        && java.util.Objects.equals(getLocation(), that.getLocation())
        && java.util.Objects.equals(getDescription(), that.getDescription());
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(
        getId(),
        getName(),
        getCreation(),
        getProposedStart(),
        getProposedEnd(),
        getLocation(),
        getDescription());
  }

  @Override
  public String toString() {
    return "CreateRollCall{"
        + "id='"
        + id
        + '\''
        + ", name='"
        + name
        + '\''
        + ", creation="
        + creation
        + ", start="
        + proposedStart
        + ", startType="
        + proposedEnd
        + ", location='"
        + location
        + '\''
        + ", description='"
        + description
        + '\''
        + '}';
  }
}
