package calendar.model.impl;

import java.util.Objects;
import java.util.UUID;

/**
 * Unique identifier for an Event.
 * Each EventID wraps a randomly generated UUID value.
 * This class is immutable and implements Comparable.
 */
public final class EventId implements Comparable<EventId> {
  private final UUID id = UUID.randomUUID();

  @Override
  public int compareTo(EventId o) {
    return id.compareTo(o.id);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || o.getClass() != this.getClass()) {
      return false;
    }
    EventId other = (EventId) o;
    return this.id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return id.toString();
  }
}
