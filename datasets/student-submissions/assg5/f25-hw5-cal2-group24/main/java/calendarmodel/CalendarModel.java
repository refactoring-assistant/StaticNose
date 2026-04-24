package calendarmodel;

import calendarmodel.enums.EditMode;
import calendarmodel.exceptions.AmbiguousEditException;
import calendarmodel.exceptions.DuplicateEventException;
import calendarmodel.exceptions.EventNotFoundException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * The interface for the Calendar Model, following the MVC pattern.
 *
 * <p>This contract defines all the business logic and data manipulation
 * operations that the calendar must support.</p>
 *
 * <p>The Model does not know about user input, command parsing, or how to
 * print to the console. It deals only in Java objects (like LocalDateTime)
 * and throws exceptions when rules are violated.</p>
 */
public interface CalendarModel {

  /**
   * Creates and adds a single event to the calendar.
   *
   * <p>The model is responsible for checking for duplicates.</p>
   *
   * @param newEvent The fully constructed Event object to add.
   * @throws DuplicateEventException if an event with the same subject, start,
   *                                 and end time already exists.
   */
  void createSingleEvent(Event newEvent) throws DuplicateEventException;

  /**
   * Creates a series of recurring events and adds them to the calendar.
   *
   * <p>The model will generate all N event occurrences based on the
   * prototype's start date, weekdays, and count.</p>
   *
   * @param prototype      The prototype Event, containing subject, start/end time
   *                       (for duration), description, etc.
   * @param weekdays       The list of DayOfWeek (e.g., MONDAY, WEDNESDAY) for the series.
   * @param numOccurrences The total number of events to create.
   * @throws DuplicateEventException if any of the generated events conflicts
   *                                 with an existing event.
   */
  void createEventSeries(Event prototype, List<DayOfWeek> weekdays, int numOccurrences)
      throws DuplicateEventException;

  /**
   * Creates a series of recurring events and adds them to the calendar.
   *
   * <p>The model will generate all events from the prototype's start date
   * until the end date (inclusive), matching the specified weekdays.</p>
   *
   * @param prototype The prototype Event, containing subject, start/end time
   *                  (for duration), description, etc.
   * @param weekdays  The list of DayOfWeek for the series.
   * @param untilDate The last possible date an event in the series can occur on.
   * @throws DuplicateEventException if any of the generated events conflicts
   *                                 with an existing event.
   */
  void createEventSeries(Event prototype, List<DayOfWeek> weekdays, LocalDate untilDate)
      throws DuplicateEventException;

  /**
   * Edits a single event instance, identified by its subject, start, and end time.
   *
   * <p>This command works on any event, whether it is part of a series or not.
   * If this is used on a series event, it may "split" it from the series.</p>
   *
   * @param findSubject      The subject of the event to find.
   * @param findStartTime    The start time of the event to find.
   * @param findEndTime      The end time of the event to find.
   * @param propertyToChange The name of the field to change (e.g., "subject", "start").
   * @param newValue         The new value (e.g., "New Subject", or a LocalDateTime object).
   * @throws Exception if the event is not found, the edit is ambiguous,
   *                   or the change results in a duplicate.
   */
  void editSingleEvent(String findSubject, LocalDateTime findStartTime, LocalDateTime findEndTime,
                       String propertyToChange, Object newValue)
      throws Exception;

  /**
   * Edits one or more events in a series, identified by subject and start time.
   *
   * <p>This command will fail if the (subject, startTime) combination is ambiguous
   * (i.e., multiple events match) or not part of a series (for ALL/THIS_AND_FUTURE).</p>
   *
   * @param findSubject      The subject of the event to find.
   * @param findStartTime    The start time of the event to find (used as the anchor).
   * @param mode             The scope of the edit (THIS_AND_FUTURE or ALL_IN_SERIES).
   * @param propertyToChange The name of the field to change (e.g., "subject", "start").
   * @param newValue         The new value for the property.
   * @throws Exception if the event is not found, the edit is ambiguous,
   *                   or the change results in a duplicate.
   */
  void editEventSeries(String findSubject, LocalDateTime findStartTime, EditMode mode,
                       String propertyToChange, Object newValue)
      throws Exception;

  /**
   * Finds all events that overlap with a specific date.
   *
   * <p>This includes events that start before the date and end during
   * or after it, and events that start during the date.</p>
   *
   * @param date The date to query.
   * @return A List of Event objects, sorted by start time.
   *         The list will be empty if no events are found.
   */
  List<Event> getEventsOn(LocalDate date);

  /**
   * Finds all events that partially or completely overlap with a given date/time range.
   *
   * <p>The range is [rangeStart, rangeEnd). Events are included if
   * {@code event.startTime < rangeEnd} and {@code event.endTime > rangeStart}.</p>
   *
   * @param rangeStart The start of the query range (inclusive).
   * @param rangeEnd   The end of the query range (exclusive).
   * @return A List of Event objects, sorted by start time.
   *         The list will be empty if no events are found.
   */
  List<Event> getEventsFrom(LocalDateTime rangeStart, LocalDateTime rangeEnd);

  /**
   * Checks if the user has any events scheduled at a specific moment in time.
   *
   * <p>This checks if the given dateTime is {@code >= event.startTime}
   * and {@code < event.endTime} for any event.</p>
   *
   * @param dateTime The exact date and time to check.
   * @return true if an event is scheduled (user is busy), false otherwise.
   */
  boolean isBusy(LocalDateTime dateTime);

  /**
   * Gets a defensive copy of all events in this calendar.
   *
   * <p>The returned list is a copy, so modifying it will not affect the
   * internal state of the model. The {@link Event} objects themselves
   * are immutable.</p>
   *
   * @return A new list containing all events in the calendar, sorted.
   */
  List<Event> getAllEvents();
}