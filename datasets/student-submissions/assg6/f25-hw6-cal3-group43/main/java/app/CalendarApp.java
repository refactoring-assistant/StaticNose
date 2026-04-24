package app;

import controller.CommandProcessor;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Path;

/**
 * This class represents the CalendarApp, which supports both headless and
 * interactive modes.
 */
public class CalendarApp {

  /**
   * Parse arguments into a simple, testable configuration.
   *
   * @param args whatever is in the command line input
   * @return config
   * @throws IllegalArgumentException missing value after --mode
   * @throws IllegalArgumentException missing filename for headless
   * @throws IllegalArgumentException no mode specified
   * @throws IllegalArgumentException when an unknown mode is specified
   */
  public static Config parseArgs(String[] args) {
    if (args.length == 0) {
      return new Config(Mode.GUI, null);
    }

    String mode = null;
    String filename = null;
    for (int i = 0; i < args.length; i++) {
      if ("--mode".equalsIgnoreCase(args[i])) {
        if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
          mode = args[i + 1];
        } else {
          throw new IllegalArgumentException("Missing value after --mode");
        }
        // if headless, optional filename is next arg
        if ("headless".equalsIgnoreCase(mode)) {
          if (i + 2 < args.length) {
            filename = args[i + 2];
          } else {
            throw new IllegalArgumentException("Missing filename for headless mode");
          }
        }
        break;
      }
      if (args[i].toLowerCase().startsWith("--mode=")) {
        mode = args[i].substring(args[i].indexOf('=') + 1);
        if ("headless".equalsIgnoreCase(mode)) {
          if (i + 1 < args.length) {
            filename = args[i + 1];
          } else {
            throw new IllegalArgumentException("Missing filename for headless mode");
          }
        }
        break;
      }
    }
    if (mode == null) {
      throw new IllegalArgumentException("No --mode specified");
    }
    if ("interactive".equalsIgnoreCase(mode)) {
      return new Config(Mode.INTERACTIVE, null);
    }
    if ("headless".equalsIgnoreCase(mode)) {
      return new Config(Mode.HEADLESS, filename);
    }
    throw new IllegalArgumentException("Unknown mode: " + mode);
  }

  /**
   * Interactive runner: reads from 'in', writes to 'out'.
   *
   * @param in         where the stream reads from
   * @param out        where the stream writes to
   * @param controller the CommandProcessor that handles the execution
   */
  public static void runInteractive(InputStream in, PrintStream out, CommandProcessor controller) {
    out.println("mode = interactive");
    try (var scanner = new java.util.Scanner(in)) {
      String input = scanner.nextLine();
      while (!"exit".equalsIgnoreCase(input)) {
        controller.interpret(input);
        input = scanner.nextLine();
      }
      out.println("exit the program.");
    }
  }

  /**
   * Headless runner: reads commands file, writes to 'out'.
   *
   * @param file       the path to the file to read
   * @param out        where the stream writes to
   * @param controller the CommandProcessor that handles the execution
   * @throws FileNotFoundException when the file does not exist
   */
  public static void runHeadless(Path file, PrintStream out, CommandProcessor controller)
      throws FileNotFoundException {
    boolean exitEncountered = false;
    try (var sc = new java.util.Scanner(file.toFile())) {
      while (sc.hasNextLine()) {
        String command = sc.nextLine();
        out.println(command);
        controller.interpret(command);
        if ("exit".equalsIgnoreCase(command.trim())) {
          exitEncountered = true;
          break;
        }
      }
    }
    if (!exitEncountered) {
      String message = "Headless mode requires the last command to be 'exit'.";
      out.println(message);
      throw new IllegalStateException(message);
    }
  }

  /**
   * And enum that represents the calendar app modes.
   */
  public enum Mode { GUI, INTERACTIVE, HEADLESS }

  /**
   * Stores the configuration settings of the calendar app. Also keeps track
   * of the filename for headless mode.
   */
  public static final class Config {
    public final Mode mode;
    public final String filename; // only for HEADLESS

    /**
     * Creates a configuration for the calendar app.
     *
     * @param mode     the mode the of the application
     * @param filename the filename used for headless mode
     */
    public Config(Mode mode, String filename) {
      this.mode = mode;
      this.filename = filename;
    }
  }
}
