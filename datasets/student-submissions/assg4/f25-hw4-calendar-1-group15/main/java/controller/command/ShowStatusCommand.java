package controller.command;

import controller.CommandResult;
import java.time.LocalDateTime;
import model.Icalendar;

/**
 * Command implementation for checking availability status at a specific date and time.
 * This command determines whether the user is busy or available at the specified time.
 */
public class ShowStatusCommand implements Command {
  private final LocalDateTime dateTime;

  /**
   * Constructs a ShowStatusCommand for the specified date and time.
   *
   * @param dateTime the date and time to check
   */
  public ShowStatusCommand(LocalDateTime dateTime) {
    this.dateTime = dateTime;
  }

  @Override
  public CommandResult execute(Icalendar calendar) {
    try {
      String status = calendar.showStatus(dateTime);
      return new CommandResult(true, status);
    } catch (Exception e) {
      return new CommandResult(false, "Error checking status: " + e.getMessage());
    }
  }
}