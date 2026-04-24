package calendar.view.dto;

import java.time.LocalDate;

/**
 * Below is the select day data transfer object.
 */
public class SelectDayDto implements SelectDayDtoI {
  LocalDate date;

  /**
   * Below constructor is the data transfer object of the select day functionality.
   *
   * @param date the date passed
   */
  public SelectDayDto(LocalDate date) {
    this.date = date;
  }

  /**
   * Returns the selected date.
   *
   * @return the {@link LocalDate} representing the selected day
   */
  @Override
  public LocalDate date() {
    return this.date;
  }

}