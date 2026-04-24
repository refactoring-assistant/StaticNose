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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

/**
 * Command to copy all events within a date range from one calendar to another.
 */
public class CopyEventsBetweenDatesCommand implements InCommand {

  private final CalendarDatabase calendarDatabase;
  private final InCalendarView view;
  private final LocalDate sourceStartDate;
  private final LocalDate sourceEndDate;
  private final String targetCalendarName;
  private final LocalDate targetStartDate;

  /**
   * Copy events between specified source start and end dates given a target calendar
   *    and a target date.
   *
   * @param calendarDatabase databaseObject
   * @param view calendarview object
   * @param sourceStartDate startDate
   * @param sourceEndDate endDate
   * @param targetCalendarName the target calendar where we copy the event
   * @param targetStartDate the start date to be put in the calendar
   */
  public CopyEventsBetweenDatesCommand(CalendarDatabase calendarDatabase,
                                       InCalendarView view,
                                       LocalDate sourceStartDate,
                                       LocalDate sourceEndDate,
                                       String targetCalendarName,
                                       LocalDate targetStartDate) {
    Objects.requireNonNull(calendarDatabase, "CalendarDatabase cannot be null");
    Objects.requireNonNull(view, "View cannot be null");
    Objects.requireNonNull(sourceStartDate, "Source start date cannot be null");
    Objects.requireNonNull(sourceEndDate, "Source end date cannot be null");
    Objects.requireNonNull(targetCalendarName, "Target calendar name cannot be null");
    Objects.requireNonNull(targetStartDate, "Target start date cannot be null");

    this.calendarDatabase = calendarDatabase;
    this.view = view;
    this.sourceStartDate = sourceStartDate;
    this.sourceEndDate = sourceEndDate;
    this.targetCalendarName = targetCalendarName.trim();
    this.targetStartDate = targetStartDate;
  }

  @Override
  public void execute() throws CalendarException {
    if (sourceStartDate.isAfter(sourceEndDate)) {
      throw new CalendarException(
          "Invalid date range: start date must be before or equal to end date");
    }

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

    LocalDateTime rangeStart = sourceStartDate.atStartOfDay();
    LocalDateTime rangeEnd = sourceEndDate.atTime(23, 59, 59);
    List<InEvent> eventsInRange = readOnlySource.getEventsBetween(rangeStart, rangeEnd);

    if (eventsInRange.isEmpty()) {
      view.displayMessage("No events found between " + sourceStartDate
          + " and " + sourceEndDate + " in calendar: " + sourceCalendar.getCalendarName());
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

    long daysOffset = ChronoUnit.DAYS.between(sourceStartDate, targetStartDate);

    int copiedCount = 0;
    int skippedCount = 0;

    for (InEvent sourceEvent : eventsInRange) {
      try {
        InEvent copiedEvent = TimezoneUtil.copyEventWithTimezoneConversion(
            sourceEvent, sourceTimezone, targetTimezone, daysOffset);

        targetCalendar.addEvent(copiedEvent);
        copiedCount++;
      } catch (DuplicateEventException e) {
        skippedCount++;
      }
    }

    view.displaySuccess("Copied " + copiedCount + " event(s) from "
        + sourceStartDate + " to " + sourceEndDate
        + " --> " + targetCalendarName + " starting " + targetStartDate);
    if (skippedCount > 0) {
      view.displayMessage("Skipped " + skippedCount + " duplicate event(s)");
    }
  }

  @Override
  public String getDescription() {
    return "Copy events from " + sourceStartDate + " to " + sourceEndDate
        + " → calendar: " + targetCalendarName;
  }
}