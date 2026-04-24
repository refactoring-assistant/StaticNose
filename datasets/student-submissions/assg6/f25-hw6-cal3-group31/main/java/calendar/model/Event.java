package calendar.model;

import java.util.Objects;

/**
 * Represents an immutable calendar event.
 * An event has a required subject, start date, and start time.
 * Optional fields include end date, end time, description, location, and status.
 * If no end date/time is specified, the event is an all-day event (8am to 5pm).
 */
public class Event implements IntEvent {
  private final String subject;
  private final Date startDate;
  private final Time startTime;
  private final Date endDate;
  private final Time endTime;
  private final String description;
  private final Location location;
  private final Status status;

  /**
   * Constructs an Event with all fields.
   *
   * @param subject     the subject of the event (required)
   * @param startDate   the start date (required)
   * @param startTime   the start time (required)
   * @param endDate     the end date (optional, defaults to startDate if null)
   * @param endTime     the end time (optional, defaults to 5pm if null)
   * @param description the description (can be null)
   * @param location    the location (can be null)
   * @param status      the status (can be null)
   */
  public Event(String subject, Date startDate, Time startTime, Date endDate, Time endTime,
               String description, Location location, Status status) {
    if (subject == null || subject.isEmpty()) {
      throw new IllegalArgumentException("Subject cannot be null or empty");
    }
    if (startDate == null || startTime == null) {
      throw new IllegalArgumentException("Start date and time are required");
    }

    this.subject = subject;
    this.startDate = startDate;
    this.startTime = startTime;

    // If endDate/endTime are null, treat as all-day event (8am to 5pm)
    if (endDate == null && endTime == null) {
      this.endDate = startDate;
      this.endTime = new Time(17, 0); // 5pm
    } else if (endDate == null) {
      this.endDate = startDate;
      this.endTime = endTime;
    } else if (endTime == null) {
      this.endDate = endDate;
      this.endTime = new Time(17, 0); // 5pm
    } else {
      this.endDate = endDate;
      this.endTime = endTime;
    }

    // Validate that end date/time is not before start date/time
    if (isEndBeforeStart(this.startDate, this.startTime, this.endDate, this.endTime)) {
      throw new IllegalArgumentException(
          "End date and time cannot be before start date and time");
    }

    this.description = description;
    this.location = location;
    this.status = status;
  }

  /**
   * Constructs an Event with description only.
   *
   * @param subject     the subject of the event (required)
   * @param startDate   the start date (required)
   * @param startTime   the start time (required)
   * @param endDate     the end date (optional, defaults to startDate if null)
   * @param endTime     the end time (optional, defaults to 5pm if null)
   * @param description the description
   */
  public Event(String subject, Date startDate, Time startTime, Date endDate, Time endTime,
               String description) {
    this(subject, startDate, startTime, endDate, endTime, description, null, null);
  }

  /**
   * Constructs an Event with location only.
   *
   * @param subject   the subject of the event (required)
   * @param startDate the start date (required)
   * @param startTime the start time (required)
   * @param endDate   the end date (optional, defaults to startDate if null)
   * @param endTime   the end time (optional, defaults to 5pm if null)
   * @param location  the location
   */
  public Event(String subject, Date startDate, Time startTime, Date endDate, Time endTime,
               Location location) {
    this(subject, startDate, startTime, endDate, endTime, null, location, null);
  }

  /**
   * Constructs an Event with status only.
   *
   * @param subject   the subject of the event (required)
   * @param startDate the start date (required)
   * @param startTime the start time (required)
   * @param endDate   the end date (optional, defaults to startDate if null)
   * @param endTime   the end time (optional, defaults to 5pm if null)
   * @param status    the status
   */
  public Event(String subject, Date startDate, Time startTime, Date endDate, Time endTime,
               Status status) {
    this(subject, startDate, startTime, endDate, endTime, null, null, status);
  }

  /**
   * Constructs an Event with required fields only.
   *
   * @param subject   the subject of the event (required)
   * @param startDate the start date (required)
   * @param startTime the start time (required)
   * @param endDate   the end date (optional, defaults to startDate if null)
   * @param endTime   the end time (optional, defaults to 5pm if null)
   */
  public Event(String subject, Date startDate, Time startTime, Date endDate, Time endTime) {
    this(subject, startDate, startTime, endDate, endTime, null, null, null);
  }

  /**
   * Gets the subject.
   *
   * @return the subject
   */
  public String getSubject() {
    return subject;
  }

  /**
   * Gets the start date.
   *
   * @return the start date
   */
  public Date getStartDate() {
    return startDate;
  }

  /**
   * Gets the start time.
   *
   * @return the start time
   */
  public Time getStartTime() {
    return startTime;
  }

  /**
   * Gets the end date.
   *
   * @return the end date
   */
  public Date getEndDate() {
    return endDate;
  }

  /**
   * Gets the end time.
   *
   * @return the end time
   */
  public Time getEndTime() {
    return endTime;
  }

  /**
   * Gets the description.
   *
   * @return the description, or null if not set
   */
  public String getDescription() {
    return description;
  }

  /**
   * Gets the location.
   *
   * @return the location, or null if not set
   */
  public Location getLocation() {
    return location;
  }

  /**
   * Gets the status.
   *
   * @return the status, or null if not set
   */
  public Status getStatus() {
    return status;
  }

  /**
   * Checks if this is an all-day event (8am to 5pm on the same day).
   *
   * @return true if the event is 8am to 5pm on the same day, false otherwise
   */
  public boolean isAllDayEvent() {
    return startDate.equals(endDate)
        && startTime.equals(new Time(8, 0))
        && endTime.equals(new Time(17, 0));
  }

  /**
   * Creates a new Event with the specified subject.
   *
   * @param newSubject the new subject
   * @return a new Event with the updated subject
   */
  @Override
  public IntEvent withSubject(String newSubject) {
    return new Event(newSubject, startDate, startTime, endDate, endTime,
        description, location, status);
  }

  /**
   * Creates a new Event with the specified start date and time.
   *
   * @param newStartDate the new start date
   * @param newStartTime the new start time
   * @return a new Event with the updated start date and time
   */
  @Override
  public IntEvent withStart(Date newStartDate, Time newStartTime) {
    return new Event(subject, newStartDate, newStartTime, endDate, endTime,
        description, location, status);
  }

  /**
   * Creates a new Event with the specified end date and time.
   *
   * @param newEndDate the new end date
   * @param newEndTime the new end time
   * @return a new Event with the updated end date and time
   */
  @Override
  public IntEvent withEnd(Date newEndDate, Time newEndTime) {
    return new Event(subject, startDate, startTime, newEndDate, newEndTime,
        description, location, status);
  }

  /**
   * Creates a new Event with the specified description.
   *
   * @param newDescription the new description
   * @return a new Event with the updated description
   */
  @Override
  public IntEvent withDescription(String newDescription) {
    return new Event(subject, startDate, startTime, endDate, endTime,
        newDescription, location, status);
  }

  /**
   * Creates a new Event with the specified location.
   *
   * @param newLocation the new location
   * @return a new Event with the updated location
   */
  @Override
  public IntEvent withLocation(Location newLocation) {
    return new Event(subject, startDate, startTime, endDate, endTime,
        description, newLocation, status);
  }

  /**
   * Creates a new Event with the specified status.
   *
   * @param newStatus the new status
   * @return a new Event with the updated status
   */
  @Override
  public IntEvent withStatus(Status newStatus) {
    return new Event(subject, startDate, startTime, endDate, endTime,
        description, location, newStatus);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Event event = (Event) o;
    return Objects.equals(subject, event.subject)
        && Objects.equals(startDate, event.startDate)
        && Objects.equals(startTime, event.startTime)
        && Objects.equals(endDate, event.endDate)
        && Objects.equals(endTime, event.endTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subject, startDate, startTime, endDate, endTime);
  }

  @Override
  public String toString() {
    return String.format("%s starting on %s at %s, ending on %s at %s",
        subject, startDate, startTime, endDate, endTime);
  }

  /**
   * Checks if the end date/time is before the start date/time.
   *
   * @param startDate the start date
   * @param startTime the start time
   * @param endDate   the end date
   * @param endTime   the end time
   * @return true if end is before start, false otherwise
   */
  private static boolean isEndBeforeStart(Date startDate, Time startTime,
                                          Date endDate, Time endTime) {
    int dateComparison = endDate.compareTo(startDate);

    // If end date is before start date, it's invalid
    if (dateComparison < 0) {
      return true;
    }

    // If same date, check if end time is before start time
    if (dateComparison == 0) {
      return endTime.compareTo(startTime) < 0;
    }

    // End date is after start date, so it's valid
    return false;
  }
}

