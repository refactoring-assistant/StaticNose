package calendar.model;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * CalendarManger manages multiple calenders.
 */
public class CalendarManager {

  /**
   * Represents a inner class for single calendar.
   */
  private class CalendarData {
    private List<Event> events;
    private ZoneId zoneId;


    CalendarData(ZoneId zoneId) {
      this.zoneId = zoneId;
      this.events = new ArrayList<>();
    }

    public List<Event> getEvents() {
      return events;
    }

    public ZoneId getZoneId() {
      return zoneId;
    }

    public void setZoneId(ZoneId zoneId) {
      this.zoneId = zoneId;
    }

  }

  private Map<String, CalendarData> calenders;
  private String activeCalendar;

  /**
   * Constructs a default calendar.
   */
  public CalendarManager() {
    this.calenders = new HashMap<>();
    this.calenders.put("Default", new CalendarData(ZoneId.of("America/New_York")));
    this.activeCalendar = "Default";
  }

  /**
   * Returns all events of the currently active calendar.
   *
   * @return a list of events of the active calendar
   */
  public List<Event> getActiveEvents() {
    return calenders.get(activeCalendar).getEvents();
  }

  /**
   * Returns the timezone of the currently active calendar.
   *
   * @return the zoneId
   */
  public ZoneId getActiveZoneId() {
    return this.calenders.get(activeCalendar).getZoneId();
  }

  /**
   * Creates a neW calendar with specified name and timezone.
   *
   * @param name the name of the new calendar
   * @param zoneId the zoneId of the new calendar
   * @throws Exception if the name already exist
   */
  public void createCalendar(String name, ZoneId zoneId) throws Exception {
    if (calenders.containsKey(name)) {
      throw new Exception("Calendar already exists");
    }
    CalendarData calendarData = new CalendarData(zoneId);
    calenders.put(name, calendarData);
  }

  /**
   * Switch the active calendar.
   *
   * @param name the name of the calendar to be set as active
   * @throws Exception if the calendar doesnt exists
   */
  public void setActiveCalendar(String name) throws Exception {
    if (!calenders.containsKey(name)) {
      throw new Exception("Calendar not found" + name);
    }
    this.activeCalendar = name;
  }

  /**
   * Returns a list of all existing calendars.
   *
   * @return a list of calendar names
   */
  public List<String> getAllCalendarNames() {
    List<String> calendarNames = new ArrayList<>(calenders.keySet());
    return calendarNames;
  }

  /**
   * Returns the timezone of a specific calendar.
   *
   * @param calendarName name of the calendar
   * @return the zoneid associated with calendar
   * @throws Exception if the specified calendar doesnt exist
   */
  public ZoneId getCalendarZoneId(String calendarName) throws Exception {
    if (!calenders.containsKey(calendarName)) {
      throw new Exception("Calendar not found" + calendarName);
    }
    return calenders.get(calendarName).getZoneId();
  }

  /**
   * Renames an existing calendar.
   *
   * @param oldName teh current name of calendar
   * @param newName the new name of the calendar
   * @throws Exception if the new name already exists or old name doesnt exists
   */
  public void renameCalendar(String oldName, String newName) throws Exception {
    if (!calenders.containsKey(oldName)) {
      throw new Exception("Calendar not found" + oldName);
    }
    if (calenders.containsKey(newName)) {
      throw new Exception("Calendar already exists" + newName);
    }
    CalendarData calendarData = calenders.get(oldName);
    calenders.remove(oldName);
    calenders.put(newName, calendarData);
    if (activeCalendar.equals(oldName)) {
      activeCalendar = newName;
    }
  }

  /**
   * Delets the specified calendar.
   *
   * @param name the name of the calendar
   * @throws Exception if the calendar doesnt exist or cannot be deleted
   */
  public void deleteCalendar(String name) throws Exception {
    if (name.equals("Default")) {
      throw new Exception("Cannot delete default calendar");
    }
    if (!calenders.containsKey(name)) {
      throw new Exception("Calendar not found" + name);
    }
    calenders.remove(name);
    if (activeCalendar.equals(name)) {
      activeCalendar = "Default";
    }
  }

  /**
   * Updates an existing calendar.
   *
   * @param name the name of the calendar to update
   * @param newzoneId the new timezone to assign
   * @throws Exception if the calendar doesnt exist
   */
  public void updateCalendarTimezone(String name, ZoneId newzoneId) throws Exception {
    if (!calenders.containsKey(name)) {
      throw new Exception("Calendar not found" + name);
    }
    CalendarData calendarData = calenders.get(name);
    calendarData.setZoneId(newzoneId);
  }

  /**
   * Helper to clone and add an event instance, handling the timezone conversion.
   */
  private Event copySingleEventInstance(Event event, CalendarData sourceCalData,
                                        CalendarData targetCalData, LocalDateTime targetDateTime) {

    LocalDateTime newStartLocal = targetDateTime;
    LocalDateTime newEndLocal;

    if (event.isAllDay()) {
      LocalDate targetDate = targetDateTime.toLocalDate();
      newEndLocal = targetDate.atTime(17, 0);
    } else {
      long durationSeconds = ChronoUnit.SECONDS.between(event.getStartTime(), event.getEndTime());
      newEndLocal = newStartLocal.plusSeconds(durationSeconds);
    }

    Event newEvent = event.copy(newStartLocal, newEndLocal, UUID.randomUUID().toString());
    targetCalData.getEvents().add(newEvent);
    return newEvent;
  }

  /**
   * Copies a single event instance using pre-parsed time objects.
   * (Called by CalendarModel's copyEvent)
   */
  public void copyEvent(String eventName, LocalDateTime sourceStart,
                        String targetCalName, LocalDateTime targetStart) throws Exception {

    CalendarData sourceCal = getActiveCalendarData();
    CalendarData targetCal = getCalendarData(targetCalName);

    Event eventToCopy = sourceCal.getEvents().stream()
        .filter(e -> e.getEventName().equalsIgnoreCase(eventName)
            && e.getStartTime().equals(sourceStart))
        .findFirst()
        .orElseThrow(() -> new Exception("Event instance not found in active calendar."));

    copySingleEventInstance(eventToCopy, sourceCal, targetCal, targetStart);
  }

  /**
   * Copies all events from a specific date range, using pre-parsed date objects.
   * (Called by CalendarModel's copyEventsInInterval)
   */
  public void copyEventsBetween(LocalDate startDate, LocalDate endDate,
                                String targetCalName, LocalDate targetStartDate)
      throws Exception {

    CalendarData sourceCal = getActiveCalendarData();
    CalendarData targetCal = getCalendarData(targetCalName);

    long dateShiftDays = ChronoUnit.DAYS.between(startDate, targetStartDate);
    sourceCal.getEvents().stream()
        .filter(e -> e.occursInInterval(startDate.atStartOfDay(),
            endDate.plusDays(1).atStartOfDay()))
        .forEach(e -> {
          try {

            LocalDate newDate = e.getStartTime().toLocalDate().plusDays(dateShiftDays);

            LocalDateTime targetStartDt = newDate.atTime(e.getStartTime().toLocalTime());

            copySingleEventInstance(e, sourceCal, targetCal, targetStartDt);
          } catch (Exception ex) {
            System.err.println("Could not copy event " + e.getEventName() + ": " + ex.getMessage());
          }
        });
  }

  /**
   * Copies all events on a single day, using pre-parsed date objects.
   * (Called by CalendarModel's copyEventsOnDate)
   */
  public void copyEventsOnDate(LocalDate date, String targetCalName,
                               LocalDate targetDate) throws Exception {

    copyEventsBetween(date, date, targetCalName, targetDate);
  }

  /**
   * Returns the CalendarData object of the currently active calendar.
   * (Used to access the active calendar's list of events and its ZoneId.)
   */
  private CalendarData getActiveCalendarData() throws Exception {
    if (activeCalendar == null || !calenders.containsKey(activeCalendar)) {
      throw new Exception("No active calendar set or calendar not found.");
    }
    return calenders.get(activeCalendar);
  }

  /**
   * Returns a specific CalendarData object by name.
   * (Used to find the target calendar during copy operations.)
   */
  private CalendarData getCalendarData(String name) throws Exception {
    if (!calenders.containsKey(name)) {
      throw new Exception("Calendar not found: " + name);
    }
    return calenders.get(name);
  }

  /**
   * Returns the name of the currently active calendar.
   */
  public String getActiveCalendarName() {
    return activeCalendar;
  }

  /**
   * Returns all events from the currently active calendar.
   */
  public List<Event> getAllActiveEvents() throws Exception {
    CalendarData activeData = getActiveCalendarData();
    return activeData.getEvents();
  }

  /**
   * Returns the timezone ID string for a specified calendar.
   */
  public String getCalendarTimeZone(String calName) throws Exception {
    CalendarData calData = getCalendarData(calName);
    return calData.getZoneId().getId();
  }

  /**
   * Deletes a single instance of a recurring event based on name and start time.
   *
   * @param eventName The name/subject of the event.
   * @param sourceStart The start time of the specific instance to delete.
   * @throws Exception if the event instance is not found.
   */
  public void deleteEvent(String eventName, LocalDateTime sourceStart) throws Exception {
    CalendarData activeCal = getActiveCalendarData();
    List<Event> events = activeCal.getEvents();

    boolean removed = events.removeIf(e ->
        e.getEventName().equalsIgnoreCase(eventName)
            && e.getStartTime().equals(sourceStart)
    );

    if (!removed) {
      throw new Exception("Event instance '" + eventName + "' at " + sourceStart
          + " not found in active calendar.");
    }
  }

  /**
   * Deletes all events in a series starting from the specified date/time (inclusive).
   *
   * @param eventName The name/subject of the recurring event series.
   * @param sourceStart The date/time from which to start deleting occurrences.
   * @throws Exception if no series events are found.
   */
  public void deleteEventsFrom(String eventName, LocalDateTime sourceStart)
      throws Exception {
    CalendarData activeCal = getActiveCalendarData();
    List<Event> events = activeCal.getEvents();
    int initialSize = events.size();

    events.removeIf(e ->
        e.getEventName().equalsIgnoreCase(eventName)
            && !e.getStartTime().isBefore(sourceStart)
    );

    if (events.size() == initialSize) {
      throw new Exception("No event occurrences found for series '" + eventName
          + "' starting from or after " + sourceStart + ".");
    }
  }

  /**
   * Deletes the entire series/all events with the specified name.
   *
   * @param eventName The name/subject of the series or event.
   * @throws Exception if no events with that name are found.
   */
  public void deleteSeries(String eventName) throws Exception {
    CalendarData activeCal = getActiveCalendarData();
    List<Event> events = activeCal.getEvents();
    int initialSize = events.size();

    events.removeIf(e -> e.getEventName().equalsIgnoreCase(eventName));

    if (events.size() == initialSize) {
      throw new Exception("No events or series found with the name '" + eventName
          + "' in active calendar.");
    }
  }

}

