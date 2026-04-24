package calendar.commands;

import calendar.model.Model;
import calendar.view.View;
import java.util.Arrays;

/**
 * This is the createCalendar class in the command design pattern that is called when a command
 * for creating a calendar is inputted in the controller.
 */
public class CreateCalendar implements Command {

  @Override
  public void execute(String calName, String userInput, Model model, View view) {
    try {
      if (!userInput.contains("--name") || !userInput.contains("--timezone")) {
        throw new IllegalArgumentException("name or timezone not present ");
      }

      String[] command = userInput.trim().split("\\s+");
      int nameIndex = Arrays.asList(command).indexOf("--name");
      int tzIndex = Arrays.asList(command).indexOf("--timezone");

      String name = (nameIndex + 1 < command.length) ? command[nameIndex + 1] : "";
      String timezone = (tzIndex + 1 < command.length) ? command[tzIndex + 1] : "";

      if (name.equals("--timezone") || name.equals("") || timezone.equals("")) {
        throw new IllegalArgumentException("Invalid name");
      }
      model.createCalendar(name, timezone);
      view.showMessage("calendar created successfully for command: " + userInput);
    } catch (Exception e) {
      view.showError("Error creating Calendar: " + userInput + ". Error: " + e.getMessage());
    }
  }
}
