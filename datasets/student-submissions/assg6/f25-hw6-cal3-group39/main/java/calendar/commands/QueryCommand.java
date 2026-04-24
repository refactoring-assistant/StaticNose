package calendar.commands;

import static calendar.controller.ControllerImpl.returnDate;
import static calendar.controller.ControllerImpl.returnTime;

import calendar.model.Model;
import calendar.view.View;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * A command class that handles querying events from the calendar.
 * Implements the {@link Command} interface.
 * When executed, it queries events from the model based on the user input
 * and displays them via the view.
 */
public class QueryCommand implements Command {

  /**
   * Executes the query command.
   * Queries events from the provided model using the given user input,
   * displays the retrieved events using the view, and shows a success message.
   * If an exception occurs during querying or displaying, an error message is shown.
   *
   * @param userInput the command string input by the user
   * @param model     the model representing the calendar and event data
   * @param view      the view used to display events and messages to the user
   */
  @Override
  public void execute(String calName, String userInput, Model model, View view) {
    try {
      boolean export = false;
      LocalDate startDate = null;
      LocalTime startTime = null;
      LocalDate endDate = null;
      LocalTime endTime = null;
      String[] command = userInput.replace("::", ":").split(" ");

      if (userInput.contains("from")) {
        startDate = returnDate(command[3]);
        startTime = returnTime(command[3]);
        endDate = returnDate(command[5]);
        endTime = returnTime(command[5]);
      } else {
        startDate = LocalDate.parse(command[command.length - 1]);
      }

      view.displayEvents(
          model.queryEvents(calName, startDate, startTime, endDate, endTime, export));
      view.showMessage("Event/s queried successfully for command: " + userInput);
    } catch (Exception e) {
      view.showError("Error querying event: " + userInput + ". Error: " + e.getMessage());
    }
  }
}
