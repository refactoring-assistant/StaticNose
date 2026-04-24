import calendar.controller.CalendarController;
import calendar.model.Calendar;
import calendar.model.Icalendar;
import calendar.view.CalendarConsoleView;
import calendar.view.IcalendarView;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Main entry point for the Calendar Application.
 * Supports both interactive and headless modes.
 */
public class CalendarRunner {

  /**
   * Main method that starts the calendar application.
   *
   * @param args command line arguments
   */
  public static void main(String[] args) {
    if (args.length < 2) {
      System.err.println("Usage: java CalendarRunner --mode <interactive|headless> [file]");
      System.err.println("  --mode interactive : Run in interactive mode");
      System.err.println("  --mode headless <file> : Run commands from file");
      System.exit(1);
    }

    String modeFlag = args[0];
    String mode = args[1];

    if (!modeFlag.equalsIgnoreCase("--mode")) {
      System.err.println("Error: First argument must be --mode");
      System.exit(1);
    }

    Icalendar model = new Calendar();
    IcalendarView view = new CalendarConsoleView();

    if (mode.equalsIgnoreCase("interactive")) {
      CalendarController controller = new CalendarController(
          model, view, new InputStreamReader(System.in));
      controller.runInteractive();

    } else if (mode.equalsIgnoreCase("headless")) {
      if (args.length < 3) {
        System.err.println("Error: Headless mode requires a command file");
        System.exit(1);
      }

      String commandFile = args[2];
      try (FileReader reader = new FileReader(commandFile)) {
        CalendarController controller = new CalendarController(model, view, reader);
        controller.runHeadless(commandFile);
      } catch (IOException e) {
        System.err.println("Error reading command file: " + e.getMessage());
        System.exit(1);
      }

    } else {
      System.err.println("Error: Mode must be 'interactive' or 'headless'");
      System.exit(1);
    }
  }
}