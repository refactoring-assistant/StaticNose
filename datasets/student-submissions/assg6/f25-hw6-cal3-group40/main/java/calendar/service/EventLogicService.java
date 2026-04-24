package calendar.service;

import calendar.model.CalendarInterface;
import calendar.model.Event;
import calendar.model.exceptions.ConflictException;
import calendar.model.repository.EventRepository;
import calendar.utils.DateTimeUtil;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * This class encapsulates all business logic for event manipulation.
 * It operates on a *single* calendar's repository and timezone,
 * which are passed into its methods.
 */
public class EventLogicService {

  private static final DateTimeFormatter CSV_DATE_FORMAT =
      DateTimeFormatter.ofPattern("MM/dd/yyyy");
  private static final DateTimeFormatter CSV_TIME_FORMAT =
      DateTimeFormatter.ofPattern("hh:mm a");

  /**
   * Creates a new event or event series for the given calendar.
   */
  public void createEvent(CalendarInterface activeCalendar, EventCreationRequest request)
      throws ConflictException, IllegalArgumentException {

    ZoneId activeZone = activeCalendar.getTimezone();

    EventSeriesBuilder builder = new EventSeriesBuilder().zone(activeZone);
    builder.subject(request.getSubject())
        .description(request.getDescription())
        .location(request.getLocation())
        .isPrivate(request.isPrivate());

    if (request.getOnStr() != null) {
      builder.startDate(DateTimeUtil.parseDate(request.getOnStr()));
    } else {
      LocalDateTime start = DateTimeUtil.parseDateTime(request.getFromStr());
      LocalDateTime end = DateTimeUtil.parseDateTime(request.getToStr());
      builder.startDate(start.toLocalDate())
          .startTime(start.toLocalTime())
          .endDate(end.toLocalDate())
          .endTime(end.toLocalTime());
    }

    if (request.getRepeats() != null) {
      if (request.getOccurrences() != null && request.getUntilStr() != null) {
        throw new IllegalArgumentException("Cannot specify both 'for' and 'until'...");
      }
      if (request.getOccurrences() == null && request.getUntilStr() == null) {
        throw new IllegalArgumentException("Repeating event must specify 'for' or 'until'...");
      }
      builder.repeats(request.getRepeats());
      if (request.getOccurrences() != null) {
        builder.forTimes(request.getOccurrences());
      } else {
        builder.until(DateTimeUtil.parseDate(request.getUntilStr()));
      }
    }

    EventRepository repo = activeCalendar.getEventRepository();
    List<Event> newEvents = builder.build();
    for (Event event : newEvents) {
      repo.addEvent(event);
    }
  }

  /**
   * Edits an existing event or event series in the given calendar.
   */
  public void editEvent(CalendarInterface activeCalendar, String subject,
      String fromStr, String toStr,
      String property, String newValueStr,
      boolean singleEventUpdate, boolean updateAll) {

    ZoneId activeZone = activeCalendar.getTimezone();
    EventRepository repo = activeCalendar.getEventRepository();

    LocalDateTime startTimeLdt = DateTimeUtil.parseDateTime(fromStr);
    Instant startInstant = startTimeLdt.atZone(activeZone).toInstant();
    LocalDateTime endTimeLdt = (toStr != null) ? DateTimeUtil.parseDateTime(toStr) : null;
    Instant endInstant = (endTimeLdt != null) ? endTimeLdt.atZone(activeZone).toInstant() : null;

    validateEditRequest(singleEventUpdate, updateAll, endInstant);

    try {
      dispatchEdit(repo, subject, startInstant, endInstant, property, newValueStr,
          singleEventUpdate, updateAll, activeZone);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Edit failed: Invalid value type for property '"
          + property + "'.", e);
    }
  }

  private void validateEditRequest(boolean singleEventUpdate, boolean updateAll,
      Instant endInstant) {
    if (singleEventUpdate && updateAll) {
      throw new IllegalArgumentException("Edit failed: singleEventUpdate and updateAll "
          + "cannot both be true.");
    }
    if (singleEventUpdate && endInstant == null) {
      throw new IllegalArgumentException("Edit failed: End time is required for single "
          + "event update.");
    }
  }

  private void dispatchEdit(EventRepository repo, String subject, Instant startInstant,
      Instant endInstant, String property, String newValueStr,
      boolean singleEventUpdate, boolean updateAll, ZoneId activeZone) {
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
        handleEdit(repo, subject, startInstant, endInstant, Event::setPrivate,
            newValueStr.equalsIgnoreCase("private"), singleEventUpdate, updateAll);
        break;
      default:
        throw new IllegalArgumentException("Unknown property: " + property);
    }
  }

  /**
   * Retrieves events on a specific local date from the given calendar.
   */
  public List<Event> getEventsOn(CalendarInterface activeCalendar, LocalDate date) {
    ZoneId activeZone = activeCalendar.getTimezone();
    Instant startInstant = date.atStartOfDay(activeZone).toInstant();
    Instant endInstant = date.plusDays(1).atStartOfDay(activeZone).toInstant();
    return activeCalendar.getEventRepository().findEventsBetween(startInstant, endInstant);
  }

  /**
   * Retrieves events between two local date/times from the given calendar.
   */
  public List<Event> getEventsBetween(CalendarInterface activeCalendar, LocalDateTime start,
      LocalDateTime end) {
    if (end.isBefore(start)) {
      throw new IllegalArgumentException("'from' datetime must be before 'to' datetime.");
    }

    ZoneId activeZone = activeCalendar.getTimezone();
    Instant startInstant = start.atZone(activeZone).toInstant();
    Instant endInstant = end.atZone(activeZone).toInstant();

    return activeCalendar.getEventRepository().findEventsBetween(startInstant, endInstant);
  }

  /**
   * Checks if the given calendar is busy at a specific local date/time.
   */
  public boolean isBusy(CalendarInterface activeCalendar, LocalDateTime dateTime) {
    ZoneId activeZone = activeCalendar.getTimezone();
    Instant instant = dateTime.atZone(activeZone).toInstant();

    List<Event> events = activeCalendar.getEventRepository()
        .findEventsBetween(instant, instant.plusNanos(1));
    return !events.isEmpty();
  }

  /**
   * Generates a CSV string of all events in the given calendar.
   */
  public String getCsvData(CalendarInterface activeCalendar) {
    StringBuilder csv = new StringBuilder();
    csv.append("Subject,Start Date,Start Time,End Date,"
        + "End Time,All Day Event,Description,Location,Private\n");

    ZoneId activeZone = activeCalendar.getTimezone();
    EventRepository repo = activeCalendar.getEventRepository();

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

    Event backup = event.copy();
    try {
      propertySetter.accept(event, newValue);
      checkConflict(repo, event, backup);
      repo.updateEvent(event, backup);
    } catch (Exception e) {
      restoreEvent(event, backup);
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

    List<Event> backups = eventsToChange.stream().map(Event::copy).collect(Collectors.toList());
    String newSeriesId = (!updateAll) ? UUID.randomUUID().toString() : null;

    try {
      for (Event eventToUpdate : eventsToChange) {
        propertySetter.accept(eventToUpdate, newValue);
        if (newSeriesId != null) {
          eventToUpdate.setSeriesId(newSeriesId);
        }
      }
      for (int i = 0; i < eventsToChange.size(); i++) {
        checkConflict(repo, eventsToChange.get(i), backups.get(i));
        repo.updateEvent(eventsToChange.get(i), backups.get(i));
      }
    } catch (Exception e) {
      for (int i = 0; i < eventsToChange.size(); i++) {
        restoreEvent(eventsToChange.get(i), backups.get(i));
      }
      throw new IllegalArgumentException("Series edit failed: " + e.getMessage(), e);
    }
  }

  private void checkConflict(EventRepository repo, Event event, Event originalState)
      throws ConflictException {
    for (Event existing : repo.getAllEvents()) {
      if (existing == event) {
        continue;
      }
      if (originalState != null && existing.equals(originalState)) {
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
}