package calendar.model;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of a calendar.
 * Representation: Uses a list to store events. This allows for flexible ordering
 * and easy iteration. Events are stored in the calendar's timezone.
 */
public class Calendar implements Icalendar {
  private String name;
  private ZoneId timezone;
  private final List<Ievent> events;

  /**
   * Creates a new calendar.
   */
  public Calendar(String name, ZoneId timezone) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be null or empty");
    }
    if (timezone == null) {
      throw new IllegalArgumentException("Timezone cannot be null");
    }
    this.name = name;
    this.timezone = timezone;
    this.events = new ArrayList<>();
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be null or empty");
    }
    this.name = name;
  }

  @Override
  public ZoneId getTimezone() {
    return timezone;
  }

  @Override
  public void setTimezone(ZoneId timezone) {
    if (timezone == null) {
      throw new IllegalArgumentException("Timezone cannot be null");
    }
    this.timezone = timezone;
  }

  @Override
  public void addEvent(Ievent event) {
    if (event == null) {
      throw new IllegalArgumentException("Event cannot be null");
    }
    events.add(event);
  }

  @Override
  public List<Ievent> getEvents() {
    return new ArrayList<>(events);
  }

  @Override
  public List<Ievent> getEventsOn(LocalDate date) {
    return events.stream()
        .filter(event -> event.occursOn(date))
        .collect(Collectors.toList());
  }

  @Override
  public List<Ievent> getEventsInRange(LocalDate start, LocalDate end) {
    return events.stream()
        .filter(event -> !event.getOccurrencesInRange(start, end).isEmpty())
        .collect(Collectors.toList());
  }

  @Override
  public Ievent findEvent(String name, LocalDate date) {
    return events.stream()
        .filter(event -> event.getName().equals(name) && event.occursOn(date))
        .findFirst()
        .orElse(null);
  }
}
