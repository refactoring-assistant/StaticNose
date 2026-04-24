package calendar.controller;

/**
 * Interface for all calendar commands.
 */
public interface Command {
  /**
   * Executes the command.
   *
   * @param controller the calendar controller
   * @return the result message
   */
  String execute(CalendarController controller);
}