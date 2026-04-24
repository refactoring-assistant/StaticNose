package calendar.model.event;

import calendar.model.CalendarEventUtils;
import java.util.Map;

/**
 * Represents a single calendar event.
 * A SingleEvent may be standalone or part of an event series.
 *
 * <p>SingleEvent instances are immutable - to modify an event,
 * create a new instance with updated properties using
 * {@link CalendarEventUtils#withUpdatedProperty}.
 */
public class SingleEvent extends AbstractCalendarEvent {

  /**
   * Constructs a new single calendar event.
   *
   * @param parameters map containing event properties: "subject" (required),
   *                   "start" (required), "end" (required), and optionally
   *                   "description", "location", "status"
   * @param seriesUid  the UID of the series this event belongs to, or null if standalone
   * @throws IllegalArgumentException if required parameters are missing or invalid
   */
  public SingleEvent(Map<String, Object> parameters, String seriesUid) {
    super(parameters, seriesUid);
  }

  /**
   * Checks if this event is part of a series.
   *
   * @return true if this event has a series UID, false otherwise
   */
  @Override
  public boolean isPartOfSeries() {
    return this.getSeriesUid() != null;
  }
}