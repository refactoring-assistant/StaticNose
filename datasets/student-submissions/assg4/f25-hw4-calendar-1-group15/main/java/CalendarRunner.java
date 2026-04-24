import app.CalendarApp;

/**
 * Entry point for the Calendar Application that processes command-line arguments
 * and launches the application in interactive or headless mode.
 * All times in the application are Eastern Standard Time (EST).
 */
public class CalendarRunner {
  /**
   * Main method that starts the calendar application.
   *
   * @param args command-line arguments: --mode interactive OR --mode headless &lt;filename&gt;
   */
  public static void main(String[] args) {
    CalendarRunner runner = new CalendarRunner();
    runner.run(args);
  }

  void run(String[] args) {
    CalendarApp app = new CalendarApp();

    if (args.length < 2) {
      printUsage();
      return;
    }

    String modeFlag = args[0].toLowerCase();
    String modeValue = args[1].toLowerCase();

    if (!modeFlag.equals("--mode")) {
      System.err.println("Error: Invalid flag '" + modeFlag + "'");
      printUsage();
      return;
    }

    switch (modeValue) {
      case "interactive":
        app.runInteractive();
        break;

      case "headless":
        if (args.length < 3) {
          System.err.println("Error: Headless mode requires a filename");
          printUsage();
          return;
        }
        app.runHeadless(args[2]);
        break;

      default:
        System.err.println("Error: Invalid mode '" + modeValue + "'");
        printUsage();
    }
  }

  static void printUsage() {
    System.out.println("Usage:");
    System.out.println("  Interactive: java -jar calendar.jar --mode interactive");
    System.out.println("  Headless:    java -jar calendar.jar --mode headless <filename>");
  }
}