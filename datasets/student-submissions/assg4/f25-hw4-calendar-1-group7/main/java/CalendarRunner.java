import calendar.controller.MyCalendarController;
import calendar.model.Calendar;
import calendar.model.MyCalendarImplement;
import calendar.view.MyCalendarView;
import calendar.view.MyTextView;

/**
 * Main entry point for the Calendar Application.
 * Supports both interactive and headless modes.
 *
 * <p>Usage:
 *   Interactive: java CalendarRunner --mode interactive
 *   Headless:    java CalendarRunner --mode headless filename
 */
public class CalendarRunner {

  /**
   * Main method to run the calendar application.
   *
   * @param args command line arguments
   */
  public static void main(String[] args) {
    if (args.length < 2) {
      printUsage();
      System.exit(1);
    }

    if (!args[0].equalsIgnoreCase("--mode")) {
      System.err.println("Error: First argument must be '--mode'");
      printUsage();
      System.exit(1);
    }

    String mode = args[1].toLowerCase();

    if (!mode.equals("interactive") && !mode.equals("headless")) {
      System.err.println("Error: Mode must be 'interactive' or 'headless'");
      printUsage();
      System.exit(1);
    }

    if (mode.equals("headless") && args.length < 3) {
      System.err.println("Error: Headless mode requires a filename");
      printUsage();
      System.exit(1);
    }

    Calendar calendar = new MyCalendarImplement();
    MyCalendarView view = new MyTextView();
    MyCalendarController controller = new MyCalendarController(calendar, view);

    if (mode.equals("interactive")) {
      controller.runInteractive();
    } else {
      String filename = args[2];
      controller.runHeadless(filename);
    }
  }

  /**
   * Prints usage information.
   */
  private static void printUsage() {
    System.err.println("Usage:");
    System.err.println("  Interactive mode: java CalendarRunner --mode interactive");
    System.err.println("  Headless mode:    java CalendarRunner --mode headless <filename>");
    System.err.println();
    System.err.println("Examples:");
    System.err.println("  java CalendarRunner --mode interactive");
    System.err.println("  java CalendarRunner --mode headless res/commands.txt");
  }
}