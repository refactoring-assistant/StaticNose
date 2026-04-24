package controller.parser;

import static model.CalendarConstants.ALL_DAY_END;
import static model.CalendarConstants.ALL_DAY_START;
import static model.CalendarConstants.DATETIME_FORMATTER;
import static model.CalendarConstants.DATE_FORMATTER;

import controller.command.Command;
import controller.command.CreateSeriesCommand;
import controller.command.CreateSingleEventCommand;
import controller.command.EditEventCommand;
import controller.command.ExitCommand;
import controller.command.ExportCommand;
import controller.command.PrintEventsCommand;
import controller.command.PrintEventsRangeCommand;
import controller.command.ShowStatusCommand;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import model.EditScope;


/**
 * Parses command strings and creates appropriate Command objects using a registry pattern.
 * All date/time parsing assumes Eastern Standard Time (EST).
 */
public class CommandParser {

  private static class CommandPattern {
    final Pattern pattern;
    final CommandFactory factory;

    CommandPattern(String regex, CommandFactory factory) {
      this.pattern = Pattern.compile(regex);
      this.factory = factory;
    }
  }

  private interface CommandFactory {
    Command create(Matcher matcher) throws Exception;
  }

  private final List<CommandPattern> commandRegistry;

  /**
   * Constructs a CommandParser and initializes the command registry with all
   * supported command patterns and their corresponding factories.
   */
  public CommandParser() {
    this.commandRegistry = new ArrayList<>();
    registerCommands();
  }

  /**
   * Validates and extracts the subject from the matched string.
   * Multi-word subjects MUST be in quotes, single-word subjects must NOT be in quotes.
   * Supports escaped quotes within quoted subjects using \".
   *
   * @param subject The subject string (may or may not have quotes)
   * @return The cleaned subject without quotes and with escaped quotes processed
   * @throws IllegalArgumentException if validation fails
   */
  private String validateAndExtractSubject(String subject) {
    subject = subject.trim();

    boolean hasQuotes = subject.startsWith("\"") && subject.endsWith("\"");

    if (hasQuotes) {
      String unquoted = subject.substring(1, subject.length() - 1);

      unquoted = processEscapedQuotes(unquoted);

      unquoted = unquoted.trim();

      if (!unquoted.contains(" ")) {
        throw new IllegalArgumentException(
            "Single-word subjects should not be enclosed in quotes: " + subject);
      }

      return unquoted;
    }
    return subject;
  }

  /**
   * Process escaped characters within a quoted string.
   *
   * @param text The text with potential escape sequences
   * @return The text with escape sequences processed
   */
  private String processEscapedQuotes(String text) {
    StringBuilder result = new StringBuilder();
    int i = 0;
    while (i < text.length()) {
      if (i < text.length() - 1 && text.charAt(i) == '\\' && text.charAt(i + 1) == '"') {
        result.append('"');
        i += 2;
      } else {
        result.append(text.charAt(i));
        i++;
      }
    }
    return result.toString();
  }

  private void registerCommands() {

    register("^exit$",
        m -> new ExitCommand());



    register("create event (\"(?:[^\\\\\"]|\\\\.)+\"|\\S+) from (\\S+) to (\\S+)$",
        m -> CreateSingleEventCommand.builder()
            .subject(validateAndExtractSubject(m.group(1)))
            .startDateTime(parseDateTime(m.group(2)))
            .endDateTime(parseDateTime(m.group(3)))
            .description(null)
            .location(null)
            .isPublic(true)
            .build());

    register("create event (\"(?:[^\\\\\"]|\\\\.)+\"|\\S+) from (\\S+)$",
        m -> {
          LocalDateTime startDateTime = parseDateTime(m.group(2));
          LocalDate date = startDateTime.toLocalDate();
          return CreateSingleEventCommand.builder()
              .subject(validateAndExtractSubject(m.group(1)))
              .startDateTime(date.atTime(ALL_DAY_START))
              .endDateTime(date.atTime(ALL_DAY_END))
              .description(null)
              .location(null)
              .isPublic(true)
              .build();
        });

    register("create event (\"(?:[^\\\\\"]|\\\\.)+\"|\\S+) on (\\S+)$",
        m -> {
          LocalDate date = parseDate(m.group(2));
          return CreateSingleEventCommand.builder()
              .subject(validateAndExtractSubject(m.group(1)))
              .startDateTime(date.atTime(ALL_DAY_START))
              .endDateTime(date.atTime(ALL_DAY_END))
              .description(null)
              .location(null)
              .isPublic(true)
              .build();
        });

    register(
        "create event (\"(?:[^\\\\\"]|\\\\.)+\"|\\S+) from (\\S+) to (\\S+) repeats "
            + "([MTWRFSU]+) for (\\d+) times$",
        m -> CreateSeriesCommand.builder()
            .subject(validateAndExtractSubject(m.group(1)))
            .startDateTime(parseDateTime(m.group(2)))
            .endDateTime(parseDateTime(m.group(3)))
            .weekdays(m.group(4))
            .occurrenceCount(Integer.parseInt(m.group(5)))
            .build());

    register(
        "create event (\"(?:[^\\\\\"]|\\\\.)+\"|\\S+) from (\\S+) to (\\S+) repeats "
            + "([MTWRFSU]+) until (\\S+)$",
        m -> CreateSeriesCommand.builder()
            .subject(validateAndExtractSubject(m.group(1)))
            .startDateTime(parseDateTime(m.group(2)))
            .endDateTime(parseDateTime(m.group(3)))
            .weekdays(m.group(4))
            .untilDate(parseDate(m.group(5)))
            .build());

    register(
        "create event (\"(?:[^\\\\\"]|\\\\.)+\"|\\S+) on (\\S+) repeats ([MTWRFSU]+) "
            + "for (\\d+) times$",
        m -> {
          LocalDate date = parseDate(m.group(2));
          return CreateSeriesCommand.builder()
              .subject(validateAndExtractSubject(m.group(1)))
              .startDateTime(date.atTime(ALL_DAY_START))
              .endDateTime(date.atTime(ALL_DAY_END))
              .weekdays(m.group(3))
              .occurrenceCount(Integer.parseInt(m.group(4)))
              .build();
        });

    register(
        "create event (\"(?:[^\\\\\"]|\\\\.)+\"|\\S+) on (\\S+) repeats ([MTWRFSU]+) "
            + "until (\\S+)$",
        m -> {
          LocalDate date = parseDate(m.group(2));
          return CreateSeriesCommand.builder()
              .subject(validateAndExtractSubject(m.group(1)))
              .startDateTime(date.atTime(ALL_DAY_START))
              .endDateTime(date.atTime(ALL_DAY_END))
              .weekdays(m.group(3))
              .untilDate(parseDate(m.group(4)))
              .build();
        });


    register(
        "edit event (subject|start|end|description|location|status) "
            + "(\"(?:[^\\\\\"]|\\\\.)+\"|\\S+) from (\\S+) to (\\S+) with (.+)$",
        m -> new EditEventCommand(
            validateAndExtractSubject(m.group(2)),
            parseDateTime(m.group(3)),
            parseDateTime(m.group(4)),
            m.group(1),
            m.group(5).trim(),
            EditScope.SINGLE
        ));

    register("edit events (\\w+) (\"(?:[^\\\\\"]|\\\\.)+\"|\\S+) from (\\S+) with (.+)$",
        m -> new EditEventCommand(
            validateAndExtractSubject(m.group(2)),
            parseDateTime(m.group(3)),
            null,
            m.group(1),
            m.group(4).trim(),
            EditScope.FROM_THIS
        ));

    register("edit series (\\w+) (\"(?:[^\\\\\"]|\\\\.)+\"|\\S+) from (\\S+) with (.+)$",
        m -> new EditEventCommand(
            validateAndExtractSubject(m.group(2)),
            parseDateTime(m.group(3)),
            null,
            m.group(1),
            m.group(4).trim(),
            EditScope.ALL_IN_SERIES
        ));


    register("print events on (\\S+)$",
        m -> new PrintEventsCommand(parseDate(m.group(1))));

    register("print events from (\\S+) to (\\S+)$",
        m -> new PrintEventsRangeCommand(
            parseDateTime(m.group(1)),
            parseDateTime(m.group(2))
        ));


    register("export cal (.+)$",
        m -> new ExportCommand(m.group(1).trim()));

    register("show status on (\\S+)$",
        m -> new ShowStatusCommand(parseDateTime(m.group(1))));
  }

  /**
   * Registers a command pattern with its associated factory in the command registry.
   *
   * @param regex the regular expression pattern to match command strings
   * @param factory the factory that creates Command instances for matching patterns
   */
  private void register(String regex, CommandFactory factory) {
    commandRegistry.add(new CommandPattern(regex, factory));
  }

  /**
   * Parses a command string and returns the corresponding Command object.
   * Iterates through registered patterns to find a match and create the appropriate command.
   *
   * @param commandString the user input command to parse
   * @return the Command object corresponding to the input string
   * @throws InvalidCommandException if the command is empty, malformed, or unrecognized
   */
  public Command parse(String commandString) throws InvalidCommandException {
    if (commandString == null || commandString.trim().isEmpty()) {
      throw new InvalidCommandException("Empty command");
    }
    commandString = commandString.trim();

    for (CommandPattern commandPattern : commandRegistry) {
      Matcher matcher = commandPattern.pattern.matcher(commandString);
      if (matcher.matches()) {
        try {
          return commandPattern.factory.create(matcher);
        } catch (Exception e) {
          throw new InvalidCommandException(
              "Error parsing command '" + commandString + "': " + e.getMessage());
        }
      }
    }

    throw new InvalidCommandException("Unknown command: '" + commandString + "'");
  }

  /**
   * Parses a date string in YYYY-MM-DD format to a LocalDate object.
   *
   * @param dateStr the date string to parse
   * @return the parsed LocalDate object
   * @throws IllegalArgumentException if the date format is invalid
   */
  private LocalDate parseDate(String dateStr) {
    try {
      return LocalDate.parse(dateStr, DATE_FORMATTER);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid date format. Expected: YYYY-MM-DD");
    }
  }

  /**
   * Parses a datetime string in YYYY-MM-DDThh:mm format to a LocalDateTime object.
   *
   * @param dateTimeStr the datetime string to parse
   * @return the parsed LocalDateTime object
   * @throws IllegalArgumentException if the datetime format is invalid
   */
  private LocalDateTime parseDateTime(String dateTimeStr) {
    try {
      return LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid datetime format. Expected: YYYY-MM-DDThh:mm");
    }
  }

  /**
   * Exception thrown when a command string cannot be parsed or is invalid.
   */
  public static class InvalidCommandException extends Exception {
    /**
     * Constructs an InvalidCommandException with the specified error message.
     *
     * @param message the detailed error message explaining why the command is invalid
     */
    public InvalidCommandException(String message) {
      super(message);
    }
  }
}