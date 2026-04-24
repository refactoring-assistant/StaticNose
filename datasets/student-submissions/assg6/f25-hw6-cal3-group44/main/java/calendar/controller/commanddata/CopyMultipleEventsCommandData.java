package calendar.controller.commanddata;

import java.time.LocalDate;

/**
 * Data transfer object for CopyMultipleEventsCalendarCommand parsed data.
 */
public class CopyMultipleEventsCommandData {
  private final LocalDate sourceStart;
  private final LocalDate sourceEnd;
  private final LocalDate targetStart;
  private final String targetCalendarName;
  private final boolean isOnCommand;

  /**
   * Constructor for "on" command format.
   *
   * @param sourceStart        the source start date
   * @param targetStart        the target start date
   * @param targetCalendarName the target calendar name
   */
  public CopyMultipleEventsCommandData(LocalDate sourceStart, LocalDate targetStart,
                                       String targetCalendarName) {
    this.sourceStart = sourceStart;
    this.sourceEnd = sourceStart;
    this.targetStart = targetStart;
    this.targetCalendarName = targetCalendarName;
    this.isOnCommand = true;
  }

  /**
   * Constructor for "between" command format.
   *
   * @param sourceStart        the source start date
   * @param sourceEnd          the source end date
   * @param targetStart        the target start date
   * @param targetCalendarName the target calendar name
   */
  public CopyMultipleEventsCommandData(LocalDate sourceStart, LocalDate sourceEnd,
                                       LocalDate targetStart, String targetCalendarName) {
    this.sourceStart = sourceStart;
    this.sourceEnd = sourceEnd;
    this.targetStart = targetStart;
    this.targetCalendarName = targetCalendarName;
    this.isOnCommand = false;
  }

  public LocalDate getSourceStart() {
    return sourceStart;
  }

  public LocalDate getSourceEnd() {
    return sourceEnd;
  }

  public LocalDate getTargetStart() {
    return targetStart;
  }

  public String getTargetCalendarName() {
    return targetCalendarName;
  }

  public boolean isOnCommand() {
    return isOnCommand;
  }
}

