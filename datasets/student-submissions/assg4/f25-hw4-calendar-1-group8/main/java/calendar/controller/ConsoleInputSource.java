package calendar.controller;

import java.io.IOException;
import java.util.Scanner;

/**
 * An InputSource that reads from the standard console (System.in).
 */
public class ConsoleInputSource implements InputSource {
  private Scanner scanner;

  /**
   * Creates a new input source that reads commands from the console (System.in).
   */
  public ConsoleInputSource() {
    this.scanner = new Scanner(System.in);
  }

  @Override
  public boolean hasNextLine() {
    return true;
  }

  @Override
  public String readLine() throws IOException {
    return scanner.nextLine();
  }

  @Override
  public void close() throws IOException {
  }
}