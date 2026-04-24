package calendar.command.event;

import calendar.model.Event;
import calendar.model.MyCalendar;
import calendar.util.DateTimeParser;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Copies a single event to a target calendar at a specified start time.
 */
public class CopyEvent extends AbstractCopyCommand {

  /** Name of the event. */
  private final String eventName;

  /** Timestamp start for origin calendar.. */
  private final String startStr;

  /** Timestamp start for target calendar. */
  private final String targetStartStr;

  /**
   * Creates a new copy command for a single event.
   *
   * @param eventName          the name of the event to copy
   * @param startStr           the start date and time of the event
   * @param targetCalendarName name of the calendar to copy into
   * @param targetStartStr     start date and time in calendar to copy into
   */
  public CopyEvent(
          String eventName,
          String startStr,
          String targetCalendarName,
          String targetStartStr) {

    super(targetCalendarName);
    this.eventName = eventName;
    this.startStr = startStr;
    this.targetStartStr = targetStartStr;
  }

  /**
   * Finds the single original event to copy.
   *
   * @param sourceCalendar the source calendar
   * @return a list containing the event to copy, empty if not found.
   */
  @Override
  protected List<Event> getEventsToCopy(MyCalendar sourceCalendar) {
    ZonedDateTime sourceStart =
            DateTimeParser.parseDateTime(startStr, sourceCalendar.getZoneId());

    Optional<Event> maybeEvent =
            sourceCalendar.findEventsBySubjectAndStart(eventName, sourceStart)
                    .stream()
                    .findFirst();

    return maybeEvent.map(List::of).orElse(Collections.emptyList());
  }

  /**
   * Computes the new start time for the copied event in the target calendar.
   *
   * @param eventToCopy    the source event
   * @param targetCalendar the calendar to receive the copy
   * @return parsed date and time in the target calendar's timezone
   */
  @Override
  protected ZonedDateTime calculateNewStart(
          Event eventToCopy, MyCalendar targetCalendar) {
    return DateTimeParser.parseDateTime(targetStartStr, targetCalendar.getZoneId());
  }
}