package calendar;

import java.time.LocalDateTime;
import java.util.List;

/**
 * A Calendar Interface that has methods to create events, event series,
 * edit events, query events and export the calendar in CSV format.
 */
public interface CalendarModel {

  /**
   * Method to create a single event.
   */
  public void createSingleEvent(String subject, LocalDateTime startDateTime,
                                LocalDateTime endDateTime);

  /**
   * Method to create to series of events.
   */
  public void createEventSeries(String subject, LocalDateTime startDateTime,
                                LocalDateTime endDateTime, String weekdays, int occurrences);

  /**
   * Method to edit a single event.
   */
  public void editEvent(Event target, String property, String value);

  /**
   * Identify the event(s) that has the given subject and
   * starts at the given date and time and edit its property.
   * If this event is part of a series then the properties of all events in that series that
   * start at or after the given date and time should be changed.
   *
   *
   * @param target the event for which we want to change the property.
   * @param property the property whose value need to be updated.
   * @param value the updated value of the property.
   */
  public void editSeriesFrom(Event target, String property, String value);

  /**
   * Identify the event that has the given subject and
   * starts at the given date and time and edit its property.
   * If this event is part of a series then the properties of all events
   * in that series should be changed.
   *
   * @param target the event for which we want to change the property.
   * @param property the property whose value need to be updated.
   * @param value the updated value of the property.
   */
  public void editEntireSeries(Event target, String property, String value);

  /**
   * Method to get the event on a specific date.
   *
   * @param date the date of the event to be retrieved.
   *
   * @return the event at the specified date.
   */
  List<Event> getEventOnDate(LocalDateTime date);

  /**
   * Method that gets the Events in a specific range.
   *
   * @param startTimeDate the start time of the event.
   * @param endTimeDate the end time of the event.
   *
   * @return the event at the specified start time and end time.
   */
  List<Event> getEventsInRange(LocalDateTime startTimeDate,
                               LocalDateTime endTimeDate);

  /**
   * Method that shows busy status if the user has events scheduled on a given day and time.
   * Otherwise, available.
   *
   * @param dateTime the date and time for showing status.
   *
   * @return true if the user has another event scheduled at the specified
   *     date and time, otherwise, false.
   */
  boolean isBusy(LocalDateTime dateTime);

  /**
   * Method that exports the calendar.
   */
  public void exportCalendar(String filename);
}