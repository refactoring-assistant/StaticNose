package calendar.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Stores and manages all events in the calendar.
 *
 * <p>Implements CalendarModel interface and provides all main calendar logic.
 * This service handles event creation, editing, and querying operations.
 *
 * <p>Design Change (Assignment 5): Changed from ArrayList to HashMap for O(1)
 * event lookup performance. Key format: "subject|startDateTime"
 *
 * @author MH
 * @version 2.0
 */
public class CalendarService implements CalendarModel {

  private final Map<String, Event> events = new HashMap<>();

  private String generateKey(String subject, LocalDateTime start) {
    return subject + "|" + start.toString();
  }

  private String generateKey(Event e) {
    return generateKey(e.getSubject(), e.getStart());
  }

  private boolean existsSameIdentity(Event e, Event except) {
    for (Event x : events.values()) {
      if (except != null && x.equals(except)) {
        continue;
      }
      if (x.getSubject().equals(e.getSubject())
          && x.getStart().equals(e.getStart())
          && x.getEnd().equals(e.getEnd())) {
        return true;
      }
    }
    return false;
  }

  private void requireNoDuplicate(Event e, Event except) {
    if (existsSameIdentity(e, except)) {
      throw new IllegalStateException("duplicate event: same subject, start, end");
    }
  }

  @Override
  public Event createSingleEvent(Event draft) {
    requireNoDuplicate(draft, null);
    String key = generateKey(draft);
    events.put(key, draft);
    return draft;
  }

  @Override
  public List<Event> createEventSeries(Event template, RecurrenceRule rule) {
    UUID sid = UUID.randomUUID();
    LocalTime startTime = template.getStart().toLocalTime();
    LocalTime endTime = template.getEnd().toLocalTime();

    if (!template.getStart().toLocalDate().equals(template.getEnd().toLocalDate())) {
      throw new IllegalArgumentException("series instance must not span multiple days");
    }

    List<Event> made = new ArrayList<>();
    LocalDate cursor = template.getStart().toLocalDate();
    LocalDate stop = rule.getUntil().orElse(LocalDate.of(9999, 12, 31));
    int remaining = rule.getCount().orElse(Integer.MAX_VALUE);

    while (cursor.isBefore(stop.plusDays(1)) && remaining > 0) {
      DayOfWeek dow = cursor.getDayOfWeek();
      if (rule.getDays().contains(dow)) {
        LocalDateTime s = LocalDateTime.of(cursor, startTime);
        LocalDateTime e = LocalDateTime.of(cursor, endTime);
        Event inst = new Event(template.getSubject(), s, e,
            template.getDescription(), template.getLocation(),
            template.getStatus(), Optional.of(sid));
        requireNoDuplicate(inst, null);
        String key = generateKey(inst);
        events.put(key, inst);
        made.add(inst);
        remaining--;
      }
      cursor = cursor.plusDays(1);
    }

    if (made.isEmpty()) {
      throw new IllegalStateException("no instances created (check days / until / count)");
    }
    return made;
  }

  @Override
  public Optional<Event> findBySubjectAndStart(String subject, LocalDateTime start) {
    String key = generateKey(subject, start);
    return Optional.ofNullable(events.get(key));
  }

  private Event applyProperty(Event e, String property, String value) {
    switch (property.toLowerCase()) {
      case "subject":
        return e.withSubject(value);
      case "start":
        return e.withStart(LocalDateTime.parse(value));
      case "end":
        return e.withEnd(LocalDateTime.parse(value));
      case "description":
        return e.withDescription(value);
      case "location":
        return e.withLocation(value);
      case "status":
        return e.withStatus(value);
      default:
        throw new IllegalArgumentException("unknown property");
    }
  }

  @Override
  public void editSingle(String subject, LocalDateTime start,
                         String property, String newValue) {
    Event target = findBySubjectAndStart(subject, start)
        .orElseThrow(() -> new IllegalStateException("no matching event"));
    Event updated = applyProperty(target, property, newValue);
    requireNoDuplicate(updated, target);

    String oldKey = generateKey(target);
    events.remove(oldKey);

    String newKey = generateKey(updated);
    events.put(newKey, updated);
  }

  @Override
  public void editFrom(String subject, LocalDateTime start,
                       String property, String newValue) {
    Event pivot = findBySubjectAndStart(subject, start)
        .orElseThrow(() -> new IllegalStateException("no matching event"));
    Optional<UUID> sid = pivot.getSeriesId();
    if (sid.isEmpty()) {
      editSingle(subject, start, property, newValue);
      return;
    }

    List<Event> affected = events.values().stream()
        .filter(e -> e.getSeriesId().isPresent()
            && e.getSeriesId().get().equals(sid.get()))
        .filter(e -> !e.getStart().isBefore(pivot.getStart()))
        .collect(Collectors.toList());

    Map<String, Event> updates = new HashMap<>();
    for (Event e : affected) {
      Event u = applyProperty(e, property, newValue);
      requireNoDuplicate(u, e);
      String oldKey = generateKey(e);
      String newKey = generateKey(u);
      events.remove(oldKey);
      updates.put(newKey, u);
    }
    events.putAll(updates);
  }

  @Override
  public void editSeries(String subject, LocalDateTime start,
                         String property, String newValue) {
    Event pivot = findBySubjectAndStart(subject, start)
        .orElseThrow(() -> new IllegalStateException("no matching event"));
    Optional<UUID> sid = pivot.getSeriesId();
    if (sid.isEmpty()) {
      editSingle(subject, start, property, newValue);
      return;
    }

    List<Event> affected = events.values().stream()
        .filter(e -> e.getSeriesId().isPresent()
            && e.getSeriesId().get().equals(sid.get()))
        .collect(Collectors.toList());

    Map<String, Event> updates = new HashMap<>();
    for (Event e : affected) {
      Event u = applyProperty(e, property, newValue);
      requireNoDuplicate(u, e);
      String oldKey = generateKey(e);
      String newKey = generateKey(u);
      events.remove(oldKey);
      updates.put(newKey, u);
    }
    events.putAll(updates);
  }

  @Override
  public List<Event> eventsOn(LocalDate date) {
    return events.values().stream()
        .filter(e -> e.isOn(date))
        .sorted(Comparator.comparing(Event::getStart))
        .collect(Collectors.toList());
  }

  @Override
  public List<Event> eventsOverlapping(LocalDateTime from, LocalDateTime to) {
    return events.values().stream()
        .filter(e -> e.overlaps(from, to))
        .sorted(Comparator.comparing(Event::getStart))
        .collect(Collectors.toList());
  }

  @Override
  public BusyStatus statusAt(LocalDateTime instant) {
    for (Event e : events.values()) {
      if (!instant.isBefore(e.getStart()) && !instant.isAfter(e.getEnd())) {
        return BusyStatus.BUSY;
      }
    }
    return BusyStatus.AVAILABLE;
  }

  @Override
  public List<Event> getAllEvents() {
    return events.values().stream()
        .sorted(Comparator.comparing(Event::getStart))
        .collect(Collectors.toList());
  }

  /**
   * Updates an event's time when calendar timezone changes.
   *
   * @param subject event subject
   * @param oldStart old start time
   * @param newStart new start time
   * @param newEnd new end time
   */
  public void updateEventTime(String subject, LocalDateTime oldStart,
                              LocalDateTime newStart, LocalDateTime newEnd) {
    String oldKey = generateKey(subject, oldStart);
    Event oldEvent = events.get(oldKey);

    if (oldEvent != null) {
      events.remove(oldKey);

      Event newEvent = new Event(
          oldEvent.getSubject(),
          newStart,
          newEnd,
          oldEvent.getDescription(),
          oldEvent.getLocation(),
          oldEvent.getStatus(),
          oldEvent.getSeriesId()
      );

      String newKey = generateKey(newEvent);
      events.put(newKey, newEvent);
    }
  }
}