package calendar.commands;

import calendar.model.Model;
import calendar.view.View;

/**
 * This is the showCalCommand class that is called when a command to show the current calendar
 * context is used in the controller input.
 */
public class ShowCalCommand implements Command {
  @Override
  public void execute(String calName, String userInput, Model model, View view) {
    view.showMessage("Using Calendar: " + calName);
  }
}
