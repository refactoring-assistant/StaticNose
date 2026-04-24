import app.CalendarApp;
import controller.CalendarController;
import controller.CommandProcessor;
import java.io.FileNotFoundException;
import messaging.Messages;
import view.CalendarView;

/**
 * This class represents the CalendarRunner. It determines what mode
 * calendar application will enter.
 */
public class CalendarRunner {
  static CommandProcessor controller;

  /**
   * The main method that starts the calendar program.
   *
   * @param args whatever is in the command-line
   */
  public static void main(String[] args) {
    CalendarApp.Config cfg = CalendarApp.parseArgs(args);
    CalendarController calendarController = new CalendarController();
    controller = calendarController;
    try {
      switch (cfg.mode) {
        case GUI:
          CalendarView.launch(calendarController);
          break;
        case INTERACTIVE:
          CalendarApp.runInteractive(System.in, System.out, controller);
          break;
        case HEADLESS:
          CalendarApp.runHeadless(java.nio.file.Paths.get(cfg.filename), System.out, controller);
          break;
        default:
          throw new IllegalStateException("Unsupported mode: " + cfg.mode);
      }
    } catch (FileNotFoundException e) {
      Messages.error("No such file exists. exiting.");
    }
  }
}
