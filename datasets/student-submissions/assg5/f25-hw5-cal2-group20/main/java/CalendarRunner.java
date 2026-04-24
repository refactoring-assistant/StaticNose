import calendar.InputFactory;
import calendar.controller.CalendarController;
import calendar.controller.Controller;
import calendar.controller.DatabaseController;
import calendar.model.Calendar;
import calendar.model.CalendarDatabase;
import calendar.model.CalendarDatabaseModel;
import calendar.model.CalendarModel;
import calendar.view.CalendarView;
import calendar.view.TextView;

/**
 * Represents a CalendarRunner class that runs the main Calendar application and accepts inputs
 * from a user.
 */
public class CalendarRunner {
  /**
   * The main method of the Calendar that runs the controller and starts the calendar application.
   */
  public static void main(String[] args) {
    CalendarDatabaseModel db = new CalendarDatabase();
    CalendarView view = new TextView(System.out);
    if (args.length < 2) {
      view.renderMessage("Invalid mode. Valid modes are --mode interactive and --mode headless "
          + "commands.txt");
    } else {
      Readable input;
      try {
        input = InputFactory.makeInput(args);
        CalendarController controller = new DatabaseController(view, input, db);
        controller.run();
      } catch (IllegalStateException e) {
        view.renderMessage(e.getMessage());
      }
    }
  }
}
