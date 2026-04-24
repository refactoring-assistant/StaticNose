import calendar.controller.CalendarController;
import calendar.controller.ConsoleInputSource;
import calendar.controller.FileInputSource;
import calendar.controller.InputSource;
import calendar.model.CalendarModel;
import calendar.model.InterfaceCalendarModel;
import calendar.view.ConsoleView;
import calendar.view.InterfaceCalendarView;
import java.io.IOException;
import java.util.TimeZone;

/**
 * Main entry point for the Virtual Calendar application.
 * This class is responsible for parsing command-line arguments
 * and launching the application in the correct mode (interactive or headless).
 */
public class CalendarRunner {

  /**
   * Main method.
   *
   * @param args Command-line arguments (e.g., --mode interactive)
   */
  public static void main(String[] args) {
    TimeZone.setDefault(TimeZone.getTimeZone(InterfaceCalendarModel.TIME_ZONE_ID));

    if (args.length == 0) {
      System.err.println("Error: No mode specified.");
      return;
    }

    String modeArg = args[0].toLowerCase();
    InputSource inputSource = null;

    try {
      if (modeArg.equals("--mode")) {
        if (args.length > 1) {
          String modeType = args[1].toLowerCase();
          if (modeType.equals("interactive")) {
            if (args.length != 2) {
              printUsageError();
              return;
            }
            inputSource = new ConsoleInputSource();
          } else if (modeType.equals("headless")) {
            if (args.length != 3) {
              printUsageError();
              return;
            }
            String filename = args[2];
            inputSource = new FileInputSource(filename);
          }
        }
      }

      if (inputSource == null) {
        printUsageError();
        return;
      }

      InterfaceCalendarModel model = new CalendarModel();
      InterfaceCalendarView view = new ConsoleView();
      CalendarController controller = new CalendarController(model, view, inputSource);
      controller.run();

    } catch (IOException e) {
      System.err.println("Error initializing input source: " + e.getMessage());
    } catch (Exception e) {
      System.err.println("A fatal error occurred during startup: " + e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * Helper method to print the correct usage instructions.
   */
  private static void printUsageError() {
    System.err.println("Error: Invalid arguments.");
    System.err.println("Usage:");
    System.err.println("  java -jar YourJarName.jar --mode interactive");
    System.err.println("  java -jar YourJarName.jar --mode headless <commands.txt>");
  }
}