package calendar.controller.commanddata;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Data transfer object for DeleteEventCommand parsed data.
 */
public class DeleteEventCommandData {
  private final String subject;
  private final LocalDateTime startDateTime;
  private final LocalDateTime endDateTime;

  /**
   * Constructor for DeleteEventCommandData.
   *
   * @param subject       the event subject
   * @param startDateTime the start date and time
   * @param endDateTime   the end date and time
   */
  public DeleteEventCommandData(String subject, LocalDateTime startDateTime,
                                LocalDateTime endDateTime) {
    this.subject = Objects.requireNonNull(subject);
    this.startDateTime = Objects.requireNonNull(startDateTime);
    this.endDateTime = Objects.requireNonNull(endDateTime);
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
}

