package calendar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Main calendar class that manages events to implement core functionality.
 * */
public class Calendar {
  private String name;
  private final Map<String, Events> events;
  private final Map<String, EventSeries> eventSeries;
  private ZoneId timeZone;

  /**
   * Constructor to initialise calendar class.
   *
   * @param name the name of calendar
   * @param timezone the given timezone
   * */
  public Calendar(String name, ZoneId timezone) {
    this.name = name;
    this.timeZone = timezone;
    this.events = new HashMap<>();
    this.eventSeries = new HashMap<>();
  }

  /**
   * Function to add an event in calendar.
   * Events with same subject, start, and end time are not allowed.
   *
   * @param event the event to add
   * @throws IllegalArgumentException if event with same subject, start, end date exists
   * */
  public void addEvent(Events event) throws IllegalArgumentException {
    String eventKey = event.getId();
    if (this.events.containsKey(eventKey)) {
      throw new IllegalArgumentException("Event with same subject, start, and end date exists");
    }
    this.events.put(eventKey, event);
  }

  /**
   * Removes an event from calendar.
   *
   * @param event the event to remove
   * */
  public void removeEvent(Events event) {
    String eventKey = event.getId();
    this.events.remove(eventKey);
  }

  /**
   * Get all events scheduled on a specific date.
   *
   * @param eventDate the date to query
   * @return list of events, sorted by start time
   * */
  public List<Events> getEvents(LocalDate eventDate) {
    List<Events> eventsList = new ArrayList<>();
    for (Events event : this.events.values()) {
      LocalDate startDate = event.getStartTime().toLocalDate();
      LocalDate endDate = event.getEndTime().toLocalDate();
      // date must be between start and end date
      if ((eventDate.isEqual(startDate) || eventDate.isAfter(startDate))
          && (eventDate.isEqual(endDate) || eventDate.isBefore(endDate))) {
        eventsList.add(event);
      }
    }
    return eventsList;
  }

  /**
   * Get all events scheduled between two dates.
   *
   * @param startDate the start date of the time range
   * @param endDate the end date of the time range
   * @return the list of events, sorted by start time
   * */
  public List<Events> getEventsBetween(LocalDateTime startDate, LocalDateTime endDate) {
    List<Events> eventsList = new ArrayList<>();
    for (Events event : this.events.values()) {
      if ((event.getStartTime().isBefore(endDate) || event.getStartTime().isEqual(startDate))
          && (event.getEndTime().isAfter(startDate) || event.getEndTime().isEqual(endDate))) {
        eventsList.add(event);
      }
    }
    return eventsList;
  }

  /**
   * Find a specific event by subject and start time.
   *
   * @param subject the event's subject
   * @param startTime the event's start time
   * @return the specific event matching the constraints
   * */
  public Events findEvent(String subject, LocalDateTime startTime) {
    for (Events event : this.events.values()) {
      if (event.getStartTime().equals(startTime) && event.getSubject().equalsIgnoreCase(subject)) {
        return event;
      }
    }
    return null;
  }

  /**
   * Find a specific event by subject and start time.
   *
   * @param subject the event's subject
   * @param startTime the event's start time
   * @return the specific event matching the constraints
   * */
  public List<Events> findEvents(String subject, LocalDateTime startTime) {
    List<Events> eventsList = new ArrayList<>();
    for (Events event : this.events.values()) {
      if (event.getStartTime().equals(startTime) && event.getSubject().equalsIgnoreCase(subject)) {
        eventsList.add(event);
      }
    }
    return eventsList;
  }

  /**
   * Function to check if user is busy at a given date and time.
   * The condition for being busy is that the current start date is
   * >= event's start date and < event's end date.
   *
   * @param date the date to check
   * @return true if user is busy, else false
   * */
  public boolean isBusy(LocalDateTime date) {
    boolean busy = false;
    for (Events event : this.events.values()) {
      if (!date.isBefore(event.getStartTime()) && date.isBefore(event.getEndTime())) {
        busy = true;
        break;
      }
    }
    return busy;
  }

  /**
   * Register an event series in the calendar.
   *
   * @param eventSeries the series to register
   * */
  public void registerSeries(EventSeries eventSeries) {
    this.eventSeries.put(eventSeries.getSeriesId(), eventSeries);
  }

  /**
   * Get all events in calendar.
   *
   * @return collection of all events
   * */
  public Collection<Events> getAllEvents() {
    return new ArrayList<>(this.events.values());
  }

  /**
   * Get an event series by its ID and start time.
   *
   * @param seriesId the seriesId
   * @param startTime the starting time of the series
   * @return the event series
   * */
  public List<Events> getEventSeries(String seriesId, LocalDateTime startTime) {
    List<Events> eventsList = new ArrayList<>();
    if (seriesId != null) {
      for (Events eventSeries : this.events.values()) {
        if (seriesId.equals(eventSeries.getIdSeries())) {
          boolean started = eventSeries.getInitStart() != null
              && Objects.equals(startTime, eventSeries.getInitStart());
          if (started) {
            eventsList.add(eventSeries);
          }
        }
      }
    }
    return eventsList;
  }

  /**
   * Get an event series by its ID and start time.
   *
   * @param seriesId the seriesId
   * @param startTime the starting time of the series
   * @return the event series
   * */
  public List<Events> getEventSeriesByDate(String seriesId, LocalDateTime startTime) {
    List<Events> eventsList = new ArrayList<>();
    if (seriesId != null) {
      for (Events eventSeries : this.events.values()) {
        if (seriesId.equals(eventSeries.getIdSeries())
            && !eventSeries.getStartTime().isBefore(startTime)) {
          eventsList.add(eventSeries);
        }
      }
    }
    return eventsList;
  }

  /**
   * Getter function to obtain name of calendar.
   *
   * @return the name of calendar
   * */
  public String getName() {
    return this.name;
  }

  /**
   * Setter function to set the name of calendar.
   *
   * @param name the name of calendar
   * */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Getter function to obtain the time zone of the calendar.
   *
   * @return the timezone of the calendar
   * */
  public ZoneId getTimeZone() {
    return this.timeZone;
  }

  /**
   * Setter function to initialise the timezone of the calendar.
   *
   * @param timeZone the given timezone
   * */
  public void setTimeZone(ZoneId timeZone) {
    this.timeZone = timeZone;
  }

  /**
   * Function to check if an event with the given key is present.
   *
   * @param key the unique event key
   * @return true if the key exists, false otherwise
   * */
  public boolean eventExists(String key) {
    return this.events.containsKey(key);
  }
}
