import calendar.controller.CalendarController;
import calendar.controller.CalendarControllerImpl;
import calendar.model.CalendarModelImpl;
import calendar.view.CalendarViewImpl;
import java.io.FileReader;
import java.io.InputStreamReader;

/**
 * Main runner class for the calendar application.
 * Supports both interactive and headless modes.
 */
public class CalendarRunner {

  /**
   * Main method to run the calendar application.
   *
   * @param args command line arguments
   */
  public static void main(String[] args) {
    int exitCode = run(args);
    System.exit(exitCode);
  }

  /**
   * Run method that returns exit code instead of calling System.exit.
   *
   * @param args command line arguments
   * @return exit code (0 for success, 1 for error)
   */
  public static int run(String[] args) {
    try {
      if (args == null || args.length == 0) {
        System.err.println("Error: Missing mode argument.");
        return 1;
      }

      if (!args[0].equalsIgnoreCase("--mode")) {
        System.err.println("Error: First argument must be '--mode'");
        return 1;
      }

      if (args.length < 2) {
        System.err.println("Error: Mode not specified.");
        return 1;
      }

      CalendarModelImpl model = new CalendarModelImpl();
      CalendarViewImpl view = new CalendarViewImpl();
      CalendarController controller = new CalendarControllerImpl(model, view);

      String mode = args[1].toLowerCase();

      switch (mode) {
        case "interactive":
          controller.run(new InputStreamReader(System.in));
          return 0;

        case "headless":
          if (args.length < 3) {
            System.err.println("Error: Filename required for headless mode.");
            return 1;
          }
          try {
            controller.run(new FileReader(args[2]));
            return 0;
          } catch (Exception e) {
            System.err.println("Error reading file: " + e.getMessage());
            return 1;
          }

        default:
          System.err.println("Error: Invalid mode '" + args[1] + "'");
          return 1;
      }
    } catch (Exception e) {
      System.err.println("Unexpected error: " + e.getMessage());
      return 1;
    }
  }
}