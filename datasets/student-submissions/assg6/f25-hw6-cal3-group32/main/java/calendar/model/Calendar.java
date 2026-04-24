package calendar.model;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * Calendar with timezone support and event management.
 */
public class Calendar {
  private static final int DEFAULT_END_HOUR = 17;
  private final List<Event> events;
  private final Set<String> identifiers;
  private final Map<String, BiConsumer<Event, Object>> propertySetters;
  private String name;
  private ZoneId timezone;

  /**
   * Creates calendar with name and timezone.
   *
   * @param name the calendar name
   * @param timezone the calendar timezone
   * @throws IllegalArgumentException if name or timezone is invalid
   */
  public Calendar(String name, ZoneId timezone) {
    validateName(name);
    validateTimezone(timezone);
    this.name = name;
    this.timezone = timezone;
    this.events = new ArrayList<>();
    this.identifiers = new HashSet<>();
    this.propertySetters = initPropertySetters();
  }

  private Map<String, BiConsumer<Event, Object>> initPropertySetters() {
    Map<String, BiConsumer<Event, Object>> m = new HashMap<>();
    m.put("subject", (e, v) -> e.setSubject((String) v));
    m.put("start", (e, v) -> e.setStart((ZonedDateTime) v));
    m.put("end", (e, v) -> e.setEnd((ZonedDateTime) v));
    m.put("description", (e, v) -> e.setDescription((String) v));
    m.put("location", (e, v) -> e.setLocation((String) v));
    m.put("status", (e, v) -> e.setPublic(((String) v).equalsIgnoreCase("public")));
    return m;
  }

  /**
   * Gets the calendar name.
   *
   * @return the calendar name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the calendar name.
   *
   * @param name the new calendar name
   * @throws IllegalArgumentException if name is invalid
   */
  public void setName(String name) {
    validateName(name);
    this.name = name;
  }

  /**
   * Gets the calendar timezone.
   *
   * @return the calendar timezone
   */
  public ZoneId getTimezone() {
    return timezone;
  }

  /**
   * Sets the calendar timezone and converts all events.
   *
   * @param tz the new timezone
   * @throws IllegalArgumentException if timezone is invalid
   */
  public void setTimezone(ZoneId tz) {
    validateTimezone(tz);
    for (Event e : events) {
      convertEventToTimezone(e, tz);
    }
    this.timezone = tz;
  }

  /**
   * Converts event to target timezone.
   *
   * @param e the event to convert
   * @param tz the target timezone
   */
  private void convertEventToTimezone(Event e, ZoneId tz) {
    identifiers.remove(createIdentifier(e));
    e.setStart(e.getStart().withZoneSameInstant(tz));
    if (e.getEnd() != null) {
      e.setEnd(e.getEnd().withZoneSameInstant(tz));
    }
    identifiers.add(createIdentifier(e));
  }

  /**
   * Adds event to calendar.
   *
   * @param e the event to add
   * @throws IllegalArgumentException if event is null or duplicate
   */
  public void addEvent(Event e) {
    if (e == null) {
      throw new IllegalArgumentException("Event cannot be null");
    }
    if (e.getEnd() == null) {
      e.setEnd(e.getStart().withHour(DEFAULT_END_HOUR).withMinute(0).withSecond(0).withNano(0));
    }
    String id = createIdentifier(e);
    if (identifiers.contains(id)) {
      throw new IllegalArgumentException("Duplicate event: " + e.getSubject());
    }
    events.add(e);
    identifiers.add(id);
  }

  /**
   * Adds multiple events to calendar.
   *
   * @param list the list of events to add
   * @throws IllegalArgumentException if list is null
   */
  public void addEvents(List<Event> list) {
    if (list == null) {
      throw new IllegalArgumentException("Event list cannot be null");
    }
    for (Event e : list) {
      addEvent(e);
    }
  }

  /**
   * Removes event from calendar.
   * Made public for GUI controller (professor-approved).
   *
   * @param e the event to remove
   */
  public void removeEvent(Event e) {
    if (e != null) {
      identifiers.remove(createIdentifier(e));
      events.remove(e);
    }
  }

  /**
   * Finds event by subject and start time.
   *
   * @param subj the event subject
   * @param start the event start time
   * @return the matching event
   * @throws IllegalArgumentException if no event found or multiple matches
   */
  public Event findEvent(String subj, ZonedDateTime start) {
    List<Event> m = events.stream()
        .filter(e -> e.getSubject().equals(subj)
            && e.getStart().toInstant().equals(start.toInstant()))
        .collect(Collectors.toList());
    if (m.isEmpty()) {
      throw new IllegalArgumentException("No event found: " + subj);
    } else if (m.size() > 1) {
      throw new IllegalArgumentException("Multiple events - specify end time");
    }
    return m.get(0);
  }

  /**
   * Finds event by subject, start time, and end time.
   *
   * @param subj the event subject
   * @param start the event start time
   * @param end the event end time
   * @return the matching event
   * @throws IllegalArgumentException if no event found
   */
  public Event findEvent(String subj, ZonedDateTime start, ZonedDateTime end) {
    return events.stream()
        .filter(e -> e.getSubject().equals(subj)
            && e.getStart().toInstant().equals(start.toInstant())
            && e.getEnd().toInstant().equals(end.toInstant()))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("No event found"));
  }

  /**
   * Gets all events on specific date.
   *
   * @param d the date to query
   * @return list of events on date, sorted by start time
   */
  public List<Event> getEventsOn(LocalDate d) {
    return events.stream()
        .filter(e -> e.getStart().toLocalDate().equals(d))
        .sorted(Comparator.comparing(Event::getStart))
        .collect(Collectors.toList());
  }

  /**
   * Gets all events overlapping time range.
   *
   * @param start the range start time
   * @param end the range end time
   * @return list of events in range, sorted by start time
   */
  public List<Event> getEventsBetween(ZonedDateTime start, ZonedDateTime end) {
    return events.stream()
        .filter(e -> e.overlaps(start, end))
        .sorted(Comparator.comparing(Event::getStart))
        .collect(Collectors.toList());
  }

  /**
   * Checks if calendar has event at specific time.
   *
   * @param t the time to check
   * @return true if busy, false if available
   */
  public boolean isBusy(ZonedDateTime t) {
    return events.stream().anyMatch(e -> e.contains(t));
  }

  /**
   * Gets all events in calendar.
   *
   * @return copy of all events
   */
  public List<Event> getAllEvents() {
    return new ArrayList<>(events);
  }

  /**
   * Edits event property with duplicate detection.
   *
   * @param e the event to edit
   * @param prop the property to edit
   * @param val the new value
   * @throws IllegalArgumentException if property unknown or edit creates duplicate
   */
  public void editEvent(Event e, String prop, Object val) {
    identifiers.remove(createIdentifier(e));
    applyEdit(e, prop, val);
    String newId = createIdentifier(e);
    if (identifiers.contains(newId)) {
      throw new IllegalArgumentException("Edit creates duplicate");
    }
    identifiers.add(newId);
  }

  /**
   * Edits event and all future occurrences in series.
   * Breaks series on start or end time changes.
   *
   * @param tgt the target event
   * @param prop the property to edit
   * @param val the new value
   */
  public void editEventsForward(Event tgt, String prop, Object val) {
    if (tgt.getSeriesId() == null) {
      editEvent(tgt, prop, val);
      return;
    }
    List<Event> toEdit = events.stream()
        .filter(e -> e.getSeriesId() != null
            && e.getSeriesId().equals(tgt.getSeriesId())
            && !e.getStart().isBefore(tgt.getStart()))
        .collect(Collectors.toList());
    if (prop.equals("start") || prop.equals("end")) {
      String newId = java.util.UUID.randomUUID().toString();
      toEdit.forEach(e -> e.setSeriesId(newId));
    }
    for (Event e : toEdit) {
      identifiers.remove(createIdentifier(e));
      applyEdit(e, prop, val);
      identifiers.add(createIdentifier(e));
    }
  }

  /**
   * Edits all events in series.
   *
   * @param tgt the target event
   * @param prop the property to edit
   * @param val the new value
   */
  public void editEntireSeries(Event tgt, String prop, Object val) {
    if (tgt.getSeriesId() == null) {
      editEvent(tgt, prop, val);
      return;
    }
    List<Event> toEdit = events.stream()
        .filter(e -> e.getSeriesId() != null
            && e.getSeriesId().equals(tgt.getSeriesId()))
        .collect(Collectors.toList());
    for (Event e : toEdit) {
      identifiers.remove(createIdentifier(e));
      applyEdit(e, prop, val);
      identifiers.add(createIdentifier(e));
    }
  }

  private void applyEdit(Event e, String prop, Object val) {
    BiConsumer<Event, Object> setter = propertySetters.get(prop.toLowerCase());
    if (setter == null) {
      throw new IllegalArgumentException("Unknown property: " + prop);
    }
    setter.accept(e, val);
  }

  private String createIdentifier(Event e) {
    String endPart = e.getEnd() != null
        ? e.getEnd().toInstant().toString()
        : "null";
    return e.getSubject() + "|" + e.getStart().toInstant() + "|" + endPart;
  }

  private void validateName(String n) {
    if (n == null || n.trim().isEmpty()) {
      throw new IllegalArgumentException("Name cannot be empty");
    }
  }

  private void validateTimezone(ZoneId tz) {
    if (tz == null) {
      throw new IllegalArgumentException("Timezone cannot be null");
    }
    tz.getRules();
  }
}