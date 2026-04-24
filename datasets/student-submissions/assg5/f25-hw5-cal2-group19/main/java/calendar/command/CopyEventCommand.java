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
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Command to copy a single event from one calendar to another.
 */
public class CopyEventCommand implements InCommand {

  private final CalendarDatabase calendarDatabase;
  private final InCalendarView view;
  private final String eventSubject;
  private final LocalDateTime sourceDateTime;
  private final String targetCalendarName;
  private final LocalDateTime targetDateTime;

  /**
   * Constructor of CopyEventCommand.
   *
   * @param calendarDatabase the calendar database containing the event to copy
   * @param view the view for displaying results and error messages
   * @param eventSubject the subject/title of the event to be copied
   * @param sourceDateTime the date and time of the original event
   * @param targetCalendarName the name of the calendar to copy the event to
   * @param targetDateTime the date and time for the copied event
   */
  public CopyEventCommand(CalendarDatabase calendarDatabase,
                          InCalendarView view,
                          String eventSubject,
                          LocalDateTime sourceDateTime,
                          String targetCalendarName,
                          LocalDateTime targetDateTime) {
    Objects.requireNonNull(calendarDatabase, "CalendarDatabase cannot be null");
    Objects.requireNonNull(view, "View cannot be null");
    Objects.requireNonNull(eventSubject, "Event subject cannot be null");
    Objects.requireNonNull(sourceDateTime, "Source date/time cannot be null");
    Objects.requireNonNull(targetCalendarName, "Target calendar name cannot be null");
    Objects.requireNonNull(targetDateTime, "Target date/time cannot be null");

    this.calendarDatabase = calendarDatabase;
    this.view = view;
    this.eventSubject = eventSubject.trim();
    this.sourceDateTime = sourceDateTime;
    this.targetCalendarName = targetCalendarName.trim();
    this.targetDateTime = targetDateTime;
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

    InEvent sourceEvent = findEventInReadOnlyCalendar(readOnlySource);
    if (sourceEvent == null) {
      throw new CalendarException(
          "Event not found: '" + eventSubject + "' at " + sourceDateTime);
    }

    InEvent copiedEvent = TimezoneUtil.copyEventToExactDateTime(sourceEvent, targetDateTime);

    try {
      targetCalendar.addEvent(copiedEvent);
      view.displaySuccess("Event copied: '" + eventSubject
          + "' from " + sourceCalendar.getCalendarName()
          + " to " + targetCalendarName);
    } catch (DuplicateEventException e) {
      throw new CalendarException(
          "Cannot copy event - duplicate already exists in target calendar", e);
    }
  }

  /**
   * Finds event in read-only calendar.
   * Demonstrates that read-only calendar allows reads but prevents modifications.
   */
  private InEvent findEventInReadOnlyCalendar(ReadOnlyCalendar readOnlyCalendar) {
    return readOnlyCalendar.getAllEvents().stream()
        .filter(e -> e.getSubject().equals(eventSubject)
            && e.getStartDateTime().equals(sourceDateTime))
        .findFirst()
        .orElse(null);
  }

  @Override
  public String getDescription() {
    return "Copy event '" + eventSubject + "' to calendar: " + targetCalendarName;
  }
}