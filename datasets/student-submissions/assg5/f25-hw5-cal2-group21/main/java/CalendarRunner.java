import calendar.controller.CalendarController;
import calendar.model.CalendarModel;
import calendar.model.Model;
import calendar.model.ModelImpl;
import calendar.view.CalendarView;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Program runner for the calendar application. Supports two modes: interactive and headless.
 */
public class CalendarRunner {

  /**
   * Main method to run the calendar application. Usage: Interactive mode: java CalendarRunner
   * --mode interactive Headless mode:    java CalendarRunner --mode headless file-path
   *
   * @param args command line arguments
   */
  public static void main(String[] args) {
    if (args.length < 2) {
      System.err.println("Error: Invalid arguments.");
      System.err.println("Usage:");
      System.err.println("  Interactive mode: java CalendarRunner --mode interactive");
      System.err.println("  Headless mode:    java CalendarRunner --mode headless <file_path>");
      System.exit(1);
    }

    if (!args[0].equalsIgnoreCase("--mode")) {
      System.err.println("Error: First argument must be '--mode'");
      System.err.println("Usage:");
      System.err.println("  Interactive mode: java CalendarRunner --mode interactive");
      System.err.println("  Headless mode:    java CalendarRunner --mode headless <file_path>");
      System.exit(1);
    }

    String mode = args[1].toLowerCase();
    CalendarView view = new CalendarView();
    Model model = new ModelImpl();
    CalendarController controller = new CalendarController(view, model);

    switch (mode) {
      case "interactive":
        System.out.println("Calendar application started in interactive mode.");
        System.out.println("Type commands or 'exit' to quit.");
        controller.go();
        System.out.println("Calendar application exited.");
        break;

      case "headless":
        if (args.length < 3) {
          System.err.println("Error: Headless mode requires a file path.");
          System.err.println("Usage: java CalendarRunner --mode headless <file_path>");
          System.exit(1);
        }
        String filePath = args[2];
        System.out.println("Calendar application started in headless mode.");
        System.out.println("Reading commands from: " + filePath);
        runHeadless(controller, view, filePath);
        System.out.println("Calendar application exited.");
        break;

      default:
        System.err.println("Error: Invalid mode '" + args[1] + "'");
        System.err.println("Valid modes: interactive, headless");
        System.exit(1);
    }
  }

  /**
   * Runs the calendar in headless mode by reading commands from a file.
   *
   * @param controller the calendar controller
   * @param view       the calendar view for displaying messages
   * @param filePath   path to the file containing commands
   */
  private static void runHeadless(CalendarController controller, CalendarView view,
      String filePath) {
    try (Scanner scanner = new Scanner(new FileInputStream(filePath))) {
      boolean exitCommandFound = false;

      while (scanner.hasNextLine()) {
        String command = scanner.nextLine().trim();

        if (command.isEmpty()) {
          continue;
        }

        controller.executeCommand(command);

        if (command.equalsIgnoreCase("exit")) {
          exitCommandFound = true;
          break;
        }
      }

      if (!exitCommandFound) {
        view.showError("File ended without 'exit' command.");
      }

    } catch (FileNotFoundException e) {
      view.showError("File not found at: " + filePath);
      System.exit(1);
    }
  }
}