package calendar.controller;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * An InputSource that reads commands from a text file.
 */
public class FileInputSource implements InputSource {
  private BufferedReader reader;
  private String nextLine;

  /**
   * Creates a new input source that reads commands from the specified file.
   *
   * @param filename the path to the input file
   * @throws IOException if the file cannot be opened or read
   */
  public FileInputSource(String filename) throws IOException {
    this.reader = new BufferedReader(new FileReader(filename));
    this.nextLine = reader.readLine();
  }

  @Override
  public boolean hasNextLine() {
    return this.nextLine != null;
  }

  @Override
  public String readLine() throws IOException {
    String lineToReturn = this.nextLine;
    this.nextLine = reader.readLine();
    return lineToReturn;
  }

  @Override
  public void close() throws IOException {
    if (reader != null) {
      reader.close();
    }
  }
}