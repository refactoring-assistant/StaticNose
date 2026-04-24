import calendar.controllers.CommandFactory;
import calendar.controllers.CommandFactoryImpl;
import calendar.controllers.CommandParser;
import calendar.controllers.CommandParserImpl;
import calendar.controllers.Controller;
import calendar.controllers.ControllerImpl;
import calendar.models.Calendar;
import calendar.models.CalendarImpl;
import calendar.views.ConsoleView;
import calendar.views.ObservableView;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * The entry point of the Calendar application.
 *
 * <p>This class initializes and runs the calendar program in either {@code interactive} or
 * {@code headless} mode based on command-line arguments.
 * </p>
 *
 * <p><b>Usage:</b></p>
 * <pre>
 *   java -jar {JAR file} --mode interactive
 *   java -jar {JAR file} --mode headless commands.txt
 * </pre>
 *
 * <ul>
 *   <li><b>Interactive mode:</b> Runs the program with user input from the console.</li>
 *   <li><b>Headless mode:</b> Executes commands from a given file without user interaction.</li>
 * </ul>
 */
public class CalendarRunner {

  /**
   * The main method that starts the application in either interactive or headless mode.
   *
   * @param args command-line arguments. Expected formats:
   *             <ul>
   *               <li>{@code --mode interactive}</li>
   *               <li>{@code --mode headless <commandsFile>}</li>
   *             </ul>
   */
  public static void main(String[] args) {
    if (args.length < 2) {
      System.err.println("Usage:");
      System.err.println("  java <JAR file> --mode interactive");
      System.err.println("  java <JAR file> --mode headless <commandsFile>");
      return;
    }

    String mode = args[1].toLowerCase();

    switch (mode) {
      case "interactive":
        runInteractive();
        break;

      case "headless":
        if (args.length < 3) {
          System.err.println("Headless mode requires a command file path.");
          return;
        }
        runHeadless(args[2]);
        break;

      default:
        runInteractive();
        break;
    }
  }

  private static void runInteractive() {
    Calendar model = new CalendarImpl("MyCalendar");
    ObservableView view = new ConsoleView(System.out);
    CommandFactory factory = new CommandFactoryImpl(model, view);
    CommandParser parser = new CommandParserImpl(factory);
    Controller controller = new ControllerImpl(model, view, System.in, parser);
    try {
      controller.go();
    } catch (Exception e) {
      System.err.println("Error running in interactive mode: " + e.getMessage());
    }
  }

  private static void runHeadless(String filePath) {
    Calendar model = new CalendarImpl("MyCalendar");
    ObservableView view = new ConsoleView(System.out);
    CommandFactory factory = new CommandFactoryImpl(model, view);
    CommandParser parser = new CommandParserImpl(factory);
    try (InputStream fileIn = new FileInputStream(filePath)) {
      Controller controller = new ControllerImpl(model, view, fileIn, parser);
      controller.go();
    } catch (FileNotFoundException e) {
      view.displayError("Command file not found: " + filePath);
    } catch (Exception e) {
      view.displayError("Error running in headless mode: " + e.getMessage());
    }
  }
}
