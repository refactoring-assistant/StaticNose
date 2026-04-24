import calendar.controller.CalendarController;
import calendar.model.Calendar;
import calendar.model.CalendarImpl;
import calendar.view.CalendarView;
import calendar.view.CalendarViewImpl;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Program runner for the Calendar Application.
 * Supports both interactive and headless modes.
 *
 * <p>Usage:
 * - Interactive: java -jar calendar.jar --mode interactive
 * - Headless: java -jar calendar.jar --mode headless commands.txt
 */
public class CalendarRunner {

  /**
   * Private constructor to avoid instantiation.
   */
  private CalendarRunner() {
    throw new UnsupportedOperationException("Utility class - cannot be instantiated");
  }

  /**
   * The main method to run the calendar application.
   *
   * @param args Command line arguments
   */
  public static void main(String[] args) {
    // Parse command line arguments
    if (args.length < 2) {
      displayUsage();
      return;
    }

    String modeFlag = args[0];
    String modeValue = args[1];

    // Validate --mode flag
    if (!modeFlag.equalsIgnoreCase("--mode")) {
      System.err.println("Error: First argument must be --mode");
      displayUsage();
      return;
    }

    // Create MVC components
    Calendar calendar = new CalendarImpl();
    CalendarView view = new CalendarViewImpl();
    CalendarController controller = new CalendarController(calendar, view);

    // Run in appropriate mode
    if (modeValue.equalsIgnoreCase("interactive")) {
      runInteractiveMode(controller, view);
    } else if (modeValue.equalsIgnoreCase("headless")) {
      if (args.length < 3) {
        System.err.println("Error: Headless mode requires a command file argument");
        displayUsage();
        return;
      }
      String commandFile = args[2];
      runHeadlessMode(controller, view, commandFile);
    } else {
      System.err.println("Error: Invalid mode '" + modeValue + "'. Must be 'interactive'"
          + " or 'headless'");
      displayUsage();
      return;
    }
  }

  /**
   * Runs the application in interactive mode.
   *
   * @param controller The calendar controller
   * @param view       The calendar view
   */
  private static void runInteractiveMode(CalendarController controller, CalendarView view) {
    view.displayWelcome();

    Scanner scanner = new Scanner(System.in);

    while (true) {
      view.displayPrompt();

      if (!scanner.hasNextLine()) {
        break;
      }

      String command = scanner.nextLine().trim();

      // Skip empty commands
      if (command.isEmpty()) {
        continue;
      }

      // Execute command
      boolean shouldContinue = controller.executeCommand(command);

      if (!shouldContinue) {
        view.displaySuccess("Goodbye!");
        break;
      }
    }

    scanner.close();
  }

  /**
   * Runs the application in headless mode.
   *
   * @param controller  The calendar controller
   * @param view        The calendar view
   * @param commandFile Path to the command file
   */
  private static void runHeadlessMode(CalendarController controller, CalendarView view,
                                      String commandFile) {
    String filePath = Paths.get(commandFile).toString();

    try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
      String command;
      boolean exitCommandFound = false;

      while ((command = reader.readLine()) != null) {
        command = command.trim();

        if (command.isEmpty()) {
          continue;
        }

        // Execute command
        boolean shouldContinue = controller.executeCommand(command);

        if (!shouldContinue) {
          exitCommandFound = true;
          view.displaySuccess("Commands executed successfully. Exiting.");
          break;
        }
      }

      // Check if exit command was found
      if (!exitCommandFound) {
        System.err.println("Error: Command file must end with an 'exit' command");
        return;
      }

    } catch (IOException e) {
      System.err.println("Error: Could not read command file '" + commandFile + "'");
      System.err.println("Details: " + e.getMessage());
      return;
    }
  }

  /**
   * Displays usage information.
   */
  private static void displayUsage() {
    System.err.println("Usage:");
    System.err.println("  Interactive mode: java -jar build/libs/calendar-1.0.jar "
        + "--mode interactive");
    System.err.println("  Headless mode:    java -jar build/libs/calendar-1.0.jar --mode headless "
        + "<command file>");
    System.err.println();
    System.err.println("Examples:");
    System.err.println("  java -jar build/libs/calendar-1.0.jar --mode interactive");
    System.err.println("  java -jar build/libs/calendar-1.0.jar --mode headless"
        + "res/commands.txt");
  }
}