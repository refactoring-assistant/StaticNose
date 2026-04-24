package calendar.controller.commands;

import calendar.controller.commanddata.CreateCommandData;
import calendar.controller.handlers.CreateEventHandler;
import calendar.model.interfaces.CalendarEditable;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Executes the "create command" command from the user input.
 * Represents the command used to create the calendar events.
 * This class handles parsing, while CreateEventHandler handles the logic.
 */
public class CreateCommand implements Command {

  private final CreateEventHandler handler;
  private final DateTimeFormatter formatter;
  private final LocalTime allDayStart;
  private final LocalTime allDayEnd;

  /**
   * Constructor for creating a new event.
   * It takes in a Calendar in which event to be created.
   *
   * @param calendar the calendar in which event is added.
   */
  public CreateCommand(CalendarEditable calendar) {
    this.handler = new CreateEventHandler(calendar);
    this.formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    this.allDayStart = LocalTime.of(8, 0);
    this.allDayEnd = LocalTime.of(17, 0);
  }

  @Override
  public String execute(List<String> parsedCommand) {
    try {
      CreateCommandData data = parse(parsedCommand);
      return handler.handle(data);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Error: Invalid date format");
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Error: Invalid value for N");
    }
  }

  /**
   * Parses the command input into a CreateCommandData object.
   *
   * @param parsedCommand the parsed command tokens
   * @return CreateCommandData containing parsed information
   */
  public CreateCommandData parse(List<String> parsedCommand) {
    String subject = parsedCommand.get(2);

    if (parsedCommand.size() > 3 && parsedCommand.get(3).equals("from")) {
      return parseFromCommand(parsedCommand, subject);
    } else if (parsedCommand.size() > 3 && parsedCommand.get(3).equals("on")) {
      return parseOnCommand(parsedCommand, subject);
    }
    throw new IllegalArgumentException("Error: Invalid creation command");
  }

  private CreateCommandData parseFromCommand(List<String> parsedCommand, String subject) {
    LocalDateTime startDateTime = LocalDateTime.parse(parsedCommand.get(4), formatter);
    LocalDateTime endDateTime = LocalDateTime.parse(parsedCommand.get(6), formatter);

    if (parsedCommand.size() > 7 && parsedCommand.get(7).equals("repeats")) {
      int repeatsIndex = parsedCommand.indexOf("repeats");
      if (parsedCommand.size() < repeatsIndex + 4) {
        throw new IllegalArgumentException("Error: Invalid creation command");
      }
      String daysString = parsedCommand.get(repeatsIndex + 1);
      String repeatType = parsedCommand.get(repeatsIndex + 2);
      String repeatValue = parsedCommand.get(repeatsIndex + 3);

      return new CreateCommandData(subject, startDateTime, endDateTime,
          daysString, repeatType, repeatValue, true);
    } else {
      return new CreateCommandData(subject, startDateTime, endDateTime, true);
    }
  }

  private CreateCommandData parseOnCommand(List<String> parsedCommand, String subject) {
    String date = parsedCommand.get(4);
    LocalDateTime startDateTime = LocalDateTime.parse(date + "T" + allDayStart, formatter);
    LocalDateTime endDateTime = LocalDateTime.parse(date + "T" + allDayEnd, formatter);

    if (parsedCommand.size() > 5 && parsedCommand.get(5).equals("repeats")) {
      int repeatsIndex = parsedCommand.indexOf("repeats");
      if (parsedCommand.size() < repeatsIndex + 4) {
        throw new IllegalArgumentException("Error: Invalid creation command");
      }
      String daysString = parsedCommand.get(repeatsIndex + 1);
      String repeatType = parsedCommand.get(repeatsIndex + 2);
      String repeatValue = parsedCommand.get(repeatsIndex + 3);

      return new CreateCommandData(subject, startDateTime, endDateTime,
          daysString, repeatType, repeatValue, false);
    } else {
      return new CreateCommandData(subject, startDateTime, endDateTime, false);
    }
  }
}
