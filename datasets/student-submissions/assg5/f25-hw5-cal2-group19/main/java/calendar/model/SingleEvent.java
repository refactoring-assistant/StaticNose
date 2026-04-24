package calendar.model;

import java.time.LocalDateTime;

/**
 * Represents a non-recurring calendar event.
 * Single events can span multiple days if needed.
 */
public class SingleEvent extends AbstractEvent {

  /**
   * Constructs a SingleEvent with required and optional fields.
   *
   * @param subject       the event subject (required)
   * @param startDateTime the start date/time (required)
   * @param endDateTime   the end date/time (null for all-day events)
   * @param description   optional event description
   * @param location      optional event location
   * @param status        the privacy status
   */
  public SingleEvent(String subject, LocalDateTime startDateTime,
                     LocalDateTime endDateTime, String description,
                     String location, EventStatus status) {
    super(subject, startDateTime, endDateTime, description, location, status);
  }

  @Override
  protected void validateEventConstraints() {
  }

  @Override
  public InEvent copy() {
    return new SingleEvent(
        this.subject,
        this.startDateTime,
        this.isAllDay ? null : this.endDateTime,
        this.description,
        this.location,
        this.status
    );
  }
}