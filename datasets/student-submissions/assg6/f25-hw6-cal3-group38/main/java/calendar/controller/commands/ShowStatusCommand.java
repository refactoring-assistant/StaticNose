package calendar.controller.commands;

import calendar.controller.CalendarController;
import calendar.controller.Command;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Command for showing busy status at a specific time.
 */
public class ShowStatusCommand implements Command {
  private final String[] tokens;

  /**
   * Constructs a ShowStatusCommand.
   *
   * @param tokens the command tokens
   */
  public ShowStatusCommand(String[] tokens) {
    this.tokens = tokens;
  }

  @Override
  public String execute(CalendarController controller) {
    try {
      if (tokens.length < 4 || !"at".equals(tokens[2])) {
        return "Error: Invalid status command. Usage: show status at <datetime>";
      }

      LocalDateTime dateTime = parseDateTime(tokens[3]);
      boolean isBusy = controller.isBusyAt(dateTime);
      return isBusy ? "Busy" : "Free";
    } catch (Exception e) {
      return "Error: " + e.getMessage();
    }
  }

  private LocalDateTime parseDateTime(String dateTimeStr) {
    try {
      return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid date-time format: " + dateTimeStr);
    }
  }
}