package calendar.manager;

import calendar.model.CalendarModel;
import java.util.List;

/**
 * Represents a manager that can handle multiple calendars.
 * Allows creating, selecting, renaming, and deleting calendars.
 */
public interface CalendarManager {

  /**
   * Creates a new calendar with the given unique name.
   *
   * @param name  the name of the calendar
   * @param model an instance of CalendarModel to associate with this calendar
   */
  void createCalendar(String name, CalendarModel model);

  /**
   * Selects a calendar so commands act on that calendar.
   *
   * @param name the name of the calendar to select
   */
  void selectCalendar(String name);

  /**
   * Renames an existing calendar to a new name.
   *
   * @param oldName the current name
   * @param newName the new name (must be unique)
   */
  void renameCalendar(String oldName, String newName);

  /**
   * Deletes a calendar by name.
   *
   * @param name the name of the calendar
   */
  void deleteCalendar(String name);

  /**
   * Returns the currently selected calendar.
   *
   * @return the currently selected CalendarModel
   */
  CalendarModel getSelectedCalendar();

  /**
   * Returns the names of all calendars currently registered with the manager.
   *
   * @return a list of all calendar names managed by this class
   */
  List<String> listCalendars();

  /**
   * Returns the calendar with the given name.
   *
   * @param name the name of the calendar
   * @return the CalendarModel instance
   * @throws IllegalArgumentException if no such calendar exists
   */
  CalendarModel getCalendar(String name);

}
