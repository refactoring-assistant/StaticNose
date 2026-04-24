package calendar.controller.commands;

import calendar.model.Calendar;

/**
 * Represents a single command that can be executed on the calendar model.
 */
public interface CalendarCommand {
  /**
   * Executes this command on the given calendar model.
   *
   * @param model the calendar model
   * @return a message describing the result
   */
  String execute(Calendar model);
}