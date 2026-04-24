package calendar.view.dialog;

import java.util.Objects;

/**
 * Represents the result of a calendar creation dialog.
 * This is a data transfer object (DTO) that encapsulates
 * the information needed to create a calendar.
 * Uses only primitives and Strings to maintain MVC separation.
 */
public class CalendarDialogResult {
  private final String name;
  private final String timezone;

  /**
   * Constructs a calendar dialog result.
   *
   * @param name     the calendar name
   * @param timezone the timezone string (e.g., "America/New_York")
   * @throws IllegalArgumentException if name or timezone is null
   */
  public CalendarDialogResult(String name, String timezone) {
    this.name = Objects.requireNonNull(name, "Calendar name cannot be null");
    this.timezone = Objects.requireNonNull(timezone, "Timezone cannot be null");
  }

  /**
   * Gets the calendar name.
   *
   * @return the calendar name
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the calendar timezone string.
   *
   * @return the calendar timezone string
   */
  public String getTimezone() {
    return timezone;
  }
}

