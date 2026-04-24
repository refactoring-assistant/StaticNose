package calendar.controller.filter;

import calendar.model.datatypes.EventStatus;
import calendar.model.datatypes.Location;
import calendar.model.datatypes.TypeOfEvent;
import calendar.model.interfaces.EventReadOnly;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Predicate class used for filtering Events in a calendar.
 */

public class EventPredicates {

  /**
   * Predicate that matches events with the given subject.
   *
   * @param subject the subject to filter
   * @return a predicate that returns true for events with specified subject.
   */

  public static Predicate<EventReadOnly> bySubject(String subject) {
    return event -> event.getSubject().equals(subject);
  }


  /**
   * Predicate that matches events with the given start date.
   *
   * @param start the subject to filter
   * @return a predicate that returns true for events with specified start date.
   */

  public static Predicate<EventReadOnly> byStartDate(LocalDateTime start) {
    return event -> event.getStartDateTime().equals(start);
  }

  /**
   * Predicate that matches events with the given end date.
   *
   * @param end the subject to filter
   * @return a predicate that returns true for events with specified end date.
   */
  public static Predicate<EventReadOnly> byEndDate(LocalDateTime end) {
    return event -> event.getEndDateTime().equals(end);
  }


  /**
   * Predicate that matches events with the given description.
   *
   * @param description the subject to filter
   * @return a predicate that returns true for events with specified description.
   */
  public static Predicate<EventReadOnly> byDescription(String description) {
    return event -> event.getDescription().equals(description);
  }

  /**
   * Predicate that matches events with the given location.
   *
   * @param location the subject to filter
   * @return a predicate that returns true for events with specified location.
   */
  public static Predicate<EventReadOnly> byLocation(Location location) {
    return event -> event.getLocation() == location;
  }

  /**
   * Predicate that matches events with the given status.
   *
   * @param status the subject to filter
   * @return a predicate that returns true for events with specified status.
   */
  public static Predicate<EventReadOnly> byStatus(EventStatus status) {
    return event -> event.getEventStatus() == status;
  }

  /**
   * Predicate that matches events of a given type.
   *
   * @param eventType the event type to match SINGLE, SERIES
   * @return a predicate that returns true for events with the specified type
   */
  public static Predicate<EventReadOnly> byEventType(TypeOfEvent eventType) {
    return event -> event.getEventType() == eventType;
  }

  /**
   * Predicate that matches an event by its unique identifier.
   *
   * @param eventId the UUID of the event to match
   * @return a predicate that returns true for the event with the specified ID
   */
  public static Predicate<EventReadOnly> byEventId(UUID eventId) {
    return event -> Objects.equals(event.getId().toString(), eventId.toString());
  }

  /**
   * Predicate that matches all-day or non-all-day events.
   *
   * @param allDay true to match all-day events, false otherwise
   * @return a predicate that returns true for events matching the all-day flag
   */
  public static Predicate<EventReadOnly> byAllDay(boolean allDay) {
    return event -> event.isAllDay() == allDay;
  }

  /**
   * Predicate that matches events that belong to a series,
   * identified by same start time, end time, and subject of the first event in the series.
   *
   * @param firstStart   the start time of the first event in the series
   * @param firstEnd     the end time of the first event in the series
   * @param firstSubject the subject of the first event in the series
   * @return a predicate that returns true for events that belong to the specified series
   */

  public static Predicate<EventReadOnly> bySeries(LocalDateTime firstStart, LocalDateTime firstEnd,
                                                  String firstSubject) {
    return event -> event.getStartDateTime().equals(firstStart)
        && event.getSubject().equals(firstSubject)
        && event.getEndDateTime().equals(firstEnd);
  }

  /**
   * Predicate that matches events occurring between the given start and end times.
   *
   * @param start the start of the time range
   * @param end   the end of the time range
   * @return a predicate that returns true for events starting between the specified times.
   */
  public static Predicate<EventReadOnly> betweenStartAndEnd(LocalDateTime start,
                                                            LocalDateTime end) {
    return event ->
        !event.getEndDateTime().isBefore(start) && !event.getStartDateTime().isAfter(end);
  }
}
