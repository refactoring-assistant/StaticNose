package calendar.model;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Objects;

/**
 * Concrete event used by the calendar model. Group of events are being stored in a map.
 * For handling timezone related features we are storing time as an instant.
 */
public class Event implements InterfaceEvent {

  private final String subject;
  private final Instant start;
  private final Instant end;
  private final String description;
  private final String location;
  private final boolean isPublic;
  private final ZoneId zone;

  /**
   * Creates a new event.
   * Subject must be nonempty.
   * Start and end must be nonnull and end must be on or after start.
   *
   * @param subject     subject text
   * @param start       start instant
   * @param end         end instant
   * @param description description text
   * @param location    location text
   * @param isPublic    true if public, false if private
   * @param zone        timezone of the event
   */
  public Event(String subject,
               Instant start,
               Instant end,
               String description,
               String location,
               boolean isPublic,
               ZoneId zone) {
    if (subject == null || subject.isBlank()) {
      throw new IllegalArgumentException("Subject cannot be empty.");
    }
    this.start = Objects.requireNonNull(start, "start");
    this.end = Objects.requireNonNull(end, "end");
    if (end.isBefore(start)) {
      throw new IllegalArgumentException("End must not be before start.");
    }
    this.subject = subject;
    this.description = description == null ? "" : description;
    this.location = location == null ? "" : location;
    this.isPublic = isPublic;
    this.zone = Objects.requireNonNull(zone, "zone");
  }

  @Override
  public String getSubject() {
    return subject;
  }

  @Override
  public Instant getStart() {
    return start;
  }

  @Override
  public Instant getEnd() {
    return end;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public String getLocation() {
    return location;
  }

  @Override
  public boolean isPublicEvent() {
    return isPublic;
  }

  @Override
  public ZoneId getZone() {
    return zone;
  }

  @Override
  public long getDurationInSeconds() {
    return end.getEpochSecond() - start.getEpochSecond();
  }

  /**
   * Equality compares all identifying fields.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Event)) {
      return false;
    }
    Event that = (Event) o;
    return isPublic == that.isPublic
        && subject.equalsIgnoreCase(that.subject)
        && start.equals(that.start)
        && end.equals(that.end)
        && Objects.equals(description, that.description)
        && Objects.equals(location, that.location)
        && zone.equals(that.zone);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subject.toLowerCase(), start, end, description, location, isPublic, zone);
  }

  @Override
  public String toString() {
    return "Event{"
        + "subject='" + subject + '\''
        + ", start=" + start
        + ", end=" + end
        + ", location='" + location + '\''
        + ", public=" + isPublic
        + ", zone=" + zone
        + '}';
  }
}
