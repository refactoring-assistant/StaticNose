package calendar.utils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class for handling file I/O, like exporting.
 * This logic was extracted from CalModel.
 */
public class FileExportUtil {

  /**
   * Saves string data to a file in the current working directory.
   *
   * @param fileName The name of the file (e.g., "my_calendar.csv").
   * @param data     The string data to write.
   * @return The absolute path of the created file.
   * @throws IOException if writing fails.
   */
  public static String save(String fileName, String data) throws IOException {
    Path currentDir = Paths.get(System.getProperty("user.dir"));
    Path filePath = currentDir.resolve(fileName);

    try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
      writer.write(data);
    } catch (IOException e) {
      throw new IOException("Failed to write calendar to file: " + filePath, e);
    }
    return filePath.toAbsolutePath().toString();
  }
}