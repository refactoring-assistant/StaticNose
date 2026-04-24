package calendar.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Represents a Calendar model class that implements Calendar Model interface methods. A calendar
 * contains maps for event series that contain ids of events that are in series and for all events,
 * current series id, and current temp events.
 */
public class Calendar implements CalendarModel {
  private final Map<Integer, List<Integer>> eventSeries;
  private final Map<Integer, EventObject> events;
  private final Map<Integer, EventObject> tempEvents;
  private int eventSeriesId;

  /**
   * Constructs a new Calendar with empty maps of events and event series. Sets current event series
   * id to -1.
   */
  public Calendar() {
    eventSeries = new HashMap<>();
    events = new HashMap<>();
    tempEvents = new HashMap<>();
    eventSeriesId = -1;
  }

  @Override
  public int getEventSeriesId() {
    return this.eventSeriesId;
  }

  @Override
  public void setEventSeriesId(int id) {
    this.eventSeriesId = id;
  }

  @Override
  public void timezoneChangeAllEvents(ZoneId timezone, ZoneId newZone) {
    for (Integer key : this.events.keySet()) {
      EventObject e = this.events.get(key);
      LocalDateTime start = newTimezone(e.getStartDateTime(), timezone, newZone);
      LocalDateTime end = newTimezone(e.getEndDateTime(), timezone, newZone);
      EventObject newEvent = changeTimes(e, start, end);
      this.tempEvents.put(newEvent.hashCode(), newEvent);
    }

    for (Integer key : this.tempEvents.keySet()) {
      EventObject e = this.tempEvents.get(key);
      if (e.getEventSeriesId() != -1) {
        if (!e.getStartDateTime().toLocalDate().equals(e.getEndDateTime().toLocalDate())) {
          throw new IllegalArgumentException("Changing timezones led to events in a series "
                + "extending beyond a single day, command terminated");
        }
      }
    }
    this.events.clear();
    this.events.putAll(this.tempEvents);
    this.tempEvents.clear();
  }

  private LocalDateTime newTimezone(LocalDateTime time, ZoneId timezone, ZoneId newZone) {
    ZonedDateTime origTime = time.atZone(timezone);
    ZonedDateTime newTime = origTime.withZoneSameInstant(newZone);
    return newTime.toLocalDateTime();
  }

  private EventObject changeTimes(EventObject event, LocalDateTime start, LocalDateTime end) {
    Event.EventBuilder builder = Event.getBuilder()
        .copyEventFrom(event)
        .subject(event.getSubject())
        .startDateTime(start)
        .endDateTime(end);

    return builder.build();
  }

  @Override
  public List<EventObject> getEvent(String subject, String startDateTime) {
    LocalDateTime startDate = returnLocalDateTime(startDateTime);
    return this.filter(new EventSubjectFilter(subject)
        .and(new EventStartDateTimeFilter(startDate)));
  }

  @Override
  public List<EventObject> getDayEvents(LocalDate startDate) {
    return this.filter(new EventStartDateFilter(startDate));
  }

  @Override
  public List<EventObject> getDayEventsInterval(LocalDate startDate, LocalDate endDate) {
    List<EventObject> events = new ArrayList<>();
    if (endDate.isBefore(startDate)) {
      throw new IllegalArgumentException("Invalid interval to copy from");
    }
    while (startDate.isBefore(endDate) || startDate.equals(endDate)) {
      events.addAll(this.filter(new EventStartDateFilter(startDate)));
      startDate = startDate.plusDays(1);
    }
    return events;
  }

  @Override
  public void addEvents(List<EventObject> events) {
    for  (EventObject event : events) {
      int key = event.hashCode();
      if (this.events.containsKey(key)) {
        throw new IllegalArgumentException("Event already exists in target calendar");
      }
      this.tempEvents.put(key, event);
    }
    this.events.putAll(this.tempEvents);
    for (EventObject event : events) {
      if (event.getEventSeriesId() != -1) {
        eventSeries.computeIfAbsent(event.getEventSeriesId(),
            eventList -> new ArrayList<>()).add(event.hashCode());
      }
    }
    this.tempEvents.clear();

  }

  @Override
  public List<EventObject> filter(Predicate<EventObject> predicate) {
    List<EventObject> results = new ArrayList<>();
    for (EventObject event : this.getAllEvents()) {
      if (predicate.test(event)) {
        results.add(event);
      }
    }
    return results;
  }

  @Override
  public void createEvent(String subject, String startDateTime, String endDateTime)
      throws IllegalArgumentException {
    EventObject newEvent = Event.getBuilder()
        .subject(subject)
        .startDateTime(returnLocalDateTime(startDateTime))
        .endDateTime(returnLocalDateTime(endDateTime))
        .build();

    int key = newEvent.hashCode();
    if (this.events.containsKey(key)) {
      throw new IllegalArgumentException("Event already exists");
    }
    events.put(key, newEvent);
  }

  @Override
  public void createAllDayEvent(String subject, String startDate) {
    LocalDate eventDate = returnLocalDate(startDate);
    LocalDateTime startDateTime = eventDate.atTime(Event.DEFAULT_START_TIME);
    LocalDateTime endDateTime = eventDate.atTime(Event.DEFAULT_END_TIME);

    EventObject newEvent = Event.getBuilder()
        .subject(subject)
        .startDateTime(startDateTime)
        .endDateTime(endDateTime)
        .build();

    int key = newEvent.hashCode();
    if (this.events.containsKey(key)) {
      throw new IllegalArgumentException("Event already exists");
    }
    events.put(key, newEvent);
  }


  @Override
  public void createEventSeries(String subject, String startDateTime, String endDateTime,
                                String weekdays, int occurrences) throws IllegalArgumentException {
    LocalDateTime curr = returnLocalDateTime(startDateTime);
    LocalDateTime endDate = returnLocalDateTime(endDateTime);
    List<LocalDateTime> daysOfWeek = getDates(subject, curr, weekdays, null, endDate,
        occurrences);
    createSeries(subject, endDate, daysOfWeek);
  }

  @Override
  public void createEventSeriesUntil(String subject, String startDateTime,
                                     String endDateTime, String weekdays, String dateUntil)
      throws IllegalArgumentException {
    LocalDateTime curr = returnLocalDateTime(startDateTime);
    LocalDateTime endDate = returnLocalDateTime(endDateTime);
    List<LocalDateTime> daysOfWeek = getDates(subject, curr, weekdays, dateUntil, endDate, 0);
    createSeries(subject, endDate, daysOfWeek);
  }


  @Override
  public void createAllDayEventSeries(String subject, String startDate, String weekdays,
                                      int occurrences) {
    LocalDate eventDate = returnLocalDate(startDate);
    LocalDateTime curr = eventDate.atTime(Event.DEFAULT_START_TIME);
    LocalDateTime endDate = eventDate.atTime(Event.DEFAULT_END_TIME);
    List<LocalDateTime> daysOfWeek = getDates(subject, curr, weekdays, null, endDate,
        occurrences);
    createSeries(subject, endDate, daysOfWeek);
  }

  @Override
  public void createAllDayEventSeriesUntil(String subject, String startDate, String weekdays,
                                           String dateUntil) {
    LocalDate eventDate = returnLocalDate(startDate);
    LocalDateTime curr = eventDate.atTime(Event.DEFAULT_START_TIME);
    LocalDateTime endDate = eventDate.atTime(Event.DEFAULT_END_TIME);
    List<LocalDateTime> daysOfWeek = getDates(subject, curr, weekdays, dateUntil, endDate, 0);
    createSeries(subject, endDate, daysOfWeek);
  }


  private void createSeries(String subject, LocalDateTime endDate, List<LocalDateTime> daysOfWeek) {
    List<Integer> series = new ArrayList<>();
    this.eventSeriesId++;

    for (LocalDateTime nextDay : daysOfWeek) {
      EventObject newEvent = Event.getBuilder()
          .subject(subject)
          .startDateTime(nextDay)
          .endDateTime(nextDay.withHour(endDate.getHour()).withMinute(endDate.getMinute()))
          .eventSeriesId(eventSeriesId)
          .build();

      events.put(newEvent.hashCode(), newEvent);
      series.add(newEvent.hashCode());
    }
    eventSeries.put(this.eventSeriesId, series);
  }

  private List<LocalDateTime> getDates(String subject, LocalDateTime curr, String weekdays,
                                       String dateUntil, LocalDateTime endDate, int occur) {
    int key = Objects.hash(subject, curr, curr.withHour(endDate.getHour())
        .withMinute(endDate.getMinute()));
    if (this.events.containsKey(key)) {
      throw new IllegalArgumentException("Event already exists");
    }

    LocalDate endLoop = null;
    if (occur == 0) {
      endLoop = returnLocalDate(dateUntil);
    }
    List<DayOfWeek> daysOfWeek = new ArrayList<>();
    for (char c : weekdays.toCharArray()) {
      daysOfWeek.add(findDayOfWeek(c));
    }

    List<LocalDateTime> dates = new ArrayList<>();
    if (!daysOfWeek.contains(curr.getDayOfWeek())) {
      dates.add(curr);
      occur = occur - 1;
    }

    while (occurOrUntil(curr, endLoop, occur)) {
      if (daysOfWeek.contains(curr.getDayOfWeek())) {
        LocalDateTime endDay = curr.withHour(endDate.getHour()).withMinute(endDate.getMinute());
        key = Objects.hash(subject, curr, endDay);
        if (this.events.containsKey(key)) {
          throw new IllegalArgumentException("Event already exists");
        }
        dates.add(curr);
        occur = occur - 1;
      }

      curr = curr.plusDays(1);
    }
    return dates;
  }

  private boolean occurOrUntil(LocalDateTime curr, LocalDate endLoop, int occur) {
    if (endLoop == null) {
      return occur > 0;
    } else {
      return !curr.isAfter(endLoop.atTime(23, 59));
    }
  }

  private boolean isPropSubjectOrDateTimes(EventProperty property) {
    return property.equals(EventProperty.SUBJECT) || property.equals(EventProperty.START)
      || property.equals(EventProperty.END);
  }

  @Override
  public void editEvent(EventProperty property, String subject, String startDateTime,
                        String endDateTime, String val) {
    changeEvent(property, subject, startDateTime, endDateTime, val);
    checkEditRules(property, val);

  }

  private void seriesSplitter(EventObject event) {
    int key = event.getEventSeriesId();
    List<Integer> series = this.eventSeries.get(key);
    List<Integer> newSeries = new ArrayList<>();
    List<Integer> oldSeries = new ArrayList<>();
    LocalTime newStart = event.getStartDateTime().toLocalTime();
    this.eventSeriesId++;
    for (Integer i : series) {
      EventObject e = this.events.get(i);
      if (e.getStartDateTime().toLocalTime().equals(newStart)) {
        Event.EventBuilder builder = Event.getBuilder()
            .subject(e.getSubject())
            .startDateTime(e.getStartDateTime())
            .copyEventFrom(e)
            .eventSeriesId(this.eventSeriesId);

        EventObject newEvent = builder.build();
        this.events.remove(e.hashCode());
        this.events.put(newEvent.hashCode(), newEvent);
        newSeries.add(newEvent.hashCode());
      } else {
        oldSeries.add(e.hashCode());
      }
    }

    this.eventSeries.put(this.eventSeriesId, newSeries);
    if (oldSeries.isEmpty()) {
      this.eventSeries.remove(key);
    } else {
      this.eventSeries.put(key, oldSeries);
    }
  }

  private void checkEditRules(EventProperty property, String val) {
    Map<Integer, EventObject> seriesSplit = new HashMap<>();
    for (Integer key : this.tempEvents.keySet()) {
      EventObject event = this.tempEvents.get(key);
      if (isPropSubjectOrDateTimes(property) && events.containsKey(event.hashCode())) {
        throw new IllegalArgumentException("Event already exists");
      }
      if (event.getStartDateTime().isAfter(event.getEndDateTime())) {
        throw new IllegalArgumentException("Event can't start after the end time or vice versa");
      }
      if (!event.getStartDateTime().toLocalTime().equals(
          this.events.get(key).getStartDateTime().toLocalTime())
          && event.getEventSeriesId() != -1) {
        seriesSplit.put(event.getEventSeriesId(), event);
      }
    }

    for (Integer key : this.tempEvents.keySet()) {
      if (this.tempEvents.get(key).getEventSeriesId() != -1) {
        List<Integer> newSeries = this.eventSeries.get(this.tempEvents.get(key).getEventSeriesId());
        newSeries.remove(key);
        newSeries.add(this.tempEvents.get(key).hashCode());
      }
      events.remove(key);
      events.put(this.tempEvents.get(key).hashCode(), this.tempEvents.get(key));
    }
    this.tempEvents.clear();
    if (!seriesSplit.isEmpty()) {
      for (EventObject newEvent : seriesSplit.values()) {
        seriesSplitter(newEvent);
      }
    }

  }

  private void changeEvent(EventProperty property, String subject, String startDateTime,
                        String endDateTime, String val) {
    LocalDateTime startDate = returnLocalDateTime(startDateTime);
    LocalDateTime endDate = returnLocalDateTime(endDateTime);

    int key = Objects.hash(subject, startDate, endDate);

    if (!events.containsKey(key)) {
      throw new IllegalArgumentException("Event to edit not found");
    }

    EventObject editEvent = events.get(key);
    Event.EventBuilder builder = Event.getBuilder()
        .subject(editEvent.getSubject())
        .startDateTime(editEvent.getStartDateTime())
        .copyEventFrom(editEvent);

    builder = property.editProp(builder, val);

    EventObject newEvent = builder.build();

    tempEvents.put(key, newEvent);
  }

  private void editPartOfSeries(EventObject start, EventProperty property, String val) {
    List<Integer> series = eventSeries.get(start.getEventSeriesId());
    for (Integer i : series) {
      EventObject e = this.events.get(i);
      if (e.getStartDateTime().isAfter(start.getStartDateTime())
          || e.getStartDateTime().equals(start.getStartDateTime())) {
        changeEvent(property, e.getSubject(), e.getStartDateTime().toString(),
            e.getEndDateTime().toString(), val);
      }
    }
  }

  @Override
  public void editEvents(EventProperty property, String subject, String startDateTime, String val) {
    LocalDateTime startDate = returnLocalDateTime(startDateTime);
    List<EventObject> foundEvents = this.filter(new EventSubjectFilter(subject)
        .and(new EventStartDateTimeFilter(startDate)));

    for (EventObject e : foundEvents) {
      if (e.getEventSeriesId() != -1) {
        editPartOfSeries(e, property, val);
      } else {
        changeEvent(property, subject, e.getStartDateTime().toString(),
            e.getEndDateTime().toString(), val);
      }
    }
    checkEditRules(property, val);
  }

  private void editAllOfSeries(EventObject start, EventProperty property, String val) {
    List<Integer> series = eventSeries.get(start.getEventSeriesId());
    for (Integer i : series) {
      EventObject e = this.events.get(i);
      changeEvent(property, e.getSubject(), e.getStartDateTime().toString(),
          e.getEndDateTime().toString(), val);
    }
  }

  @Override
  public void editSeries(EventProperty property, String subject, String startDateTime, String val) {
    LocalDateTime startDate = returnLocalDateTime(startDateTime);
    List<EventObject> foundEvents = this.filter(new EventSubjectFilter(subject)
        .and(new EventStartDateTimeFilter(startDate)));

    for (EventObject e : foundEvents) {
      if (e.getEventSeriesId() != -1) {
        editAllOfSeries(e, property, val);
      } else {
        changeEvent(property, subject, e.getStartDateTime().toString(),
            e.getEndDateTime().toString(), val);
      }
    }
    checkEditRules(property, val);
  }

  private DayOfWeek findDayOfWeek(char c) {
    switch (c) {
      case 'M':
        return DayOfWeek.MONDAY;
      case 'T':
        return DayOfWeek.TUESDAY;
      case 'W':
        return DayOfWeek.WEDNESDAY;
      case 'R':
        return DayOfWeek.THURSDAY;
      case 'F':
        return DayOfWeek.FRIDAY;
      case 'S':
        return DayOfWeek.SATURDAY;
      case 'U':
        return DayOfWeek.SUNDAY;
      default:
        throw new IllegalArgumentException("Unknown day of week: " + c);

    }
  }

  private LocalDateTime returnLocalDateTime(String val) {
    try {
      return LocalDateTime.parse(val);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid date/time format: " + val);
    }
  }

  private LocalDate returnLocalDate(String val) {
    try {
      return LocalDate.parse(val);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid date/time format: " + val);
    }
  }

  @Override
  public List<EventObject> getAllEvents() {
    return new ArrayList<>(this.events.values());
  }

  @Override
  public List<EventObject> getEventsBetween(String startDateTime, String endDateTime) {
    List<EventObject> filteredEvents;
    if (endDateTime.isEmpty()) {
      LocalDate eventDate = returnLocalDate(startDateTime);
      filteredEvents = this.filter(new EventStartDateFilter(eventDate));
    } else {
      LocalDateTime startEventDateTime = returnLocalDateTime(startDateTime);
      LocalDateTime endEventDateTime = returnLocalDateTime(endDateTime);
      filteredEvents = this.filter(new EventDateTimeIntervalFilter(startEventDateTime,
          endEventDateTime));
    }
    return filteredEvents;
  }

  @Override
  public String eventsForList(List<EventObject> events) {
    StringBuilder s = new StringBuilder();
    for (EventObject event : events) {
      s.append(event.eventForBulletPoint()).append("\n");
    }
    return s.toString();
  }

  @Override
  public String getUserStatus(String datetimeString) {
    LocalDateTime datetime = returnLocalDateTime(datetimeString);
    List<EventObject> events = this.getAllEvents();
    List<EventObject> filteredEvents = new ArrayList<>();
    for (EventObject event : events) {
      if (datetime.equals(event.getStartDateTime()) || datetime.equals(event.getEndDateTime())
          || (datetime.isAfter(event.getStartDateTime())
          && datetime.isBefore(event.getEndDateTime()))) {
        filteredEvents.add(event);
      }
    }
    DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.ENGLISH);
    DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);
    if (filteredEvents.isEmpty()) {
      return "Available on " + datetime.format(dateFormat) + " at " + datetime.format(timeFormat);
    } else {
      return "Busy on " + datetime.format(dateFormat) + " at " + datetime.format(timeFormat);
    }
  }
}
