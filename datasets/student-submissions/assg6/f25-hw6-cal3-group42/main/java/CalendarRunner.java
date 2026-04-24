import calendar.controller.CalendarControllerInterface;
import calendar.controller.gui.CalendarGuiController;
import calendar.controller.text.CalendarController;
import calendar.controller.text.CommandParser;
import calendar.model.CalendarManager;
import calendar.model.CalendarManagerInterface;
import calendar.view.CalendarViewInterface;
import calendar.view.gui.CalendarGuiViewImpl;
import calendar.view.text.CalendarView;

/**
 * Main program runner to run the calendar application.
 * It will start in either interactive or in headless mode and takes in arguments via the
 * command line.
 */
public class CalendarRunner {

  /**
   * Main method called on app start.
   * Takes in command line arguments and sends it to the Runner.
   *
   * @param args command line arguments as list of string
   */
  public static void main(String[] args) {
    CalendarManagerInterface calendar = new CalendarManager();

    if (args.length == 0) {
      CalendarViewInterface guiView = new CalendarGuiViewImpl();
      CalendarControllerInterface controller = new CalendarGuiController(guiView, calendar);
      controller.guiMode();

    } else if (args.length >= 2 && args[0].equalsIgnoreCase("--mode")) {
      CalendarViewInterface view = new CalendarView();
      CommandParser parser = new CommandParser();
      CalendarControllerInterface controller = new CalendarController(calendar, view, parser);

      if (args[1].equalsIgnoreCase("interactive") && args.length == 2) {
        controller.interactiveMode();

      } else if (args[1].equalsIgnoreCase("headless") && args.length == 3) {
        controller.headlessMode(args[2]);

      } else {
        controller.showUsageAndExit();
      }
    }
  }
}