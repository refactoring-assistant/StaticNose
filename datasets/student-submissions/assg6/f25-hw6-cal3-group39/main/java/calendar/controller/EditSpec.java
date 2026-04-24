package calendar.controller;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Below is the EditSpec class, it is used to create a data transfer object from controller to
 * the create method of the model.
 */
public final class EditSpec {
  private final String subject;
  private final LocalDate startDate;
  private final LocalDate endDate;
  private final LocalTime startTime;
  private final LocalTime endTime;
  private final String property;
  private final String newPropValue;
  private final String type;
  private final long daysDiff;
  private final long minsDiff;

  /**
   * This is the inner Edit Spec class.
   *
   * @param builder is the builder.
   */
  private EditSpec(EditSpecBuilder builder) {
    this.subject = builder.subject;
    this.startDate = builder.startDate;
    this.endDate = builder.endDate;
    this.startTime = builder.startTime;
    this.endTime = builder.endTime;
    this.property = builder.property;
    this.newPropValue = builder.newPropValue;
    this.type = builder.type;
    this.daysDiff = builder.daysDiff;
    this.minsDiff = builder.minsDiff;
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
   * Getter for the property field.
   *
   * @return the property of the event, or null if none
   */
  public String getProperty() {
    return this.property;
  }

  /**
   * Getter for the newPropValue field.
   *
   * @return the newPropValue of the event, or null if none
   */
  public String getNewPropValue() {
    return this.newPropValue;
  }

  /**
   * Getter for the type field.
   *
   * @return the type of the event, or null if none
   */
  public String getType() {
    return this.type;
  }

  /**
   * Getter for the daysDiff field.
   *
   * @return the days diff.
   */
  public long getDaysDiff() {
    return this.daysDiff;
  }

  /**
   * Getter for the minsDiff field.
   *
   * @return the mins diff.
   */
  public long getMinsDiff() {
    return this.minsDiff;
  }

  /**
   * The inner EditSpecBuilder class.
   */
  public static class EditSpecBuilder {
    String subject;
    LocalDate startDate;
    LocalDate endDate = null;
    LocalTime startTime;
    LocalTime endTime = null;
    String property;
    String newPropValue;
    String type;
    long daysDiff;
    long minsDiff;

    /**
     * The Edit Spec Builder constructor.
     *
     * @param type         is the type.
     * @param property     is the property.
     * @param subject      is the subject.
     * @param startDate    is the startDate.
     * @param startTime    is the start Time.
     * @param newPropValue is the new property value.
     */
    public EditSpecBuilder(String type, String property, String subject, LocalDate startDate,
                           LocalTime startTime, String newPropValue) {
      this.type = type;
      this.property = property;
      this.subject = subject;
      this.startDate = startDate;
      this.startTime = startTime;
      this.newPropValue = newPropValue;
    }

    /**
     * Function to set the endDate.
     *
     * @param endDate the end date to be set
     */
    public void endDate(LocalDate endDate) {
      this.endDate = endDate;
    }

    /**
     * Function to set the end time.
     *
     * @param endTime the end time to be set.
     */
    public void endTime(LocalTime endTime) {
      this.endTime = endTime;
    }

    /**
     * Function to set days diff.
     *
     * @param daysDiff the value of days diff with.
     */
    public void daysDiff(long daysDiff) {
      this.daysDiff = daysDiff;
    }

    /**
     * function to set mins diff.
     *
     * @param minsDiff the value to set mins diff with.
     */
    public void minsDiff(long minsDiff) {
      this.minsDiff = minsDiff;
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
    public LocalTime getEndTime() {
      return this.endTime;
    }


    /**
     * Builds the CreateSpec instance.
     *
     * @return a CreateSpec object
     */
    public EditSpec build() {
      return new EditSpec(this);
    }
  }

}


