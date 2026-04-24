import calendar.controller.CommandController;
import calendar.controller.GuiController;
import calendar.service.CalendarManager;
import calendar.view.CalendarView;
import java.io.File;
import java.util.Scanner;

/**
 * Main entry point for the calendar application.
 */
public class CalendarRunner {

  private CalendarRunner() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }

  /**
   * Main entry point for calendar application.
   * Supports Gui, interactive and headless modes.
   *
   * @param args command line arguments
   */
  public static void main(String[] args) {
    if (args.length == 0) {
      runGuiMode();
      return;
    }
    if (args.length < 2) {
      System.err.println("Usage: --mode interactive OR --mode headless <filename>");
      return;
    }
    if (!args[0].equals("--mode")) {
      System.err.println("First argument must be --mode");
      return;
    }
    String mode = args[1].toLowerCase();
    if (mode.equals("interactive")) {
      runInteractiveMode();
    } else if (mode.equals("headless")) {
      if (args.length < 3) {
        System.err.println("Headless mode requires filename argument");
        return;
      }
      runHeadlessMode(args[2]);
    } else {
      System.err.println("Invalid mode. Use 'interactive' or 'headless'");
    }
  }

  private static void runGuiMode() {
    javax.swing.SwingUtilities.invokeLater(() -> {
      GuiController controller =
          new GuiController();
      controller.show();
    });
  }

  private static void runInteractiveMode() {
    CalendarView view = new CalendarView();
    CalendarManager manager = new CalendarManager();
    final CommandController controller = new CommandController(view, manager);
    final Scanner scanner = new Scanner(System.in);
    System.out.println("Calendar Application - Interactive Mode");
    System.out.println("Type 'exit' to quit");
    System.out.println();
    while (true) {
      System.out.print("> ");
      if (!scanner.hasNextLine()) {
        break;
      }
      String input = scanner.nextLine().trim();
      if (input.equals("exit")) {
        break;
      }
      if (input.isEmpty()) {
        continue;
      }
      controller.executeCommand(input);
    }
    scanner.close();
    System.out.println("Goodbye!");
  }

  private static void runHeadlessMode(String filename) {
    CalendarView view = new CalendarView();
    CalendarManager manager = new CalendarManager();
    CommandController controller = new CommandController(view, manager);
    try (Scanner fileScanner = new Scanner(new File(filename))) {
      processCommandsFromFile(fileScanner, controller);
    } catch (Exception e) {
      System.err.println("Error reading file: " + e.getMessage());
    }
  }

  private static void processCommandsFromFile(Scanner fileScanner, CommandController controller) {
    boolean foundExit = false;
    while (fileScanner.hasNextLine()) {
      String input = fileScanner.nextLine().trim();
      if (input.isEmpty()) {
        continue;
      }
      if (input.equals("exit")) {
        foundExit = true;
        break;
      }
      controller.executeCommand(input);
    }
    if (!foundExit) {
      System.err.println("Error: Command file must end with 'exit' command");
    }
  }
}