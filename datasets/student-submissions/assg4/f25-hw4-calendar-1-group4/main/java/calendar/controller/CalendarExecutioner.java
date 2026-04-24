package calendar.controller;

import calendar.model.Icalendar;
import calendar.view.IcalendarView;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Controller responsible for executing calendar commands in both
 * interactive and headless modes. It parses user input, maps it to
 * corresponding command objects, and delegates execution to them.
 */
public class CalendarExecutioner implements IcalendarExecution {

  private final Icalendar calendarModel;
  private final IcalendarView calendarView;
  private final Map<String, Function<String[], Command>> commands = new HashMap<>();
  private final Readable in;

  /**
   * Constructs a new CalendarExecutioner with the given model and view.
   * Initializes supported commands and their respective factory functions.
   *
   * @param model the calendar model instance
   * @param view the view instance used for output
   */
  public CalendarExecutioner(Icalendar model, IcalendarView view, Readable in) {
    this.calendarModel = model;
    this.calendarView = view;
    this.in = in;
    commands.put("create", args -> new Create(args, calendarModel));
    commands.put("edit", args -> new Edit(args, calendarModel));
    commands.put("print", args -> new Print(args, calendarModel));
    commands.put("export", args -> new Export(args, calendarModel));
    commands.put("show", args -> new ShowStatus(args, calendarModel));
    commands.put("exit", args -> () -> {
      calendarView.showOutput("Calendar exited.");

      return "EXIT_SIGNAL";
    });
  }

  /**
   * Starts the execution of the calendar application in either interactive
   * or headless mode based on the given parameters.
   *
   * @param mode the mode to start in ("interactive" or "headless")
   * @param filePath optional file path for headless mode commands
   */
  @Override
  public void start(String mode, String filePath) {
    try {
      if (mode.equalsIgnoreCase("interactive")) {
        startInteractiveMode();
      } else if (mode.equalsIgnoreCase("headless")) {
        if (filePath == null) {
          calendarView.showOutput("Error: headless mode requires a file path.\n");
          return;
        }
        startHeadlessMode(filePath);
      } else {
        calendarView.showOutput("Error: Unknown mode: " + mode + "\n");
      }
    } catch (IOException e) {
      calendarView.showOutput("IO Error: " + e.getMessage() + "\n");
    }
  }

  /**
   * Runs the calendar in interactive mode, continuously reading user
   * input from the console until the user types exit.
   */
  private void startInteractiveMode() {
    Scanner scanner = new Scanner(this.in);
    calendarView.showOutput("Interactive mode started. Type 'exit' to quit.\n");

    while (true) {
      calendarView.showOutput("> ");
      if (!scanner.hasNextLine()) {
        break;
      }
      String input = scanner.nextLine().trim();
      if (input.isEmpty()) {
        continue;
      }
      try {
        String output = executeCommand(input);
        if ("EXIT_SIGNAL".equals(output)) {
          break;
        }
        calendarView.showOutput(output);
      } catch (Exception e) {
        calendarView.showOutput("Error: " + e.getMessage() + "\n");
      }
    }
  }

  /**
   * Runs the calendar in headless mode, reading commands from a file.
   * The file must end with an exit command for proper termination.
   *
   * @param filePath the path to the command file
   * @throws IOException if the file cannot be read
   */
  private void startHeadlessMode(String filePath) throws IOException {
    List<String> lines = Files.readAllLines(Paths.get(filePath));
    String exitCommand = "exit";
    for (String line : lines) {
      String trimmed = line.trim();
      if (!trimmed.isEmpty()) {
        executeCommand(trimmed);
      }
    }
    executeCommand(exitCommand);
  }

  /**
   * Parses and executes a single user command line. It identifies the command
   * keyword, creates the corresponding command object, and executes it.
   *
   * @param fullInput the full command input string
   */
  private String executeCommand(String fullInput) {
    List<String> tokens = new ArrayList<>();
    Matcher m = Pattern.compile("\"([^\"]*)\"|(\\S+)").matcher(fullInput);
    while (m.find()) {
      if (m.group(1) != null) {
        tokens.add(m.group(1));
      } else {
        tokens.add(m.group(2));
      }
    }
    if (tokens.isEmpty()) {
      return "Error: Empty command.\n";
    }
    String commandKey = tokens.get(0).toLowerCase();
    Function<String[], Command> commandFactory = commands.get(commandKey);
    if (commandFactory == null) {
      return "Unknown command.\n";
    }
    try {
      Command command = commandFactory.apply(tokens.toArray(new String[0]));
      return command.execute();
    } catch (Exception e) {
      return "Error: " + e.getMessage() + "\n";
    }
  }

}