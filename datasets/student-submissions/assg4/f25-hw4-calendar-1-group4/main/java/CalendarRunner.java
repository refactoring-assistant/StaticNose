import calendar.DummyCalendar;
import calendar.controller.CalendarExecutioner;
import calendar.model.CalendarImpl;
import calendar.model.Icalendar;
import calendar.view.CalendarViewImpl;
import calendar.view.IcalendarView;
import java.io.InputStreamReader;

/**
 * Program runner.
 */
public class CalendarRunner {
  /**
   * The main method placeholder.
   */
  public static void main(String[] args) {

    if (args.length < 2 || !args[0].equalsIgnoreCase("--mode")) {
      System.err.println("Usage: --mode [interactive | headless <filename>]");
      System.exit(1);
    }

    String mode = args[1].toLowerCase();
    String filePath = args.length >= 3 ? args[2] : null;

    Icalendar model = new CalendarImpl();
    IcalendarView view = new CalendarViewImpl(System.out);
    CalendarExecutioner controller = new CalendarExecutioner(model, view,
        new InputStreamReader(System.in));
    controller.start(mode, filePath);
  }
}
