package com.github.dedis.student20_pop.model.network.method.message.data.rollcall;

import com.github.dedis.student20_pop.model.network.method.message.data.Action;
import com.github.dedis.student20_pop.model.network.method.message.data.Data;
import com.github.dedis.student20_pop.model.network.method.message.data.Objects;
import com.github.dedis.student20_pop.utility.network.IdGenerator;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/** Data sent to close a Roll-Call */
public class CloseRollCall extends Data {

  @SerializedName("update_id")
  private final String updateId;
  private final String closes;
  @SerializedName("closed_at")
  private final long closedAt;
  private final List<String> attendees;

  /**
   * Constructor for a data Close Roll-Call Event
   *
   * @param laoId id of the LAO
   * @param closes The 'update_id' of the latest roll call open, or in its absence, the 'id' field
   *     of the roll call creation
   * @param closedAt timestamp of the roll call close
   * @param attendees list of attendees of the Roll-Call
   */
  public CloseRollCall(String laoId, String closes, long closedAt, List<String> attendees) {
    this.updateId = IdGenerator.generateCloseRollCallId(laoId, closes, closedAt);
    this.closes = closes;
    this.closedAt = closedAt;
    this.attendees = attendees;
  }

  @Override
  public String getObject() {
    return Objects.ROLL_CALL.getObject();
  }

  @Override
  public String getAction() {
    return Action.CLOSE.getAction();
  }

  public String getUpdateId() {
    return updateId;
  }

  public String getCloses() {
    return closes;
  }

  public long getClosedAt() {
    return closedAt;
  }

  public List<String> getAttendees() {
    return attendees;
  }
}
