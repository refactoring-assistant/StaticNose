package calendar.controller.commanddata;

import java.time.LocalDateTime;

/**
 * Data transfer object for CopySingleEventCalendarCommand parsed data.
 */
public class CopySingleEventCommandData {
  private final String eventName;
  private final LocalDateTime sourceStart;
  private final String targetCalendarName;
  private final LocalDateTime targetStart;

  /**
   * Constructor for CopySingleEventCommandData.
   *
   * @param eventName          the event name to copy
   * @param sourceStart        the source start date and time
   * @param targetCalendarName the target calendar name
   * @param targetStart        the target start date and time
   */
  public CopySingleEventCommandData(String eventName, LocalDateTime sourceStart,
                                    String targetCalendarName, LocalDateTime targetStart) {
    this.eventName = eventName;
    this.sourceStart = sourceStart;
    this.targetCalendarName = targetCalendarName;
    this.targetStart = targetStart;
  }

  public String getEventName() {
    return eventName;
  }

  public LocalDateTime getSourceStart() {
    return sourceStart;
  }

  public String getTargetCalendarName() {
    return targetCalendarName;
  }

  public LocalDateTime getTargetStart() {
    return targetStart;
  }
}

