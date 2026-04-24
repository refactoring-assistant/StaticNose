package calendar.controller;

import calendar.command.CalendarCommand;
import calendar.command.CopyEventSingleCommand;
import calendar.command.CopyEventsBetweenCommand;
import calendar.command.CopyEventsOnCommand;
import calendar.command.CreateCalendarCommand;
import calendar.command.CreateEventCommand;
import calendar.command.EditCalendarCommand;
import calendar.command.EditEventCommand;
import calendar.command.ExportCalendarCommand;
import calendar.command.HelpCommand;
import calendar.command.ListCalendarsCommand;
import calendar.command.PrintEventsCommand;
import calendar.command.ShowStatusCommand;
import calendar.command.UseCalendarCommand;
import calendar.service.CalendarService;
import calendar.view.textbased.CalendarView;
import java.io.InputStream;
import java.time.DateTimeException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A concrete implementation of the {@code CalendarController} that processes
 * user commands
 * read from an {@code InputStream} and organizes interactions between the
 * {@code CalendarService} and {@code CalendarView}. This controller provides
 * support for
 * **multi-calendar operations**, parsing various commands related to creating,
 * editing,
 * listing, and exporting calendars and their associated events. It uses a
 * **command pattern**
 * where specific parser methods are mapped to command prefixes to transform raw
 * input strings
 * into executable {@code CalendarCommand} objects.
 */
public class CalController implements CalendarController {

  private final Scanner scanner;
  private final CalendarService service;
  private final CalendarView view;
  private final Map<String, CommandParser> commandParsers;

  /**
   * Constructs a new CalController.
   *
   * @param service  The service to interact with.
   * @param view     The view to send output to.
   * @param inStream The input stream to read commands from.
   */
  public CalController(CalendarService service, CalendarView view, InputStream inStream) {
    this.scanner = new Scanner(inStream);
    this.service = service;
    this.view = view;
    this.commandParsers = initializeCommandParsers();
  }

  /**
   * Initializes the map of command parsers.
   *
   * @return A map of command prefixes to their parsers.
   */
  private Map<String, CommandParser> initializeCommandParsers() {
    Map<String, CommandParser> parsers = new HashMap<>();

    parsers.put("create calendar ", this::parseCreateCalendar);
    parsers.put("use calendar ", this::parseUseCalendar);
    parsers.put("edit calendar ", this::parseEditCalendar);
    parsers.put("list calendars", input -> new ListCalendarsCommand());

    parsers.put("create event ", this::parseCreateEvent);
    parsers.put("edit event ", input -> parseEditEvent(input, true, false));
    parsers.put("edit events ", input -> parseEditEvent(input, false, false));
    parsers.put("edit series", input -> parseEditEvent(input, false, false));
    parsers.put("print events on", this::parsePrintOn);
    parsers.put("print events from", this::parsePrintFromTo);
    parsers.put("export cal", this::parseExport);
    parsers.put("show status on", this::parseShowStatus);
    parsers.put("help", input -> new HelpCommand());

    parsers.put("copy event ", this::parseCopyEventSingle);
    parsers.put("copy events on ", this::parseCopyEventsOn);
    parsers.put("copy events between ", this::parseCopyEventsBetween);

    return parsers;
  }

  /**
   * Parses: copy event {name} on {datetime} --target {calName} to {datetime}.
   */
  private CalendarCommand parseCopyEventSingle(String input) {
    List<String> tokens = tokenize(input);
    if (tokens.size() < 9 || !tokens.get(3).equals("on")
        || !tokens.get(5).equals("--target") || !tokens.get(7).equals("to")) {
      throw new IllegalArgumentException(
          "Invalid format. Use: copy event <name> on <datetime> --target <calName> to <datetime>");
    }
    String eventName = tokens.get(2);
    String onTime = tokens.get(4);
    String targetCal = tokens.get(6);
    String toTime = tokens.get(8);
    return new CopyEventSingleCommand(eventName, onTime, targetCal, toTime);
  }

  /**
   * Parses: copy events on {date} --target {calName} to {date}.
   */
  private CalendarCommand parseCopyEventsOn(String input) {
    List<String> tokens = tokenize(input);
    if (tokens.size() < 8 || !tokens.get(2).equals("on")
        || !tokens.get(4).equals("--target") || !tokens.get(6).equals("to")) {
      throw new IllegalArgumentException(
          "Invalid format. Use: copy events on <date> --target <calName> to <date>");
    }
    String onDate = tokens.get(3);
    String targetCal = tokens.get(5);
    String toDate = tokens.get(7);
    return new CopyEventsOnCommand(onDate, targetCal, toDate);
  }

  /**
   * Parses: copy events between {date} and {date} --target {calName} to {date}.
   */
  private CalendarCommand parseCopyEventsBetween(String input) {
    List<String> tokens = tokenize(input);
    if (tokens.size() < 10 || !tokens.get(2).equals("between")
        || !tokens.get(4).equals("and") || !tokens.get(6).equals("--target")
        || !tokens.get(8).equals("to")) {
      throw new IllegalArgumentException(
          "Invalid format. Use: copy events between "
              + "<date> and <date> --target <calName> to <date>");
    }
    String startDate = tokens.get(3);
    String endDate = tokens.get(5);
    String targetCal = tokens.get(7);
    String toDate = tokens.get(9);
    return new CopyEventsBetweenCommand(startDate, endDate, targetCal, toDate);
  }

  /**
   * Tokenizes an input string, respecting quoted phrases.
   */
  private List<String> tokenize(String input) {
    Pattern pattern = Pattern.compile("\"([^\"]+)\"|(\\S+)");
    Matcher matcher = pattern.matcher(input);
    List<String> tokens = new ArrayList<>();
    while (matcher.find()) {
      if (matcher.group(1) != null) {
        tokens.add(matcher.group(1));
      } else {
        tokens.add(matcher.group(2));
      }
    }
    return tokens;
  }

  /**
   * Parses keyword-based arguments from tokens.
   */
  private Map<String, String> parseKeywordArguments(List<String> tokens, int startIndex) {
    Map<String, String> params = new HashMap<>();
    try {
      for (int i = startIndex; i < tokens.size(); i += 2) {
        String key = tokens.get(i);
        String value = tokens.get(i + 1);
        params.put(key, value);
      }
    } catch (IndexOutOfBoundsException e) {
      // Keyword provided without value - let the command handle this
    }
    return params;
  }

  /**
   * Parses a "create calendar" command.
   */
  private CalendarCommand parseCreateCalendar(String input) {
    List<String> tokens = tokenize(input);
    if (tokens.size() < 6) {
      throw new IllegalArgumentException(
          "Invalid create calendar format. "
              + "Use: create calendar --name <name> --timezone <timezone>");
    }
    if (!tokens.get(2).equals("--name")) {
      throw new IllegalArgumentException(
          "Missing or misplaced '--name' flag."
              + " Use: create calendar --name <name> --timezone <timezone>");
    }
    if (!tokens.get(4).equals("--timezone")) {
      throw new IllegalArgumentException(
          "Missing or misplaced '--timezone' flag."
              + " Use: create calendar --name <name> --timezone <timezone>");
    }
    String name = tokens.get(3);
    String timezone = tokens.get(5);
    return new CreateCalendarCommand(name, timezone);
  }

  /**
   * Parses a "use calendar" command.
   */
  private CalendarCommand parseUseCalendar(String input) {
    List<String> tokens = tokenize(input);
    if (tokens.size() < 4) {
      throw new IllegalArgumentException(
          "Invalid use calendar format. Use: use calendar --name <name>");
    }
    if (!tokens.get(2).equals("--name")) {
      throw new IllegalArgumentException(
          "Missing or misplaced '--name' flag. Use: use calendar --name <name>");
    }
    String name = tokens.get(3);
    return new UseCalendarCommand(name);
  }

  /**
   * Parses an "edit calendar" command.
   */
  private CalendarCommand parseEditCalendar(String input) {
    List<String> tokens = tokenize(input);
    if (tokens.size() < 7) {
      throw new IllegalArgumentException(
          "Invalid edit calendar format."
              + " Use: edit calendar --name <name> --property <prop> <value>");
    }
    if (!tokens.get(2).equals("--name")) {
      throw new IllegalArgumentException(
          "Missing or misplaced '--name' flag."
              + " Use: edit calendar --name <name> --property <prop> <value>");
    }
    if (!tokens.get(4).equals("--property")) {
      throw new IllegalArgumentException(
          "Missing or misplaced '--property' flag."
              + " Use: edit calendar --name <name> --property <prop> <value>");
    }
    String calendarName = tokens.get(3);
    String property = tokens.get(5);
    String newValue = tokens.get(6);
    return new EditCalendarCommand(property, calendarName, newValue);
  }

  /**
   * Parses a "create event" command.
   */
  private CalendarCommand parseCreateEvent(String input) {
    List<String> tokens = tokenize(input);
    if (tokens.size() < 3) {
      throw new IllegalArgumentException("Invalid create event command format.");
    }
    String subject = tokens.get(2);
    Map<String, String> params = parseKeywordArguments(tokens, 3);
    return new CreateEventCommand(subject, params);
  }

  /**
   * Parses an "edit" command (event/events/series).
   */
  private CalendarCommand parseEditEvent(String input, boolean singleEventUpdate,
      boolean updateAll) {
    List<String> tokens = tokenize(input);
    if (tokens.size() < 4) {
      throw new IllegalArgumentException("Invalid edit command format.");
    }
    String property = tokens.get(2);
    String subject = tokens.get(3);
    Map<String, String> params = parseKeywordArguments(tokens, 4);
    return new EditEventCommand(property, subject, params, singleEventUpdate, updateAll);
  }

  /**
   * Parses a "print events on" command.
   */
  private CalendarCommand parsePrintOn(String input) {
    List<String> tokens = tokenize(input);
    if (tokens.size() < 4) {
      throw new IllegalArgumentException("Invalid print on command format.");
    }
    Map<String, String> params = new HashMap<>();
    params.put("on", tokens.get(3));
    return new PrintEventsCommand(params, true);
  }

  /**
   * Parses a "print events from...to" command.
   */
  private CalendarCommand parsePrintFromTo(String input) {
    List<String> tokens = tokenize(input);
    Map<String, String> params = parseKeywordArguments(tokens, 2);
    return new PrintEventsCommand(params, false);
  }

  /**
   * Parses an "export cal" command.
   */
  private CalendarCommand parseExport(String input) {
    List<String> tokens = tokenize(input);
    if (tokens.size() < 3) {
      throw new IllegalArgumentException("Invalid export command format.");
    }
    String fileName = tokens.get(2);
    return new ExportCalendarCommand(fileName);
  }

  /**
   * Parses a "show status on" command.
   */
  private CalendarCommand parseShowStatus(String input) {
    List<String> tokens = tokenize(input);
    if (tokens.size() < 4) {
      throw new IllegalArgumentException("Invalid show status command format.");
    }
    String dateTime = tokens.get(3);
    return new ShowStatusCommand(dateTime);
  }

  @Override
  public void run() {
    boolean quit = false;

    while (!quit && scanner.hasNextLine()) {
      view.showPrompt();
      String input = scanner.nextLine().trim();

      if (input.isEmpty()) {
        continue;
      }

      if (input.equals("exit")) {
        quit = true;
        continue;
      }

      try {
        CalendarCommand command = parseCommand(input);
        command.execute(service, view);
      } catch (IllegalArgumentException | IllegalStateException | DateTimeException e) {
        view.showError("Error in command '" + input + "': " + e.getMessage());
      } catch (Exception e) {
        view.showError("An unexpected error occurred"
            + " while running '" + input + "': " + e.getMessage());
      }
    }

    if (!quit) {
      view.showError("Error: Command file finished without 'exit' command.");
    }
  }

  /**
   * Parses the input string and returns the appropriate command.
   */
  private CalendarCommand parseCommand(String input) throws IllegalArgumentException {
    for (Map.Entry<String, CommandParser> entry : commandParsers.entrySet()) {
      if (input.startsWith(entry.getKey())) {
        return entry.getValue().parse(input);
      }
    }
    throw new IllegalArgumentException("Invalid command. Type 'help' to see all commands.");
  }

  /**
   * Functional interface for parsing commands.
   */
  @FunctionalInterface
  private interface CommandParser {
    CalendarCommand parse(String input) throws IllegalArgumentException;
  }
}