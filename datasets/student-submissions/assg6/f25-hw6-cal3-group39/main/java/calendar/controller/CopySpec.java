package calendar.controller;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Represents the specification for a copy operation within the calendar application.
 * This class acts as a Data Transfer Object (DTO) used to pass details about
 * source events, target dates, and target calendars from the controller to the model.
 * It is immutable and relies on the Builder pattern for instantiation.
 */
public final class CopySpec {
  private final String subject;
  private final LocalDate startDate;
  private final LocalDate endDate;
  private final LocalTime startTime;
  private final String targetCal;
  private final LocalDate targetDate;
  private final LocalTime targetTime;

  /**
   * Constructs a new CopySpec object using the provided builder.
   *
   * @param builder the builder containing the parameters for the copy specification
   */
  private CopySpec(CopySpecBuilder builder) {
    this.subject = builder.subject;
    this.startDate = builder.startDate;
    this.endDate = builder.endDate;
    this.startTime = builder.startTime;
    this.targetCal = builder.targetCal;
    this.targetDate = builder.targetDate;
    this.targetTime = builder.targetTime;
  }

  /**
   * Getter for the subject field.
   *
   * @return the subject of the event
   */
  public String getSubject() {
    return this.subject;
  }

  /**
   * Getter for the startDate field.
   *
   * @return the start date of the event
   */
  public LocalDate getStartDate() {
    return this.startDate;
  }

  /**
   * Getter for the endDate field.
   *
   * @return the endDate of the event
   */
  public LocalDate getEndDate() {
    return this.endDate;
  }

  /**
   * Getter for the startTime field.
   *
   * @return the start time of the event
   */
  public LocalTime getStartTime() {
    return this.startTime;
  }

  /**
   * Getter for the target time field.
   *
   * @return the target time of the event
   */
  public LocalTime getTargetTime() {
    return this.targetTime;
  }

  /**
   * Getter for the targetCal field.
   *
   * @return the targetCal of the event, or null if none
   */
  public String getTargetCalName() {
    return this.targetCal;
  }

  /**
   * Getter for the location field.
   *
   * @return the location of the event, or null if none
   */
  public LocalDate getTargetDate() {
    return this.targetDate;
  }

  /**
   * Builder class for constructing CopySpec instances.
   */
  public static class CopySpecBuilder {
    String subject = null;
    LocalDate startDate;
    LocalDate endDate = null;
    LocalTime startTime = null;
    String targetCal;
    LocalDate targetDate;
    LocalTime targetTime = null;

    /**
     * Constructs a new CopySpecBuilder with the mandatory fields required for a copy operation.
     *
     * @param startDate  the source date of the event(s) to be copied
     * @param targetCal  the name of the destination calendar
     * @param targetDate the destination date for the copied event(s)
     */
    public CopySpecBuilder(LocalDate startDate, String targetCal, LocalDate targetDate) {
      this.startDate = startDate;
      this.targetCal = targetCal;
      this.targetDate = targetDate;
    }

    /**
     * Getter for end date.
     *
     * @return The end date
     */
    public LocalDate getEndDate() {
      return this.endDate;
    }

    /**
     * Getter for start date.
     *
     * @return the start date
     */
    public LocalDate getStartDate() {
      return this.startDate;
    }

    /**
     * The function below sets the endDate field.
     *
     * @param endDate value to be set.
     * @return the updated this object
     */
    public CopySpecBuilder endDate(LocalDate endDate) {
      this.endDate = endDate;
      return this;
    }

    /**
     * The function below sets the startTime field.
     *
     * @param startTime the start time to be set
     * @return the updated this object
     */
    public CopySpecBuilder startTime(LocalTime startTime) {
      this.startTime = startTime;
      return this;
    }

    /**
     * The function below sets the targetTime field.
     *
     * @param targetTime the target time to be set
     * @return the updated this object
     */
    public CopySpecBuilder targetTime(LocalTime targetTime) {
      this.targetTime = targetTime;
      return this;
    }

    /**
     * The function below sets the subject field.
     *
     * @param subject the subject to be set
     * @return the updated this object
     */
    public CopySpecBuilder subject(String subject) {
      this.subject = subject;
      return this;
    }

    /**
     * Builds the CopySpec instance.
     *
     * @return a CopySpec object
     */
    public CopySpec build() {
      return new CopySpec(this);
    }
  }

}