package calendar.command.event;

import calendar.command.PrintScope;
import calendar.model.CalendarManager;
import calendar.model.Event;
import calendar.model.MyCalendar;
import calendar.util.DateTimeParser;
import calendar.view.CalendarTextView;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Command to print events from the calendar.
 */
public class Print extends AbstractEventCommand {

  private final PrintScope scope;
  private final String onDateStr;
  private final String fromDateTimeStr;
  private final String toDateTimeStr;

  /**
   * Constructs a new Print command to print events on a specific day.
   *
   * @param dateStr The date string.
   */
  public Print(String dateStr) {
    this.scope = PrintScope.ON;
    this.onDateStr = dateStr;
    this.fromDateTimeStr = null;
    this.toDateTimeStr = null;
  }

  /**
   * Constructs a new Print command to print events in a specific time range.
   *
   * @param startStr The start time string.
   * @param endStr   The end time string.
   */
  public Print(String startStr, String endStr) {
    this.scope = PrintScope.FROM_TO;
    this.onDateStr = null;
    this.fromDateTimeStr = startStr;
    this.toDateTimeStr = endStr;
  }

  @Override
  protected void executeWithCalendar(CalendarManager manager, MyCalendar model,
                                     CalendarTextView view) {
    List<Event> events;
    if (scope == PrintScope.ON) {
      LocalDate onDate = DateTimeParser.parseDate(onDateStr);
      events = model.getEventsOnDate(onDate);
    } else {
      ZonedDateTime fromDateTime = DateTimeParser.parseDateTime(fromDateTimeStr, model.getZoneId());
      ZonedDateTime toDateTime = DateTimeParser.parseDateTime(toDateTimeStr, model.getZoneId());
      events = model.getEventsInRange(fromDateTime, toDateTime);
    }
    view.printEvents(events);
  }
}
