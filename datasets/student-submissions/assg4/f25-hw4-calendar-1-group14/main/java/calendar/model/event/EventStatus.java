package calendar.model.event;

/**
 * Enum representing the privacy status of an event.
 */
public enum EventStatus {
  /**
   * Public event - visible to everyone.
   */
  PUBLIC("Public"),

  /**
   * Private event - restricted visibility.
   */
  PRIVATE("Private");

  private final String displayName;

  /**
   * Constructs an EventStatus with a display name.
   *
   * @param displayName the human-readable name
   */
  EventStatus(String displayName) {
    this.displayName = displayName;
  }

  /**
   * Gets the display name for this status.
   *
   * @return the display name
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * Parses an EventStatus from a string.
   * Accepts "public", "private" (case-insensitive).
   *
   * @param status the status string
   * @return the corresponding EventStatus
   * @throws IllegalArgumentException if status is invalid
   */
  public static EventStatus fromString(String status) {
    if (status == null) {
      throw new IllegalArgumentException("Status cannot be null");
    }

    String normalized = status.trim().toUpperCase();

    switch (normalized) {
      case "PUBLIC":
        return PUBLIC;
      case "PRIVATE":
        return PRIVATE;
      default:
        throw new IllegalArgumentException("Invalid status: " + status
            + ". Must be 'public' or 'private'");
    }
  }

  /**
   * Checks if this status is public.
   *
   * @return true if PUBLIC
   */
  public boolean isPublic() {
    return this == PUBLIC;
  }

  /**
   * Checks if this status is private.
   *
   * @return true if PRIVATE
   */
  public boolean isPrivate() {
    return this == PRIVATE;
  }

  /**
   * Returns a string representation of this object.
   *
   * @return the display name
   */
  @Override
  public String toString() {
    return displayName;
  }
}