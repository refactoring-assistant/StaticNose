package calendar.util;

import calendar.model.EventStatus;
import calendar.model.InEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.function.Predicate;

/**
 * Factory for common event predicates.
 * Provides reusable predicates for filtering events based on various criteria.
 */
public class EventPredicates {

  /**
   * Creates a predicate that matches events with an exact subject.
   *
   * @param subject the exact subject to match
   * @return predicate that returns true if event subject equals the given subject
   */
  public static Predicate<InEvent> subjectEquals(String subject) {
    return event -> event.getSubject().equals(subject);
  }

  /**
   * Creates a predicate that matches events containing a keyword in their subject.
   * Case-insensitive matching.
   *
   * @param keyword the keyword to search for in the subject
   * @return predicate that returns true if event subject contains the keyword
   */
  public static Predicate<InEvent> subjectContains(String keyword) {
    return event ->
        event.getSubject().toLowerCase().contains(keyword.toLowerCase());
  }

  /**
   * Creates a predicate that matches events occurring on a specific date.
   *
   * @param date the date to check
   * @return predicate that returns true if event occurs on the given date
   */
  public static Predicate<InEvent> occursOnDate(LocalDate date) {
    return event -> event.occursOn(date);
  }

  /**
   * Creates a predicate that matches events occurring within a date/time range.
   *
   * @param start the start of the range (inclusive)
   * @param end   the end of the range (inclusive)
   * @return predicate that returns true if event occurs between start and end
   */
  public static Predicate<InEvent> occursBetween(
      LocalDateTime start, LocalDateTime end) {
    return event -> event.occursBetween(start, end);
  }

  /**
   * Creates a predicate that matches events at a specific location.
   *
   * @param location the location to match
   * @return predicate that returns true if event has the given location
   */
  public static Predicate<InEvent> hasLocation(String location) {
    return event ->
        event.getLocation().map(loc -> loc.equals(location)).orElse(false);
  }

  /**
   * Creates a predicate that matches events with a specific privacy status.
   *
   * @param status the status to match (PUBLIC or PRIVATE)
   * @return predicate that returns true if event has the given status
   */
  public static Predicate<InEvent> hasStatus(EventStatus status) {
    return event -> event.getStatus() == status;
  }

  /**
   * Creates a predicate that matches all-day events.
   *
   * @return predicate that returns true if event is an all-day event
   */
  public static Predicate<InEvent> isAllDay() {
    return InEvent::isAllDayEvent;
  }

  /**
   * Creates a predicate that matches events starting at an exact time.
   *
   * @param startTime the exact start time to match
   * @return predicate that returns true if event starts at the given time
   */
  public static Predicate<InEvent> startsAt(LocalDateTime startTime) {
    return event -> event.getStartDateTime().equals(startTime);
  }

  /**
   * Creates a predicate that matches events starting after a given time.
   *
   * @param startTime the time threshold (exclusive)
   * @return predicate that returns true if event starts after the given time
   */
  public static Predicate<InEvent> startsAfter(LocalDateTime startTime) {
    return event -> event.getStartDateTime().isAfter(startTime);
  }

  /**
   * Creates a predicate that matches events starting before a given time.
   *
   * @param startTime the time threshold (exclusive)
   * @return predicate that returns true if event starts before the given time
   */
  public static Predicate<InEvent> startsBefore(LocalDateTime startTime) {
    return event -> event.getStartDateTime().isBefore(startTime);
  }
}