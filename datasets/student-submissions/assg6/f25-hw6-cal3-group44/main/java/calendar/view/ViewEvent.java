package calendar.view;

import java.time.LocalDateTime;

/**
 * View Event transfer object used for event information to be displayed in the view.
 */
public interface ViewEvent {

  /**
   * Returns the event subject.
   */
  String getSubject();

  /**
   * Returns the start date and time.
   */
  LocalDateTime getStartDateTime();

  /**
   * Returns the end date and time.
   */
  LocalDateTime getEndDateTime();

  /**
   * Returns the event description.
   */
  String getDescription();

  /**
   * Returns the event location.
   */
  String getLocation();

  /**
   * Returns the event status.
   */
  String getStatus();

  /**
   * Indicates whether the event is all day.
   */
  boolean isAllDay();

  /**
   * Indicates whether the event is part of a series.
   */
  boolean isSeries();
}
