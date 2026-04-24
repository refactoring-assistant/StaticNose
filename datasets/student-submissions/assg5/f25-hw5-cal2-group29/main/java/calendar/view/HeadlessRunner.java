package calendar.view;

import calendar.controller.CalendarController;
import calendar.controller.CommandParser;
import calendar.controller.ExitCommand;
import calendar.model.CalendarApplication;
import calendar.model.InMemoryCalendarApplication;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Manages the "headless" execution mode for the application.
 */
public class HeadlessRunner {

  private final CalendarController controller;
  private final CalendarView view;

  /**
   * Constructor to initialize headless runner.
   */
  public HeadlessRunner() {
    this.view = new Interface();
    this.controller = new CalendarController(
        new InMemoryCalendarApplication(),
        view,
        new CommandParser()
    );
  }

  /**
   * Runs the view in Headless mode with the file as input.
   *
   * @param filePath The file to be run in headless mode.
   */
  public void run(String filePath) {
    System.out.println("Running in headless mode with file: " + filePath);

    try {
      Path path = Paths.get(filePath);
      if (!Files.exists(path)) {
        view.displayError("Command file not found: " + filePath);
        return;
      }

      List<String> commands = Files.readAllLines(path);
      if (commands.isEmpty()) {
        view.displayError("Command file is empty: " + filePath);
        return;
      }

      boolean exitFound = false;
      int commandCount = 0;

      for (String command : commands) {
        commandCount++;
        String trimmedCommand = command.trim();

        if (trimmedCommand.isEmpty() || trimmedCommand.startsWith("#")) {
          continue;
        }

        try {
          if ("exit".equalsIgnoreCase(trimmedCommand)) {
            exitFound = true;
          }
          controller.processCommand(trimmedCommand);
        } catch (Exception e) {
          if (e instanceof ExitCommand.ExitApplicationException) {
            break;
          } else {
            view.displayError(
                "Error executing command #" + commandCount + ":" + trimmedCommand + ".");
            view.displayError("  " + e.getMessage());
          }
        }
      }

      if (!exitFound) {
        view.displayError("No 'exit' command found in file. Application terminated.");
      } else {
        System.out.println(
            "Headless execution completed. Processed " + commandCount + " commands.");
      }

    } catch (IOException e) {
      view.displayError("Error reading command file: " + e.getMessage());
    }
  }

  /**
   * Main to run the headless mode only (for testing purposes only).
   *
   * @param args Args to confirm the running mode.
   */
  public static void main(String[] args) {
    if (args.length != 1) {
      System.err.println("Usage: java HeadlessRunner <command-file>");
      System.exit(1);
    }

    HeadlessRunner runner = new HeadlessRunner();
    runner.run(args[0]);
  }
}