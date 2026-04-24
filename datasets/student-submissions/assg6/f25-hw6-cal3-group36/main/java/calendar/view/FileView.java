package calendar.view;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * Provides a file-based view for headless execution.
 */
public class FileView implements CalendarView {

  private final BufferedReader reader;
  private final Writer writer;

  /**
   * Creates a new FileView with given input and output streams.
   *
   * @param input  the readable command source
   * @param output the appendable output destination
   */
  public FileView(Reader input, Writer output) {
    this.reader = new BufferedReader(input);
    this.writer = output;
  }

  @Override
  public void renderMessage(String message) throws IOException {
    writer.write(message);
    writer.write(System.lineSeparator());
    writer.flush();
  }

  @Override
  public String readInput() throws IOException {
    return reader.readLine();
  }
}