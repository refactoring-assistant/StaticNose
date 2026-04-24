import calendar.controller.CalendarController;
import calendar.controller.CommandParser;
import calendar.controller.ExitCommand.ExitApplicationException;
import calendar.model.CalendarApplication;
import calendar.model.InMemoryCalendarApplication;
import calendar.view.CalendarView;
import calendar.view.HeadlessRunner;
import calendar.view.Interface;
import java.util.Scanner;

/**
 * Instantiates the top-level InMemoryCalendarApplication model.
 */
public class CalendarRunner {

  /**
   * The main entry point for the application.
   *
   * @param args Command-line arguments.
   *             Expected: --mode interactive
   *             or:       --mode headless &lt;filepath&gt;
   */
  public static void main(String[] args) {
    if (args.length < 2 || !"--mode".equalsIgnoreCase(args[0])) {
      printUsageError();
      return;
    }

    String mode = args[1];

    try {
      if ("interactive".equalsIgnoreCase(mode) && args.length == 2) {
        runInteractiveMode();
      } else if ("headless".equalsIgnoreCase(mode) && args.length == 3) {
        runHeadlessMode(args[2]);
      } else {
        printUsageError();
      }

    } catch (ExitApplicationException e) {
      //Empty Catch
    } catch (Exception e) {
      System.err.println("A critical error occurred: " + e.getMessage());
    }
  }

  /**
   * Sets up and runs the application in interactive mode.
   */
  private static void runInteractiveMode() {
    CalendarApplication model = new InMemoryCalendarApplication();
    CalendarView view = new Interface();
    CommandParser parser = new CommandParser();
    CalendarController controller = new CalendarController(model, view, parser);

    Scanner scanner = new Scanner(System.in);

    while (true) {
      try {
        view.showPrompt();
        if (!scanner.hasNextLine()) {
          break;
        }
        String line = scanner.nextLine();
        controller.processCommand(line);
      } catch (ExitApplicationException e) {
        break;
      }
    }
  }

  /**
   * Sets up and runs the application in headless mode.
   *
   * @param filename The path to the command file.
   * @throws Exception if a critical error occurs.
   */
  private static void runHeadlessMode(String filename) throws Exception {
    HeadlessRunner runner = new HeadlessRunner();
    runner.run(filename);
  }

  /**
   * Prints the correct application usage to System.err.
   */
  private static void printUsageError() {
    System.err.println("Usage: java -jar <jarfile> --mode <mode>");
    System.err.println("Modes:");
    System.err.println("  --mode interactive");
    System.err.println("  --mode headless <filepath.txt>");
  }
}