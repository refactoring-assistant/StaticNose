package calendar.model.impl;

import calendar.model.EventSpec;
import calendar.model.EventView;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Immutable representation of a calendar event.
 * Each Event belongs optionally to SeriesId (for recurring events)
 * and is identified uniquely by an EventId. All fields are immutable.
 * Any modification produces a new Event.
 */
public final class Event implements EventView {

  private final EventId id;
  private final SeriesId seriesId;
  private final String subject;
  private final LocalDateTime start;
  private final LocalDateTime end;
  private final String description;
  private final String location;
  private final EventSpec.Status status;
  private final boolean allDay;

  /**
   * Creates a new immutable Event.
   *
   * @param id          unique identifier for the event (non-null)
   * @param seriesId    identifier of the series, or {@code null} if not part of one
   * @param subject     event subject/title (non-null)
   * @param start       event start date-time (non-null)
   * @param end         event end date-time, or {@code null} if not specified
   * @param description optional description (defaults to empty string if null)
   * @param location    optional location (defaults to empty string if null)
   * @param status      visibility or sharing status
   * @param allDay      true if the event spans the entire day
   */
  public Event(EventId id, SeriesId seriesId, String subject,
               LocalDateTime start, LocalDateTime end,
               String description, String location,
               EventSpec.Status status, boolean allDay) {
    this.id = Objects.requireNonNull(id, "EventId cannot be null");
    this.seriesId = seriesId;
    this.subject = Objects.requireNonNull(subject, "Subject cannot be null");
    this.start = Objects.requireNonNull(start, "Start time cannot be null");
    this.end = end;
    this.description = description == null ? "" : description;
    this.location = location == null ? "" : location;
    this.status = status == null ? EventSpec.Status.PUBLIC : status;
    this.allDay = allDay;
  }

  /**
   * Returns the unique identifier of this event.
   *
   * @return the unique identifier of this event
   */
  public EventId id() {
    return id;
  }

  /**
   * Returns the series identifier of this event.
   *
   * @return the series identifier if part of a recurring series.
   */
  public SeriesId seriesId() {
    return seriesId;
  }

  /**
   * Returns the event subject or title.
   *
   * @return the event subject or title
   */
  public String subject() {
    return subject;
  }

  /**
   * Returns the start date and time of the event.
   *
   * @return the start date and time of the event
   */
  public LocalDateTime start() {
    return start;
  }

  /**
   * Returns the end date and time of the event.
   *
   * @return the end date and time of the event, or {@code null} if unspecified
   */
  public LocalDateTime end() {
    return end;
  }

  /**
   * Returns the event Description string.
   *
   * @return the event description text (never null)
   */
  public String description() {
    return description;
  }

  /**
   * Returns the event Location string.
   *
   * @return the event location (never null)
   */
  public String location() {
    return location;
  }

  /**
   * Returns the event visibility string.
   *
   * @return the event visibility or status
   */
  public EventSpec.Status status() {
    return status;
  }

  /**
   * Returns if the event is spanning all day.
   *
   * @return true if the event spans the entire day
   */
  public boolean allDay() {
    return allDay;
  }


  /**
   * Creates a copy of this event with a new subject.
   *
   * @param s new subject text
   * @return a new Event instance with the updated subject
   */
  public Event withSubject(String s) {
    return new Event(id, seriesId, s, start, end, description, location, status, allDay);
  }

  /**
   * Creates a copy of this event with a new start time.
   *
   * @param s new start date-time
   * @return a new Event instance with the updated start time
   */
  public Event withStart(LocalDateTime s) {
    return new Event(id, seriesId, subject, s, end, description, location, status, allDay);
  }

  /**
   * Creates a copy of this event with a new end time.
   *
   * @param e new end date-time
   * @return a new Event instance with the updated end time
   */
  public Event withEnd(LocalDateTime e) {
    return new Event(id, seriesId, subject, start, e, description, location, status, allDay);
  }

  /**
   * Creates a copy of this event with a new description.
   *
   * @param d new description text
   * @return a new code Event instance with the updated description
   */
  public Event withDescription(String d) {
    return new Event(id, seriesId, subject, start, end, d, location, status, allDay);
  }

  /**
   * Creates a copy of this event with a new location.
   *
   * @param l new location text
   * @return a new Event instance with the updated location
   */
  public Event withLocation(String l) {
    return new Event(id, seriesId, subject, start, end, description, l, status, allDay);
  }

  /**
   * Creates a copy of this event with a new status.
   *
   * @param st new Status
   * @return a new Event instance with the updated status
   */
  public Event withStatus(EventSpec.Status st) {
    return new Event(id, seriesId, subject, start, end, description, location, st, allDay);
  }

  /**
   * Creates a copy of this event with a new SeriesID.
   *
   * @param sid new Sid
   * @return a new Event instance with the updated Sid
   */
  public Event withSeriesId(SeriesId sid) {
    return new Event(id, sid, subject, start, end, description, location, status, allDay);
  }

}
