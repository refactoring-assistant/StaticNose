import calendar.controller.CalendarController;
import calendar.controller.GuiController;
import calendar.model.CalendarSystemModel;
import calendar.model.CalendarSystemModelImpl;
import calendar.view.CalendarView;
import calendar.view.FileView;
import calendar.view.InteractiveView;
import calendar.view.gui.LightTheme;
import calendar.view.gui.SwingCalendarView;
import calendar.view.gui.UiTheme;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import javax.swing.UIManager;

/**
 * Entry point for the calendar application.
 */
public class CalendarRunner {

  /**
   * Main method to run the calendar in GUI, interactive or headless mode.
   *
   * @param args command-line arguments
   */
  public static void main(String[] args) {
    if (args.length == 0) {
      runGui();
      return;
    }

    if (args.length == 2
        && "--mode".equalsIgnoreCase(args[0])
        && "interactive".equalsIgnoreCase(args[1])) {
      runInteractive();
      return;
    }

    if (args.length == 3
        && "--mode".equalsIgnoreCase(args[0])
        && "headless".equalsIgnoreCase(args[1])) {
      runHeadless(args[2], null);
      return;
    }

    if (args.length == 4
        && "--mode".equalsIgnoreCase(args[0])
        && "headless".equalsIgnoreCase(args[1])) {
      runHeadless(args[2], args[3]);
      return;
    }

    printUsage();
  }

  /**
   * Launches the Swing GUI using the light theme and Nimbus look and feel.
   */
  private static void runGui() {
    try {
      UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
    } catch (Exception e) {
      System.err.println("Could not set Nimbus look and feel: " + e.getMessage());
    }

    CalendarSystemModel model = new CalendarSystemModelImpl();
    UiTheme theme = new LightTheme();
    SwingCalendarView view = new SwingCalendarView(theme);
    GuiController controller = new GuiController(model, view);
    view.setFeatures(controller);
    view.showView();
  }

  /**
   * Launches the interactive console mode.
   */
  private static void runInteractive() {
    CalendarSystemModel model = new CalendarSystemModelImpl();
    CalendarView view = new InteractiveView();
    CalendarController controller = new CalendarController(model, view);
    try {
      controller.run();
    } catch (IOException e) {
      System.err.println("Error: " + e.getMessage());
    }
  }

  /**
   * Runs the headless mode, reading commands from a file and optionally
   * writing output to another file.
   *
   * @param inputFile  path to input command file
   * @param outputFile path to output file or null for standard output
   */
  private static void runHeadless(String inputFile, String outputFile) {
    try (Reader in = new FileReader(inputFile)) {

      Writer out;
      boolean writingToStdout;

      if (outputFile != null) {
        out = new FileWriter(outputFile);
        writingToStdout = false;
      } else {
        out = new OutputStreamWriter(System.out, StandardCharsets.UTF_8);
        writingToStdout = true;
      }

      try {
        CalendarSystemModel model = new CalendarSystemModelImpl();
        CalendarView view = new FileView(in, out);
        CalendarController controller = new CalendarController(model, view);
        controller.run();
        out.flush();
      } finally {
        if (!writingToStdout) {
          try {
            out.close();
          } catch (IOException e) {
            System.err.println("Error closing output: " + e.getMessage());
          }
        }
      }

    } catch (IOException e) {
      System.err.println("Error: " + e.getMessage());
    }
  }

  /**
   * Prints usage information for the command-line interface.
   */
  private static void printUsage() {
    System.err.println("Invalid arguments. Use:");
    System.err.println(" --mode interactive");
    System.err.println(" --mode headless <inputFile> [outputFile]");
    System.err.println("Or no args for GUI mode.");
  }
}
