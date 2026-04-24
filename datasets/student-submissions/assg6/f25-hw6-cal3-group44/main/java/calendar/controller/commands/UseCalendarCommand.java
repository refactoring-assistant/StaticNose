package calendar.controller.commands;

import calendar.controller.commanddata.UseCalendarCommandData;
import calendar.controller.handlers.UseCalendarHandler;
import calendar.model.interfaces.CalendarContainer;
import java.util.List;

/**
 * Performs the parsing to extract the information from the user input list.
 * This class handles parsing, while UseCalendarHandler handles the logic.
 */
public class UseCalendarCommand implements Command {
  private final UseCalendarHandler handler;

  /**
   * We are passing the calendar Manager which has all the calendars.
   *
   * @param calendarManager current calendar manager.
   */
  public UseCalendarCommand(CalendarContainer calendarManager) {
    this.handler = new UseCalendarHandler(calendarManager);
  }

  @Override
  public String execute(List<String> parsedCommand) {
    UseCalendarCommandData data = parse(parsedCommand);
    return handler.handle(data);
  }

  /**
   * Parses the command input into a UseCalendarCommandData object.
   *
   * @param parsedCommand the parsed command tokens
   * @return UseCalendarCommandData containing parsed information
   */
  public UseCalendarCommandData parse(List<String> parsedCommand) {
    if (parsedCommand.size() != 4 || !parsedCommand.get(2).equalsIgnoreCase("--name")) {
      throw new IllegalArgumentException("Error: Invalid command. "
          + "Usage: use calendar --name <name-of-calendar>");
    }
    String name = parsedCommand.get(3);
    return new UseCalendarCommandData(name);
  }
}
