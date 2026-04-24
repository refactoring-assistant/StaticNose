package calendar.command;

import calendar.service.CalendarService;
import calendar.utils.DateTimeUtil;
import calendar.view.textbased.CalendarView;
import java.time.LocalDate;

/**
 * Command to copy all events on a specific day.
 */
public class CopyEventsOnCommand implements CalendarCommand {

  private final String onDateStr;
  private final String targetCalName;
  private final String toDateStr;

  /**
   * Initializes a CopyEventsOnCommand object.
   *
   * @param onDateStr The on date
   * @param targetCalName The target calendar name
   * @param toDateStr The to date
   */
  public CopyEventsOnCommand(String onDateStr, String targetCalName, String toDateStr) {
    this.onDateStr = onDateStr;
    this.targetCalName = targetCalName;
    this.toDateStr = toDateStr;
  }

  @Override
  public void execute(CalendarService service, CalendarView view) {
    try {
      LocalDate onDate = DateTimeUtil.parseDate(onDateStr);
      LocalDate toDate = DateTimeUtil.parseDate(toDateStr);

      service.copyEventsOn(onDate, targetCalName, toDate);
      view.showMessage("Events from " + onDate
          + " copied successfully to " + toDate + " in calendar '" + targetCalName + "'.");

    } catch (IllegalArgumentException e) {
      view.showError("Failed to copy events: " + e.getMessage());
    }
  }
}