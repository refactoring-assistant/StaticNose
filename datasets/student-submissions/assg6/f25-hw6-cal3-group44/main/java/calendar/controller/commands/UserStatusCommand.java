package calendar.controller.commands;

import calendar.controller.commanddata.UserStatusCommandData;
import calendar.controller.handlers.UserStatusHandler;
import calendar.model.interfaces.CalendarEditable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Executes the "show status" command from the user input.
 * Represents the command used to print events for a given date and time.
 * This class handles parsing, while UserStatusHandler handles the logic.
 */
public class UserStatusCommand implements Command {

  private final UserStatusHandler handler;

  /**
   * We are passing the active calendar in the constructor.
   * It is the calendar on which the current operation is to be performed.
   *
   * @param calendarModel current active calendar.
   */
  public UserStatusCommand(CalendarEditable calendarModel) {
    this.handler = new UserStatusHandler(calendarModel);
  }

  /**
   * Parses the command and delegates to the handler.
   *
   * @throws IllegalArgumentException if the command syntax is of invalid length or contains
   *                                  invalid values.
   */
  @Override
  public String execute(List<String> parsedCommand) {
    UserStatusCommandData data = parse(parsedCommand);
    return handler.handle(data);
  }

  /**
   * Parses the command input into a UserStatusCommandData object.
   *
   * @param parsedCommand the parsed command tokens
   * @return UserStatusCommandData containing parsed information
   */
  public UserStatusCommandData parse(List<String> parsedCommand) {
    if (parsedCommand.size() != 4 || !parsedCommand.get(2).equalsIgnoreCase("on")) {
      throw new IllegalArgumentException("Error: Invalid show status command");
    }
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    LocalDateTime dateTime = LocalDateTime.parse(parsedCommand.get(3), formatter);
    return new UserStatusCommandData(dateTime);
  }
}
