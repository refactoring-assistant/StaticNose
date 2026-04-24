import calendar.controller.CommandController;
import calendar.controller.GuiController;
import calendar.controller.Parser;
import calendar.model.CalendarManager;
import calendar.view.CalendarGuiView;
import calendar.view.CalendarTextView;
import java.awt.Color;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Main entry point for the calendar application.
 * Supports three execution modes: GUI, interactive text, and headless script.
 */
public class CalendarRunner {

  private static final String USAGE = "Usage:\n"
      + "  java -jar calendar.jar                          (GUI mode)\n"
      + "  java -jar calendar.jar --mode interactive       (Interactive text mode)\n"
      + "  java -jar calendar.jar --mode headless <file>   (Headless script mode)\n";

  /**
   * Main method.
   *
   * @param args command line arguments
   */
  public static void main(String[] args) {
    try {
      if (args.length == 0) {
        runGuiMode();
      } else if (args.length == 2 && "--mode".equals(args[0])
          && "interactive".equals(args[1])) {
        runInteractiveMode();
      } else if (args.length == 3 && "--mode".equals(args[0])
          && "headless".equals(args[1])) {
        runHeadlessMode(args[2]);
      } else {
        System.err.println("Error: Invalid command line arguments");
        System.err.println(USAGE);
        System.exit(1);
      }
    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
      System.exit(1);
    }
  }

  /**
   * Runs the application in GUI mode.
   */
  private static void runGuiMode() {
    CalendarManager manager = new CalendarManager();
    manager.createCalendar("Default", ZoneId.systemDefault());
    manager.useCalendar("Default");

    Map<String, Color> colorMap = new HashMap<>();
    colorMap.put("Default", new Color(66, 133, 244));

    GuiController controller = new GuiController(manager);
    CalendarGuiView view = new CalendarGuiView(controller, colorMap);
    view.display();
  }

  /**
   * Runs the application in interactive text mode.
   */
  private static void runInteractiveMode() {
    CalendarManager manager = new CalendarManager();
    manager.createCalendar("Default", ZoneId.systemDefault());
    manager.useCalendar("Default");

    CalendarTextView view = new CalendarTextView(System.out);

    System.out.println("Calendar Application - Interactive Mode");
    System.out.println("Type 'exit' to quit");
    System.out.println();


    Scanner scanner = new Scanner(new InputStreamReader(System.in));
    while (true) {
      System.out.print("> ");
      if (!scanner.hasNextLine()) {
        break;
      }

      String line = scanner.nextLine().trim();

      if (line.isEmpty()) {
        continue;
      }

      if ("exit".equalsIgnoreCase(line)) {
        System.out.println("Goodbye!");
        break;
      }

      Parser parser = new Parser();
      CommandController controller = new CommandController(manager);

      try {
        String result = controller.handle(parser.parse(line));
        if (result != null && !result.isBlank()) {
          System.out.println(result);
        }
      } catch (IllegalArgumentException e) {
        System.err.println("Error: " + e.getMessage());
      } catch (Exception e) {
        System.err.println("Unexpected error: " + e.getMessage());
      }
    }

    scanner.close();
  }

  /**
   * Runs the application in headless mode with a script file.
   *
   * @param scriptPath path to the script file
   * @throws IOException if file cannot be read
   */
  private static void runHeadlessMode(String scriptPath) throws IOException {
    CalendarManager manager = new CalendarManager();
    manager.createCalendar("Default", ZoneId.systemDefault());
    manager.useCalendar("Default");

    Parser parser = new Parser();
    CalendarTextView view = new CalendarTextView(System.out);
    CommandController controller = new CommandController(manager);

    try (Scanner scanner = new Scanner(new FileReader(scriptPath))) {
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine().trim();

        if (line.isEmpty() || line.startsWith("#")) {
          continue;
        }

        if ("exit".equalsIgnoreCase(line)) {
          break;
        }

        try {
          String result = controller.handle(parser.parse(line));
          if (result != null && !result.isBlank()) {
            System.out.println(result);
          }
        } catch (IllegalArgumentException e) {
          System.err.println("Error: " + e.getMessage());
        } catch (Exception e) {
          System.err.println("Unexpected error: " + e.getMessage());
        }
      }
    }
  }
}