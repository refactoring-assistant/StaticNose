package calendar.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Parses text commands entered by the user into a structured representation.
 */
public class CommandParser {

  /**
   * Parses a raw command string into a list of tokens (split by spaces),
   * while respecting quoted strings for multi-word subjects.
   *
   * @param input the user input command
   * @return list of command tokens
   */
  public List<String> parse(String input) {
    Objects.requireNonNull(input, "Command cannot be null");
    List<String> tokens = new ArrayList<>();

    boolean insideQuotes = false;
    StringBuilder current = new StringBuilder();

    for (char c : input.toCharArray()) {
      if (c == '"') {
        insideQuotes = !insideQuotes; // toggle quote mode
      } else if (Character.isWhitespace(c) && !insideQuotes) {
        if (current.length() > 0) {
          tokens.add(current.toString());
          current.setLength(0);
        }
      } else {
        current.append(c);
      }
    }

    if (current.length() > 0) {
      tokens.add(current.toString());
    }

    return tokens;
  }
}
