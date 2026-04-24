package calendar.service;

import calendar.model.CalendarInterface;
import calendar.model.Event;
import calendar.model.exceptions.ConflictException;
import calendar.model.repository.EventRepository;
import calendar.utils.DateTimeUtil;
import calendar.utils.FileExportUtil;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * This is the BASE class for event logic, implementing the EventService
 * interface.
 * It holds the calendar data and all the complex logic for
 * creating, editing, and querying events.
 */
public class EventServiceImpl implements EventService {

  private static final DateTimeFormatter CSV_DATE_FORMAT =
      DateTimeFormatter.ofPattern("MM/dd/yyyy");
  private static final DateTimeFormatter CSV_TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm a");
  private static final DateTimeFormatter ICAL_DATE_TIME_FORMAT =
      DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
          .withZone(ZoneId.of("UTC"));
  protected final Map<String, CalendarInterface> calendars = new HashMap<>();
  protected String activeCalendarName = null;

  /**
   * Helper method to get the currently active calendar.
   *
   * @return The active Calendar object.
   * @throws IllegalStateException if no calendar is active.
   */
  protected CalendarInterface getActiveCalendar() {
    if (activeCalendarName == null) {
      throw new IllegalStateException("No calendar is in use. Please 'use calendar' first.");
    }
    return calendars.get(activeCalendarName);
  }

  @Override
  public void createEvent(String subject, String fromStr, String toStr,
                          String onStr, String description, String location, boolean isPrivate,
                          String repeats, Integer occurrences, String untilStr)
      throws ConflictException, IllegalArgumentException {

    ZoneId activeZone = getActiveCalendar().getTimezone();

    EventSeriesBuilder builder = new EventSeriesBuilder().zone(activeZone);
    builder.subject(subject)
        .description(description)
        .location(location)
        .isPrivate(isPrivate);

    if (onStr != null) {
      builder.startDate(DateTimeUtil.parseDate(onStr));
    } else {
      LocalDateTime start = DateTimeUtil.parseDateTime(fromStr);
      LocalDateTime end = DateTimeUtil.parseDateTime(toStr);
      builder.startDate(start.toLocalDate())
          .startTime(start.toLocalTime())
          .endDate(end.toLocalDate())
          .endTime(end.toLocalTime());
    }

    if (repeats != null && !repeats.isEmpty()) {
      if (occurrences != null && untilStr != null && !untilStr.isEmpty()) {
        throw new IllegalArgumentException("Cannot specify both 'for' and 'until'...");
      }
      if (occurrences == null && untilStr == null) {
        throw new IllegalArgumentException("Repeating event must specify 'for' or 'until'...");
      }
      builder.repeats(repeats);
      if (occurrences != null) {
        builder.forTimes(occurrences);
      } else if (untilStr != null && !untilStr.isEmpty()) {
        builder.until(DateTimeUtil.parseDate(untilStr));
      }
    }

    EventRepository repo = getActiveCalendar().getEventRepository();

    List<Event> newEvents = builder.build();
    for (Event event : newEvents) {
      repo.addEvent(event);
    }
  }

  @Override
  public void editEvent(String subject, String fromStr, String toStr,
                        String property, String newValueStr,
                        boolean singleEventUpdate, boolean updateAll) {

    ZoneId activeZone = getActiveCalendar().getTimezone();
    EventRepository repo = getActiveCalendar().getEventRepository();

    LocalDateTime startTimeLdt = DateTimeUtil.parseDateTime(fromStr);
    LocalDateTime endTimeLdt = (toStr != null) ? DateTimeUtil.parseDateTime(toStr) : null;

    Instant startInstant = startTimeLdt.atZone(activeZone).toInstant();
    Instant endInstant = (endTimeLdt != null) ? endTimeLdt.atZone(activeZone).toInstant() : null;

    if (singleEventUpdate && updateAll) {
      throw new IllegalArgumentException("Edit failed: singleEventUpdate and updateAll "
          + "cannot both be true.");
    }
    if (singleEventUpdate && endInstant == null) {
      throw new IllegalArgumentException("Edit failed: End time is required for "
          + "single event update.");
    }

    try {
      switch (property.toLowerCase()) {
        case "subject":
          handleEdit(repo, subject, startInstant, endInstant, Event::setSubject,
              newValueStr, singleEventUpdate, updateAll);
          break;
        case "start":
          Instant newStart = DateTimeUtil.parseDateTime(newValueStr).atZone(activeZone).toInstant();
          handleEdit(repo, subject, startInstant, endInstant, Event::setStart,
              newStart, singleEventUpdate, updateAll);
          break;
        case "end":
          Instant newEnd = DateTimeUtil.parseDateTime(newValueStr).atZone(activeZone).toInstant();
          handleEdit(repo, subject, startInstant, endInstant, Event::setEnd,
              newEnd, singleEventUpdate, updateAll);
          break;
        case "description":
          handleEdit(repo, subject, startInstant, endInstant, Event::setDescription,
              newValueStr, singleEventUpdate, updateAll);
          break;
        case "location":
          handleEdit(repo, subject, startInstant, endInstant, Event::setLocation,
              newValueStr, singleEventUpdate, updateAll);
          break;
        case "status":
        case "isprivate":
          handleEdit(repo, subject, startInstant, endInstant, Event::setPrivate,
              newValueStr.equalsIgnoreCase("private") || newValueStr.equalsIgnoreCase("true"),
              singleEventUpdate,
              updateAll);
          break;
        default:
          throw new IllegalArgumentException("Unknown property: " + property);
      }
    } catch (ClassCastException | DateTimeParseException e) {
      throw new IllegalArgumentException("Edit failed: Invalid value type for property '"
          + property + "'.", e);
    }
  }

  @Override
  public void updateEventTime(String subject, String currentStartStr, String newStartStr,
                              String newEndStr,
                              boolean singleEventUpdate, boolean updateAll) {
    ZoneId activeZone = getActiveCalendar().getTimezone();
    EventRepository repo = getActiveCalendar().getEventRepository();

    LocalDateTime currentStartLdt = DateTimeUtil.parseDateTime(currentStartStr);
    Instant currentStartInstant = currentStartLdt.atZone(activeZone).toInstant();

    LocalDateTime newStartLdt = DateTimeUtil.parseDateTime(newStartStr);
    Instant newStartInstant = newStartLdt.atZone(activeZone).toInstant();

    LocalDateTime newEndLdt = DateTimeUtil.parseDateTime(newEndStr);
    Instant newEndInstant = newEndLdt.atZone(activeZone).toInstant();

    if (singleEventUpdate && updateAll) {
      throw new IllegalArgumentException(
          "Edit failed: singleEventUpdate and updateAll cannot both be true.");
    }

    if (singleEventUpdate) {
      executeSingleEdit(repo, subject, currentStartInstant, null, (e, pair) -> {
        e.setStart(pair.getKey());
        e.setEnd(pair.getValue());
      }, new java.util.AbstractMap.SimpleEntry<>(newStartInstant, newEndInstant));
    } else {
      java.time.Duration startDelta =
          java.time.Duration.between(currentStartInstant, newStartInstant);
      java.time.Duration endDelta =
          java.time.Duration.between(currentStartInstant, newStartInstant);

      Event baseEvent = repo.findUniqueEvent(subject, currentStartInstant, null);
      if (baseEvent == null) {
        throw new IllegalArgumentException("Edit failed: Event not found.");
      }
      Instant currentEndInstant = baseEvent.getEnd();

      java.time.Duration startShift =
          java.time.Duration.between(currentStartInstant, newStartInstant);
      java.time.Duration endShift = java.time.Duration.between(currentEndInstant, newEndInstant);

      executeSeriesEdit(repo, subject, currentStartInstant, (e, shifts) -> {
        e.setStart(e.getStart().plus(shifts.getKey()));
        e.setEnd(e.getEnd().plus(shifts.getValue()));
      }, new java.util.AbstractMap.SimpleEntry<>(startShift, endShift), updateAll);
    }
  }

  @Override
  public List<Event> getEventsOn(LocalDate date) {
    ZoneId activeZone = getActiveCalendar().getTimezone();
    Instant startInstant = date.atStartOfDay(activeZone).toInstant();
    Instant endInstant = date.plusDays(1).atStartOfDay(activeZone).toInstant();
    return getActiveCalendar().getEventRepository().findEventsBetween(startInstant, endInstant);
  }

  @Override
  public List<Event> getEventsBetween(LocalDateTime start, LocalDateTime end) {
    if (end.isBefore(start)) {
      throw new IllegalArgumentException("'from' datetime must be before 'to' datetime.");
    }

    ZoneId activeZone = getActiveCalendar().getTimezone();
    Instant startInstant = start.atZone(activeZone).toInstant();
    Instant endInstant = end.atZone(activeZone).toInstant();

    return getActiveCalendar().getEventRepository().findEventsBetween(startInstant, endInstant);
  }

  @Override
  public boolean isBusy(LocalDateTime dateTime) {
    ZoneId activeZone = getActiveCalendar().getTimezone();
    Instant instant = dateTime.atZone(activeZone).toInstant();

    List<Event> events = getActiveCalendar().getEventRepository()
        .findEventsBetween(instant, instant.plusNanos(1));
    return !events.isEmpty();
  }

  @Override
  public String getCsvData() {
    StringBuilder csv = new StringBuilder();
    csv.append("Subject,Start Date,Start Time,End Date,"
        + "End Time,All Day Event,Description,Location,Private\n");

    ZoneId activeZone = getActiveCalendar().getTimezone();
    EventRepository repo = getActiveCalendar().getEventRepository();

    for (Event e : repo.getAllEvents()) {
      LocalDateTime startLdt = LocalDateTime.ofInstant(e.getStart(), activeZone);
      LocalDateTime endLdt = LocalDateTime.ofInstant(e.getEnd(), activeZone);

      boolean isAllDay = startLdt.toLocalTime().equals(LocalTime.of(8, 0))
          && endLdt.toLocalTime().equals(LocalTime.of(17, 0))
          && startLdt.toLocalDate().equals(endLdt.toLocalDate());

      csv.append(formatCsvField(e.getSubject())).append(",");
      csv.append(startLdt.format(CSV_DATE_FORMAT)).append(",");
      csv.append(startLdt.format(CSV_TIME_FORMAT)).append(",");
      csv.append(endLdt.format(CSV_DATE_FORMAT)).append(",");
      csv.append(endLdt.format(CSV_TIME_FORMAT)).append(",");
      csv.append(isAllDay ? "True" : "False").append(",");
      csv.append(formatCsvField(e.getDescription())).append(",");
      csv.append(formatCsvField(e.getLocation())).append(",");
      csv.append(e.isPrivate() ? "True" : "False").append("\n");
    }
    return csv.toString();
  }

  /**
   * Generates a string containing all event data in the iCalendar (.ical) format.
   *
   * @return A string representation of the calendar in iCal format.
   */
  private String getIcalData() {

    StringBuilder ical = new StringBuilder();
    ical.append("BEGIN:VCALENDAR\r\n");
    ical.append("VERSION:2.0\r\n");
    ical.append("PRODID:-//Gemini-Calendar-Project//EN\r\n");

    EventRepository repo = getActiveCalendar().getEventRepository();
    for (Event e : repo.getAllEvents()) {
      ical.append("BEGIN:VEVENT\r\n");

      ical.append("UID:").append(UUID.randomUUID()).append("\r\n");

      Instant now = Instant.now();
      ical.append("DTSTAMP:").append(ICAL_DATE_TIME_FORMAT.format(now)).append("\r\n");

      ical.append("DTSTART:").append(ICAL_DATE_TIME_FORMAT.format(e.getStart())).append("\r\n");
      ical.append("DTEND:").append(ICAL_DATE_TIME_FORMAT.format(e.getEnd())).append("\r\n");

      ical.append("SUMMARY:").append(e.getSubject()).append("\r\n");

      if (e.getDescription() != null && !e.getDescription().isEmpty()) {
        ical.append("DESCRIPTION:").append(e.getDescription()).append("\r\n");
      }

      if (e.getLocation() != null && !e.getLocation().isEmpty()) {
        ical.append("LOCATION:").append(e.getLocation()).append("\r\n");
      }

      ical.append("CLASS:").append(e.isPrivate() ? "PRIVATE" : "PUBLIC").append("\r\n");

      ical.append("END:VEVENT\r\n");
    }

    ical.append("END:VCALENDAR\r\n");
    return ical.toString();
  }

  @Override
  public String exportCalendar(String fileName) throws IOException, IllegalArgumentException {
    String data;

    if (fileName.endsWith(".csv")) {
      data = this.getCsvData();
    } else if (fileName.endsWith(".ical")) {
      data = this.getIcalData();
    } else {
      throw new IllegalArgumentException("Unsupported file format. "
          + "Please use '.csv' or '.ical'.");
    }

    return FileExportUtil.save(fileName, data);
  }

  private <T> void handleEdit(EventRepository repo,
                              String subject, Instant start, Instant end,
                              BiConsumer<Event, T> propertySetter, T newValue,
                              boolean single, boolean all) {
    if (single) {
      executeSingleEdit(repo, subject, start, end, propertySetter, newValue);
    } else {
      executeSeriesEdit(repo, subject, start, propertySetter, newValue, all);
    }
  }

  private <T> void executeSingleEdit(EventRepository repo,
                                     String subject, Instant start, Instant end,
                                     BiConsumer<Event, T> propertySetter, T newValue)
      throws IllegalArgumentException {
    Event event = repo.findUniqueEvent(subject, start, end);
    if (event == null) {
      throw new IllegalArgumentException("Edit failed: Event not found.");
    }

    Event newEvent = event.copy();
    try {
      propertySetter.accept(newEvent, newValue);

      checkConflict(repo, newEvent, event);
      repo.updateEvent(newEvent, event);
    } catch (Exception e) {
      throw new IllegalArgumentException("Edit failed: " + e.getMessage(), e);
    }
  }

  private <T> void executeSeriesEdit(EventRepository repo,
                                     String subject, Instant start,
                                     BiConsumer<Event, T> propertySetter, T newValue,
                                     boolean updateAll) throws IllegalArgumentException {
    Event baseEvent = repo.findUniqueEvent(subject, start, null);
    if (baseEvent == null) {
      throw new IllegalArgumentException("Edit failed: Event not found or criteria ambiguous.");
    }
    if (!baseEvent.isSeries()) {
      executeSingleEdit(repo, subject, start, baseEvent.getEnd(), propertySetter, newValue);
      return;
    }

    String seriesId = baseEvent.getSeriesId();
    List<Event> eventsToChange = new ArrayList<>();

    for (Event e : repo.getAllEvents()) {
      if (Objects.equals(e.getSeriesId(), seriesId)) {
        if (updateAll || !e.getStart().isBefore(baseEvent.getStart())) {
          eventsToChange.add(e);
        }
      }
    }

    List<Event> newEvents = eventsToChange.stream().map(Event::copy).collect(Collectors.toList());
    String newSeriesId = (!updateAll) ? UUID.randomUUID().toString() : null;

    try {
      for (Event eventToUpdate : newEvents) {
        propertySetter.accept(eventToUpdate, newValue);
        if (newSeriesId != null) {
          eventToUpdate.setSeriesId(newSeriesId);
        }
      }

      for (int i = 0; i < eventsToChange.size(); i++) {
        Event original = eventsToChange.get(i);
        Event newEvent = newEvents.get(i);

        checkConflict(repo, newEvent, original);
        repo.updateEvent(newEvent, original);
      }
    } catch (Exception e) {
      throw new IllegalArgumentException("Series edit failed: " + e.getMessage(), e);
    }
  }

  private void checkConflict(EventRepository repo, Event event, Event originalState)
      throws ConflictException {
    for (Event existing : repo.getAllEvents()) {
      if (existing == event) {
        continue;
      }
      if (existing.equals(originalState)) {
        continue;
      }
      if (existing.equals(event)) {
        throw new ConflictException("Event conflict: An event with the "
            + "same subject, start, and end time already exists.");
      }
    }
  }

  private void restoreEvent(Event eventToRestore, Event backup) {
    eventToRestore.setSubject(backup.getSubject());
    eventToRestore.setStart(backup.getStart());
    eventToRestore.setEnd(backup.getEnd());
    eventToRestore.setDescription(backup.getDescription());
    eventToRestore.setLocation(backup.getLocation());
    eventToRestore.setPrivate(backup.isPrivate());
    eventToRestore.setSeriesId(backup.getSeriesId());
  }

  private String formatCsvField(String field) {
    if (field == null || field.isEmpty()) {
      return "";
    }
    if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
      return "\"" + field.replace("\"", "\"\"") + "\"";
    }
    return field;
  }

  @Override
  public Event findUniqueEvent(String subject, LocalDateTime start) {
    CalendarInterface activeCal = getActiveCalendar();
    ZoneId zone = activeCal.getTimezone();
    EventRepository repo = activeCal.getEventRepository();

    Instant startInstant = start.atZone(zone).toInstant();

    List<Event> eventsAtInstant = repo.findEventsBetween(startInstant, startInstant.plusNanos(1));

    for (Event e : eventsAtInstant) {
      if (e.getSubject().equals(subject)) {
        return e;
      }
    }
    return null;
  }
}