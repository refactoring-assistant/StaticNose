import calendar.controller.CalController;
import calendar.controller.CalendarController;
import calendar.controller.UiCalController;
import calendar.service.CalendarService;
import calendar.service.CalendarServiceImpl;
import calendar.view.guibased.CalendarSwingViewImpl;
import calendar.view.guibased.SwingView;
import calendar.view.textbased.CalView;
import calendar.view.textbased.CalendarView;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Main entry point for the Calendar application.
 * This class is responsible for "wiring" all the application components
 * together (Dependency Injection) and launching the controller.
 */
public class CalendarRunner {
  /**
   * The main method.
   */
  public static void main(String[] args) {

    CalendarService service = new CalendarServiceImpl();

    if (args.length == 0) {
      SwingView view = new CalendarSwingViewImpl();
      UiCalController controller = new UiCalController(service);
      controller.setView(view);
      view.display();
    } else {
      CalendarView view = new CalView(System.out);
      InputStream inStream;
      if (args.length == 2 && args[0].equalsIgnoreCase("--mode")
          && args[1].equalsIgnoreCase("interactive")) {

        inStream = System.in;

      } else if (args.length == 3 && args[0].equalsIgnoreCase("--mode")
          && args[1].equalsIgnoreCase("headless")) {

        String fileName = args[2];
        try {
          inStream = new FileInputStream(fileName);
        } catch (FileNotFoundException e) {
          view.showError("Error: Command file not found: " + fileName);
          return;
        }

      } else {
        view.showError("Usage: java CalendarRunner "
            + "--mode <interactive|headless> [filename] OR no arguments for GUI");
        return;
      }

      CalendarController controller = new CalController(service, view, inStream);
      controller.run();
    }
  }
}