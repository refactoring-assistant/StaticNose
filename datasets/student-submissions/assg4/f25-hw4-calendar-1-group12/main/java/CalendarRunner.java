import calendar.controller.Calendar;
import calendar.controller.CalendarController;
import calendar.model.CalendarModel;
import calendar.model.SimpleCalendar;
import calendar.view.CalendarTextView;
import calendar.view.CalendarView;
import java.io.InputStreamReader;

/**
 * Main entry point for the Calendar application.
 * Supports running in interactive mode or headless mode with command file input.
 */
public class CalendarRunner {

  /**
   * Main method to run the calendar application.
   * Accepts command line arguments to determine the mode of operation.
   *
   * @param args command line arguments (--mode interactive / --mode headless)
   */
  public static void main(String[] args) {
    if (args.length < 2) {
      System.err.println("Usage: java CalendarRunner --mode <interactive|headless> [fileName]");
      System.exit(1);
    }

    String modeFlag = args[0].toLowerCase();
    String mode = args[1].toLowerCase();

    if (!modeFlag.equals("--mode")) {
      System.err.println("First argument must be --mode");
      System.exit(1);
    }

    CalendarModel model = new SimpleCalendar();
    CalendarView view = new CalendarTextView(System.out);
    CalendarController controller = new Calendar(model, view, new InputStreamReader(System.in));

    try {
      switch (mode) {
        case "interactive":
          controller.runInteractive();
          break;
        case "headless":
          if (args.length < 3) {
            System.err.println("Headless mode requests a filepath");
            System.exit(1);
          }
          controller.runHeadless(args[2]);
          break;
        default:
          System.err.println("Unknown mode: " + mode);
          System.exit(1);
      }
    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
      System.exit(1);
    }
  }
}