package calendar.cli;

import calendar.manager.CalendarManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

/**
 * Provides an interactive command-line interface for CalendarManager.
 */
public class InteractiveRunner {
  private final CalendarManager manager;
  private final InputStream in;
  private final PrintStream out;
  private final CommandParser parser = new CommandParser();

  /**
   * Creates a new interactive runner for the given calendar manager.
   *
   * @param manager the manager controlling multiple calendars
   * @param in      the input stream from which user commands will be read
   * @param out     the print stream to which output and error messages will be written
   */
  public InteractiveRunner(CalendarManager manager, InputStream in, PrintStream out) {
    this.manager = manager;
    this.in = in;
    this.out = out;
  }

  /**
   * Starts the interactive session. Repeatedly prompts the user for input until exit.
   */
  public void run() {
    try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
      String line;
      while (true) {
        out.print("> ");
        line = br.readLine();
        if (line == null || line.trim().equalsIgnoreCase("exit")) {
          break;
        }
        try {
          parser.parse(line).execute(manager, out);
        } catch (Exception e) {
          out.println("Error: " + e.getMessage());
        }
      }
    } catch (IOException e) {
      out.println("Fatal I/O: " + e.getMessage());
    }
  }
}
