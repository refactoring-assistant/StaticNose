package calendar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of a calendar that manages events.
 */
public class CalendarImpl implements Calendar {

  private final List<Event> events;
  private String name;
  private ZoneId timezone;
  private final Map<String, PropertyEditor> propertyEditors;

  /**
   * Creates a new calendar with a name and timezone.
   *
   * @param name     the calendar name
   * @param timezone the calendar timezone
   * @throws IllegalArgumentException if name is null/empty or timezone is null
   */
  public CalendarImpl(String name, ZoneId timezone) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be null or empty");
    }
    if (timezone == null) {
      throw new IllegalArgumentException("Timezone cannot be null");
    }
    this.events = new ArrayList<>();
    this.name = name.trim();
    this.timezone = timezone;
    this.propertyEditors = initializePropertyEditors();
  }

  /**
   * Creates a new empty calendar with default timezone.
   */
  public CalendarImpl() {
    this("Default", ZoneId.systemDefault());
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be null or empty");
    }
    this.name = name.trim();
  }

  @Override
  public ZoneId getTimezone() {
    return timezone;
  }

  @Override
  public void setTimezone(ZoneId timezone) {
    if (timezone == null) {
      throw new IllegalArgumentException("Timezone cannot be null");
    }
    this.timezone = timezone;
  }

  @Override
  public void addEvent(Event event) {
    if (event == null) {
      throw new IllegalArgumentException("Event cannot be null");
    }
    for (Event existing : events) {
      if (existing.conflictsWith(event)) {
        throw new IllegalStateException(
            String.format("Event already exists: '%s' from %s to %s", event.getSubject(),
                event.getStartDateTime(), event.getEndDateTime()));
      }
    }
    events.add(event);
  }

  @Override
  public String addEventSeries(Event templateEvent, List<WeekDay> weekDays, int occurrences) {
    if (templateEvent == null) {
      throw new IllegalArgumentException("Template event cannot be null");
    }
    if (weekDays == null || weekDays.isEmpty()) {
      throw new IllegalArgumentException("Week days cannot be null or empty");
    }
    if (occurrences <= 0) {
      throw new IllegalArgumentException("Number of occurrences must be positive");
    }

    validateEventDoesNotSpanMultipleDays(templateEvent);

    String seriesId = UUID.randomUUID().toString();
    List<Event> generatedEvents = new ArrayList<>();
    LocalDateTime currentDate = templateEvent.getStartDateTime();
    int count = 0;
    while (count < occurrences) {
      WeekDay currentWeekDay = WeekDay.fromDayOfWeek(currentDate.getDayOfWeek());
      if (weekDays.contains(currentWeekDay)) {
        Event newEvent = createSeriesEvent(templateEvent, currentDate, seriesId);
        validateEventForSeries(newEvent, generatedEvents);
        generatedEvents.add(newEvent);
        count++;
      }
      currentDate = currentDate.plusDays(1);
    }
    events.addAll(generatedEvents);
    return seriesId;
  }

  @Override
  public String addEventSeriesUntil(Event templateEvent, List<WeekDay> weekDays,
                                    LocalDate endDate) {
    if (templateEvent == null) {
      throw new IllegalArgumentException("Template event cannot be null");
    }
    if (weekDays == null || weekDays.isEmpty()) {
      throw new IllegalArgumentException("Week days cannot be null or empty");
    }
    if (endDate == null) {
      throw new IllegalArgumentException("End date cannot be null");
    }
    if (endDate.isBefore(templateEvent.getStartDateTime().toLocalDate())) {
      throw new IllegalArgumentException("End date cannot be before template event start date");
    }

    validateEventDoesNotSpanMultipleDays(templateEvent);

    String seriesId = UUID.randomUUID().toString();
    List<Event> generatedEvents = new ArrayList<>();
    LocalDateTime currentDate = templateEvent.getStartDateTime();
    while (!currentDate.toLocalDate().isAfter(endDate)) {
      WeekDay currentWeekDay = WeekDay.fromDayOfWeek(currentDate.getDayOfWeek());
      if (weekDays.contains(currentWeekDay)) {
        Event newEvent = createSeriesEvent(templateEvent, currentDate, seriesId);
        validateEventForSeries(newEvent, generatedEvents);
        generatedEvents.add(newEvent);
      }
      currentDate = currentDate.plusDays(1);
    }
    if (generatedEvents.isEmpty()) {
      throw new IllegalStateException("No events generated for the given criteria");
    }
    events.addAll(generatedEvents);
    return seriesId;
  }

  /**
   * Validates that an event does not span multiple days.
   * Series events must start and end on the same day.
   *
   * @param event the event to validate
   * @throws IllegalArgumentException if the event spans multiple days
   */
  private void validateEventDoesNotSpanMultipleDays(Event event) {
    LocalDate startDate = event.getStartDateTime().toLocalDate();
    LocalDate endDate = event.getEndDateTime().toLocalDate();

    if (!startDate.equals(endDate)) {
      throw new IllegalArgumentException(
          String.format("Series events cannot span multiple days. "
                  + "Event '%s' starts on %s and ends on %s",
              event.getSubject(), startDate, endDate));
    }
  }

  private Event createSeriesEvent(Event templateEvent, LocalDateTime currentDate,
                                  String seriesId) {
    Event newEvent = templateEvent.copy();
    long durationMinutes = java.time.Duration.between(templateEvent.getStartDateTime(),
        templateEvent.getEndDateTime()).toMinutes();
    LocalDateTime newStart = currentDate;
    LocalDateTime newEnd = newStart.plusMinutes(durationMinutes);

    if (!newStart.toLocalDate().equals(newEnd.toLocalDate())) {
      throw new IllegalStateException(
          String.format("Generated series event would span multiple days: '%s' from %s to %s",
              newEvent.getSubject(), newStart, newEnd));
    }

    newEvent.setEndDateTime(newEnd);
    newEvent.setStartDateTime(newStart);
    newEvent.setSeriesId(seriesId);
    return newEvent;
  }

  private void validateEventForSeries(Event newEvent, List<Event> generatedEvents) {
    for (Event existing : events) {
      if (existing.conflictsWith(newEvent)) {
        throw new IllegalStateException(String.format(
            "Cannot create series: event '%s' at %s conflicts with existing event",
            newEvent.getSubject(), newEvent.getStartDateTime()));
      }
    }
    for (Event generated : generatedEvents) {
      if (generated.conflictsWith(newEvent)) {
        throw new IllegalStateException(
            String.format("Cannot create series: duplicate event '%s' at %s in series",
                newEvent.getSubject(), newEvent.getStartDateTime()));
      }
    }
  }

  @Override
  public Event findEvent(String subject, LocalDateTime startDateTime) {
    if (subject == null || startDateTime == null) {
      return null;
    }
    for (Event event : events) {
      if (event.getSubject().equals(subject) && event.getStartDateTime().equals(startDateTime)) {
        return event;
      }
    }
    return null;
  }

  @Override
  public List<Event> findEvents(String subject, LocalDateTime startDateTime) {
    if (subject == null || startDateTime == null) {
      return new ArrayList<>();
    }
    return events.stream()
        .filter(e -> e.getSubject().equals(subject)
            && e.getStartDateTime().equals(startDateTime))
        .collect(Collectors.toList());
  }

  @Override
  public void editEvent(String subject, LocalDateTime startDateTime, String property,
                        String newValue) {
    Event event = findEvent(subject, startDateTime);
    if (event == null) {
      throw new IllegalStateException(
          String.format("Event not found: '%s' starting at %s", subject, startDateTime));
    }
    PropertyType propertyType = PropertyType.fromString(property);
    boolean breaksSeries = propertyType == PropertyType.START;
    applyEdit(event, propertyType, newValue, breaksSeries);
  }

  @Override
  public void editEventsFromThisForward(String subject, LocalDateTime startDateTime,
                                        String property, String newValue) {
    Event event = findEvent(subject, startDateTime);
    if (event == null) {
      throw new IllegalStateException(
          String.format("Event not found: '%s' starting at %s", subject, startDateTime));
    }
    PropertyType propertyType = PropertyType.fromString(property);
    String seriesId = event.getSeriesId();
    if (seriesId == null) {
      applyEdit(event, propertyType, newValue, false);
    } else {
      List<Event> eventsToEdit = events.stream().filter(
              e -> seriesId.equals(e.getSeriesId())
                  && !e.getStartDateTime().isBefore(startDateTime))
          .collect(Collectors.toList());

      boolean breaksSeries = propertyType == PropertyType.START;
      String newSeriesId = null;
      if (breaksSeries) {
        newSeriesId = UUID.randomUUID().toString();
      }

      for (Event e : eventsToEdit) {
        applyEdit(e, propertyType, newValue, false);
        if (breaksSeries) {
          e.setSeriesId(newSeriesId);
        }
      }
    }
  }

  @Override
  public void editEntireSeries(String subject, LocalDateTime startDateTime, String property,
                               String newValue) {
    Event event = findEvent(subject, startDateTime);
    if (event == null) {
      throw new IllegalStateException(
          String.format("Event not found: '%s' starting at %s", subject, startDateTime));
    }
    PropertyType propertyType = PropertyType.fromString(property);
    String seriesId = event.getSeriesId();
    if (seriesId == null) {
      applyEdit(event, propertyType, newValue, false);
    } else {
      List<Event> eventsToEdit = events.stream().filter(e -> seriesId.equals(e.getSeriesId()))
          .collect(Collectors.toList());

      for (Event e : eventsToEdit) {
        applyEdit(e, propertyType, newValue, false);
      }
    }
  }

  private Map<String, PropertyEditor> initializePropertyEditors() {
    Map<String, PropertyEditor> editors = new HashMap<>();
    editors.put("subject", (event, value) -> editSubject(event, value));
    editors.put("start", (event, value) -> editStartTime(event, value));
    editors.put("end", (event, value) -> editEndTime(event, value));
    editors.put("description", (event, value) -> editDescription(event, value));
    editors.put("location", (event, value) -> editLocation(event, value));
    editors.put("status", (event, value) -> editStatus(event, value));
    return editors;
  }

  private void applyEdit(Event event, PropertyType property, String newValue,
                         boolean breaksSeries) {
    String oldSubject = event.getSubject();
    LocalDateTime oldStart = event.getStartDateTime();
    LocalDateTime oldEnd = event.getEndDateTime();
    String oldDescription = event.getDescription();
    String oldLocation = event.getLocation();
    EventStatus oldStatus = event.getStatus();
    String oldSeriesId = event.getSeriesId();

    try {
      PropertyEditor editor = propertyEditors.get(property.getValue());
      if (editor == null) {
        throw new IllegalArgumentException("Invalid property: " + property
            + ". Must be one of: subject, start, end, description, location, status");
      }

      editor.edit(event, newValue);

      if (event.getSeriesId() != null) {
        validateEventDoesNotSpanMultipleDays(event);
      }

      validateNoConflicts(event);

      if (breaksSeries) {
        event.setSeriesId(null);
      }
    } catch (IllegalArgumentException | IllegalStateException e) {
      rollbackEvent(event, oldSubject, oldStart, oldEnd, oldDescription,
          oldLocation, oldStatus, oldSeriesId);
      throw e;
    }
  }

  private void editSubject(Event event, String newValue) {
    event.setSubject(newValue);
  }

  private void editStartTime(Event event, String newValue) {
    LocalDateTime parsedStart = parseDateTime(newValue);
    LocalTime newStartTime = parsedStart.toLocalTime();
    LocalDate eventDate = event.getStartDateTime().toLocalDate();
    LocalDateTime newStart = LocalDateTime.of(eventDate, newStartTime);

    if (newStart.isAfter(event.getEndDateTime())) {
      throw new IllegalArgumentException(
          "New start time cannot be after current end time. "
              + "Start: " + newStart + ", End: " + event.getEndDateTime());
    }
    event.setStartDateTime(newStart);
  }

  private void editEndTime(Event event, String newValue) {
    LocalDateTime parsedEnd = parseDateTime(newValue);
    LocalTime newEndTime = parsedEnd.toLocalTime();
    LocalDate eventEndDate = event.getStartDateTime().toLocalDate();
    LocalDateTime newEnd = LocalDateTime.of(eventEndDate, newEndTime);
    event.setEndDateTime(newEnd);
  }

  private void editDescription(Event event, String newValue) {
    event.setDescription(newValue);
  }

  private void editLocation(Event event, String newValue) {
    event.setLocation(newValue);
  }

  private void editStatus(Event event, String newValue) {
    event.setStatus(EventStatus.fromString(newValue));
  }

  private void validateNoConflicts(Event event) {
    for (Event other : events) {
      if (other != event && other.conflictsWith(event)) {
        throw new IllegalStateException(
            String.format("Cannot edit: would create duplicate event '%s' from %s to %s",
                event.getSubject(), event.getStartDateTime(), event.getEndDateTime()));
      }
    }
  }

  private void rollbackEvent(Event event, String oldSubject, LocalDateTime oldStart,
                             LocalDateTime oldEnd, String oldDescription, String oldLocation,
                             EventStatus oldStatus, String oldSeriesId) {
    event.setSubject(oldSubject);
    event.setStartDateTime(oldStart);
    event.setEndDateTime(oldEnd);
    event.setDescription(oldDescription);
    event.setLocation(oldLocation);
    event.setStatus(oldStatus);
    event.setSeriesId(oldSeriesId);
  }

  private LocalDateTime parseDateTime(String dateTimeString) {
    try {
      return LocalDateTime.parse(dateTimeString);
    } catch (Exception e) {
      throw new IllegalArgumentException(
          "Invalid date/time format: " + dateTimeString + ". Expected format: YYYY-MM-DDThh:mm", e);
    }
  }

  @Override
  public List<Event> getEventsOnDate(LocalDate date) {
    if (date == null) {
      return new ArrayList<>();
    }
    LocalDateTime startOfDay = date.atStartOfDay();
    LocalDateTime endOfDay = date.atTime(23, 59, 59);
    return events.stream().filter(e -> e.occursInRange(startOfDay, endOfDay))
        .collect(Collectors.toList());
  }

  @Override
  public List<Event> getEventsInRange(LocalDateTime start, LocalDateTime end) {
    if (start == null || end == null) {
      return new ArrayList<>();
    }
    return events.stream().filter(e -> e.occursInRange(start, end)).collect(Collectors.toList());
  }

  @Override
  public boolean isBusy(LocalDateTime dateTime) {
    if (dateTime == null) {
      return false;
    }
    return events.stream().anyMatch(e -> e.occursAt(dateTime));
  }

  @Override
  public Event findEventByTimes(String subject, LocalDateTime startDateTime,
                                LocalDateTime endDateTime) {
    if (subject == null || startDateTime == null || endDateTime == null) {
      return null;
    }
    for (Event event : events) {
      if (event.getSubject().equals(subject)
          && event.getStartDateTime().equals(startDateTime)
          && event.getEndDateTime().equals(endDateTime)) {
        return event;
      }
    }
    return null;
  }

  @Override
  public List<Event> getAllEvents() {
    return new ArrayList<>(events);
  }

  @Override
  public List<Event> getEventsBySeries(String seriesId) {
    if (seriesId == null) {
      return new ArrayList<>();
    }
    return events.stream().filter(e -> seriesId.equals(e.getSeriesId()))
        .collect(Collectors.toList());
  }

  @Override
  public void removeEvent(Event event) {
    events.remove(event);
  }

  @Override
  public void clear() {
    events.clear();
  }
}