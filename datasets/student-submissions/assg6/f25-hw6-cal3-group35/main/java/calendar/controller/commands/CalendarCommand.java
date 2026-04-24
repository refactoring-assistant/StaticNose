package calendar.controller.commands;

/**
 * Represents an executable calendar command.
 */
public interface CalendarCommand {
  /**
   * Executes the command.
   *
   * @return a result string suitable for console output
   */
  String execute();
}
