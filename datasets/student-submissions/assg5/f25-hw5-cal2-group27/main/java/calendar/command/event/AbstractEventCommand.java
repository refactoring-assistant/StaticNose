package calendar.command.event;

import calendar.command.Command;
import calendar.model.CalendarManager;
import calendar.model.MyCalendar;
import calendar.view.CalendarTextView;
import java.util.Optional;

/**
 * Base class for commands that operate on the currently selected calendar.
 */
public abstract class AbstractEventCommand implements Command {

  /**
   * Executes the command if a calendar is selected, otherwise prints an error.
   *
   * @param manager the calendar manager that provides calendar access
   * @param view    the view used to output messages to the user
   */
  @Override
  public void execute(CalendarManager manager, CalendarTextView view) {
    Optional<MyCalendar> maybeCalendar = manager.getActiveCalendar();
    if (maybeCalendar.isEmpty()) {
      view.printMessage(
              "Error: No calendar is currently in use. "
                      + "Use the 'use calendar' command to select one.");
      return;
    }
    executeWithCalendar(manager, maybeCalendar.get(), view);
  }

  /**
   * Executes the command using the active calendar.
   *
   * @param manager  the calendar manager
   * @param calendar the active calendar
   * @param view     the view for output
   */
  protected abstract void executeWithCalendar(
          CalendarManager manager, MyCalendar calendar, CalendarTextView view);
}