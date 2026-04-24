package calendar.cli;

import calendar.manager.CalendarManager;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Executes a series of calendar commands from a text file in headless mode.
 */
public class HeadlessRunner {
  private final CalendarManager manager;
  private final String file;
  private final PrintStream out;
  private final CommandParser parser = new CommandParser();

  /**
   * Creates a new headless runner for the given calendar manager and input file.
   *
   * @param manager the calendar manager on which commands will be executed
   * @param file    the path to the file containing commands
   * @param out     the PrintStream to write output and error messages
   */
  public HeadlessRunner(CalendarManager manager, String file, PrintStream out) {
    this.manager = manager;
    this.file = file;
    this.out = out;
  }

  /**
   * Executes commands from the input file sequentially.
   */
  public void run() {
    boolean sawExit = false;
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      String line;
      while ((line = br.readLine()) != null) {
        if (line.trim().equalsIgnoreCase("exit")) {
          sawExit = true;
          break;
        }
        try {
          parser.parse(line).execute(manager, out);
        } catch (Exception e) {
          out.println("Error: " + e.getMessage());
        }
      }
    } catch (IOException e) {
      out.println("Error reading file: " + e.getMessage());
    }

    if (!sawExit) {
      out.println("Error: headless file ended without an 'exit' command.");
    }
  }
}
