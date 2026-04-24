package calendarcontroller.commands;

import calendarmodel.CalendarModel;
import calendarview.CalendarView;

/**
 * An interface for a single command that can be executed by the calendar controller.
 *
 * <p>Implementations of this interface represent one specific user action,
 * such as "create event" or "print events".</p>
 */
public interface CalendarCommand {

  /**
   * Attempts to execute the command based on the provided input line.
   *
   * <p>If the {@code inputLine} matches the command's expected format (e.g., its regex),
   * this method will parse the line, call the appropriate methods on the
   * {@link CalendarModel}, and display results using the {@link CalendarView}.</p>
   *
   * @param inputLine The full command string entered by the user.
   * @param model     The calendar model to operate on.
   * @param view      The view to display results or errors.
   * @return {@code true} if this command successfully matched and processed
   *                      the input line, {@code false} otherwise.
   */
  boolean execute(String inputLine, CalendarModel model, CalendarView view);
}
