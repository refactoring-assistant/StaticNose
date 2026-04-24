package view;

import controller.CommandResult;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

/**
 * Console-based implementation of the ICalendarView interface.
 * This view provides a text-based user interface for the calendar application,
 * suitable for command-line interaction.
 */
public class ConsoleView implements IcalendarView {
  private final Scanner scanner;
  private final Appendable out;
  private final Appendable err;
  private static final String PROMPT = "calendar> ";

  /**
   * Constructs a ConsoleView using standard I/O streams.
   * This constructor is intended for production use, reading from
   * System.in and writing to System.out and System.err.
   */
  public ConsoleView() {
    this(new InputStreamReader(System.in), System.out, System.err);
  }

  /**
   * Constructs a ConsoleView with injectable I/O streams.
   * This constructor supports dependency injection for testing purposes,
   * allowing tests to provide mock or in-memory streams.
   *
   * @param in  the readable source for user input
   * @param out the appendable destination for normal output
   * @param err the appendable destination for error output
   */
  public ConsoleView(Readable in, Appendable out, Appendable err) {
    this.scanner = new Scanner(in);
    this.out = out;
    this.err = err;
  }

  @Override
  public void displayMessage(String message) throws IOException {
    out.append(message).append("\n");
    if (out instanceof Flushable) {
      ((Flushable) out).flush();
    }
  }

  @Override
  public void displayError(String error) throws IOException {
    err.append("ERROR: ").append(error).append("\n");
    if (err instanceof Flushable) {
      ((Flushable) err).flush();
    }
  }

  @Override
  public void displayResult(CommandResult result) throws IOException {
    if (result.isSuccess()) {
      displayMessage(result.getMessage());
    } else {
      displayError(result.getMessage());
    }
  }

  @Override
  public String getInput() throws IOException {
    if (scanner.hasNextLine()) {
      return scanner.nextLine().trim();
    }
    return "";
  }

  @Override
  public void displayPrompt() throws IOException {
    out.append(PROMPT);
    if (out instanceof Flushable) {
      ((Flushable) out).flush();
    }
  }

  @Override
  public void close() throws IOException {
    scanner.close();
  }
}
