package calendar.controller;

import calendar.model.CalendarApplication;
import calendar.view.CalendarView;

/**
 * Command Interface, receives the top-level CalendarApplication model.
 */
public interface Command {

  /**
   * Executes the specific command.
   *
   * @param model The main CalendarApplication instance.
   * @param view  The main CalendarView instance.
   * @throws Exception if an unrecoverable error occurs.
   */
  void execute(CalendarApplication model, CalendarView view) throws Exception;
}