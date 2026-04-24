package calendar.controller.commands;

import calendar.controller.Command;
import calendar.model.Model;
import calendar.view.View;
import java.time.LocalDateTime;

/**
 * Command to show the user's status (busy or available) at a given time.
 */
public class ShowStatusCommand implements Command {
  private final LocalDateTime dateTime;

  /**
   * Creates a show status command.
   *
   * @param dateTime The date and time to check status for
   */
  public ShowStatusCommand(LocalDateTime dateTime) {
    this.dateTime = dateTime;
  }

  @Override
  public void execute(Model model, View view) {
    ShowStatus showStatus = model.showStatus(dateTime);
    view.render(showStatus.name().toLowerCase() + System.lineSeparator());
  }
}