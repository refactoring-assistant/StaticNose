package calendar.utils;

/**
 * Utility class for common string operations across commands.
 */
public class StringUtils {

  /**
   * private constructor.
   */
  private StringUtils() {
    throw new AssertionError("No instances allowed");
  }

  /**
   * Removes surrounding quotes from a string if present.
   * Handles both double quotes and single quotes.
   *
   * @param s the string possibly surrounded by quotes
   * @return the unquoted string or the original string if no quotes
   */
  public static String removeQuotes(String s) {
    if (s == null) {
      return null;
    }
    s = s.trim();
    if (s.length() >= 2) {
      char first = s.charAt(0);
      char last = s.charAt(s.length() - 1);
      if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
        return s.substring(1, s.length() - 1);
      }
    }
    return s;
  }
}