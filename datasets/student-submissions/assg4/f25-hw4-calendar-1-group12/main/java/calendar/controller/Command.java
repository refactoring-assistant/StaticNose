package calendar.controller;

import calendar.model.CalendarModel;
import calendar.view.CalendarView;
import java.io.IOException;

/**
 * Represents an executable command in the calendar application.
 */
public interface Command {
  /**
   * Execute this command on the given model and view.
   *
   * @param model the calendar model
   * @param view the calendar view
   * @throws IOException if there's an error with I/O
   * @throws IllegalArgumentException if the command cannot be executed
   */
  void execute(CalendarModel model, CalendarView view) throws IOException;
}