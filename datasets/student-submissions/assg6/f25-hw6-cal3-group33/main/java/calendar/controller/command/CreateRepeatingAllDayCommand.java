package calendar.controller.command;

import calendar.model.calendar.CalendarInterface;
import calendar.model.event.EventBuilder;
import calendar.model.manager.CalendarManager;
import calendar.view.CalendarView;

/**
 * Command to create a repeating all-day event series.
 *
 * <p>Creates multiple all-day event occurrences on specified weekdays.
 * Each event spans an entire day (8:00 AM to 5:00 PM by default).
 */
public class CreateRepeatingAllDayCommand implements Command {

  private final String subject;
  private final String date;
  private final String weekdays;
  private final Integer count;      // null if using untilDate
  private final String untilDate;   // null if using count

  /**
   * Creates a command for a repeating all-day event with a count.
   *
   * @param subject the event subject/title
   * @param date the start date (format: YYYY-MM-DD)
   * @param weekdays the weekdays to repeat on (e.g., "MWF" for Mon/Wed/Fri)
   * @param count the number of occurrences
   */
  public CreateRepeatingAllDayCommand(String subject, String date, String weekdays, int count) {
    this.subject = subject;
    this.date = date;
    this.weekdays = weekdays;
    this.count = count;
    this.untilDate = null;
  }

  /**
   * Creates a command for a repeating all-day event until a date.
   *
   * @param subject the event subject/title
   * @param date the start date (format: YYYY-MM-DD)
   * @param weekdays the weekdays to repeat on (e.g., "MWF" for Mon/Wed/Fri)
   * @param untilDate the end date for repetition (format: YYYY-MM-DD)
   */
  public CreateRepeatingAllDayCommand(String subject, String date, String weekdays,
                                      String untilDate) {
    this.subject = subject;
    this.date = date;
    this.weekdays = weekdays;
    this.count = null;
    this.untilDate = untilDate;
  }

  @Override
  public void execute(CalendarManager manager, CalendarView view) throws Exception {
    CalendarInterface calendar = manager.getCurrentCalendar();

    EventBuilder builder = calendar.newEvent(subject, date).weekdays(weekdays);

    if (count != null) {
      builder.forTimes(count);
    } else {
      builder.until(untilDate);
    }

    builder.create(calendar);
    view.displayMessage("Created All-Day Event Series: " + subject);
  }
}