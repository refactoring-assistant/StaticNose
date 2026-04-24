package calendar.controller.commands;

import calendar.controller.commanddata.PrintCommandData;
import calendar.controller.handlers.PrintEventHandler;
import calendar.model.interfaces.CalendarEditable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Executes the "print events" command from the user input.
 * Represents the command used to print events for a given date and time.
 * This class handles parsing, while PrintEventHandler handles the logic.
 */
public class PrintCommand implements Command {

  private final PrintEventHandler handler;

  /**
   * We are passing the active calendar in the constructor.
   * It is the calendar on which the current operation is to be performed.
   *
   * @param calendar current active calendar.
   */
  public PrintCommand(CalendarEditable calendar) {
    this.handler = new PrintEventHandler(calendar);
  }

  /**
   * Parses the command and delegates to the handler.
   *
   * @param parsedCommand list of user input values.
   * @return a string to print.
   */
  @Override
  public String execute(List<String> parsedCommand) {
    PrintCommandData data = parse(parsedCommand);
    return handler.handle(data);
  }

  /**
   * Parses the command input into a PrintCommandData object.
   *
   * @param parsedCommand the parsed command tokens
   * @return PrintCommandData containing parsed information
   */
  public PrintCommandData parse(List<String> parsedCommand) {
    LocalDateTime startDateTime;
    LocalDateTime endDateTime;
    boolean isOnCommand;

    if (parsedCommand.contains("on") && parsedCommand.size() == 4) {
      LocalDate localDate = LocalDate.parse(parsedCommand.get(3),
          DateTimeFormatter.ofPattern("yyyy-MM-dd"));
      startDateTime = localDate.atStartOfDay();
      endDateTime = localDate.atTime(23, 59);
      isOnCommand = true;
    } else if (parsedCommand.contains("from")
        && parsedCommand.size() == 6 && parsedCommand.get(4).equals("to")) {
      startDateTime = LocalDateTime.parse(parsedCommand.get(3),
          DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
      endDateTime = LocalDateTime.parse(parsedCommand.get(5),
          DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
      if (endDateTime.isBefore(startDateTime)) {
        throw new IllegalArgumentException("Error: End date cannot be before start date");
      }
      isOnCommand = false;
    } else {
      throw new IllegalArgumentException("Error: Invalid print command");
    }

    return new PrintCommandData(startDateTime, endDateTime, isOnCommand);
  }
}
