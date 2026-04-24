package calendar.control;

import calendar.control.commands.Icommand;
import calendar.control.results.CommandResult;
import calendar.model.database.IcalendarDatabase;
import calendar.utils.CommandParser;
import calendar.view.Iview;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Main controller implementation connecting the model and view layers.
 * Uses CommandParser to translate user input into executable commands.
 * Supports both interactive and headless modes.
 */
public class ControllerImpl implements Icontroller {

  private final Iview view;
  private final CommandParser parser;
  private final IcalendarDatabase multipleCalendar;


  /**
   * Creates a controller bound to a model and a view.
   *
   * @param multipleCalendar - the multiple calendar
   * @param view             active view (interactive or headless)
   */
  public ControllerImpl(IcalendarDatabase multipleCalendar, Iview view) {
    this.multipleCalendar = multipleCalendar;
    this.view = view;
    this.parser = new CommandParser(multipleCalendar, view);
  }


  @Override
  public void startInteractive() {
    view.print("Interactive Calendar started. Type a command or 'exit' to quit.");
    while (true) {
      String input = view.readInput();
      if (input == null || input.trim().equalsIgnoreCase("exit")) {
        view.print("Quitting.");
        break;
      }
      executeCommand(input);
    }
  }


  @Override
  public void startHeadless() {
    view.print("Headless Calendar started.");
    String path = view.getSourcePath();

    if (path == null) {
      view.print("Error: No input file provided for headless mode.");
      return;
    }

    Path filePath = Paths.get(path);
    if (!filePath.isAbsolute()) {
      filePath = Paths.get("res", path);
    }

    view.print("The source path is: " + filePath);

    try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
      String line;
      boolean exitFound = false;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (line.isEmpty()) {
          continue;
        }
        if (line.equalsIgnoreCase("exit")) {
          exitFound = true;
          break;
        }
        executeCommand(line);
      }
      if (!exitFound) {
        view.print("Error: commands file ended without an 'exit' command.");
      } else {
        view.print("Headless execution complete. \n Quitting.");
      }
    } catch (IOException e) {
      view.print("Error reading command file in headless mode: "
          + e.getMessage());
    }
  }

  /**
   * Parses and executes a single command line.
   *
   * @param input raw command text
   */
  private void executeCommand(String input) {
    try {
      Icommand cmd = parser.parse(input);
      CommandResult result = cmd.execute();
      view.print(result.getMessage());
    } catch (Exception e) {
      view.print("Error while executing command: " + e.getMessage());
    }
  }
}
