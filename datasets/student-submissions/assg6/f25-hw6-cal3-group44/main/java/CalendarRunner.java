import calendar.controller.AdvanceCalendarController;
import calendar.controller.CalendarController;
import calendar.controller.guicontroller.CalendarGuiController;
import calendar.model.CalendarContainerImpl;
import calendar.model.CalendarImpl;
import calendar.model.interfaces.CalendarContainer;
import calendar.model.interfaces.CalendarEditable;
import calendar.view.CalendarGuiView;
import calendar.view.CalendarGuiViewImpl;
import calendar.view.CalendarView;
import calendar.view.CalendarViewImpl;
import java.awt.EventQueue;

/**
 * Program runner.
 */
public class CalendarRunner {
  private static final CalendarContainer container = new CalendarContainerImpl();
  private static final CalendarEditable calendar = new CalendarImpl();
  private static final CalendarView view = new CalendarViewImpl(System.out);

  /**
   * Main method for the Calendar project.
   * Initializes the input source, sets up the controller, and runs the application.
   *
   * @param args Command-line arguments (used to determine interactive or headless input mode)
   */
  public static void main(String[] args) {
    if (args.length == 0) {
      EventQueue.invokeLater(() -> {
        try {
          CalendarGuiView guiView = new CalendarGuiViewImpl();
          CalendarController guiController = new CalendarGuiController(container, guiView);
          guiController.run();
        } catch (Exception e) {
          e.printStackTrace();
        }
      });
      return;
    }

    try {
      Readable input = InputFactory.getInput(args);
      CalendarView view = new CalendarViewImpl(System.out);
      CalendarController controller = new AdvanceCalendarController(container, input, view);
      controller.run();
    } catch (IllegalArgumentException e) {
      view.renderError("Error: " + e.getMessage());
      view.renderError("Usage:");
      view.renderError("  java -jar JARNAME.jar                    # GUI mode");
      view.renderError("  java -jar JARNAME.jar --mode interactive # Interactive text mode");
      view.renderError("  java -jar JARNAME.jar --mode headless <filename> # Headless mode");
    }
  }
}
