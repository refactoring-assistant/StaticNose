package calendar.model.event;

import calendar.model.datetime.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a series of recurring events.
 * All events in a series share the same subject, duration, and properties,
 * but occur on different dates according to a recurrence pattern.
 */
public class EventSeries {

  private final String subject;
  private final LocalTime startTime;
  private final LocalTime endTime;
  private final Set<DayOfWeek> weekdays;
  private final LocalDate firstStartDate;
  private final String description;
  private final String location;
  private final EventStatus status;
  private final List<Ievent> events;

  /**
   * Enum for types of modifications to a series.
   */
  public enum ModificationType {
    SINGLE,
    FROM_THIS,
    ALL
  }

  /**
   * Constructs an event series with the specified parameters.
   *
   * @param subject     the subject for all events
   * @param firstStart  the start date/time of the first occurrence
   * @param firstEnd    the end date/time of the first occurrence
   * @param weekdays    the days of week when events occur
   * @param description optional description
   * @param location    optional location
   * @param isPublic    true for public events
   * @throws IllegalArgumentException if parameters are invalid
   */
  public EventSeries(String subject, LocalDateTime firstStart, LocalDateTime firstEnd,
                     Set<DayOfWeek> weekdays, String description, String location,
                     boolean isPublic) {
    if (subject == null || subject.trim().isEmpty()) {
      throw new IllegalArgumentException("Subject cannot be null or empty");
    }
    if (firstStart == null || firstEnd == null) {
      throw new IllegalArgumentException("Start and end times cannot be null");
    }
    if (!firstStart.toLocalDate().equals(firstEnd.toLocalDate())) {
      throw new IllegalArgumentException("Events in a series must start and end on the same day");
    }
    if (weekdays == null || weekdays.isEmpty()) {
      throw new IllegalArgumentException("Weekdays cannot be null or empty");
    }

    this.subject = subject.trim();
    this.startTime = firstStart.toLocalTime();
    this.endTime = firstEnd.toLocalTime();
    this.firstStartDate = firstStart.toLocalDate();
    this.weekdays = EnumSet.copyOf(weekdays);
    this.description = description;
    this.location = location;
    this.status = isPublic ? EventStatus.PUBLIC : EventStatus.PRIVATE;
    this.events = new ArrayList<>();
  }

  /**
   * Generates events for a specific number of occurrences.
   *
   * @param occurrences the number of events to generate
   */
  public void generateOccurrences(int occurrences) {
    if (occurrences <= 0) {
      throw new IllegalArgumentException("Occurrences must be positive");
    }

    events.clear();
    LocalDate currentDate = firstStartDate;

    int count = 0;
    while (count < occurrences) {
      DayOfWeek dayOfWeek = DayOfWeek.fromDate(currentDate);

      if (weekdays.contains(dayOfWeek)) {
        LocalDateTime start = currentDate.atTime(startTime);
        LocalDateTime end = currentDate.atTime(endTime);
        Event event = new Event(subject, start, end, description, location, status);
        events.add(event);
        count++;
      }

      currentDate = currentDate.plusDays(1);
    }
  }

  /**
   * Generates events from a start date for a specific number of occurrences.
   *
   * @param startDate   the date to start generating from
   * @param occurrences the number of events to generate
   */
  public void generateOccurrencesFrom(LocalDate startDate, int occurrences) {
    if (occurrences <= 0) {
      throw new IllegalArgumentException("Occurrences must be positive");
    }
    if (startDate == null) {
      throw new IllegalArgumentException("Start date cannot be null");
    }

    events.clear();
    LocalDate currentDate = startDate;

    int count = 0;
    while (count < occurrences) {
      DayOfWeek dayOfWeek = DayOfWeek.fromDate(currentDate);

      if (weekdays.contains(dayOfWeek)) {
        LocalDateTime start = currentDate.atTime(startTime);
        LocalDateTime end = currentDate.atTime(endTime);
        Event event = new Event(subject, start, end, description, location, status);
        events.add(event);
        count++;
      }

      currentDate = currentDate.plusDays(1);
    }
  }

  /**
   * Generates events until a specific end date (inclusive).
   *
   * @param untilDate the last date to generate events for
   */
  public void generateUntilDate(LocalDate untilDate) {
    if (untilDate == null) {
      throw new IllegalArgumentException("Until date cannot be null");
    }

    events.clear();
    LocalDate currentDate = firstStartDate;

    while (!currentDate.isAfter(untilDate)) {
      DayOfWeek dayOfWeek = DayOfWeek.fromDate(currentDate);

      if (weekdays.contains(dayOfWeek)) {
        LocalDateTime start = currentDate.atTime(startTime);
        LocalDateTime end = currentDate.atTime(endTime);
        Event event = new Event(subject, start, end, description, location, status);
        events.add(event);
      }

      currentDate = currentDate.plusDays(1);
    }
  }

  /**
   * Generates events from a start date until an end date.
   *
   * @param startDate the first date to consider
   * @param untilDate the last date to generate events for
   */
  public void generateBetweenDates(LocalDate startDate, LocalDate untilDate) {
    if (startDate == null || untilDate == null) {
      throw new IllegalArgumentException("Dates cannot be null");
    }
    if (startDate.isAfter(untilDate)) {
      throw new IllegalArgumentException("Start date must be before or equal to until date");
    }

    events.clear();
    LocalDate currentDate = startDate;

    while (!currentDate.isAfter(untilDate)) {
      DayOfWeek dayOfWeek = DayOfWeek.fromDate(currentDate);

      if (weekdays.contains(dayOfWeek)) {
        LocalDateTime start = currentDate.atTime(startTime);
        LocalDateTime end = currentDate.atTime(endTime);
        Event event = new Event(subject, start, end, description, location, status);
        events.add(event);
      }

      currentDate = currentDate.plusDays(1);
    }
  }

  /**
   * Gets all events in this series.
   *
   * @return unmodifiable list of events
   */
  public List<Ievent> getEvents() {
    return Collections.unmodifiableList(events);
  }

  /**
   * Gets events from a specific date forward.
   *
   * @param fromDate the starting date/time
   * @return list of events from that date forward
   */
  public List<Ievent> getEventsFromDate(LocalDateTime fromDate) {
    return events.stream()
        .filter(e -> !e.getStartDateTime().isBefore(fromDate))
        .collect(Collectors.toList());
  }

  /**
   * Checks if this series contains a specific event.
   *
   * @param event the event to check
   * @return true if the event is part of this series
   */
  public boolean containsEvent(Ievent event) {
    if (event == null) {
      return false;
    }

    if (!subject.equals(event.getSubject())) {
      return false;
    }

    LocalTime eventStartTime = event.getStartDateTime().toLocalTime();
    LocalTime eventEndTime = event.getEndDateTime().toLocalTime();

    if (!startTime.equals(eventStartTime) || !endTime.equals(eventEndTime)) {
      return false;
    }

    return events.contains(event);
  }

  /**
   * Removes an event from this series.
   *
   * @param event the event to remove
   * @return true if the event was removed
   */
  public boolean removeEvent(Ievent event) {
    return events.remove(event);
  }

  /**
   * Splits this series at a specific event.
   * Returns a new series containing events from the split point forward,
   * while this series retains events before the split.
   *
   * @param splitPoint the event where the split occurs
   * @return a new EventSeries with events from the split point, or null if not found
   */
  public EventSeries splitAt(Ievent splitPoint) {
    int index = events.indexOf(splitPoint);
    if (index == -1) {
      return null;
    }

    EventSeries newSeries = new EventSeries(subject,
        splitPoint.getStartDateTime(), splitPoint.getEndDateTime(),
        weekdays, description, location, status == EventStatus.PUBLIC);

    List<Ievent> eventsToMove = new ArrayList<>(events.subList(index, events.size()));
    newSeries.events.addAll(eventsToMove);

    events.subList(index, events.size()).clear();

    return newSeries;
  }

  /**
   * Gets the subject of events in this series.
   *
   * @return the subject
   */
  public String getSubject() {
    return subject;
  }

  /**
   * Gets the start time for events in this series.
   *
   * @return the start time
   */
  public LocalTime getStartTime() {
    return startTime;
  }

  /**
   * Gets the end time for events in this series.
   *
   * @return the end time
   */
  public LocalTime getEndTime() {
    return endTime;
  }

  /**
   * Gets the weekdays when events occur.
   *
   * @return set of weekdays
   */
  public Set<DayOfWeek> getWeekdays() {
    return Collections.unmodifiableSet(weekdays);
  }

  /**
   * Gets the description for events in this series.
   *
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Gets the location for events in this series.
   *
   * @return the location
   */
  public String getLocation() {
    return location;
  }

  /**
   * Gets the status of events in this series.
   *
   * @return the event status
   */
  public EventStatus getStatus() {
    return status;
  }

  /**
   * Checks if events in this series are public.
   *
   * @return true if public
   */
  public boolean isPublic() {
    return status == EventStatus.PUBLIC;
  }

  /**
   * Gets the number of events in this series.
   *
   * @return the event count
   */
  public int size() {
    return events.size();
  }

  /**
   * Checks if this series is empty.
   *
   * @return true if no events
   */
  public boolean isEmpty() {
    return events.isEmpty();
  }

  /**
   * Returns a string representation of the event series.
   *
   * @return formatted string with subject, time, days, and event count
   */
  @Override
  public String toString() {
    return String.format("EventSeries[subject=%s, time=%s-%s, days=%s, events=%d]",
        subject, startTime, endTime,
        DayOfWeek.toCodes(weekdays), events.size());
  }
}