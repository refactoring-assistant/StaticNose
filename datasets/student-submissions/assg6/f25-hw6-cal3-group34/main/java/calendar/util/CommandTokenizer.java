package calendar.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits command strings into tokens while respecting quoted segments.
 */
public final class CommandTokenizer {
  private CommandTokenizer() {
  }

  /**
   * Tokenizes the supplied line into whitespace separated tokens. Portions wrapped in double
   * quotes are treated as a single token without the surrounding quotes.
   *
   * @param line raw command line
   * @return ordered tokens
   */
  public static List<String> tokenize(String line) {
    List<String> tokens = new ArrayList<>();
    if (line == null) {
      return tokens;
    }
    StringBuilder current = new StringBuilder();
    boolean inQuotes = false;
    for (int i = 0; i < line.length(); i++) {
      char ch = line.charAt(i);
      if (ch == '"') {
        inQuotes = !inQuotes;
        continue;
      }
      if (Character.isWhitespace(ch) && !inQuotes) {
        if (current.length() > 0) {
          tokens.add(current.toString());
          current.setLength(0);
        }
      } else {
        current.append(ch);
      }
    }
    if (inQuotes) {
      throw new IllegalArgumentException("Unterminated quoted string in command.");
    }
    if (current.length() > 0) {
      tokens.add(current.toString());
    }
    return tokens;
  }
}
