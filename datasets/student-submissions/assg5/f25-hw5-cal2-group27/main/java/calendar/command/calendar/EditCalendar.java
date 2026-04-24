package calendar.command.calendar;

import calendar.command.Command;
import calendar.model.CalendarManager;
import calendar.model.MyCalendar;
import calendar.view.CalendarTextView;
import java.time.ZoneId;
import java.util.Optional;

/**
 * Command to edit a calendar's properties.
 */
public class EditCalendar implements Command {

  private final String name;
  private final String property;
  private final String newValue;

  /**
   * Constructs a new EditCalendar command.
   *
   * @param name     the name of the calendar to edit
   * @param property the property to edit
   * @param newValue the new value for the property
   */
  public EditCalendar(String name, String property, String newValue) {
    this.name = name;
    this.property = property;
    this.newValue = newValue;
  }

  @Override
  public void execute(CalendarManager manager, CalendarTextView view) {
    Optional<MyCalendar> maybeCalendar = manager.getCalendar(name);
    if (maybeCalendar.isEmpty()) {
      view.printMessage("Error: Calendar '" + name + "' not found.");
      return;
    }
    MyCalendar calendar = maybeCalendar.get();

    try {
      switch (property.toLowerCase()) {
        case "name":
          manager.renameCalendar(name, newValue);
          view.printMessage("Calendar '" + name + "' renamed to '" + newValue + "'.");
          break;
        case "timezone":
          ZoneId newZoneId = ZoneId.of(newValue);
          calendar.setZoneId(newZoneId);
          view.printMessage("Timezone for calendar '" + name + "' updated to '" + newValue + "'.");
          break;
        default:
          view.printMessage("Error: Unknown property '" + property
                  + "'. Supported properties are: name, timezone.");
      }
    } catch (Exception e) {
      view.printMessage("Error: " + e.getMessage());
    }
  }
}
