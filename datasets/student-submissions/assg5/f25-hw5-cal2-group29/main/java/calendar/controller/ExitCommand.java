package calendar.controller;

import calendar.model.Calendar;
import calendar.model.CalendarApplication;
import calendar.view.CalendarView;

/**
 * Command to handle the 'exit' command.
 * Signals the main loop to terminate via a special exception.
 */
public class ExitCommand implements Command {

  /**
   * Constructs an exit command.
   */
  public ExitCommand() {
  }

  @Override
  public void execute(CalendarApplication model, CalendarView view) {
    view.displaySuccess("Exiting calendar application. Goodbye!");
    throw new ExitApplicationException();
  }

  /**
   * Special exception to signal the application should exit.
   */
  public static class ExitApplicationException extends RuntimeException {
    /**
     * Constructs a new {@code ExitApplicationException} with a default message
     * indicating that the user has requested to exit the application.
     */
    public ExitApplicationException() {
      super("Application exit requested");
    }
  }
}