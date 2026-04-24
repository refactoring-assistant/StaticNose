package calendar.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of a calendar that manages events.
 * Representation choice: Uses a List to store events for simple iteration
 * and filtering. HashMap tracks series for efficient series management.
 * Class invariant: No two events can have same subject, start, and end times.
 */
public class Calendar implements Icalendar {
  private final List<Event> events;
  private final Map<String, List<Event>> seriesMap;
  private static final LocalTime ALL_DAY_START = LocalTime.of(8, 0);
  private static final LocalTime ALL_DAY_END = LocalTime.of(17, 0);

  /**
   * Constructs an empty calendar.
   */
  public Calendar() {
    this.events = new ArrayList<>();
    this.seriesMap = new HashMap<>();
  }

  @Override
  public void createEvent(String subject, LocalDateTime start, LocalDateTime end) {
    if (subject == null || subject.trim().isEmpty()) {
      throw new IllegalArgumentException("Subject cannot be null or empty");
    }
    if (start == null) {
      throw new IllegalArgumentException("Start time cannot be null");
    }

    // Handle all-day events
    LocalDateTime actualEnd = end;
    LocalDateTime actualStart = start;
    if (end == null) {
      actualEnd = start.toLocalDate().atTime(ALL_DAY_END);
      actualStart = start.toLocalDate().atTime(ALL_DAY_START);
    }

    Event newEvent = new Event(subject.trim(), actualStart, actualEnd);

    // Check for duplicates
    if (isDuplicate(newEvent)) {
      throw new IllegalArgumentException(
          "Event with same subject, start, and end time already exists");
    }

    events.add(newEvent);
  }

  @Override
  public void createEventSeries(String subject, LocalDateTime start, LocalDateTime end,
                                String weekdays, int occurrences) {
    validateSeriesInput(subject, start, end, weekdays, occurrences);

    // Check if event spans multiple days
    if (!start.toLocalDate().equals(end.toLocalDate())) {
      throw new IllegalArgumentException(
          "Events in a series must start and end on the same day");
    }

    String seriesId = UUID.randomUUID().toString();
    List<DayOfWeek> days = parseWeekdays(weekdays);
    List<Event> seriesEvents = new ArrayList<>();

    LocalDate currentDate = start.toLocalDate();
    int count = 0;

    while (count < occurrences) {
      if (days.contains(currentDate.getDayOfWeek())) {
        LocalDateTime eventStart = currentDate.atTime(start.toLocalTime());
        LocalDateTime eventEnd = currentDate.atTime(end.toLocalTime());
        Event event = new Event(subject.trim(), eventStart, eventEnd,
            null, null, null, seriesId);

        if (!isDuplicate(event)) {
          events.add(event);
          seriesEvents.add(event);
          count++;
        }
      }
      currentDate = currentDate.plusDays(1);
    }

    seriesMap.put(seriesId, seriesEvents);
  }

  @Override
  public void createEventSeriesUntil(String subject, LocalDateTime start, LocalDateTime end,
                                     String weekdays, LocalDate until) {
    validateSeriesInput(subject, start, end, weekdays, 1);

    if (until == null) {
      throw new IllegalArgumentException("Until date cannot be null");
    }
    if (until.isBefore(start.toLocalDate())) {
      throw new IllegalArgumentException("Until date must be on or after start date");
    }

    // Check if event spans multiple days
    if (!start.toLocalDate().equals(end.toLocalDate())) {
      throw new IllegalArgumentException(
          "Events in a series must start and end on the same day");
    }

    String seriesId = UUID.randomUUID().toString();
    List<DayOfWeek> days = parseWeekdays(weekdays);
    List<Event> seriesEvents = new ArrayList<>();

    LocalDate currentDate = start.toLocalDate();

    while (!currentDate.isAfter(until)) {
      if (days.contains(currentDate.getDayOfWeek())) {
        LocalDateTime eventStart = currentDate.atTime(start.toLocalTime());
        LocalDateTime eventEnd = currentDate.atTime(end.toLocalTime());
        Event event = new Event(subject.trim(), eventStart, eventEnd,
            null, null, null, seriesId);

        if (!isDuplicate(event)) {
          events.add(event);
          seriesEvents.add(event);
        }
      }
      currentDate = currentDate.plusDays(1);
    }

    seriesMap.put(seriesId, seriesEvents);
  }

  @Override
  public void editEvent(String subject, LocalDateTime start, String property, String newValue) {
    List<Event> matchingEvents = findEvents(subject, start);

    if (matchingEvents.isEmpty()) {
      throw new IllegalArgumentException("No event found with given subject and start time");
    }
    if (matchingEvents.size() > 1) {
      throw new IllegalArgumentException(
          "Multiple events found. Please provide more specific criteria");
    }

    Event event = matchingEvents.get(0);
    Event updatedEvent = applyPropertyChange(event, property, newValue);

    // Check if update would create duplicate
    if (!event.equals(updatedEvent) && isDuplicate(updatedEvent)) {
      throw new IllegalArgumentException(
          "Cannot update: would create duplicate event");
    }

    int index = events.indexOf(event);
    events.set(index, updatedEvent);

    // Update series map if part of series
    if (event.isPartOfSeries()) {
      updateSeriesMap(event, updatedEvent);
    }
  }

  @Override
  public void editEventsFrom(String subject, LocalDateTime start, String property,
                             String newValue) {
    List<Event> matchingEvents = findEvents(subject, start);

    if (matchingEvents.isEmpty()) {
      throw new IllegalArgumentException("No event found with given subject and start time");
    }
    if (matchingEvents.size() > 1) {
      throw new IllegalArgumentException(
          "Multiple events found. Please provide more specific criteria");
    }

    Event targetEvent = matchingEvents.get(0);

    if (!targetEvent.isPartOfSeries()) {
      // Single event - same as editEvent
      editEvent(subject, start, property, newValue);
      return;
    }

    // Get all events in series from this point forward
    String seriesId = targetEvent.getSeriesId();
    List<Event> seriesToUpdate = seriesMap.get(seriesId).stream()
        .filter(e -> !e.getStart().isBefore(start))
        .collect(Collectors.toList());

    // Check if updates would create duplicates
    for (Event event : seriesToUpdate) {
      Event updated = applyPropertyChange(event, property, newValue);
      if (!event.equals(updated) && isDuplicate(updated)) {
        throw new IllegalArgumentException(
            "Cannot update: would create duplicate event");
      }
    }

    // Perform updates
    for (Event event : seriesToUpdate) {
      Event updated = applyPropertyChange(event, property, newValue);
      int index = events.indexOf(event);
      events.set(index, updated);

      // Handle series split on start time change
      if (property.equals("start") && !event.getSeriesId().equals(updated.getSeriesId())) {
        handleSeriesSplit(seriesId, event);
      }
    }
  }

  @Override
  public void editSeries(String subject, LocalDateTime start, String property, String newValue) {
    List<Event> matchingEvents = findEvents(subject, start);

    if (matchingEvents.isEmpty()) {
      throw new IllegalArgumentException("No event found with given subject and start time");
    }
    if (matchingEvents.size() > 1) {
      throw new IllegalArgumentException(
          "Multiple events found. Please provide more specific criteria");
    }

    Event targetEvent = matchingEvents.get(0);

    if (!targetEvent.isPartOfSeries()) {
      editEvent(subject, start, property, newValue);
      return;
    }

    String seriesId = targetEvent.getSeriesId();
    List<Event> seriesToUpdate = new ArrayList<>(seriesMap.get(seriesId));

    // Check if updates would create duplicates
    for (Event event : seriesToUpdate) {
      Event updated = applyPropertyChange(event, property, newValue);
      if (!event.equals(updated) && isDuplicate(updated)) {
        throw new IllegalArgumentException(
            "Cannot update: would create duplicate event");
      }
    }

    // Perform updates
    for (Event event : seriesToUpdate) {
      Event updated = applyPropertyChange(event, property, newValue);
      int index = events.indexOf(event);
      events.set(index, updated);
    }
  }

  @Override
  public List<Ievent> getEventsOnDate(LocalDate date) {
    return events.stream()
        .filter(e -> {
          LocalDate eventStart = e.getStart().toLocalDate();
          LocalDate eventEnd = e.getEnd().toLocalDate();
          return !date.isBefore(eventStart) && !date.isAfter(eventEnd);
        })
        .collect(Collectors.toList());
  }

  @Override
  public List<Ievent> getEventsInRange(LocalDateTime start, LocalDateTime end) {
    return events.stream()
        .filter(e -> !e.getEnd().isBefore(start) && !e.getStart().isAfter(end))
        .collect(Collectors.toList());
  }

  @Override
  public boolean isBusyAt(LocalDateTime dateTime) {
    return events.stream()
        .anyMatch(e -> !dateTime.isBefore(e.getStart()) && dateTime.isBefore(e.getEnd()));
  }

  @Override
  public String exportToCsv() {
    StringBuilder csv = new StringBuilder();
    csv.append("Subject,Start Date,Start Time,End Date,End Time,All Day Event,")
        .append("Description,Location,Private\n");

    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

    for (Event event : events) {
      csv.append(escapeCsv(event.getSubject())).append(",");
      csv.append(event.getStart().format(dateFormatter)).append(",");
      csv.append(event.getStart().format(timeFormatter)).append(",");
      csv.append(event.getEnd().format(dateFormatter)).append(",");
      csv.append(event.getEnd().format(timeFormatter)).append(",");
      csv.append("False,");
      csv.append(escapeCsv(event.getDescription())).append(",");
      csv.append(escapeCsv(event.getLocation())).append(",");
      csv.append("True".equals(event.getStatus()) ? "True" : "False");
      csv.append("\n");
    }

    return csv.toString();
  }

  @Override
  public List<Ievent> getAllEvents() {
    return new ArrayList<>(events);
  }

  private boolean isDuplicate(Event event) {
    return events.stream()
        .anyMatch(e -> e.getSubject().equals(event.getSubject())
            && e.getStart().equals(event.getStart())
            && e.getEnd().equals(event.getEnd()));
  }

  private List<Event> findEvents(String subject, LocalDateTime start) {
    return events.stream()
        .filter(e -> e.getSubject().equalsIgnoreCase(subject.trim())
            && e.getStart().equals(start))
        .collect(Collectors.toList());
  }

  private Event applyPropertyChange(Event event, String property, String newValue) {
    switch (property.toLowerCase()) {
      case "subject":
        return (Event) event.copyWith(newValue, null, null, null, null, null);
      case "start":
        LocalDateTime newStart = LocalDateTime.parse(newValue.replace("T", "T"));
        String newSeriesId = event.isPartOfSeries() ? UUID.randomUUID().toString() : null;
        return new Event(event.getSubject(), newStart, event.getEnd(),
            event.getDescription(), event.getLocation(),
            event.getStatus(), newSeriesId);
      case "end":
        LocalDateTime newEnd = LocalDateTime.parse(newValue.replace("T", "T"));
        return (Event) event.copyWith(null, null, newEnd, null, null, null);
      case "description":
        return (Event) event.copyWith(null, null, null, newValue, null, null);
      case "location":
        return (Event) event.copyWith(null, null, null, null, newValue, null);
      case "status":
        return (Event) event.copyWith(null, null, null, null, null, newValue);
      default:
        throw new IllegalArgumentException("Invalid property: " + property);
    }
  }

  private void validateSeriesInput(String subject, LocalDateTime start, LocalDateTime end,
                                   String weekdays, int occurrences) {
    if (subject == null || subject.trim().isEmpty()) {
      throw new IllegalArgumentException("Subject cannot be null or empty");
    }
    if (start == null || end == null) {
      throw new IllegalArgumentException("Start and end times cannot be null");
    }
    if (weekdays == null || weekdays.isEmpty()) {
      throw new IllegalArgumentException("Weekdays cannot be null or empty");
    }
    if (occurrences < 1) {
      throw new IllegalArgumentException("Occurrences must be at least 1");
    }
  }

  private List<DayOfWeek> parseWeekdays(String weekdays) {
    List<DayOfWeek> days = new ArrayList<>();
    for (char c : weekdays.toUpperCase().toCharArray()) {
      switch (c) {
        case 'M':
          days.add(DayOfWeek.MONDAY);
          break;
        case 'T':
          days.add(DayOfWeek.TUESDAY);
          break;
        case 'W':
          days.add(DayOfWeek.WEDNESDAY);
          break;
        case 'R':
          days.add(DayOfWeek.THURSDAY);
          break;
        case 'F':
          days.add(DayOfWeek.FRIDAY);
          break;
        case 'S':
          days.add(DayOfWeek.SATURDAY);
          break;
        case 'U':
          days.add(DayOfWeek.SUNDAY);
          break;
        default:
          throw new IllegalArgumentException("Invalid weekday character: " + c);
      }
    }
    return days;
  }

  private void updateSeriesMap(Event oldEvent, Event newEvent) {
    String seriesId = oldEvent.getSeriesId();
    List<Event> series = seriesMap.get(seriesId);
    int index = series.indexOf(oldEvent);
    series.set(index, newEvent);
  }

  private void handleSeriesSplit(String oldSeriesId, Event splitEvent) {
    List<Event> oldSeries = seriesMap.get(oldSeriesId);
    oldSeries.remove(splitEvent);
  }

  private String escapeCsv(String value) {
    if (value == null) {
      return "";
    }
    if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
      return "\"" + value.replace("\"", "\"\"") + "\"";
    }
    return value;
  }
}