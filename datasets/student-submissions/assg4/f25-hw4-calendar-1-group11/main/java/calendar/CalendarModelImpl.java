package calendar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of the CalendarModel interface.
 */
public class CalendarModelImpl implements CalendarModel {
  private List<Event> events = new ArrayList<>();

  /**
   * Method that creates a single event.
   *
   * @param subject the subject of the event.
   * @param startDateTime the start date and time of the event.
   * @param endDateTime the end date and time of the event.
   */
  @Override
  public void createSingleEvent(String subject, LocalDateTime startDateTime,
                                LocalDateTime endDateTime) {
    Event tempEvent = new Event(subject, startDateTime, endDateTime);

    if (isDuplicate(subject, startDateTime, tempEvent.getEndDateTime())) {
      throw new IllegalArgumentException("Duplicate event already exists");
    }
    events.add(tempEvent);
  }

  /**
   * Method that checks whether the event already exists. Returns true if found, otherwise false.
   *
   * @param subject the subject of the new event.
   * @param startDateTime the start date and time of the new event.
   * @param endDateTime the end date and time of the new event.
   *
   * @return true if event is found, otherwise false.
   */
  boolean isDuplicate(String subject, LocalDateTime startDateTime, LocalDateTime endDateTime) {
    for (Event event : events) {
      if (hasSameProperties(event, subject, startDateTime, endDateTime)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Method that creates an Event Series with the specified parameters.
   *
   * @param subject the subject of the event series.
   * @param startDateTime the start date and time of the event series.
   * @param endDateTime the end date and time of the event series.
   * @param weekdays the days of the week that the event must be repeated.
   * @param occurrences no. of times the event must be repeated.
   */
  @Override
  public void createEventSeries(String subject, LocalDateTime startDateTime,
                                LocalDateTime endDateTime, String weekdays, int occurrences) {
    if (subject == null || subject.trim().isEmpty()) {
      throw new IllegalArgumentException("Subject cannot be null or empty");
    }
    if (startDateTime == null || endDateTime == null) {
      throw new IllegalArgumentException("Start and end times cannot be null");
    }
    if (weekdays == null || weekdays.isEmpty()) {
      throw new IllegalArgumentException("Weekdays cannot be null or empty");
    }
    if (occurrences <= 0) {
      throw new IllegalArgumentException("Occurrences must be positive");
    }

    LocalDate startDate = startDateTime.toLocalDate();
    LocalDate endDate = endDateTime.toLocalDate();
    if (!startDate.equals(endDate)) {
      throw new IllegalArgumentException("Events in a series cannot span multiple days");
    }

    String seriesId = UUID.randomUUID().toString();

    List<DayOfWeek> targetDays = parseWeekdays(weekdays);

    LocalTime startTime = startDateTime.toLocalTime();
    LocalTime endTime = endDateTime.toLocalTime();
    LocalDate currentDate = startDate;
    int count = 0;

    while (count < occurrences) {
      if (targetDays.contains(currentDate.getDayOfWeek())) {
        LocalDateTime newStartDateTime = LocalDateTime.of(currentDate, startTime);
        LocalDateTime newEndDateTime = LocalDateTime.of(currentDate, endTime);

        if (!isDuplicate(subject, newStartDateTime, newEndDateTime)) {
          Event newEvent = new Event(subject, newStartDateTime, newEndDateTime);
          newEvent.setSeriesId(seriesId);
          events.add(newEvent);
          count++;
        }
      }
      currentDate = currentDate.plusDays(1);

      if (currentDate.isAfter(startDate.plusYears(2))) {
        throw new IllegalArgumentException("Could not generate series - invalid weekday pattern");
      }
    }
  }

  /**
   * Parses the weekdays on which the series is to be repeated.
   *
   * @param weekdays the weekdays on which the series is to be repeated.
   *
   * @return parsed weekdays.
   */
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
          throw new IllegalArgumentException("Invalid weekday code: " + c);
      }
    }

    if (days.isEmpty()) {
      throw new IllegalArgumentException("No valid weekdays specified");
    }

    return days;
  }

  /**
   * Edit a single event.
   *
   * @param target the event to be edited.
   * @param property the property of that event to be edited.
   * @param value the new value the property needs to be updated with.
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
   * @param target the event for which we want to change the property.
   * @param property the property whose value need to be updated.
   * @param value the updated value of the property.
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
   * @param target the event for which we want to change the property.
   * @param property the property whose value need to be updated.
   * @param value the updated value of the property.
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
   * @param newDateTime the new date and time of the event.
   *
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
   * @param target the event to be edited.
   * @param property the property whose value need to be updated.
   * @param value the updated value of the property.
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
        checkDuplicateForEdit(target, originalSubject, newStart, originalEnd);
        target.setStartDateTime(newStart);
        break;

      case "end":
        LocalDateTime newEnd = parseDateTime(value);
        if (target.getSeriesId() != null) {
          newEnd = preserveDateChangeTime(originalEnd, newEnd);
        }

        validateEndTimeAfterStart(newEnd, originalStart);
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
   * @param target the event to be edited.
   * @param property the property whose value need to be updated.
   * @param value the updated value of the property.
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
   * @param obj the edit input.
   * @param fieldName the property name.
   */
  private void validateNotNull(Object obj, String fieldName) {
    if (obj == null) {
      throw new IllegalArgumentException(fieldName + " cannot be null");
    }
  }

  /**
   * Validates that end time always comes after start.
   *
   * @param endTime the end time.
   * @param startTime the start time.
   */
  private void validateEndTimeAfterStart(LocalDateTime endTime, LocalDateTime startTime) {
    if (endTime.isBefore(startTime) || endTime.isEqual(startTime)) {
      throw new IllegalArgumentException("End time must be after start time");
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
   * @param end the end date and time.
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
   *
   * @param event the entire event object.
   *
   * @param subject the subject of the event.
   *
   * @param start the start date and time of the event.
   *
   * @param end the end date and time of the event.
   *
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
   *
   * @return null if no seriesId found.
   */
  private boolean isNotPartOfSeries(String seriesId) {
    return seriesId == null || seriesId.trim().isEmpty();
  }

  /**
   * Verifies whether the property we need to change is start time.
   *
   * @param property the property we need to change.
   *
   * @return true if property is 'start'.
   */
  private boolean isStartPropertyChange(String property) {
    return property.equalsIgnoreCase("start");
  }

  /**
   * Find events in series starting from a specified start time.
   *
   * @param seriesId the seriesId of the series.
   * @param fromDateTime the start from date and time.
   *
   * @return the list of events after a specific start date.
   */
  private List<Event> findEventsInSeriesFrom(String seriesId, LocalDateTime fromDateTime) {
    List<Event> eventsToEdit = new ArrayList<>();

    for (Event event : events) {
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
   *
   * @return the list of all the events.
   */
  private List<Event> findAllEventsInSeries(String seriesId) {
    List<Event> eventsToEdit = new ArrayList<>();

    for (Event event : events) {
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
   * @param event the event whose property needs to be edited.
   * @param seriesId the seriesId of the event.
   * @param fromDateTime the new start date and time.
   *
   * @return true if new start date and time is not before the original
   *     start date and time, otherwise false.
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
   * @param events the events we need to apply changes to.
   * @param property the property of that event to be updated.
   * @param value the updated value.
   */
  private void applyChangesToMultipleEvents(List<Event> events, String property, String value) {
    for (Event event : events) {
      applyPropertyChange(event, property, value);
    }
  }

  /**
   * Check if any duplicate event exist.
   *
   * @param target the target event to compare.
   * @param subject the subject of the event.
   * @param start the start time of the event.
   * @param end the end time of the event.
   */
  private void checkDuplicateForEdit(Event target, String subject, LocalDateTime start,
                                     LocalDateTime end) {
    for (Event event : events) {
      if (event != target && hasSameProperties(event, subject, start, end)) {
        throw new IllegalArgumentException("Duplicate event properties");
      }
    }
  }

  /**
   * Parses date and time of start dateTime and end dateTime.
   *
   * @param value the value dateTime string to parse.
   *
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

  /**
   * Fetches events on a specific date.
   *
   * @param date the date of the event to be retrieved.
   *
   * @return the list of events.
   */
  @Override
  public List<Event> getEventOnDate(LocalDateTime date) {
    LocalDate queryDate = date.toLocalDate();

    List<Event> eventsOnDate = new ArrayList<>();

    for (Event event : events) {
      LocalDate eventStartDate = event.getStartDateTime().toLocalDate();
      LocalDate eventEndDate = event.getEndDateTime().toLocalDate();
      if (!queryDate.isBefore(eventStartDate) && !queryDate.isAfter(eventEndDate)) {
        eventsOnDate.add(event);
      }
    }
    eventsOnDate.sort((e1, e2) -> e1.getStartDateTime().compareTo(e2.getStartDateTime()));

    return eventsOnDate;
  }

  /**
   * Gets Events in a specified range.
   *
   * @param startTimeDate the start time of the event.
   * @param endTimeDate the end time of the event.
   *
   * @return all the events in a given range.
   */
  @Override
  public List<Event> getEventsInRange(LocalDateTime startTimeDate, LocalDateTime endTimeDate) {
    if (startTimeDate == null || endTimeDate == null) {
      throw new IllegalArgumentException("Start and end date/time cannot be null");
    }
    if (startTimeDate.isAfter(endTimeDate)) {
      throw new IllegalArgumentException("Start time must be before or equal to end time");
    }

    List<Event> eventsInRange = new ArrayList<>();

    for (Event event : events) {
      boolean overlaps = event.getStartDateTime().isBefore(endTimeDate)
          &&
          event.getEndDateTime().isAfter(startTimeDate);

      if (overlaps) {
        eventsInRange.add(event);
      }
    }
    eventsInRange.sort((e1, e2) -> e1.getStartDateTime().compareTo(e2.getStartDateTime()));

    return eventsInRange;
  }

  /**
   * Checks whether there is an event at the specified date and time.
   *
   * @param dateTime the date and time for showing status.
   *
   * @return true, if event exists.
   */
  @Override
  public boolean isBusy(LocalDateTime dateTime) {
    if (dateTime == null) {
      throw new IllegalArgumentException("DateTime cannot be null");
    }

    for (Event event : events) {
      if (!dateTime.isBefore(event.getStartDateTime())
          &&
          dateTime.isBefore(event.getEndDateTime())) {
        return true;
      }
    }

    return false;
  }

  /**
   * Exports the calendar in CSV format.
   *
   * @param filename the name of the file.
   */
  @Override
  public void exportCalendar(String filename) {
    try (PrintWriter writer = new PrintWriter(new File(filename))) {
      writer.println("Subject,Start Date,Start Time,End Date,"
          + "End Time,All Day Event,Description,Location,Private");

      for (Event event : events) {
        writer.println(formatEventAsCsv(event));
      }

      System.out.println("Exported to: "
          +
          new File(filename).getAbsolutePath());
    } catch (FileNotFoundException e) {
      throw new RuntimeException("Could not write to file: " + filename);
    }
  }

  /**
   * Formats the event as CSV.
   *
   * @param event the event to be formatted.
   *
   * @return formatted event as CSV.
   */
  private String formatEventAsCsv(Event event) {
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

    boolean allDay = (event.getEndDateTime() == null);

    String startDate = event.getStartDateTime().toLocalDate().format(dateFormatter);
    String endDate;
    String startTime;
    String endTime;

    if (allDay) {
      startTime = "08:00 AM";
      endTime = "05:00 PM";
      endDate = startDate;
    } else {
      startTime = event.getStartDateTime().toLocalTime().format(timeFormatter);
      endTime = event.getEndDateTime().toLocalTime().format(timeFormatter);
      endDate = event.getEndDateTime().toLocalDate().format(dateFormatter);
    }

    String description = (event.getDescription() != null) ? event.getDescription() : "";
    String location = (event.getLocation() != null) ? event.getLocation() : "";
    String privacy = event.getStatus().equalsIgnoreCase("private") ? "True" : "False";

    return String.format("\"%s\",%s,%s,%s,%s,%s,\"%s\",\"%s\",%s",
        event.getSubject(),
        startDate,
        startTime,
        endDate,
        endTime,
        allDay ? "True" : "False",
        description,
        location,
        privacy
    );
  }
}