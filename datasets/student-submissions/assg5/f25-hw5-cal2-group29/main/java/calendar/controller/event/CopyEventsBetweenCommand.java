package calendar.controller.event;

import calendar.controller.Command;
import calendar.model.CalendarApplication;
import calendar.model.utils.DateTimeCheck;
import calendar.view.CalendarView;
import java.time.LocalDate;

/**
 * Command class for copying events between.
 * Handles parsing and executing the copy events between dates command.
 */
public class CopyEventsBetweenCommand implements Command {

  private final String startDateStr;
  private final String endDateStr;
  private final String targetCalendarName;
  private final String targetStartDateStr;

  /**
   * Initializes an object for this command.
   *
   * @param startDateStr       start date/time
   * @param endDateStr         end date/time
   * @param targetCalendarName target calendar
   * @param targetStartDateStr new start date/time
   */
  public CopyEventsBetweenCommand(String startDateStr, String endDateStr, String targetCalendarName,
                                  String targetStartDateStr) {
    this.startDateStr = startDateStr;
    this.endDateStr = endDateStr;
    this.targetCalendarName = targetCalendarName;
    this.targetStartDateStr = targetStartDateStr;
  }

  @Override
  public void execute(CalendarApplication model, CalendarView view) {
    try {
      LocalDate startDate = DateTimeCheck.parseDate(startDateStr);
      LocalDate endDate = DateTimeCheck.parseDate(endDateStr);
      LocalDate targetStartDate = DateTimeCheck.parseDate(targetStartDateStr);

      if (endDate.isBefore(startDate)) {
        throw new IllegalArgumentException("Start date must be before end date.");
      }

      model.copyEventsBetween(startDate, endDate, targetCalendarName, targetStartDate);
      view.displaySuccess("Events copied successfully.");
    } catch (IllegalArgumentException | IllegalStateException e) {
      view.displayError(e.getMessage());
    }
  }
}