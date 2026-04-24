package calendar.controller.text.commandfactory;

/**
 * Describes the execute method which is implemented by all the command factory classes.
 */
public interface CommandInterface {

  /**
   * Delegates the command work to the model by sending the parameters and getting back information
   * if it has passed or not. Then the view is called to display the result of the command.
   */
  void execute();
}