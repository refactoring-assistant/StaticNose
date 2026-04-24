package calendar.controller.commands;

import calendar.model.Calendar;
import calendar.model.Event;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Command to show busy available status for a given time.
 */
public class ShowStatusCommand implements CalendarCommand {
  private final LocalDateTime checkTime;

  /**
   * constructs a show status command for the given time.
   *
   *
   */
  public ShowStatusCommand(LocalDateTime checkTime) {
    this.checkTime = checkTime;
  }

  @Override
  public String execute(Calendar model) {
    return model.isBusy(checkTime) ? "busy" : "available";
  }
}