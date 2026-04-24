package calendar.model;

import java.io.IOException;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Top-level model that manages multiple named calendars. */
public class CalendarModelImpl implements CalendarModel {

  private final Map<String, SingleCalendar> calendars;
  private String activeCalendarName;

  /** Creates a model with no calendars configured. */
  public CalendarModelImpl() {
    this.calendars = new LinkedHashMap<>();
  }

  @Override
  public void createCalendar(String name, ZoneId zoneId) {
    validateName(name);
    Objects.requireNonNull(zoneId, "Timezone cannot be null.");
    if (calendars.containsKey(name)) {
      throw new IllegalArgumentException("Calendar name must be unique.");
    }
    calendars.put(name, new SingleCalendar(name, zoneId));
  }

  @Override
  public void renameCalendar(String currentName, String newName) {
    validateName(currentName);
    validateName(newName);
    if (!calendars.containsKey(currentName)) {
      throw new IllegalArgumentException("Calendar '" + currentName + "' does not exist.");
    }
    if (calendars.containsKey(newName)) {
      throw new IllegalArgumentException("Calendar name must be unique.");
    }
    SingleCalendar calendar = calendars.remove(currentName);
    calendar.setName(newName);
    calendars.put(newName, calendar);
    if (currentName.equals(activeCalendarName)) {
      activeCalendarName = newName;
    }
  }

  @Override
  public void changeCalendarTimezone(String calendarName, ZoneId zoneId) {
    Objects.requireNonNull(zoneId, "Timezone cannot be null.");
    SingleCalendar calendar = getCalendar(calendarName);
    calendar.shiftToTimezone(zoneId);
  }

  @Override
  public void useCalendar(String name) {
    SingleCalendar calendar = getCalendar(name);
    this.activeCalendarName = name;
  }

  @Override
  public boolean hasActiveCalendar() {
    return activeCalendarName != null;
  }

  @Override
  public String getActiveCalendarName() {
    return activeCalendarName;
  }

  @Override
  public ZoneId getActiveCalendarZone() {
    return requireActive().getZoneId();
  }

  @Override
  public List<String> listCalendars() {
    return new ArrayList<>(calendars.keySet());
  }

  @Override
  public CalendarEvent createEvent(
      String subject, LocalDateTime start, LocalDateTime end, boolean allDay) {
    return requireActive().createEvent(subject, start, end, allDay);
  }

  @Override
  public List<CalendarEvent> createRecurringEventsByCount(
      String subject,
      LocalDateTime start,
      LocalDateTime end,
      boolean allDay,
      Set<DayOfWeek> weekdays,
      int occurrences) {
    return requireActive()
        .createRecurringEventsByCount(subject, start, end, allDay, weekdays, occurrences);
  }

  @Override
  public List<CalendarEvent> createRecurringEventsUntil(
      String subject,
      LocalDateTime start,
      LocalDateTime end,
      boolean allDay,
      Set<DayOfWeek> weekdays,
      LocalDate until) {
    return requireActive().createRecurringEventsUntil(subject, start, end, allDay, weekdays, until);
  }

  @Override
  public CalendarEvent editSingleEvent(
      String subject,
      LocalDateTime start,
      LocalDateTime end,
      EventProperty property,
      Object newValue) {
    return requireActive().editSingleEvent(subject, start, end, property, newValue);
  }

  @Override
  public List<CalendarEvent> editEventsFrom(
      String subject, LocalDateTime start, EventProperty property, Object newValue) {
    return requireActive().editEventsFrom(subject, start, property, newValue);
  }

  @Override
  public List<CalendarEvent> editEntireSeries(
      String subject, LocalDateTime start, EventProperty property, Object newValue) {
    return requireActive().editEntireSeries(subject, start, property, newValue);
  }

  @Override
  public List<CalendarEvent> eventsOn(LocalDate date) {
    return requireActive().eventsOn(date);
  }

  @Override
  public List<CalendarEvent> eventsBetween(LocalDateTime start, LocalDateTime end) {
    return requireActive().eventsBetween(start, end);
  }

  @Override
  public boolean isBusy(LocalDateTime moment) {
    return requireActive().isBusy(moment);
  }

  @Override
  public CalendarEvent copyEvent(
      String subject, LocalDateTime start, String targetCalendarName, LocalDateTime targetStart) {
    Objects.requireNonNull(targetCalendarName, "Target calendar required.");
    Objects.requireNonNull(targetStart, "Target start required.");
    SingleCalendar source = requireActive();
    SingleCalendar target = getCalendar(targetCalendarName);
    CalendarEvent original = source.findUniqueBySubjectAndStart(subject, start);
    Duration duration = Duration.between(original.getStart(), original.getEnd());
    Map<String, String> seriesMapping = new LinkedHashMap<>();
    String newSeriesId = mapSeriesId(original, source, target, seriesMapping);
    return target.addClonedEvent(original, targetStart, targetStart.plus(duration), newSeriesId);
  }

  @Override
  public List<CalendarEvent> copyEventsOn(
      LocalDate sourceDate, String targetCalendarName, LocalDate targetDate) {
    Objects.requireNonNull(sourceDate, "Source date required.");
    Objects.requireNonNull(targetCalendarName, "Target calendar required.");
    Objects.requireNonNull(targetDate, "Target date required.");
    SingleCalendar source = requireActive();
    SingleCalendar target = getCalendar(targetCalendarName);
    List<CalendarEvent> events = source.eventsOn(sourceDate);
    return copyEventsWithOffsets(events, source, target, sourceDate, targetDate);
  }

  @Override
  public List<CalendarEvent> copyEventsBetween(
      LocalDate startDate,
      LocalDate endDate,
      String targetCalendarName,
      LocalDate targetStartDate) {
    Objects.requireNonNull(startDate, "Start date required.");
    Objects.requireNonNull(endDate, "End date required.");
    Objects.requireNonNull(targetCalendarName, "Target calendar required.");
    Objects.requireNonNull(targetStartDate, "Target start date required.");
    if (endDate.isBefore(startDate)) {
      throw new IllegalArgumentException("End date must not be before start date.");
    }
    SingleCalendar source = requireActive();
    SingleCalendar target = getCalendar(targetCalendarName);
    LocalDateTime intervalStart = startDate.atStartOfDay();
    LocalDateTime intervalEnd = endDate.plusDays(1).atStartOfDay().minusSeconds(1);
    List<CalendarEvent> events = source.eventsBetween(intervalStart, intervalEnd);
    List<CalendarEvent> expanded = expandSeries(events, source);
    return copyEventsWithOffsets(expanded, source, target, startDate, targetStartDate);
  }

  @Override
  public Path exportCalendar(Path outputFile) throws IOException {
    Objects.requireNonNull(outputFile, "Output file required.");
    SingleCalendar calendar = requireActive();
    String lower = outputFile.getFileName().toString().toLowerCase(Locale.US);
    if (lower.endsWith(".csv")) {
      return calendar.exportToCsv(outputFile);
    } else if (lower.endsWith(".ical") || lower.endsWith(".ics")) {
      return calendar.exportToIcs(outputFile);
    }
    throw new IllegalArgumentException("Unsupported export format: " + outputFile);
  }

  private List<CalendarEvent> expandSeries(List<CalendarEvent> events, SingleCalendar source) {
    if (events.isEmpty()) {
      return events;
    }
    List<CalendarEvent> expanded = new ArrayList<>(events);
    Set<String> seenIds = new HashSet<>();
    for (CalendarEvent event : events) {
      seenIds.add(event.getId());
    }
    Set<String> processedSeries = new HashSet<>();
    for (CalendarEvent event : events) {
      event
          .getSeriesId()
          .ifPresent(
              seriesId -> {
                if (processedSeries.add(seriesId)) {
                  for (CalendarEvent seriesEvent : source.eventsBySeries(seriesId)) {
                    if (seenIds.add(seriesEvent.getId())) {
                      expanded.add(seriesEvent);
                    }
                  }
                }
              });
    }
    expanded.sort(null);
    return expanded;
  }

  private List<CalendarEvent> copyEventsWithOffsets(
      List<CalendarEvent> events,
      SingleCalendar source,
      SingleCalendar target,
      LocalDate sourceBase,
      LocalDate targetBase) {
    List<CalendarEvent> created = new ArrayList<>();
    Map<String, String> seriesMapping = new LinkedHashMap<>();
    for (CalendarEvent event : events) {
      long dayOffset = ChronoUnit.DAYS.between(sourceBase, event.getStart().toLocalDate());
      LocalDateTime newStart = alignEventStart(event, source, target, targetBase, dayOffset);
      Duration duration = Duration.between(event.getStart(), event.getEnd());
      String newSeriesId = mapSeriesId(event, source, target, seriesMapping);
      created.add(target.addClonedEvent(event, newStart, newStart.plus(duration), newSeriesId));
    }
    return created;
  }

  private LocalDateTime alignEventStart(
      CalendarEvent event,
      SingleCalendar source,
      SingleCalendar target,
      LocalDate targetBase,
      long dayOffset) {
    ZonedDateTime sourceStart = event.getStart().atZone(source.getZoneId());
    ZonedDateTime converted = sourceStart.withZoneSameInstant(target.getZoneId());
    LocalDate targetDate = targetBase.plusDays(dayOffset);
    return LocalDateTime.of(targetDate, converted.toLocalTime());
  }

  private String mapSeriesId(
      CalendarEvent event,
      SingleCalendar source,
      SingleCalendar target,
      Map<String, String> mapping) {
    return event
        .getSeriesId()
        .map(
            seriesId -> {
              if (mapping.containsKey(seriesId)) {
                return mapping.get(seriesId);
              }
              EventSeries metadata = source.getSeriesMetadata(seriesId);
              if (metadata == null) {
                mapping.put(seriesId, null);
                return null;
              }
              String newId = target.cloneSeries(metadata);
              mapping.put(seriesId, newId);
              return newId;
            })
        .orElse(null);
  }

  private SingleCalendar getCalendar(String name) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be empty.");
    }
    SingleCalendar calendar = calendars.get(name);
    if (calendar == null) {
      throw new IllegalArgumentException("Calendar '" + name + "' does not exist.");
    }
    return calendar;
  }

  private SingleCalendar requireActive() {
    if (!hasActiveCalendar()) {
      throw new IllegalStateException("No calendar is currently in use.");
    }
    return calendars.get(activeCalendarName);
  }

  private void validateName(String name) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be empty.");
    }
  }
}
