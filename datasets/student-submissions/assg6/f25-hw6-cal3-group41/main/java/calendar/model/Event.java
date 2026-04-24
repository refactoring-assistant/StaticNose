package calendar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Represents a single calendar event.
 */
public class Event implements EventInterface {
  private final UUID id;
  private UUID seriesId;
  private boolean recurring;

  private String subject;
  private LocalDateTime start;
  private LocalDateTime end;
  private String description;
  private String location;
  private String status; // "public" or "private"

  /**
   * Creates an event with a subject and start/end timestamps.
   */
  public Event(String subject, LocalDateTime start, LocalDateTime end) {
    this.id = UUID.randomUUID();
    this.subject = subject;
    this.start = start;
    this.end = end;
  }

  @Override
  public UUID getId() {
    return id;
  }

  @Override
  public UUID getSeriesId() {
    return seriesId;
  }

  @Override
  public String subject() {
    return subject;
  }

  @Override
  public LocalDateTime startDate() {
    return start;
  }

  @Override
  public LocalDateTime endDate() {
    return end;
  }

  @Override
  public String description() {
    return description == null ? "" : description;
  }

  @Override
  public String location() {
    return location == null ? "" : location;
  }

  @Override
  public String status() {
    return status;
  }

  @Override
  public boolean isRecurring() {
    return recurring;
  }

  @Override
  public boolean isAllDay() {
    LocalDate ds = start.toLocalDate();
    LocalDate de = end.toLocalDate();
    LocalTime ts = start.toLocalTime();
    LocalTime te = end.toLocalTime();
    return ds.equals(de) && ts.equals(LocalTime.of(8, 0)) && te.equals(LocalTime.of(17, 0));
  }

  @Override
  public void setSeriesId(UUID seriesId) {
    this.seriesId = seriesId;
  }

  @Override
  public void setSubject(String subject) {
    this.subject = subject;
  }

  @Override
  public void setStart(LocalDateTime start) {
    this.start = start;
  }

  @Override
  public void setEnd(LocalDateTime end) {
    this.end = end;
  }

  @Override
  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public void setLocation(String location) {
    this.location = location;
  }

  @Override
  public void setStatus(String status) {
    this.status = status;
  }

  @Override
  public void setRecurring(boolean isRecurring) {
    this.recurring = isRecurring;
  }

  @Override
  public boolean conflictsWith(Event other) {
    return normalizeSubject(this.subject).equalsIgnoreCase(normalizeSubject(other.subject()))
        && this.start.equals(other.startDate())
        && this.end.equals(other.endDate());
  }

  /**
   * Removes quotes from subject.
   *
   * @param subject the subject
   * @return cleaned subject
   */
  private static String normalizeSubject(String subject) {
    if (subject == null) {
      return "";
    }
    String normalized = subject.trim();
    if (normalized.startsWith("\"") && normalized.endsWith("\"") && normalized.length() >= 2) {
      normalized = normalized.substring(1, normalized.length() - 1);
    }
    return normalized;
  }

  @Override
  public String toCsv() {
    return "";
  }

  @Override
  public Event copy() {
    Event e = new Event(this.subject, this.start, this.end);
    e.description = this.description;
    e.location = this.location;
    e.status = this.status;
    e.seriesId = this.seriesId;
    e.recurring = this.recurring;
    return e;
  }

  @Override
  public String toString() {
    return String.format("%s from %s to %s%s",
        subject, start, end, (location == null || location.isEmpty() ? "" : " @" + location));
  }
}
