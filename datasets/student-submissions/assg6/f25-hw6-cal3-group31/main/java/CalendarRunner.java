import calendar.controller.Controller;
import calendar.controller.GuiController;
import calendar.controller.IntController;
import calendar.model.CalendarManager;
import calendar.model.IntCalendarManager;
import calendar.view.GuiView;
import calendar.view.IntGuiView;
import calendar.view.IntView;
import calendar.view.View;
import calendar.view.dialog.SwingDialogFactory;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintStream;

/**
 * Program runner.
 */
public class CalendarRunner {
  public static final int FILE_PATH_ARGUMENT_INDEX = 2;

  /**
   * The main method to run the calendar application.
   */
  public static void main(String[] args) {
    IntCalendarManager calendarManager = new CalendarManager();

    if (args.length == 0) {
      IntGuiView guiView = new GuiView();
      IntController guiController = new GuiController(
          calendarManager, guiView, SwingDialogFactory.getDefault());
      guiController.go();
    } else if (args.length < 2) {
      throw new IllegalArgumentException(
          "Incorrect args. Usage: [--mode] [interactive | headless <path>]");
    } else {
      for (int i = 0; i < args.length; i++) {
        if (i == FILE_PATH_ARGUMENT_INDEX) {
          continue;
        }
        args[i] = args[i].toLowerCase();
      }

      switch (args[1]) {
        case "headless":
          if (args.length < 3) {
            throw new IllegalArgumentException(
                "Incorrect args. Usage: [--mode] [interactive | headless <path>]");
          }

          try {
            BufferedReader in = new BufferedReader(new FileReader(args[FILE_PATH_ARGUMENT_INDEX]));
            IntView view = new View(new PrintStream(System.out));
            IntController controller = new Controller(in, view, calendarManager);
            controller.go();
          } catch (FileNotFoundException e) {
            throw new IllegalStateException("File not found: " + args[FILE_PATH_ARGUMENT_INDEX]);
          }
          break;
        case "interactive":
          InputStreamReader in = new InputStreamReader(System.in);
          IntView view = new View(new PrintStream(System.out));
          IntController controller = new Controller(in, view, calendarManager);
          controller.go();
          break;
        default:
          throw new IllegalArgumentException(
              "Unknown command. Usage: [--mode] [headless | interactive]");
      }
    }
  }
}
