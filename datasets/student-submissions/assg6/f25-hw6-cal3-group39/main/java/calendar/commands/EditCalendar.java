package calendar.commands;

import calendar.model.Model;
import calendar.view.View;

/**
 * This is the command design edit class that handles calls for editing an event.
 */
public class EditCalendar implements Command {

  @Override
  public void execute(String calName, String userInput, Model model, View view) {
    try {
      if (!userInput.contains("--name") || !userInput.contains("--property")) {
        throw new IllegalArgumentException("Wrong input");
      }
      String[] command = userInput.split(" ");
      if (command.length != 7) {
        throw new IllegalArgumentException("Incorrect input format");
      }
      String name = command[3];
      String property = command[5];
      String newPropValue = command[6];
      model.editCalendar(name, property, newPropValue);
      view.showMessage("calendar edited successfully for command: " + userInput);
    } catch (Exception e) {
      view.showError("Error editing Calendar: " + userInput + ". Error: " + e.getMessage());
    }
  }
}
