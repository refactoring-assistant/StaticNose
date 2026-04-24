import calendar.controller.Controller;
import calendar.controller.ControllerV1;
import calendar.model.Model;
import calendar.model.ModelV1;
import calendar.util.PickReadable;
import calendar.view.AppendableView;
import calendar.view.View;
import java.util.Arrays;

/**
 * Calendar application entry point.
 */
public class CalendarRunner {
  /**
   * Entry point of the program.
   *
   * @param args command line argument
   */
  public static void main(String[] args) {
    Readable readable = PickReadable.load(args);

    if (readable == null) {
      return;
    }

    Model model = new ModelV1();
    View view = new AppendableView(System.out);
    Controller controller = new ControllerV1(readable);
    controller.go(model, view);
  }
}
