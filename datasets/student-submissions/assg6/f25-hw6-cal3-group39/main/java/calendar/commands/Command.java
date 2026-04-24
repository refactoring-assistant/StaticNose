package calendar.commands;

import calendar.model.Model;
import calendar.view.View;

/**
 * Represents a command that can be executed by the Calendar application.
 * Each command performs an action using the provided model and updates the view accordingly.
 */
public interface Command {

  /**
   * Executes the command with the given user input, using the provided model and view.
   *
   * @param userInput the raw command string entered by the user
   * @param model     the model containing the calendar data and business logic
   * @param view      the view used to display messages and events to the user
   */
  void execute(String calName, String userInput, Model model, View view);
}
