package calendar.commands;

import static calendar.controller.ControllerImpl.returnDate;
import static calendar.controller.ControllerImpl.returnTime;

import calendar.controller.EditSpec;
import calendar.model.Model;
import calendar.view.View;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

/**
 * Implements the calendar.Commands.Command interface to handle editing of calendar events.
 * Executes an edit event command and notifies the view of success or failure.
 */
public class EditCommand implements Command {

  /**
   * Executes the edit event command using the given user input.
   *
   * @param userInput the full command input from the user
   * @param model     the model containing calendar data and logic
   * @param view      the view used to display messages or errors
   */
  @Override
  public void execute(String calName, String userInput, Model model, View view) {
    try {
      String[] userCommand = userInput.replace("::", ":").split(" ");

      String type = userCommand[1];
      String property = userCommand[2];
      String subject = userCommand[3];
      LocalDate startDate = returnDate(userCommand[5]);
      LocalTime startTime = returnTime(userCommand[5]);
      String newPropValue = userCommand[userCommand.length - 1];
      LocalDate endDate = null;
      LocalTime endTime = null;
      EditSpec.EditSpecBuilder editSpec = new EditSpec.EditSpecBuilder(type, property,
          subject, startDate, startTime, newPropValue);

      if (type.equals("event")) {
        editSpec.endDate(returnDate(userCommand[7]));
        editSpec.endTime(returnTime(userCommand[7]));
        endDate = returnDate(userCommand[7]);
        endTime = returnTime(userCommand[7]);
      }

      long daysDiff = -1;
      long minutesDiff = -1;

      if (property.equals("start")) {

        daysDiff = ChronoUnit.DAYS.between(startDate, returnDate(newPropValue));
        minutesDiff = ChronoUnit.MINUTES.between(startTime, returnTime(newPropValue));

      } else if (endDate != null && property.equals("end")) {

        daysDiff = ChronoUnit.DAYS.between(endDate, returnDate(newPropValue));
        minutesDiff = ChronoUnit.MINUTES.between(endTime, returnTime(newPropValue));

        if ((endDate.plusDays(daysDiff).isBefore(startDate))
            || (endTime.plusMinutes(minutesDiff).isBefore(startTime))) {
          throw new IllegalArgumentException("End date/time cannot be before Start date/time");
        }
      }
      editSpec.daysDiff(daysDiff);
      editSpec.minsDiff(minutesDiff);
      EditSpec editDto = editSpec.build();
      model.edit(calName, editDto);

      view.showMessage("Event edited successfully for command: " + userInput);
    } catch (Exception e) {
      view.showError("Error editing event: " + userInput + ". Error: " + e.getMessage());
    }
  }
}
