package calendar.controller.commanddata;

import java.time.LocalDateTime;

/**
 * Data transfer object for CreateCommand parsed data.
 * Contains all the parsed information needed to create an event.
 */
public class CreateCommandData {
  private final String subject;
  private final LocalDateTime startDateTime;
  private final LocalDateTime endDateTime;
  private final boolean isRepeating;
  private final String daysString;
  private final String repeatType;
  private final String repeatValue;
  private final boolean isFromCommand;

  /**
   * Constructor for single event creation (non-repeating).
   *
   * @param subject       the event subject
   * @param startDateTime the start date and time
   * @param endDateTime   the end date and time
   * @param isFromCommand true if using "from" syntax, false if using "on" syntax
   */
  public CreateCommandData(String subject, LocalDateTime startDateTime,
                           LocalDateTime endDateTime, boolean isFromCommand) {
    this.subject = subject;
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
    this.isRepeating = false;
    this.daysString = null;
    this.repeatType = null;
    this.repeatValue = null;
    this.isFromCommand = isFromCommand;
  }

  /**
   * Constructor for repeating event creation.
   *
   * @param subject       the event subject
   * @param startDateTime the start date and time
   * @param endDateTime   the end date and time
   * @param daysString    the days string (e.g., "MWF")
   * @param repeatType    the repeat type ("for" or "until")
   * @param repeatValue   the repeat value (number or date)
   * @param isFromCommand true if using "from" syntax, false if using "on" syntax
   */
  public CreateCommandData(String subject, LocalDateTime startDateTime,
                           LocalDateTime endDateTime, String daysString,
                           String repeatType, String repeatValue, boolean isFromCommand) {
    this.subject = subject;
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
    this.isRepeating = true;
    this.daysString = daysString;
    this.repeatType = repeatType;
    this.repeatValue = repeatValue;
    this.isFromCommand = isFromCommand;
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

  public boolean isRepeating() {
    return isRepeating;
  }

  public String getDaysString() {
    return daysString;
  }

  public String getRepeatType() {
    return repeatType;
  }

  public String getRepeatValue() {
    return repeatValue;
  }

  public boolean isFromCommand() {
    return isFromCommand;
  }
}

