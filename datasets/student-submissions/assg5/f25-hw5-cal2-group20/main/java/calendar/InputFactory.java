package calendar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;

/**
 * Factory class that helps with processing inputs for main method.
 */
public class InputFactory {

  /**
   * Factory method that makes inputs for Main method.
   *
   * @param args the arguments passed from user.
   * @return the input in appropriate type.
   */
  public static Readable makeInput(String[] args) {
    if (args.length > 2) {
      File file = new File(args[2]);
      try {
        return new FileReader(file);
      } catch (FileNotFoundException e) {
        throw new IllegalStateException("File " + args[2] + " does not exist.");
      }
    } else {
      return new InputStreamReader(System.in);
    }
  }
}
