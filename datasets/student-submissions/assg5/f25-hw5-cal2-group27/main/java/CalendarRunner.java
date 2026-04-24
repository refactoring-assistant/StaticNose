import calendar.controller.CalendarController;
import calendar.model.CalendarManager;
import calendar.view.CalendarTextView;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Scanner;

/**
 * Program runner.
 */
public class CalendarRunner {
  /**
   * The entry point for the calendar application.
   */
  public static void main(String[] args) {
    try {
      if (args.length == 0) {
        System.err.println("Mode not specified");
        System.err.println("Usage: --mode interactive | --mode headless <commands.txt>");
        return;
      }

      if ("--mode".equalsIgnoreCase(args[0])) {
        if (args.length < 2) {
          System.err.println("Mode type not specified");
          return;
        }

        String mode = args[1].toLowerCase(Locale.ROOT);

        CalendarManager manager = new CalendarManager();
        CalendarTextView view = new CalendarTextView();
        CalendarController controller = new CalendarController(manager, view);

        if ("interactive".equals(mode)) {
          try (Scanner sc = new Scanner(System.in)) {
            controller.run(sc, /*exitRequired=*/false);
          }
        } else if ("headless".equals(mode)) {
          if (args.length < 3) {
            System.err.println("Headless mode requires a command file.");
            return;
          }
          Path file = Paths.get(args[2]);
          try {
            if (!Files.isRegularFile(file)) {
              throw new IOException("File does not exist");
            }
            try (Scanner sc = new Scanner(Files.newBufferedReader(file))) {
              controller.run(sc, /*exitRequired=*/true);
            }
          } catch (IOException ioe) {
            System.err.println("Error reading command file: " + file);
          }
        } else {
          System.err.println("Invalid mode: " + args[1]);
        }
      } else {
        System.err.println("Invalid argument: " + args[0]);
      }
    } catch (RuntimeException e) {
      System.err.println("Unexpected error: " + e.getMessage());
    }
  }
}
