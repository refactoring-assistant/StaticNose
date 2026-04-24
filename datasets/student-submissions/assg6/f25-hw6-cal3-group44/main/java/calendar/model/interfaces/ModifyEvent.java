package calendar.model.interfaces;

/**
 * Represents an operation that modifies a specific attribute of an Event.
 * Each event edit operation implements this ModifyEvent.
 */
public interface ModifyEvent {

  /**
   * Edits a given event with the given new value.
   *
   * @param event    event to be edited
   * @param newValue the new value of the respective field
   */
  EventReadOnly edit(CalendarEditable calendar, EventReadOnly event, String newValue);
}
