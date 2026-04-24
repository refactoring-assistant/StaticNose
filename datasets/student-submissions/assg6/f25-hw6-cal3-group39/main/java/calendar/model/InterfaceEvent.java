package calendar.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Represents a read-only view of a calendar event.
 * This interface provides public getters for all essential event properties,
 * ensuring that consumers of event data (like Views or Exporters) can read
 * event details without being able to modify the event's state.
 */
public interface InterfaceEvent {

  /**
   * Gets the subject or title of the event.
   *
   * @return the event subject
   */
  String getSubject();

  /**
   * Gets the start date of the event.
   *
   * @return the event start date
   */
  LocalDate getStartDate();

  /**
   * Gets the end date of the event.
   * For single-day events, this will be the same as the start date.
   *
   * @return the event end date
   */
  LocalDate getEndDate();

  /**
   * Gets the start time of the event.
   *
   * @return the event start time
   */
  LocalTime getStartTime();

  /**
   * Gets the end time of the event.
   *
   * @return the event end time
   */
  LocalTime getEndTime();

  /**
   * Gets the description for the event.
   *
   * @return the event description, or null/empty if not set
   */
  String getDescription();

  /**
   * Gets the location of the event (e.g., "physical" or "online").
   *
   * @return the event location, or null/empty if not set
   */
  String getLocation();

  /**
   * Gets the status of the event (e.g., "public" or "private").
   *
   * @return the event status, or null/empty if not set
   */
  String getStatus();

  /**
   * Gets the series ID for the event.
   * This ID is used to link recurring events together.
   *
   * @return the series ID, or -1 for a single, non-recurring event
   */
  int getSeriesId();
}