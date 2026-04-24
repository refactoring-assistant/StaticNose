package calendar.controller.text;

import calendar.controller.CalendarControllerInterface;
import calendar.controller.text.commandfactory.CommandInterface;
import calendar.model.CalendarManager;
import calendar.model.CalendarManagerInterface;
import calendar.model.CalendarModelInterface;
import calendar.view.CalendarViewInterface;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

/**
 * Calendar controller class which receives and sends data to delegate work to the model and view.
 */
public class CalendarController implements CalendarControllerInterface {
  private final CalendarViewInterface view;
  private final CommandParser parser;
  private final CalendarManagerInterface calendar;

  /**
   * Constructor to initialize the other components of the calendar application.
   * This constructor takes in a calendar manager instead of a direct calendar model.
   *
   * @param calendar the active calendar holding the business logic for this calendar
   * @param view     the display of this calendar
   * @param parser   parses the command line
   */
  public CalendarController(CalendarManagerInterface calendar, CalendarViewInterface view,
                            CommandParser parser) {
    if (calendar == null || view == null || parser == null) {
      throw new IllegalArgumentException(
          "Calendar, View, or Parser cannot be null when creating a calendar controller.");
    }
    this.calendar = calendar;
    this.view = view;
    this.parser = parser;
  }

  /**
   * Constructor to initialize the other components of the calendar application.
   *
   * @param model  the business logic for this calendar
   * @param view   the display of this calendar
   * @param parser parses the command line
   * @throws IllegalArgumentException for invalid input
   */
  public CalendarController(CalendarModelInterface model, CalendarViewInterface view,
                            CommandParser parser) throws IllegalArgumentException {
    if (model == null || view == null || parser == null) {
      throw new IllegalArgumentException(
          "Model, View, or Parser cannot be null when creating a calendar controller.");
    }
    this.view = view;
    this.parser = parser;
    this.calendar = new CalendarManager();
  }

  @Override
  public void interactiveMode() {
    startProcessCommands(new InputStreamReader(System.in), true);
  }

  @Override
  public void headlessMode(String fileName) {
    try {
      FileReader fileReader = new FileReader(fileName);
      startProcessCommands(fileReader, false);
    } catch (IOException e) {
      view.displayError("Error reading command file: " + e.getMessage());
    }
  }

  /**
   * Processes commands from a Readable input source.
   *
   * @param input       the Readable source (System.in, FileReader, etc.)
   * @param interactive true if running in interactive mode, false for headless
   */
  private void startProcessCommands(Readable input, boolean interactive) {
    Scanner scanner = new Scanner(input);
    boolean exited = false;

    if (interactive) {
      view.display("Enter command (or 'exit' to quit): ");
    }

    while (scanner.hasNextLine()) {
      if (interactive) {
        System.out.print("-> ");
      }

      String command = scanner.nextLine();

      if (command.trim().isEmpty()) {
        continue;
      }

      if (command.trim().equalsIgnoreCase("exit")) {
        exited = true;
        view.display("Exit command encountered. Terminating "
            + (interactive ? "interactive" : "headless") + " mode.");
        break;
      }

      processCommand(command);
    }

    scanner.close();

    if (!exited && !interactive) {
      view.displayError("No exit command found at EOF. Terminating headless mode forcibly.");
    }
  }

  @Override
  public void guiMode() {
    view.displayError("GUI Mode not supported in text based mode.");
  }

  @Override
  public void processCommand(String command) {
    try {
      CommandInterface executable = this.parser.parse(command, this.calendar, this.view);
      executable.execute();
    } catch (Exception e) {
      view.displayError(e.getMessage());
    }
  }

  @Override
  public void showUsageAndExit() {
    String error = "Invalid command-line arguments." + System.lineSeparator() + "Usage:"
        + System.lineSeparator() + "  java -jar JARNAME.jar" + System.lineSeparator()
        + "    Launches the graphical user interface" + System.lineSeparator()
        + "  java -jar JARNAME.jar --mode headless <script-file>" + System.lineSeparator()
        + "    Runs the application in headless mode with a script file" + System.lineSeparator()
        + "  java -jar JARNAME.jar --mode interactive" + System.lineSeparator()
        + "    Runs the application in interactive text mode";
    view.displayError(error);
  }
}