package calendar.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a series of recurring events.
 * Events in a series repeat on specific days of the week for a specific number of occurrences
 * or until a specific end date.
 * All events in a series have the same start time.
 * A single event in a series can only span one day (must start and finish on the same day).
 */
public class EventSeries {
  private final String seriesId;
  private final String subject;
  private final Time startTime;
  private final Time endTime;
  private final Set<Day> repeatDays;
  private final Date startDate;
  private final Date endDate;
  private final Integer occurrences;
  private final String description;
  private final Location location;
  private final Status status;

  /**
   * Constructs an EventSeries that repeats for a specific number of occurrences.
   *
   * @param subject     the subject of the events
   * @param startDate   the start date of the first event
   * @param startTime   the start time for all events
   * @param endTime     the end time for all events
   * @param repeatDays  the days of the week to repeat on
   * @param occurrences the number of occurrences
   * @param description the description (can be null)
   * @param location    the location (can be null)
   * @param status      the status (can be null)
   */
  public EventSeries(String subject, Date startDate, Time startTime, Time endTime,
                     Set<Day> repeatDays, int occurrences, String description,
                     Location location, Status status) {
    this.seriesId = UUID.randomUUID().toString();
    this.subject = subject;
    this.startDate = startDate;
    this.startTime = startTime;
    this.endTime = endTime;
    this.repeatDays = new HashSet<>(repeatDays);
    this.occurrences = occurrences;
    this.endDate = null;
    this.description = description;
    this.location = location;
    this.status = status;
  }

  /**
   * Constructs an EventSeries that repeats for a specific number of occurrences with description.
   *
   * @param subject     the subject of the events
   * @param startDate   the start date of the first event
   * @param startTime   the start time for all events
   * @param endTime     the end time for all events
   * @param repeatDays  the days of the week to repeat on
   * @param occurrences the number of occurrences
   * @param description the description
   */
  public EventSeries(String subject, Date startDate, Time startTime, Time endTime,
                     Set<Day> repeatDays, int occurrences, String description) {
    this(subject, startDate, startTime, endTime, repeatDays, occurrences, description, null, null);
  }

  /**
   * Constructs an EventSeries that repeats for a specific number of occurrences with location.
   *
   * @param subject     the subject of the events
   * @param startDate   the start date of the first event
   * @param startTime   the start time for all events
   * @param endTime     the end time for all events
   * @param repeatDays  the days of the week to repeat on
   * @param occurrences the number of occurrences
   * @param location    the location
   */
  public EventSeries(String subject, Date startDate, Time startTime, Time endTime,
                     Set<Day> repeatDays, int occurrences, Location location) {
    this(subject, startDate, startTime, endTime, repeatDays, occurrences, null, location, null);
  }

  /**
   * Constructs an EventSeries that repeats for a specific number of occurrences with status.
   *
   * @param subject     the subject of the events
   * @param startDate   the start date of the first event
   * @param startTime   the start time for all events
   * @param endTime     the end time for all events
   * @param repeatDays  the days of the week to repeat on
   * @param occurrences the number of occurrences
   * @param status      the status
   */
  public EventSeries(String subject, Date startDate, Time startTime, Time endTime,
                     Set<Day> repeatDays, int occurrences, Status status) {
    this(subject, startDate, startTime, endTime, repeatDays, occurrences, null, null, status);
  }

  /**
   * Constructs an EventSeries that repeats for a specific number of occurrences
   * (no optional fields).
   *
   * @param subject     the subject of the events
   * @param startDate   the start date of the first event
   * @param startTime   the start time for all events
   * @param endTime     the end time for all events
   * @param repeatDays  the days of the week to repeat on
   * @param occurrences the number of occurrences
   */
  public EventSeries(String subject, Date startDate, Time startTime, Time endTime,
                     Set<Day> repeatDays, int occurrences) {
    this(subject, startDate, startTime, endTime, repeatDays, occurrences, null, null, null);
  }

  /**
   * Constructs an EventSeries that repeats until a specific date.
   *
   * @param subject     the subject of the events
   * @param startDate   the start date of the first event
   * @param startTime   the start time for all events
   * @param endTime     the end time for all events
   * @param repeatDays  the days of the week to repeat on
   * @param endDate     the end date (inclusive)
   * @param description the description (can be null)
   * @param location    the location (can be null)
   * @param status      the status (can be null)
   */
  public EventSeries(String subject, Date startDate, Time startTime, Time endTime,
                     Set<Day> repeatDays, Date endDate, String description,
                     Location location, Status status) {
    this.seriesId = UUID.randomUUID().toString();
    this.subject = subject;
    this.startDate = startDate;
    this.startTime = startTime;
    this.endTime = endTime;
    this.repeatDays = new HashSet<>(repeatDays);
    this.occurrences = null;
    this.endDate = endDate;
    this.description = description;
    this.location = location;
    this.status = status;
  }

  /**
   * Constructs an EventSeries that repeats until a specific date with description.
   *
   * @param subject     the subject of the events
   * @param startDate   the start date of the first event
   * @param startTime   the start time for all events
   * @param endTime     the end time for all events
   * @param repeatDays  the days of the week to repeat on
   * @param endDate     the end date (inclusive)
   * @param description the description
   */
  public EventSeries(String subject, Date startDate, Time startTime, Time endTime,
                     Set<Day> repeatDays, Date endDate, String description) {
    this(subject, startDate, startTime, endTime, repeatDays, endDate, description, null, null);
  }

  /**
   * Constructs an EventSeries that repeats until a specific date with location.
   *
   * @param subject    the subject of the events
   * @param startDate  the start date of the first event
   * @param startTime  the start time for all events
   * @param endTime    the end time for all events
   * @param repeatDays the days of the week to repeat on
   * @param endDate    the end date (inclusive)
   * @param location   the location
   */
  public EventSeries(String subject, Date startDate, Time startTime, Time endTime,
                     Set<Day> repeatDays, Date endDate, Location location) {
    this(subject, startDate, startTime, endTime, repeatDays, endDate, null, location, null);
  }

  /**
   * Constructs an EventSeries that repeats until a specific date with status.
   *
   * @param subject    the subject of the events
   * @param startDate  the start date of the first event
   * @param startTime  the start time for all events
   * @param endTime    the end time for all events
   * @param repeatDays the days of the week to repeat on
   * @param endDate    the end date (inclusive)
   * @param status     the status
   */
  public EventSeries(String subject, Date startDate, Time startTime, Time endTime,
                     Set<Day> repeatDays, Date endDate, Status status) {
    this(subject, startDate, startTime, endTime, repeatDays, endDate, null, null, status);
  }

  /**
   * Constructs an EventSeries that repeats until a specific date (no optional fields).
   *
   * @param subject    the subject of the events
   * @param startDate  the start date of the first event
   * @param startTime  the start time for all events
   * @param endTime    the end time for all events
   * @param repeatDays the days of the week to repeat on
   * @param endDate    the end date (inclusive)
   */
  public EventSeries(String subject, Date startDate, Time startTime, Time endTime,
                     Set<Day> repeatDays, Date endDate) {
    this(subject, startDate, startTime, endTime, repeatDays, endDate, null, null, null);
  }

  /**
   * Gets the unique series ID.
   *
   * @return the series ID
   */
  public String getSeriesId() {
    return seriesId;
  }

  /**
   * Gets the subject.
   *
   * @return the subject
   */
  public String getSubject() {
    return subject;
  }

  /**
   * Gets the start time.
   *
   * @return the start time
   */
  public Time getStartTime() {
    return startTime;
  }

  /**
   * Gets the end time.
   *
   * @return the end time
   */
  public Time getEndTime() {
    return endTime;
  }

  /**
   * Gets the days of the week this series repeats on.
   *
   * @return the repeat days
   */
  public Set<Day> getRepeatDays() {
    return new HashSet<>(repeatDays);
  }

  /**
   * Gets the start date.
   *
   * @return the start date
   */
  public Date getStartDate() {
    return startDate;
  }

  /**
   * Gets the end date (if specified).
   *
   * @return the end date, or null if using occurrences
   */
  public Date getEndDate() {
    return endDate;
  }

  /**
   * Gets the number of occurrences (if specified).
   *
   * @return the number of occurrences, or null if using end date
   */
  public Integer getOccurrences() {
    return occurrences;
  }

  /**
   * Gets the description.
   *
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Gets the location.
   *
   * @return the location
   */
  public Location getLocation() {
    return location;
  }

  /**
   * Gets the status.
   *
   * @return the status
   */
  public Status getStatus() {
    return status;
  }

  /**
   * Generates all events in this series.
   *
   * @return a list of all events in the series
   */
  public List<Event> generateEvents() {
    List<Event> events = new ArrayList<>();
    LocalDate current = LocalDate.of(startDate.getYear(), startDate.getMonth(), startDate.getDay());
    int count = 0;

    LocalDate endLocalDate = null;
    if (endDate != null) {
      endLocalDate = LocalDate.of(endDate.getYear(), endDate.getMonth(), endDate.getDay());
    } else if (occurrences == null) {
      int futureYear = startDate.getYear() + 10;
      endLocalDate = LocalDate.of(futureYear, startDate.getMonth(), startDate.getDay());
    }

    while (true) {
      // Check if we should stop
      if (occurrences != null && count >= occurrences) {
        break;
      }
      if (endLocalDate != null && current.isAfter(endLocalDate)) {
        break;
      }

      // Check if current day matches one of the repeat days
      if (dayMatches(current)) {
        Date eventDate = new Date(current.getYear(), current.getMonthValue(),
            current.getDayOfMonth());
        Event event = new Event(subject, eventDate, startTime, eventDate, endTime,
            description, location, status);
        events.add(event);
        count++;
      }

      current = current.plusDays(1);

      // Safety check to prevent infinite loops
      if (count > 10000) {
        throw new IllegalStateException("Too many events generated");
      }
    }

    return events;
  }

  /**
   * Checks if a given LocalDate matches one of the repeat days.
   *
   * @param date the date to check
   * @return true if the date's day of week is in repeatDays, false otherwise
   */
  private boolean dayMatches(LocalDate date) {
    Day dayOfWeek = Day.valueOf(date.getDayOfWeek().name());
    return repeatDays.contains(dayOfWeek);
  }

  /**
   * Creates a new EventSeries with the specified subject.
   *
   * @param newSubject the new subject
   * @return a new EventSeries with the updated subject
   */
  public EventSeries withSubject(String newSubject) {
    if (occurrences != null) {
      return new EventSeries(newSubject, startDate, startTime, endTime, repeatDays,
          occurrences, description, location, status);
    } else {
      return new EventSeries(newSubject, startDate, startTime, endTime, repeatDays,
          endDate, description, location, status);
    }
  }

  /**
   * Creates a new EventSeries with the specified start time.
   *
   * @param newStartTime the new start time
   * @return a new EventSeries with the updated start time
   */
  public EventSeries withStartTime(Time newStartTime) {
    if (occurrences != null) {
      return new EventSeries(subject, startDate, newStartTime, endTime, repeatDays,
          occurrences, description, location, status);
    } else {
      return new EventSeries(subject, startDate, newStartTime, endTime, repeatDays,
          endDate, description, location, status);
    }
  }

  /**
   * Creates a new EventSeries with the specified end time.
   *
   * @param newEndTime the new end time
   * @return a new EventSeries with the updated end time
   */
  public EventSeries withEndTime(Time newEndTime) {
    if (occurrences != null) {
      return new EventSeries(subject, startDate, startTime, newEndTime, repeatDays,
          occurrences, description, location, status);
    } else {
      return new EventSeries(subject, startDate, startTime, newEndTime, repeatDays,
          endDate, description, location, status);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EventSeries that = (EventSeries) o;
    return Objects.equals(seriesId, that.seriesId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(seriesId);
  }
}

