package calendar.model;

/**
 * Interface for calendar events.
 * An event has a required subject, start date, and start time.
 * Optional fields include end date, end time, description, location, and status.
 */
public interface IntEvent {
  /**
   * Gets the subject.
   *
   * @return the subject
   */
  String getSubject();

  /**
   * Gets the start date.
   *
   * @return the start date
   */
  Date getStartDate();

  /**
   * Gets the start time.
   *
   * @return the start time
   */
  Time getStartTime();

  /**
   * Gets the end date.
   *
   * @return the end date
   */
  Date getEndDate();

  /**
   * Gets the end time.
   *
   * @return the end time
   */
  Time getEndTime();

  /**
   * Gets the description.
   *
   * @return the description, or null if not set
   */
  String getDescription();

  /**
   * Gets the location.
   *
   * @return the location, or null if not set
   */
  Location getLocation();

  /**
   * Gets the status.
   *
   * @return the status, or null if not set
   */
  Status getStatus();

  /**
   * Checks if this is an all-day event (8am to 5pm on the same day).
   *
   * @return true if the event is 8am to 5pm on the same day, false otherwise
   */
  boolean isAllDayEvent();

  /**
   * Creates a new Event with the specified subject.
   *
   * @param newSubject the new subject
   * @return a new Event with the updated subject
   */
  IntEvent withSubject(String newSubject);

  /**
   * Creates a new Event with the specified start date and time.
   *
   * @param newStartDate the new start date
   * @param newStartTime the new start time
   * @return a new Event with the updated start date and time
   */
  IntEvent withStart(Date newStartDate, Time newStartTime);

  /**
   * Creates a new Event with the specified end date and time.
   *
   * @param newEndDate the new end date
   * @param newEndTime the new end time
   * @return a new Event with the updated end date and time
   */
  IntEvent withEnd(Date newEndDate, Time newEndTime);

  /**
   * Creates a new Event with the specified description.
   *
   * @param newDescription the new description
   * @return a new Event with the updated description
   */
  IntEvent withDescription(String newDescription);

  /**
   * Creates a new Event with the specified location.
   *
   * @param newLocation the new location
   * @return a new Event with the updated location
   */
  IntEvent withLocation(Location newLocation);

  /**
   * Creates a new Event with the specified status.
   *
   * @param newStatus the new status
   * @return a new Event with the updated status
   */
  IntEvent withStatus(Status newStatus);
}

