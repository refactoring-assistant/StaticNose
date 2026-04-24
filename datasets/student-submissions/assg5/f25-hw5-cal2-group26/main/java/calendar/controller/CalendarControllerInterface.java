package calendar.controller;

import calendar.model.EventInterface;
import calendar.model.RecurringEventInterface;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.function.Predicate;

/**
 * Handles main calendar actions.
 */
public interface CalendarControllerInterface {

  /**
   * Adds a new event.
   */
  void createEvent(EventInterface event);

  /**
   * Adds a recurring event.
   */
  void createRecurringEvent(RecurringEventInterface recurringEvent);

  /**
   * Edits a single event instance by identifying it with subject, start, and end.
   * This edits ONLY that one event, even if it's part of a series.
   */
  void editSingleEvent(String subject, ZonedDateTime start, ZonedDateTime end,
                       String property, Object newValue);

  /**
   * Edits all events in a series starting from the identified event.
   * If not a series, acts like editSingleEvent.
   */
  void editEventsFromHere(String subject, ZonedDateTime start,
                          String property, Object newValue);

  /**
   * Edits all events in a series.
   * If not a series, acts like editSingleEvent.
   */
  void editEntireSeries(String subject, ZonedDateTime start,
                        String property, Object newValue);

  /**
   * Queries events dynamically using a predicate.
   */
  List<EventInterface> queryEvents(Predicate<EventInterface> filter);

  /**
   * Checks if user is busy at a time.
   */
  boolean isUserBusy(ZonedDateTime dateTime);

  /**
   * Deletes an event.
   */
  void deleteEvent(EventInterface event);

  /**
   * Deletes a recurring event.
   */
  void deleteRecurringEvent(RecurringEventInterface recurringEvent);
}