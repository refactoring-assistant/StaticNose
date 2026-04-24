package calendar.controller;

import calendar.model.Calendar;
import calendar.model.Event;
import calendar.model.EventSeries;
import calendar.model.EventStatus;
import calendar.model.MultiCalendarModel;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Main controller that coordinates between model and view.
 */
public class CalendarController {
  private final MultiCalendarModel multiModel;
  private final CsvExporter csvExporter;
  private final IcalExporter icalExporter;

  /**
   * Constructs a new CalendarController.
   */
  public CalendarController() {
    this.multiModel = new MultiCalendarModel();
    this.csvExporter = new CsvExporter();
    this.icalExporter = new IcalExporter();
  }

  /**
   * Creates a new calendar.
   */
  public void createCalendar(String name, String timezone) {
    ZoneId zoneId = ZoneId.of(timezone);
    multiModel.createCalendar(name, zoneId);
  }

  /**
   * Edits a calendar property.
   */
  public void editCalendar(String name, String property, String newValue) {
    multiModel.editCalendar(name, property, newValue);
  }

  /**
   * Sets the current calendar.
   */
  public void useCalendar(String name) {
    multiModel.setCurrentCalendar(name);
  }

  /**
   * Gets the current calendar name.
   */
  public String getCurrentCalendarName() {
    return multiModel.getCurrentCalendar().getName();
  }

  /**
   * Creates an all-day event in the current calendar.
   */
  public void createAllDayEvent(String subject, LocalDate date) {
    createAllDayEvent(subject, date, "", "", EventStatus.PUBLIC);
  }

  /**
   * Creates an all-day event with all parameters.
   */
  public void createAllDayEvent(String subject, LocalDate date,
                                String description, String location, EventStatus status) {
    if (subject == null || subject.trim().isEmpty()) {
      throw new IllegalArgumentException("Event subject cannot be null or empty");
    }
    if (date == null) {
      throw new IllegalArgumentException("Date cannot be null");
    }

    Calendar currentCal = multiModel.getCurrentCalendar();
    LocalDateTime start = LocalDateTime.of(date, LocalTime.of(8, 0));
    LocalDateTime end = LocalDateTime.of(date, LocalTime.of(17, 0));

    // Use the helper methods instead of direct null checks
    String desc = handleNullDescription(description);
    String loc = handleNullLocation(location);
    EventStatus stat = handleNullStatus(status);

    Event event = new Event(subject, start, end, desc, loc, stat, null);
    currentCal.getModel().addEvent(event);
  }


  /**
   * Creates an all-day event series with all parameters.
   */
  public void createAllDayEventSeries(String subject, LocalDate startDate, Set<DayOfWeek> days,
                                      Integer occurrences, LocalDate untilDate,
                                      String description, String location, EventStatus status) {
    if (subject == null || subject.trim().isEmpty()) {
      throw new IllegalArgumentException("Event subject cannot be null or empty");
    }
    if (startDate == null) {
      throw new IllegalArgumentException("Start date cannot be null");
    }
    if (days == null || days.isEmpty()) {
      throw new IllegalArgumentException("Repeat days cannot be null or empty");
    }

    Calendar currentCal = multiModel.getCurrentCalendar();
    LocalDateTime start = LocalDateTime.of(startDate, LocalTime.of(8, 0));
    LocalDateTime end = LocalDateTime.of(startDate, LocalTime.of(17, 0));

    // Use the helper methods instead of direct null checks
    String desc = handleNullDescription(description);
    String loc = handleNullLocation(location);
    EventStatus stat = handleNullStatus(status);

    // Handle null occurrences and untilDate
    Integer finalOccurrences = occurrences;

    // Ensure at least one termination condition is provided
    if (finalOccurrences == null && untilDate == null) {
      finalOccurrences = 5; // Default to 5 occurrences
    }

    EventSeries series = new EventSeries(subject, start, end, desc, loc, stat,
        days, finalOccurrences, untilDate);
    currentCal.getModel().addEventSeries(series);
  }

  /**
   * Creates an all-day event series in the current calendar.
   */
  public void createAllDayEventSeries(String subject, LocalDate startDate, Set<DayOfWeek> days,
                                      Integer occurrences, LocalDate untilDate) {
    createAllDayEventSeries(subject, startDate, days,
        occurrences, untilDate, "", "", EventStatus.PUBLIC);
  }

  /**
   * Edits a single event in the current calendar.
   */
  public void editSingleEvent(String subject, LocalDateTime startTime,
                              String property, String newValue) {
    Calendar currentCal = multiModel.getCurrentCalendar();
    Event event = currentCal.getModel().findEvent(subject, startTime);
    if (event == null) {
      throw new IllegalArgumentException("Event not found");
    }

    Event updatedEvent = createUpdatedEvent(event, property, newValue);
    currentCal.getModel().updateEvent(event, updatedEvent);
  }

  /**
   * Edits events from a specific point in the current calendar.
   */
  public void editEventsFrom(String subject, LocalDateTime startTime,
                             String property, String newValue) {
    Calendar currentCal = multiModel.getCurrentCalendar();
    Event event = currentCal.getModel().findEvent(subject, startTime);
    if (event == null) {
      throw new IllegalArgumentException("Event not found");
    }

    if (event.getSeriesId() != null) {
      // Edit all events in series from this point
      List<Event> seriesEvents = currentCal.getModel().findEventsInSeriesFrom(event.getSeriesId(),
          startTime);
      for (Event seriesEvent : seriesEvents) {
        Event updatedEvent = createUpdatedEvent(seriesEvent, property, newValue);
        currentCal.getModel().updateEvent(seriesEvent, updatedEvent);
      }
    } else {
      // Single event - same as editSingleEvent
      editSingleEvent(subject, startTime, property, newValue);
    }
  }

  /**
   * Edits an entire event series in the current calendar.
   */
  public void editEventSeries(String subject, LocalDateTime startTime,
                              String property, String newValue) {
    Calendar currentCal = multiModel.getCurrentCalendar();
    Event event = currentCal.getModel().findEvent(subject, startTime);
    if (event == null) {
      throw new IllegalArgumentException("Event not found");
    }

    if (event.getSeriesId() != null) {
      // Edit all events in series
      List<Event> seriesEvents = currentCal.getModel().findEventsInSeries(event.getSeriesId());
      for (Event seriesEvent : seriesEvents) {
        Event updatedEvent = createUpdatedEvent(seriesEvent, property, newValue);
        currentCal.getModel().updateEvent(seriesEvent, updatedEvent);
      }
    } else {
      // Single event - same as editSingleEvent
      editSingleEvent(subject, startTime, property, newValue);
    }
  }

  /**
   * Creates an updated event with modified property.
   *
   * @param original the original event
   * @param property the property to update
   * @param newValue the new value
   * @return the updated event
   */
  public Event createUpdatedEvent(Event original, String property,
                                  String newValue) {
    switch (property.toLowerCase()) {
      case "subject":
        return new Event(newValue, original.getStartDateTime(), original.getEndDateTime(),
            original.getDescription(), original.getLocation(), original.getStatus(),
            original.getSeriesId());
      case "start":
        LocalDateTime newStart = LocalDateTime.parse(newValue);
        return new Event(original.getSubject(), newStart, original.getEndDateTime(),
            original.getDescription(), original.getLocation(), original.getStatus(),
            original.getSeriesId());
      case "end":
        LocalDateTime newEnd = LocalDateTime.parse(newValue);
        return new Event(original.getSubject(), original.getStartDateTime(), newEnd,
            original.getDescription(), original.getLocation(), original.getStatus(),
            original.getSeriesId());
      case "description":
        return new Event(original.getSubject(), original.getStartDateTime(),
            original.getEndDateTime(), newValue, original.getLocation(),
            original.getStatus(), original.getSeriesId());
      case "location":
        return new Event(original.getSubject(), original.getStartDateTime(),
            original.getEndDateTime(), original.getDescription(),
            newValue, original.getStatus(), original.getSeriesId());
      case "status":
        EventStatus newStatus = "private".equalsIgnoreCase(newValue)
            ? EventStatus.PRIVATE
            : EventStatus.PUBLIC;
        return new Event(original.getSubject(), original.getStartDateTime(),
            original.getEndDateTime(), original.getDescription(),
            original.getLocation(), newStatus, original.getSeriesId());
      default:
        throw new IllegalArgumentException("Unknown property: " + property);
    }
  }

  /**
   * Updates an event with new data (for GUI editing).
   */
  public void updateEvent(Event oldEvent, String newSubject, LocalDateTime newStart,
                          LocalDateTime newEnd, String newDescription, String newLocation,
                          EventStatus newStatus) {
    Calendar currentCal = multiModel.getCurrentCalendar();

    Event updatedEvent = new Event(
        newSubject, newStart, newEnd,
        newDescription != null ? newDescription : "",
        newLocation != null ? newLocation : "",
        newStatus != null ? newStatus : EventStatus.PUBLIC,
        oldEvent.getSeriesId()
    );

    currentCal.getModel().updateEvent(oldEvent, updatedEvent);
  }

  /**
   * Copies an event to another calendar.
   */
  public void copyEvent(String eventName, LocalDateTime sourceStartTime,
                        String targetCalendarName, LocalDateTime targetStartTime) {
    Calendar sourceCal = multiModel.getCurrentCalendar();
    Calendar targetCal = multiModel.getCalendar(targetCalendarName);

    if (targetCal == null) {
      throw new IllegalArgumentException("Target calendar '" + targetCalendarName + "' not found");
    }

    Event sourceEvent = sourceCal.getModel().findEvent(eventName, sourceStartTime);
    if (sourceEvent == null) {
      throw new IllegalArgumentException("Event '" + eventName + "' not found at specified time");
    }

    // Calculate time difference for adjustment
    java.time.Duration duration = java.time.Duration.between(sourceStartTime, targetStartTime);

    LocalDateTime newStart = sourceEvent.getStartDateTime().plus(duration);
    LocalDateTime newEnd = sourceEvent.getEndDateTime().plus(duration);

    Event copiedEvent = new Event(sourceEvent.getSubject(), newStart, newEnd,
        sourceEvent.getDescription(), sourceEvent.getLocation(),
        sourceEvent.getStatus(), null);

    targetCal.getModel().addEvent(copiedEvent);
  }

  /**
   * Copies all events on a specific day to another calendar.
   */
  public void copyEventsOnDate(LocalDate sourceDate,
                               String targetCalendarName, LocalDate targetDate) {
    Calendar sourceCal = multiModel.getCurrentCalendar();
    Calendar targetCal = multiModel.getCalendar(targetCalendarName);

    if (targetCal == null) {
      throw new IllegalArgumentException("Target calendar '" + targetCalendarName + "' not found");
    }

    List<Event> eventsOnDate = sourceCal.getModel().getEventsOnDate(sourceDate);

    forEventOnDate(sourceDate, targetDate, targetCal, eventsOnDate);
  }

  private void forEventOnDate(LocalDate sourceDate, LocalDate targetDate, Calendar targetCal,
                              List<Event> eventsOnDate) {
    for (Event sourceEvent : eventsOnDate) {
      // Calculate day difference
      long daysDiff = java.time.temporal.ChronoUnit.DAYS.between(sourceDate, targetDate);

      LocalDateTime newStart = sourceEvent.getStartDateTime().plusDays(daysDiff);
      LocalDateTime newEnd = sourceEvent.getEndDateTime().plusDays(daysDiff);

      Event copiedEvent = new Event(sourceEvent.getSubject(), newStart, newEnd,
          sourceEvent.getDescription(), sourceEvent.getLocation(),
          sourceEvent.getStatus(), sourceEvent.getSeriesId());

      try {
        targetCal.getModel().addEvent(copiedEvent);
      } catch (IllegalArgumentException e) {
        // Skip conflicting events
      }
    }
  }

  /**
   * Copies events in a date range to another calendar.
   */
  public void copyEventsInRange(LocalDate startDate, LocalDate endDate,
                                String targetCalendarName, LocalDate targetStartDate) {
    Calendar sourceCal = multiModel.getCurrentCalendar();
    Calendar targetCal = multiModel.getCalendar(targetCalendarName);

    if (targetCal == null) {
      throw new IllegalArgumentException("Target calendar '" + targetCalendarName + "' not found");
    }

    LocalDateTime rangeStart = LocalDateTime.of(startDate, LocalTime.MIN);
    LocalDateTime rangeEnd = LocalDateTime.of(endDate, LocalTime.MAX);

    List<Event> eventsInRange = sourceCal.getModel().getEventsInRange(rangeStart, rangeEnd);

    forEventOnDate(startDate, targetStartDate, targetCal, eventsInRange);
  }

  /**
   * Gets events on a specific date from current calendar.
   */
  public String getEventsOnDate(LocalDate date) {
    Calendar currentCal = multiModel.getCurrentCalendar();
    List<Event> events = currentCal.getModel().getEventsOnDate(date);

    if (events.isEmpty()) {
      return "No events on " + date + " in calendar '" + currentCal.getName() + "'";
    }

    StringBuilder result = new StringBuilder();
    result.append("Events on ").append(date).append(" in calendar '")
        .append(currentCal.getName()).append("':\n");

    for (Event event : events) {
      result.append("• ").append(event.getSubject());
      if (!event.isAllDayEvent()) {
        result.append(" (").append(event.getStartDateTime().toLocalTime())
            .append(" - ").append(event.getEndDateTime().toLocalTime()).append(")");
      } else {
        result.append(" (All Day)");
      }
      if (event.getLocation() != null && !event.getLocation().isEmpty()) {
        result.append(" at ").append(event.getLocation());
      }
      result.append("\n");
    }
    return result.toString();
  }

  /**
   * Gets events on a specific date as a List for GUI display.
   */
  public List<Event> getEventsOnDateAsList(LocalDate date) {
    if (date == null) {
      throw new IllegalArgumentException("Date cannot be null");
    }
    Calendar currentCal = multiModel.getCurrentCalendar();
    return currentCal.getModel().getEventsOnDate(date);
  }

  /**
   * Deletes an event.
   */
  public void deleteEvent(Event event) {
    Calendar currentCal = multiModel.getCurrentCalendar();
    currentCal.getModel().removeEvent(event);
  }

  /**
   * Gets events in a range from current calendar.
   */
  public String getEventsInRange(LocalDateTime start, LocalDateTime end) {
    Calendar currentCal = multiModel.getCurrentCalendar();
    List<Event> events = currentCal.getModel().getEventsInRange(start, end);

    if (events.isEmpty()) {
      return "No events in the specified range in calendar '" + currentCal.getName() + "'";
    }

    StringBuilder result = new StringBuilder();
    result.append("Events in range in calendar '").append(currentCal.getName()).append("':\n");

    for (Event event : events) {
      result.append("• ").append(event.getSubject())
          .append(" (").append(event.getStartDateTime())
          .append(" to ").append(event.getEndDateTime()).append(")");
      if (event.getLocation() != null && !event.getLocation().isEmpty()) {
        result.append(" at ").append(event.getLocation());
      }
      result.append("\n");
    }
    return result.toString();
  }

  /**
   * Checks if busy at a specific date-time in current calendar.
   */
  public boolean isBusyAt(LocalDateTime dateTime) {
    Calendar currentCal = multiModel.getCurrentCalendar();
    return currentCal.getModel().isBusyAt(dateTime);
  }

  /**
   * Exports current calendar to file (CSV or iCal based on extension).
   */
  public String exportCalendar(String fileName) {
    try {
      Calendar currentCal = multiModel.getCurrentCalendar();
      List<Event> allEvents = currentCal.getModel().getAllEvents();

      if (fileName.toLowerCase().endsWith(".ical")) {
        return icalExporter.exportToIcal(allEvents, fileName);
      } else {
        // Default to CSV
        if (!fileName.toLowerCase().endsWith(".csv")) {
          fileName += ".csv";
        }
        return csvExporter.exportToCsv(allEvents, fileName);
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to export calendar: " + e.getMessage(), e);
    }
  }

  /**
   * Gets all calendar names.
   */
  public List<String> getCalendarNames() {
    return multiModel.getAllCalendars().stream()
        .map(Calendar::getName)
        .collect(Collectors.toList());
  }

  /**
   * Gets all events for current calendar.
   */
  public List<Event> getAllEvents() {
    Calendar currentCal = multiModel.getCurrentCalendar();
    return currentCal.getModel().getAllEvents();
  }

  /**
   * Finds an event by exact match (for GUI operations).
   */
  public Event findEvent(Event eventToFind) {
    Calendar currentCal = multiModel.getCurrentCalendar();
    return currentCal.getModel().findEvent(
        eventToFind.getSubject(),
        eventToFind.getStartDateTime()
    );
  }

  /**
   * Helper method to handle null description - extracted for testability.
   */
  public String handleNullDescription(String description) {
    return description != null ? description : "";
  }

  /**
   * Helper method to handle null location - extracted for testability.
   */
  public String handleNullLocation(String location) {
    return location != null ? location : "";
  }

  /**
   * Helper method to handle null status - extracted for testability.
   */
  public EventStatus handleNullStatus(EventStatus status) {
    return status != null ? status : EventStatus.PUBLIC;
  }

  /**
   * Creates a single event with explicit null handling for better testability.
   */
  public void createSingleEvent(String subject, LocalDateTime start, LocalDateTime end,
                                String description, String location, EventStatus status) {
    if (subject == null || subject.trim().isEmpty()) {
      throw new IllegalArgumentException("Event subject cannot be null or empty");
    }
    if (start == null) {
      throw new IllegalArgumentException("Start time cannot be null");
    }
    if (end == null) {
      throw new IllegalArgumentException("End time cannot be null");
    }
    if (end.isBefore(start)) {
      throw new IllegalArgumentException("End time cannot be before start time");
    }

    Calendar currentCal = multiModel.getCurrentCalendar();

    // Make null handling very explicit and testable
    String finalDescription = handleNullWithDefault(description, "");
    String finalLocation = handleNullWithDefault(location, "");
    EventStatus finalStatus = handleNullWithDefault(status, EventStatus.PUBLIC);

    Event event = new Event(subject, start, end, finalDescription,
        finalLocation, finalStatus, null);
    currentCal.getModel().addEvent(event);
  }

  /**
   * Generic null handler with explicit default value.
   */
  public <T> T handleNullWithDefault(T value, T defaultValue) {
    return value != null ? value : defaultValue;
  }

  /**
   * Creates an event series in the current calendar.
   */
  public void createEventSeries(String subject, LocalDateTime start, LocalDateTime end,
                                Set<DayOfWeek> days, Integer occurrences, LocalDate untilDate,
                                String description, String location, EventStatus status) {
    if (subject == null || subject.trim().isEmpty()) {
      throw new IllegalArgumentException("Event subject cannot be null or empty");
    }
    if (start == null) {
      throw new IllegalArgumentException("Start time cannot be null");
    }
    if (end == null) {
      throw new IllegalArgumentException("End time cannot be null");
    }
    if (end.isBefore(start)) {
      throw new IllegalArgumentException("End time cannot be before start time");
    }
    if (days == null || days.isEmpty()) {
      throw new IllegalArgumentException("Repeat days cannot be null or empty");
    }

    Calendar currentCal = multiModel.getCurrentCalendar();

    // Use the helper methods instead of direct null checks
    String desc = handleNullDescription(description);
    String loc = handleNullLocation(location);
    EventStatus stat = handleNullStatus(status);

    // Handle null occurrences and untilDate
    Integer finalOccurrences = occurrences;

    // Ensure at least one termination condition is provided
    if (finalOccurrences == null && untilDate == null) {
      finalOccurrences = 5; // Default to 5 occurrences
    }

    EventSeries series = new EventSeries(subject, start, end, desc, loc, stat,
        days, finalOccurrences, untilDate);
    currentCal.getModel().addEventSeries(series);
  }
}