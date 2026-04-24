package calendar;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * The Calendar interface.
 */
public interface Icalender {
  /**
   * Helps to add an event to the calendar.
   *
   * @param e a single event.
   */
  void addEvent(Event e);

  /**
   * Helps to add an event series to the calendar.
   *
   * @param series an Event series.
   */
  void addEvent(EventSeries series);

  /**
   * Finds events matching the given criteria.
   *
   * @param subject       the subject to match
   * @param startDateTime the start date/time to match
   * @return list of matching events
   */
  List<Event> findEvents(String subject, LocalDateTime startDateTime);

  /**
   * Edits a single event instance.
   * Works for both standalone events and events that are part of a series.
   *
   * @param subject       the subject of the event to edit
   * @param startDateTime the start date/time of the event to edit
   * @param property      the property to change
   * @param newValue      the new value for the property
   * @throws IllegalArgumentException if event not found or edit causes conflict
   */
  void editEvent(String subject, LocalDateTime startDateTime,
                 String property, String newValue);

  /**
   * Edits all events in a series starting from the specified event.
   * If the event is not part of a series, behaves like editEvent.
   *
   * @param subject       the subject of the event to edit
   * @param startDateTime the start date/time of the event to edit
   * @param property      the property to change
   * @param newValue      the new value for the property
   * @throws IllegalArgumentException if event not found or edit causes conflict
   */
  void editEventsFromDate(String subject, LocalDateTime startDateTime,
                          String property, String newValue);

  /**
   * Edits all events in a series.
   * If the event is not part of a series, behaves like editEvent.
   *
   * @param subject       the subject of the event to edit
   * @param startDateTime the start date/time of the event to edit
   * @param property      the property to change
   * @param newValue      the new value for the property
   * @throws IllegalArgumentException if event not found or edit causes conflict
   */
  void editSeries(String subject, LocalDateTime startDateTime,
                  String property, String newValue);

  /**
   * Checks if the user is busy at a specific date and time.
   *
   * @param dateTime the date and time to check
   * @return true if there are events scheduled at that time
   */
  boolean isBusyAt(LocalDateTime dateTime);

  /**
   * Displays all events on a specific date in bullet format.
   *
   * @param date the date to display events for
   * @return a list of events on that day.
   */
  List<Event> displayEventOn(LocalDate date);

  /**
   * Displays all events in a date/time range.
   *
   * @param rangeStart the start of the range
   * @param rangeEnd   the end of the range
   * @return a list with events in that range.
   */
  List<Event> displayEventBetween(LocalDateTime rangeStart, LocalDateTime rangeEnd);

  /**
   * Exports all events in the calendar to a CSV file in Google Calendar format.
   *
   * @param fileName the name of the CSV file
   * @return the path of the CSV
   * @throws IOException if there is error writing the file
   */
  String exportToCsv(String fileName) throws IOException;

}
