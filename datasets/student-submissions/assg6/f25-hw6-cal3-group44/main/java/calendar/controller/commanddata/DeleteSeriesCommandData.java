package calendar.controller.commanddata;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Data transfer object for DeleteSeriesCommand parsed data.
 */
public class DeleteSeriesCommandData {
  private final String subject;
  private final LocalDateTime startDateTime;

  /**
   * Constructor for DeleteSeriesCommandData.
   *
   * @param subject       the event subject
   * @param startDateTime the start date and time
   */
  public DeleteSeriesCommandData(String subject, LocalDateTime startDateTime) {
    this.subject = Objects.requireNonNull(subject);
    this.startDateTime = Objects.requireNonNull(startDateTime);
  }

  public String getSubject() {
    return subject;
  }

  public LocalDateTime getStartDateTime() {
    return startDateTime;
  }
}

