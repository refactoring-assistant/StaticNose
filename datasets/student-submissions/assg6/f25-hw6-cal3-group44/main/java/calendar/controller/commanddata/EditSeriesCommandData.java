package calendar.controller.commanddata;

import java.time.LocalDateTime;

/**
 * Data transfer object for EditSeriesCommand parsed data.
 */
public class EditSeriesCommandData {
  private final String propertyToUpdate;
  private final String subject;
  private final LocalDateTime startDateTime;
  private final String newPropertyValue;

  /**
   * Constructor for EditSeriesCommandData.
   *
   * @param propertyToUpdate the property to update
   * @param subject          the event subject
   * @param startDateTime    the start date and time
   * @param newPropertyValue the new value for the property
   */
  public EditSeriesCommandData(String propertyToUpdate, String subject,
                               LocalDateTime startDateTime, String newPropertyValue) {
    this.propertyToUpdate = propertyToUpdate;
    this.subject = subject;
    this.startDateTime = startDateTime;
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

  public String getNewPropertyValue() {
    return newPropertyValue;
  }
}

