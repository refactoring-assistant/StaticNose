package calendar.controller.commands;

import calendar.controller.Command;
import calendar.model.CalendarModel;
import calendar.view.CalendarView;
import java.time.LocalDateTime;

/**
 * Command to show busy/available status at a specific time.
 */
public class ShowStatusCommand implements Command {
  private final LocalDateTime dateTime;

  /**
   * Constructs a ShowStatusCommand.
   *
   * @param dateTime the date and time to check status for
   */
  public ShowStatusCommand(LocalDateTime dateTime) {
    this.dateTime = dateTime;
  }

  @Override
  public void execute(CalendarModel calendar, CalendarView view) {
    try {
      boolean busy = calendar.isBusy(dateTime);
      view.displayStatus(busy);
    } catch (Exception e) {
      view.displayError(e.getMessage());
    }
  }
}