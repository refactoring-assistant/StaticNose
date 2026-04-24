import calendar.controller.CommandProcessor;
import calendar.model.CalendarModel;
import calendar.model.CalendarModelImpl;
import calendar.view.CommandLineView;
import calendar.view.gui.CalendarGui;
import java.awt.GraphicsEnvironment;
import java.time.ZoneId;
import java.util.Locale;

/**
 * Entry point for the calendar application.
 */
public final class CalendarRunner {

  private CalendarRunner() {
  }

  /**
   * Main entry point. Supports interactive and headless modes.
   *
   * @param args command line arguments
   */
  public static void main(String[] args) {
    CalendarModel model = bootstrapModel();
    if (args.length == 0) {
      if (GraphicsEnvironment.isHeadless()) {
        System.err.println("GUI mode is unavailable in a headless environment.");
        printUsage();
      } else {
        CalendarGui gui = new CalendarGui(model);
        gui.show();
      }
      return;
    }
    if (!args[0].equalsIgnoreCase("--mode")) {
      System.err.println("Expected '--mode' as the first argument.");
      printUsage();
      return;
    }
    if (args.length < 2) {
      System.err.println("Missing mode argument.");
      printUsage();
      return;
    }

    CommandProcessor processor = new CommandProcessor(model);
    CommandLineView view = new CommandLineView(processor);
    String mode = args[1].toLowerCase(Locale.US);
    switch (mode) {
      case "interactive":
        if (args.length != 2) {
          System.err.println("Interactive mode does not take additional arguments.");
          return;
        }
        view.runInteractive();
        break;
      case "headless":
        if (args.length != 3) {
          System.err.println("Headless mode requires a command file path.");
          return;
        }
        view.runHeadless(args[2]);
        break;
      default:
        System.err.println("Unsupported mode: " + args[1]);
        printUsage();
    }
  }

  private static CalendarModel bootstrapModel() {
    CalendarModel model = new CalendarModelImpl();
    ZoneId defaultZone = ZoneId.systemDefault();
    model.createCalendar("Home", defaultZone);
    model.useCalendar("Home");
    return model;
  }

  private static void printUsage() {
    System.err.println("Usage:");
    System.err.println("  java CalendarRunner               # Launches the GUI");
    System.err.println("  java CalendarRunner --mode interactive");
    System.err.println("  java CalendarRunner --mode headless <commands-file>");
  }
}

