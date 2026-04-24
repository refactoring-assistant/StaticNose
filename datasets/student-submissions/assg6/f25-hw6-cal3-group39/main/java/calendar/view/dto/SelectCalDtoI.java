package calendar.view.dto;

/**
 * Represents a read-only contract for a request to select a specific calendar.
 * Implementations of this interface provide the name of the calendar
 * that the user wishes to select or operate on.
 */
public interface SelectCalDtoI {

  /**
   * Returns the name of the selected calendar.
   *
   * @return the calendar name as a non-null string
   */
  String getCalendarName();
}
