package calendar.command;

/**
 * Command design pattern interface.
 * Each command encapsulates a request as an object.
 */
public interface CommandInterface {

  /**
   * Execute this command.
   *
   * @return result message
   */
  String execute();

  /**
   * Get command description for help.
   */
  String getDescription();
}