package calendar.model.interfaces;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Represents the read-only view of the calendar model.
 * Provides operations to check the calendar status, print events,
 * and retrieve events within a given date range.
 */
public interface CalendarReadOnly {

  /**
   * Checks whether the status in calendar is busy at the given date and time.
   *
   * @param dateTime the date and time to check
   * @return true if busy at the given time else false.
   */

  boolean isBusy(LocalDateTime dateTime);

  /**
   * Executes the provided consumer for every event in the calendar.
   * Used mainly for filtering and passing each event to the filter predicate.
   *
   * @param consumer the consumer that processes each event.
   */
  void forEachEvent(Consumer<EventReadOnly> consumer);

  /**
   * Retrieves all events that occur between the given start and end times.
   *
   * @param startDateTime the start date and time of the range.
   * @param endDateTime   the end date and time of the range.
   * @return a list of events occurring in the given range, empty if no events.
   */
  List<EventReadOnly> getEvents(LocalDateTime startDateTime, LocalDateTime endDateTime);


  /**
   * Returns all the events of a calendar.
   *
   * @return map of all events in calendar where key of map is date and value is list of events.
   *
   */
  Map<LocalDate, List<EventReadOnly>> getAllEvents();
}
