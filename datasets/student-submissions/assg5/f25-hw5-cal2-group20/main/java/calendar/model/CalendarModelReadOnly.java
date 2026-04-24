package calendar.model;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Predicate;

/**
 * This interface is a readonly interface that contains methods that do not mutate objects.
 */
public interface CalendarModelReadOnly {
  /**
   * Gets the list of all events.
   *
   * @return list of all events.
   */
  List<EventObject> getAllEvents();

  /**
   * Returns a filtered list of events based on the filtering predicate.
   *
   * @param predicate predicate for filter.
   * @return filtered list of events.
   */
  List<EventObject> filter(Predicate<EventObject> predicate);


  /**
   * Gets the list of events between the given interval of datetimes. Needed for overlapping events.
   *
   * @param startDateTime the start datetime of interval.
   * @param endDateTime the end datetime of interval.
   * @return the list of events falling in the interval.
   */
  List<EventObject> getEventsBetween(String startDateTime, String endDateTime);

  /**
   * Gets the string representation of list of events prepared for bullet list.
   *
   * @param events the events.
   * @return the string of the list of events for bullet list.
   */
  String eventsForList(List<EventObject> events);

  /**
   * Shows the user status on the datetime provided in string format.
   *
   * @param datetimeString the datetime of the day to check status for.
   * @return Busy or Available on that datetime in String.
   */
  String getUserStatus(String datetimeString);

  /**
   * Returns an event(s) with a specific name and start date and returns all that fit.
   *
   * @param subject the name of the event
   * @param startDateTime the start date/time of the event
   * @return List of all event objects that fit these parameters
   */
  List<EventObject> getEvent(String subject, String startDateTime);

  /**
   * Returns all events that take place on a specific start date.
   *
   * @param startDate the day to get events from
   * @return List of all event objects that fit these parameters
   */
  List<EventObject> getDayEvents(LocalDate startDate);

  /**
   * Returns all events that take place in a range of dates.
   *
   * @param startDate the first day to get events from
   * @param endDate the last day to get events from
   * @return List of all event objects that fit these parameters
   */
  List<EventObject> getDayEventsInterval(LocalDate startDate, LocalDate endDate);

  /**
   * Returns the event series ID of the model.
   *
   * @return int of current eventSeriesID
   */
  int getEventSeriesId();
}
