import calendar.controller.ControllerImpl;
import calendar.controller.Icontroller;
import calendar.model.CalendarInterface;
import calendar.model.CalendarModelImpl;
import calendar.view.Iview;
import calendar.view.IviewImpl;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Main method for the CalendarRunner Application.
 * Supports two modes:
 * <ul>
 *   <li> Interactive mode: {@code java CalendarRunner}
 *   <li> Headless mode: {@code java CalenderRunner <filename>}
 * </ul>
 */
public class CalendarRunner {
  /**
   * Constructor for the main method.
   */
  public static void main(String[] args) {
    if (args.length < 2) {
      System.err.println("ERROR: Invalid arguments. See USEME.md for usage instructions.");
      System.exit(1);
    }

    if (!args[0].equalsIgnoreCase("--mode")) {
      System.err.println("ERROR: First argument must be '--mode'. "
          + "See USEME.md for usage instructions.");
      System.exit(1);
    }

    String mode = args[1].toLowerCase();
    CalendarInterface model = new CalendarModelImpl();
    Iview view = new IviewImpl(System.out, System.err);
    Icontroller controller = new ControllerImpl(model, view);
    Reader input = null;

    try {
      if (mode.equals("interactive")) {
        input = new InputStreamReader(System.in);
        controller.run(input);

      } else {
        String filename = args[2];

        if (!validateHeadlessFile(filename)) {
          System.err.println("ERROR: File must end with an exit command");
          System.exit(1);
        }

        input = new FileReader(filename);
        controller.run(input);
      }

    } catch (FileNotFoundException e) {
      System.err.println("ERROR: File not found: " + args[2]);
      System.exit(1);
    } catch (IOException e) {
      System.err.println("ERROR: IO error: " + e.getMessage());
      System.exit(1);
    } catch (Exception e) {
      System.err.println("ERROR: " + e.getMessage());
      System.exit(1);
    } finally {
      if (input != null) {
        try {
          input.close();
        } catch (IOException e) {
          System.err.println("Warning: Failed to close input stream");
        }
      }
    }
  }

  /**
   * Validates that a headless mode file ends with an exit command.
   *
   * @param filename the file to validate
   * @return true if file ends with exit command, false otherwise
   */
  private static boolean validateHeadlessFile(String filename) {
    try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
      String line;
      String lastNonEmptyLine = null;

      while ((line = reader.readLine()) != null) {
        String trimmed = line.trim();
        if (!trimmed.isEmpty()) {
          lastNonEmptyLine = trimmed;
        }
      }

      // Check if last non-empty line is an exit command
      if (lastNonEmptyLine == null) {
        return false; // Empty file
      }

      // Case-insensitive check for exit command
      return lastNonEmptyLine.toLowerCase().equals("exit");

    } catch (IOException e) {
      return false;
    }
  }
}
