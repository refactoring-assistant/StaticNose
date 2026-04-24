package calendar.view.dto;

import java.time.LocalDateTime;

/**
 * Represents a read-only contract for an availabilityDto entry
 * containing a specific date and time.
 */
public interface AvailabilityDtoI {

  /**
   * Returns the date and time associated with this availability.
   *
   * @return the {@link LocalDateTime} of the availability
   */
  LocalDateTime getDateTime();
}
