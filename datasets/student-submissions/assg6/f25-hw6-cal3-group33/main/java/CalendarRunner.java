import calendar.controller.CalendarController;
import calendar.controller.GuiController;
import calendar.controller.HeadlessController;
import calendar.controller.InteractiveController;
import calendar.model.manager.CalendarCatalog;
import calendar.model.manager.CalendarManager;
import calendar.view.CalendarView;
import calendar.view.ConsoleView;
import calendar.view.GuiCalendarInterface;
import calendar.view.GuiCalendarView;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.ZoneId;

/**
 * Entry point for the Calendar application.
 * Supports interactive mode (console), headless mode (file-based commands), and GUI mode.
 */
public class CalendarRunner {
  /**
   * The main method that acts as program runner.
   */
  public static void main(String[] args) {

    try {
      CommandLineArgs parsedArgs = parseArguments(args);

      CalendarManager calendarManager = new CalendarCatalog();

      if (parsedArgs.mode == Mode.GUI) {
        createDefaultCalendar(calendarManager);

        GuiCalendarInterface guiView = new GuiCalendarView();
        CalendarController controller = new GuiController(calendarManager, guiView);
        controller.run();
      } else {
        CalendarView view = new ConsoleView();

        try (Reader reader = createReader(parsedArgs.mode, parsedArgs.filePath)) {
          CalendarController controller =
              createController(parsedArgs.mode, reader, view, calendarManager);
          controller.run();
        }
      }

    } catch (IllegalArgumentException e) {
      CalendarView view = new ConsoleView();
      view.displayFatalError(e.getMessage());
      view.displayUsageInformation();
      System.exit(1);
    } catch (FileNotFoundException e) {
      CalendarView view = new ConsoleView();
      view.displayFileNotFound(e.getMessage());
      view.displayUsageInformation();
      System.exit(1);
    } catch (IOException e) {
      CalendarView view = new ConsoleView();
      view.displayFatalError("IO error: " + e.getMessage());
      System.exit(1);
    } catch (Exception e) {
      CalendarView view = new ConsoleView();
      view.displayFatalError("Unexpected error: " + e.getMessage());
      System.exit(1);
    }
  }

  /**
   * Creates a default calendar with system timezone and selects it.
   *
   * @param manager the calendar manager
   */
  private static void createDefaultCalendar(CalendarManager manager) {
    try {
      String systemTimezone = ZoneId.systemDefault().getId();
      manager.createCalendar("Default", systemTimezone);
      manager.useCalendar("Default");
    } catch (Exception e) {
      throw new RuntimeException("Failed to create default calendar: " + e.getMessage(), e);
    }
  }

  /**
   * Parses and validates command line arguments.
   *
   * @param args command line arguments
   * @return parsed arguments
   * @throws IllegalArgumentException if arguments are invalid
   */
  private static CommandLineArgs parseArguments(String[] args) {
    if (args.length == 0) {
      return new CommandLineArgs(Mode.GUI, null);
    }

    if (args.length < 2) {
      throw new IllegalArgumentException("Insufficient arguments provided.");
    }

    if (!args[0].equalsIgnoreCase("--mode")) {
      throw new IllegalArgumentException("First argument must be '--mode'.");
    }

    String modeString = args[1].toLowerCase();

    Mode mode;
    try {
      mode = Mode.valueOf(modeString.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(
          "Invalid mode. Must be 'interactive', 'headless', or no arguments for GUI.");
    }

    String filePath = null;
    if (mode == Mode.HEADLESS) {
      if (args.length < 3) {
        throw new IllegalArgumentException("Headless mode requires a file path.");
      }
      filePath = args[2];
    }

    return new CommandLineArgs(mode, filePath);
  }

  /**
   * Creates a Reader based on the mode.
   *
   * @param mode the execution mode
   * @param filePath the file path (null for interactive mode)
   * @return Reader for reading commands
   * @throws FileNotFoundException if file doesn't exist in headless mode
   */
  private static Reader createReader(Mode mode, String filePath)
      throws FileNotFoundException {
    if (mode == Mode.INTERACTIVE) {
      return new InputStreamReader(System.in);
    } else {
      return new FileReader(filePath);
    }
  }

  /**
   * Creates the appropriate controller based on mode.
   *
   * @param mode the execution mode
   * @param reader the input reader
   * @param view the view
   * @param calendarManager the calendar manager for managing multiple calendars
   * @return the appropriate controller
   */
  private static CalendarController createController(Mode mode, Reader reader, CalendarView view,
                                                     CalendarManager calendarManager) {
    if (mode == Mode.INTERACTIVE) {
      return new InteractiveController(reader, view, calendarManager);
    } else {
      return new HeadlessController(reader, view, calendarManager);
    }
  }

  /**
   * Execution mode enum.
   */
  private enum Mode {
    INTERACTIVE,
    HEADLESS,
    GUI
  }

  /**
   * Container for parsed command line arguments.
   */
  private static class CommandLineArgs {
    final Mode mode;
    final String filePath;

    CommandLineArgs(Mode mode, String filePath) {
      this.mode = mode;
      this.filePath = filePath;
    }
  }
}