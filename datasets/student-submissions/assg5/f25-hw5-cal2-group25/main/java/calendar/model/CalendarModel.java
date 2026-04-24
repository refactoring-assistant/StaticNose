package calendar.model;

import calendar.model.impl.Event;
import calendar.model.impl.EventId;
import calendar.model.impl.SeriesRule;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Defines operations for managing calendar events.
 * Supports creating, editing, listing, and exporting events.
 */
public interface CalendarModel {

  /**
   * Creates a single event using the given event specification.
   *
   * @param spec the details of the event to create
   * @return the ID of the created event
   */
  EventId createSingle(EventSpec spec);

  /**
   * Creates a series of repeating events based on a rule.
   *
   * @param base the base event specification
   * @param rule the rule defining how the series repeats
   * @return a list of IDs for the created events
   */
  List<EventId> createSeries(EventSpec base, SeriesRule rule);

  /**
   * Edits one or more events using the given selector and scope.
   *
   * @param selector selects which events to edit
   * @param scope    determines whether to edit one event or a whole series
   * @param change   describes what property to change and its new value
   */
  void edit(EventSelector selector, EditScope scope, PropertyChange change);

  /**
   * Gets all events occurring on the given date.
   *
   * @param date the date to search for
   * @return a list of events on that date
   */
  List<Event> eventsOn(LocalDate date);

  /**
   * Gets all events happening between two date-times.
   *
   * @param start the start of the time range
   * @param end   the end of the time range
   * @return a list of events within the range
   */
  List<Event> eventsBetween(LocalDateTime start, LocalDateTime end);

  /**
   * Checks if there is an event happening at the given time.
   *
   * @param at the time to check
   * @return true if the time is busy, false otherwise
   */
  boolean isBusy(LocalDateTime at);

  /**
   * Exports all events using the given exporter to a file.
   *
   * @param exporter the exporter to use
   * @param file     the file to export to
   * @throws java.io.IOException if an error occurs during export
   */
  void export(Exporter exporter, Path file) throws java.io.IOException;

  /**
   * Returns the timezone of this calendar.
   */
  String getTimezone();

  /**
   * Changes the timezone of this calendar.
   *
   * @param timezone IANA timezone string (e.g., "America/New_York")
   */
  void setTimezone(String timezone);
}
