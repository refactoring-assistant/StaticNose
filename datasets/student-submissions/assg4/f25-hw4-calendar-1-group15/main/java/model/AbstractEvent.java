package model;


import static model.CalendarConstants.ALL_DAY_END;

import java.time.LocalDateTime;
import java.util.Objects;


/**
 * it is an abstract class for calendar events that has common functionality for
 * both single events and series events.
 * All date/time values are stored and interpreted as Eastern Standard Time (EST).
 */
public abstract class AbstractEvent implements Ievent {
  private String subject;
  private String description;
  private LocalDateTime startDateTime;
  private LocalDateTime endDateTime;
  private boolean isPublic;
  private String location;

  /**
   * Constructor that initializes the properties of a calendar object.
   *
   * @param subject       the subject of the event
   * @param description   the description of the event
   * @param startDateTime the start date and time of the event
   * @param endDateTime   the end date and time of the event
   * @param isPublic      true if event is public, false if private
   * @param location      the location of the event
   * @throws IllegalArgumentException if endDateTime is not null and is not after startDateTime
   */
  protected AbstractEvent(String subject, String description, LocalDateTime startDateTime,
                          LocalDateTime endDateTime, boolean isPublic, String location
  ) {
    this.subject = subject;
    this.description = description;
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
    this.isPublic = isPublic;
    this.location = location;

    if (endDateTime != null && !endDateTime.isAfter(startDateTime)) {
      throw new IllegalArgumentException(
          "endDateTime should not be null and endDateTime must be after startDateTime");
    }

  }

  @Override
  public String getSubject() {
    return subject;
  }


  @Override
  public LocalDateTime getStartDateTime() {
    return startDateTime;
  }


  @Override
  public LocalDateTime getEndDateTime() {
    if (endDateTime == null) {
      return startDateTime.toLocalDate().atTime(ALL_DAY_END);
    }
    return endDateTime;
  }


  @Override
  public String getDescription() {
    return description;
  }


  @Override
  public String getLocation() {
    return location;
  }


  @Override
  public boolean isPublic() {
    return isPublic;
  }

  /**
   * Compares this event to another object for equality.
   *
   * @param o the reference object with which to compare.
   * @return true if both the objects represent the same event, false otherwise
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Ievent)) {
      return false;
    }

    Ievent other = (Ievent) o;
    return Objects.equals(subject, other.getSubject())
        && Objects.equals(startDateTime, other.getStartDateTime())
        && Objects.equals(getEndDateTime(), other.getEndDateTime());
  }

  /**
   * Generates a hash code for this event based on its subject, startDateTime and endDateTime.
   *
   * @return the hash value for the event
   */
  @Override
  public int hashCode() {
    return Objects.hash(subject, startDateTime, getEndDateTime());
  }

  /**
   * Returns the string representation of this event by showing subject,
   * startDateTime and endDateTime.
   *
   * @return a string
   */
  @Override
  public String toString() {
    return String.format("%s: from %s to %s", subject, startDateTime, getEndDateTime());
  }


  /**
   * Sets the subject of the event.
   *
   * @param subject the new subject
   * @throws IllegalArgumentException if subject is null or empty after trimming
   */
  public void setSubject(String subject) {
    if (subject == null || subject.trim().isEmpty()) {
      throw new IllegalArgumentException("subject cannot be null or empty");
    }
    this.subject = subject;
  }

  /**
   * Sets the start date and time of the event.
   *
   * @param startDateTime the new start date and time
   * @throws IllegalArgumentException if startDateTime is null
   * @throws IllegalArgumentException if the current endDateTime would be before or
   *                                  equal to the new startDateTime
   */
  public void setStartDateTime(LocalDateTime startDateTime) {
    if (startDateTime == null) {
      throw new IllegalArgumentException("startDateTime cannot be null");
    }
    if (this.endDateTime != null && !this.endDateTime.isAfter(startDateTime)) {
      throw new IllegalArgumentException("Would make endDateTime before startDateTime");
    }
    this.startDateTime = startDateTime;
  }

  /**
   * Sets the start date and time of the event.
   *
   * @param endDateTime the new end date and time
   * @throws IllegalArgumentException if endDateTime is not null and is not after startDateTime
   */
  public void setEndDateTime(LocalDateTime endDateTime) {
    if (endDateTime != null && !endDateTime.isAfter(startDateTime)) {
      throw new IllegalArgumentException("endDateTime must be after start dateTime");
    }
    this.endDateTime = endDateTime;
  }

  /**
   * Sets the visibility of the event.
   *
   * @param isPublic true to make the event public, false to make it private
   */
  public void setIsPublic(boolean isPublic) {
    this.isPublic = isPublic;
  }

  /**
   * Sets the location of the event.
   *
   * @param location the new location
   */
  public void setLocation(String location) {
    this.location = location == null ? "" : location;
  }

  /**
   * Sets the description of the event.
   *
   * @param description the new description
   */
  public void setDescription(String description) {
    this.description = description == null ? "" : description;
  }


}
