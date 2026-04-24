package calendar.command.impl;

import calendar.command.CommandInterface;
import calendar.controller.CalendarManagerControllerInterface;
import java.time.ZoneId;
import java.util.Objects;

/**
 * Command to edit an existing calendar's property (name or timezone).
 * Example:
 * edit calendar --name Work --property name Personal
 * edit calendar --name Work --property timezone Europe/Paris
 */
public class EditCalendarCommand implements CommandInterface {
  private final CalendarManagerControllerInterface controller;
  private final String calendarName;
  private final String propertyName;
  private final String newValue;

  /**
   * Creates a command to edit a property of the calendar with the given name.
   */
  public EditCalendarCommand(CalendarManagerControllerInterface controller,
                             String calendarName, String propertyName, String newValue) {
    this.controller = controller;
    this.calendarName = Objects.requireNonNull(calendarName, "Calendar name cannot be null");
    this.propertyName = Objects.requireNonNull(propertyName, "Property name cannot be null");
    this.newValue = Objects.requireNonNull(newValue, "New value cannot be null");
  }

  @Override
  public String execute() {
    try {
      switch (propertyName.toLowerCase()) {
        case "name":
          controller.editCalendarName(calendarName, newValue);
          return "Calendar name changed from '" + calendarName + "' to '" + newValue + "'";
        case "timezone":
          ZoneId zone = ZoneId.of(newValue);
          controller.changeCalendarTimezone(calendarName, zone);
          return "Calendar '" + calendarName + "' timezone changed to " + zone.getId();
        default:
          return "Error: Unknown property '" + propertyName + "'";
      }
    } catch (Exception e) {
      return "Error: " + e.getMessage();
    }
  }

  @Override
  public String getDescription() {
    return "Edit calendar '" + calendarName + "' property '" + propertyName
        + "' to '" + newValue + "'";
  }
}
