package calendar.command.event;

import calendar.model.Event;
import calendar.model.MyCalendar;
import calendar.util.DateTimeParser;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Copies multiple events from the active calendar to a target calendar.
 */
public class CopyEvents extends AbstractCopyCommand {

  private final String fromDateStr;
  private final String toDateStr;
  private final String targetStartDateStr;
  private final boolean isRange;

  private ZoneId sourceZone;
  private LocalDate fromDate;

  /**
   * Creates a command that copies a block of events.
   *
   * @param fromDateStr        the start date in the source calendar
   * @param toDateStr          the end date in the source calendar
   * @param targetCalendarName the name of the calendar to copy into
   * @param targetStartDateStr the starting date in the target calendar for the first day's copies
   * @param isRange            the whether to copy an inclusive range or a single day
   */
  public CopyEvents(
          String fromDateStr,
          String toDateStr,
          String targetCalendarName,
          String targetStartDateStr,
          boolean isRange) {

    super(targetCalendarName);
    this.fromDateStr = fromDateStr;
    this.toDateStr = toDateStr;
    this.targetStartDateStr = targetStartDateStr;
    this.isRange = isRange;
  }

  /**
   * Selects events on the single day or in the inclusive range.
   *
   * @param sourceCalendar the active calendar to read from
   * @return list of events to copy
   */
  @Override
  protected List<Event> getEventsToCopy(MyCalendar sourceCalendar) {
    this.sourceZone = sourceCalendar.getZoneId();
    this.fromDate = DateTimeParser.parseDate(fromDateStr);

    if (isRange) {
      LocalDate toDate = DateTimeParser.parseDate(toDateStr);

      ZonedDateTime startInclusive =
              fromDate.atStartOfDay(sourceCalendar.getZoneId());
      ZonedDateTime endExclusive =
              toDate.plusDays(1).atStartOfDay(sourceCalendar.getZoneId());

      return sourceCalendar.getEventsInRange(startInclusive, endExclusive);
    }

    return sourceCalendar.getEventsOnDate(fromDate);
  }

  /**
   * Computes the new start time for a copied event in the target calendar.
   *
   * @param eventToCopy    the event being copied
   * @param targetCalendar the calendar that will receive the copy
   * @return the timestamp of the copied event's start in the target calendar
   */
  @Override
  protected ZonedDateTime calculateNewStart(Event eventToCopy, MyCalendar targetCalendar) {
    LocalDate targetStartDate = DateTimeParser.parseDate(targetStartDateStr);
    ZoneId targetZone = targetCalendar.getZoneId();

    ZonedDateTime sourceStart =
            eventToCopy.getStart().withZoneSameInstant(sourceZone);

    long dayOffset =
            java.time.temporal.ChronoUnit.DAYS.between(fromDate, sourceStart.toLocalDate());

    LocalDate targetDate = targetStartDate.plusDays(dayOffset);

    ZonedDateTime sourceStartInTarget =
            eventToCopy.getStart().withZoneSameInstant(targetZone);

    LocalTime eventLocalTime = sourceStartInTarget.toLocalTime();

    return ZonedDateTime.of(targetDate, eventLocalTime, targetZone);
  }
}