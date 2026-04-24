import calendar.controller.CalendarController;
import calendar.model.CalendarSystem;
import calendar.view.CalendarView;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Main entry point for the calendar application.
 */
public class CalendarRunner {
  /**
   * Main method to run the calendar application.
   */
  public static void main(String[] args) {
    CalendarSystem system = new CalendarSystem();
    CalendarView view = new CalendarView(System.out);
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    CalendarController controller = new CalendarController(system, view, reader);
    controller.start();
  }
}