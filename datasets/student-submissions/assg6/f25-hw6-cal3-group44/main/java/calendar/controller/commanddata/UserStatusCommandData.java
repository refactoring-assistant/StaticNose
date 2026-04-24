package calendar.controller.commanddata;

import java.time.LocalDateTime;

/**
 * Data transfer object for UserStatusCommand parsed data.
 */
public class UserStatusCommandData {
  private final LocalDateTime dateTime;

  /**
   * Constructor for UserStatusCommandData.
   *
   * @param dateTime the date and time to check status for
   */
  public UserStatusCommandData(LocalDateTime dateTime) {
    this.dateTime = dateTime;
  }

  public LocalDateTime getDateTime() {
    return dateTime;
  }
}

