import calendar.controller.CalendarController;
import calendar.model.Calendar;
import calendar.model.CalendarDatabase;
import calendar.model.InCalendar;
import calendar.repository.InMemoryEventRepository;
import calendar.service.ExportService;
import calendar.service.InExportService;
import calendar.view.HeadlessView;
import calendar.view.InteractiveView;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.util.Objects;

/**
 * Main entry point for Calendar Application.
 * Parses command-line arguments and initializes appropriate mode.
 * - Now creates CalendarDatabase instead of single calendar
 * - Creates default calendar with EST timezone
 * Usage:
 * java -jar calendar.jar --mode interactive
 * java -jar calendar.jar --mode headless commands.txt
 */
public class CalendarRunner {

  private static final String DEFAULT_CALENDAR_NAME = "MyCalendar";
  private static final ZoneId DEFAULT_TIMEZONE = ZoneId.of("America/New_York");

  /**
   * The main calling function.
   *
   * @param args command-line arguments: --mode [interactive|headless] [filepath]
   */
  public static void main(String[] args) {
    if (args.length < 2) {
      System.err.println("Usage: java -jar calendar.jar --mode "
          + "[interactive|headless] [filepath]");
      System.exit(1);
      return;
    }

    String modeFlag = args[0].toLowerCase();
    String mode = args[1].toLowerCase();

    if (!"--mode".equals(modeFlag)) {
      System.err.println("Invalid flag. Expected: --mode");
      System.exit(1);
      return;
    }

    CalendarDatabase calendarDatabase = new CalendarDatabase();

    try {
      InCalendar defaultCalendar = new Calendar(
          DEFAULT_CALENDAR_NAME,
          new InMemoryEventRepository()
      );

      calendarDatabase.addCalendar(
          DEFAULT_CALENDAR_NAME,
          defaultCalendar,
          DEFAULT_TIMEZONE
      );

      calendarDatabase.setActiveCalendar(DEFAULT_CALENDAR_NAME);

      InExportService exportService = new ExportService(DEFAULT_TIMEZONE);

      switch (mode) {
        case "interactive":
          runInteractiveMode(calendarDatabase, exportService);
          break;

        case "headless":
          if (args.length < 3) {
            System.err.println("Headless mode requires file path");
            System.exit(1);
            return;
          }
          String filePath = args[2];
          runHeadlessMode(calendarDatabase, exportService, filePath);
          break;

        default:
          System.err.println("Invalid mode. Use 'interactive' or 'headless'");
          System.exit(1);
      }
    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
      e.printStackTrace();
      System.exit(1);
    }
  }

  /**
   * Runs the application in interactive mode.
   * It now accepts CalendarDatabase instead of individual services.
   *
   * @param calendarDatabase the calendar database
   * @param exportService the export service
   */
  private static void runInteractiveMode(CalendarDatabase calendarDatabase,
                                         InExportService exportService) {
    Objects.requireNonNull(calendarDatabase, "CalendarDatabase cannot be null");
    Objects.requireNonNull(exportService, "ExportService cannot be null");

    InteractiveView view = new InteractiveView();
    CalendarController controller =
        new CalendarController(calendarDatabase, exportService, view);

    controller.start();

    while (controller.isRunning()) {
      String command = view.getNextCommand();
      try {
        controller.executeCommand(command);
      } catch (Exception e) {
        System.err.println("Error: " + e.getMessage());
      }
    }

    view.close();
  }

  /**
   * Runs the application in headless mode.
   * Now accepts CalendarDatabase instead of individual services.
   *
   * @param calendarDatabase the calendar database
   * @param exportService the export service
   * @param filePath the path to the command file
   * @throws IOException if file reading fails
   */
  private static void runHeadlessMode(CalendarDatabase calendarDatabase,
                                      InExportService exportService,
                                      String filePath) throws IOException {
    Objects.requireNonNull(calendarDatabase, "CalendarDatabase cannot be null");
    Objects.requireNonNull(exportService, "ExportService cannot be null");
    Objects.requireNonNull(filePath, "File path cannot be null");

    HeadlessView view = new HeadlessView(Paths.get(filePath));
    CalendarController controller =
        new CalendarController(calendarDatabase, exportService, view);

    controller.start();
    view.executeCommandsFromFile(controller);
  }
}