package calendar.view.dto;

/**
 * Represents a read-only contract for a request to create a new event
 * within the calendar system. Implementations of this interface
 * encapsulate all user-provided event details, including timing,
 * metadata, and recurrence rules.
 */
public interface CreateEventDtoI {

  /**
   * Returns the name/title of the event being created.
   *
   * @return the event name as a non-null string
   */
  String getEventName();

  /**
   * Returns the start date of the event in raw string format
   * (e.g., {@code "2025-11-04"}).
   *
   * @return the start date string
   */
  String getStartDate();

  /**
   * Returns the start time of the event in raw string format
   * (e.g., {@code "09:00"}), or {@code null} for all-day events.
   *
   * @return the start time string, or {@code null} if not applicable
   */
  String getStartTime();

  /**
   * Returns the end date of the event in raw string format.
   *
   * @return the end date string
   */
  String getEndDate();

  /**
   * Returns the end time of the event in raw string format.
   *
   * @return the end time string, or {@code null} if not applicable
   */
  String getEndTime();

  /**
   * Returns the location of the event as provided by the user.
   *
   * @return the event location string, or {@code null} if none provided
   */
  String getLocation();

  /**
   * Returns the user-provided description or notes for the event.
   *
   * @return the event description string, or {@code null} if none provided
   */
  String getDescription();

  /**
   * Returns the status of the event (e.g., {@code "CONFIRMED"},
   * {@code "TENTATIVE"}), as provided by the user interface.
   *
   * @return the event status string
   */
  String getStatus();

  /**
   * Indicates whether the event is recurring.
   *
   * @return {@code true} if the event repeats, {@code false} otherwise
   */
  boolean isRecurring();

  /**
   * Returns the recurrence pattern expressed as a string
   * (e.g., {@code "MO,WE,FR"}).
   *
   * @return the recurrence day pattern, or {@code null} if not recurring
   */
  String getRecurrenceDays();

  /**
   * Returns the recurrence end condition as a string.
   * Can be a date (e.g., {@code "2025-12-01"}) or a count
   * (e.g., {@code "5"} repetitions).
   *
   * @return the recurrence end value, or {@code null} if not recurring
   */
  String getRecurrenceEnd();

  /**
   * Indicates whether the event is an all-day event.
   *
   * @return {@code true} if the event spans the whole day,
   *         {@code false} otherwise
   */
  boolean isAllDay();
}
