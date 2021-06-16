package com.github.dedis.student20_pop.model.network.method.message.data.rollcall;

import com.github.dedis.student20_pop.model.event.EventState;
import com.github.dedis.student20_pop.model.network.method.message.data.Action;
import com.github.dedis.student20_pop.model.network.method.message.data.Data;
import com.github.dedis.student20_pop.model.network.method.message.data.Objects;
import com.github.dedis.student20_pop.utility.network.IdGenerator;
import com.google.gson.annotations.SerializedName;

/** Data sent to open a roll call */
public class OpenRollCall extends Data {

  @SerializedName("update_id")
  private final String updateId;
  private final String opens;
  @SerializedName("opened_at")
  private final long openedAt;
  private String action;

  /**
   * Constructor of a data Open Roll-Call
   *
   * @param laoId id of lao
   * @param opens The 'update_id' of the latest roll call close, or in its absence, the 'id' field
   *     of the roll call creation
   * @param openedAt timestamp corresponding to roll call open
   * @param state the state in which the roll call is when this instance is created
   */
  public OpenRollCall(String laoId, String opens, long openedAt, EventState state) {
    this.updateId = IdGenerator.generateOpenRollCallId(laoId, opens, openedAt);
    this.opens = opens;
    this.openedAt = openedAt;
    if(state==EventState.CLOSED){
      this.action = Action.REOPEN.getAction();
    }else{
      this.action = Action.OPEN.getAction();
    }
  }

  public OpenRollCall(String updateId, String opens, long openedAt, String action) {
    this.updateId = updateId;
    this.opens = opens;
    this.openedAt = openedAt;
    this.action = action;
  }

  public long getOpenedAt() {
    return openedAt;
  }

  @Override
  public String getObject() {
    return Objects.ROLL_CALL.getObject();
  }

  @Override
  public String getAction() {
    return action;
  }

  public String getUpdateId() {
    return updateId;
  }

  public String getOpens() {
    return opens;
  }
}
