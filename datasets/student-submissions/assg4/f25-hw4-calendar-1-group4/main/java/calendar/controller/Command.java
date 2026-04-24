package calendar.controller;

/**
 * Represents a single executable command within the calendar application.
 * Each command corresponds to a specific user action such as creating,
 * editing, printing, or exporting events.
 */
interface Command {

  /**
   * Executes the logic associated with this command.
   *
   * @return the result message or output produced after execution
   */
  String execute();
}
