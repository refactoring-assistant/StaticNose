package calendar.controller.commands;

import calendar.controller.CalendarController;
import calendar.controller.Command;

/**
 * Command for exporting calendar to CSV or iCal.
 */
public class ExportCalendarCommand implements Command {
  private final String[] tokens;

  /**
   * Constructs an ExportCalendarCommand.
   *
   * @param tokens the command tokens
   */
  public ExportCalendarCommand(String[] tokens) {
    this.tokens = tokens;
  }

  @Override
  public String execute(CalendarController controller) {
    try {
      if (tokens.length < 3) {
        return "Error: Invalid export command. Usage: export cal <filename>";
      }

      String fileName = tokens[2];
      String filePath = controller.exportCalendar(fileName);
      return "Calendar exported successfully to: " + filePath;
    } catch (Exception e) {
      return "Error: " + e.getMessage();
    }
  }
}