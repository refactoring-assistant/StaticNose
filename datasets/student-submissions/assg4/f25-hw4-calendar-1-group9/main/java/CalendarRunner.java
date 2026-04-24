import calendar.controller.CalendarController;
import calendar.model.Calendar;
import calendar.model.InCalendar;
import calendar.repository.InEventRepository;
import calendar.repository.InMemoryEventRepository;
import calendar.service.CsvExportService;
import calendar.service.EventService;
import calendar.service.InEventService;
import calendar.service.InExportService;
import calendar.view.HeadlessView;
import calendar.view.InteractiveView;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Main entry point for Calendar Application.
 * Parses command-line arguments and initializes appropriate mode.
 * Usage:
 *   java -jar calendar.jar --mode interactive
 *   java -jar calendar.jar --mode headless commands.txt
 * Both mode flag and mode value are case-insensitive per requirements.
 */
public class CalendarRunner {

  private static final String DEFAULT_CALENDAR_NAME = "MyCalendar";
  private static final String MODE_FLAG = "--mode";
  private static final String MODE_INTERACTIVE = "interactive";
  private static final String MODE_HEADLESS = "headless";

  private static final String USAGE_MESSAGE =
      "Usage: java -jar calendar.jar --mode [interactive|headless] [filepath]";
  private static final String INVALID_FLAG_MESSAGE = "Invalid flag. Expected: --mode";
  private static final String INVALID_MODE_MESSAGE =
      "Invalid mode. Use 'interactive' or 'headless'";
  private static final String HEADLESS_FILEPATH_REQUIRED =
      "Headless mode requires file path";
  private static final String FILE_NOT_FOUND_MESSAGE =
      "Command file not found: ";

  /**
   * Main method.
   *
   * @param args command-line arguments
   */
  public static void main(String[] args) {
    if (args.length < 2) {
      System.err.println(USAGE_MESSAGE);
      System.exit(1);
    }

    String modeFlag = args[0].toLowerCase();
    String mode = args[1].toLowerCase();

    if (!MODE_FLAG.equals(modeFlag)) {
      System.err.println(INVALID_FLAG_MESSAGE);
      System.exit(1);
    }

    InEventRepository repository = new InMemoryEventRepository();
    InCalendar calendar = new Calendar(DEFAULT_CALENDAR_NAME, repository);
    InEventService eventService = new EventService(calendar);
    InExportService exportService = new CsvExportService(calendar);

    try {
      if (MODE_INTERACTIVE.equals(mode)) {
        runInteractiveMode(eventService, exportService);
      } else if (MODE_HEADLESS.equals(mode)) {
        if (args.length < 3) {
          System.err.println(HEADLESS_FILEPATH_REQUIRED);
          System.exit(1);
        }
        String filePath = args[2];
        validateFilePath(filePath);
        runHeadlessMode(eventService, exportService, filePath);
      } else {
        System.err.println(INVALID_MODE_MESSAGE);
        System.exit(1);
      }
    } catch (IOException e) {
      System.err.println("File error: " + e.getMessage());
      System.exit(1);
    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
      e.printStackTrace();
      System.exit(1);
    }
  }

  /**
   * Validates that the file path is not null/empty and file exists.
   *
   * @param filePath the file path to validate
   * @throws IOException if file path is invalid or file doesn't exist
   */
  private static void validateFilePath(String filePath) throws IOException {
    if (filePath == null || filePath.trim().isEmpty()) {
      throw new IOException("File path cannot be null or empty");
    }

    Path path = Paths.get(filePath);
    if (!Files.exists(path)) {
      throw new IOException(FILE_NOT_FOUND_MESSAGE + filePath);
    }
  }

  /**
   * Runs the application in interactive mode.
   * User can type commands interactively until exit command.
   *
   * @param eventService  the event service
   * @param exportService the export service
   */
  private static void runInteractiveMode(InEventService eventService,
                                         InExportService exportService) {
    InteractiveView view = new InteractiveView();
    CalendarController controller =
        new CalendarController(eventService, exportService, view);

    controller.start();

    while (controller.isRunning()) {
      String command = view.getNextCommand();
      try {
        controller.executeCommand(command);
      } catch (Exception e) {
        // Empty Catch for purpose.
      }
    }

    view.close();
  }

  /**
   * Runs the application in headless mode.
   * Executes commands from file sequentially.
   *
   * @param eventService  the event service
   * @param exportService the export service
   * @param filePath      the path to the command file
   * @throws IOException if file reading fails
   */
  private static void runHeadlessMode(InEventService eventService,
                                      InExportService exportService,
                                      String filePath) throws IOException {
    HeadlessView view = new HeadlessView(Paths.get(filePath));
    CalendarController controller =
        new CalendarController(eventService, exportService, view);

    controller.start();
    view.executeCommandsFromFile(controller);
  }
}