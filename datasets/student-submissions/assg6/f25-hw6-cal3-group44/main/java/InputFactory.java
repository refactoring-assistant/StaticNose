import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;

/**
 * It is Factory method to help parse the args input given in main class.
 */
public class InputFactory {

  /**
   * Performs the validation the input arguments.
   * Determines mode and returns it to main.
   *
   * @param args input received by main.
   * @return a Readable either a System out or fileReader.
   */
  public static Readable getInput(String[] args) {
    if (args.length < 2 || args.length > 3 || !args[0].equalsIgnoreCase("--mode")) {
      throw new IllegalArgumentException("Error: Invalid arguments. "
          + "Valid way to execute: java CalendarRunner.java --mode interactive "
          + "or --mode headless <filename>");
    }

    if (args[1].equalsIgnoreCase("interactive")) {
      return new InputStreamReader(System.in);
    } else if (args[1].equalsIgnoreCase("headless")) {
      if (args.length != 3) {
        throw new IllegalArgumentException("Error: Missing file name in arguments. "
            + "Valid way to execute: java CalendarRunner.java --mode interactive "
            + "or --mode headless <filename>");
      }
      try {
        return new FileReader(args[2]);
      } catch (FileNotFoundException e) {
        throw new IllegalArgumentException("Error: File not found. " + args[2]);
      }
    }

    throw new IllegalArgumentException("Error: Invalid Run Mode." + args[1]);
  }
}
