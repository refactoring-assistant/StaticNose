import calendar.controller.CommandController;
import calendar.controller.CommandProcessor;
import calendar.model.CalendarModel;
import calendar.model.CalendarModelImpl;
import calendar.view.ConsoleView;
import calendar.view.ConsoleViewImpl;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Main application entry.
 * Checks for mode arguments (interactive or headless) and sets up the
 * Model, View, and Controller parts.
 */
public class CalendarRunner {

  /**
   * Starts the calendar app.
   * Checks for valid command-line arguments (must start with "--mode").
   * Runs either interactive or headless mode.
   *
   * @param args commandline arguments, expects "--mode" followed by the mode.
   */
  public static void main(String[] args) {
    if (args == null || args.length < 2 || !"--mode".equalsIgnoreCase(args[0])) {
      System.err.println(
          "ERROR: Missing or invalid arguments. Use: --mode interactive OR --mode headless <file>");
      System.exit(1);
    }

    String mode = args[1].toLowerCase();

    ConsoleView view = new ConsoleViewImpl();
    CalendarModel model = new CalendarModelImpl();
    CommandController controller = new CommandController(model, view);
    CommandProcessor processor = new CommandProcessor(controller, view);

    switch (mode) {
      case "interactive":
        runInteractive(processor);
        break;
      case "headless":
        if (args.length < 3) {
          System.err.println("ERROR: Headless mode requires a commands file path.");
          System.exit(1);
        }
        runHeadless(processor, args[2]);
        break;
      default:
        System.err.println("ERROR: Unknown mode: " + args[1]);
        System.exit(1);
    }
  }

  /**
   * Runs the app in interactive mode.
   * Reads commands from the console (System.in) until 'exit' is seen.
   * Loops, processes command, and prints a prompt.
   *
   * @param processor The CommandProcessor to handle user input.
   */
  private static void runInteractive(CommandProcessor processor) {
    try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
      System.out.println("Calendar (interactive). Type commands, 'exit' to quit.");
      while (true) {
        System.out.print("> ");
        String line = br.readLine();
        if (line == null) {
          break;
        }
        boolean keep = processor.process(line);
        if (!keep) {
          break;
        }
      }
    } catch (IOException e) {
      System.err.println("ERROR: " + e.getMessage());
    }
  }

  /**
   * Runs the app in headless mode.
   * Reads and runs commands one by one from the specified file path.
   * Checks if the last command in the file was 'exit'.
   *
   * @param processor The CommandProcessor to handle file commands.
   * @param filePath The path to the file containing commands.
   */
  private static void runHeadless(CommandProcessor processor, String filePath) {
    boolean sawExit = false;
    try {
      for (String line : Files.readAllLines(Paths.get(filePath))) {
        boolean keep = processor.process(line);
        if (!keep) {
          sawExit = true;
          break;
        }
      }
      if (!sawExit) {
        System.err.println("ERROR: Commands file ended without 'exit'. Quitting gracefully.");
      }
    } catch (IOException e) {
      System.err.println("ERROR: " + e.getMessage());
    }
  }
}
