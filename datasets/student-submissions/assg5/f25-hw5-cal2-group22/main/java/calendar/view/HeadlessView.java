package calendar.view;

import java.io.PrintStream;

/**
 * View implementation for headless (non-interactive) mode.
 * Executes commands from a provided input file and outputs results to the console.
 */

public class HeadlessView implements Iview {

  private final String filePath;
  private final PrintStream out;

  /**
   * Creates a headless view using the specified command file.
   *
   * @param path path to the command file
   */

  public HeadlessView(String path, PrintStream out) {
    this.filePath = path;
    this.out = out;
  }

  /**
   * Prints messages or command results to the console.
   */
  @Override
  public void print(String message) {
    out.println(message);
  }

  /**
   * Not used in headless mode as input comes from file, no console input.
   *
   * @return always returns null.
   */
  @Override
  public String readInput() {
    return null;
  }

  /**
   * To get the source path of the file.
   *
   * @return the path of the command file as a string.
   */
  @Override
  public String getSourcePath() {
    return filePath;
  }
}
