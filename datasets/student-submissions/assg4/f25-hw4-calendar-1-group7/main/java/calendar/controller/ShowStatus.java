package calendar.controller;

import calendar.model.Calendar;
import calendar.view.MyCalendarView;
import java.time.LocalDateTime;

/**
 * Command to show busy/available status at a specific date/time.
 */
public class ShowStatus implements Command {
  private final LocalDateTime dateTime;

  /**
   * Creates a new ShowStatusCommand.
   *
   * @param dateTime the date/time to check
   */
  public ShowStatus(LocalDateTime dateTime) {
    this.dateTime = dateTime;
  }

  @Override
  public void execute(Calendar calendar, MyCalendarView view) {
    try {
      boolean busy = calendar.isBusy(dateTime);
      view.displayMessage(busy ? "busy" : "available");
    } catch (Exception e) {
      view.displayError("Error checking status: " + e.getMessage());
    }
  }

  @Override
  public boolean validate() {
    return dateTime != null;
  }
}