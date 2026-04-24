package calendar.command.impl;

import calendar.command.CommandInterface;
import calendar.controller.CalendarControllerInterface;
import java.time.ZonedDateTime;

/**
 * This class implements the show status command and shows if the user is busy or available.
 */
public class ShowStatusCommand implements CommandInterface {
  private final CalendarControllerInterface controller;
  private final ZonedDateTime dateTime;

  /**
   * This is the constructor to implement the check of if the user is busy or available.
   *
   * @param dateTime is the date and time on which the command is passed.
   */
  public ShowStatusCommand(CalendarControllerInterface controller, ZonedDateTime dateTime) {
    this.controller = controller;
    this.dateTime = dateTime;
  }

  @Override
  public String execute() {
    try {
      boolean busy = controller.isUserBusy(dateTime);
      return busy ? "busy" : "available";
    } catch (Exception e) {
      return "Error: " + e.getMessage();
    }
  }

  @Override
  public String getDescription() {
    return "Show busy/available status";
  }
}