package calendar.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.file.Files;

/**
 * A utility class to load the appropriate input source.
 */
public class PickReadable {
  /**
   * Chooses the right input source based on the set of provided arguments.
   *
   * <p>Could throw an IO or illegal argument exception if the path is invalid when trying headless
   * mode.
   *
   * @param args A list of String arguments.
   * @return The appropriate Readable object.
   */
  public static Readable load(String[] args) {
    PrintStream defaultWriter = getDefaultWriter();

    if (args.length == 2
        && args[0].equalsIgnoreCase("--mode")
        && args[1].equalsIgnoreCase("interactive")) {
      return new InputStreamReader(System.in);
    } else if (args.length == 3
        && args[0].equalsIgnoreCase("--mode")
        && args[1].equalsIgnoreCase("headless")) {
      try {
        return newFileReader(args[2]);
      } catch (IOException | IllegalArgumentException e) {
        defaultWriter.println(e.getMessage());
        return null;
      }
    }

    defaultWriter.println("Invalid arguments");
    return null;
  }

  /**
   * This method creates and returns a buffer writer that writes to a file at the given path.
   *
   * <p>This helper method's access has been relaxed to protected in order to help mock a buffered
   * writer during testing.
   *
   * @param filePath String path to the target file.
   * @return A buffered writer that writes to the target file.
   * @throws IOException If it wasn't possible to create a writer to the requested file location.
   */
  private static Readable newFileReader(String filePath) throws IOException {
    return Files.newBufferedReader(PathObj.getPathObj(filePath));
  }

  protected static PrintStream getDefaultWriter() {
    return System.out;
  }
}
