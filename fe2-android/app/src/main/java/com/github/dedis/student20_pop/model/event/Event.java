package com.github.dedis.student20_pop.model.event;

/** Class modeling an Event */
public abstract class Event implements Comparable<Event> {

  public abstract long getStartTimestamp();

  public abstract long getEndTimestamp();
  public  EventType type;
  @Override
  public int compareTo(Event o) {
    int start = Long.compare(this.getStartTimestamp(), o.getStartTimestamp());
    if (start != 0) {
      return start;
    }

    return Long.compare(this.getEndTimestamp(), o.getEndTimestamp());
  }

}
