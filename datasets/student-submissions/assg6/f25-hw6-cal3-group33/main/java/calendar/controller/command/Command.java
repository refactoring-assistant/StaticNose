package calendar.controller.command;

import calendar.model.manager.CalendarManager;
import calendar.view.CalendarView;

/**
 * Represents an executable calendar command.
 *
 * <p>All calendar commands implement this interface to provide uniform execution.
 * Commands encapsulate a single user action (create event, edit calendar, etc.)
 * along with all necessary parameters.
 *
 * <p>This design follows the Command Pattern, which:
 * - Decouples command parsing from execution
 * - Makes each command independently testable
 * - Allows easy addition of new commands
 * - Enables future features like undo/redo, command history, etc.
 *
 * <p>Commands are stateless except for their parameters, which are set
 * during construction and remain immutable.
 */
public interface Command {

  /**
   * Executes this command.
   *
   * <p>The command uses the provided calendar manager to access and modify
   * calendars and events, and the view to display results or errors to the user.
   *
   * <p>All exceptions are propagated to the caller (typically the controller)
   * for centralized error handling.
   *
   * @param manager the calendar manager for accessing and modifying calendars
   * @param view the view for displaying results and errors to the user
   * @throws Exception if command execution fails for any reason
   *         (invalid parameters, calendar not found, event conflicts, etc.)
   */
  void execute(CalendarManager manager, CalendarView view) throws Exception;
}