package calendar.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Main calendar model that stores events and series.
 */
public class CalendarModel {
  private final List<Event> events;
  private final List<EventSeries> eventSeries;

  /**
   * Constructs a new CalendarModel.
   */
  public CalendarModel() {
    this.events = new ArrayList<>();
    this.eventSeries = new ArrayList<>();
  }

  /**
   * Adds an event to the calendar.
   */
  public void addEvent(Event event) {
    // Check for conflicts
    for (Event existing : events) {
      if (existing.conflictsWith(event)) {
        throw new IllegalArgumentException("Event conflict: An event with same subject, "
            + "start and end time already exists");
      }
    }
    events.add(event);
  }

  /**
   * Adds an event series to the calendar.
   */
  public void addEventSeries(EventSeries series) {
    eventSeries.add(series);
    generateSeriesEvents(series);
  }

  /**
   * Generate events for a series.
   */
  private void generateSeriesEvents(EventSeries series) {
    List<Event> seriesEvents = new ArrayList<>();
    LocalDateTime currentStart = series.getBaseStartTime();
    LocalDateTime currentEnd = series.getBaseEndTime();

    int occurrences = 0;
    int maxOccurrences = series.getOccurrences() != null
        ? series.getOccurrences() : Integer.MAX_VALUE;
    LocalDate untilDate = series.getUntilDate();

    while (occurrences < maxOccurrences) {
      // Check if we've passed the until date
      if (untilDate != null && currentStart.toLocalDate().isAfter(untilDate)) {
        break;
      }

      // Check if current day is in repeat days
      DayOfWeek currentDay = currentStart.getDayOfWeek();
      if (series.getRepeatDays().contains(currentDay)) {
        // Create event for this occurrence
        Event event = new Event(
            series.getBaseSubject(),
            currentStart,
            currentEnd,
            series.getBaseDescription(),
            series.getBaseLocation(),
            series.getBaseStatus(),
            series.getSeriesId()
        );

        // Check for conflicts before adding
        boolean hasConflict = false;
        for (Event existing : events) {
          if (existing.conflictsWith(event)) {
            hasConflict = true;
            break;
          }
        }

        if (!hasConflict) {
          seriesEvents.add(event);
        }

        occurrences++;
      }

      // Move to next day
      currentStart = currentStart.plusDays(1);
      currentEnd = currentEnd.plusDays(1);

      // Safety break to prevent infinite loops
      if (occurrences > 365) { // Max one year of events
        break;
      }
    }

    // Add all generated events
    events.addAll(seriesEvents);
  }

  /**
   * Gets events on a specific date.
   */
  public List<Event> getEventsOnDate(LocalDate date) {
    return events.stream()
        .filter(event -> event.getStartDateTime().toLocalDate().equals(date)
            || (event.getStartDateTime().toLocalDate().isBefore(date)
            && event.getEndDateTime().toLocalDate().isAfter(date))
            || event.getEndDateTime().toLocalDate().equals(date))
        .collect(Collectors.toList());
  }

  /**
   * Gets events in a date-time range.
   */
  public List<Event> getEventsInRange(LocalDateTime start, LocalDateTime end) {
    return events.stream()
        .filter(event -> event.getStartDateTime().isBefore(end)
            && event.getEndDateTime().isAfter(start))
        .collect(Collectors.toList());
  }

  /**
   * Checks if busy at a specific date-time.
   */
  public boolean isBusyAt(LocalDateTime dateTime) {
    return events.stream()
        .anyMatch(event -> !dateTime.isBefore(event.getStartDateTime())
            && !dateTime.isAfter(event.getEndDateTime()));
  }

  /**
   * Gets all events.
   */
  public List<Event> getAllEvents() {
    return new ArrayList<>(events);
  }

  /**
   * Finds an event by subject and start time.
   */
  public Event findEvent(String subject, LocalDateTime startDateTime) {
    return events.stream()
        .filter(event -> event.getSubject().equals(subject)
            && event.getStartDateTime().equals(startDateTime))
        .findFirst()
        .orElse(null);
  }

  /**
   * Finds events in a series from a specific date-time.
   */
  public List<Event> findEventsInSeriesFrom(String seriesId, LocalDateTime fromDateTime) {
    return events.stream()
        .filter(event -> seriesId.equals(event.getSeriesId())
            && !event.getStartDateTime().isBefore(fromDateTime))
        .collect(Collectors.toList());
  }

  /**
   * Finds all events in a series.
   */
  public List<Event> findEventsInSeries(String seriesId) {
    return events.stream()
        .filter(event -> seriesId.equals(event.getSeriesId()))
        .collect(Collectors.toList());
  }

  /**
   * Removes an event.
   */
  public void removeEvent(Event event) {
    events.remove(event);
  }

  /**
   * Updates an event.
   */
  public void updateEvent(Event oldEvent, Event newEvent) {
    if (!oldEvent.equals(newEvent)) {
      // Check for conflicts excluding the old event
      for (Event existing : events) {
        if (!existing.equals(oldEvent) && existing.conflictsWith(newEvent)) {
          throw new IllegalArgumentException("Event conflict: An event with same subject, "
              + "start and end time already exists");
        }
      }
    }

    events.remove(oldEvent);
    events.add(newEvent);
  }
}