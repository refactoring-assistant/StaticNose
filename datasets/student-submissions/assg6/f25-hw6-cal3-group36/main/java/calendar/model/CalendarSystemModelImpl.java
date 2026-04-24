package calendar.model;

import calendar.export.CsvExporter;
import calendar.export.EventExporter;
import calendar.export.IcalExporter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the CalendarSystemModel interface.
 * Maintains multiple named calendars and an active calendar selection.
 * Event operations are applied to the currently active calendar.
 */
public class CalendarSystemModelImpl implements CalendarSystemModel {

  private final Map<String, CalendarModel> calendars;
  private CalendarModel activeCalendar;

  /**
   * Constructs a new calendar system model with no calendars.
   * No active calendar is selected initially.
   */
  public CalendarSystemModelImpl() {
    this.calendars = new HashMap<String, CalendarModel>();
    this.activeCalendar = null;
  }

  /**
   * Creates a new calendar with the given name and time zone.
   *
   * @param name     calendar name; must be non-blank and unique
   * @param timezone IANA time zone ID string
   * @throws IllegalArgumentException if the name is null, blank, duplicate,
   *                                  or if the time zone string is invalid
   */
  @Override
  public void createCalendar(String name, String timezone) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Calendar name cannot be empty.");
    }
    if (calendars.containsKey(name)) {
      throw new IllegalArgumentException("Calendar already exists: " + name);
    }
    ZoneId zone = ZoneId.of(timezone);
    calendars.put(name, new CalendarModel(name, zone));
  }

  /**
   * Renames an existing calendar.
   * The underlying CalendarModel instance is kept but re-registered
   * under the new name. If the renamed calendar is currently active,
   * the active calendar reference is cleared and must be set again.
   *
   * @param oldName existing calendar name
   * @param newName new calendar name, which must not already exist
   * @throws IllegalArgumentException if the old calendar does not exist
   *                                  or the new name is already in use
   */
  @Override
  public void renameCalendar(String oldName, String newName) {
    CalendarModel model = calendars.get(oldName);
    if (model == null) {
      throw new IllegalArgumentException("Calendar not found: " + oldName);
    }
    if (calendars.containsKey(newName)) {
      throw new IllegalArgumentException("Calendar name already exists: " + newName);
    }
    calendars.remove(oldName);
    model.setName(newName);
    calendars.put(newName, model);
    if (activeCalendar == model) {
      activeCalendar = null;
    }
  }

  /**
   * Changes the time zone of the specified calendar.
   *
   * @param calendarName name of the calendar to update
   * @param timezone     new IANA time zone ID string
   * @throws IllegalArgumentException if the calendar does not exist
   *                                  or if the time zone ID is invalid
   */
  @Override
  public void changeCalendarTimezone(String calendarName, String timezone) {
    CalendarModel model = calendars.get(calendarName);
    if (model == null) {
      throw new IllegalArgumentException("Calendar not found: " + calendarName);
    }
    model.setTimeZone(ZoneId.of(timezone));
  }

  /**
   * Selects the active calendar by name.
   * Many operations require that an active calendar has been selected.
   *
   * @param calendarName name of the calendar to make active
   * @throws IllegalArgumentException if no calendar exists with that name
   */
  @Override
  public void useCalendar(String calendarName) {
    CalendarModel model = calendars.get(calendarName);
    if (model == null) {
      throw new IllegalArgumentException("No such calendar: " + calendarName);
    }
    activeCalendar = model;
  }

  /**
   * Adds a single event to the active calendar.
   * The event is rejected if it conflicts with an existing event
   * as determined by the conflicts method.
   *
   * @param event event to add
   * @throws IllegalArgumentException if there is no active calendar
   *                                  or if the event conflicts with an existing one
   */
  @Override
  public void createEvent(InterfaceEvent event) {
    ensureActive();
    if (conflicts(event, activeCalendar.getEvents())) {
      throw new IllegalArgumentException("Event conflicts with existing one.");
    }
    activeCalendar.addEvent(event);
  }

  /**
   * Creates a series of recurring events based on a template and a recurrence rule.
   * The method walks forward day by day from the template's start. For each date
   * whose day of week appears in the recurrence rule's list of days, a new event
   * is created with the same subject, description, zone, and duration as the
   * template. The process stops when either the repeat count is reached (for
   * count-based rules) or a date past the until date is reached (for until-based
   * rules). Events that would conflict with existing events are skipped.
   *
   * @param template template event used to define subject, duration, and metadata
   * @param rule     recurrence rule defining days of week and termination condition
   * @throws IllegalArgumentException if there is no active calendar
   */
  @Override
  public void createRecurringEvent(InterfaceEvent template, RecurrenceRule rule) {
    ensureActive();
    List<DayOfWeek> days = rule.getDays();
    int created = 0;

    ZonedDateTime current = ZonedDateTime.ofInstant(
        template.getStart(), template.getZone()).withZoneSameInstant(template.getZone());

    while (true) {
      if (rule.isCountBased() && created >= rule.getCount()) {
        break;
      }
      if (rule.isUntilBased()) {
        LocalDate until = LocalDate.parse(rule.getUntilDate());
        if (current.toLocalDate().isAfter(until)) {
          break;
        }
      }

      if (days.contains(current.getDayOfWeek())) {
        Instant start = current.toInstant();
        Instant end = start.plusSeconds(template.getDurationInSeconds());
        InterfaceEvent newEvent = new Event(
            template.getSubject(),
            start,
            end,
            template.getDescription(),
            template.getLocation(),
            template.isPublicEvent(),
            template.getZone()
        );
        if (!conflicts(newEvent, activeCalendar.getEvents())) {
          activeCalendar.addEvent(newEvent);
          created++;
        }
      }
      current = current.plusDays(1);
    }
  }

  /**
   * Replaces a single event in the active calendar with a new event.
   * The event to replace is located by subject and start time. The old
   * event is removed, and the replacement is checked for conflicts. If
   * a conflict is detected, the original event is restored and an
   * exception is thrown.
   *
   * @param subject     subject of the event to edit
   * @param start       original start instant of the event
   * @param replacement new event that should replace the original
   * @throws IllegalArgumentException if there is no active calendar,
   *                                  if the original event is not found,
   *                                  or if the replacement conflicts
   */
  @Override
  public void editEvent(String subject, Instant start, InterfaceEvent replacement) {
    ensureActive();
    InterfaceEvent existing = activeCalendar.findEvent(subject, start);
    if (existing == null) {
      throw new IllegalArgumentException("Event not found: " + subject);
    }
    activeCalendar.removeEvent(existing);
    if (conflicts(replacement, activeCalendar.getEvents())) {
      activeCalendar.addEvent(existing);
      throw new IllegalArgumentException("Replacement conflicts with another event.");
    }
    activeCalendar.addEvent(replacement);
  }

  /**
   * Edits a series of events that share a common subject.
   * The method supports three modes, based on the flags:
   * - If affectAll is true, all events in the series are replaced using
   *   the replacement event's metadata and duration, but keeping each
   *   occurrence's original start time.
   * - If affectFuture is true and affectAll is false, all events with
   *   start times at or after the pivot event's start time are replaced.
   * - If both flags are false, this behaves like a single event edit
   *   and delegates to editEvent.
   *
   * @param subject      subject shared by the event series
   * @param start        start instant of the pivot event
   * @param replacement  replacement event used as a template
   * @param affectFuture true to affect this event and future ones in the series
   * @param affectAll    true to affect every event in the series
   * @throws IllegalArgumentException if there is no active calendar,
   *                                  if the base event cannot be found, or
   *                                  if the delegated single edit fails
   */
  @Override
  public void editSeries(String subject, Instant start, InterfaceEvent replacement,
                         boolean affectFuture, boolean affectAll) {
    ensureActive();
    List<InterfaceEvent> series = activeCalendar.findSeries(subject);
    if (series.isEmpty()) {
      editEvent(subject, start, replacement);
      return;
    }

    InterfaceEvent pivot = activeCalendar.findEvent(subject, start);
    if (pivot == null) {
      throw new IllegalArgumentException("Base event not found for series edit.");
    }

    Instant pivotStart = pivot.getStart();

    if (affectAll) {
      for (InterfaceEvent e : new ArrayList<InterfaceEvent>(series)) {
        activeCalendar.removeEvent(e);
      }
      for (InterfaceEvent e : series) {
        InterfaceEvent copy = new Event(
            replacement.getSubject(),
            e.getStart(),
            e.getStart().plusSeconds(replacement.getDurationInSeconds()),
            replacement.getDescription(),
            replacement.getLocation(),
            replacement.isPublicEvent(),
            replacement.getZone()
        );
        activeCalendar.addEvent(copy);
      }
    } else if (affectFuture) {
      activeCalendar.removeSeriesFrom(subject, pivotStart);
      for (InterfaceEvent e : series) {
        if (!e.getStart().isBefore(pivotStart)) {
          InterfaceEvent copy = new Event(
              replacement.getSubject(),
              e.getStart(),
              e.getStart().plusSeconds(replacement.getDurationInSeconds()),
              replacement.getDescription(),
              replacement.getLocation(),
              replacement.isPublicEvent(),
              replacement.getZone()
          );
          activeCalendar.addEvent(copy);
        }
      }
    } else {
      editEvent(subject, start, replacement);
    }
  }

  /**
   * Copies a single event from the active calendar into a target calendar.
   * The event to copy is identified by subject and start time. The copy
   * uses the target calendar's time zone and the supplied new start time,
   * preserving the original event's duration and metadata.
   *
   * @param subject        subject of the source event
   * @param start          start instant of the source event
   * @param targetCalendar name of the target calendar
   * @param newStart       start instant to use for the copied event
   * @throws IllegalArgumentException if there is no active calendar,
   *                                  if the target calendar does not exist,
   *                                  if the source event is not found,
   *                                  or if the new event conflicts in the target
   */
  @Override
  public void copyEvent(String subject, Instant start,
                        String targetCalendar, Instant newStart) {
    ensureActive();
    CalendarModel target = calendars.get(targetCalendar);
    if (target == null) {
      throw new IllegalArgumentException("Target calendar not found: " + targetCalendar);
    }

    InterfaceEvent src = activeCalendar.findEvent(subject, start);
    if (src == null) {
      throw new IllegalArgumentException("Source event not found: " + subject);
    }

    Instant newEnd = newStart.plusSeconds(src.getDurationInSeconds());
    InterfaceEvent copy = new Event(
        src.getSubject(),
        newStart,
        newEnd,
        src.getDescription(),
        src.getLocation(),
        src.isPublicEvent(),
        target.getTimeZone()
    );

    if (conflicts(copy, target.getEvents())) {
      throw new IllegalArgumentException("Conflict in target calendar.");
    }
    target.addEvent(copy);
  }

  /**
   * Copies all events on a given date from the active calendar to a target calendar,
   * shifting them to a new date.
   * The time shift is computed as the day difference between date and targetDate.
   * For each event on the source date, start and end instants are shifted by the
   * same number of days.
   * Events that would conflict in the target calendar are skipped.
   *
   * @param date           date in the active calendar to copy events from
   * @param targetCalendar target calendar name
   * @param targetDate     date in the target calendar where events should land
   * @throws IllegalArgumentException if there is no active calendar
   *                                  or if the target calendar does not exist
   */
  @Override
  public void copyEventsOn(LocalDate date, String targetCalendar, LocalDate targetDate) {
    ensureActive();
    CalendarModel target = calendars.get(targetCalendar);
    if (target == null) {
      throw new IllegalArgumentException("Target calendar not found: " + targetCalendar);
    }

    List<InterfaceEvent> toCopy = activeCalendar.getEventsOn(date);
    long offsetDays = Duration.between(
        date.atStartOfDay(), targetDate.atStartOfDay()).toDays();

    for (InterfaceEvent e : toCopy) {
      Instant newStart = e.getStart().plusSeconds(offsetDays * 86400);
      Instant newEnd = e.getEnd().plusSeconds(offsetDays * 86400);

      InterfaceEvent newEvent = new Event(
          e.getSubject(),
          newStart,
          newEnd,
          e.getDescription(),
          e.getLocation(),
          e.isPublicEvent(),
          target.getTimeZone()
      );
      if (!conflicts(newEvent, target.getEvents())) {
        target.addEvent(newEvent);
      }
    }
  }

  /**
   * Copies all events between two dates (inclusive) from the active calendar
   * to a target calendar, shifting the entire date range to start at targetStart.
   * The day offset is computed between start and targetStart. Each event's start
   * and end instants are shifted by that many days. Events that would conflict in
   * the target calendar are skipped.
   *
   * @param start          first date (inclusive) in the source range
   * @param end            last date (inclusive) in the source range
   * @param targetCalendar target calendar name
   * @param targetStart    date in the target calendar that corresponds to start
   * @throws IllegalArgumentException if there is no active calendar
   *                                  or if the target calendar does not exist
   */
  @Override
  public void copyEventsBetween(LocalDate start, LocalDate end,
                                String targetCalendar, LocalDate targetStart) {
    ensureActive();
    CalendarModel target = calendars.get(targetCalendar);
    if (target == null) {
      throw new IllegalArgumentException("Target calendar not found: " + targetCalendar);
    }

    List<InterfaceEvent> toCopy = activeCalendar.getEventsBetween(start, end);
    long offsetDays = Duration.between(
        start.atStartOfDay(), targetStart.atStartOfDay()).toDays();

    for (InterfaceEvent e : toCopy) {
      Instant newStart = e.getStart().plusSeconds(offsetDays * 86400);
      Instant newEnd = e.getEnd().plusSeconds(offsetDays * 86400);
      InterfaceEvent newEvent = new Event(
          e.getSubject(),
          newStart,
          newEnd,
          e.getDescription(),
          e.getLocation(),
          e.isPublicEvent(),
          target.getTimeZone()
      );
      if (!conflicts(newEvent, target.getEvents())) {
        target.addEvent(newEvent);
      }
    }
  }

  /**
   * Returns the active calendar.
   *
   * @return active CalendarModel instance
   * @throws IllegalArgumentException if no active calendar is selected
   */
  @Override
  public CalendarModel getActiveCalendar() {
    ensureActive();
    return activeCalendar;
  }

  /**
   * Returns the time zone of the named calendar.
   *
   * @param calendarName calendar name
   * @return zone ID for the calendar
   * @throws IllegalArgumentException if the calendar does not exist
   */
  @Override
  public ZoneId getCalendarTimeZone(String calendarName) {
    CalendarModel model = calendars.get(calendarName);
    if (model == null) {
      throw new IllegalArgumentException("No such calendar: " + calendarName);
    }
    return model.getTimeZone();
  }

  /**
   * Checks whether a new event conflicts with a list of existing events.
   * Conflict is defined here as an existing event with the same subject,
   * the same start instant, and the same end instant.
   *
   * @param newEvent       event to test
   * @param existingEvents events to compare against
   * @return true if a conflict is detected, false otherwise
   */
  private boolean conflicts(InterfaceEvent newEvent, List<InterfaceEvent> existingEvents) {
    for (InterfaceEvent e : existingEvents) {
      boolean sameSubject = e.getSubject().equals(newEvent.getSubject());
      boolean sameStart = e.getStart().equals(newEvent.getStart());
      boolean sameEnd = e.getEnd().equals(newEvent.getEnd());
      if (sameSubject && sameStart && sameEnd) {
        return true;
      }
    }
    return false;
  }

  /**
   * Ensures that an active calendar has been selected.
   *
   * @throws IllegalArgumentException if activeCalendar is null
   */
  private void ensureActive() {
    if (activeCalendar == null) {
      throw new IllegalArgumentException("No active calendar selected.");
    }
  }

  /**
   * Exports the active calendar to a CSV file using CsvExporter.
   * The file name may be relative or absolute. Relative paths are
   * resolved against the current working directory.
   *
   * @param fileName target file path
   * @return absolute path string of the created CSV file
   * @throws IllegalArgumentException if no active calendar is selected
   * @throws IllegalStateException    if writing the file fails
   */
  @Override
  public String exportCsv(String fileName) {
    ensureActive();
    Path path = resolvePath(fileName);

    try (BufferedWriter w = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
      EventExporter exporter = new CsvExporter();
      exporter.export(activeCalendar, w);
    } catch (IOException ioe) {
      throw new IllegalStateException("Could not export calendar to CSV: "
          + ioe.getMessage(), ioe);
    }

    return path.toAbsolutePath().toString();
  }

  /**
   * Exports the active calendar to an iCalendar (ICS) file using IcalExporter.
   * The file name may be relative or absolute. Relative paths are
   * resolved against the current working directory.
   *
   * @param fileName target file path
   * @return absolute path string of the created iCal file
   * @throws IllegalArgumentException if no active calendar is selected
   * @throws IllegalStateException    if writing the file fails
   */
  @Override
  public String exportIcal(String fileName) {
    ensureActive();
    Path path = resolvePath(fileName);

    try (BufferedWriter w = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
      EventExporter exporter = new IcalExporter();
      exporter.export(activeCalendar, w);
    } catch (IOException ioe) {
      throw new IllegalStateException("Could not export calendar to iCal: "
          + ioe.getMessage(), ioe);
    }

    return path.toAbsolutePath().toString();
  }

  /**
   * Resolves a file name to an absolute path.
   * If the provided path is already absolute, it is returned unchanged.
   * or else it is converted to an absolute path based on the current
   * working directory.
   *
   * @param fileName file name or relative path
   * @return absolute path
   */
  private Path resolvePath(String fileName) {
    Path path = Paths.get(fileName);
    if (!path.isAbsolute()) {
      path = path.toAbsolutePath();
    }
    return path;
  }
}
