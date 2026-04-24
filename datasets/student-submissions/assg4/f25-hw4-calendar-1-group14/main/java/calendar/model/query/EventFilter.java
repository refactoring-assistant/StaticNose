package calendar.model.query;

import calendar.model.datetime.DayOfWeek;
import calendar.model.event.Ievent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Utility class for filtering and searching events based on various criteria.
 * Provides methods to apply EventQuery filters to lists of events.
 */
public class EventFilter {

  /**
   * Filters a list of events based on a query.
   *
   * @param events the events to filter
   * @param query  the query parameters
   * @return filtered and sorted list of events
   */
  public static List<Ievent> filter(List<Ievent> events, EventQuery query) {
    if (events == null || events.isEmpty()) {
      return new ArrayList<>();
    }

    if (query == null || !query.hasFilters()) {
      return events.stream()
          .sorted(Comparator.comparing(Ievent::getStartDateTime))
          .collect(Collectors.toList());
    }

    return events.stream()
        .filter(createPredicate(query))
        .sorted(Comparator.comparing(Ievent::getStartDateTime))
        .collect(Collectors.toList());
  }

  /**
   * Creates a predicate for filtering events based on a query.
   *
   * @param query the query parameters
   * @return a predicate that tests events against the query
   */
  private static Predicate<Ievent> createPredicate(EventQuery query) {
    Predicate<Ievent> predicate = event -> true;

    if (query.getSubject() != null) {
      predicate = predicate.and(event ->
          event.getSubject().equalsIgnoreCase(query.getSubject()));
    }

    if (query.getStartDateTime() != null) {
      predicate = predicate.and(event ->
          event.getStartDateTime().equals(query.getStartDateTime()));
    }

    if (query.getEndDateTime() != null) {
      predicate = predicate.and(event ->
          event.getEndDateTime().equals(query.getEndDateTime()));
    }

    if (query.getDate() != null) {
      predicate = predicate.and(event ->
          event.occursOn(query.getDate()));
    }

    if (query.getFromDateTime() != null || query.getToDateTime() != null) {
      predicate = predicate.and(createRangePredicate(
          query.getFromDateTime(), query.getToDateTime()));
    }

    if (query.getLocation() != null) {
      predicate = predicate.and(event -> {
        String eventLocation = event.getLocation();
        return eventLocation != null
            && eventLocation.toLowerCase().contains(query.getLocation().toLowerCase());
      });
    }

    if (query.getIsPublic() != null) {
      predicate = predicate.and(event ->
          event.isPublic() == query.getIsPublic());
    }

    Set<DayOfWeek> weekdays = query.getWeekdays();
    if (weekdays != null) {
      if (!weekdays.isEmpty()) {
        predicate = predicate.and(event -> {
          DayOfWeek eventDay = DayOfWeek.fromJavaDayOfWeek(
              event.getStartDateTime().getDayOfWeek());
          return weekdays.contains(eventDay);
        });
      }
    }

    return predicate;
  }

  /**
   * Creates a predicate for filtering events within a date/time range.
   *
   * @param from the start of the range (inclusive, nullable)
   * @param to   the end of the range (inclusive, nullable)
   * @return a predicate that tests if events overlap with the range
   */
  private static Predicate<Ievent> createRangePredicate(LocalDateTime from, LocalDateTime to) {
    return event -> {
      if (from != null && event.getEndDateTime().isBefore(from)) {
        return false;
      }

      if (to != null && event.getStartDateTime().isAfter(to)) {
        return false;
      }

      return true;
    };
  }

  /**
   * Finds a single event matching exact criteria.
   *
   * @param events        the list of events to search
   * @param subject       the event subject
   * @param startDateTime the start date/time
   * @return the matching event or null if not found
   */
  public static Ievent findExact(List<Ievent> events, String subject,
                                 LocalDateTime startDateTime) {
    if (events == null || subject == null || startDateTime == null) {
      return null;
    }

    return events.stream()
        .filter(e -> e.getSubject().equals(subject)
            && e.getStartDateTime().equals(startDateTime))
        .findFirst()
        .orElse(null);
  }

  /**
   * Finds a single event matching exact criteria including end time.
   *
   * @param events        the list of events to search
   * @param subject       the event subject
   * @param startDateTime the start date/time
   * @param endDateTime   the end date/time
   * @return the matching event or null if not found
   */
  public static Ievent findExact(List<Ievent> events, String subject,
                                 LocalDateTime startDateTime, LocalDateTime endDateTime) {
    if (events == null || subject == null || startDateTime == null || endDateTime == null) {
      return null;
    }

    return events.stream()
        .filter(e -> e.getSubject().equals(subject)
            && e.getStartDateTime().equals(startDateTime)
            && e.getEndDateTime().equals(endDateTime))
        .findFirst()
        .orElse(null);
  }

  /**
   * Checks if any events match a query.
   *
   * @param events the list of events to search
   * @param query  the query parameters
   * @return true if at least one event matches
   */
  public static boolean anyMatch(List<Ievent> events, EventQuery query) {
    if (events == null || events.isEmpty() || query == null) {
      return false;
    }

    return events.stream().anyMatch(createPredicate(query));
  }

  /**
   * Counts events matching a query.
   *
   * @param events the list of events to search
   * @param query  the query parameters
   * @return the number of matching events
   */
  public static long count(List<Ievent> events, EventQuery query) {
    if (events == null || events.isEmpty() || query == null) {
      return 0;
    }

    return events.stream().filter(createPredicate(query)).count();
  }

  /**
   * Groups events by date.
   *
   * @param events the list of events to group
   * @return a list of date-grouped events
   */
  public static List<DateGroupedEvents> groupByDate(List<Ievent> events) {
    if (events == null || events.isEmpty()) {
      return new ArrayList<>();
    }

    return events.stream()
        .collect(Collectors.groupingBy(
            e -> e.getStartDateTime().toLocalDate(),
            Collectors.toList()))
        .entrySet().stream()
        .map(entry -> new DateGroupedEvents(entry.getKey(), entry.getValue()))
        .sorted(Comparator.comparing(DateGroupedEvents::getDate))
        .collect(Collectors.toList());
  }

  /**
   * Class for events grouped by date.
   */
  public static final class DateGroupedEvents {
    private final LocalDate date;
    private final List<Ievent> events;

    /**
     * Constructor for DateGroupedEvents.
     *
     * @param date   to take in.
     * @param events to take in.
     */
    public DateGroupedEvents(LocalDate date, List<Ievent> events) {
      this.date = date;
      if (events != null) {
        this.events = new ArrayList<>(events);
        this.events.sort(Comparator.comparing(Ievent::getStartDateTime));
      } else {
        this.events = null;
      }
    }

    public LocalDate getDate() {
      return date;
    }

    public List<Ievent> getEvents() {
      return events;
    }

    /**
     * Gets the number of events on this date.
     *
     * @return the event count
     */
    public int count() {
      return events == null ? 0 : events.size();
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
        return false;
      }
      DateGroupedEvents that = (DateGroupedEvents) obj;
      return Objects.equals(date, that.date)
          && Objects.equals(events, that.events);
    }

    @Override
    public int hashCode() {
      return Objects.hash(date, events);
    }
  }

  /**
   * Filters events to only public ones.
   *
   * @param events the list of events
   * @return list of public events
   */
  public static List<Ievent> filterPublic(List<Ievent> events) {
    if (events == null) {
      return new ArrayList<>();
    }

    return events.stream()
        .filter(Ievent::isPublic)
        .sorted(Comparator.comparing(Ievent::getStartDateTime))
        .collect(Collectors.toList());
  }

  /**
   * Filters events to only private ones.
   *
   * @param events the list of events
   * @return list of private events
   */
  public static List<Ievent> filterPrivate(List<Ievent> events) {
    if (events == null) {
      return new ArrayList<>();
    }

    return events.stream()
        .filter(e -> !e.isPublic())
        .sorted(Comparator.comparing(Ievent::getStartDateTime))
        .collect(Collectors.toList());
  }

  /**
   * Finds events that conflict with a given event.
   * Events conflict if they have the same subject, start, and end times.
   *
   * @param events the list of events to check
   * @param event  the event to check against
   * @return list of conflicting events
   */
  public static List<Ievent> findConflicts(List<Ievent> events, Ievent event) {
    if (events == null || event == null) {
      return new ArrayList<>();
    }

    return events.stream()
        .filter(e -> e.conflictsWith(event))
        .collect(Collectors.toList());
  }

  /**
   * Finds events that overlap in time with a given event.
   *
   * @param events the list of events to check
   * @param event  the event to check against
   * @return list of overlapping events
   */
  public static List<Ievent> findOverlapping(List<Ievent> events, Ievent event) {
    if (events == null || event == null) {
      return new ArrayList<>();
    }

    return events.stream()
        .filter(e -> e.overlapsWith(event.getStartDateTime(), event.getEndDateTime()))
        .sorted(Comparator.comparing(Ievent::getStartDateTime))
        .collect(Collectors.toList());
  }
}