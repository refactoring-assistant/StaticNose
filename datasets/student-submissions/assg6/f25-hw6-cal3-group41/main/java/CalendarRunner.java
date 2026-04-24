import calendar.controller.CalendarController;
import calendar.controller.gui.GuiController;
import calendar.model.MultiCalendarModel;
import calendar.view.CalendarView;
import calendar.view.ConsoleView;
import calendar.view.gui.SwingGuiView;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;

/**
 * Chooses between interactive mode  and headless mode .
 */
public class CalendarRunner {
  /**
   * Starts the program.
   *
   * @param args command-line arguments
   * @throws Exception if file I/O or command execution fails
   */
  public static void main(String[] args) throws Exception {
    if (args == null) {
      throw new NullPointerException();
    }

    if (args.length == 0) {
      MultiCalendarModel model = new MultiCalendarModel();
      SwingGuiView gui = new SwingGuiView();
      GuiController guiController = new GuiController(model, gui);
      return;
    }

    if (args.length >= 2 && "--mode".equalsIgnoreCase(args[0])) {
      String mode = args[1].toLowerCase();

      MultiCalendarModel multiModel = new MultiCalendarModel();
      CalendarView view = new ConsoleView();
      CalendarController controller = new CalendarController(multiModel, view);

      switch (mode) {
        case "interactive":
          controller.runInteractive(new BufferedReader(new InputStreamReader(System.in)));
          return;

        case "headless":
          if (args.length >= 3) {
            controller.runHeadless(Path.of(args[2]));
          } else {
            System.err.println("Usage: --mode interactive | --mode headless <file>");
          }
          return;

        default:
          System.err.println("Usage: --mode interactive | --mode headless <file>");
          return;
      }
    }

    System.err.println("Usage: --mode interactive | --mode headless <file>");
  }
}

