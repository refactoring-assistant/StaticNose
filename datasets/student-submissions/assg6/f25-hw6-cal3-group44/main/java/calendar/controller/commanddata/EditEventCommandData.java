package calendar.controller.commanddata;

import java.time.LocalDateTime;

/**
 * Data transfer object for EditEventCommand parsed data.
 */
public class EditEventCommandData {
  private final String propertyToUpdate;
  private final String subject;
  private final LocalDateTime startDateTime;
  private final LocalDateTime endDateTime;
  private final String newPropertyValue;

  /**
   * Constructor for EditEventCommandData.
   *
   * @param propertyToUpdate the property to update
   * @param subject          the event subject
   * @param startDateTime    the start date and time
   * @param endDateTime      the end date and time
   * @param newPropertyValue the new value for the property
   */
  public EditEventCommandData(String propertyToUpdate, String subject,
                              LocalDateTime startDateTime, LocalDateTime endDateTime,
                              String newPropertyValue) {
    this.propertyToUpdate = propertyToUpdate;
    this.subject = subject;
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
    this.newPropertyValue = newPropertyValue;
  }

  public String getPropertyToUpdate() {
    return propertyToUpdate;
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

  public String getNewPropertyValue() {
    return newPropertyValue;
  }
}

