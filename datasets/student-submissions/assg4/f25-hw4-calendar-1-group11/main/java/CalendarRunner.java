import calendar.CalendarController;
import calendar.CalendarModel;
import calendar.CalendarModelImpl;
import calendar.CalendarView;
import calendar.CalendarViewImpl;
import calendar.DummyCalendar;

/**
 * Parses command line argument and starts the application.
 */
public class CalendarRunner {

  /**
   * The main method to run calendar application.
   */
  public static void main(String[] args) {
    if (args.length == 0) {
      System.err.println("Error: Missing mode argument.");
      System.exit(1);
    }
    if (!args[0].equalsIgnoreCase("--mode")) {
      System.err.println("Error: First argument must be '--mode'");
      System.exit(1);
    }
    if (args.length < 2) {
      System.err.println("Error: Mode not specified.");
      System.err.println("Valid modes: interactive, headless");
      System.exit(1);
    }

    CalendarModel model = new CalendarModelImpl();
    CalendarView view = new CalendarViewImpl();
    CalendarController controller = new CalendarController(model, view);

    String mode = args[1].toLowerCase();

    switch (mode) {
      case "interactive":
        controller.runInteractiveMode();
        break;

      case "headless":

        if (args.length < 3) {
          System.err.println("Error: Filename required for headless mode.");
          System.err.println("Usage: java CalendarRunner --mode headless <filename>");
          System.exit(1);
        }
        controller.runHeadlessMode(args[2]);
        break;

      default:
        System.err.println("Error: Invalid mode '" + args[1] + "'");
        System.err.println("Valid modes: interactive, headless");
        System.exit(1);
    }
  }
}

