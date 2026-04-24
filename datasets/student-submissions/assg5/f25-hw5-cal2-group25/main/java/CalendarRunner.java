import calendar.cli.HeadlessRunner;
import calendar.cli.InteractiveRunner;
import calendar.manager.CalendarManager;
import calendar.manager.CalendarManagerImpl;

/**
 * Entry point for the calendar application.
 * Supports interactive and headless (file-based) modes.
 */
public class CalendarRunner {

  /**
   * Main entry point for the program.
   */
  public static void main(String[] args) {
    int exitCode = run(args);
    System.exit(exitCode);
  }

  /**
   * Runs the calendar application with the given arguments.
   *
   * @param args command-line arguments
   * @return 0 if successful, otherwise 1
   */
  static int run(String[] args) {
    if (args == null || args.length == 0) {
      System.err.println(
          "Error: Missing arguments. Use --mode interactive | --mode headless <file>");
      return 1;
    }

    if (!"--mode".equalsIgnoreCase(args[0]) || args.length < 2) {
      System.err.println("Error: first arg must be --mode <interactive|headless> [file]");
      return 1;
    }

    String which = args[1].toLowerCase();
    CalendarManager manager = new CalendarManagerImpl();

    switch (which) {
      case "interactive":
        new InteractiveRunner(manager, System.in, System.out).run();
        return 0;
      case "headless":
        if (args.length < 3) {
          System.err.println("Error: headless mode requires a path to a commands file.");
          return 1;
        }
        new HeadlessRunner(manager, args[2], System.out).run();
        return 0;
      default:
        System.err.println("Error: unknown mode '" + which + "'. Use interactive | headless.");
        return 1;
    }
  }
}
