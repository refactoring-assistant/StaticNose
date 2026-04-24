package calendar.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Handles editing of calendar events with support for:
 * - Editing a single event
 * - Editing events in a series from a point onwards
 * - Editing all events in a series
 *
 * <p>Important: Modifying start times can break events out of their series.
 */
public class EventEditor {
  private final Calendar calendar;

  /**
   * Constructor for Event Editor.
   *
   * @param calendar Calendar Object
   */
  public EventEditor(Calendar calendar) {
    this.calendar = calendar;
  }

  /**
   * Edits a single event (even if part of a series).
   *
   * @param subject       Subject to search for
   * @param startDateTime Start time to search for
   * @param property      Property to edit (subject, start, end, description, location, status)
   * @param newValue      New value for the property
   * @throws IllegalArgumentException if event not found or edit would create duplicate
   */
  public void editSingleEvent(String subject, LocalDateTime startDateTime,
                              String property, String newValue) {
    List<Event> matches = calendar.findEvents(subject, startDateTime);

    if (matches.isEmpty()) {
      throw new IllegalArgumentException("No event found with given subject and start time");
    }
    if (matches.size() > 1) {
      throw new IllegalArgumentException("Multiple events found. Cannot edit.");
    }

    Event event = matches.get(0);

    // If modifying start time, remove from series
    if (property.equalsIgnoreCase("start") && event.getSeriesId() != null) {
      event.setSeriesId(null);
    }

    applyEdit(event, property, newValue);
  }

  /**
   * Edits an event and all future events in its series.
   * If event is not part of a series, behaves like editSingleEvent.
   *
   * @param subject       Subject of the Event as String
   * @param startDateTime Start date and time as Local Date Time
   * @param property      Property of the event as String
   * @param newValue      New Property value as String
   */
  public void editEventsFrom(String subject, LocalDateTime startDateTime,
                             String property, String newValue) {
    List<Event> matches = calendar.findEvents(subject, startDateTime);

    if (matches.isEmpty()) {
      throw new IllegalArgumentException("No event found with given subject and start time");
    }
    if (matches.size() > 1) {
      throw new IllegalArgumentException("Multiple events found. Cannot edit.");
    }

    Event event = matches.get(0);
    List<Event> eventsToEdit = calendar.getEventsInSeriesFrom(event);

    // If modifying start time, remove edited events from series
    if (property.equalsIgnoreCase("start") && event.getSeriesId() != null) {
      String newSeriesId = null;
      for (Event e : eventsToEdit) {
        e.setSeriesId(newSeriesId);
      }
    }

    for (Event e : eventsToEdit) {
      applyEdit(e, property, newValue);
    }
  }


  /**
   * Edits all events in a series.
   * If event is not part of a series, behaves like editSingleEvent.
   *
   * @param subject       Subject of the Event as String
   * @param startDateTime Start date and time as Local Date Time
   * @param property      Property of the event as String
   * @param newValue      New Property value as String
   */
  public void editEntireSeries(String subject, LocalDateTime startDateTime,
                               String property, String newValue) {
    List<Event> matches = calendar.findEvents(subject, startDateTime);

    if (matches.isEmpty()) {
      throw new IllegalArgumentException("No event found with given subject and start time");
    }
    if (matches.size() > 1) {
      throw new IllegalArgumentException("Multiple events found. Cannot edit.");
    }

    Event event = matches.get(0);
    List<Event> eventsToEdit = calendar.getEventsInSeries(event);

    // If modifying start time, remove all events from series
    if (property.equalsIgnoreCase("start") && event.getSeriesId() != null) {
      for (Event e : eventsToEdit) {
        e.setSeriesId(null);
      }
    }

    for (Event e : eventsToEdit) {
      applyEdit(e, property, newValue);
    }
  }

  /**
   * Applies a property edit to an event.
   * Validates that the edit doesn't create duplicates.
   *
   * @param event    Event of the calendar as Event
   * @param property Property of the event as String
   * @param newValue New Property value as String
   */
  private void applyEdit(Event event, String property, String newValue) {
    // Create a temporary copy to test for duplicates
    Event tempEvent = new EventImpl(event.getSubject(),
        event.getStartDateTime(),
        event.getEndDateTime());

    try {
      // Apply edit to temp event
      applyPropertyChange(tempEvent, property, newValue);

      // Check if this would create a duplicate
      List<Event> allEvents = calendar.getAllEvents();
      for (Event existing : allEvents) {
        if (existing != event && existing.equals(tempEvent)) {
          throw new IllegalArgumentException(
              "Edit would create duplicate event: " + tempEvent.getSubject()
                  + " at " + tempEvent.getStartDateTime());
        }
      }

      // If no duplicate, apply to actual event
      applyPropertyChange(event, property, newValue);

    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Cannot edit event: " + e.getMessage());
    }
  }

  /**
   * Applies a property change to an event.
   *
   * @param event    Event of the calendar as Event
   * @param property Property of the event as String
   * @param newValue New Property value as String
   */
  private void applyPropertyChange(Event event, String property, String newValue) {
    switch (property.toLowerCase()) {
      case "subject":
        event.setSubject(newValue);
        break;
      case "start":
        event.setStartDateTime(parseDateTime(newValue));
        break;
      case "end":
        event.setEndDateTime(parseDateTime(newValue));
        break;
      case "description":
        event.setDescription(newValue);
        break;
      case "location":
        event.setLocation(newValue);
        break;
      case "status":
        event.setStatus(newValue);
        break;
      default:
        throw new IllegalArgumentException("Unknown property: " + property);
    }
  }

  /**
   * Parses a date-time string in format "YYYY-MM-DDThh:mm".
   */
  private LocalDateTime parseDateTime(String dateTimeStr) {
    try {
      return LocalDateTime.parse(dateTimeStr);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid date/time format: " + dateTimeStr
          + ". Expected format: YYYY-MM-DDThh:mm");
    }
  }
}