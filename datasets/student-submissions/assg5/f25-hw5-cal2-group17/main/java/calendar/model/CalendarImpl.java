package calendar.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a calendar that manages events.
 * Each calendar has a unique name and associated timezone.
 * Ensures no duplicate events (same subject, start, and end time).
 * Supports single events and event series.
 */
public class CalendarImpl implements Calendar {
  private String name;
  private ZoneId timezone;
  private final List<Event> events;
  private int seriesCounter;  // for generating unique series IDs

  /**
   * Constructor for Calendar Implementation with name and timezone.
   *
   * @param name     Calendar name (must not be null or empty)
   * @param timezone Calendar timezone (must not be null)
   * @throws IllegalArgumentException if name or timezone is invalid
   */
  public CalendarImpl(String name, ZoneId timezone) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be null or empty");
    }
    if (timezone == null) {
      throw new IllegalArgumentException("Timezone cannot be null");
    }
    this.name = name;
    this.timezone = timezone;
    this.events = new ArrayList<>();
    this.seriesCounter = 0;
  }

  /**
   * Default constructor for backward compatibility.
   * Creates calendar with default name and EST timezone.
   */
  public CalendarImpl() {
    this("My Calendar", ZoneId.of("America/New_York"));
  }

  // ========== NEW METHODS FOR ASSIGNMENT 2 ==========

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be null or empty");
    }
    this.name = name;
  }

  @Override
  public ZoneId getTimezone() {
    return timezone;
  }

  @Override
  public void setTimezone(ZoneId timezone) {
    if (timezone == null) {
      throw new IllegalArgumentException("Timezone cannot be null");
    }
    this.timezone = timezone;
  }

  // ========== EXISTING METHODS (unchanged) ==========

  /**
   * Adds a single event to the calendar.
   *
   * @throws IllegalArgumentException if event already exists
   */
  @Override
  public void addEvent(Event event) {
    if (eventExists(event)) {
      throw new IllegalArgumentException(
          "Event with same subject, start time, and end time already exists");
    }
    events.add(event);
  }

  /**
   * Creates and adds an event series that repeats on specific weekdays.
   *
   * @param subject       Event subject
   * @param startDateTime Start date and time of first occurrence
   * @param endDateTime   End date and time (null for all-day)
   * @param weekdays      Set of days to repeat on (MONDAY, TUESDAY, etc.)
   * @param occurrences   Number of occurrences (if greater than 0), or -1 to use untilDate
   * @param untilDate     Repeat until this date (inclusive), or null to use occurrences
   * @throws IllegalArgumentException if series would create duplicates
   */
  @Override
  public void addEventSeries(String subject, LocalDateTime startDateTime,
                             LocalDateTime endDateTime, Set<DayOfWeek> weekdays,
                             int occurrences, LocalDate untilDate) {

    // Validate that single event in series spans only one day
    if (endDateTime != null
        && !startDateTime.toLocalDate().equals(endDateTime.toLocalDate())) {
      throw new IllegalArgumentException(
          "Events in a series must start and end on the same day");
    }

    String seriesId = "series-" + (++seriesCounter);
    List<Event> seriesEvents = new ArrayList<>();

    LocalDate currentDate = startDateTime.toLocalDate();
    int count = 0;

    // Generate events based on occurrences or until date
    while (true) {
      // Check if we should stop
      if (occurrences > 0 && count >= occurrences) {
        break;
      }
      if (untilDate != null && currentDate.isAfter(untilDate)) {
        break;
      }

      // Check if current day is in the weekdays set
      if (weekdays.contains(currentDate.getDayOfWeek())) {
        LocalDateTime eventStart = LocalDateTime.of(currentDate, startDateTime.toLocalTime());
        LocalDateTime eventEnd = null;
        if (endDateTime != null) {
          eventEnd = LocalDateTime.of(currentDate, endDateTime.toLocalTime());
        }

        Event event = new EventImpl(subject, eventStart, eventEnd);
        event.setSeriesId(seriesId);

        // Check for duplicates
        if (eventExists(event)) {
          throw new IllegalArgumentException(
              "Event series would create duplicate event on " + currentDate);
        }

        seriesEvents.add(event);
        count++;
      }

      currentDate = currentDate.plusDays(1);

      // Safety check to prevent infinite loops
      if (untilDate == null && occurrences <= 0) {
        throw new IllegalArgumentException(
            "Must specify either occurrences or until date");
      }
      if (currentDate.isAfter(startDateTime.toLocalDate().plusYears(10))) {
        throw new IllegalArgumentException(
            "Event series extends too far into the future");
      }
    }

    events.addAll(seriesEvents);
  }

  /**
   * Checks if an event with the same subject, start, and end time exists.
   */
  private boolean eventExists(Event event) {
    return events.stream().anyMatch(e -> e.equals(event));
  }

  /**
   * Finds events matching the given criteria.
   *
   * @param subject       Event subject to match
   * @param startDateTime Start date/time to match
   * @return List of matching events
   */
  @Override
  public List<Event> findEvents(String subject, LocalDateTime startDateTime) {
    return events.stream()
        .filter(e -> e.getSubject().equals(subject)
            && e.getStartDateTime().equals(startDateTime))
        .collect(Collectors.toList());
  }

  /**
   * Gets all events on a specific date.
   */
  @Override
  public List<Event> getEventsOnDate(LocalDate date) {
    return events.stream()
        .filter(e -> e.occursOnDate(date))
        .sorted(Comparator.comparing(Event::getStartDateTime))
        .collect(Collectors.toList());
  }

  /**
   * Gets all events within a date/time range.
   */
  @Override
  public List<Event> getEventsInRange(LocalDateTime startRange, LocalDateTime endRange) {
    return events.stream()
        .filter(e -> e.overlapsWithRange(startRange, endRange))
        .sorted(Comparator.comparing(Event::getStartDateTime))
        .collect(Collectors.toList());
  }

  /**
   * Checks if user is busy at a specific date/time.
   */
  @Override
  public boolean isBusyAt(LocalDateTime dateTime) {
    return events.stream().anyMatch(e -> e.isScheduledAt(dateTime));
  }

  /**
   * Gets all events in the calendar.
   */
  @Override
  public List<Event> getAllEvents() {
    return new ArrayList<>(events);
  }

  /**
   * Removes an event from the calendar.
   */
  @Override
  public void removeEvent(Event event) {
    events.remove(event);
  }

  /**
   * Gets all events in the same series as the given event.
   */
  @Override
  public List<Event> getEventsInSeries(Event event) {
    if (event.getSeriesId() == null) {
      return Collections.singletonList(event);
    }

    return events.stream()
        .filter(e -> event.getSeriesId().equals(e.getSeriesId()))
        .sorted(Comparator.comparing(Event::getStartDateTime))
        .collect(Collectors.toList());
  }

  /**
   * Gets events in the same series starting from a specific event (inclusive).
   */
  @Override
  public List<Event> getEventsInSeriesFrom(Event event) {
    if (event.getSeriesId() == null) {
      return Collections.singletonList(event);
    }

    return events.stream()
        .filter(e -> event.getSeriesId().equals(e.getSeriesId())
            && !e.getStartDateTime().isBefore(event.getStartDateTime()))
        .sorted(Comparator.comparing(Event::getStartDateTime))
        .collect(Collectors.toList());
  }
}