package calendar.models;

/**
 * Represents a calendar that can store and manage events and event series.
 *
 * <p>A calendar provides methods for adding individual events or recurring event series,
 * filtering events based on specific conditions, checking availability at a given time, and
 * exporting stored data.
 * </p>
 */
public interface Calendar extends ObservableCalendar {

  /**
   * Adds a single event to the calendar.
   *
   * <p>Implementations should prevent adding duplicate events with the same subject
   * and exact start and end times.
   * </p>
   *
   * @param event the event to add; must not be {@code null}.
   * @return true if event was successfully added; false if an event conflicts with existing ones.
   */
  boolean addEvent(Event event);

  /**
   * Adds a recurring series of events to the calendar.
   *
   * <p>This may represent events defined by a recurrence rule or pattern.
   * </p>
   *
   * @param series the event series to add; must not be {@code null}.
   */
  void addEventSeries(EventSeries series);

  /**
   * Edits a single event's property in the calendar.
   *
   * @param event    the event to be edited
   * @param property the property of the event to be edited
   * @param newValue the new value of the property
   * @return new event
   */
  Event editSingleEvent(Event event, EventProperty property, String newValue);

  /**
   * Edits a series' (all events part of the series) property in the calendar. If the event is not
   * part of a series, it edits only the single event.
   *
   * @param event    the event to be edited
   * @param property the property of the event to be edited
   * @param newValue the new value of the property
   */
  EventSeries editSeriesEvent(Event event, EventProperty property, String newValue);

  /**
   * Edits the current event and the following events of the series. If the event is not part of a
   * series, it edits only the single event.
   *
   * @param event    the event to be edited
   * @param property the property of the event to be edited
   * @param newValue the new value of the property
   */
  EventSeries editThisAndFollowingEvents(Event event, EventProperty property, String newValue);

}
