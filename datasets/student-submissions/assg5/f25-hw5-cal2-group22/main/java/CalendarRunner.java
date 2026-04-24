import calendar.control.ControllerImpl;
import calendar.control.Icontroller;
import calendar.model.database.CalendarDatabaseImpl;
import calendar.view.HeadlessView;
import calendar.view.InteractiveView;
import calendar.view.Iview;
import java.io.InputStream;
import java.io.PrintStream;

/**
 * Entry point for the calendar application.
 * Runs the program in either interactive or headless mode based on command-line arguments.
 * Example:
 * java -jar calendar.jar --mode interactive
 * java -jar calendar.jar --mode headless "filename.txt"
 */
public class CalendarRunner {

  /**
   * Main method that starts the calendar application.
   * We check for missing arguments, and then initialize base components.
   * Ensuring that the mode - headless or interactive is provided.
   * Interactive mode lets user input commands one line at a time.
   * Headless mode lets the user use a txt file with commands inside which is run.
   *
   * @param args command-line arguments specifying the mode and optional file path
   */
  public static void main(String[] args) {
    InputStream input = System.in;
    PrintStream output = System.out;
    if (commandError(args, output)) {
      return;
    }

    try {
      executeCommand(args, input, output);
    } catch (Exception e) {
      output.println("Fatal error: " + e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * Printing any Errors in the command.
   */
  private static boolean commandError(String[] args, PrintStream out) {
    if (args.length < 2 || !args[0].equalsIgnoreCase("--mode")) {
      out.println("Usage:");
      out.println("  --mode interactive");
      out.println("  --mode headless <filename.txt>");
      return true;
    }
    return false;

  }

  /**
   * Method used by main method to execute the command line given.
   *
   * @param args   - input command
   * @param input  - input stream
   * @param output - output stream
   */
  private static void executeCommand(String[] args, InputStream input, PrintStream output) {
    String subMode = args[1].toLowerCase();
    CalendarDatabaseImpl model = new CalendarDatabaseImpl();
    Iview view;


    if (subMode.equals("interactive")) {
      view = new InteractiveView(input, output);
      Icontroller controller = new ControllerImpl(model, view);
      controller.startInteractive();
    } else if (subMode.equals("headless")) {
      if (args.length < 3) {
        output.println("Filename required for headless mode.");
        return;
      }
      view = new HeadlessView(args[2], output);
      ControllerImpl controller = new ControllerImpl(model, view);
      controller.startHeadless();
    } else {
      output.println("Unrecognized mode: " + subMode);
    }
  }


}