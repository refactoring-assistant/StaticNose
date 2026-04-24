package calendar.controller.commands;

import calendar.model.CalendarSystem;

/**
 * Represents a command that operates on the calendar system (not individual calendars).
 */
public interface SystemCommand {
  /**
   * Executes this command on the calendar system.
   *
   * @param system the calendar system to execute on
   * @return a message describing the result
   */
  String execute(CalendarSystem system);
}