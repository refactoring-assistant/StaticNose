package calendar.controller.commands;

import calendar.model.CalendarSystem;
import java.time.ZoneId;

/**
 * Command to create a new calendar.
 */
public class CreateCalendarCommand implements SystemCommand {
  private final String name;
  private final ZoneId timezone;

  /**
   * Creates a cal with a specified name and timezone.
   *
   * @param name name of the new cal
   * @param timezone timezone
   */
  public CreateCalendarCommand(String name, ZoneId timezone) {
    this.name = name;
    this.timezone = timezone;
  }

  @Override
  public String execute(CalendarSystem system) {
    try {
      system.createCalendar(name, timezone);
      return "Calendar '" + name + "' created with timezone " + timezone.getId();
    } catch (IllegalArgumentException e) {
      return "Error: " + e.getMessage();
    }
  }
}