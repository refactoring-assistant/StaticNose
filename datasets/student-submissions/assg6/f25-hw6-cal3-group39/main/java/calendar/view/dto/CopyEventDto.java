package calendar.view.dto;

/**
 * A Data Transfer Object (DTO) representing a request to copy events
 * from one location in a calendar to another or to the same one. This DTO encapsulates
 * all metadata required for different copy modes such as copying a
 * specific event, all events on a date, or events within a date range.
 */
public class CopyEventDto {

  private final CopyMode mode;
  private final String targetCalendarName;
  private final String targetDate;
  private final String targetTime;
  private final String startDate;
  private final String endDate;
  private final String startTime;
  private final String subject;

  /**
   * Constructs a CopyEventDto using the provided builder.
   *
   * @param builder the builder instance
   */
  private CopyEventDto(Builder builder) {

    this.mode = builder.mode;
    this.targetCalendarName = builder.targetCalendarName;
    this.targetDate = builder.targetDate;
    this.targetTime = builder.targetTime;
    this.startDate = builder.startDate;
    this.endDate = builder.endDate;
    this.startTime = builder.startTime;
    this.subject = builder.subject;
  }

  /**
   * Returns the copy mode.
   *
   * @return the copy mode
   */
  public CopyMode getMode() {
    return mode;
  }

  /**
   * Returns the target calendar name.
   *
   * @return the target calendar name
   */
  public String getTargetCalendarName() {
    return targetCalendarName;
  }

  /**
   * Returns the target date.
   *
   * @return the target date
   */
  public String getTargetDate() {
    return targetDate;
  }

  /**
   * Returns the target time.
   *
   * @return the target time or null if not provided
   */
  public String getTargetTime() {
    return targetTime;
  }

  /**
   * Returns the start date used for ALL_ON_DATE mode.
   *
   * @return the start date or null if not provided
   */
  public String getStartDate() {
    return startDate;
  }

  /**
   * Returns the end date used for DATE_RANGE mode.
   *
   * @return the end date or null if not provided
   */
  public String getEndDate() {
    return endDate;
  }

  /**
   * Returns the start time used for DATE_RANGE mode.
   *
   * @return the start time or null if not provided
   */
  public String getStartTime() {
    return startTime;
  }

  /**
   * Returns the subject.
   *
   * @return the subject or null if not provided
   */
  public String getSubject() {
    return subject;
  }

  /**
   * Describes the different modes of copying events.
   *
   * <ul>
   *   <li>{@code SELECTED_EVENT}: Copy a single, explicitly provided event.</li>
   *   <li>{@code ALL_ON_DATE}: Copy all events that occur on a specified date.</li>
   *   <li>{@code DATE_RANGE}: Copy all events within a defined date range.</li>
   * </ul>
   */
  public enum CopyMode {
    SELECTED_EVENT,
    ALL_ON_DATE,
    DATE_RANGE
  }

  /**
   * Builder class for constructing CopyEventDto instances.
   */
  public static class Builder {

    private CopyMode mode;
    private String targetCalendarName;
    private String targetDate;
    private String targetTime;
    private String startDate;
    private String endDate;
    private String startTime;
    private String subject;

    /**
     * Sets the copy mode.
     *
     * @param mode the copy mode
     * @return this builder instance
     */
    public Builder mode(CopyMode mode) {
      this.mode = mode;
      return this;
    }

    /**
     * Sets the target calendar name.
     *
     * @param targetCalendarName the target calendar name
     * @return this builder instance
     */
    public Builder targetCalendarName(String targetCalendarName) {
      this.targetCalendarName = targetCalendarName;
      return this;
    }

    /**
     * Sets the target date.
     *
     * @param targetDate the target date
     * @return this builder instance
     */
    public Builder targetDate(String targetDate) {
      this.targetDate = targetDate;
      return this;
    }

    /**
     * Sets the target time.
     *
     * @param targetTime the target time
     * @return this builder instance
     */
    public Builder targetTime(String targetTime) {
      this.targetTime = targetTime;
      return this;
    }

    /**
     * Sets the start date used for ALL_ON_DATE mode.
     *
     * @param startDate the start date
     * @return this builder instance
     */
    public Builder startDate(String startDate) {
      this.startDate = startDate;
      return this;
    }

    /**
     * Sets the end date used for DATE_RANGE mode.
     *
     * @param endDate the end date
     * @return this builder instance
     */
    public Builder endDate(String endDate) {
      this.endDate = endDate;
      return this;
    }

    /**
     * Sets the start time used for DATE_RANGE mode.
     *
     * @param startTime the start time
     * @return this builder instance
     */
    public Builder startTime(String startTime) {
      this.startTime = startTime;
      return this;
    }

    /**
     * Sets the subject.
     *
     * @param subject of the event
     * @return this builder instance
     */
    public Builder subject(String subject) {
      this.subject = subject;
      return this;
    }

    /**
     * Constructs the CopyEventDto instance.
     *
     * @return a new CopyEventDto
     */
    public CopyEventDto build() {
      return new CopyEventDto(this);
    }
  }
}
