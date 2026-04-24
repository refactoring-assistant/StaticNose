package calendar.controller.event;

import calendar.controller.Command;
import calendar.model.Calendar;
import calendar.model.CalendarApplication;
import calendar.model.VerifyModification;
import calendar.model.utils.DateTimeCheck;
import calendar.model.utils.EditType;
import calendar.view.CalendarView;
import java.time.DateTimeException;
import java.time.LocalDateTime;

/**
 * Command for editing event.
 * implements the Command interface and uses Active Calendar.
 */
public class EditEventCommand implements Command {

  private final EditType editType;
  private final String propertyName;
  private final String subject;
  private final String startDateTimeStr;
  private final String endDateTimeStr;
  private final String newValueStr;

  /**
   * Constructs a new command for editing an event or series.
   *
   * @param editType         The type of edit (SINGLE_EVENT, THIS_AND_FUTURE, FULL_SERIES).
   * @param propertyName     The property to change (e.g., "subject").
   * @param subject          The subject of the event to find.
   * @param startDateTimeStr The start time of the event to find (YYYY-MM-DDTHH:mm).
   * @param endDateTimeStr   The end time (YYYY-MM-DDTHH:mm), only used for SINGLE_EVENT.
   * @param newValueStr      The new value for the property, as a string.
   */
  public EditEventCommand(EditType editType, String propertyName, String subject,
                          String startDateTimeStr, String endDateTimeStr, String newValueStr) {
    this.editType = editType;
    this.propertyName = propertyName;
    this.subject = subject;
    this.startDateTimeStr = startDateTimeStr;
    this.endDateTimeStr = endDateTimeStr;
    this.newValueStr = newValueStr;
  }

  @Override
  public void execute(CalendarApplication model, CalendarView view) {
    try {
      Calendar activeCalendar = model.getActiveCalendar();

      LocalDateTime start = DateTimeCheck.parseDateTime(startDateTimeStr);
      Object newValue = VerifyModification.parseNewValue(propertyName, newValueStr);

      switch (editType) {
        case SINGLE_EVENT:
          if (endDateTimeStr == null) {
            throw new IllegalArgumentException(
                "'edit event' command requires 'to <dateTime>' to be specified.");
          }
          LocalDateTime end = DateTimeCheck.parseDateTime(endDateTimeStr);
          activeCalendar.editEvent(subject, start, end, propertyName, newValue);
          break;

        case THIS_AND_FUTURE:
          activeCalendar.editEventAndFuture(subject, start, propertyName, newValue);
          break;

        default:
          activeCalendar.editFullSeries(subject, start, propertyName, newValue);
          break;
      }

      view.displaySuccess("Event modified successfully.");

    } catch (IllegalArgumentException
             | DateTimeException
             | NullPointerException
             | IllegalStateException e) {
      view.displayError(e.getMessage());
    }
  }
}