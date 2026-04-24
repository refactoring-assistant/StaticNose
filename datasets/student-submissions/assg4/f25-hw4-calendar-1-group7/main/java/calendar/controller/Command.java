package calendar.controller;

import calendar.model.Calendar;
import calendar.view.MyCalendarView;

/**
 * Represents a command that can be executed on the calendar.
 * Follows the Command pattern for extensibility.
 */
public interface Command {

  /**
   * Executes the command.
   *
   * @param calendar the calendar model
   * @param view the view for output
   */
  void execute(Calendar calendar, MyCalendarView view);

  /**
   * Validates the command before execution.
   *
   * @return true if valid, false otherwise
   */
  boolean validate();
}