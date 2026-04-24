package calendar.model.event;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Implementation of a calendar event.
 * This class is immutable - all modification methods return new instances.
 */
public class Event implements Ievent {

  private final String subject;
  private final LocalDateTime startDateTime;
  private final LocalDateTime endDateTime;
  private final String description;
  private final String location;
  private final EventStatus status;

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

  /**
   * Constructs an event with all parameters.
   *
   * @param subject       the event subject (required)
   * @param startDateTime the start date/time (required)
   * @param endDateTime   the end date/time (required)
   * @param description   optional description
   * @param location      optional location
   * @param isPublic      true for public, false for private
   * @throws IllegalArgumentException if required parameters are null or invalid
   */
  public Event(String subject, LocalDateTime startDateTime, LocalDateTime endDateTime,
               String description, String location, boolean isPublic) {
    this(subject, startDateTime, endDateTime, description, location,
        isPublic ? EventStatus.PUBLIC : EventStatus.PRIVATE);
  }

  /**
   * Constructs an event with EventStatus.
   *
   * @param subject       the event subject (required)
   * @param startDateTime the start date/time (required)
   * @param endDateTime   the end date/time (required)
   * @param description   optional description
   * @param location      optional location
   * @param status        the event status
   * @throws IllegalArgumentException if required parameters are null or invalid
   */
  public Event(String subject, LocalDateTime startDateTime, LocalDateTime endDateTime,
               String description, String location, EventStatus status) {
    if (subject == null || subject.trim().isEmpty()) {
      throw new IllegalArgumentException("Event subject cannot be null or empty");
    }
    if (startDateTime == null) {
      throw new IllegalArgumentException("Start date/time cannot be null");
    }
    if (endDateTime == null) {
      throw new IllegalArgumentException("End date/time cannot be null");
    }
    if (startDateTime.isAfter(endDateTime)) {
      throw new IllegalArgumentException("Start time must be before or equal to end time");
    }
    if (status == null) {
      throw new IllegalArgumentException("Event status cannot be null");
    }

    this.subject = subject.trim();
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
    this.description = description;
    this.location = location;
    this.status = status;
  }

  /**
   * Creates an event with minimal parameters (no description or location).
   *
   * @param subject       the event subject
   * @param startDateTime the start date/time
   * @param endDateTime   the end date/time
   */
  public Event(String subject, LocalDateTime startDateTime, LocalDateTime endDateTime) {
    this(subject, startDateTime, endDateTime, null, null, EventStatus.PUBLIC);
  }

  /**
   * Creates an all-day event on a specific date.
   *
   * @param subject the event subject
   * @param date    the date of the event
   * @return a new all-day event (8:00 AM to 5:00 PM)
   */
  public static Event allDayEvent(String subject, LocalDate date) {
    LocalDateTime start = date.atTime(8, 0);
    LocalDateTime end = date.atTime(17, 0);
    return new Event(subject, start, end);
  }

  /**
   * Creates an all-day event with description and location.
   *
   * @param subject     the event subject
   * @param date        the date of the event
   * @param description optional description
   * @param location    optional location
   * @param isPublic    true for public, false for private
   * @return a new all-day event
   */
  public static Event allDayEvent(String subject, LocalDate date, String description,
                                  String location, boolean isPublic) {
    LocalDateTime start = date.atTime(8, 0);
    LocalDateTime end = date.atTime(17, 0);
    return new Event(subject, start, end, description, location, isPublic);
  }

  /**
   * Gets the event subject.
   *
   * @return the subject
   */
  @Override
  public String getSubject() {
    return subject;
  }

  /**
   * Gets the event start date and time.
   *
   * @return the start date/time
   */
  @Override
  public LocalDateTime getStartDateTime() {
    return startDateTime;
  }

  /**
   * Gets the event end date and time.
   *
   * @return the end date/time
   */
  @Override
  public LocalDateTime getEndDateTime() {
    return endDateTime;
  }

  /**
   * Gets the event description.
   *
   * @return the description, or null if none
   */
  @Override
  public String getDescription() {
    return description;
  }

  /**
   * Gets the event location.
   *
   * @return the location, or null if none
   */
  @Override
  public String getLocation() {
    return location;
  }

  /**
   * Checks if the event is public.
   *
   * @return true if public, false if private
   */
  @Override
  public boolean isPublic() {
    return status == EventStatus.PUBLIC;
  }

  /**
   * Gets the event status.
   *
   * @return the event status
   */
  @Override
  public EventStatus getStatus() {
    return status;
  }

  /**
   * Checks if this is an all-day event.
   *
   * @return true if event runs from 8:00 AM to 5:00 PM on same day
   */
  @Override
  public boolean isAllDay() {
    LocalTime startTime = startDateTime.toLocalTime();
    LocalTime endTime = endDateTime.toLocalTime();
    return startTime.equals(LocalTime.of(8, 0))
        && endTime.equals(LocalTime.of(17, 0))
        && startDateTime.toLocalDate().equals(endDateTime.toLocalDate());
  }

  /**
   * Checks if the event spans multiple days.
   *
   * @return true if start and end dates differ
   */
  @Override
  public boolean isMultiDay() {
    return !startDateTime.toLocalDate().equals(endDateTime.toLocalDate());
  }

  /**
   * Checks if the event occurs on a specific date.
   *
   * @param date the date to check
   * @return true if the event occurs on this date
   */
  @Override
  public boolean occursOn(LocalDate date) {
    LocalDate startDate = startDateTime.toLocalDate();
    LocalDate endDate = endDateTime.toLocalDate();
    return !date.isBefore(startDate) && !date.isAfter(endDate);
  }

  /**
   * Checks if the event overlaps with a time range.
   *
   * @param start the range start time
   * @param end   the range end time
   * @return true if there is any overlap
   */
  @Override
  public boolean overlapsWith(LocalDateTime start, LocalDateTime end) {
    return startDateTime.isBefore(end) && endDateTime.isAfter(start);
  }

  /**
   * Checks if this event conflicts with another.
   *
   * @param other the event to compare with
   * @return true if both events have same subject, start, and end times
   */
  @Override
  public boolean conflictsWith(Ievent other) {
    if (other == null) {
      return false;
    }
    return subject.equals(other.getSubject()) && startDateTime.equals(other.getStartDateTime())
        && endDateTime.equals(other.getEndDateTime());
  }

  /**
   * Creates a copy with a new subject.
   *
   * @param newSubject the new subject
   * @return a new event with updated subject
   */
  @Override
  public Ievent withSubject(String newSubject) {
    return new Event(newSubject, startDateTime, endDateTime, description, location, status);
  }

  /**
   * Creates a copy with a new start time.
   *
   * @param newStart the new start date/time
   * @return a new event with updated start time
   */
  @Override
  public Ievent withStartDateTime(LocalDateTime newStart) {
    return new Event(subject, newStart, endDateTime, description, location, status);
  }

  /**
   * Creates a copy with a new end time.
   *
   * @param newEnd the new end date/time
   * @return a new event with updated end time
   */
  @Override
  public Ievent withEndDateTime(LocalDateTime newEnd) {
    return new Event(subject, startDateTime, newEnd, description, location, status);
  }

  /**
   * Creates a copy with a new description.
   *
   * @param newDescription the new description
   * @return a new event with updated description
   */
  @Override
  public Ievent withDescription(String newDescription) {
    return new Event(subject, startDateTime, endDateTime, newDescription, location, status);
  }

  /**
   * Creates a copy with a new location.
   *
   * @param newLocation the new location
   * @return a new event with updated location
   */
  @Override
  public Ievent withLocation(String newLocation) {
    return new Event(subject, startDateTime, endDateTime, description, newLocation, status);
  }

  /**
   * Creates a copy with a new status.
   *
   * @param newStatus the new status
   * @return a new event with updated status
   */
  @Override
  public Ievent withStatus(EventStatus newStatus) {
    return new Event(subject, startDateTime, endDateTime, description, location, newStatus);
  }

  /**
   * Formats the event as a readable string.
   *
   * @return formatted event details including subject, dates, times, and location
   */
  @Override
  public String format() {
    StringBuilder sb = new StringBuilder();

    if (isMultiDay()) {
      sb.append(subject).append(" starting on ")
          .append(startDateTime.toLocalDate().format(DATE_FORMATTER)).append(" at ")
          .append(startDateTime.toLocalTime().format(TIME_FORMATTER)).append(", ending on ")
          .append(endDateTime.toLocalDate().format(DATE_FORMATTER)).append(" at ")
          .append(endDateTime.toLocalTime().format(TIME_FORMATTER));
    } else if (isAllDay()) {
      sb.append(subject).append(" on ").append(startDateTime.toLocalDate().format(DATE_FORMATTER))
          .append(" (all day)");
    } else {
      sb.append(subject).append(" on ").append(startDateTime.toLocalDate().format(DATE_FORMATTER))
          .append(" from ").append(startDateTime.toLocalTime().format(TIME_FORMATTER))
          .append(" to ").append(endDateTime.toLocalTime().format(TIME_FORMATTER));
    }

    if (location != null && !location.trim().isEmpty()) {
      sb.append(" at ").append(location);
    }

    return sb.toString();
  }

  /**
   * Checks equality with another object.
   *
   * @param obj the object to compare
   * @return true if all fields are equal
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    Event event = (Event) obj;
    return Objects.equals(subject, event.subject)
        && Objects.equals(startDateTime, event.startDateTime)
        && Objects.equals(endDateTime, event.endDateTime)
        && Objects.equals(description, event.description)
        && Objects.equals(location, event.location) && status == event.status;
  }

  /**
   * Generates a hash code for the event.
   *
   * @return hash based on all fields
   */
  @Override
  public int hashCode() {
    return Objects.hash(subject, startDateTime, endDateTime, description, location, status);
  }

  /**
   * Returns a string representation of the event.
   *
   * @return the formatted event string
   */
  @Override
  public String toString() {
    return format();
  }
}