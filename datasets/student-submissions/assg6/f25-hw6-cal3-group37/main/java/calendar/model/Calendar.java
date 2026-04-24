package calendar.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of CalendarModel using a list-based storage.
 * Maintains events and tracks series relationships.
 */
public class Calendar implements CalendarModel {
  private final List<CalendarEvent> events;
  private final Map<String, Set<CalendarEvent>> seriesMap; // seriesId -> events in that series

  /**
   * Constructs a new empty Calendar.
   */
  public Calendar() {
    this.events = new ArrayList<>();
    this.seriesMap = new HashMap<>();
  }

  @Override
  public void addEvent(CalendarEvent event) {
    validateNoConflict(event);
    events.add(event);

    if (event.getSeriesId() != null) {
      seriesMap.computeIfAbsent(event.getSeriesId(), k -> new HashSet<>()).add(event);
    }
  }

  @Override
  public String addEventSeries(String subject, LocalDateTime startDateTime,
                               LocalDateTime endDateTime, Set<DayOfWeek> daysOfWeek,
                               Integer occurrences, LocalDate untilDate) {
    // Validate that event doesn't span multiple days
    if (!startDateTime.toLocalDate().equals(endDateTime.toLocalDate())) {
      throw new IllegalArgumentException(
          "Events in a series must start and end on the same day");
    }

    String seriesId = UUID.randomUUID().toString();
    List<CalendarEvent> seriesEvents = new ArrayList<>();

    LocalDateTime currentStart = startDateTime;
    LocalTime startTime = startDateTime.toLocalTime();
    LocalTime endTime = endDateTime.toLocalTime();
    int count = 0;

    while (true) {
      // Check if we should stop
      if (occurrences != null && count >= occurrences) {
        break;
      }
      if (untilDate != null && currentStart.toLocalDate().isAfter(untilDate)) {
        break;
      }

      // Check if current day is in the set of days to repeat on
      if (daysOfWeek.contains(currentStart.getDayOfWeek())) {
        LocalDateTime eventStart = LocalDateTime.of(currentStart.toLocalDate(), startTime);
        LocalDateTime eventEnd = LocalDateTime.of(currentStart.toLocalDate(), endTime);

        CalendarEvent event = new Event(subject, eventStart, eventEnd);
        event.setSeriesId(seriesId);
        seriesEvents.add(event);
        count++;
      }

      currentStart = currentStart.plusDays(1);
    }

    // Validate all events before adding any
    for (CalendarEvent event : seriesEvents) {
      validateNoConflict(event);
    }

    // Add all events
    for (CalendarEvent event : seriesEvents) {
      events.add(event);
    }
    seriesMap.put(seriesId, new HashSet<>(seriesEvents));

    return seriesId;
  }

  @Override
  public void editEvent(String subject, LocalDateTime startDateTime,
                        String property, String newValue) {
    CalendarEvent event = findEvent(subject, startDateTime);

    if (property.equals("start")) {
      // When changing start time, event leaves its series
      if (event.getSeriesId() != null) {
        removeFromSeries(event);
      }
    }

    applyEdit(event, property, newValue);
  }

  @Override
  public void editEventsFromDate(String subject, LocalDateTime startDateTime,
                                 String property, String newValue) {
    CalendarEvent event = findEvent(subject, startDateTime);

    if (event.getSeriesId() == null) {
      // Single event, same as editEvent
      editEvent(subject, startDateTime, property, newValue);
      return;
    }

    Set<CalendarEvent> seriesEvents = seriesMap.get(event.getSeriesId());
    List<CalendarEvent> eventsToEdit = seriesEvents.stream()
        .filter(e -> !e.getStartDateTime().isBefore(startDateTime))
        .collect(Collectors.toList());


    // If editing start time, create new series for affected events
    if (property.equals("start")) {
      String oldSeriesId = event.getSeriesId();
      String newSeriesId = UUID.randomUUID().toString();

      // Create new sets (don't modify existing ones!)
      Set<CalendarEvent> oldSeriesEvents = new HashSet<>(seriesEvents);
      oldSeriesEvents.removeAll(eventsToEdit);

      Set<CalendarEvent> newSeriesEvents = new HashSet<>(eventsToEdit);

      // Update series IDs on events
      for (CalendarEvent e : eventsToEdit) {
        e.setSeriesId(newSeriesId);
      }

      // Update the map with both series
      seriesMap.put(oldSeriesId, oldSeriesEvents);  // Update old series
      seriesMap.put(newSeriesId, newSeriesEvents);   // Add new series
    }

    // Needed to add this to apply the edits to the affected events
    for (CalendarEvent e : eventsToEdit) {
      applyEdit(e, property, newValue);
    }
  }

  @Override
  public void editEntireSeries(String subject, LocalDateTime startDateTime,
                               String property, String newValue) {
    CalendarEvent event = findEvent(subject, startDateTime);

    if (event.getSeriesId() == null) {
      // Single event
      editEvent(subject, startDateTime, property, newValue);
      return;
    }

    Set<CalendarEvent> seriesEvents = seriesMap.get(event.getSeriesId());

    // If editing start time, need to handle series split
    if (property.equals("start")) {
      // This would split into new series
      String newSeriesId = UUID.randomUUID().toString();
      Set<CalendarEvent> newSeriesSet = new HashSet<>(seriesEvents);

      String oldSeriesId = event.getSeriesId();
      seriesMap.remove(oldSeriesId);

      for (CalendarEvent e : seriesEvents) {
        e.setSeriesId(newSeriesId);
      }
      seriesMap.put(newSeriesId, newSeriesSet);
    }

    // Apply edit to all events in series
    for (CalendarEvent e : seriesEvents) {
      applyEdit(e, property, newValue);
    }
  }

  @Override
  public List<CalendarEvent> getEventsOnDate(LocalDate date) {
    return events.stream()
        .filter(e -> !e.getStartDateTime().toLocalDate().isAfter(date)
            && !e.getEndDateTime().toLocalDate().isBefore(date))
        .sorted((e1, e2) -> e1.getStartDateTime().compareTo(e2.getStartDateTime()))
        .collect(Collectors.toList());
  }

  @Override
  public List<CalendarEvent> getEventsInRange(LocalDateTime startDateTime,
                                              LocalDateTime endDateTime) {
    return events.stream()
        .filter(e -> !(e.getEndDateTime().isBefore(startDateTime)
            || e.getStartDateTime().isAfter(endDateTime)))
        .sorted((e1, e2) -> e1.getStartDateTime().compareTo(e2.getStartDateTime()))
        .collect(Collectors.toList());
  }

  @Override
  public boolean isBusy(LocalDateTime dateTime) {
    return events.stream()
        .anyMatch(e -> !dateTime.isBefore(e.getStartDateTime())
            && dateTime.isBefore(e.getEndDateTime()));
  }

  @Override
  public List<CalendarEvent> getAllEvents() {
    return new ArrayList<>(events);
  }

  // Helper methods

  private CalendarEvent findEvent(String subject, LocalDateTime startDateTime) {
    List<CalendarEvent> matches = events.stream()
        .filter(e -> e.getSubject().equals(subject)
            && e.getStartDateTime().equals(startDateTime))
        .collect(Collectors.toList());

    if (matches.isEmpty()) {
      throw new IllegalArgumentException(
          "No event found with subject '" + subject + "' at " + startDateTime);
    }
    if (matches.size() > 1) {
      throw new IllegalArgumentException(
          "Multiple events found with same subject and start time. Cannot edit.");
    }

    return matches.get(0);
  }

  private void validateNoConflict(CalendarEvent newEvent) {
    for (CalendarEvent existing : events) {
      if (existing.getSubject().equals(newEvent.getSubject())
          && existing.getStartDateTime().equals(newEvent.getStartDateTime())
          && existing.getEndDateTime().equals(newEvent.getEndDateTime())) {
        throw new IllegalArgumentException(
            "Event with same subject, start and end time already exists");
      }
    }
  }

  private void removeFromSeries(CalendarEvent event) {
    String seriesId = event.getSeriesId();
    if (seriesId != null) {
      Set<CalendarEvent> seriesEvents = seriesMap.get(seriesId);
      if (seriesEvents != null) {
        seriesEvents.remove(event);
        if (seriesEvents.isEmpty()) {
          seriesMap.remove(seriesId);
        }
      }
      event.setSeriesId(null);
    }
  }

  private void applyEdit(CalendarEvent event, String property, String newValue) {
    switch (property.toLowerCase()) {
      case "subject":
        event.setSubject(newValue);
        break;
      case "start": // had to update this!
        LocalDateTime newStart = LocalDateTime.parse(newValue);
        // Keep the original date, only change the Time
        LocalDateTime updatedStart = LocalDateTime.of(
            event.getStartDateTime().toLocalDate(),  // Original date
            newStart.toLocalTime()                    // New time
        );

        // Calculate duration to preserve it
        long durationMinutes = java.time.Duration.between(
            event.getStartDateTime(),
            event.getEndDateTime()
        ).toMinutes();

        // Update start and end
        event.setStartDateTime(updatedStart);
        event.setEndDateTime(updatedStart.plusMinutes(durationMinutes));
        break;
      case "end":
        LocalDateTime newEnd = LocalDateTime.parse(newValue);
        event.setEndDateTime(newEnd);
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
}