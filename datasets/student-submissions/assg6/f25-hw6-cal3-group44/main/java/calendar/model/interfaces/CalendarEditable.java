package calendar.model.interfaces;

import java.util.List;

/**
 * Represents a modifiable version of the calendar model.
 * Provides methods for adding, editing, removing.
 */
public interface CalendarEditable extends CalendarReadOnly {

  /**
   * Adds a new event to the calendar.
   *
   * @param event the event to add
   * @throws IllegalArgumentException if the event already exists
   */
  EventReadOnly addEvent(EventReadOnly event);

  /**
   * Edits one or more existing events by modifying a specified property.
   *
   * @param events   the list of events to modify
   * @param property the property to edit like subject
   * @param newValue the new value to assign to the property
   * @throws IllegalArgumentException      if no events match the given criteria
   * @throws UnsupportedOperationException if the property name is invalid
   */
  List<EventReadOnly> editEvent(List<EventReadOnly> events, String property, String newValue);

  /**
   * Removes an event from the calendar.
   *
   * @param event the event to remove
   */
  void removeEvent(EventReadOnly event);
}
