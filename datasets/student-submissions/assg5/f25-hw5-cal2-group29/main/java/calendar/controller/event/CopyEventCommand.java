package calendar.controller.event;

import calendar.controller.Command;
import calendar.model.CalendarApplication;
import calendar.model.utils.DateTimeCheck;
import calendar.view.CalendarView;
import java.time.LocalDateTime;

/**
 * Command class for copying event.
 * Handles parsing and executing the copy event command.
 */
public class CopyEventCommand implements Command {

  private final String subject;
  private final String startDateTimeStr;
  private final String targetCalendarName;
  private final String targetStartDateTimeStr;

  /**
   * Initializes an object for this command.
   *
   * @param subject                Subject of event
   * @param startDateTimeStr       start date/time
   * @param targetCalendarName     target calendar
   * @param targetStartDateTimeStr new start date/time
   */
  public CopyEventCommand(String subject, String startDateTimeStr, String targetCalendarName,
                          String targetStartDateTimeStr) {
    this.subject = subject;
    this.startDateTimeStr = startDateTimeStr;
    this.targetCalendarName = targetCalendarName;
    this.targetStartDateTimeStr = targetStartDateTimeStr;
  }

  @Override
  public void execute(CalendarApplication model, CalendarView view) {
    try {
      LocalDateTime start = DateTimeCheck.parseDateTime(startDateTimeStr);
      LocalDateTime targetStart = DateTimeCheck.parseDateTime(targetStartDateTimeStr);

      model.copyEvent(subject, start, targetCalendarName, targetStart);
      view.displaySuccess("Event copied successfully.");
    } catch (IllegalArgumentException | IllegalStateException e) {
      view.displayError(e.getMessage());
    }
  }
}