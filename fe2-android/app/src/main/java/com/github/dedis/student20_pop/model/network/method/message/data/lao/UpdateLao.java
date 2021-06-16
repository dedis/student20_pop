package com.github.dedis.student20_pop.model.network.method.message.data.lao;

import com.github.dedis.student20_pop.model.network.method.message.data.Action;
import com.github.dedis.student20_pop.model.network.method.message.data.Data;
import com.github.dedis.student20_pop.model.network.method.message.data.Objects;
import com.github.dedis.student20_pop.utility.network.IdGenerator;
import com.google.gson.annotations.SerializedName;

import java.util.HashSet;
import java.util.Set;

/** Data sent to update the lao specifications */
public class UpdateLao extends Data {

  private final String id;
  private final String name;

  @SerializedName("last_modified")
  private final long lastModified;

  private final Set<String> witnesses;

  /**
   * Constructor for a data Update LAO
   *
   * @param organizer public key of the LAO
   * @param creation creation time
   * @param name name of the LAO
   * @param lastModified time of last modification
   * @param witnesses list of witnesses of the LAO
   */
  public UpdateLao(String organizer, long creation, String name, long lastModified, Set<String> witnesses) {
    this.id = IdGenerator.generateLaoId(organizer, creation, name);
    this.name = name;
    this.lastModified = lastModified;
    this.witnesses = witnesses;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public long getLastModified() {
    return lastModified;
  }

  public Set<String> getWitnesses() {
    return new HashSet<>(witnesses);
  }

  @Override
  public String getObject() {
    return Objects.LAO.getObject();
  }

  @Override
  public String getAction() {
    return Action.UPDATE.getAction();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    UpdateLao updateLao = (UpdateLao) o;
    return getLastModified() == updateLao.getLastModified()
        && java.util.Objects.equals(getName(), updateLao.getName())
        && java.util.Objects.equals(getWitnesses(), updateLao.getWitnesses());
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(getName(), getLastModified(), getWitnesses());
  }

  @Override
  public String toString() {
    return "UpdateLao{"
        + "name='"
        + name
        + '\''
        + ", last_modified="
        + lastModified
        + ", witnesses="
        + witnesses
        + '}';
  }
}
