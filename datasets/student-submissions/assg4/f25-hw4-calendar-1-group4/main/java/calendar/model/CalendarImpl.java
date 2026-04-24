package calendar.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Concrete implementation of the Icalendar interface.
 * Manages creation, editing, exporting, and querying of events.
 * Stores events in-memory and supports both single and recurring entries.
 */
public class CalendarImpl implements Icalendar {

  private final Map<Integer, Event> events;
  private int nextEventId;
  private int nextSeriesId;

  /**
   * Constructs a new CalendarImpl instance.
   * Initializes the event storage map and sets the event and series ID counters to zero.
   */
  public CalendarImpl() {
    events = new HashMap<>();
    nextEventId = 0;
    nextSeriesId = 0;
  }

  /**
   * Generates and returns a new unique series ID.
   *
   * @return a newly generated unique series ID
   */
  private String generateSeriesId() {
    return "series-" + (nextSeriesId++);
  }

  /**
   * Generates and returns a new unique event ID.
   *
   * @return a newly generated unique event ID
   */
  private int generateEventId() {
    return nextEventId++;
  }

  /**
   * Parses a weekday string (e.g., "MWF") into a set of DayOfWeek values.
   * Removes the event if invalid characters are found.
   *
   * @param weekdays the weekday abbreviation string
   * @param eventId the ID of the event being processed
   * @return a set of parsed DayOfWeek values
   */
  private Set<DayOfWeek> parseWeekdays(String weekdays, Integer eventId) {
    Set<DayOfWeek> days = new HashSet<>();
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
          events.remove(eventId);
          throw new IllegalArgumentException("Invalid repeat days entered.");
      }
    }
    return days;
  }


  /**
   * Creates a new single event and adds it to the internal map.
   * Prevents creation if the requested time slot overlaps with an existing event.
   */
  @Override
  public int createEvent(String subject, String startDateTime, String endDateTime) {
    String seriesId = generateSeriesId();
    int eventId = generateEventId();
    LocalDateTime start;
    LocalDateTime end;
    try {
      start = LocalDateTime.parse(startDateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
      end = LocalDateTime.parse(endDateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid dates entered.");
    }
    isValidEvent(start, end, subject);
    Event newEvent = new Event(subject, startDateTime, endDateTime, seriesId);
    events.put(eventId, newEvent);
    return eventId;
  }

  /**
   * Updates an event to form a recurring series based on specified
   * weekdays and recurrence rules.
   */
  @Override
  public String updateEventDetails(int eventId, String weekdays, Integer occurrences,
                                   String untilDate) {
    Event baseEvent = events.get(eventId);
    if (baseEvent == null) {
      throw new IllegalArgumentException("Invalid event id entered.");
    }
    String seriesId = baseEvent.getSeriesId();
    boolean hasWeekdays = weekdays != null && !weekdays.isBlank();
    boolean hasOccurrences = occurrences != null && occurrences > 0;
    boolean hasUntilDate = untilDate != null && !untilDate.isBlank();
    if (hasWeekdays && hasOccurrences && !hasUntilDate) {
      Set<DayOfWeek> repeatDays = parseWeekdays(weekdays, eventId);
      return updateRecurringEventsByOccurrences(eventId, repeatDays, occurrences, seriesId);
    }
    if (hasWeekdays && hasUntilDate && !hasOccurrences) {
      Set<DayOfWeek> repeatDays = parseWeekdays(weekdays, eventId);
      return updateRecurringEventsUntilDate(eventId, repeatDays, untilDate, seriesId);
    }
    return seriesId;
  }

  /**
   * Generates recurring events up to a specified end date.
   *
   * @param eventId the base event ID
   * @param repeatDays the days of recurrence
   * @param untilDate the date until which the recurrence applies
   * @param seriesId the shared series identifier
   * @return the series ID if successful
   */
  private String updateRecurringEventsUntilDate(Integer eventId, Set<DayOfWeek> repeatDays,
                                                String untilDate, String seriesId) {
    DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
    Event baseEvent = events.get(eventId);
    LocalDate currentDate = baseEvent.getStartDate();
    LocalDate endDate;
    try {
      endDate = LocalDate.parse(untilDate, dateFormatter);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid until date entered.");
    }
    DayOfWeek baseDay = baseEvent.getStartDate().getDayOfWeek();
    boolean baseIncluded = repeatDays.contains(baseDay);
    if (baseIncluded) {
      currentDate = currentDate.plusDays(1);
    }
    while (!currentDate.isAfter(endDate)) {
      if (repeatDays.contains(currentDate.getDayOfWeek())) {
        checkAndAddRecurringEvent(eventId, seriesId, currentDate);
      }
      currentDate = currentDate.plusDays(1);
    }
    if (!baseIncluded) {
      events.remove(eventId);
    }
    return seriesId;
  }

  /**
   * Generates a fixed number of recurring events based on an occurrence count.
   *
   * @param eventId the base event ID
   * @param repeatDays the recurrence days
   * @param occurrences number of events to generate
   * @param seriesId the shared series identifier
   * @return the series ID if successful
   */
  private String updateRecurringEventsByOccurrences(Integer eventId, Set<DayOfWeek> repeatDays,
                                                    int occurrences, String seriesId) {
    Event baseEvent = events.get(eventId);
    LocalDate currentDate = baseEvent.getStartDate();
    DayOfWeek baseDay = baseEvent.getStartDate().getDayOfWeek();
    int created = 0;
    boolean baseIncluded = repeatDays.contains(baseDay);
    if (baseIncluded) {
      created++;
      currentDate = currentDate.plusDays(1);
    }
    while (created < occurrences) {
      if (repeatDays.contains(currentDate.getDayOfWeek())) {
        checkAndAddRecurringEvent(eventId, seriesId, currentDate);
        created++;
      }
      currentDate = currentDate.plusDays(1);
    }
    if (!baseIncluded) {
      events.remove(eventId);
    }
    return seriesId;
  }

  /**
   * Adds a recurring event instance if no overlap exists.
   *
   * @param baseEvent the original event
   * @param startDateTime the new start time
   * @param endDateTime the new end time
   * @param seriesId the shared series ID
   */
  private void addRecurringEvent(Event baseEvent, LocalDateTime startDateTime,
                                 LocalDateTime endDateTime, String seriesId) {
    DateTimeFormatter dtFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    isValidEvent(startDateTime, endDateTime, baseEvent.getSubject());

    int eventId = generateEventId();
    String startStr = startDateTime.format(dtFormatter);
    String endStr = endDateTime.format(dtFormatter);
    Event recurringEvent = new Event(
        baseEvent.getSubject(), startStr, endStr, seriesId
    );
    events.put(eventId, recurringEvent);
  }

  /**
   * Edits a single event instance based on subject and date-time.
   *
   * @param subject event title
   * @param startDateTime start time in ISO-8601
   * @param endDateTime end time in ISO-8601
   * @param propertyName property to modify
   * @param newPropertyValue new value to apply
   * @return edited event ID or -1 if not found
   */
  @Override
  public int editSingleEventInstance(String subject, String startDateTime, String endDateTime,
                                     String propertyName, String newPropertyValue) {
    DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    LocalDateTime start = LocalDateTime.parse(startDateTime, formatter);
    LocalDateTime end = LocalDateTime.parse(endDateTime, formatter);

    for (Map.Entry<Integer, Event> entry : events.entrySet()) {
      Event e = entry.getValue();
      if (e.getSubject().equalsIgnoreCase(subject)
          && e.getStartDate().equals(start.toLocalDate())
          && e.getStartTime().equals(start.toLocalTime())
          && e.getEndDate().equals(end.toLocalDate())
          && e.getEndTime().equals(end.toLocalTime())) {

        applyEdit(e, propertyName, newPropertyValue, e, e.getSeriesId());
        return entry.getKey();
      }
    }
    return -1;
  }

  /**
   * Edits an event series from a specified date onward.
   *
   * @param subject event title
   * @param startDateTime edit start time
   * @param propertyName property to modify
   * @param newPropertyValue new value to apply
   * @param applyToFutureOnly true if only future events are modified
   * @return edited event ID or -1 if not found
   */
  @Override
  public int editSeriesEvents(String subject, String startDateTime,
                              String propertyName, String newPropertyValue,
                              boolean applyToFutureOnly) {
    DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    LocalDateTime targetStart = LocalDateTime.parse(startDateTime, formatter);
    for (Map.Entry<Integer, Event> entry : events.entrySet()) {
      Event e = entry.getValue();
      if (e.getSubject().equalsIgnoreCase(subject)
          && e.getStartDate().equals(targetStart.toLocalDate())
          && e.getStartTime().equals(targetStart.toLocalTime())) {
        String seriesId = e.getSeriesId();
        if (isRecurringSeries(seriesId)) {
          String updatedSeriesId = applyToFutureOnly
              && (propertyName.equalsIgnoreCase("start")
                  || propertyName.equalsIgnoreCase("end"))
              ? generateSeriesId()
              : seriesId;
          for (Event ev : events.values()) {
            if (seriesId.equals(ev.getSeriesId())) {
              LocalDateTime evStart = LocalDateTime.of(ev.getStartDate(), ev.getStartTime());
              if (!applyToFutureOnly) {
                applyEdit(ev, propertyName, newPropertyValue, e, seriesId);
              } else if (!evStart.isBefore(targetStart)) {
                applyEdit(ev, propertyName, newPropertyValue, e, updatedSeriesId);
              }
            }
          }
        } else {
          applyEdit(e, propertyName, newPropertyValue, e, e.getSeriesId());
        }
        return entry.getKey();
      }
    }

    return -1;
  }


  /**
   * Checks whether the given series ID corresponds to a recurring series.
   * A series is considered recurring if more than one event shares the same series ID.
   *
   * @param seriesId the series identifier to check
   * @return true if multiple events belong to the same series, false otherwise
   */
  private boolean isRecurringSeries(String seriesId) {
    int count = 0;
    for (Event e : events.values()) {
      if (e.getSeriesId().equals(seriesId)) {
        count++;
        if (count > 1) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Prints all events for a specific date.
   *
   * @param dateString date in ISO-8601 format
   * @return list of formatted event descriptions
   */
  @Override
  public List<String> printEventsOn(String dateString) {
    DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
    LocalDate targetDate = LocalDate.parse(dateString, dateFormatter);

    List<String> result = new ArrayList<>();

    for (Event e : events.values()) {
      if (e.getStartDate().equals(targetDate)) {
        StringBuilder sb = new StringBuilder("• ");
        sb.append(e.getSubject()).append(": ")
            .append(e.getStartTime()).append(" - ").append(e.getEndTime());
        if (e.getLocation() != null) {
          sb.append(" @ ").append(e.getLocation().name().toLowerCase());
        }
        result.add(sb.toString());
      }
    }
    return result;
  }

  /**
   * Prints all events that fall within a given date-time interval.
   *
   * @param startDateTime interval start
   * @param endDateTime interval end
   * @return list of formatted event descriptions
   */
  @Override
  public List<String> printEventsInInterval(String startDateTime, String endDateTime) {
    DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    LocalDateTime start = LocalDateTime.parse(startDateTime, formatter);
    LocalDateTime end = LocalDateTime.parse(endDateTime, formatter);

    List<String> result = new ArrayList<>();
    for (Event e : events.values()) {
      LocalDateTime eventStart = LocalDateTime.of(e.getStartDate(), e.getStartTime());
      LocalDateTime eventEnd = LocalDateTime.of(e.getEndDate(), e.getEndTime());
      boolean overlaps = !eventEnd.isBefore(start) && !eventStart.isAfter(end);

      if (overlaps) {
        StringBuilder sb = new StringBuilder();
        sb.append(e.getSubject())
            .append(" starting on ").append(e.getStartDate())
            .append(" at ").append(e.getStartTime())
            .append(", ending on ").append(e.getEndDate())
            .append(" at ").append(e.getEndTime());

        if (e.getLocation() != null) {
          sb.append(" @ ").append(e.getLocation().name().toLowerCase());
        }

        result.add(sb.toString());
      }
    }

    return result;
  }

  /**
   * Exports all events to a list of CSV rows compatible with Google Calendar format.
   *
   * @param filename target filename
   * @return list of CSV rows representing events
   */
  @Override
  public List<String[]> exportCalendar(String filename) {
    List<String[]> rows = new ArrayList<>();

    for (Event e : events.values()) {
      boolean isAllDayEvent =
          e.getStartTime().equals(LocalTime.of(8, 0))
              && e.getEndTime().equals(LocalTime.of(17, 0));

      String[] row = {
          escapeCsv(e.getSubject()),
          e.getStartDate().toString(),
          e.getStartTime().toString(),
          e.getEndDate().toString(),
          e.getEndTime().toString(),
          String.valueOf(isAllDayEvent),
          escapeCsvOrEmpty(e.getLocation() != null ? e.getLocation().name() : null),
          escapeCsvOrEmpty(e.getDescription()),
          escapeCsv(e.getStatus() != null ? e.getStatus().name() : null)
      };
      rows.add(row);
    }

    return rows;
  }

  /**
   * Escapes special characters in a string for safe CSV formatting.
   * Wraps the value in quotes if it contains commas or double quotes.
   *
   * @param value the string to escape
   * @return a CSV-safe version of the string, or an empty string if null or blank
   */
  private String escapeCsv(String value) {
    if (value == null || value.isBlank()) {
      return "";
    }
    if (value.contains(",") || value.contains("\"")) {
      return "\"" + value.replace("\"", "\"\"") + "\"";
    }
    return value;
  }

  /**
   * Returns a CSV-safe version of the given field or an empty string if null.
   * Delegates to escapeCsv(String) for proper escaping.
   *
   * @param field the input string to format
   * @return escaped CSV string, or an empty string if the field is null
   */
  private String escapeCsvOrEmpty(String field) {
    if (field == null) {
      return "";
    }
    return escapeCsv(field);
  }

  /**
   * Applies the specified property update to an event. Supports updating all editable fields:
   * subject, description, location, status, start time, and end time.
   *
   * @param e         the event to be modified
   * @param field     the name of the property to update (case-insensitive)
   * @param value     the new value to assign to the field (must be valid for that field)
   * @param baseEvent the original matched event from which the edit started; used to determine
   *                  whether full conversion is needed or only time update
   * @param seriesId  the series ID to assign (can be new if detaching from original series)
   * @throws IllegalArgumentException if the field name is invalid or the value fails validation
   */
  private void applyEdit(Event e, String field, String value, Event baseEvent, String seriesId) {
    if (field.equalsIgnoreCase("subject")
        || field.equalsIgnoreCase("start")
        || field.equalsIgnoreCase("end")) {
      validateEditConflict(e, field, value);
    }
    switch (field.toLowerCase()) {
      case "subject": e.setSubject(value);
        break;
      case "description": e.setDescription(value);
        break;
      case "location":
        validateLocation(value);
        e.setLocation(EventLocation.valueOf(value.toUpperCase()));
        break;
      case "status":
        validateStatus(value);
        e.setStatus(EventStatus.valueOf(value.toUpperCase()));
        break;
      case "start":
        e.setSeriesId(seriesId);
        if (!e.equals(baseEvent)) {
          LocalTime time = LocalTime.from(LocalDateTime.parse(value));
          e.setStartTime(time);
          break;
        }
        validateTime(value, true, e);
        String endDate = LocalDateTime.of(e.getEndDate(), e.getEndTime()).toString();
        e.convertDateTimeToFormat(value, endDate);
        break;
      case "end":
        e.setSeriesId(seriesId);
        if (!e.equals(baseEvent)) {
          LocalTime time = LocalTime.from(LocalDateTime.parse(value));
          e.setEndTime(time);
          break;
        }
        validateTime(value, false, e);
        String startDate = LocalDateTime.of(e.getStartDate(), e.getStartTime()).toString();
        e.convertDateTimeToFormat(startDate, value);
        break;
      default:
        throw new IllegalArgumentException("Invalid field: " + field);
    }
  }

  /**
   * Validates that the specified time value maintains a proper chronological order.
   * Ensures the start time precedes the end time and vice versa for an event.
   *
   * @param value the new time value in ISO-8601 format
   * @param isStart true if validating a start time, false if validating an end time
   * @param e the event whose time fields are being compared
   * @throws IllegalArgumentException if the time violates chronological constraints
   */
  private void validateTime(String value, boolean isStart, Event e) {
    LocalDateTime time = LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    if (isStart) {
      if (!time.isBefore(LocalDateTime.of(e.getEndDate(), e.getEndTime()))) {
        throw new IllegalArgumentException("Date and Time cannot be before end date");
      }
    } else {
      if (!time.isAfter(LocalDateTime.of(e.getStartDate(), e.getStartTime()))) {
        throw new IllegalArgumentException("Date and Time cannot be before end date");
      }
    }
  }

  /**
   * Validates that applying an edit does not create a duplicate event conflict.
   * Compares the updated event values against all existing events to ensure
   * no other event shares the same subject, start, and end time.
   *
   * @param e the event being edited
   * @param field the field being modified (e.g., "subject", "start", "end")
   * @param value the new value proposed for the field
   * @throws IllegalArgumentException if a conflicting event already exists
   */
  private void validateEditConflict(Event e, String field, String value) {
    String newSubject = e.getSubject();
    LocalDateTime newStart = LocalDateTime.of(e.getStartDate(), e.getStartTime());
    LocalDateTime newEnd = LocalDateTime.of(e.getEndDate(), e.getEndTime());
    switch (field.toLowerCase()) {
      case "subject":
        newSubject = value;
        break;
      case "start":
        newStart = LocalDateTime.parse(value);
        break;
      case "end":
        newEnd = LocalDateTime.parse(value);
        break;
      default:
        throw new IllegalArgumentException("Invalid field: " + field);
    }
    for (Map.Entry<Integer, Event> entry : events.entrySet()) {
      Event other = entry.getValue();
      if (other == e) {
        continue;
      }
      boolean sameSubject = other.getSubject().equalsIgnoreCase(newSubject);
      boolean sameStart = other.getStartDate().equals(newStart.toLocalDate())
          && other.getStartTime().equals(newStart.toLocalTime());
      boolean sameEnd = other.getEndDate().equals(newEnd.toLocalDate())
          && other.getEndTime().equals(newEnd.toLocalTime());
      if (sameSubject && sameStart && sameEnd) {
        throw new IllegalArgumentException("Conflict: another event with same subject, start and "
            + "end exists in a different series.\n");
      }
    }
  }

  /**
   * Validates that a new event’s time range and subject are valid and non-conflicting.
   * Ensures the start time is not before the current date, the end time follows the start,
   * and no duplicate event with the same subject and times already exists.
   *
   * @param newStart the proposed start date and time
   * @param newEnd the proposed end date and time
   * @param subjectName the event subject being created
   * @return false if no conflicts are found
   * @throws IllegalArgumentException if the event violates time or duplication rules
   */
  private void isValidEvent(LocalDateTime newStart, LocalDateTime newEnd, String subjectName) {
    LocalDateTime currentDateTime = LocalDateTime.now();
    if (newStart.isBefore(currentDateTime)) {
      throw new IllegalArgumentException("Date and Time cannot be before current date");
    }
    if (newEnd.isBefore(newStart)) {
      throw new IllegalArgumentException("End date cannot be before start date");
    }
    for (Event e : events.values()) {
      LocalDateTime existingStart = LocalDateTime.of(e.getStartDate(), e.getStartTime());
      LocalDateTime existingEnd = LocalDateTime.of(e.getEndDate(), e.getEndTime());
      if (existingStart.equals(newStart) && existingEnd.equals(newEnd)
          && e.getSubject().equals(subjectName)) {
        throw new IllegalArgumentException("Duplicate event cannot be added");
      }
    }
  }

  /**
   * Checks if the user is busy at a given date-time.
   *
   * @param dateTime ISO-8601 date-time to check
   * @return true if any event overlaps, false otherwise
   */
  @Override
  public boolean checkBusyStatus(String dateTime) {
    LocalDateTime checkTime = LocalDateTime.parse(dateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

    for (Event e : events.values()) {
      LocalDateTime eventStart = LocalDateTime.of(e.getStartDate(), e.getStartTime());
      LocalDateTime eventEnd = LocalDateTime.of(e.getEndDate(), e.getEndTime());

      if (!checkTime.isBefore(eventStart) && !checkTime.isAfter(eventEnd)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Validates that the provided event status value is valid.
   * Accepts only PUBLIC or PRIVATE as valid options.
   *
   * @param value the status value to validate
   * @throws IllegalArgumentException if the value is not a valid status
   */
  private void validateStatus(String value) {
    try {
      EventStatus.valueOf(value.toUpperCase());
    } catch (IllegalArgumentException ex) {
      throw new IllegalArgumentException("Invalid status. Allowed: PUBLIC, PRIVATE.");
    }
  }

  /**
   * Validates that the provided event location value is valid.
   * Accepts only PHYSICAL or ONLINE as valid options.
   *
   * @param value the location value to validate
   * @throws IllegalArgumentException if the value is not a valid location
   */
  private void validateLocation(String value) {
    try {
      EventLocation.valueOf(value.toUpperCase());
    } catch (IllegalArgumentException ex) {
      throw new IllegalArgumentException("Invalid location. Allowed: PHYSICAL, ONLINE.");
    }
  }

  private void checkAndAddRecurringEvent(Integer eventId, String seriesId, LocalDate currentDate) {
    Event baseEvent = events.get(eventId);
    LocalTime startTime = baseEvent.getStartTime();
    LocalTime endTime = baseEvent.getEndTime();
    LocalDateTime startDateTime = LocalDateTime.of(currentDate, startTime);
    LocalDateTime endDateTime = LocalDateTime.of(currentDate, endTime);
    addRecurringEvent(baseEvent, startDateTime, endDateTime, seriesId);
  }
}