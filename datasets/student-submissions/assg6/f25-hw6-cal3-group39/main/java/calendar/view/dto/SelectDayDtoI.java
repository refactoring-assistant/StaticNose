package calendar.view.dto;

import java.time.LocalDate;

/**
 * Represents a read-only contract for selecting a specific day in the calendar.
 * Implementations of this interface provide the date that the user wishes
 * to select or operate on.
 */
public interface SelectDayDtoI {

  /**
   * Returns the selected date.
   *
   * @return the {@link LocalDate} representing the selected day
   */
  LocalDate date();
}
