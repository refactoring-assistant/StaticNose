package calendar.command;

import calendar.exception.CalendarException;
import calendar.exception.CalendarNotFoundException;
import calendar.exception.DuplicateEventException;
import calendar.model.CalendarDatabase;
import calendar.model.InCalendar;
import calendar.model.InEvent;
import calendar.model.ReadOnlyCalendar;
import calendar.util.TimezoneUtil;
import calendar.view.InCalendarView;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

/**
 * Command to copy all events on a specific date from one calendar to another.
 */
public class CopyEventsOnDateCommand implements InCommand {

  private final CalendarDatabase calendarDatabase;
  private final InCalendarView view;
  private final LocalDate sourceDate;
  private final String targetCalendarName;
  private final LocalDate targetDate;

  /**
   * Constructs a copy event command.
   * This command copies all events scheduled on a specific date from the currently
   *    active calendar to a target calendar on a specified target date. Times are
   *    automatically converted to match the target calendar's timezone.
   *
   * @param calendarDatabase the database managing all calendars
   * @param view the view for displaying success/error messages to the user
   * @param sourceDate the date to copy events from in the active calendar's timezone
   * @param targetCalendarName the name of the target calendar to copy events into
   * @param targetDate the date to place copied events on in the target calendar's timezone
   * @throws NullPointerException if any parameter is null (enforced by Objects.requireNonNull)
   */
  public CopyEventsOnDateCommand(CalendarDatabase calendarDatabase,
                                 InCalendarView view,
                                 LocalDate sourceDate,
                                 String targetCalendarName,
                                 LocalDate targetDate) {
    Objects.requireNonNull(calendarDatabase, "CalendarDatabase cannot be null");
    Objects.requireNonNull(view, "View cannot be null");
    Objects.requireNonNull(sourceDate, "Source date cannot be null");
    Objects.requireNonNull(targetCalendarName, "Target calendar name cannot be null");
    Objects.requireNonNull(targetDate, "Target date cannot be null");

    this.calendarDatabase = calendarDatabase;
    this.view = view;
    this.sourceDate = sourceDate;
    this.targetCalendarName = targetCalendarName.trim();
    this.targetDate = targetDate;
  }

  @Override
  public void execute() throws CalendarException {
    InCalendar sourceCalendar = calendarDatabase.getActiveCalendar();
    if (sourceCalendar == null) {
      throw new CalendarException(
          "No calendar is active. Use 'use calendar --name <name>' first.");
    }

    ReadOnlyCalendar readOnlySource = new ReadOnlyCalendar(sourceCalendar);

    InCalendar targetCalendar;
    try {
      targetCalendar = calendarDatabase.getCalendar(targetCalendarName);
    } catch (CalendarNotFoundException e) {
      throw new CalendarException("Target calendar not found: " + targetCalendarName, e);
    }

    List<InEvent> eventsOnDate = readOnlySource.getEventsOnDate(sourceDate);

    if (eventsOnDate.isEmpty()) {
      view.displayMessage("No events found on " + sourceDate
          + " in calendar: " + sourceCalendar.getCalendarName());
      return;
    }

    ZoneId sourceTimezone;
    ZoneId targetTimezone;
    try {
      sourceTimezone = calendarDatabase.getTimezone(sourceCalendar.getCalendarName());
      targetTimezone = calendarDatabase.getTimezone(targetCalendarName);
    } catch (CalendarNotFoundException e) {
      throw new CalendarException("Error getting calendar timezone", e);
    }

    long daysDiff = ChronoUnit.DAYS.between(sourceDate, targetDate);

    int copiedCount = 0;
    int skippedCount = 0;

    for (InEvent sourceEvent : eventsOnDate) {
      try {
        InEvent copiedEvent = TimezoneUtil.copyEventWithTimezoneConversion(
            sourceEvent, sourceTimezone, targetTimezone, daysDiff);

        targetCalendar.addEvent(copiedEvent);
        copiedCount++;
      } catch (DuplicateEventException e) {
        skippedCount++;
      }
    }

    view.displaySuccess("Copied " + copiedCount + " event(s) from "
        + sourceDate + " to " + targetDate + " in calendar: " + targetCalendarName);
    if (skippedCount > 0) {
      view.displayMessage("Skipped " + skippedCount + " duplicate event(s)");
    }
  }

  @Override
  public String getDescription() {
    return "Copy events from " + sourceDate + " to " + targetDate
        + " in calendar: " + targetCalendarName;
  }
}