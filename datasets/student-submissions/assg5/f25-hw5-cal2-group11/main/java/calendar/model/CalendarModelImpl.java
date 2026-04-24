package calendar.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Enhanced implementation of CalendarModel supporting multiple calendars.
 */
public class CalendarModelImpl implements CalendarModel {

  private Map<String, Calendar> calendars = new HashMap<>();
  private String currentCalendarName = null;

  /**
   * Creates a new calendar with the specified name and timezone.
   *
   * @param name     the name of the calendar to create
   * @param timezone the timezone for the calendar
   * @throws IllegalArgumentException if a calendar with the given name already exists
   */
  public void createCalendar(String name, ZoneId timezone) {
    if (calendars.containsKey(name)) {
      throw new IllegalArgumentException("Calendar already exists: " + name);
    }
    if (timezone == null) {
      throw new IllegalArgumentException("Timezone cannot be null");
    }

    Calendar calendar = new Calendar.Builder(name).timezone(timezone).build();
    calendars.put(name, calendar);
    if (currentCalendarName == null) {
      currentCalendarName = name;
    }
  }

  /**
   * Switches to use the specified calendar as the current active calendar.
   *
   * @param calendarName the name of the calendar to use
   * @throws IllegalArgumentException if the specified calendar does not exist
   */

  public void useCalendar(String calendarName) {
    if (!calendars.containsKey(calendarName)) {
      throw new IllegalArgumentException("Calendar not found: " + calendarName);
    }
    this.currentCalendarName = calendarName;
  }

  /**
   * Gets the current calendar context.
   */
  public String getCurrentCalendarName() {
    return currentCalendarName;
  }

  /**
   * Copies a single event from source to target calendar.
   */
  public void copyEvent(String eventName, LocalDateTime sourceStartDateTime,
                        String targetCalendarName, LocalDateTime targetStartDateTime) {
    Calendar sourceCalendar = getCurrentCalendarOrThrow();
    Calendar targetCalendar = getCalendarOrThrow(targetCalendarName);
    Event sourceEvent = findEvent(sourceCalendar, eventName, sourceStartDateTime);

    if (sourceEvent == null) {
      throw new IllegalArgumentException("Event '" + eventName + "' not found at specified time");
    }
    copyEventInternal(sourceEvent, sourceCalendar, targetCalendar, targetStartDateTime);
  }

  private void copyEventInternal(Event sourceEvent, Calendar sourceCalendar,
                                 Calendar targetCalendar, LocalDateTime targetStartDateTime) {
    ZonedDateTime sourceStartZoned = sourceEvent.getStartDateTime()
        .atZone(sourceCalendar.getTimezone());
    ZonedDateTime sourceEndZoned = sourceEvent.getEndDateTime()
        .atZone(sourceCalendar.getTimezone());

    ZonedDateTime targetStartZoned = sourceStartZoned
        .withZoneSameInstant(targetCalendar.getTimezone());
    ZonedDateTime targetEndZoned = sourceEndZoned
        .withZoneSameInstant(targetCalendar.getTimezone());

    LocalDateTime finalTargetStart = targetStartDateTime.toLocalDate()
        .atTime(targetStartZoned.toLocalTime());
    LocalDateTime finalTargetEnd = targetStartDateTime.toLocalDate()
        .atTime(targetEndZoned.toLocalTime());

    Event copiedEvent = new Event(sourceEvent.getSubject(), finalTargetStart, finalTargetEnd);
    copiedEvent.setLocation(sourceEvent.getLocation());
    copiedEvent.setDescription(sourceEvent.getDescription());
    copiedEvent.setStatus(sourceEvent.getStatus());
    copiedEvent.setSeriesId(sourceEvent.getSeriesId());

    targetCalendar.addEvent(copiedEvent);
  }

  /**
   * Copies all events from a specific date to target calendar.
   */
  public void copyEventsOnDate(LocalDate sourceDate, String targetCalendarName,
                               LocalDate targetDate) {
    Calendar sourceCalendar = getCurrentCalendarOrThrow();
    Calendar targetCalendar = getCalendarOrThrow(targetCalendarName);
    List<Event> eventsOnDate = getEventOnDate(sourceDate.atStartOfDay());

    for (Event event : eventsOnDate) {
      LocalDateTime targetStartDateTime = targetDate.atTime(event.getStartDateTime().toLocalTime());
      copyEventInternal(event, sourceCalendar, targetCalendar, targetStartDateTime);
    }
  }

  /**
   * Copies all events within a date range to target calendar.
   */

  public void copyEventsBetweenDates(LocalDate startDate, LocalDate endDate,
                                     String targetCalendarName, LocalDate targetStartDate) {
    Calendar sourceCalendar = getCurrentCalendarOrThrow();
    Calendar targetCalendar = getCalendarOrThrow(targetCalendarName);
    List<Event> eventsInRange = getEventsInRange(
        startDate.atStartOfDay(), endDate.atTime(23, 59, 59));

    for (Event event : eventsInRange) {
      LocalDate eventDate = event.getStartDateTime().toLocalDate();
      long daysFromStart = java.time.temporal.ChronoUnit.DAYS.between(startDate, eventDate);

      LocalDate targetEventDate = targetStartDate.plusDays(daysFromStart);
      LocalDateTime targetStartDateTime =
          targetEventDate.atTime(event.getStartDateTime().toLocalTime());

      if (!eventDate.isBefore(startDate) && !eventDate.isAfter(endDate)) {
        copyEventInternal(event, sourceCalendar, targetCalendar, targetStartDateTime);
      }
    }
  }

  /**
   * Edits properties of an existing calendar.
   */
  public void editCalendar(String calendarName, String property, String newValue) {
    Calendar calendar = getCalendarOrThrow(calendarName);

    if (property == null) {
      throw new IllegalArgumentException("Property must not be null");
    }

    switch (property.toLowerCase()) {
      case "name":
        if (calendars.containsKey(newValue)) {
          throw new IllegalArgumentException(
              "Calendar with name '" + newValue + "' already exists");
        }
        Calendar newCalendar = new Calendar.Builder(newValue)
            .timezone(calendar.getTimezone())
            .description(calendar.getDescription())
            .color(calendar.getColor())
            .build();
        for (Event event : calendar.getEvents()) {
          newCalendar.addEvent(event);
        }

        calendars.remove(calendarName);
        calendars.put(newValue, newCalendar);

        if (calendarName.equals(currentCalendarName)) {
          currentCalendarName = newValue;
        }
        break;

      case "timezone":
        try {
          ZoneId newTimezone = ZoneId.of(newValue);
          Calendar updatedCalendar = calendar.copyWithTimezone(newTimezone);
          for (Event event : calendar.getEvents()) {
            updatedCalendar.addEvent(event);
          }

          calendars.put(calendarName, updatedCalendar);
        } catch (Exception e) {
          throw new IllegalArgumentException("Invalid timezone: " + newValue, e);
        }
        break;

      default:
        throw new IllegalArgumentException(
            "Invalid property: " + property + ". Valid properties: name, timezone");
    }
  }

  private Calendar getCurrentCalendar() {
    if (currentCalendarName == null) {
      throw new IllegalStateException("No calendar selected");
    }
    return calendars.get(currentCalendarName);
  }

  private Calendar getCurrentCalendarOrThrow() {
    if (currentCalendarName == null) {
      throw new IllegalStateException(
          "No calendar is currently in use. Use 'use calendar' command first.");
    }
    return getCalendarOrThrow(currentCalendarName);
  }

  private Calendar getCalendarOrThrow(String calendarName) {
    Calendar calendar = calendars.get(calendarName);
    if (calendar == null) {
      throw new IllegalArgumentException("Calendar '" + calendarName + "' does not exist");
    }
    return calendar;
  }

  /**
   * Finds an event by name and start time in the specified calendar.
   */
  private Event findEvent(Calendar calendar, String eventName, LocalDateTime startDateTime) {
    for (Event event : calendar.getEvents()) {
      if (event.getSubject().equals(eventName)
          &&
          event.getStartDateTime().equals(startDateTime)) {
        return event;
      }
    }
    return null;
  }

  @Override
  public void createSingleEvent(String subject, LocalDateTime startDateTime,
                                LocalDateTime endDateTime) {
    Event event = new Event(subject, startDateTime, endDateTime);
    getCurrentCalendar().addEvent(event);
  }

  @Override
  public void createEventSeries(String subject, LocalDateTime startDateTime,
                                LocalDateTime endDateTime, String weekdays, int occurrences) {
    if (startDateTime == null) {
      throw new IllegalArgumentException("Start date/time cannot be null");
    }
    if (occurrences <= 0) {
      throw new IllegalArgumentException("Occurrences must be positive");
    }
    if (weekdays == null || weekdays.isEmpty()) {
      throw new IllegalArgumentException("Weekdays cannot be null or empty");
    }

    String seriesId = UUID.randomUUID().toString();
    List<DayOfWeek> targetDays = parseWeekdays(weekdays);

    if (targetDays.isEmpty()) {
      throw new IllegalArgumentException("At least one valid weekday must be specified");
    }

    LocalTime startTime = startDateTime.toLocalTime();
    LocalTime endTime = endDateTime.toLocalTime();
    LocalDate currentDate = startDateTime.toLocalDate();
    int count = 0;

    int maxIterations = occurrences * 7 + 7;
    int iterations = 0;
    LocalDate maxDate = startDateTime.toLocalDate().plusYears(2);
    LocalDate estimatedEndDate =
        startDateTime.toLocalDate().plusWeeks((occurrences / targetDays.size()) + 4);
    LocalDate hardLimit = estimatedEndDate.isBefore(maxDate) ? estimatedEndDate : maxDate;

    while (count < occurrences && iterations < maxIterations) {
      if (currentDate.isAfter(hardLimit)) {
        throw new IllegalStateException(
            "Could not generate " + occurrences
                + " occurrences within reasonable timeframe. "
                + "Generated " + count + " events. Check weekday pattern: " + weekdays);
      }
      if (targetDays.contains(currentDate.getDayOfWeek())) {
        LocalDateTime newStart = LocalDateTime.of(currentDate, startTime);
        LocalDateTime newEnd = LocalDateTime.of(currentDate, endTime);
        Event event = new Event(subject, newStart, newEnd);
        event.setSeriesId(seriesId);
        getCurrentCalendar().addEvent(event);
        count++;
      }

      currentDate = currentDate.plusDays(1);
      iterations++;
    }
    if (count < occurrences) {
      throw new IllegalStateException(
          "Failed to create event series. Created "
              + count + " of " + occurrences
              + " occurrences after " + iterations
              + " iterations. "
              + "This may indicate an invalid weekday pattern or too many occurrences requested.");
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

  /**
   * Edit a single event.
   *
   * @param target   the event to be edited.
   * @param property the property of that event to be edited.
   * @param value    the new value the property needs to be updated with.
   */
  @Override
  public void editEvent(Event target, String property, String value) {
    if (target == null) {
      throw new IllegalArgumentException("target cannot be null");
    }
    if (property == null || property.trim().isEmpty()) {
      throw new IllegalArgumentException("property cannot be null or empty");
    }
    if (value == null) {
      throw new IllegalArgumentException("value cannot be null or empty");
    }

    if (target.getSeriesId() != null) {
      target.setSeriesId(null);
    }

    applyPropertyChange(target, property, value);

  }

  /**
   * Edit all events series starting from a specified start date.
   * If this event is part of a series then the properties of all events in that
   * series that start at or after the given date and time should be changed.
   *
   * @param target   the event for which we want to change the property.
   * @param property the property whose value need to be updated.
   * @param value    the updated value of the property.
   */
  public void editSeriesFrom(Event target, String property, String value) {

    validateEditInputs(target, property, value);

    if (isNotPartOfSeries(target.getSeriesId())) {
      editEvent(target, property, value);
      return;
    }


    List<Event> eventsToEdit = findEventsInSeriesFrom(
        target.getSeriesId(),
        target.getStartDateTime()
    );

    if (isStartPropertyChange(property)) {
      reassignSeriesId(eventsToEdit);
    }
    applyChangesToMultipleEvents(eventsToEdit, property, value);
  }

  /**
   * Edit the entire series.
   *
   * @param target   the event for which we want to change the property.
   * @param property the property whose value need to be updated.
   * @param value    the updated value of the property.
   */
  public void editEntireSeries(Event target, String property, String value) {
    validateEditInputs(target, property, value);

    if (isNotPartOfSeries(target.getSeriesId())) {
      editEvent(target, property, value);
      return;
    }

    List<Event> eventsToEdit = findAllEventsInSeries(target.getSeriesId());


    if (isStartPropertyChange(property)) {
      reassignSeriesId(eventsToEdit);
    }
    applyChangesToMultipleEvents(eventsToEdit, property, value);

  }

  /**
   * Preserves the original date and time in case of edit events start date
   * from a specific date.
   *
   * @param originalDateTime the original date and time of the event.
   * @param newDateTime      the new date and time of the event.
   * @return the original and the new date time.
   */
  private LocalDateTime preserveDateChangeTime(LocalDateTime originalDateTime,
                                               LocalDateTime newDateTime) {
    return LocalDateTime.of(
        originalDateTime.toLocalDate(),
        newDateTime.toLocalTime()
    );
  }

  /**
   * Method that changes a specific property of an event.
   *
   * @param target   the event to be edited.
   * @param property the property whose value need to be updated.
   * @param value    the updated value of the property.
   */
  private void applyPropertyChange(Event target, String property, String value) {
    String originalSubject = target.getSubject();
    LocalDateTime originalStart = target.getStartDateTime();
    LocalDateTime originalEnd = target.getEndDateTime();

    switch (property.toLowerCase()) {
      case "subject":
        validateSubject(value);
        checkDuplicateForEdit(target, value, originalStart, originalEnd);
        target.setSubject(value);
        break;

      case "start":
        LocalDateTime newStart = parseDateTime(value);
        if (target.getSeriesId() != null) {
          newStart = preserveDateChangeTime(originalStart, newStart);
        }
        validateDateTimeRange(newStart, originalEnd);
        checkDuplicateForEdit(target, originalSubject, newStart, originalEnd);
        target.setStartDateTime(newStart);
        break;

      case "end":
        LocalDateTime newEnd = parseDateTime(value);
        if (target.getSeriesId() != null) {
          newEnd = preserveDateChangeTime(originalEnd, newEnd);
        }
        validateDateTimeRange(originalStart, newEnd);
        checkDuplicateForEdit(target, originalSubject, originalStart, newEnd);
        target.setEndDateTime(newEnd);
        break;

      case "description":
        target.setDescription(value);
        break;

      case "location":
        target.setLocation(value);
        break;

      case "status":
        validateStatus(value);
        target.setStatus(value);
        break;

      default:
        throw new IllegalArgumentException(
            "Invalid property: " + property);
    }
  }

  /**
   * Method that validates the edit inputs.
   *
   * @param target   the event to be edited.
   * @param property the property whose value need to be updated.
   * @param value    the updated value of the property.
   */
  private void validateEditInputs(Event target, String property, String value) {
    validateNotNull(target, "Target event");

    if (property == null || property.trim().isEmpty()) {
      throw new IllegalArgumentException("Property cannot be null or empty");
    }
    if (value == null) {
      throw new IllegalArgumentException("Value cannot be null");
    }
  }

  /**
   * Method that validates the subject.
   *
   * @param subject the subject that needs to be validated.
   */
  private void validateSubject(String subject) {
    if (subject == null || subject.trim().isEmpty()) {
      throw new IllegalArgumentException("Subject cannot be null or empty");
    }
  }

  /**
   * Validates that edit inputs are not null.
   *
   * @param obj       the edit input.
   * @param fieldName the property name.
   */
  private void validateNotNull(Object obj, String fieldName) {
    if (obj == null) {
      throw new IllegalArgumentException(fieldName + " cannot be null");
    }
  }

  /**
   * Validates the status of an event. Must be 'private' or 'public'.
   *
   * @param status the status that needs to be validated.
   */
  private void validateStatus(String status) {
    if (!status.equalsIgnoreCase("public") && !status.equalsIgnoreCase("private")) {
      throw new IllegalArgumentException(
          "Invalid event status. Must be private or public");
    }
  }

  /**
   * Validate the date and time range.
   *
   * @param start the start date and time.
   * @param end   the end date and time.
   */
  private void validateDateTimeRange(LocalDateTime start, LocalDateTime end) {
    if (start.isAfter(end)) {
      throw new IllegalArgumentException("Start time must be before or equal to end time");
    }
  }

  /**
   * Verifies whether an event has same properties or not
   * (i.e. event already exists).
   *
   * @param event   the entire event object.
   * @param subject the subject of the event.
   * @param start   the start date and time of the event.
   * @param end     the end date and time of the event.
   * @return true if event has same properties.
   */
  private boolean hasSameProperties(Event event, String subject,
                                    LocalDateTime start, LocalDateTime end) {
    return event.getSubject().equals(subject)
        &&
        event.getStartDateTime().equals(start)
        &&
        event.getEndDateTime().equals(end);
  }

  /**
   * Verifies that an event is not part of any series.
   *
   * @param seriesId the seriesId to verify.
   * @return null if no seriesId found.
   */
  private boolean isNotPartOfSeries(String seriesId) {
    return seriesId == null || seriesId.trim().isEmpty();
  }

  /**
   * Verifies whether the property we need to change is start time.
   *
   * @param property the property we need to change.
   * @return true if property is 'start'.
   */
  private boolean isStartPropertyChange(String property) {
    return property.equalsIgnoreCase("start");
  }

  /**
   * Find events in series starting from a specified start time.
   *
   * @param seriesId     the seriesId of the series.
   * @param fromDateTime the start from date and time.
   * @return the list of events after a specific start date.
   */
  private List<Event> findEventsInSeriesFrom(String seriesId, LocalDateTime fromDateTime) {
    List<Event> eventsToEdit = new ArrayList<>();

    for (Event event : getCurrentCalendar().getEvents()) {
      if (isEventInSeriesAfterDate(event, seriesId, fromDateTime)) {
        eventsToEdit.add(event);
      }
    }

    return eventsToEdit;
  }

  /**
   * Finds all events in series and adds it to eventsToEdit list.
   *
   * @param seriesId the seriesId of the series.
   * @return the list of all the events.
   */
  private List<Event> findAllEventsInSeries(String seriesId) {
    List<Event> eventsToEdit = new ArrayList<>();

    for (Event event : getCurrentCalendar().getEvents()) {
      if (seriesId.equals(event.getSeriesId())) {
        eventsToEdit.add(event);
      }
    }

    return eventsToEdit;
  }

  /**
   * Verifies that the new start date and time is not before the original
   * start date and time.
   *
   * @param event        the event whose property needs to be edited.
   * @param seriesId     the seriesId of the event.
   * @param fromDateTime the new start date and time.
   * @return true if new start date and time is not before the original start date
   */
  private boolean isEventInSeriesAfterDate(Event event, String seriesId,
                                           LocalDateTime fromDateTime) {
    return seriesId.equals(event.getSeriesId())
        &&
        !event.getStartDateTime().isBefore(fromDateTime);
  }

  /**
   * Reassigns seriesId after editing start date of an event in a series.
   *
   * @param events the list of events to assign new seriesId.
   */
  private void reassignSeriesId(List<Event> events) {
    String newSeriesId = UUID.randomUUID().toString();
    for (Event event : events) {
      event.setSeriesId(newSeriesId);
    }
  }

  /**
   * Applies changes to multiple events.
   *
   * @param events   the events we need to apply changes to.
   * @param property the property of that event to be updated.
   * @param value    the updated value.
   */
  private void applyChangesToMultipleEvents(List<Event> events, String property, String value) {
    for (Event event : events) {
      applyPropertyChange(event, property, value);
    }
  }

  /**
   * Check if any duplicate event exist.
   *
   * @param target  the target event to compare.
   * @param subject the subject of the event.
   * @param start   the start time of the event.
   * @param end     the end time of the event.
   */
  private void checkDuplicateForEdit(Event target, String subject, LocalDateTime start,
                                     LocalDateTime end) {
    for (Event event : getCurrentCalendar().getEvents()) {
      if (event != target && hasSameProperties(event, subject, start, end)) {
        throw new IllegalArgumentException("Duplicate event properties");
      }
    }
  }

  /**
   * Parses date and time of start dateTime and end dateTime.
   *
   * @param value the value dateTime string to parse.
   * @return parsed date and time.
   */
  private LocalDateTime parseDateTime(String value) {
    try {
      return LocalDateTime.parse(value);
    } catch (Exception e) {
      throw new IllegalArgumentException(
          "Invalid date/time format. Expected format: YYYY-MM-DDThh:mm", e);
    }
  }


  @Override
  public List<Event> getEventOnDate(LocalDateTime date) {
    List<Event> result = new ArrayList<>();
    LocalDate queryDate = date.toLocalDate();
    for (Event event : getCurrentCalendar().getEvents()) {
      LocalDate eventDate = event.getStartDateTime().toLocalDate();
      if (eventDate.equals(queryDate)) {
        result.add(event);
      }
    }
    return result;
  }

  @Override
  public List<Event> getEventsInRange(LocalDateTime startDateTime, LocalDateTime endDateTime) {
    List<Event> result = new ArrayList<>();
    for (Event event : getCurrentCalendar().getEvents()) {
      if (!event.getStartDateTime().isAfter(endDateTime)
          &&
          !event.getEndDateTime().isBefore(startDateTime)) {
        result.add(event);
      }
    }
    return result;
  }

  @Override
  public boolean isBusy(LocalDateTime dateTime) {
    for (Event event : getCurrentCalendar().getEvents()) {
      if (!dateTime.isBefore(event.getStartDateTime())
          &&
          dateTime.isBefore(event.getEndDateTime())) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void exportCalendar(String filename) {
    if (filename.toLowerCase().endsWith(".ical")) {
      exportToIcal(filename);
    } else if (filename.toLowerCase().endsWith(".csv")) {
      exportToCsv(filename);
    } else {
      throw new IllegalArgumentException("Unsupported file format. Use .csv or .ical extension");
    }
  }

  private void exportToCsv(String filename) {
    try (PrintWriter writer = new PrintWriter(new File(filename))) {
      writer.println(
          "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,"
              +
              "Private");
      for (Event event : getCurrentCalendar().getEvents()) {
        writer.println(formatEventAsCsv(event));
      }
      System.out.println("Exported to: " + new File(filename).getAbsolutePath());
    } catch (FileNotFoundException e) {
      throw new RuntimeException("Could not write to file: " + filename, e);
    }
  }

  private void exportToIcal(String filename) {
    try (PrintWriter writer = new PrintWriter(new File(filename))) {
      writer.println("BEGIN:VCALENDAR");
      writer.println("VERSION:2.0");
      writer.println("PRODID:-//Calendar Application//EN");
      writer.println("CALSCALE:GREGORIAN");
      writer.println("METHOD:PUBLISH");

      for (Event event : getCurrentCalendar().getEvents()) {
        writer.println(formatEventAsIcal(event));
      }

      writer.println("END:VCALENDAR");
      System.out.println("Exported to: " + new File(filename).getAbsolutePath());
    } catch (FileNotFoundException e) {
      throw new RuntimeException("Could not write to file: " + filename, e);
    }
  }

  private String formatEventAsCsv(Event event) {
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

    String startDate = event.getStartDateTime().toLocalDate().format(dateFormatter);
    String startTime = event.getStartDateTime().toLocalTime().format(timeFormatter);
    String endDate = event.getEndDateTime().toLocalDate().format(dateFormatter);
    String endTime = event.getEndDateTime().toLocalTime().format(timeFormatter);

    String description = (event.getDescription() != null) ? event.getDescription() : "";
    String location = (event.getLocation() != null) ? event.getLocation() : "";
    String privacy = event.getStatus().equalsIgnoreCase("private") ? "True" : "False";

    return String.format("\"%s\",%s,%s,%s,%s,%s,\"%s\",\"%s\",%s",
        event.getSubject(), startDate, startTime, endDate, endTime,
        event.isAllDay() ? "True" : "False", description, location, privacy);
  }

  private String formatEventAsIcal(Event event) {
    DateTimeFormatter icalFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    StringBuilder sb = new StringBuilder();
    sb.append("BEGIN:VEVENT\n");
    sb.append("UID:").append(UUID.randomUUID().toString()).append("\n");
    sb.append("DTSTART:").append(event.getStartDateTime().format(icalFormatter)).append("\n");
    sb.append("DTEND:").append(event.getEndDateTime().format(icalFormatter)).append("\n");
    sb.append("SUMMARY:").append(event.getSubject()).append("\n");

    if (event.getDescription() != null && !event.getDescription().isEmpty()) {
      sb.append("DESCRIPTION:").append(event.getDescription()).append("\n");
    }

    if (event.getLocation() != null && !event.getLocation().isEmpty()) {
      sb.append("LOCATION:").append(event.getLocation()).append("\n");
    }

    if (event.getStatus().equalsIgnoreCase("private")) {
      sb.append("CLASS:PRIVATE\n");
    } else {
      sb.append("CLASS:PUBLIC\n");
    }

    sb.append("END:VEVENT");

    return sb.toString();
  }
}