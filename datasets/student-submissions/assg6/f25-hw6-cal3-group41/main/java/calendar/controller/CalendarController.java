package calendar.controller;

import calendar.controller.command.Command;
import calendar.controller.command.CommandFactory;
import calendar.model.MultiCalendarModel;
import calendar.view.CalendarView;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Controller that coordinates between model and view using the Command pattern.
 */
public class CalendarController {
  private final MultiCalendarModel multiModel;
  private final CalendarView view;
  private final CommandFactory commandFactory;

  /**
   * Creates a CalendarController with the given multi-calendar model and view.
   *
   * @param multiModel the multi-calendar model to use
   * @param view  the view to use for output
   */
  public CalendarController(MultiCalendarModel multiModel, CalendarView view) {
    this.multiModel = multiModel;
    this.view = view;
    this.commandFactory = new CommandFactory(multiModel);
  }

  /**
   * Runs the calendar in interactive mode.
   * Reads user input from the console and processes commands until the user types 'exit'.
   *
   * @param in the input reader
   * @throws IOException if I/O error occurs
   */
  public void runInteractive(BufferedReader in) throws IOException {
    for (; ; ) {
      view.printPrompt("> ");
      String line = in.readLine();
      if (line == null) {
        break;
      }
      String trimmed = line.trim();
      if (trimmed.equalsIgnoreCase("exit")) {
        view.println("Bye!");
        break;
      }
      dispatch(trimmed);
    }
  }

  /**
   * Runs the calendar in headless mode.
   * Reads commands from a script file. Ends when an 'exit' command is found.
   *
   * @param script the path to the script file
   * @throws IOException if I/O error occurs
   */
  public void runHeadless(Path script) throws IOException {
    boolean sawExit = false;
    for (String line : Files.readAllLines(script)) {
      String trimmed = line.trim();
      if (trimmed.isEmpty() || trimmed.startsWith("#")) {
        continue;
      }
      if (trimmed.equalsIgnoreCase("exit")) {
        sawExit = true;
        break;
      }
      dispatch(trimmed);
    }
    if (!sawExit) {
      view.println("ERROR: headless script ended without an 'exit' command.");
    }
  }

  /**
   * Processes a single command using the Command pattern.
   *
   * @param cmd the command string to process
   */
  private void dispatch(String cmd) {
    String out;
    try {
      Command command = commandFactory.createCommand(cmd);
      if (command == null) {
        out = "ERROR: Unknown command: " + cmd;
      } else {
        out = command.execute(cmd);
      }
    } catch (IllegalArgumentException e) {
      out = "ERROR: " + e.getMessage();
    } catch (Exception e) {
      out = "ERROR: " + e.getClass().getSimpleName() + ": " + e.getMessage();
    }
    view.println(out);
  }
}
