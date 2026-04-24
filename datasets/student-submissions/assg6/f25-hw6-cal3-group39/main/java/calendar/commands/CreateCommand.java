package calendar.commands;

import static calendar.controller.ControllerImpl.returnDate;
import static calendar.controller.ControllerImpl.returnTime;

import calendar.controller.CreateSpec;
import calendar.model.Model;
import calendar.view.View;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Implements the calendar.Commands.Command interface to handle the creation of calendar events.
 * Executes a create event command and notifies the view of success or failure.
 */
public class CreateCommand implements Command {

  /**
   * Executes the create event command using the given user input.
   *
   * @param userInput the full command input from the user
   * @param model     the model containing calendar data and logic
   * @param view      the view used to display messages or errors
   */
  @Override
  public void execute(String calName, String userInput, Model model, View view) {

    try {
      userInput = userInput.replace("::", ":");
      String[] userCommand = userInput.split(" ");
      String subject = userCommand[2];
      LocalDate startDate;
      LocalDate endDate;
      LocalTime startTime;
      LocalTime endTime;
      CreateSpec.CreateSpecBuilder createDto;

      if (userInput.contains("from")) {
        startDate = returnDate(userCommand[4]);
        startTime = returnTime(userCommand[4]);
        endDate = returnDate(userCommand[6]);
        endTime = returnTime(userCommand[6]);
        if ((startTime.isAfter(endTime) && startDate.equals(endDate))
            || (startDate.isAfter(endDate))) {
          throw new IllegalArgumentException("Start Time/Date is After End Time/Date");
        }
      } else {
        startDate = LocalDate.parse(userCommand[4]);
        startTime = LocalTime.parse("08:00");
        endDate = LocalDate.parse(userCommand[4]);
        endTime = LocalTime.parse("17:00");
      }

      createDto = new CreateSpec.CreateSpecBuilder(subject, startDate,
          startTime, endDate, endTime);

      addRecur(createDto, userInput, userCommand);
      addAddn(createDto, userInput, userCommand);
      CreateSpec createObj = createDto.build();
      model.create(calName, createObj);
      view.showMessage("Event created successfully for command: " + userInput);
    } catch (Exception e) {
      view.showError("Error creating event: " + userInput + ". Error: " + e.getMessage());
    }
  }

  private void addRecur(CreateSpec.CreateSpecBuilder createDto,
                        String userInput, String[] userCommand) {
    if (userInput.contains("repeats")) {
      if (!createDto.getStartDate().equals(createDto.getEndDate())) {
        throw new IllegalArgumentException("Recurrent Event cannot span multiple days");
      }
      if (userInput.contains("times")) {
        createDto.times(Integer.parseInt(userCommand[userCommand.length - 2]));
      } else if (userInput.contains("until")) {
        createDto.until(LocalDate.parse(userCommand[userCommand.length - 1]));
      }
    }
  }

  private void addAddn(CreateSpec.CreateSpecBuilder createDto,
                       String userInput, String[] userCommand) {
    for (int i = 0; i < userCommand.length; i++) {
      if (userCommand[i].equals("description") && i + 1 < userCommand.length) {
        createDto.description(userCommand[i + 1]);
        i++;
      } else if (userCommand[i].equals("location") && i + 1 < userCommand.length) {
        createDto.location(userCommand[i + 1]);
        i++;
      } else if (userCommand[i].equals("status") && i + 1 < userCommand.length) {
        createDto.status(userCommand[i + 1]);
        i++;
      } else if (userCommand[i].equals("repeats") && i + 1 < userCommand.length) {
        createDto.weekdays(userCommand[i + 1]);
        i++;
      }
    }
  }


}
