package calendar.controller.commands;

import calendar.controller.Command;
import calendar.model.CalendarModel;
import calendar.view.CalendarView;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Abstract base class for commands in the calendar application.
 * Provides a template for implementing different commands by overriding the execute method.
 */
public abstract class AbstractCommand implements Command {

  /**
   * Executes the command using the provided model and view.
   * Concrete implementations of this method should define the specific behavior of the command.
   *
   * @param model the calendar model used for data manipulation
   * @param view the calendar view used for displaying output
   * @throws IOException if an I/O error occurs during command execution
   */
  public abstract void execute(CalendarModel model, CalendarView view) throws IOException;

  /**
   * Parses a property value from a string based on the given property name.
   * Converts the value into the appropriate type based on the property.
   *
   * @param property the name of the property to be parsed
   * @param value the string value to be converted
   * @return the parsed value, either a string or LocalDateTime depending on the property
   * @throws IllegalArgumentException if the property name is unknown or invalid
   */
  protected Object parsePropertyValue(String property, String value) {
    String p = property.toLowerCase();
    switch (p) {
      case "subject":
      case "location":
      case "description":
      case "status":
        return value;
      case "start":
      case "startdatetime":
      case "end":
      case "enddatetime":
        return LocalDateTime.parse(value);

      default:
        throw new IllegalArgumentException("Unknown property: " + property);
    }
  }
}
