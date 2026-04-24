import calendar.Calendar;
import calendar.CalenderController;
import calendar.CalenderView;
import java.text.ParseException;
import java.util.Objects;

/**
 * Program runner.
 */
public class CalendarRunner {
  /**
   * The main method placeholder 1 headless 0 interactive.
   */
  public static void main(String[] args) {
    Calendar model = new Calendar();
    CalenderView view = new CalenderView(System.out, System.out);
    CalenderController controller = new CalenderController(model, System.in, view);
    try {
      if (Objects.equals(args[1], "headless")) {
        controller.go(1, args[2]); //headless
      } else {
        controller.go(0, null); //interactive
      }
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }
}
