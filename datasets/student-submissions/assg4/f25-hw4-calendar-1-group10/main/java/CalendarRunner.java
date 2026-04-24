import calendar.DummyCalendar;
import calendar.controller.CalendarController;
import calendar.model.CalendarModelImpl;

/**
 * Program runner.
 */
public class CalendarRunner {
  /**
   * The main method placeholder.
   */
  public static void main(String[] args) {
    if (args.length == 0) {
      printUsage();
      return;
    }

    // Parse arguments
    if (!args[0].equalsIgnoreCase("--mode")) {
      printUsage();
      return;
    }

    if (args.length < 2) {
      System.out.println("Error: Missing mode argument.");
      printUsage();
      return;
    }

    String mode = args[1].toLowerCase();
    CalendarModelImpl model = new CalendarModelImpl();
    CalendarController controller = new CalendarController(model);

    switch (mode) {
      case "interactive":
        controller.runInteractive();
        break;

      case "headless":
        if (args.length < 3) {
          System.out.println("Error: Missing commands file for headless mode.");
          printUsage();
          return;
        }
        controller.runHeadless(args[2]);
        break;

      default:
        System.out.println("Error: Invalid mode: " + mode);
        printUsage();
    }
  }

  private static void printUsage() {
    System.out.println("Usage:");
    System.out.println("  java -jar build/libs/calendar-app.jar --mode interactive");
    System.out.println("  java -jar build/libs/calendar-app.jar --mode headless <commands.txt>");
  }
}
