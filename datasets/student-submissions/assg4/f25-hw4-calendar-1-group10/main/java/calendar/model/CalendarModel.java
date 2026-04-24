package calendar.model;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * A model for a calendar.
 */
public interface CalendarModel {

  /**
   * Creates an event in the calendar.
   *
   * @param event the event to create
   * @throws IllegalArgumentException if the event conflicts or duplicates with an existing event
   */
  void createEvent(Event event) throws IllegalArgumentException;

  /**
   * Creates a series of events in the calendar.
   *
   * @param series the series of events to create
   * @throws IllegalArgumentException if the series conflicts or duplicates with an existing event
   */
  void createSeries(EventSeries series) throws IllegalArgumentException;

  /**
   * Edits an event in the calendar.
   *
   * @param property      the property to edit ("subject", "start", "end", "description",
   *                      "location", "status")
   * @param subject       the subject of the event
   * @param startDateTime the start date and time of the event
   * @param newValue      the new value of the property
   * @param mode          the edit mode: "event", "events" (this and future), or "series"
   *                      (all in series)
   * @throws IllegalArgumentException if the edit conflicts or duplicates with an existing event
   */
  void editEvent(String property, String subject, LocalDateTime startDateTime, String newValue,
                 String mode)
      throws IllegalArgumentException;

  /**
   * Gets all events on a given date.
   *
   * @param date the date to get events for
   * @return a list of events on the given date
   */
  List<Event> getEventsOn(LocalDate date);

  /**
   * Gets all events between two dates.
   *
   * @param start the start date
   * @param end   the end date
   * @return a list of events between the two dates
   */
  List<Event> getEventsBetween(LocalDateTime start, LocalDateTime end);

  /**
   * Checks if a given time is busy.
   *
   * @param time the time to check
   * @return true if the time is busy, false otherwise
   */
  boolean isBusy(LocalDateTime time);

  /**
   * Exports the calendar to a CSV file.
   *
   * @param filename the filename to export to
   * @throws IOException if there is an error writing to the file
   */
  void exportToCsv(String filename) throws IOException;

  /**
   * Gets all events in the calendar.
   *
   * @return a list of all events in the calendar
   */
  List<Event> getAllEvents();
}
