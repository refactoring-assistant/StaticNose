package calendar;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * The Calendar Model.
 */
public class Calendar implements Icalender {

  private final List<Event> events = new ArrayList<>();
  private final List<EventSeries> eventSeriesList = new ArrayList<>();

  /**
   * Adds a single event to the calendar.
   *
   * @param e the event to add
   * @throws IllegalArgumentException if event conflicts with existing events
   */
  public void addEvent(Event e) {

    for (Event existingEvent : events) {
      if (existingEvent.conflictsWith(e)) {
        throw new IllegalArgumentException();
      }
    }
    events.add(e);
  }

  /**
   * Adds an event series to the calendar.
   *
   * @param series the event series to add
   * @throws IllegalArgumentException if any generated event conflicts
   */
  public void addEvent(EventSeries series) {

    for (EventSeries existingSeries : eventSeriesList) {
      if (series.conflictsWithSeries(existingSeries)) {
        throw new IllegalArgumentException();
      }
    }

    List<Event> generatedEvents = series.generateEvents();

    for (Event newEvent : generatedEvents) {
      for (Event existingEvent : events) {
        if (existingEvent.conflictsWith(newEvent)) {
          throw new IllegalArgumentException();
        }
      }
    }

    events.addAll(generatedEvents);
    eventSeriesList.add(series);
  }

  /**
   * Finds events matching the given criteria.
   *
   * @param subject       the subject to match
   * @param startDateTime the start date/time to match
   * @return list of matching events
   */
  public List<Event> findEvents(String subject, LocalDateTime startDateTime) {

    List<Event> matches = new ArrayList<>();
    for (Event event : events) {
      if (event.matches(subject, startDateTime)) {
        matches.add(event);
      }
    }
    return matches;
  }

  /**
   * Enum to specify the scope of editing operations.
   */
  private enum EditScope {
    SINGLE,
    FROM_DATE,
    ENTIRE_SERIES
  }

  /**
   * Gets the list of events to edit based on the scope.
   *
   * @param targetEvent the event that was found by the search
   * @param fromDate    the start date/time specified in the search
   * @param scope       the scope of the edit operation
   * @return list of events to edit
   */
  private List<Event> getEventsToEdit(Event targetEvent, LocalDateTime fromDate, EditScope scope) {
    if (scope == EditScope.SINGLE || !targetEvent.isPartOfSeries()) {
      List<Event> singleEvent = new ArrayList<>();
      singleEvent.add(targetEvent);
      return singleEvent;
    }

    String seriesId = targetEvent.getSeriesId();

    if (scope == EditScope.FROM_DATE) {
      return events.stream()
          .filter(e -> seriesId.equals(e.getSeriesId()))
          .filter(e -> !e.getStartDateTime().isBefore(fromDate))
          .collect(Collectors.toList());
    } else {
      return events.stream()
          .filter(e -> seriesId.equals(e.getSeriesId()))
          .collect(Collectors.toList());
    }
  }

  /**
   * Creates backup copies of all events in the list.
   */
  private List<Event> createBackups(List<Event> eventsToBackup) {
    List<Event> backups = new ArrayList<>();
    for (Event event : eventsToBackup) {
      backups.add(event.copy());
    }
    return backups;
  }

  /**
   * Restores events from their backup copies.
   */
  public void restoreBackups(List<Event> events, List<Event> backups) {
    for (int i = 0; i < events.size(); i++) {
      restoreEvent(events.get(i), backups.get(i));
    }
  }

  /**
   * Restores a single event from a backup copy.
   */
  private void restoreEvent(Event target, Event backup) {
    target.setSubject(backup.getSubject());
    target.setStartDateTime(backup.getStartDateTime());
    target.setEndDateTime(backup.getEndDateTime());
    target.setDescription(backup.getDescription());
    target.setLocation(backup.isPhysicalLocation());
    target.setPublic(backup.isPublic());
    target.setSeriesId(backup.getSeriesId());
  }

  /**
   * Handles start time changes by creating a new series ID.
   * When start time changes, events break away from their original series.
   */
  private void handleStartTimeChange(List<Event> eventsToEdit, String property, Event targetEvent) {
    if (property.equalsIgnoreCase("start") && targetEvent.isPartOfSeries()) {
      String oldSeriesId = targetEvent.getSeriesId();
      String newSeriesId = UUID.randomUUID().toString();

      for (Event event : eventsToEdit) {
        event.setSeriesId(newSeriesId);
      }

      boolean hasRemainingEvents = events.stream()
          .anyMatch(e -> oldSeriesId.equals(e.getSeriesId()));

      if (!hasRemainingEvents) {
        eventSeriesList.removeIf(series -> series.getSeriesId().equals(oldSeriesId));
      }
    }
  }

  /**
   * Updates a property for all events in the list.
   */
  private void updatePropertyForEvents(List<Event> eventsToUpdate, String property,
                                       String newValue) {
    for (Event event : eventsToUpdate) {
      event.updateProperty(property, newValue);
    }
  }

  /**
   * Checks if any events in the list conflict with other events in the calendar.
   *
   * @param eventsToCheck the events to check for conflicts
   * @param excludeEvents events to exclude from conflict checking
   * @return true if there are conflicts
   */
  private boolean hasConflicts(List<Event> eventsToCheck, List<Event> excludeEvents) {
    for (Event checkEvent : eventsToCheck) {
      for (Event otherEvent : events) {
        if (otherEvent != checkEvent
            && !excludeEvents.contains(otherEvent)
            && otherEvent.conflictsWith(checkEvent)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Core method that performs edit operations.
   *
   * @param subject       the subject of the event to edit
   * @param startDateTime the start date/time of the event to edit
   * @param property      the property to change
   * @param newValue      the new value for the property
   * @param scope         the scope of the edit
   * @throws IllegalArgumentException if event not found, multiple matches, or edit causes conflict
   */
  private void performEdit(String subject, LocalDateTime startDateTime, String property,
                           String newValue, EditScope scope) {
    List<Event> matches = findEvents(subject, startDateTime);

    if (matches.isEmpty()) {
      throw new IllegalArgumentException(
          "No event found with subject '" + subject + "' starting at " + startDateTime);
    }

    if (matches.size() > 1) {
      throw new IllegalArgumentException(
          "Multiple events found. Please provide more specific criteria.");
    }

    Event targetEvent = matches.get(0);
    List<Event> eventsToEdit = getEventsToEdit(targetEvent, startDateTime, scope);
    List<Event> backups = createBackups(eventsToEdit);

    try {
      handleStartTimeChange(eventsToEdit, property, targetEvent);
      updatePropertyForEvents(eventsToEdit, property, newValue);

      if (hasConflicts(eventsToEdit, eventsToEdit)) {
        restoreBackups(eventsToEdit, backups);
        throw new IllegalArgumentException(
            "Edit would create a conflict with existing event");
      }
    } catch (Exception e) {
      restoreBackups(eventsToEdit, backups);
      throw e;
    }
  }

  /**
   * Edits a single event instance.
   * Works for both standalone events and events that are part of a series.
   *
   * @param subject       the subject of the event to edit
   * @param startDateTime the start date/time of the event to edit
   * @param property      the property to change
   * @param newValue      the new value for the property
   * @throws IllegalArgumentException if event not found or edit causes conflict
   */
  public void editEvent(String subject, LocalDateTime startDateTime,
                        String property, String newValue) {
    performEdit(subject, startDateTime, property, newValue, EditScope.SINGLE);
  }

  /**
   * Edits all events in a series starting from the specified event.
   * If the event is not part of a series, behaves like editEvent.
   *
   * @param subject       the subject of the event to edit
   * @param startDateTime the start date/time of the event to edit
   * @param property      the property to change
   * @param newValue      the new value for the property
   * @throws IllegalArgumentException if event not found or edit causes conflict
   */
  public void editEventsFromDate(String subject, LocalDateTime startDateTime,
                                 String property, String newValue) {
    performEdit(subject, startDateTime, property, newValue, EditScope.FROM_DATE);
  }

  /**
   * Edits all events in a series.
   * If the event is not part of a series, behaves like editEvent.
   *
   * @param subject       the subject of the event to edit
   * @param startDateTime the start date/time of the event to edit
   * @param property      the property to change
   * @param newValue      the new value for the property
   * @throws IllegalArgumentException if event not found or edit causes conflict
   */
  public void editSeries(String subject, LocalDateTime startDateTime,
                         String property, String newValue) {
    performEdit(subject, startDateTime, property, newValue, EditScope.ENTIRE_SERIES);
  }

  /**
   * Checks if the user is busy at a specific date and time.
   *
   * @param dateTime the date and time to check
   * @return true if there are events scheduled at that time
   */
  public boolean isBusyAt(LocalDateTime dateTime) {
    for (Event event : events) {
      if (event.occursAt(dateTime)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Displays all events on a specific date in bullet format.
   *
   * @param date the date to display events for
   * @return a list of events on that day.
   */
  public List<Event> displayEventOn(LocalDate date) {
    List<Event> dayEvents = new ArrayList<>();
    for (Event event : events) {
      if (event.occursOnDate(date)) {
        dayEvents.add(event);
      }
    }

    dayEvents.sort(Event::compareTo);
    return dayEvents;
  }

  /**
   * Displays all events in a date/time range.
   *
   * @param rangeStart the start of the range
   * @param rangeEnd   the end of the range
   * @return a list with events in that range.
   */
  public List<Event> displayEventBetween(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
    List<Event> rangeEvents = new ArrayList<>();
    for (Event event : events) {
      if (event.occursInRange(rangeStart, rangeEnd)) {
        rangeEvents.add(event);
      }
    }

    rangeEvents.sort(Event::compareTo);
    return rangeEvents;
  }

  /**
   * Exports all events in the calendar to a CSV file in Google Calendar format.
   *
   * @param fileName the name of the CSV file
   * @return the path of the CSV
   * @throws IOException if there is error writing the file
   */
  public String exportToCsv(String fileName) throws IOException {
    if (fileName == null || fileName.trim().isEmpty()) {
      throw new IllegalArgumentException("File name cannot be empty");
    }

    if (!fileName.toLowerCase().endsWith(".csv")) {
      fileName += ".csv";
    }

    File file = Paths.get(fileName).toFile();

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {

      writer.write("Subject,Start Date,Start Time,End Date,End Time,All Day Event,"
          + "Description,Location,Private");
      writer.newLine();

      List<Event> sortedEvents = new ArrayList<>(events);
      sortedEvents.sort(Event::compareTo);

      for (Event event : sortedEvents) {
        writeCsvRow(writer, event);
      }
    }

    return file.getAbsolutePath();
  }

  /**
   * Writes a single event as a CSV row.
   *
   * @param writer the BufferedWriter to write to
   * @param event  the event to write
   * @throws IOException if there's an error writing
   */
  private void writeCsvRow(BufferedWriter writer, Event event) throws IOException {

    String row = escapeCsvField(event.getSubject()) + ","
        + event.getStartDateForCsv() + ","
        + event.getStartTimeForCsv() + ","
        + event.getEndDateForCsv() + ","
        + event.getEndTimeForCsv() + ","
        + (event.isAllDayEvent() ? "True" : "False") + ","
        + escapeCsvField(event.getDescription()) + ","
        + escapeCsvField(event.getLocation()) + ","
        + event.getPrivateForCsv();

    writer.write(row);
    writer.newLine();
  }

  /**
   * Escapes a field for CSV format.
   * Fields containing commas, quotes, or newlines are wrapped in quotes,
   * and internal quotes are doubled.
   *
   * @param field the field to escape
   * @return the escaped field
   */
  private String escapeCsvField(String field) {
    if (field == null || field.isEmpty()) {
      return "";
    }

    boolean needsEscaping = field.contains(",")
        || field.contains("\"")
        || field.contains("\n")
        || field.contains("\r");

    if (needsEscaping) {
      String escaped = field.replace("\"", "\"\"");
      return "\"" + escaped + "\"";
    }

    return field;
  }

  /**
   * Get all events in the list.
   *
   * @return a list of events.
   */
  public List<Event> getEvents() {
    return new ArrayList<>(events);
  }

  /**
   * Get all events in the list.
   *
   * @return a list of events.
   */
  public List<EventSeries> getEventSeriesList() {
    return new ArrayList<>(eventSeriesList);
  }

  /**
   * Remove a particular event.
   *
   * @param e the event itself.
   */
  public void removeEvent(Event e) {
    events.remove(e);
  }

  /**
   * Clear all events from Calendar.
   */
  public void clear() {
    events.clear();
    eventSeriesList.clear();
  }
}