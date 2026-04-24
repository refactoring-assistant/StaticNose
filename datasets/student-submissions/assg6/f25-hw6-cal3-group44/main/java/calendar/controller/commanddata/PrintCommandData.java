package calendar.controller.commanddata;

import java.time.LocalDateTime;

/**
 * Data transfer object for PrintCommand parsed data.
 */
public class PrintCommandData {
  private final LocalDateTime startDateTime;
  private final LocalDateTime endDateTime;
  private final boolean isOnCommand;

  /**
   * Constructor for PrintCommandData.
   *
   * @param startDateTime the start date and time
   * @param endDateTime   the end date and time
   * @param isOnCommand   true if using "on" syntax, false if using "from" syntax
   */
  public PrintCommandData(LocalDateTime startDateTime, LocalDateTime endDateTime,
                          boolean isOnCommand) {
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
    this.isOnCommand = isOnCommand;
  }

  public LocalDateTime getStartDateTime() {
    return startDateTime;
  }

  public LocalDateTime getEndDateTime() {
    return endDateTime;
  }

  public boolean isOnCommand() {
    return isOnCommand;
  }
}

