package calendar.controller;

import java.io.IOException;

/**
 * An interface for getting the next command,
 * hiding whether it's from the console or a file.
 */
public interface InputSource extends AutoCloseable {
  /**
   * Checks if there is another command to read.
   *
   * @return true if there is a next line, false otherwise.
   */
  boolean hasNextLine();

  /**
   * Reads the next command as a single string.
   *
   * @return The next command line.
   * @throws IOException If an I/O error occurs.
   */
  String readLine() throws IOException;

  /**
   * By adding this, we override the close() method from AutoCloseable
   * and promise that it will ONLY ever throw IOException, not the
   * generic Exception.
   */
  @Override
  void close() throws IOException;
}