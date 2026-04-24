package calendar.controller;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Below is the CreateSpec class, it is used to create a data transfer object from controller to
 * the create method of the model.
 */
public final class CreateSpec {
  private final String subject;
  private final LocalDate startDate;
  private final LocalDate endDate;
  private final LocalTime startTime;
  private final LocalTime endTime;
  private final String description;
  private final String location;
  private final String status;
  private final LocalDate until;
  private final int times;
  private final String weekdays;

  private CreateSpec(CreateSpecBuilder builder) {
    this.subject = builder.subject;
    this.startDate = builder.startDate;
    this.endDate = builder.endDate;
    this.startTime = builder.startTime;
    this.endTime = builder.endTime;
    this.description = builder.description;
    this.location = builder.location;
    this.status = builder.status;
    this.until = builder.until;
    this.times = builder.times;
    this.weekdays = builder.weekdays;
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
   * @return the end date of the event
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
   * Getter for the endTime field.
   *
   * @return the end time of the event
   */
  public LocalTime getEndTime() {
    return this.endTime;
  }

  /**
   * Getter for the description field.
   *
   * @return the description of the event, or null if none
   */
  public String getDescription() {
    return this.description;
  }

  /**
   * Getter for the location field.
   *
   * @return the location of the event, or null if none
   */
  public String getLocation() {
    return this.location;
  }

  /**
   * Getter for the status field.
   *
   * @return the status of the event, or null if none
   */
  public String getStatus() {
    return this.status;
  }

  /**
   * Getter for the until field.
   *
   * @return the date until which the event repeats, or null if not applicable
   */
  public LocalDate getUntil() {
    return this.until;
  }

  /**
   * Getter for the times field.
   *
   * @return the number of times the event repeats
   */
  public int getTimes() {
    return this.times;
  }

  /**
   * Getter for the weekdays field.
   *
   * @return the weekdays on which the event repeats, or null if not applicable
   */
  public String getWeekdays() {
    return this.weekdays;
  }

  /**
   * This is the inner CreateSpecBuilder class.
   */
  public static class CreateSpecBuilder {
    String subject;
    LocalDate startDate;
    LocalDate endDate;
    LocalTime startTime = LocalTime.of(8, 0);
    LocalTime endTime = LocalTime.of(17, 0);
    String description = "null";
    String location = "null";
    String status = "null";
    LocalDate until = null;
    int times = -1;
    String weekdays = null;

    /**
     * The createSpecBuilder constructor.
     *
     * @param subject   is the subject.
     * @param startDate is the start date.
     * @param startTime is the start time.
     * @param endDate   is the end date.
     * @param endTime   is the end time.
     */
    public CreateSpecBuilder(String subject, LocalDate startDate, LocalTime startTime,
                             LocalDate endDate, LocalTime endTime) {
      this.subject = subject;
      this.startDate = startDate;
      this.endDate = endDate;
      this.startTime = startTime;
      this.endTime = endTime;
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
     * The function below sets the times field.
     *
     * @param times the number of times an event repeats
     */
    public CreateSpecBuilder times(int times) {
      this.times = times;
      return this;
    }

    /**
     * The function below sets the until field.
     *
     * @param until the en date until the event repeats
     */
    public CreateSpecBuilder until(LocalDate until) {
      this.until = until;
      return this;
    }

    /**
     * This is the weekdays setter.
     *
     * @param weekdays weekdays input.
     */
    public CreateSpecBuilder weekdays(String weekdays) {
      this.weekdays = weekdays;
      return this;
    }


    /**
     * This is the description setter.
     *
     * @param description setting description
     */
    public CreateSpecBuilder description(String description) {
      this.description = description;
      return this;
    }

    /**
     * This is the location setter.
     *
     * @param location setting description
     */
    public CreateSpecBuilder location(String location) {
      this.location = location;
      return this;
    }

    /**
     * This is the status setter.
     *
     * @param status setting description
     */
    public CreateSpecBuilder status(String status) {
      this.status = status;
      return this;
    }


    /**
     * Builds the CreateSpec instance.
     *
     * @return a CreateSpec object
     */
    public CreateSpec build() {
      return new CreateSpec(this);
    }
  }

}


