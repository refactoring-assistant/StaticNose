package calendar.commands;

import static calendar.controller.ControllerImpl.returnDate;
import static calendar.controller.ControllerImpl.returnTime;

import calendar.controller.CopySpec;
import calendar.model.Model;
import calendar.view.View;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * This class is the copy command class in the command design pattern whose main job is to call
 * the copy method in the model.
 */
public class CopyCommand implements Command {

  @Override
  public void execute(String calName, String userInput, Model model, View view) {
    try {
      userInput = userInput.replace("::", ":");
      String[] command = userInput.split(" ");
      String subject = null;
      String targetCal = null;
      LocalDate targetDate;
      LocalTime targetTime = null;
      LocalDate startDate;
      LocalTime startTime = null;
      LocalDate endDate = null;

      for (int i = 0; i < command.length; i++) {
        if (command[i].equals("--target") && i + 1 < command.length) {
          targetCal = command[++i];
          break;
        }
      }
      if (targetCal == null) {
        view.showError("Error: No target calendar found! " + userInput);
        return;
      }

      if (userInput.contains("between")) {
        startDate = LocalDate.parse(command[3]);
        endDate = LocalDate.parse(command[5]);
        targetDate = LocalDate.parse(command[command.length - 1]);
      } else if (userInput.contains("events")) {
        startDate = LocalDate.parse(command[3]);
        targetDate = LocalDate.parse(command[command.length - 1]);
      } else {
        subject = command[2];
        startDate = returnDate(command[4]);
        startTime = returnTime(command[4]);
        targetDate = returnDate(command[command.length - 1]);
        targetTime = returnTime(command[command.length - 1]);
      }

      CopySpec.CopySpecBuilder builder = new CopySpec.CopySpecBuilder(startDate,
          targetCal, targetDate)
          .subject(subject)
          .targetTime(targetTime)
          .startTime(startTime)
          .endDate(endDate);

      CopySpec copyDto = builder.build();
      model.copy(calName, copyDto);
      view.showMessage("calendar events copied successfully for command: " + userInput);
    } catch (Exception e) {
      view.showError("Error copying events: " + userInput + ". Error: " + e.getMessage());
    }
  }
}
