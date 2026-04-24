package calendar.model.event;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a recurring event series.
 * Creates and manages multiple calendar events that repeat on specific days
 * of the week for a given number of occurrences or until a specific date.
 *
 * <p>All events in a series share the same series UID and must be single-day events
 * (start and end on the same day).
 */
public class SeriesEvent extends AbstractCalendarEvent {

  /**
   * Set of days of the week on which events in this series occur.
   */
  private final HashSet<DayOfWeek> daysOfWeek;

  /**
   * Number of occurrences (0 if using untilDate instead).
   */
  private final int repeatCount;

  /**
   * Date until which events repeat (null if using repeatCount instead).
   */
  private final LocalDate repeatUntil;

  /**
   * Linked list maintaining the chronological order of events in this series.
   */
  private final SeriesLinkedList eventsList;

  /**
   * Constructs a new event series and generates all recurring events.
   *
   * @param parameters map containing: "subject", "start", "end", "weekdays",
   *                   and either "ndays" or "untildate"
   * @throws IllegalArgumentException if events would span multiple days or parameters are invalid
   */
  public SeriesEvent(Map<String, Object> parameters) {
    super(validateAndPrepare(parameters), UUID.randomUUID().toString());
    String weekdays = parameters.get("weekdays").toString().toUpperCase();
    this.daysOfWeek = Weekday.parseDaysOfWeek(weekdays);
    if (parameters.containsKey("ndays")) {
      this.repeatCount = Integer.parseInt(parameters.get("ndays").toString());
      this.repeatUntil = null;
    } else {
      this.repeatCount = 0;
      this.repeatUntil = (LocalDate) parameters.get("untildate");
    }
    this.eventsList = new SeriesLinkedList(this.seriesUid);
    this.eventsList.addAll(createEventsInSeries());
  }

  /**
   * Gets the set of days on which events in this series occur.
   *
   * @return set of DayOfWeek values
   */
  public HashSet<DayOfWeek> getDaysOfWeek() {
    return daysOfWeek;
  }

  /**
   * Gets the number of occurrences in this series.
   *
   * @return the repeat count, or 0 if using untilDate instead
   */
  public int getRepeatCount() {
    return repeatCount;
  }

  /**
   * Gets the date until which events repeat.
   *
   * @return the until date, or null if using repeat count instead
   */
  public LocalDate getRepeatUntil() {
    return repeatUntil;
  }

  /**
   * Generates all recurring events in this series.
   *
   * @return list of calendar events in chronological order
   */
  public List<CalendarEvent> createEventsInSeries() {
    List<CalendarEvent> events = new ArrayList<>();
    ZonedDateTime eventStart = startDateTime;
    ZonedDateTime eventEnd = endDateTime;
    int count = 0;

    while (shouldContinue(eventStart.toLocalDate(), count)) {
      DayOfWeek eventDay = eventStart.getDayOfWeek();
      if (daysOfWeek.contains(eventDay)) {
        Map<String, Object> params = new HashMap<>();
        params.put("subject", subject);
        params.put("start", eventStart);
        params.put("description", description);
        params.put("end", eventEnd);
        params.put("location", location);
        params.put("status", status);
        events.add(new SingleEvent(params, this.seriesUid));
        count++;
      }
      eventStart = eventStart.plusDays(1);
      eventEnd = eventEnd.plusDays(1);
    }

    return events;
  }

  /**
   * Gets all events in this series.
   *
   * @return list of all calendar events in the series
   */
  public List<CalendarEvent> getEventsInSeries() {
    return this.eventsList.getAllEvents();
  }

  /**
   * Gets the linked list managing events in this series.
   *
   * @return the SeriesLinkedList containing all series events
   */
  public SeriesLinkedList getEventsList() {
    return this.eventsList;
  }

  /**
   * Determines if event generation should continue.
   *
   * @param currentDate the current date being checked
   * @param count       the current occurrence count
   * @return true if more events should be generated
   */
  private boolean shouldContinue(LocalDate currentDate, int count) {
    if (repeatUntil != null) {
      return currentDate.isBefore(repeatUntil) || currentDate.equals(repeatUntil);
    } else {
      return count < repeatCount;
    }
  }

  /**
   * Validates and prepares parameters for series creation.
   *
   * @param parameters the parameter map to validate
   * @return the validated parameter map
   * @throws IllegalArgumentException if validation fails
   */
  private static Map<String, Object> validateAndPrepare(Map<String, Object> parameters) {
    ZonedDateTime start = (ZonedDateTime) parameters.get("start");
    ZonedDateTime end = (ZonedDateTime) parameters.get("end");
    if (!start.toLocalDate().equals(end.toLocalDate())) {
      throw new IllegalArgumentException(
          "Series events cannot span multiple days. "
              + "Start and end must be on the same day."
      );
    }
    validateDaysOfWeek(parameters);
    validateRepeatCount(parameters);
    validateUntilDate(parameters, start);
    return parameters;
  }

  /**
   * Validates the weekdays parameter.
   *
   * @param parameters the parameter map
   * @throws IllegalArgumentException if weekdays is missing or contains invalid codes
   */
  private static void validateDaysOfWeek(Map<String, Object> parameters) {
    if (parameters.containsKey("weekdays")
        && !parameters.get("weekdays").toString().trim().isEmpty()) {
      Weekday.parseDaysOfWeek(parameters.get("weekdays").toString());
    } else {
      throw new IllegalArgumentException("Must specify weekdays for series events");
    }
  }

  /**
   * Validates the repeat count parameter if present.
   *
   * @param parameters the parameter map
   * @throws IllegalArgumentException if repeat count is less than 1
   */
  private static void validateRepeatCount(Map<String, Object> parameters) {
    if (parameters.containsKey("ndays")) {
      int count = Integer.parseInt(parameters.get("ndays").toString());
      if (count < 1) {
        throw new IllegalArgumentException(
            "Repeat count must be at least 1. Cannot create series with count: " + count
        );
      }
    }
  }

  /**
   * Validates the until date parameter if present.
   *
   * @param parameters the parameter map
   * @param start      the series start date
   * @throws IllegalArgumentException if until date is before start date
   */
  private static void validateUntilDate(Map<String, Object> parameters, ZonedDateTime start) {
    if (parameters.containsKey("untildate")) {
      LocalDate untilDate = (LocalDate) parameters.get("untildate");
      if (untilDate.isBefore(start.toLocalDate())) {
        throw new IllegalArgumentException(
            "Repeat until date must be after the Start date. "
                + "Until date: " + untilDate + ", Start date: " + start
        );
      }
    }
  }

  @Override
  public boolean isPartOfSeries() {
    return true;
  }
}