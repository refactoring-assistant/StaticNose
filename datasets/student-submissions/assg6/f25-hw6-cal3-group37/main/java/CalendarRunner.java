import calendar.controller.CalendarContext;
import calendar.controller.CalendarController;
import calendar.controller.GuiCalendarController;
import calendar.model.CalendarSystem;
import calendar.model.CalendarSystemImpl;
import calendar.view.CalendarView;
import calendar.view.ConsoleView;
import calendar.view.SwingCalendarView;
import javax.swing.SwingUtilities;

/**
 * Main entry point for the Calendar Application.
 * Supports three modes: GUI (default), interactive, and headless.
 *
 * <p>Usage:
 *   java CalendarRunner                    → GUI mode
 *   java CalendarRunner --mode interactive → Text mode
 *   java CalendarRunner --mode headless commands.txt → Script mode
 */
public class CalendarRunner {

  /**
   * Main method to run the calendar application.
   *
   * @param args command line arguments
   */
  public static void main(String[] args) {
    // No arguments → GUI mode
    if (args.length == 0) {
      launchGui();
      return;
    }

    // Check for valid arguments
    if (args.length < 2) {
      System.err.println("Usage:");
      System.err.println("  java CalendarRunner                    (GUI mode)");
      System.err.println("  java CalendarRunner --mode interactive (text mode)");
      System.err.println("  java CalendarRunner --mode headless <file> (script mode)");
      System.exit(1);
    }

    // Parse command line arguments
    if (!args[0].equalsIgnoreCase("--mode")) {
      System.err.println("Error: First argument must be '--mode'");
      System.exit(1);
    }

    String mode = args[1].toLowerCase();

    // Initialize MVC components with multi-calendar support
    CalendarSystem system = new CalendarSystemImpl();
    CalendarContext context = new CalendarContext(system);
    CalendarView view = new ConsoleView();
    CalendarController controller = new CalendarController(context, view);

    // Run in appropriate mode
    switch (mode) {
      case "interactive":
        controller.runInteractive();
        break;

      case "headless":
        if (args.length < 3) {
          System.err.println("Error: Headless mode requires a command file");
          System.exit(1);
        }
        controller.runHeadless(args[2]);
        break;

      default:
        System.err.println("Error: Invalid mode '" + mode + "'");
        System.err.println("Mode must be 'interactive' or 'headless'");
        System.exit(1);
    }
  }

  /**
   * Launches the GUI mode.
   */
  private static void launchGui() {
    SwingUtilities.invokeLater(() -> {
      CalendarSystem system = new CalendarSystemImpl();
      GuiCalendarController controller = new GuiCalendarController(system);
      SwingCalendarView view = new SwingCalendarView(controller);

      controller.setView(view);
      controller.initialize();
      view.display();
    });
  }
}