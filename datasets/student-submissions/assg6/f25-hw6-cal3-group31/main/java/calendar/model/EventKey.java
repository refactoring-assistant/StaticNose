package calendar.model;

import java.util.Objects;

/**
 * Represents a unique key for an event based on its identifying properties.
 * Used as a key in HashMap storage to enable fast lookups and uniqueness enforcement.
 * Two events are considered to have the same key if they have the same subject,
 * start date/time, and end date/time.
 */
public class EventKey {
  private final String subject;
  private final Date startDate;
  private final Time startTime;
  private final Date endDate;
  private final Time endTime;

  /**
   * Constructs an EventKey with the given properties.
   *
   * @param subject   the subject of the event
   * @param startDate the start date
   * @param startTime the start time
   * @param endDate   the end date
   * @param endTime   the end time
   */
  public EventKey(String subject, Date startDate, Time startTime,
                  Date endDate, Time endTime) {
    this.subject = subject;
    this.startDate = startDate;
    this.startTime = startTime;
    this.endDate = endDate;
    this.endTime = endTime;
  }

  /**
   * Constructs an EventKey from an IEvent object.
   *
   * @param event the event to create a key from
   */
  public EventKey(IntEvent event) {
    this(event.getSubject(), event.getStartDate(), event.getStartTime(),
        event.getEndDate(), event.getEndTime());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EventKey key = (EventKey) o;
    return Objects.equals(subject, key.subject)
        && Objects.equals(startDate, key.startDate)
        && Objects.equals(startTime, key.startTime)
        && Objects.equals(endDate, key.endDate)
        && Objects.equals(endTime, key.endTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subject, startDate, startTime, endDate, endTime);
  }

  @Override
  public String toString() {
    return String.format("EventKey[%s, %s %s - %s %s]",
        subject, startDate, startTime, endDate, endTime);
  }
}

