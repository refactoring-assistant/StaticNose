import calendar.controller.Controller;
import calendar.model.Calendar;
import calendar.model.CalenderManager;
import calendar.view.ViewConsole;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Main class to design calendar by MVC design pattern. MVC principles followed:
 * - create model, view, and controller
 * - give control to controller
 * - NO direct interaction with user
 * - NO direct display of results
 * - does NOT implement functionality directly
 */
public class CalendarRunner {

  /**
   * The main method placeholder. Wires up MVC components and starts the application.
   */
  public static void main(String[] args) {
    // exit if less than 2 arguments
    if (args.length < 2) {
      // error messages
      System.err.println("Usage: java -jar [jarFile] --mode <interactive|headless> [commandsFile]");
      System.err.println("Interactive mode: --mode interactive");
      System.err.println("Headless mode: --mode headless [commandsFile]");
      throw (new IllegalArgumentException());
    }
    // check if mode flag does not contain mode
    if (!args[0].equalsIgnoreCase("--mode")) {
      System.err.println("First argument must be --mode");
      throw (new IllegalArgumentException());
    }

    CalenderManager manager = new CalenderManager();
    ViewConsole viewOb = new ViewConsole();
    Controller controller = new Controller(manager, viewOb);

    if (args[1].equalsIgnoreCase("interactive")) {
      // interactive mode
      interactiveMode(controller, viewOb);
    } else if (args[1].equalsIgnoreCase("headless")) {
      // headless mode
      String commandsFile = (args.length > 2) ? args[2] : "";
      headlessMode(controller, viewOb, commandsFile);
    } else {
      System.err.println("Mode must be either interactive or headless");
      throw (new IllegalArgumentException());
    }
  }

  /**
   * Private function to implement interactive mode. In this mode
   * user can type commands and see results immediately.
   * This is synchronous controller pattern.
   *
   * @param controller instance of Controller class
   * @param view instance of ViewConsole class
   * */
  private static void interactiveMode(Controller controller, ViewConsole view) {
    Scanner scanner = new Scanner(System.in);

    while (true) {
      System.out.print("> ");

      if (!scanner.hasNextLine()) {
        view.dispEnd();
        break;
      }

      String inputStr = scanner.nextLine().trim();
      // continue on empty input
      if (inputStr.isEmpty()) {
        continue;
      }
      // exit loop when exit is entered
      if (inputStr.equalsIgnoreCase("exit")) {
        view.dispEnd();
        break;
      }
      controller.processInput(inputStr);
    }
    scanner.close();
  }

  /**
   * Private function to run the application in headless mode.
   * Commands are read from a file and executed sequentially.
   * Unlike interactive mode, this is batch processing.
   *
   * @param controller instance of Controller class
   * @param view instance of ViewConsole class
   * @param filePath directory of command file
   * */
  private static void headlessMode(Controller controller, ViewConsole view, String filePath) {
    boolean exitFlag = false;
    File file = new File(filePath);

    if (!file.exists()) {
      System.err.println("File does not exist: " + filePath);
      throw new IllegalArgumentException("File does not exist");
    }

    try {
      String line;
      BufferedReader br = new BufferedReader(new FileReader(file));
      while ((line = br.readLine()) != null) {
        line = line.trim();
        boolean hasComment = line.startsWith("#");
        if (line.isEmpty() || hasComment) {
          // skip empty lines and comments
          continue;
        }
        if (line.equalsIgnoreCase("exit")) {
          exitFlag = true;
          view.dispEnd();
          break;
        }
        controller.processInput(line);
      }

      if (!exitFlag) {
        System.err.println("Command file does not end with exit command");
        throw new NoSuchElementException("Command file does not end with exit command");
      }
    } catch (IOException e) {
      System.err.println("Error reading commands file: " + filePath);
      throw  new NoSuchElementException("Error reading commands file: " + filePath);
    }
  }
}