package calendar.view;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

/**
 * View implementation for interactive mode.
 * Allows the user to type commands in the console and see immediate results.
 */
public class InteractiveView implements Iview {

  private final Scanner scanner;
  private final PrintStream out;

  /**
   * Creates an interactive console view using standard input/output.
   *
   * @param in  an InputStream from which user commands will be read.
   * @param out a PrintStream to which messages will be printed.
   */
  public InteractiveView(InputStream in, PrintStream out) {
    this.scanner = new Scanner(in);
    this.out = out;
  }

  /**
   * Prints a message to the console.
   *
   * @param message given message to be printed to the console.
   */
  @Override
  public void print(String message) {
    out.println(message);
  }

  /**
   * Prompts and reads the next command from the user.
   * Reads a single line of input.
   *
   * @return the next command string entered by the user, or null if input ends
   */
  @Override
  public String readInput() {
    out.print("> ");
    if (scanner.hasNextLine()) {
      return scanner.nextLine();
    }
    return null;
  }

  /**
   * Not used in interactive mode since source path is not applicable.
   *
   * @return always returns null
   */
  @Override
  public String getSourcePath() {
    return null;
  }
}
