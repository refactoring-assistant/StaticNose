package calendar.commands;

import calendar.model.Model;
import calendar.view.View;

/**
 * A command class that handles checking the availability status of the user at a specific datetime.
 * Implements the {@link Command} interface.
 * When executed, it queries the model to determine if the user is busy at the given date and time
 * and displays the result via the view.
 */
public class StatusCommand implements Command {

  /**
   * Executes the status command.
   * Parses the user input to extract the date and time, checks the user's availability
   * using the model, and displays a message via the view indicating whether the user
   * is busy or available. If the command format is invalid or an exception occurs,
   * an error message is displayed.
   */
  @Override
  public void execute(String calName, String userInput, Model model, View view) {

    String[] userCommand = userInput.split(" ", 4); // "show status on <dateTime>"
    if (userCommand.length != 4) {
      view.showError("Invalid command format: " + userInput);
      return;
    }
    String dateTime = userCommand[3];
    boolean busy = model.isBusy(calName, userInput);

    if (busy) {
      view.showMessage("Result of " + userInput + ": User is busy on " + dateTime);
    } else {
      view.showMessage("Result of " + userInput + ": User is available on " + dateTime);
    }
  }
}
