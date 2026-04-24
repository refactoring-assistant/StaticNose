package calendar.model;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A single calendar holding events for a specific time zone.
 * Internally stores events in hash-based indexes for efficient lookup:
 * by unique key (subject,start), by subject for series operations,
 * and by local date for day-based queries.
 */
public class CalendarModel {

  /**
   * Composite key for indexing events by subject and start instant.
   */
  private static final class EventKey {
    private final String subject;
    private final Instant start;

    EventKey(String subject, Instant start) {
      this.subject = subject;
      this.start = start;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof EventKey)) {
        return false;
      }
      EventKey k = (EventKey) o;
      return Objects.equals(subject, k.subject) && Objects.equals(start, k.start);
    }

    @Override
    public int hashCode() {
      return Objects.hash(subject, start);
    }
  }

  private String name;
  private ZoneId timeZone;

  private final Map<EventKey, InterfaceEvent> byKey;
  private final Map<String, List<InterfaceEvent>> bySubject;
  private final Map<LocalDate, List<InterfaceEvent>> byDate;

  /**
   * Creates a new calendar with a name and time zone.
   *
   * @param name calendar name
   * @param timeZone time zone
   */
  public CalendarModel(String name, ZoneId timeZone) {
    this.name = Objects.requireNonNull(name);
    this.timeZone = Objects.requireNonNull(timeZone);
    this.byKey = new HashMap<EventKey, InterfaceEvent>();
    this.bySubject = new HashMap<String, List<InterfaceEvent>>();
    this.byDate = new HashMap<LocalDate, List<InterfaceEvent>>();
  }

  /**
   * Returns the calendar name.
   *
   * @return name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the calendar name.
   *
   * @param name new name
   */
  public void setName(String name) {
    this.name = Objects.requireNonNull(name);
  }

  /**
   * Returns the time zone of this calendar.
   *
   * @return time zone
   */
  public ZoneId getTimeZone() {
    return timeZone;
  }

  /**
   * Sets the calendar time zone.
   *
   * @param timeZone new time zone
   */
  public void setTimeZone(ZoneId timeZone) {
    this.timeZone = Objects.requireNonNull(timeZone);
  }

  /**
   * Adds an event to this calendar.
   *
   * @param e event to add
   */
  public void addEvent(InterfaceEvent e) {
    Objects.requireNonNull(e);
    EventKey k = new EventKey(e.getSubject(), e.getStart());
    byKey.put(k, e);

    bySubject.computeIfAbsent(e.getSubject(), s -> new ArrayList<InterfaceEvent>()).add(e);

    LocalDate d = LocalDate.ofInstant(e.getStart(), e.getZone());
    byDate.computeIfAbsent(d, x -> new ArrayList<InterfaceEvent>()).add(e);
  }

  /**
   * Removes an event from this calendar.
   *
   * @param e event to remove
   */
  public void removeEvent(InterfaceEvent e) {
    if (e == null) {
      return;
    }
    EventKey k = new EventKey(e.getSubject(), e.getStart());
    byKey.remove(k);

    List<InterfaceEvent> series = bySubject.get(e.getSubject());
    if (series != null) {
      series.remove(e);
      if (series.isEmpty()) {
        bySubject.remove(e.getSubject());
      }
    }

    LocalDate d = LocalDate.ofInstant(e.getStart(), e.getZone());
    List<InterfaceEvent> day = byDate.get(d);
    if (day != null) {
      day.remove(e);
      if (day.isEmpty()) {
        byDate.remove(d);
      }
    }
  }

  /**
   * Returns all events in this calendar without guaranteed order.
   *
   * @return list of events
   */
  public List<InterfaceEvent> getEvents() {
    return new ArrayList<InterfaceEvent>(byKey.values());
  }

  /**
   * Finds the first event in this calendar by subject and start instant.
   *
   * @param subject event subject
   * @param start start instant
   * @return event or null
   */
  public InterfaceEvent findEvent(String subject, Instant start) {
    return byKey.get(new EventKey(subject, start));
  }

  /**
   * Returns all events on a given local date in the event zone.
   *
   * @param date local date
   * @return events occurring on that date
   */
  public List<InterfaceEvent> getEventsOn(LocalDate date) {
    List<InterfaceEvent> day = byDate.get(date);
    if (day == null) {
      return Collections.emptyList();
    }
    List<InterfaceEvent> out = new ArrayList<InterfaceEvent>(day);
    out.sort((a, b) -> a.getStart().compareTo(b.getStart()));
    return out;
  }

  /**
   * Returns events with start date within inclusive date range.
   *
   * @param start inclusive start date
   * @param end inclusive end date
   * @return list of events in range
   */
  public List<InterfaceEvent> getEventsBetween(LocalDate start, LocalDate end) {
    List<InterfaceEvent> out = new ArrayList<InterfaceEvent>();
    LocalDate cur = start;
    while (!cur.isAfter(end)) {
      List<InterfaceEvent> day = byDate.get(cur);
      if (day != null) {
        out.addAll(day);
      }
      cur = cur.plusDays(1);
    }
    out.sort((a, b) -> a.getStart().compareTo(b.getStart()));
    return out;
  }

  /**
   * Finds all events sharing the same subject as a series.
   *
   * @param subject subject
   * @return list of events in the series
   */
  public List<InterfaceEvent> findSeries(String subject) {
    List<InterfaceEvent> series = bySubject.get(subject);
    if (series == null) {
      return Collections.emptyList();
    }
    List<InterfaceEvent> out = new ArrayList<InterfaceEvent>(series);
    out.sort((a, b) -> a.getStart().compareTo(b.getStart()));
    return out;
  }

  /**
   * Removes events in a series from the given pivot start instant onward.
   *
   * @param subject series subject
   * @param pivotStart pivot start instant
   */
  public void removeSeriesFrom(String subject, Instant pivotStart) {
    List<InterfaceEvent> series = bySubject.get(subject);
    if (series == null || series.isEmpty()) {
      return;
    }
    List<InterfaceEvent> toRemove = new ArrayList<InterfaceEvent>();
    for (InterfaceEvent e : series) {
      if (!e.getStart().isBefore(pivotStart)) {
        toRemove.add(e);
      }
    }
    for (InterfaceEvent e : toRemove) {
      removeEvent(e);
    }
  }

  /**
   * Reports whether there exists at least one event whose time interval
   * contains the given instant in this calendar.
   *
   * @param instant instant in the calendar's time line
   * @return true if the calendar is busy at that instant, false otherwise
   */
  public boolean isBusyAt(Instant instant) {
    for (InterfaceEvent e : byKey.values()) {
      boolean startsBeforeOrAt = !instant.isBefore(e.getStart());
      boolean endsAfter = instant.isBefore(e.getEnd());
      if (startsBeforeOrAt && endsAfter) {
        return true;
      }
    }
    return false;
  }
}