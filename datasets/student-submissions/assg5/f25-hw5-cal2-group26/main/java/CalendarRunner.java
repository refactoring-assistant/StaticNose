import calendar.command.CommandExecutor;
import calendar.controller.CalendarControllerImpl;
import calendar.controller.CalendarManagerControllerImpl;
import calendar.model.CalendarManager;
import calendar.view.CalendarViewInterface;
import calendar.view.ConsoleCalendarView;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.ZoneId;

/**
 * Main entry point for the Calendar application.
 * Supports interactive and headless modes.
 */
public class CalendarRunner {

  /**
   * Application entry point.
   */
  public static void main(String[] args) {
    if (args.length < 2 || !args[0].equalsIgnoreCase("--mode")) {
      System.err.println(
          "Usage: java -jar Calendar.jar --mode <interactive|headless> [commands.txt]");
      return;
    }

    String mode = args[1].toLowerCase();

    CalendarManager manager = new CalendarManager();
    manager.createCalendar("default", ZoneId.of("America/New_York"));
    CalendarControllerImpl eventController = new CalendarControllerImpl(manager);
    CalendarManagerControllerImpl managerController =
        new CalendarManagerControllerImpl(manager, eventController);

    CommandExecutor executor = new CommandExecutor(eventController, managerController);
    CalendarViewInterface view = new ConsoleCalendarView();

    switch (mode) {
      case "interactive":
        runInteractive(executor, view);
        break;
      case "headless":
        if (args.length < 3) {
          view.showError(
              "Headless mode requires a commands file: java -jar Calendar.jar "
                  + "--mode headless commands.txt");
          return;
        }
        runHeadless(executor, view, args[2]);
        break;
      default:
        view.showError("Unknown mode: " + args[1] + ". Use 'interactive' or 'headless'.");
    }
  }

  private static void runInteractive(CommandExecutor executor, CalendarViewInterface view) {
    view.showMessage("Entering interactive mode. Type commands or 'exit' to quit.");

    while (true) {
      String input = view.getUserInput();
      if (input.equalsIgnoreCase("exit")) {
        break;
      }

      String result = executor.execute(input);
      view.showMessage(result);
    }

    view.showMessage("Goodbye!");
  }

  private static void runHeadless(CommandExecutor executor, CalendarViewInterface view,
                                  String filePath) {
    File file = new File(filePath);
    if (!file.exists()) {
      view.showError("Commands file does not exist: " + filePath);
      return;
    }

    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      String line;
      boolean exitFound = false;

      while ((line = reader.readLine()) != null) {
        String input = line.trim();
        if (input.isEmpty()) {
          continue;
        }

        if (input.equalsIgnoreCase("exit")) {
          exitFound = true;
          break;
        }

        String result = executor.execute(input);
        view.showMessage(result);
      }

      if (!exitFound) {
        view.showError("Commands file ended without an 'exit' command. Exiting.");
      }

    } catch (IOException e) {
      view.showError("Error reading commands file: " + e.getMessage());
    }

    view.showMessage("Goodbye!");
  }
}