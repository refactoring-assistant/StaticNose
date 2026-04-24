package calendar.view.dto;

/**
 * Represents a read-only contract for a request to edit an existing
 * calendar's properties. Implementations of this interface encapsulate
 * the new name and/or new timezone for the calendar being modified.
 */
public interface EditCalDtoI {

  /**
   * Returns the new name assigned to the calendar.
   * If unchanged, this may be {@code null}.
   *
   * @return the updated calendar name, or {@code null} if no change was provided
   */
  String getNewName();

  /**
   * Returns the new timezone identifier for the calendar
   * (e.g., {@code "America/New_York"}).
   * If unchanged, this may be {@code null}.
   *
   * @return the updated timezone ID, or {@code null} if no change was provided
   */
  String getNewTimezone();
}
