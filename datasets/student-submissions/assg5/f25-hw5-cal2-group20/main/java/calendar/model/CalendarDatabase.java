package calendar.model;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents an implementation of Calendar Database Model interface with required features to
 * support multiple calendars.
 */
public class CalendarDatabase implements CalendarDatabaseModel {
  private final Map<String, CalendarModel> models;
  private final Map<String, ZoneId> timezones;
  private String currCal; // Current calendar selected

  /**
   * Constructs a new Calendar Database with initially empty maps for storing calendar models
   * with their associated unique names their corresponding timezones.
   */
  public CalendarDatabase() {
    this.models = new HashMap<>();
    this.timezones = new HashMap<>();;
    this.currCal = "";
  }

  @Override
  public CalendarModel getCurrCalendarModel() {
    return models.get(currCal);
  }

  @Override
  public void createCalendar(String name, String timezoneName) {
    if (models.containsKey(name)) {
      throw new IllegalArgumentException("Calendar name: " + name + " already exists");
    }
    ZoneId timezone = this.convertTimezone(timezoneName);
    CalendarModel newModel = new Calendar();
    models.put(name, newModel);
    timezones.put(name, timezone);
  }

  private ZoneId convertTimezone(String timezone) {
    try {
      return ZoneId.of(timezone);
    } catch (DateTimeException e) {
      throw new IllegalArgumentException("Unknown timezone: " + timezone);
    }
  }

  @Override
  public void editCalendar(String name, CalendarProperty property, String value) {
    if (!models.containsKey(name)) {
      throw new IllegalArgumentException("Calendar name: " + name + " does not exist");
    }
    String newName = name;
    ZoneId timezone = timezones.get(name);
    final CalendarModel model = models.get(name);
    models.remove(name);
    timezones.remove(name);
    if (property == CalendarProperty.NAME) {
      newName = value;
    } else {
      ZoneId originalZone = timezone;
      timezone = this.convertTimezone(value);
      model.timezoneChangeAllEvents(originalZone, timezone);
    }
    models.put(newName, model);
    timezones.put(newName, timezone);
    if (this.currCal.equals(name)) {
      this.currCal = newName;
    }
  }

  @Override
  public void useCalendar(String name) {
    if (!models.containsKey(name)) {
      throw new IllegalArgumentException("Calendar name: " + name + " does not exist");
    }

    this.currCal = name;
  }

  //Need to implement this for series and consider resetting series ID's in other copys to -1
  @Override
  public void copyEventsInterval(String startDate, String endDate, String targetCal,
                                 String toDate) {
    if (!models.containsKey(targetCal) || this.currCal.isEmpty()) {
      throw new IllegalArgumentException("Target or current calendar has not been created");
    }
    CalendarModel model = models.get(this.currCal);
    CalendarModel targetModel = models.get(targetCal);
    List<EventObject> events = model.getDayEventsInterval(parseLocalDate(startDate),
        parseLocalDate(endDate));
    if (events.isEmpty()) {
      throw new IllegalArgumentException("Events not found to copy");
    }
    Map<Integer, List<EventObject>> seriesMap = new HashMap<>();
    for (EventObject event : events) {
      Duration duration = getDuration(event.getStartDateTime(), event.getEndDateTime());
      long interval = ChronoUnit.DAYS.between(parseLocalDate(startDate),
          event.getStartDateTime().toLocalDate());
      LocalDate newIntervalStart = parseLocalDate(toDate).plusDays(interval);
      LocalDateTime newStart = changeTimeZone(event.getStartDateTime(), targetCal,
          newIntervalStart);
      LocalDateTime newEnd = newStart.plus(duration);
      EventObject newEvent = returnEventCopy(newStart, event, newEnd, event.getEventSeriesId());
      if (seriesMap.containsKey(event.getEventSeriesId())) {
        seriesMap.get(event.getEventSeriesId()).add(newEvent);
      } else {
        seriesMap.put(event.getEventSeriesId(), new ArrayList<>());
        seriesMap.get(event.getEventSeriesId()).add(newEvent);
      }
    }
    List<EventObject> newEvents = new ArrayList<>(correctSeriesEvents(seriesMap, targetModel));
    targetModel.addEvents(newEvents);
  }

  private List<EventObject> correctSeriesEvents(Map<Integer, List<EventObject>> events,
                                                CalendarModel model) {
    int id = model.getEventSeriesId();
    List<EventObject> newEvents = new ArrayList<>();
    for (Integer eventSeriesId : events.keySet()) {
      if (eventSeriesId != -1) {
        List<EventObject> eventList = events.get(eventSeriesId);
        if (eventList.size() == 1) {
          EventObject newEvent = eventList.get(0);
          newEvents.add(returnEventCopy(newEvent.getStartDateTime(), newEvent,
              newEvent.getEndDateTime(), -1));
        } else {
          id++;
          for (EventObject e : eventList) {
            newEvents.add(returnEventCopy(e.getStartDateTime(), e,
                e.getEndDateTime(), id));
          }
        }
      } else {
        newEvents.add(events.get(eventSeriesId).get(0));
      }
    }
    model.setEventSeriesId(id);
    return newEvents;
  }

  @Override
  public void copyEvents(String startDate, String targetCal, String toDate) {
    if (!models.containsKey(targetCal) || this.currCal.isEmpty()) {
      throw new IllegalArgumentException("Target or current calendar has not been created");
    }
    CalendarModel model = models.get(this.currCal);
    CalendarModel targetModel = models.get(targetCal);
    List<EventObject> events = model.getDayEvents(parseLocalDate(startDate));
    if (events.isEmpty()) {
      throw new IllegalArgumentException("Event not found to copy");
    }
    List<EventObject> newEvents = new ArrayList<>();
    for (EventObject event : events) {
      Duration duration = getDuration(event.getStartDateTime(), event.getEndDateTime());
      LocalDateTime newStart = changeTimeZone(event.getStartDateTime(), targetCal,
          parseLocalDate(toDate));
      LocalDateTime newEnd = newStart.plus(duration);
      EventObject newEvent = returnEventCopy(newStart, event, newEnd, -1);
      newEvents.add(newEvent);
    }
    targetModel.addEvents(newEvents);
  }

  @Override
  public void copyEvent(String subject, String targetCal, String startDateTime, String toDateTime) {
    if (!models.containsKey(targetCal) || this.currCal.isEmpty()) {
      throw new IllegalArgumentException("Target or current calendar has not been created");
    }
    CalendarModel model = models.get(this.currCal);
    CalendarModel targetModel = models.get(targetCal);
    List<EventObject> events = model.getEvent(subject, startDateTime);
    LocalDateTime newStart = returnLocalDateTime(toDateTime);
    if (events.isEmpty()) {
      throw new IllegalArgumentException("Event not found to copy");
    }

    List<EventObject> newEvents = new ArrayList<>();
    for (EventObject event : events) {
      Duration duration = getDuration(event.getStartDateTime(), event.getEndDateTime());
      LocalDateTime newEnd = newStart.plus(duration);
      EventObject newEvent = returnEventCopy(newStart, event, newEnd, -1);
      newEvents.add(newEvent);
    }
    targetModel.addEvents(newEvents);

  }

  private Duration getDuration(LocalDateTime startDateTime, LocalDateTime endDateTime) {
    return Duration.between(startDateTime, endDateTime);
  }

  private LocalDateTime changeTimeZone(LocalDateTime start, String targetCal, LocalDate toDate) {
    ZoneId targetZone = this.timezones.get(targetCal);
    ZoneId origZone = ZoneId.of("America/New_York");
    ZonedDateTime origTime = start.atZone(origZone);
    ZonedDateTime newZone = origTime.withZoneSameInstant(targetZone);
    return toDate.atTime(newZone.toLocalTime());
  }

  @Override
  public String getCurrCalendarName() {
    return currCal;
  }

  @Override
  public ZoneId getCurrTimezone() {
    return timezones.get(this.currCal);
  }

  private EventObject returnEventCopy(LocalDateTime toDateTime, EventObject event,
                                      LocalDateTime end, int seriesId) {
    Event.EventBuilder builder = Event.getBuilder()
        .copyEventFrom(event)
        .subject(event.getSubject())
        .startDateTime(toDateTime)
        .endDateTime(end)
        .eventSeriesId(seriesId);

    return builder.build();
  }

  private LocalDateTime returnLocalDateTime(String val) {
    try {
      return LocalDateTime.parse(val);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid date/time format: " + val);
    }
  }

  private LocalDate parseLocalDate(String val) {
    try {
      return LocalDate.parse(val);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid date format: " + val);
    }
  }
}
