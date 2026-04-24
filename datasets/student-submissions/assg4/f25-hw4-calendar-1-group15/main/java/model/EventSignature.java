package model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents the unique signature of an event based on its subject,
 * start time, and end time. This class is used for conflict detection
 * and event identification within the calendar system.
 */
public class EventSignature {
  private final String subject;
  private final LocalDateTime startDateTime;
  private final LocalDateTime endDateTime;

  /**
   * Constructs an EventSignature with the specified properties.
   *
   * @param subject       the event subject (cannot be null)
   * @param startDateTime the start date and time (cannot be null)
   * @param endDateTime   the end date and time (cannot be null)
   * @throws IllegalArgumentException if any parameter is null
   */
  public EventSignature(String subject, LocalDateTime startDateTime, LocalDateTime endDateTime) {
    if (subject == null) {
      throw new IllegalArgumentException("subject cannot be null");
    }
    if (startDateTime == null) {
      throw new IllegalArgumentException("startDateTime cannot be null");
    }
    if (endDateTime == null) {
      throw new IllegalArgumentException("endDateTime cannot be null");
    }
    this.subject = subject;
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
  }

  /**
   * Constructs an EventSignature from an existing event.
   *
   * @param event the event to create a signature from
   * @throws IllegalArgumentException if the event or its properties are null
   */
  public EventSignature(Ievent event) {
    this(event.getSubject(), event.getStartDateTime(), event.getEndDateTime());
  }


  /**
   * Compares this signature with another object for equality.
   * Two signatures are equal if they have the same subject, start time, and end time.
   *
   * @param o the object to compare with
   * @return true if the signatures are equal, false otherwise
   */
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof EventSignature)) {
      return false;
    }
    EventSignature other = (EventSignature) o;
    return Objects.equals(subject, other.subject)
        && Objects.equals(startDateTime, other.startDateTime)
        && Objects.equals(endDateTime, other.endDateTime);
  }

  /**
   * Generates a hash code for this signature.
   *
   * @return the hash code based on subject, start time, and end time
   */
  public int hashCode() {
    return Objects.hash(subject, startDateTime, endDateTime);
  }

  /**
   * Returns a string representation of this signature.
   *
   * @return a string in the format "EventSignature[subject from startDateTime to endDateTime]"
   */
  public String toString() {
    return String.format("EventSignature[%s from %s to %s]", subject, startDateTime, endDateTime);
  }
}
