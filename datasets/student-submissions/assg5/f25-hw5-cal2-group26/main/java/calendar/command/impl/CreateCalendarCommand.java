package calendar.command.impl;

import calendar.command.CommandInterface;
import calendar.controller.CalendarManagerControllerInterface;
import java.time.ZoneId;

/**
 * Command to create a new calendar with a unique name and timezone.
 * Example: create calendar --name Work --timezone America/New_York
 */
public class CreateCalendarCommand implements CommandInterface {
  private final CalendarManagerControllerInterface controller;
  private final String name;
  private final ZoneId zone;

  /**
   * Constructs a command to create a new calendar.
   *
   * @param controller is the controller that the method is in
   * @param name unique name for the calendar.
   * @param zone timezone for the calendar.
   */
  public CreateCalendarCommand(CalendarManagerControllerInterface controller, String name,
                               ZoneId zone) {
    this.controller = controller;
    this.name = name;
    this.zone = zone;
  }

  @Override
  public String execute() {
    try {
      controller.createCalendar(name, zone);
      return "Calendar '" + name + "' created with timezone " + zone.getId();
    } catch (RuntimeException e) {
      return "Error: " + e.getMessage();
    }
  }

  @Override
  public String getDescription() {
    return "Create calendar: " + name + " with timezone " + zone.getId();
  }
}
