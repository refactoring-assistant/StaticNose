package calendar.model.impl;

import java.util.Objects;
import java.util.UUID;

/**
 * Unique identifier for a series of related events.
 * Each SeriesId wraps a randomly generated UUID.
 * Two SeriesId objects are equal if they have the same UUID.
 */
public final class SeriesId implements Comparable<SeriesId> {
  private final UUID id = UUID.randomUUID();

  @Override
  public int compareTo(SeriesId o) {
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
    SeriesId other = (SeriesId) o;
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
