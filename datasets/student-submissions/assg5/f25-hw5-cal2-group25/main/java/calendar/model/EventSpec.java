package calendar.model;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Immutable spec for creating events.
 */
public final class EventSpec {
  /**
   * Represents the status of the event, by default it is public.
   */
  public enum Status { PUBLIC, PRIVATE }

  private final String subject;
  private final LocalDateTime start;
  private final Optional<LocalDateTime> end;
  private final String description;
  private final String location;
  private final Status status;
  private final boolean allDay;

  /**
   * Creates an Event Specification object with all the specifications an event would have.
   *
   * @param subject     Subject of the Event.
   * @param start       Start Time of the Event.
   * @param end         End Time of the Event.
   * @param description Long Description of the Event.
   * @param location    Location of the Event
   * @param status      Status of the Event.
   * @param allDay      variable to track if event is being held all day.
   */
  public EventSpec(String subject, LocalDateTime start, LocalDateTime end,
                   String description, String location, Status status, boolean allDay) {
    if (subject == null || subject.isBlank()) {
      throw new IllegalArgumentException("Subject blank");
    }
    if (start == null) {
      throw new IllegalArgumentException("Start null");
    }
    this.subject = subject;
    this.start = start;
    this.end = Optional.ofNullable(end);
    this.description = description == null ? "" : description;
    this.location = location == null ? "" : location;
    this.status = status == null ? Status.PUBLIC : status;
    this.allDay = allDay;
  }

  /**
   * Returns the subject of the event.
   *
   * @return Subjet of the event.
   */
  public String subject() {
    return subject;
  }

  /**
   * Gets the start time of the event.
   *
   * @return the event start time
   */
  public LocalDateTime start() {
    return start;
  }

  /**
   * Gets the optional end time of the event.
   *
   * @return an {@code Optional} containing the end time if set
   */
  public Optional<LocalDateTime> end() {
    return end;
  }

  /**
   * Gets the description of the event.
   *
   * @return the event description (never null)
   */
  public String description() {
    return description;
  }

  /**
   * Gets the location of the event.
   *
   * @return the event location (never null)
   */
  public String location() {
    return location;
  }

  /**
   * Gets the visibility status of the event.
   *
   * @return the event status (PUBLIC or PRIVATE)
   */
  public Status status() {
    return status;
  }

  /**
   * Indicates whether the event lasts all day.
   *
   * @return {@code true} if the event is all day, {@code false} otherwise
   */
  public boolean allDay() {
    return allDay;
  }
}