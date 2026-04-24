package calendar.util;

import calendar.model.EventInterface;
import java.time.ZonedDateTime;
import java.util.function.Predicate;

/**
 * Helper class to create simple filters for finding calendar events.
 * All predicates now use ZonedDateTime directly.
 */
public final class EventQueryHelper {

  private EventQueryHelper() {
  }

  /**
   * Predicate: event subject equals given string (case-insensitive).
   */
  public static Predicate<EventInterface> hasSubject(String subject) {
    return e -> e.getSubject().equalsIgnoreCase(subject);
  }

  /**
   * Predicate: event starts after given ZonedDateTime.
   */
  public static Predicate<EventInterface> startsAfter(ZonedDateTime time) {
    return e -> e.getStart().isAfter(time);
  }

  /**
   * Predicate: event starts before given ZonedDateTime.
   */
  public static Predicate<EventInterface> startsBefore(ZonedDateTime time) {
    return e -> e.getStart().isBefore(time);
  }

  /**
   * Predicate: event overlaps with given time interval.
   */
  public static Predicate<EventInterface> overlapsWith(ZonedDateTime start, ZonedDateTime end) {
    return e -> {
      ZonedDateTime eventStart = e.getStart();
      ZonedDateTime eventEnd = e.getEnd() != null ? e.getEnd() : eventStart.plusHours(9);
      return !(eventEnd.isBefore(start) || eventStart.isAfter(end));
    };
  }

  /**
   * Predicate: event is all-day.
   */
  public static Predicate<EventInterface> isAllDay() {
    return EventInterface::isAllDay;
  }

  /**
   * Predicate: event location equals given string (case-insensitive).
   */
  public static Predicate<EventInterface> hasLocation(String location) {
    return e -> e.getLocation() != null && e.getLocation().equalsIgnoreCase(location);
  }

  /**
   * Predicate: event status equals given status.
   */
  public static Predicate<EventInterface> hasStatus(calendar.model.EventStatus status) {
    return e -> e.getStatus() == status;
  }

  /**
   * Combine two predicates with AND.
   */
  public static Predicate<EventInterface> and(Predicate<EventInterface> first,
                                              Predicate<EventInterface> second) {
    return first.and(second);
  }

  /**
   * Combine two predicates with OR.
   */
  public static Predicate<EventInterface> or(Predicate<EventInterface> first,
                                             Predicate<EventInterface> second) {
    return first.or(second);
  }
}
