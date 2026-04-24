package calendar.model;

import calendar.exporter.CalendarExporter;
import calendar.exporter.ExporterFactory;
import calendar.util.TimezoneConverter;
import calendar.util.TimezoneConverter.DateTimePair;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Concrete implementation of a calendar that manages events and event series.
 * Maintains uniqueness constraint: no two events can have the same subject,
 * start date/time, and end date/time.
 * This class is immutable - modifications return new Calendar instances.
 */
public class Calendar implements IntCalendar {
  private final String name;
  private final ZoneId timezone;
  private final Map<EventKey, Event> events;
  private final Map<String, EventSeries> eventSeries;
  private final Map<String, BiFunction<Event, String, Event>> propertyUpdaters;


  /**
   * Constructs a Calendar with the given name and the system default timezone.
   *
   * @param name the name of the calendar
   */
  public Calendar(String name) {
    this(name, ZoneId.systemDefault());
  }

  /**
   * Constructs a Calendar with the given name and timezone.
   *
   * @param name     the name of the calendar
   * @param timezone the timezone for the calendar
   */
  public Calendar(String name, ZoneId timezone) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be null or empty");
    }
    if (timezone == null) {
      throw new IllegalArgumentException("Timezone cannot be null");
    }
    this.name = name;
    this.timezone = timezone;
    this.events = new HashMap<>();
    this.eventSeries = new HashMap<>();
    this.propertyUpdaters = initializePropertyUpdaters();
  }

  /**
   * Private copy constructor for creating modified copies of the calendar.
   * Used by withName() and withTimezone() methods to maintain immutability.
   * When timezone is changed, all event times are converted to the new timezone.
   *
   * @param other       the calendar to copy from
   * @param newName     the new name (null to keep existing)
   * @param newTimezone the new timezone (null to keep existing)
   */
  private Calendar(Calendar other, String newName, ZoneId newTimezone) {
    this.name = newName != null ? newName : other.name;
    this.timezone = newTimezone != null ? newTimezone : other.timezone;
    this.propertyUpdaters = other.propertyUpdaters;

    // If timezone is being changed, convert all event times
    if (newTimezone != null && !newTimezone.equals(other.timezone)) {
      this.events = new HashMap<>();
      this.eventSeries = new HashMap<>();

      // Convert all events to the new timezone
      for (Event event : other.events.values()) {
        // Convert start date/time
        DateTimePair convertedStart = TimezoneConverter.convert(
            event.getStartDate(), event.getStartTime(),
            other.timezone, newTimezone
        );

        // Convert end date/time
        DateTimePair convertedEnd = TimezoneConverter.convert(
            event.getEndDate(), event.getEndTime(),
            other.timezone, newTimezone
        );

        // Create new event with converted times
        Event convertedEvent = new Event(
            event.getSubject(),
            convertedStart.getDate(),
            convertedStart.getTime(),
            convertedEnd.getDate(),
            convertedEnd.getTime(),
            event.getDescription(),
            event.getLocation(),
            event.getStatus()
        );

        this.events.put(new EventKey(convertedEvent), convertedEvent);
      }

      // Note: Event series are not converted as they would need to be regenerated
      // This is a design decision - when timezone changes, series are cleared
    } else {
      // No timezone change, just copy the maps
      this.events = new HashMap<>(other.events);
      this.eventSeries = new HashMap<>(other.eventSeries);
    }
  }

  @Override
  public void createEvent(String subject, Date startDate, Time startTime, Date endDate,
                          Time endTime, String description, Location location, Status status) {
    Event event = new Event(subject, startDate, startTime, endDate, endTime,
        description, location, status);

    // Check uniqueness constraint
    if (eventExists(event)) {
      throw new IllegalArgumentException(
          "An event with the same subject, start date/time, and end date/time already exists");
    }

    events.put(new EventKey(event), event);
  }

  @Override
  public void createEvent(String subject, Date startDate, Time startTime, Date endDate,
                          Time endTime, String description) {
    createEvent(subject, startDate, startTime, endDate, endTime, description, null, null);
  }

  @Override
  public void createEvent(String subject, Date startDate, Time startTime, Date endDate,
                          Time endTime, Location location) {
    createEvent(subject, startDate, startTime, endDate, endTime, null, location, null);
  }

  @Override
  public void createEvent(String subject, Date startDate, Time startTime, Date endDate,
                          Time endTime, Status status) {
    createEvent(subject, startDate, startTime, endDate, endTime, null, null, status);
  }

  @Override
  public void createEvent(String subject, Date startDate, Time startTime, Date endDate,
                          Time endTime) {
    createEvent(subject, startDate, startTime, endDate, endTime, null, null, null);
  }

  @Override
  public void createAllDayEvent(String subject, Date date, String description,
                                Location location, Status status) {
    createEvent(subject, date, new Time(8, 0), date, new Time(17, 0),
        description, location, status);
  }

  @Override
  public void createAllDayEvent(String subject, Date date, String description) {
    createAllDayEvent(subject, date, description, null, null);
  }

  @Override
  public void createAllDayEvent(String subject, Date date, Location location) {
    createAllDayEvent(subject, date, null, location, null);
  }

  @Override
  public void createAllDayEvent(String subject, Date date, Status status) {
    createAllDayEvent(subject, date, null, null, status);
  }

  @Override
  public void createAllDayEvent(String subject, Date date) {
    createAllDayEvent(subject, date, null, null, null);
  }

  @Override
  public void createEventSeries(String subject, Date startDate, Time startTime, Time endTime,
                                Set<Day> repeatDays, int occurrences,
                                String description, Location location, Status status) {
    EventSeries series = new EventSeries(subject, startDate, startTime, endTime,
        repeatDays, occurrences, description, location, status);

    // Check uniqueness constraint for all generated events
    List<Event> generatedEvents = series.generateEvents();
    for (Event event : generatedEvents) {
      if (eventExists(event)) {
        throw new IllegalArgumentException(
            "One or more events in the series violate the uniqueness constraint");
      }
    }

    eventSeries.put(series.getSeriesId(), series);
    for (Event event : generatedEvents) {
      events.put(new EventKey(event), event);
    }
  }

  @Override
  public void createEventSeries(String subject, Date startDate, Time startTime, Time endTime,
                                Set<Day> repeatDays, int occurrences, String description) {
    createEventSeries(subject, startDate, startTime, endTime, repeatDays, occurrences,
        description, null, null);
  }

  @Override
  public void createEventSeries(String subject, Date startDate, Time startTime, Time endTime,
                                Set<Day> repeatDays, int occurrences, Location location) {
    createEventSeries(subject, startDate, startTime, endTime, repeatDays, occurrences,
        null, location, null);
  }

  @Override
  public void createEventSeries(String subject, Date startDate, Time startTime, Time endTime,
                                Set<Day> repeatDays, int occurrences, Status status) {
    createEventSeries(subject, startDate, startTime, endTime, repeatDays, occurrences,
        null, null, status);
  }

  @Override
  public void createEventSeries(String subject, Date startDate, Time startTime, Time endTime,
                                Set<Day> repeatDays, int occurrences) {
    createEventSeries(subject, startDate, startTime, endTime, repeatDays, occurrences,
        null, null, null);
  }

  @Override
  public void createEventSeries(String subject, Date startDate, Time startTime, Time endTime,
                                Set<Day> repeatDays, Date endDate,
                                String description, Location location, Status status) {
    EventSeries series = new EventSeries(subject, startDate, startTime, endTime,
        repeatDays, endDate, description, location, status);

    // Check uniqueness constraint for all generated events
    List<Event> generatedEvents = series.generateEvents();
    for (Event event : generatedEvents) {
      if (eventExists(event)) {
        throw new IllegalArgumentException(
            "One or more events in the series violate the uniqueness constraint");
      }
    }

    eventSeries.put(series.getSeriesId(), series);
    for (Event event : generatedEvents) {
      events.put(new EventKey(event), event);
    }
  }

  @Override
  public void createEventSeries(String subject, Date startDate, Time startTime, Time endTime,
                                Set<Day> repeatDays, Date endDate, String description) {
    createEventSeries(subject, startDate, startTime, endTime, repeatDays, endDate,
        description, null, null);
  }

  @Override
  public void createEventSeries(String subject, Date startDate, Time startTime, Time endTime,
                                Set<Day> repeatDays, Date endDate, Location location) {
    createEventSeries(subject, startDate, startTime, endTime, repeatDays, endDate,
        null, location, null);
  }

  @Override
  public void createEventSeries(String subject, Date startDate, Time startTime, Time endTime,
                                Set<Day> repeatDays, Date endDate, Status status) {
    createEventSeries(subject, startDate, startTime, endTime, repeatDays, endDate,
        null, null, status);
  }

  @Override
  public void createEventSeries(String subject, Date startDate, Time startTime, Time endTime,
                                Set<Day> repeatDays, Date endDate) {
    createEventSeries(subject, startDate, startTime, endTime, repeatDays, endDate,
        null, null, null);
  }

  @Override
  public void editEvent(String subject, Date startDate, Time startTime, Date endDate,
                        Time endTime, String property, String newValue) {
    Event targetEvent = findEvent(subject, startDate, startTime, endDate, endTime);
    if (targetEvent == null) {
      throw new IllegalArgumentException("Event not found");
    }

    Event updatedEvent = applyPropertyChange(targetEvent, property, newValue);


    events.remove(new EventKey(targetEvent));
    events.put(new EventKey(updatedEvent), updatedEvent);
  }

  @Override
  public void editEventsFromDate(String subject, Date startDate, Time startTime,
                                 String property, String newValue) {
    Event targetEvent = findEventByStartDateTime(subject, startDate, startTime);
    if (targetEvent == null) {
      throw new IllegalArgumentException("Event not found");
    }

    // Find all events with same subject that start at or after the given date/time
    List<Event> eventsToUpdate = events.values().stream()
        .filter(e -> e.getSubject().equals(subject))
        .filter(e -> isAtOrAfter(e.getStartDate(), e.getStartTime(), startDate, startTime))
        .collect(Collectors.toList());


    for (Event event : eventsToUpdate) {
      Event updatedEvent = applyPropertyChange(event, property, newValue);

      // Check uniqueness constraint
      if (!event.equals(updatedEvent) && eventExists(updatedEvent)) {
        throw new IllegalArgumentException(
            "One or more updated events violate the uniqueness constraint");
      }

      events.remove(new EventKey(event));
      events.put(new EventKey(updatedEvent), updatedEvent);
    }
  }

  @Override
  public void editSeries(String subject, Date startDate, Time startTime,
                         String property, String newValue) {
    Event targetEvent = findEventByStartDateTime(subject, startDate, startTime);
    if (targetEvent == null) {
      throw new IllegalArgumentException("Event not found");
    }

    // Find all events with the same subject
    List<Event> eventsToUpdate = events.values().stream()
        .filter(e -> e.getSubject().equals(subject))
        .collect(Collectors.toList());

    for (Event event : eventsToUpdate) {
      Event updatedEvent = applyPropertyChange(event, property, newValue);

      // Check uniqueness constraint
      if (!event.equals(updatedEvent) && eventExists(updatedEvent)) {
        throw new IllegalArgumentException(
            "One or more updated events violate the uniqueness constraint");
      }

      events.remove(new EventKey(event));
      events.put(new EventKey(updatedEvent), updatedEvent);
    }
  }

  @Override
  public List<IntEvent> getEventsOnDate(Date date) {
    return events.values().stream()
        .filter(e -> e.getStartDate().equals(date)
            || (e.getStartDate().compareTo(date) < 0 && e.getEndDate().compareTo(date) >= 0))
        .sorted(Comparator.comparing(IntEvent::getStartTime))
        .collect(Collectors.toList());
  }

  @Override
  public List<IntEvent> getEventsInRange(Date startDate, Time startTime,
                                         Date endDate, Time endTime) {
    LocalDateTime rangeStart = toLocalDateTime(startDate, startTime);
    LocalDateTime rangeEnd = toLocalDateTime(endDate, endTime);

    return events.values().stream()
        .filter(e -> {
          LocalDateTime eventStart = toLocalDateTime(e.getStartDate(), e.getStartTime());
          LocalDateTime eventEnd = toLocalDateTime(e.getEndDate(), e.getEndTime());
          return !(eventEnd.isBefore(rangeStart) || eventStart.isAfter(rangeEnd));
        })
        .sorted(Comparator.comparing(IntEvent::getStartDate)
            .thenComparing(IntEvent::getStartTime))
        .collect(Collectors.toList());
  }

  @Override
  public boolean isBusy(Date date, Time time) {
    LocalDateTime queryTime = toLocalDateTime(date, time);

    return events.values().stream()
        .anyMatch(e -> {
          LocalDateTime eventStart = toLocalDateTime(e.getStartDate(), e.getStartTime());
          LocalDateTime eventEnd = toLocalDateTime(e.getEndDate(), e.getEndTime());
          return !queryTime.isBefore(eventStart) && queryTime.isBefore(eventEnd);
        });
  }

  @Override
  public String export(String fileName) {
    // Get the appropriate exporter based on file extension
    CalendarExporter exporter = ExporterFactory.getExporter(fileName);

    // Get all events as a list
    List<IntEvent> eventList = events.values().stream()
        .sorted(Comparator.comparing(IntEvent::getStartDate)
            .thenComparing(IntEvent::getStartTime))
        .collect(Collectors.toList());

    // Export using the selected exporter
    return exporter.export(eventList, fileName, this.name);
  }

  @Override
  public String getName() {
    return name;
  }

  // Helper methods

  private boolean eventExists(IntEvent event) {
    return events.containsKey(new EventKey(event));
  }

  private Event findEvent(String subject, Date startDate, Time startTime,
                          Date endDate, Time endTime) {
    return events.get(new EventKey(subject, startDate, startTime, endDate, endTime));
  }

  private Event findEventByStartDateTime(String subject, Date startDate, Time startTime) {
    return events.values().stream()
        .filter(e -> e.getSubject().equals(subject)
            && e.getStartDate().equals(startDate)
            && e.getStartTime().equals(startTime))
        .findFirst()
        .orElse(null);
  }

  private boolean isAtOrAfter(Date date1, Time time1, Date date2, Time time2) {
    int dateCompare = date1.compareTo(date2);
    if (dateCompare != 0) {
      return dateCompare > 0;
    }
    return time1.compareTo(time2) >= 0;
  }

  private LocalDateTime toLocalDateTime(Date date, Time time) {
    return LocalDateTime.of(date.getYear(), date.getMonth(), date.getDay(),
        time.getHour(), time.getMinute());
  }

  /**
   * Initializes the property updaters map with functions for each supported property.
   * This makes it easy to add new properties in the future.
   *
   * @return a map of property names to their corresponding update functions
   */
  private Map<String, BiFunction<Event, String, Event>> initializePropertyUpdaters() {
    Map<String, BiFunction<Event, String, Event>> updaters = new HashMap<>();

    updaters.put("subject", (event, newValue) -> (Event) event.withSubject(newValue));

    updaters.put("start", (event, newValue) -> {
      // Parse newValue as "YYYY-MM-DDThh:mm"
      Date newStartDate = parseDate(newValue);
      Time newStartTime = parseTime(newValue);
      return (Event) event.withStart(newStartDate, newStartTime);
    });

    updaters.put("end", (event, newValue) -> {
      Date newEndDate = parseDate(newValue);
      Time newEndTime = parseTime(newValue);
      return (Event) event.withEnd(newEndDate, newEndTime);
    });

    updaters.put("description", (event, newValue) -> (Event) event.withDescription(newValue));

    updaters.put("location", (event, newValue) -> {
      Location newLocation = Location.valueOf(newValue.toUpperCase());
      return (Event) event.withLocation(newLocation);
    });

    updaters.put("status", (event, newValue) -> {
      Status newStatus = Status.valueOf(newValue.toUpperCase());
      return (Event) event.withStatus(newStatus);
    });

    return updaters;
  }

  /**
   * Applies a property change to an event using the property updaters map.
   * This approach makes it easy to add new properties without modifying this method.
   *
   * @param event    the event to update
   * @param property the property to change
   * @param newValue the new value for the property
   * @return a new Event with the updated property
   * @throws IllegalArgumentException if the property is not supported
   */
  private Event applyPropertyChange(Event event, String property, String newValue) {
    BiFunction<Event, String, Event> updater = propertyUpdaters.get(property.toLowerCase());
    if (updater == null) {
      throw new IllegalArgumentException("Unknown property: " + property);
    }
    return updater.apply(event, newValue);
  }

  private Date parseDate(String dateTimeString) {
    int year = Integer.parseInt(dateTimeString.substring(0, 4));
    int month = Integer.parseInt(dateTimeString.substring(5, 7));
    int day = Integer.parseInt(dateTimeString.substring(8, 10));
    return new Date(year, month, day);
  }

  private Time parseTime(String dateTimeString) {
    int hour = Integer.parseInt(dateTimeString.substring(11, 13));
    int minute = Integer.parseInt(dateTimeString.substring(14, 16));
    return new Time(hour, minute);
  }

  @Override
  public ZoneId getTimezone() {
    return timezone;
  }

  @Override
  public Calendar withName(String newName) {
    if (newName == null || newName.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be null or empty");
    }
    return new Calendar(this, newName, null);
  }

  @Override
  public Calendar withTimezone(ZoneId newTimezone) {
    if (newTimezone == null) {
      throw new IllegalArgumentException("Timezone cannot be null");
    }
    return new Calendar(this, null, newTimezone);
  }

  @Override
  public void copyEventTo(String subject, Date startDate, Time startTime,
                          IntCalendar targetCalendar, Date newStartDate, Time newStartTime) {
    // Find the event in this calendar by searching through events
    Event sourceEvent = null;
    for (Event event : events.values()) {
      if (event.getSubject().equals(subject)
          && event.getStartDate().equals(startDate)
          && event.getStartTime().equals(startTime)) {
        sourceEvent = event;
        break;
      }
    }

    if (sourceEvent == null) {
      throw new IllegalArgumentException(
          "Event not found: " + subject + " on " + startDate + " at " + startTime);
    }

    // Calculate the duration of the event
    long durationMinutes = calculateDurationMinutes(
        sourceEvent.getStartDate(), sourceEvent.getStartTime(),
        sourceEvent.getEndDate(), sourceEvent.getEndTime()
    );

    // If timezones differ, convert the new start time to target timezone
    Date finalStartDate = newStartDate;
    Time finalStartTime = newStartTime;

    if (!this.timezone.equals(targetCalendar.getTimezone())) {
      DateTimePair converted = TimezoneConverter.convert(
          newStartDate, newStartTime,
          this.timezone, targetCalendar.getTimezone()
      );
      finalStartDate = converted.getDate();
      finalStartTime = converted.getTime();
    }

    // Calculate new end date and time based on duration
    DateTimePair newEnd = addMinutes(finalStartDate, finalStartTime, durationMinutes);

    // Create a new event with the new dates/times and same properties
    Event copiedEvent = new Event(
        sourceEvent.getSubject(),
        finalStartDate,
        finalStartTime,
        newEnd.getDate(),
        newEnd.getTime(),
        sourceEvent.getDescription(),
        sourceEvent.getLocation(),
        sourceEvent.getStatus()
    );

    // Add to target calendar
    targetCalendar.addEvent(copiedEvent);
  }

  @Override
  public void copyEventsOnDateTo(Date date, IntCalendar targetCalendar, Date newDate) {
    List<IntEvent> eventsOnDate = getEventsOnDate(date);

    if (eventsOnDate.isEmpty()) {
      throw new IllegalArgumentException(
          "No events found on: " + date);
    }

    for (IntEvent event : eventsOnDate) {
      // Start with the original event times on the new date
      Date eventStartDate = newDate;
      Time eventStartTime = event.getStartTime();

      // If timezones differ, convert the event times to target timezone
      if (!this.timezone.equals(targetCalendar.getTimezone())) {
        DateTimePair convertedStart = TimezoneConverter.convert(
            newDate, event.getStartTime(),
            this.timezone, targetCalendar.getTimezone()
        );
        eventStartDate = convertedStart.getDate();
        eventStartTime = convertedStart.getTime();
      }

      // Calculate duration
      long durationMinutes = calculateDurationMinutes(
          event.getStartDate(), event.getStartTime(),
          event.getEndDate(), event.getEndTime()
      );

      // Calculate new end date and time
      DateTimePair newEnd = addMinutes(eventStartDate, eventStartTime, durationMinutes);

      // Create copied event
      Event copiedEvent = new Event(
          event.getSubject(),
          eventStartDate,
          eventStartTime,
          newEnd.getDate(),
          newEnd.getTime(),
          event.getDescription(),
          event.getLocation(),
          event.getStatus()
      );

      targetCalendar.addEvent(copiedEvent);
    }
  }

  @Override
  public void copyEventsInRangeTo(Date startDate, Date endDate,
                                  IntCalendar targetCalendar, Date newStartDate) {
    // Get all events in the range (from start of startDate to end of endDate)
    List<IntEvent> eventsInRange = getEventsInRange(
        startDate, new Time(0, 0),
        endDate, new Time(23, 59)
    );

    if (eventsInRange.isEmpty()) {
      throw new IllegalArgumentException(
          "No events found between: " + startDate + " and " + endDate);
    }

    // Calculate the offset in days between old and new start dates
    long dayOffset = calculateDayOffset(startDate, newStartDate);

    for (IntEvent event : eventsInRange) {
      // Calculate new dates by applying the offset
      Date newEventStartDate = addDays(event.getStartDate(), dayOffset);
      Date newEventEndDate = addDays(event.getEndDate(), dayOffset);
      Time newEventStartTime = event.getStartTime();
      Time newEventEndTime = event.getEndTime();

      // If timezones differ, convert the event times to target timezone
      if (!this.timezone.equals(targetCalendar.getTimezone())) {
        DateTimePair convertedStart = TimezoneConverter.convert(
            newEventStartDate, event.getStartTime(),
            this.timezone, targetCalendar.getTimezone()
        );
        DateTimePair convertedEnd = TimezoneConverter.convert(
            newEventEndDate, event.getEndTime(),
            this.timezone, targetCalendar.getTimezone()
        );

        newEventStartDate = convertedStart.getDate();
        newEventStartTime = convertedStart.getTime();
        newEventEndDate = convertedEnd.getDate();
        newEventEndTime = convertedEnd.getTime();
      }

      // Create copied event with offset dates and converted times
      Event copiedEvent = new Event(
          event.getSubject(),
          newEventStartDate,
          newEventStartTime,
          newEventEndDate,
          newEventEndTime,
          event.getDescription(),
          event.getLocation(),
          event.getStatus()
      );

      targetCalendar.addEvent(copiedEvent);
    }
  }

  @Override
  public void addEvent(IntEvent event) {
    if (event == null) {
      throw new IllegalArgumentException("Event cannot be null");
    }

    // Check uniqueness constraint
    if (eventExists(event)) {
      throw new IllegalArgumentException(
          "An event with the same subject, start date/time, and end date/time already exists");
    }

    // Cast to Event since our implementation only works with Event instances
    if (!(event instanceof Event)) {
      throw new IllegalArgumentException("Event must be an instance of Event class");
    }
    events.put(new EventKey(event), (Event) event);
  }

  /**
   * Calculates the duration in minutes between two date/time pairs.
   */
  private long calculateDurationMinutes(Date startDate, Time startTime,
                                        Date endDate, Time endTime) {
    LocalDateTime start = LocalDateTime.of(
        startDate.getYear(), startDate.getMonth(), startDate.getDay(),
        startTime.getHour(), startTime.getMinute()
    );
    LocalDateTime end = LocalDateTime.of(
        endDate.getYear(), endDate.getMonth(), endDate.getDay(),
        endTime.getHour(), endTime.getMinute()
    );
    return java.time.Duration.between(start, end).toMinutes();
  }

  /**
   * Adds minutes to a date/time pair and returns the result.
   */
  private DateTimePair addMinutes(Date date, Time time, long minutes) {
    LocalDateTime dateTime = LocalDateTime.of(
        date.getYear(), date.getMonth(), date.getDay(),
        time.getHour(), time.getMinute()
    );
    LocalDateTime result = dateTime.plusMinutes(minutes);
    return new DateTimePair(
        new Date(result.getYear(), result.getMonthValue(), result.getDayOfMonth()),
        new Time(result.getHour(), result.getMinute())
    );
  }

  /**
   * Calculates the offset in days between two dates.
   */
  private long calculateDayOffset(Date fromDate, Date toDate) {
    LocalDateTime from = LocalDateTime.of(
        fromDate.getYear(), fromDate.getMonth(), fromDate.getDay(), 0, 0
    );
    LocalDateTime to = LocalDateTime.of(
        toDate.getYear(), toDate.getMonth(), toDate.getDay(), 0, 0
    );
    return java.time.temporal.ChronoUnit.DAYS.between(from, to);
  }

  /**
   * Adds days to a date and returns the result.
   */
  private Date addDays(Date date, long days) {
    LocalDateTime dateTime = LocalDateTime.of(
        date.getYear(), date.getMonth(), date.getDay(), 0, 0
    );
    LocalDateTime result = dateTime.plusDays(days);
    return new Date(result.getYear(), result.getMonthValue(), result.getDayOfMonth());
  }

}

