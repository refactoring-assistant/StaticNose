package calendar.view.dto;

/**
 * Represents a read-only contract for a request to create a new calendar
 * within the calendar system. Implementations of this interface provide
 * the calendar name and its associated timezone.
 */
public interface CreateCalDtoI {

  /**
   * Returns the name of the calendar to be created.
   *
   * @return the calendar name as a non-null string
   */
  String getCalendarName();

  /**
   * Returns the timezone identifier (e.g., {@code "America/New_York"}) for the
   * calendar being created.
   *
   * @return the timezone ID as a non-null string
   */
  String getTimezone();
}
