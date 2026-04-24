import calendar.controller.CalendarController;
import calendar.controller.IcalendarController;
import calendar.model.calendar.Calendar;
import calendar.model.calendar.Icalendar;
import calendar.view.IcalendarView;
import calendar.view.TextView;

/**
 * Main entry point for the Calendar application.
 * Supports both interactive and headless modes.
 * Usage:
 * java -jar calendar.jar --mode interactive
 * java -jar calendar.jar --mode headless commands.txt
 */
public class CalendarRunner {

  /**
   * Main function of the Program.
   *
   * @param args default main signature.
   */
  public static void main(String[] args) {
    // Validate arguments
    if (args.length < 2) {
      printUsage();
      System.exit(1);
    }

    // Parse mode argument
    if (!args[0].equalsIgnoreCase("--mode")) {
      System.err.println("Error: First argument must be '--mode'");
      printUsage();
      System.exit(1);
    }

    String mode = args[1].toLowerCase();

    // Create calendar.model, calendar.view, and calendar.controller
    Icalendar calendar = new Calendar();
    IcalendarView view = new TextView();
    IcalendarController controller = new CalendarController(calendar, view);

    try {
      if (mode.equals("interactive")) {
        // Run in interactive mode
        controller.runInteractive();

      } else if (mode.equals("headless")) {
        // Validate file argument
        if (args.length < 3) {
          System.err.println("Error: Headless mode requires a command file path");
          printUsage();
          System.exit(1);
        }

        String commandFilePath = args[2];

        // Run in headless mode
        controller.runHeadless(commandFilePath);

      } else {
        System.err.println("Error: Invalid mode '" + mode + "'");
        printUsage();
        System.exit(1);
      }

    } catch (Exception e) {
      System.err.println("Fatal error: " + e.getMessage());
      System.exit(1);
    }
  }

  /**
   * Print usage instructions.
   */
  private static void printUsage() {
    System.err.println("Usage:");
    System.err.println("  Interactive mode: java -jar calendar.jar --mode interactive");
    System.err.println("  Headless mode:    java -jar calendar.jar --mode headless <command_file>");
  }
}