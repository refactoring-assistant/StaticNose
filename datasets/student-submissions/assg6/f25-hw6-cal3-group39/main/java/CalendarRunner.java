import calendar.controller.Controller;
import calendar.controller.ControllerImpl;
import calendar.controller.GuiControllerImpl;
import calendar.model.Model;
import calendar.model.ModelImpl;
import calendar.view.GuiViewImpl;
import calendar.view.TerminalView;
import calendar.view.View;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

/**
 * Entry point for the Calendar application.
 * Supports interactive and headless modes.
 */
public class CalendarRunner {

  /**
   * Starts the Calendar application in the specified mode.
   * In interactive mode, the user can enter commands in the console.
   * In headless mode, commands are read from the specified file.
   */
  public static void main(String[] args) {

    Model model = new ModelImpl();
    if (args.length == 0) {
      try {
        System.out.println("Launching GUI mode...");
        GuiViewImpl view = new GuiViewImpl();
        GuiControllerImpl controller = new calendar.controller.GuiControllerImpl(model, view);
        view.setFeatures(controller);
        controller.initialize();
        view.setVisible(true);
      } catch (Exception e) {
        //ignored
      }
      return;
    }


    String mode = args[1].toLowerCase();

    View tview = new TerminalView(System.out);
    Readable in;

    try {
      if (mode.equals("interactive")) {
        in = new InputStreamReader(System.in);
        Controller controllerImpl = new ControllerImpl(in, model, tview, true);
        controllerImpl.go();
      } else if (mode.equals("headless")) {
        if (args.length < 3) {
          System.err.println("Headless mode requires a command file.");
          return;
        }
        File file = new File(args[2]);
        if (!file.exists()) {
          System.err.println("calendar.Commands.Command file not found: " + args[2]);
          return;
        }
        FileInputStream fis = new FileInputStream(file);
        in = new InputStreamReader(fis);
        ControllerImpl controllerImpl = new ControllerImpl(in, model, tview, false);
        controllerImpl.go();
      } else {
        System.err.println("Unknown mode: " + args[1]);
      }
    } catch (FileNotFoundException e) {
      System.err.println("Error opening file: " + e.getMessage());
    }
  }
}
