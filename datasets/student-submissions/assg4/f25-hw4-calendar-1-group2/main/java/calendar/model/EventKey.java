package calendar.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * EventKey (subject + start + end) serves as the primal key of an event in calendar.
 */
public class EventKey {
  private final String subject;
  private final LocalDateTime startDateTime;
  private final LocalDateTime endDateTime;

  /**
   * Constructor for EventKey.
   *
   * @param subject the theme of the event
   * @param startDateTime the start of the event, format: YYYY-MM-DDThh:mm
   * @param endDateTime the end of the event, format: YYYY-MM-DDThh:mm
   */
  public EventKey(String subject, LocalDateTime startDateTime, LocalDateTime endDateTime) {
    this.subject = subject.trim();
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
  }

  public String getSubject() {
    return subject;
  }

  public LocalDateTime getStartDateTime() {
    return startDateTime;
  }

  public LocalDateTime getEndDateTime() {
    return endDateTime;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EventKey eventKey = (EventKey) o;
    return Objects.equals(subject, eventKey.subject)
        && Objects.equals(startDateTime, eventKey.startDateTime)
        && Objects.equals(endDateTime, eventKey.endDateTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subject, startDateTime, endDateTime);
  }
}
