package calendar.model;

import java.time.ZoneId;
import java.util.List;

/**
 * This interface represents a Calendar model that contains methods that can create and edit objects
 * and extends readonly model interface.
 */
public interface CalendarModel extends CalendarModelReadOnly {

  /**
   * Creates a single event in the calendar given required subject, start and end datetimes.
   *
   * @param subject the event subject.
   * @param startDateTime the start datetime.
   * @param endDateTime the end datetime.
   * @throws IllegalArgumentException if subject or start or end date times are invalid.
   */
  void createEvent(String subject, String startDateTime, String endDateTime)
      throws IllegalArgumentException;

  /**
   * Creates an event series that repeats N times on specific weekdays.
   *
   * @param subject event subject.
   * @param startDateTime the start datetime.
   * @param endDateTime the end datetime.
   * @param weekdays the weekdays when event repeats.
   * @param occurrences how many times event repeats.
   * @throws IllegalArgumentException if any of the args is invalid.
   */
  void createEventSeries(String subject, String startDateTime, String endDateTime,
                         String weekdays, int occurrences) throws IllegalArgumentException;

  /**
   * Creates an event series until a specific date (inclusive).
   *
   * @param subject event subject.
   * @param startDateTime the start datetime.
   * @param endDateTime the end datetime.
   * @param weekdays the weekdays when event repeats.
   * @param dateUntil until when it repeats (including that date).
   * @throws IllegalArgumentException if any of the args is invalid.
   */
  void createEventSeriesUntil(String subject, String startDateTime,
                              String endDateTime, String weekdays, String dateUntil)
      throws IllegalArgumentException;

  /**
   * Creates an all day event, from 8am to 5pm.
   *
   * @param subject subject event.
   * @param startDate the start date.
   * @throws IllegalArgumentException if any of the args is invalid.
   */
  void createAllDayEvent(String subject, String startDate) throws IllegalArgumentException;

  /**
   * Creates a series of all day events, from 8am to 5pm each.
   *
   * @param subject subject event.
   * @param startDate the start date.
   * @param weekdays the weekdays when event repeats.
   * @param occurrences how many times event repeats.
   * @throws IllegalArgumentException if any of the args is invalid.
   */
  void createAllDayEventSeries(String subject, String startDate, String weekdays, int occurrences)
      throws IllegalArgumentException;

  /**
   * Creates a series of all day events until a specific date (inclusive).
   *
   * @param subject subject event.
   * @param startDate the start date.
   * @param weekdays the weekdays when event repeats.
   * @param dateUntil until when it repeats (including that date).
   * @throws IllegalArgumentException if any of the args is invalid.
   */
  void createAllDayEventSeriesUntil(String subject, String startDate, String weekdays,
                                    String dateUntil) throws IllegalArgumentException;

  /**
   * Identifies the event that has the given subject and starts at the given date and time,
   * and edits its property.
   *
   * @param property property to edit.
   * @param subject subject event.
   * @param startDateTime the start datetime.
   * @param endDateTime end datetime.
   * @param val new value of property.
   */
  void editEvent(EventProperty property, String subject, String startDateTime, String endDateTime,
                 String val);

  /**
   * Identifies the event(s) that has the given subject and starts at the given date and time
   * and edits its property.
   *
   * @param property property to edit.
   * @param subject subject event.
   * @param startDateTime the start datetime.
   * @param val new value of property.
   */
  void editEvents(EventProperty property, String subject, String startDateTime, String val);

  /**
   * Identifies the event that has the given subject and starts at the given date and time
   * and edits its property.
   *
   * @param property property to edit.
   * @param subject subject event.
   * @param startDateTime the start datetime.
   * @param val new value of property.
   */
  void editSeries(EventProperty property, String subject, String startDateTime, String val);

  /**
   * Sets a new seriesID number after copying in series.
   *
   * @param id new highest series ID
   */
  void setEventSeriesId(int id);

  /**
   * Changes the timezone of all events in the model.
   *
   * @param timezone timezone to change all events to
   */
  void timezoneChangeAllEvents(ZoneId timezone, ZoneId newZone);

  /**
   * Adds new events to the model.
   *
   * @param event the events to add to the model
   */
  void addEvents(List<EventObject> event);
}
