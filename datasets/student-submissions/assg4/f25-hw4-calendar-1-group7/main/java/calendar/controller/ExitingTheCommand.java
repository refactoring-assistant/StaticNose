package calendar.controller;

import calendar.model.Calendar;
import calendar.view.MyCalendarView;

/**
 * Command to exit the application.
 */
public class ExitingTheCommand implements Command {

  @Override
  public void execute(Calendar calendar, MyCalendarView view) {
    view.displayMessage("Goodbye!");
    // The controller will handle actually stopping the program
  }

  @Override
  public boolean validate() {
    return true;
  }

  public boolean isExit() {
    return true;
  }
}