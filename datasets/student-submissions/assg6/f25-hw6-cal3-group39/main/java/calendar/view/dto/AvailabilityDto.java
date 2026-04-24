package calendar.view.dto;

import java.time.LocalDateTime;

/**
 * Below is the dto to check if the user is busy or available.
 */
public class AvailabilityDto implements AvailabilityDtoI {
  private final LocalDateTime dateTime;

  /**
   * The public constructor which takes in a date time object.
   *
   * @param dateTime the date time to check availability on.
   */
  public AvailabilityDto(LocalDateTime dateTime) {
    this.dateTime = dateTime;
  }

  /**
   * Return the set date time.
   *
   * @return the date time set.
   */
  public LocalDateTime getDateTime() {
    return dateTime;
  }
}