import calendar.controller.CommandParser;
import calendar.model.CalendarModel;
import calendar.model.IcalendarModel;
import java.io.File;
import java.util.Scanner;


/**
 * Program runner.
 */
public class CalendarRunner {
  /**
   * The main method placeholder.
   */
  public static void main(String[] args) {
    if (args.length < 2) {
      System.out.println("Usage: ");
      System.out.println("--mode interactive");
      System.out.println("--mode headless <commandsFile");
      return;
    }

    String modeValue = args[1].toLowerCase();

    IcalendarModel model = new CalendarModel();
    CommandParser parser = new CommandParser(model);

    switch (modeValue) {
      case "interactive":
        interactiveMode(parser);
        break;

      case "headless":
        if (args.length < 3) {
          System.out.println("Error: Provide a file name");
          return;
        }
        String fileName = args[2];
        headlessMode(parser, fileName);
        break;

      default:
        System.out.println("Error: Unknown mode" + modeValue);
    }
  }

  private static void headlessMode(CommandParser parser, String fileName) {
    File file = new File(fileName);
    if (!file.exists()) {
      System.out.println("Error: File does not exist");
      return;
    }
    try (Scanner filescan = new Scanner(file)) {
      while (filescan.hasNextLine()) {
        String line = filescan.nextLine();
        if (line.equalsIgnoreCase("exit")) {
          break;
        }
        String result = parser.processCommand(line);
        if (result != null) {
          System.out.println(result);
        }
      }
      System.out.println("Successfully processed " + fileName);
    } catch (Exception e) {
      System.out.println("Error: " + e.getMessage());
    }
  }

  private static void interactiveMode(CommandParser parser) {
    Scanner scanner = new Scanner(System.in);
    System.out.print("Entering interactive mode. Type command or exit to quit");
    System.out.println(">");
    while (scanner.hasNextLine()) {
      String command = scanner.nextLine().trim();
      if (command.equalsIgnoreCase("exit")) {
        System.out.println("Exiting Interactive Mode...");
        break;
      }
      if (command.isEmpty()) {
        System.out.print("> ");
        continue;
      }
      String result = parser.processCommand(command);
      if (result != null) {
        System.out.println(result);
      }
      System.out.print("> ");
    }
    scanner.close();
  }
}
