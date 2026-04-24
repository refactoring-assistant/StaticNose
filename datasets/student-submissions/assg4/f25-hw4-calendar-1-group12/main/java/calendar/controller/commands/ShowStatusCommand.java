package calendar.controller.commands;

import calendar.model.CalendarModel;
import calendar.view.CalendarView;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Command to show busy/available status at a specific date/time.
 */
public class ShowStatusCommand extends AbstractCommand {
  private final LocalDateTime dateTime;

  /**
   * Constructs a ShowStatusCommand to check busy status at a specific time.
   *
   * @param dateTime the date and time to check
   */
  public ShowStatusCommand(LocalDateTime dateTime) {
    this.dateTime = dateTime;
  }

  @Override
  public void execute(CalendarModel model, CalendarView view) throws IOException {
    boolean isBusy = model.isBusy(dateTime);
    view.displayStatus(isBusy);
  }
}