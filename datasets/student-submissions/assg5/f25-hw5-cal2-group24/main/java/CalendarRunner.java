import calendarcontroller.CalendarController;
import calendarcontroller.TextCalendarController;
import calendarmodel.CalendarModel;
import calendarmodel.CalendarModelImpl;
import calendarview.CalendarView;
import calendarview.ConsoleView;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import multicalendarmodel.MultiCalendarModel;
import multicalendarmodel.MultiCalendarModelImpl;

/**
 * The main entry point for the Virtual Calendar application.
 *
 * <p>This class is responsible for parsing command-line arguments to
 * run the application in either "interactive" or "headless" mode.
 */
public class CalendarRunner {

  /**
   * The main method.
   *
   * @param args Command-line arguments.
   *             Expected: --mode interactive
   *             or:       --mode headless File_Path
   */
  public static void main(String[] args) {
    Readable input = null;
    boolean isHeadless = false;

    try {
      if (args.length == 0) {
        System.err.println("Error: No arguments provided.");
        printUsage();
        return;
      }
      if (!args[0].equalsIgnoreCase("--mode")) {
        System.err.println("Error: First argument must be '--mode'.");
        printUsage();
        return;
      }
      if (args.length < 2) {
        System.err.println("Error: No mode specified after '--mode'.");
        printUsage();
        return;
      }
      String modeType = args[1];
      if (modeType.equalsIgnoreCase("interactive")) {
        if (args.length > 2) {
          System.err.println("Warning: Extra arguments for interactive mode ignored.");
        }
        input = new InputStreamReader(System.in);
      } else if (modeType.equalsIgnoreCase("headless")) {
        if (args.length < 3) {
          System.err.println("Error: Headless mode requires a file path argument.");
          printUsage();
          return;
        }
        if (args.length > 3) {
          System.err.println("Warning: Extra arguments for headless mode ignored.");
        }
        String filePath = args[2];
        try {
          input = new FileReader(filePath);
          isHeadless = true;
        } catch (FileNotFoundException e) {
          System.err.println("Error: Headless mode file not found: " + filePath);
          return;
        }
      } else {
        System.err.println("Error: Unknown mode '" + modeType + "'.");
        printUsage();
        return;
      }

      CalendarView view = new ConsoleView();
      MultiCalendarModel appModel = new MultiCalendarModelImpl();
      CalendarController controller = new TextCalendarController(appModel, view, input);
      controller.run();

    } catch (Exception e) {
      System.err.println("A critical error occurred: " + e.getMessage());
      e.printStackTrace();
    } finally {
      if (isHeadless && input != null) {
        try {
          ((FileReader) input).close();
        } catch (Exception e) {
          System.err.println("Warning: Failed to close input file stream.");
        }
      }
    }
  }

  /**
   * Prints the correct command-line usage to System.err.
   */
  private static void printUsage() {
    System.err.println("Usage:");
    System.err.println("  java CalendarRunner --mode interactive");
    System.err.println("  java CalendarRunner --mode headless <file_path>");
  }
}