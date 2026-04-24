package calendar.controller.event;

import calendar.controller.Command;
import calendar.model.CalendarApplication;
import calendar.model.utils.DateTimeCheck;
import calendar.view.CalendarView;
import java.time.LocalDate;

/**
 * Command class for copying events on.
 * Handles parsing and executing the copy events on date command.
 */
public class CopyEventsOnDateCommand implements Command {

  private final String dateStr;
  private final String targetCalendarName;
  private final String targetDateStr;

  /**
   * Initializes an object for this command.
   *
   * @param dateStr            Date string
   * @param targetCalendarName target calendar
   * @param targetDateStr      new start date
   */
  public CopyEventsOnDateCommand(String dateStr, String targetCalendarName, String targetDateStr) {
    this.dateStr = dateStr;
    this.targetCalendarName = targetCalendarName;
    this.targetDateStr = targetDateStr;
  }

  @Override
  public void execute(CalendarApplication model, CalendarView view) {
    try {
      LocalDate date = DateTimeCheck.parseDate(dateStr);
      LocalDate targetDate = DateTimeCheck.parseDate(targetDateStr);

      model.copyEventsOnDate(date, targetCalendarName, targetDate);
      view.displaySuccess("Events copied successfully.");
    } catch (IllegalArgumentException | IllegalStateException e) {
      view.displayError(e.getMessage());
    }
  }
}