import calendar.controller.CalendarController;
import calendar.controller.CalendarControllerImpl;
import calendar.model.CalendarSystem;
import calendar.model.CalendarSystemImpl;
import calendar.view.ConsoleView;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Main entry point for the Calendar application.
 * Supports both interactive and headless modes.
 */
public class CalendarRunner {

  /**
   * Runner function.
   *
   * @param args command line arguments
   */
  public static void main(String[] args) {
    if (args.length < 2) {
      System.err.println("Usage: java CalendarRunner --mode <interactive|headless> [filename]");
      System.err.println("  Interactive mode: --mode interactive");
      System.err.println("  Headless mode: --mode headless <commands-file>");
      return;
    }

    String modeFlag = args[0];
    String mode = args[1];

    if (!modeFlag.equalsIgnoreCase("--mode")) {
      System.err.println("Error: First argument must be --mode");
      return;
    }

    CalendarSystem system = new CalendarSystemImpl(); // Changed from Calendar to CalendarSystem
    ConsoleView view = new ConsoleView();

    try {
      if (mode.equalsIgnoreCase("interactive")) {
        runInteractiveMode(system, view);
      } else if (mode.equalsIgnoreCase("headless")) {
        if (args.length < 3) {
          System.err.println("Error: Headless mode requires a commands file");
          return;
        }
        String filename = args[2];
        runHeadlessMode(system, view, filename);
      } else {
        System.err.println("Error: Unknown mode '" + mode + "'. Use 'interactive' or 'headless'");
      }
    } catch (IOException e) {
      System.err.println("Error running application: " + e.getMessage());
    }
  }

  /**
   * Runs the application in interactive mode.
   *
   * @param system the calendar system
   * @param view   the calendar view
   * @throws IOException if an I/O error occurs
   */
  private static void runInteractiveMode(CalendarSystem system, ConsoleView view)
      throws IOException {
    Readable input = new InputStreamReader(System.in);
    CalendarController controller = new CalendarControllerImpl(system, view, input);
    controller.run();
  }

  /**
   * Runs the application in headless mode.
   *
   * @param system   the calendar system
   * @param view     the calendar view
   * @param filename the file containing commands
   * @throws IOException if an I/O error occurs
   */
  private static void runHeadlessMode(CalendarSystem system, ConsoleView view, String filename)
      throws IOException {
    try (FileReader fileReader = new FileReader(filename)) {
      CalendarController controller = new CalendarControllerImpl(system, view, fileReader);
      boolean exitedProperly = controller.run();

      if (!exitedProperly) {
        System.err.println("Error: Commands file must end with 'exit' command");
      }
    } catch (IOException e) {
      System.err.println("Error reading commands file: " + e.getMessage());
      throw e;
    }
  }
}