package calendar.controller;

import calendar.model.CalendarModel;
import calendar.view.CalendarView;

/**
 * Represents a command that can be executed on the calendar.
 * Follows the Command design pattern.
 */
public interface Command {

  /**
   * Executes the command on the given calendar and view.
   *
   * @param calendar the calendar model
   * @param view the view for output
   * @throws IllegalArgumentException if command execution fails
   */
  void execute(CalendarModel calendar, CalendarView view);
}