package calendar.command.event;

import calendar.model.CalendarManager;
import calendar.model.MyCalendar;
import calendar.util.DateTimeParser;
import calendar.view.CalendarTextView;
import java.time.ZonedDateTime;

/**
 * Command to check the user's status (busy or available) at a particular date and time.
 */
public class Status extends AbstractEventCommand {

  private final String dateTimeStr;

  /**
   * Constructs a new Status command.
   *
   * @param dateTimeStr The date and time string to check.
   */
  public Status(String dateTimeStr) {
    this.dateTimeStr = dateTimeStr;
  }

  @Override
  protected void executeWithCalendar(CalendarManager manager, MyCalendar model,
                                     CalendarTextView view) {
    ZonedDateTime dateTime = DateTimeParser.parseDateTime(dateTimeStr, model.getZoneId());
    boolean isBusy = model.isBusy(dateTime);
    view.printStatus(isBusy);
  }
}
