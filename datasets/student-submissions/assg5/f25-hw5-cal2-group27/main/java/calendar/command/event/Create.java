package calendar.command.event;

import calendar.model.CalendarManager;
import calendar.model.Event;
import calendar.model.EventBuilder;
import calendar.model.MyCalendar;
import calendar.model.RecurrenceRule;
import calendar.util.DateTimeParser;
import calendar.view.CalendarTextView;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

/**
 * Command to create a new single or recurring event.
 */
public class Create extends AbstractEventCommand {

  private final String subject;
  private final String startStr;
  private final String endStr;
  private final RecurrenceRule rule;

  private Create(String subject, String startStr, String endStr, RecurrenceRule rule) {
    this.subject = subject;
    this.startStr = startStr;
    this.endStr = endStr;
    this.rule = rule;
  }

  /** Creates a command for a single event with given  start and end times. */
  public static Create single(String subject, String startStr, String endStr) {
    return new Create(subject, startStr, endStr, null);
  }

  /** Creates a command for a single all-day event. */
  public static Create allDay(String subject, String dateStr) {
    return new Create(subject, dateStr + "T08:00", dateStr + "T17:00", null);
  }

  /**
   * Creates a command for an event series that repeats for a specific count.
   */
  public static Create recurringForCount(
          String subject,
          String startStr,
          String endStr,
          String weekdaysStr,
          int count) {

    Set<DayOfWeek> weekdays = DateTimeParser.parseWeekdays(weekdaysStr);
    RecurrenceRule rule = new RecurrenceRule(weekdays, count);
    return new Create(subject, startStr, endStr, rule);
  }

  /**
   * Creates a command for a recurring event that repeats until a specific date.
   */
  public static Create recurringUntilDate(
          String subject,
          String startStr,
          String endStr,
          String weekdaysStr,
          String untilDateStr) {

    Set<DayOfWeek> weekdays = DateTimeParser.parseWeekdays(weekdaysStr);
    LocalDate untilDate = DateTimeParser.parseDate(untilDateStr);
    RecurrenceRule rule = new RecurrenceRule(weekdays, untilDate);
    return new Create(subject, startStr, endStr, rule);
  }

  /**
   * Creates a command for a recurring all-day event that repeats for a specific count.
   */
  public static Create allDayRecurringForCount(
          String subject,
          String dateStr,
          String weekdaysStr,
          int count) {

    Set<DayOfWeek> weekdays = DateTimeParser.parseWeekdays(weekdaysStr);
    RecurrenceRule rule = new RecurrenceRule(weekdays, count);
    return new Create(subject, dateStr + "T08:00", dateStr + "T17:00", rule);
  }

  /**
   * Creates a command for a recurring all-day event that repeats until a specific date.
   */
  public static Create allDayRecurringUntilDate(
          String subject,
          String dateStr,
          String weekdaysStr,
          String untilDateStr) {

    Set<DayOfWeek> weekdays = DateTimeParser.parseWeekdays(weekdaysStr);
    LocalDate untilDate = DateTimeParser.parseDate(untilDateStr);
    RecurrenceRule rule = new RecurrenceRule(weekdays, untilDate);
    return new Create(subject, dateStr + "T08:00", dateStr + "T17:00", rule);
  }

  @Override
  protected void executeWithCalendar(
          CalendarManager manager, MyCalendar model, CalendarTextView view) {

    ZoneId zoneId = model.getZoneId();
    ZonedDateTime start = DateTimeParser.parseDateTime(startStr, zoneId);
    ZonedDateTime end = DateTimeParser.parseDateTime(endStr, zoneId);

    if (rule == null) {
      Event newEvent = new EventBuilder()
              .subject(subject)
              .start(start)
              .end(end)
              .build();

      model.addEvent(newEvent);
      view.printMessage("Event created successfully!");
      view.printEvent(newEvent);
      return;
    }

    Event templateEvent = new EventBuilder()
            .subject(subject)
            .start(start)
            .end(end)
            .recurrence(rule)
            .build();

    List<Event> conflicts = model.addEventSeries(templateEvent, rule);

    if (conflicts.isEmpty()) {
      view.printMessage("Recurring event created successfully!");
      return;
    }

    int totalExpected = (rule.getCount() != null) ? rule.getCount() : 0;

    if (totalExpected == 0) {
      view.printMessage(
              String.format(
                      "Recurring event series created. %d events were skipped due to conflicts.",
                      conflicts.size()));
    } else {
      int added = totalExpected - conflicts.size();
      view.printMessage(
              String.format(
                      "Recurring event series created. %d events were added, "
                              + "%d were skipped due to conflicts.",
                      added, conflicts.size()));
    }

    view.printMessage("Skipped events:");
    for (Event conflict : conflicts) {
      view.printEvent(conflict);
    }
  }
}
