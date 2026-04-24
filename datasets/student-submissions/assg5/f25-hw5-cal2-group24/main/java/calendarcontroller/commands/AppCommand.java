package calendarcontroller.commands;

import calendarcontroller.TextCalendarController;
import calendarview.CalendarView;
import multicalendarmodel.MultiCalendarModel;

/**
 * An interface for "Application-Level" commands to be executed.
 * This commands will operate on basis of {@link MultiCalendarModel}
 * and will not require active calendar to be selected.
 */
public interface AppCommand {

  /**
   * Attempts to execute the command based on the provided input line.
   *
   * <p>If the {@code inputLine} matches the command's expected format (e.g., its regex),
   * this method will parse the line, call the appropriate methods on the
   * {@link MultiCalendarModel}, and display results using the {@link CalendarView}.</p>
   *
   * @param inputLine  The full command string entered by the user.
   * @param appModel   {@link MultiCalendarModel} model object.
   * @param view       The view to display results or errors.
   * @param controller The controller to control the flow.
   * @return           {@code true} if this command successfully matched and processed
   *                   the input line, {@code false} otherwise.
   */
  boolean execute(String inputLine, MultiCalendarModel appModel,
                  CalendarView view, TextCalendarController controller);

}
