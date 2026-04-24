package calendar.model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Represents a calendar event (single or part of a series).
 *
 * <p>Implements IEvent interface to maintain abstraction and encapsulation.
 *
 * <p>Design Change (Assignment 5): All fields are now private to follow
 * proper encapsulation. Access is provided through getter methods only.
 * This prevents external modification and maintains immutability.
 *
 * @author MH
 * @version 2.0
 */
public class Event implements Ievent {

  public static final int ALLDAY_START_HOUR = 8;
  public static final int ALLDAY_END_HOUR = 17;

  private final String subject;
  private final LocalDateTime start;
  private final LocalDateTime end;
  private final Optional<String> description;
  private final Optional<String> location;
  private final Optional<String> status;
  private final Optional<UUID> seriesId;

  /**
   * Creates a new event.
   *
   * @param subject subject of the event
   * @param start start date/time
   * @param end end date/time (must be >= start)
   * @param description optional description
   * @param location optional location
   * @param status optional status (public/private)
   * @param seriesId optional series ID if recurring
   * @throws IllegalArgumentException if subject is blank or end is before start
   */
  public Event(String subject, LocalDateTime start, LocalDateTime end,
               Optional<String> description, Optional<String> location,
               Optional<String> status, Optional<UUID> seriesId) {
    if (subject == null || subject.isBlank()) {
      throw new IllegalArgumentException("subject required");
    }
    if (end.isBefore(start)) {
      throw new IllegalArgumentException("end before start");
    }
    this.subject = subject;
    this.start = start;
    this.end = end;
    this.description = description == null ? Optional.empty() : description;
    this.location = location == null ? Optional.empty() : location;
    this.status = status == null ? Optional.empty() : status;
    this.seriesId = seriesId == null ? Optional.empty() : seriesId;
  }

  /**
   * Creates an all-day event (8 AM – 5 PM).
   *
   * @param subject subject of the event
   * @param date date of the all-day event
   * @param description optional description
   * @param location optional location
   * @param status optional status
   * @param seriesId optional series ID
   * @return new all-day Event
   */
  public static Event allDay(String subject, LocalDate date,
                             Optional<String> description,
                             Optional<String> location,
                             Optional<String> status,
                             Optional<UUID> seriesId) {
    LocalDateTime s = date.atTime(ALLDAY_START_HOUR, 0);
    LocalDateTime e = date.atTime(ALLDAY_END_HOUR, 0);
    return new Event(subject, s, e, description, location, status, seriesId);
  }

  @Override
  public String getSubject() {
    return subject;
  }

  @Override
  public LocalDateTime getStart() {
    return start;
  }

  @Override
  public LocalDateTime getEnd() {
    return end;
  }

  @Override
  public Optional<String> getDescription() {
    return description;
  }

  @Override
  public Optional<String> getLocation() {
    return location;
  }

  @Override
  public Optional<String> getStatus() {
    return status;
  }

  @Override
  public Optional<UUID> getSeriesId() {
    return seriesId;
  }

  @Override
  public Event withSubject(String newSubject) {
    return new Event(newSubject, start, end, description, location, status, seriesId);
  }

  @Override
  public Event withStart(LocalDateTime newStart) {
    long minutes = Duration.between(start, end).toMinutes();
    LocalDateTime newEnd = newStart.plusMinutes(minutes);
    return new Event(subject, newStart, newEnd, description, location, status, seriesId);
  }

  @Override
  public Event withEnd(LocalDateTime newEnd) {
    return new Event(subject, start, newEnd, description, location, status, seriesId);
  }

  @Override
  public Event withDescription(String d) {
    return new Event(subject, start, end, Optional.ofNullable(d),
        location, status, seriesId);
  }

  @Override
  public Event withLocation(String l) {
    return new Event(subject, start, end, description,
        Optional.ofNullable(l), status, seriesId);
  }

  @Override
  public Event withStatus(String s) {
    return new Event(subject, start, end, description,
        location, Optional.ofNullable(s), seriesId);
  }

  @Override
  public boolean overlaps(LocalDateTime from, LocalDateTime to) {
    return !end.isBefore(from) && !start.isAfter(to);
  }

  @Override
  public boolean isOn(LocalDate date) {
    return !start.toLocalDate().isAfter(date)
        && !end.toLocalDate().isBefore(date);
  }

  @Override
  public String toString() {
    String loc = location.map(x -> " @ " + x).orElse("");
    return subject + " starting on " + start.toLocalDate()
        + " at " + start.toLocalTime()
        + ", ending on " + end.toLocalDate()
        + " at " + end.toLocalTime() + loc;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Event)) {
      return false;
    }
    Event e = (Event) o;
    return subject.equals(e.subject)
        && start.equals(e.start)
        && end.equals(e.end)
        && Objects.equals(description, e.description)
        && Objects.equals(location, e.location)
        && Objects.equals(status, e.status)
        && Objects.equals(seriesId, e.seriesId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subject, start, end,
        description, location, status, seriesId);
  }
}