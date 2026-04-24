package calendar.command;

import calendar.service.CalendarService;
import calendar.utils.DateTimeUtil;
import calendar.view.textbased.CalendarView;
import java.time.LocalDate;

/**
 * Command to copy all events within a date range.
 */
public class CopyEventsBetweenCommand implements CalendarCommand {

  private final String startDateStr;
  private final String endDateStr;
  private final String targetCalName;
  private final String toDateStr;

  /**
   * Initializes the CopyEventsBetweenCommand object.
   *
   * @param startDateStr The start date string
   * @param endDateStr The end date string
   * @param targetCalName The target calendar name
   * @param toDateStr The to date string
   */
  public CopyEventsBetweenCommand(String startDateStr, String endDateStr,
                                  String targetCalName, String toDateStr) {
    this.startDateStr = startDateStr;
    this.endDateStr = endDateStr;
    this.targetCalName = targetCalName;
    this.toDateStr = toDateStr;
  }

  @Override
  public void execute(CalendarService service, CalendarView view) {
    try {
      LocalDate startDate = DateTimeUtil.parseDate(startDateStr);
      LocalDate endDate = DateTimeUtil.parseDate(endDateStr);
      LocalDate toDate = DateTimeUtil.parseDate(toDateStr);

      service.copyEventsBetween(startDate, endDate, targetCalName, toDate);
      view.showMessage("Events between " + startDate + " and " + endDate
          + " copied successfully to a timeline starting " + toDate
          + " in calendar '" + targetCalName + "'.");

    } catch (IllegalArgumentException e) {
      view.showError("Failed to copy events: " + e.getMessage());
    }
  }
}