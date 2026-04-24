package calendar.util;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A utility class to validate a file path.
 */
public class PathObj {
  /**
   * Converts a string filepath to a Java Path object.
   * Throws an illegal argument exception if the string path is invalid.
   *
   * @param path String path.
   * @return A Java Path object.
   */
  public static Path getPathObj(String path) {
    try {
      return Paths.get(path);
    } catch (InvalidPathException e) {
      throw new IllegalArgumentException("Invalid path");
    }
  }
}
