package calendar.command;

import calendar.service.CalendarService;
import calendar.view.textbased.CalendarView;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/**
 * Command to show the user's busy/available status at a specific time.
 */
public class ShowStatusCommand implements CalendarCommand {
  private final String dateTime;

  /**
   * Constructs a ShowStatusCommand.
   *
   * @param dateTime The date and time to check status for.
   */
  public ShowStatusCommand(String dateTime) {
    this.dateTime = dateTime;
  }

  @Override
  public void execute(CalendarService service, CalendarView view) throws IllegalArgumentException {
    try {
      boolean isBusy = service.isBusy(LocalDateTime.parse(dateTime));
      view.showStatus(isBusy);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid date/time format. Use YYYY-MM-DDTHH:MM:SS.", e);
    }
  }
}