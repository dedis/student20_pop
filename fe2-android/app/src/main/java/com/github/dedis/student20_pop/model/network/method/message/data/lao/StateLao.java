package com.github.dedis.student20_pop.model.network.method.message.data.lao;

import com.github.dedis.student20_pop.model.network.method.message.PublicKeySignaturePair;
import com.github.dedis.student20_pop.model.network.method.message.data.Action;
import com.github.dedis.student20_pop.model.network.method.message.data.Data;
import com.github.dedis.student20_pop.model.network.method.message.data.Objects;
import com.github.dedis.student20_pop.utility.network.IdGenerator;
import com.google.gson.annotations.SerializedName;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Data received to track the state of a lao */
public class StateLao extends Data {

  private final String id;
  private final String name;
  private final long creation;
  @SerializedName("last_modified")
  private final long lastModified;
  private final String organizer;
  @SerializedName("modification_id")
  private final String modificationId;
  private final Set<String> witnesses;

  @SerializedName("modification_signatures")
  private final List<PublicKeySignaturePair> modificationSignatures;

  /**
   * Constructor for a data State LAO
   *
   * @param id of the LAO state message, Hash(organizer||creation||name)
   * @param name name of the LAO
   * @param creation time of creation
   * @param lastModified time of last modification
   * @param organizer id of the LAO's organizer
   * @param witnesses list of witnesses of the LAO
   * @throws IllegalArgumentException if the id is not valid
   */
  public StateLao(
      String id,
      String name,
      long creation,
      long lastModified,
      String organizer,
      String modificationId,
      Set<String> witnesses,
      List<PublicKeySignaturePair> modificationSignatures) {
    if(!id.equals(IdGenerator.generateLaoId(organizer, creation, name))) {
      throw new IllegalArgumentException("StateLao id must be Hash(organizer||creation||name)");
    }
    this.id = id;
    this.name = name;
    this.creation = creation;
    this.lastModified = lastModified;
    this.organizer = organizer;
    this.modificationId = modificationId;
    this.witnesses = witnesses;
    this.modificationSignatures = modificationSignatures;
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

  public long getLastModified() {
    return lastModified;
  }

  public String getOrganizer() {
    return organizer;
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
    return Action.STATE.getAction();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    StateLao stateLao = (StateLao) o;
    return getCreation() == stateLao.getCreation()
        && getLastModified() == stateLao.getLastModified()
        && java.util.Objects.equals(getId(), stateLao.getId())
        && java.util.Objects.equals(getName(), stateLao.getName())
        && java.util.Objects.equals(getOrganizer(), stateLao.getOrganizer())
        && java.util.Objects.equals(getWitnesses(), stateLao.getWitnesses());
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(
        getId(), getName(), getCreation(), getLastModified(), getOrganizer(), getWitnesses());
  }

  @Override
  public String toString() {
    return "StateLao{"
        + "id='"
        + id
        + '\''
        + ", name='"
        + name
        + '\''
        + ", creation="
        + creation
        + ", last_modified="
        + lastModified
        + ", organizer='"
        + organizer
        + '\''
        + ", witnesses="
        + witnesses
        + '}';
  }

  public String getModificationId() {
    return modificationId;
  }

  public List<PublicKeySignaturePair> getModificationSignatures() {
    return modificationSignatures;
  }
}
