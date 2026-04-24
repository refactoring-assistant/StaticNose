package calendar.command;

import calendar.exception.CalendarException;

/**
 * Every action a user can do gets wrapped in a Command object.
 * This class contains a package of all the actions (create event, edit event, delete event)
 * Enables undo/redo functionality in future iterations and provides
 * a clean separation between command parsing and command execution.
 * This interface follows the Command design pattern, which encapsulates
 * a request as an object, allowing for parameterization of clients with
 * different requests, queuing of requests, and logging of operations.
 * Common Commands:
 *    CreateEventCommand - adds new events
 *    EditEventCommand - changes existing ones
 *    QueryEventCommand - finds events
 *    ExportCommand - saves to file
 */
public interface InCommand {

  /**
   * Executes the command.
   * This method contains the actual logic for the command operation.
   * It should be idempotent where possible and handle errors gracefully.
   *
   * @throws CalendarException if command execution fails due to business logic errors
   */
  void execute() throws CalendarException;

  /**
   * Gets a description of what this command does.
   * Useful for debugging and potential undo/redo UI.
   *
   * @return the command description (e.g., "Create event: Team Meeting")
   */
  String getDescription();
}
