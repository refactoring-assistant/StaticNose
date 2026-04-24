package calendar.model;

import java.util.Objects;
import java.util.UUID;

/**
 * A simple type-safe wrapper for a series id UUID.
 */
public final class SeriesId {
  private final UUID id;

  private SeriesId(UUID id) {
    this.id = id;
  }

  /**
   * Generates a new unique SeriesId.
   *
   * @return a new SeriesId with a randomly generated UUID
   */
  public static SeriesId newId() {
    return new SeriesId(UUID.randomUUID());
  }

  /**
   * Wraps an existing UUID in a SeriesId.
   *
   * @param id the UUID to wrap; must not be null
   * @return a SeriesId representing the given UUID
   */
  public static SeriesId of(UUID id) {
    return new SeriesId(Objects.requireNonNull(id));
  }

  /**
   * Returns the underlying UUID value of this SeriesId.
   *
   * @return the UUID backing this SeriesId
   */
  public UUID value() {
    return id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SeriesId)) {
      return false;
    }
    SeriesId seriesId = (SeriesId) o;
    return id.equals(seriesId.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  @Override
  public String toString() {
    return id.toString();
  }
}

