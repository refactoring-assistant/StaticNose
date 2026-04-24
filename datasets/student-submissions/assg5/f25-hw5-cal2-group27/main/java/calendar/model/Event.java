package calendar.model;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Represents a single, immutable calendar event.
 */
public final class Event {

  private final UUID id;
  private final UUID seriesId;
  private final String subject;
  private final ZonedDateTime start;
  private final ZonedDateTime end;
  private final String description;
  private final String location;
  private final EventStatus status;
  private final RecurrenceRule recurrence;

  Event(UUID id, UUID seriesId, String subject, ZonedDateTime start, ZonedDateTime end,
        String description, String location, EventStatus status, RecurrenceRule recurrence) {
    this.id = id;
    this.seriesId = seriesId;
    this.subject = subject;
    this.start = start;
    this.end = end;
    this.description = description;
    this.location = location;
    this.status = status;
    this.recurrence = recurrence;
  }

  public UUID getId() {
    return id;
  }

  public UUID getSeriesId() {
    return seriesId;
  }

  public String getSubject() {
    return subject;
  }

  public ZonedDateTime getStart() {
    return start;
  }

  public ZonedDateTime getEnd() {
    return end;
  }

  public String getDescription() {
    return description;
  }

  public String getLocation() {
    return location;
  }

  public EventStatus getStatus() {
    return status;
  }

  public RecurrenceRule getRecurrence() {
    return recurrence;
  }

  /**
   * Creates a new EventBuilder pre-populated with this event's data.
   *
   * @return A new EventBuilder instance.
   */
  public EventBuilder toBuilder() {
    return new EventBuilder()
        .seriesId(this.seriesId)
        .subject(this.subject)
        .start(this.start)
        .end(this.end)
        .description(this.description)
        .location(this.location)
        .status(this.status)
        .recurrence(this.recurrence);
  }
}
