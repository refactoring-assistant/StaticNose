package calendar.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Calendar Model class.
 */
public class CalendarModel implements IcalendarModel {

  private final CalendarManager manager;

  /**
   * Calendar model Constructor.
   */
  public CalendarModel() {

    this.manager = new CalendarManager();
  }

  @Override
  public List<Event> getAllEvents() {

    return new ArrayList<>(manager.getActiveEvents());
  }
  /** Parses date time string into LocalDateTime object. */

  public LocalDateTime parseDateTime(String dateTimeStr) throws Exception {
    try {
      return LocalDateTime.parse(dateTimeStr);
    } catch (Exception e) {
      throw new Exception("Invalid date format" + dateTimeStr);
    }
  }

  private LocalDate parseDate(String dateStr) throws Exception {
    try {
      return LocalDate.parse(dateStr);
    } catch (Exception e) {
      throw new Exception("Invalid date format" + dateStr);
    }
  }

  private Set<DayOfWeek> parseRepeatDays(String repeatDays) {
    Set<DayOfWeek> days = new HashSet<>();
    for (char c : repeatDays.toCharArray()) {
      switch (c) {
        case 'M':
        case 'm':
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
      }
    }
    return days;
  }

  private boolean isDuplicate(Event event) {
    for (Event e : manager.getActiveEvents()) {
      if (e.equals(event)) {
        return true;
      }
    }
    return false;
  }

  private Event findEvent(String subject, LocalDateTime start) {
    for (Event e : manager.getActiveEvents()) {
      if (e.getEventName().equals(subject) && e.getStartTime().equals(start)) {
        return e;
      }
    }
    return null;
  }

  @Override
  public void createTimedEvent(String subject, String startDateTime, String endDateTime)
      throws Exception {

    LocalDateTime startTime = parseDateTime(startDateTime);
    LocalDateTime endTime = parseDateTime(endDateTime);

    Event event = new TimedEvent(subject, null, null, null, null,
        startTime, endTime);
    if (isDuplicate(event)) {
      throw new Exception("Event already exists!");
    }
    manager.getActiveEvents().add(event);
  }

  @Override
  public void createAllDayEvent(String subject, String dateStr) throws Exception {
    LocalDate date = parseDate(dateStr);
    LocalDateTime startTime = LocalDateTime.of(date, LocalTime.of(8, 0));
    LocalDateTime endTime = LocalDateTime.of(date, LocalTime.of(17, 0));
    Event event = new AllDayEvent(subject, null, null, null, null,
        date);
    if (isDuplicate(event)) {
      throw new Exception("Event already exists!");

    }
    manager.getActiveEvents().add(event);
  }

  @Override
  public void createRecurringEvent(String subject, String startDateTime, String endDateTime,
                                   String repeatDays, int occurrences) throws Exception {
    LocalDateTime startTime = parseDateTime(startDateTime);
    LocalDateTime endTime = parseDateTime(endDateTime);

    Set<DayOfWeek> days = parseRepeatDays(repeatDays);

    String seriesId = UUID.randomUUID().toString();

    LocalDate currentDate = startTime.toLocalDate();
    int count = 0;

    while (count < occurrences) {
      if (days.contains(currentDate.getDayOfWeek())) {

        LocalDateTime eventStart = LocalDateTime.of(currentDate, startTime.toLocalTime());
        LocalDateTime eventEnd = LocalDateTime.of(currentDate, endTime.toLocalTime());

        Event event = new TimedEvent(subject, null, null, null, seriesId,
            eventStart, eventEnd);

        if (isDuplicate(event)) {
          throw new Exception("Event already exists!");
        }
        manager.getActiveEvents().add(event);
        count++;

      }
      currentDate = currentDate.plusDays(1);
    }
  }

  @Override
  public List<Event> getEventsOnDate(String dateStr) throws Exception {
    LocalDate date = parseDate(dateStr);
    List<Event> result = new ArrayList<>();
    for (Event e : manager.getActiveEvents()) {
      if (e.occursOn(date)) {
        result.add(e);
      }
    }
    return result;


  }

  @Override
  public boolean isBusyAt(String dateTimeStr) throws Exception {
    LocalDateTime time = parseDateTime(dateTimeStr);

    for (Event e : manager.getActiveEvents()) {
      LocalDateTime start = e.getStartTime();
      LocalDateTime end = e.getActualEndTime();
      if (!start.isAfter(time) && end.isAfter(time)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void editEvent(String subject, String startDateTime, String property, String newValue)
      throws Exception {
    LocalDateTime startTime = parseDateTime(startDateTime);

    Event event = findEvent(subject, startTime);
    if (event == null) {
      throw new Exception("Event not found!");
    }
    if (event.getSeriesId() != null) {
      event.setSeriesId(UUID.randomUUID().toString());
    }
    switch (property.toLowerCase()) {
      case "subject":
        event.setEventName(newValue);
        break;
      case "start":
        if (event instanceof TimedEvent) {
          ((TimedEvent) event).setStartTime(parseDateTime(newValue));
        } else {
          throw new Exception("Cannot modify start time of all day event");
        }
        break;
      case "end":
        if (event instanceof TimedEvent) {
          ((TimedEvent) event).setEndTime(parseDateTime(newValue));
        } else {
          throw new Exception("Cannot modify end time of all day event");
        }
        break;
      case "notes":
        event.setNotes(newValue);
        break;
      case "location":
        event.setLocation(newValue);
        break;
      case "status":
        event.setStatus(newValue);
        break;
      default:
        throw new Exception("Invalid event property!" + property);
    }

  }

  @Override
  public void createRecurringEventUntil(String subject, String startDateTime, String endDateTime,
                                        String repeatDays, String endDate) throws Exception {
    LocalDateTime startTime = parseDateTime(startDateTime);
    LocalDateTime endTime = parseDateTime(endDateTime);
    Set<DayOfWeek> days = parseRepeatDays(repeatDays);
    LocalDate untilDate = parseDate(endDate);
    String seriesId = UUID.randomUUID().toString();
    LocalDate currentDate = startTime.toLocalDate();


    while (!currentDate.isAfter(untilDate)) {
      if (days.contains(currentDate.getDayOfWeek())) {
        LocalDateTime eventStart = LocalDateTime.of(currentDate, startTime.toLocalTime());
        LocalDateTime eventEnd = LocalDateTime.of(currentDate, endTime.toLocalTime());

        Event event = new TimedEvent(subject, null, null, null, seriesId,
            eventStart, eventEnd);

        if (isDuplicate(event)) {
          throw new Exception("Event already exists!");
        }
        manager.getActiveEvents().add(event);


      }
      currentDate = currentDate.plusDays(1);
    }
  }

  @Override
  public void createAllDayRecurringEvent(String subject, String startDateStr, String repeatDays,
                                         int occurrences) throws Exception {
    LocalDate startDate = parseDate(startDateStr);

    Set<DayOfWeek> days = parseRepeatDays(repeatDays);

    String seriesId = UUID.randomUUID().toString();

    LocalDate currentDate = startDate;
    int count = 0;
    while (count < occurrences) {
      if (days.contains(currentDate.getDayOfWeek())) {

        LocalDateTime startTime = LocalDateTime.of(currentDate, LocalTime.of(8, 0));
        LocalDateTime endTime = LocalDateTime.of(currentDate, LocalTime.of(17, 0));

        Event event = new AllDayEvent(subject, null, null, null, seriesId,
            currentDate);
        if (isDuplicate(event)) {
          throw new Exception("Event already exists!");
        }
        manager.getActiveEvents().add(event);
        count++;
      }
      currentDate = currentDate.plusDays(1);

    }

  }

  @Override
  public void createAllDayRecurringEventUntil(String subject, String startDateStr,
                                              String repeatDays, String endDate) throws Exception {
    LocalDate startDate = parseDate(startDateStr);

    Set<DayOfWeek> days = parseRepeatDays(repeatDays);

    String seriesId = UUID.randomUUID().toString();

    LocalDate untilDate = parseDate(endDate);
    LocalDate currentDate = startDate;

    while (!currentDate.isAfter(untilDate)) {

      if (days.contains(currentDate.getDayOfWeek())) {

        LocalDateTime startTime = LocalDateTime.of(currentDate, LocalTime.of(8, 0));
        LocalDateTime endTime = LocalDateTime.of(currentDate, LocalTime.of(17, 0));

        Event event = new AllDayEvent(subject, null, null, null, seriesId,
            currentDate);
        if (isDuplicate(event)) {
          throw new Exception("Event already exists!");
        }
        manager.getActiveEvents().add(event);

      }
      currentDate = currentDate.plusDays(1);

    }

  }

  private void updateProperty(Event event, String property, String newValue) throws Exception {
    switch (property.toLowerCase()) {
      case "subject":
        event.setEventName(newValue);
        break;
      case "start":
        if (event instanceof TimedEvent) {
          ((TimedEvent) event).setStartTime(parseDateTime(newValue));
        } else {
          throw new Exception("Cannot modify start time of all day event");
        }
        break;
      case "end":
        if (event instanceof TimedEvent) {
          ((TimedEvent) event).setEndTime(parseDateTime(newValue));
        } else {
          throw new Exception("Cannot modify end time of all day event");
        }
        break;
      case "notes":
        event.setNotes(newValue);
        break;
      case "location":
        event.setLocation(newValue);
        break;
      case "status":
        event.setStatus(newValue);
        break;
      default:
        throw new Exception("Invalid event property!" + property);

    }
  }

  @Override
  public void editEventsFrom(String subject, String startDateTime, String property, String newValue)
      throws Exception {

    LocalDateTime startTime = parseDateTime(startDateTime);

    Event target = findEvent(subject, startTime);
    if (target == null) {
      throw new Exception("Event not found!");
    }

    String oldSeriesId = target.getSeriesId();

    if (oldSeriesId == null) {
      updateProperty(target, property, newValue);
      return;
    }

    List<Event> toUpdate = new ArrayList<>();
    for (Event e : manager.getActiveEvents()) {
      if (e.getSeriesId() != null && e.getSeriesId()
          .equals(oldSeriesId) && !e.getStartTime()
          .isBefore(startTime)) {
        toUpdate.add(e);
      }
    }

    if (property.toLowerCase().equals("start")) {
      String newSeriesId = UUID.randomUUID().toString();
      for (Event e : toUpdate) {
        e.setSeriesId(newSeriesId);
      }
    }
    for (Event e : toUpdate) {
      updateProperty(e, property, newValue);
    }
  }

  @Override
  public void editSeries(String subject, String startDateTime, String property, String newValue)
      throws Exception {
    LocalDateTime startTime = parseDateTime(startDateTime);
    Event target = findEvent(subject, startTime);
    if (target == null) {
      throw new Exception("Event not found!");
    }

    String oldSeriesId = target.getSeriesId();
    if (oldSeriesId == null) {
      updateProperty(target, property, newValue);
      return;
    }
    List<Event> toUpdate = new ArrayList<>();
    for (Event e : manager.getActiveEvents()) {
      if (e.getSeriesId() != null && e.getSeriesId()
          .equals(oldSeriesId)
          &&
          e.getStartTime().toLocalTime().equals(target.getStartTime().toLocalTime())) {
        toUpdate.add(e);
      }
    }

    for (Event e : toUpdate) {
      updateProperty(e, property, newValue);
    }
  }

  @Override
  public List<Event> getEventsInInterval(String startDateTime, String endDateTime)
      throws Exception {
    LocalDateTime intervalStart = parseDateTime(startDateTime);
    LocalDateTime intervalEnd = parseDateTime(endDateTime);

    List<Event> result = new ArrayList<>();

    for (Event e : manager.getActiveEvents()) {
      LocalDateTime start = e.getStartTime();
      LocalDateTime end = e.getActualEndTime();
      if (start.isBefore(intervalEnd) && end.isAfter(intervalStart)) {
        result.add(e);
      }
    }
    return result;
  }

  @Override
  public String exportToCsv() throws Exception {
    StringBuilder sb = new StringBuilder();
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a");

    sb.append(
        "Subject,Start Date,Start Time,End Date,End Time,"
            +
            "All Day Event,Description,Location,Private\n");

    for (Event e : manager.getActiveEvents()) {
      String line = formatEventForCsv(e, dateFormatter, timeFormatter);
      sb.append(line);
    }
    return sb.toString();
  }

  private String formatEventForCsv(Event e, DateTimeFormatter dateFormatter,
                                   DateTimeFormatter timeFormatter) {

    String subject = e.getEventName();
    String description = (e.getNotes() == null ? "" : e.getNotes());
    String location = (e.getLocation() == null ? "" : e.getLocation());
    String privateStatus =
        (e.getStatus() != null && e.getStatus().equals("private")) ? "True" : "False";

    if (e.isAllDay()) {

      String startDate = e.getStartTime().format(dateFormatter);
      return subject + "," + startDate + ",,,,True,"
          + description
          + "," + location + ","
          + privateStatus + "\n";
    } else {
      String startDate = e.getStartTime().format(dateFormatter);
      String startTime = e.getStartTime().format(timeFormatter);
      String endDate = e.getActualEndTime().format(dateFormatter);
      String endTime = e.getActualEndTime().format(timeFormatter);
      return subject + "," + startDate + "," + startTime + ","
          + endDate
          + "," + endTime + ",False," + description + ","
          + location + "," + privateStatus + "\n";
    }

  }

  /** creates a new calendar with name and timezone. */

  public void createCalendar(String name, String zoneId) throws Exception {
    manager.createCalendar(name, ZoneId.of(zoneId));
  }

  /** Deletes the specified calendar. */

  public void deleteCalendar(String name) throws Exception {
    manager.deleteCalendar(name);
  }

  /** Renames an existing calendar. */

  public void renameCalendar(String name, String newName) throws Exception {
    manager.renameCalendar(name, newName);
  }

  /** List all calendar names. */

  public List<String> getAllCalendarNames() {
    return manager.getAllCalendarNames();
  }

  /** returns timezone of specified calendar. */

  public String getCalendarTimeZone(String name) throws Exception {
    return manager.getCalendarZoneId(name).getId();
  }

  /** Updates timezone of existing calendar. */

  public void updateCalendarTimeZone(String name, String newzoneId) throws Exception {
    manager.updateCalendarTimezone(name, ZoneId.of(newzoneId));
  }

  /** Sets specified calendar as active. */

  public void setActiveCalendar(String name) throws Exception {
    manager.setActiveCalendar(name);
  }

  /** Converts datetime from one timezone to another. */

  public LocalDateTime convertTimezone(LocalDateTime time, String sourceTimezone,
                                       String targetTimezone) {
    ZonedDateTime zoned = ZonedDateTime.of(time, ZoneId.of(sourceTimezone));
    ZonedDateTime converted = zoned.withZoneSameInstant(ZoneId.of(targetTimezone));
    return converted.toLocalDateTime();

  }

  /** Returns name of current active calendar. */

  public String getActiveCalendarName() throws Exception {
    return manager.getActiveCalendarName();
  }

  @Override
  public void copyEvent(String subject, String sourceStartDateTime,
                        String targetCalName, String targetStartDateTime) throws Exception {

    LocalDateTime sourceStart = parseDateTime(sourceStartDateTime);
    LocalDateTime targetStart = parseDateTime(targetStartDateTime);

    manager.copyEvent(subject, sourceStart, targetCalName, targetStart);
  }

  @Override
  public void copyEventsOnDate(String sourceDateStr, String targetCalName,
                               String targetDateStr) throws Exception {

    LocalDate sourceDate = parseDate(sourceDateStr);
    LocalDate targetDate = parseDate(targetDateStr);

    manager.copyEventsOnDate(sourceDate, targetCalName, targetDate);
  }

  @Override
  public void copyEventsInInterval(String sourceStartStr, String sourceEndStr,
                                   String targetCalName, String targetDateStr) throws Exception {

    LocalDate sourceStartDate = parseDate(sourceStartStr);
    LocalDate sourceEndDate = parseDate(sourceEndStr);
    LocalDate targetStartDate = parseDate(targetDateStr);
    manager.copyEventsBetween(sourceStartDate, sourceEndDate, targetCalName,
        targetStartDate);
  }

  @Override
  public String exportToIcal() throws Exception {

    String activeCalName = manager.getActiveCalendarName();
    List<Event> events = manager.getAllActiveEvents();
    String zoneIdStr = manager.getCalendarTimeZone(activeCalName);

    ZoneId zoneId = ZoneId.of(zoneIdStr);

    return IcsExporter.exportToIcal(events, activeCalName, zoneId);
  }

  @Override
  public void deleteEvent(String subject, String startDateTime) throws Exception {
    LocalDateTime startTime = parseDateTime(startDateTime);

    manager.deleteEvent(subject, startTime);
  }

  @Override
  public void deleteEventsFrom(String subject, String startDateTime) throws Exception {
    LocalDateTime startTime = parseDateTime(startDateTime);

    manager.deleteEventsFrom(subject, startTime);
  }

  @Override
  public void deleteSeries(String subject) throws Exception {
    manager.deleteSeries(subject);
  }

}
