package app;


import controller.CalendarController;
import controller.CommandResult;
import controller.IcalendarController;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import model.CalendarImpl;
import model.Icalendar;
import view.ConsoleView;
import view.IcalendarView;


/**
 * Main application class for the Calendar system.
 * All times are in Eastern Standard Time (EST).
 * This class serves as the entry point and coordinator for the calendar application,
 * supporting both interactive and headless (batch) modes of operation.
 */
public class CalendarApp {
  private final IcalendarController controller;
  private final IcalendarView view;

  /**
   * Default constructor for production use.
   * Creates a new calendar model and console view internally.
   * This constructor is used when running the application normally
   * from the command line or JAR file.
   */
  public CalendarApp() {
    Icalendar model = new CalendarImpl();
    this.controller = new CalendarController(model);
    this.view = new ConsoleView();
  }

  /**
   * Constructor for testing with a custom view.
   * Creates a new calendar model internally but allows injection
   * of a custom view implementation for UI testing purposes.
   *
   * @param view the custom view implementation to use
   */
  public CalendarApp(IcalendarView view) {
    Icalendar model = new CalendarImpl();
    this.controller = new CalendarController(model);
    this.view = view;
  }

  /**
   * Constructor with full dependency injection for comprehensive testing.
   * Allows complete control over both the model and view components,
   * enabling unit tests to verify application behavior with mock objects.
   *
   * @param model the calendar model implementation to use
   * @param view  the view implementation to use
   */
  public CalendarApp(Icalendar model, IcalendarView view) {
    this.controller = new CalendarController(model);
    this.view = view;
  }

  /**
   * Runs the application in interactive mode.
   * In this mode, the application displays a prompt and waits for user
   * commands, processing them one at a time until an exit command is received.
   */
  public void runInteractive() {
    try {
      view.displayMessage("===== Calendar Application - Interactive Mode =====");
      view.displayMessage("Type 'help' for available commands or 'exit' to quit");
      view.displayMessage("");

      boolean running = true;
      while (running) {
        view.displayPrompt();
        String command = view.getInput();

        if (command.isEmpty()) {
          continue;
        }

        if (command.equalsIgnoreCase("help")) {
          displayHelp();
          continue;
        }

        CommandResult result = controller.executeCommand(command);
        view.displayResult(result);

        if (result.shouldExit()) {
          running = false;
        }
      }
    } catch (IOException e) {
      System.err.println("I/O Error: " + e.getMessage());
    } finally {
      try {
        view.close();
      } catch (IOException e) {
        System.err.println("I/O Error: " + e.getMessage());
      }
    }
  }

  /**
   * Runs the application in headless mode, processing commands from a file.
   * This mode is designed for batch processing and automated testing,
   * reading commands from a text file and executing them sequentially.
   *
   * @param filename the path to the file containing commands to execute
   *                 The file should contain one command per line, with
   *                 optional comments (lines starting with #) and empty lines
   */
  public void runHeadless(String filename) {
    try {
      String fileContent = new String(Files.readAllBytes(Paths.get(filename)));
      Readable in = new StringReader(fileContent);
      StringBuilder out = new StringBuilder();
      ConsoleView fileView = new ConsoleView(in, System.out, System.err);

      IcalendarView originalView = this.view;

      view.displayMessage("===== Calendar Application - Headless Mode =====");
      view.displayMessage("Executing commands from: " + filename);
      view.displayMessage("");

      boolean foundExit = false;
      Scanner fileScanner = new Scanner(fileContent);

      while (fileScanner.hasNextLine()) {
        String command = fileScanner.nextLine().trim();

        if (command.isEmpty() || command.startsWith("#")) {
          continue;
        }

        view.displayMessage("Executing: " + command);
        CommandResult result = controller.executeCommand(command);
        view.displayResult(result);
        view.displayMessage("");

        if (result.shouldExit()) {
          foundExit = true;
          break;
        }
      }

      if (!foundExit) {
        view.displayError("No 'exit' command found in file. Terminating.");
      }

    } catch (IOException e) {
      try {
        view.displayError("Error reading file '" + filename + "': " + e.getMessage());
      } catch (IOException ioe) {
        System.err.println("Error: " + e.getMessage());
      }
      System.exit(1);
    } catch (Exception e) {
      System.err.println("Unexpected error: " + e.getMessage());
      System.exit(1);
    } finally {
      try {
        view.close();
      } catch (IOException e) {
        System.err.println("Unexpected error: " + e.getMessage());
        System.exit(1);
      }
    }
  }

  /**
   * Displays comprehensive help information about available commands.
   * This method provides a formatted reference guide showing all supported
   * commands with their syntax and parameters.
   *
   * @throws IOException if an I/O error occurs while displaying help text
   */
  private void displayHelp() throws IOException {
    view.displayMessage("\n===== AVAILABLE COMMANDS =====\n");
    view.displayMessage("CREATE EVENTS:");
    view.displayMessage("  create event <subject> from <YYYY-MM-DDThh:mm> to <YYYY-MM-DDThh:mm>");
    view.displayMessage("  create event <subject> on <YYYY-MM-DD>  (all-day event)");
    view.displayMessage(
        "  create event <subject> from <datetime> to <datetime> repeats <days> for <N> times");
    view.displayMessage(
        "  create event <subject> from <datetime> to <datetime> repeats <days> until <date>");
    view.displayMessage("");

    view.displayMessage("EDIT EVENTS:");
    view.displayMessage(
        "  edit event <property> <subject> from <datetime> to <datetime> with <value>");
    view.displayMessage("  edit events <property> <subject> from <datetime> with <value>");
    view.displayMessage("  edit series <property> <subject> from <datetime> with <value>");
    view.displayMessage("");

    view.displayMessage("QUERY EVENTS:");
    view.displayMessage("  print events on <YYYY-MM-DD>");
    view.displayMessage("  print events from <datetime> to <datetime>");
    view.displayMessage("  show status on <YYYY-MM-DDThh:mm>");
    view.displayMessage("");

    view.displayMessage("OTHER:");
    view.displayMessage("  export cal <filename.csv>");
    view.displayMessage("  help");
    view.displayMessage("  exit");
    view.displayMessage("");
  }
}