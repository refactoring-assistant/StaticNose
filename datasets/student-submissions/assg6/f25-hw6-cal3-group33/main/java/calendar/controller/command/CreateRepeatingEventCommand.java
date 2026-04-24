package calendar.controller.command;

import calendar.model.calendar.CalendarInterface;
import calendar.model.event.EventBuilder;
import calendar.model.manager.CalendarManager;
import calendar.view.CalendarView;

/**
 * Command to create a repeating event series.
 *
 * <p>Creates multiple event occurrences on specified weekdays.
 * Repeats either for a specified number of times or until a specific date.
 */
public class CreateRepeatingEventCommand implements Command {

  private final String subject;
  private final String startDateTime;
  private final String endDateTime;
  private final String weekdays;
  private final Integer count;      // null if using untilDate
  private final String untilDate;   // null if using count

  /**
   * Creates a command for a repeating event with a count.
   *
   * @param subject the event subject/title
   * @param startDateTime the start date and time (format: YYYY-MM-DDThh:mm)
   * @param endDateTime the end date and time (format: YYYY-MM-DDThh:mm)
   * @param weekdays the weekdays to repeat on (e.g., "MWF" for Mon/Wed/Fri)
   * @param count the number of occurrences
   */
  public CreateRepeatingEventCommand(String subject, String startDateTime, String endDateTime,
                                     String weekdays, int count) {
    this.subject = subject;
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
    this.weekdays = weekdays;
    this.count = count;
    this.untilDate = null;
  }

  /**
   * Creates a command for a repeating event until a date.
   *
   * @param subject the event subject/title
   * @param startDateTime the start date and time (format: YYYY-MM-DDThh:mm)
   * @param endDateTime the end date and time (format: YYYY-MM-DDThh:mm)
   * @param weekdays the weekdays to repeat on (e.g., "MWF" for Mon/Wed/Fri)
   * @param untilDate the end date for repetition (format: YYYY-MM-DD)
   */
  public CreateRepeatingEventCommand(String subject, String startDateTime, String endDateTime,
                                     String weekdays, String untilDate) {
    this.subject = subject;
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
    this.weekdays = weekdays;
    this.count = null;
    this.untilDate = untilDate;
  }

  @Override
  public void execute(CalendarManager manager, CalendarView view) throws Exception {
    CalendarInterface calendar = manager.getCurrentCalendar();

    EventBuilder builder = calendar.newEvent(subject, startDateTime)
        .end(endDateTime)
        .weekdays(weekdays);

    if (count != null) {
      builder.forTimes(count);
    } else {
      builder.until(untilDate);
    }

    builder.create(calendar);
    view.displayMessage("Created Event Series: " + subject);
  }
}