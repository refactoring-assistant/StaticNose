package calendar.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility class to parse command strings.
 * It correctly handles arguments enclosed in double quotes.
 */
public class CommandParser {

  /**
   * This regex matches either:
   * 1. A sequence of non-quote, non-whitespace characters.
   * 2. A sequence of any characters enclosed in double quotes.
   */
  private static final Pattern TOKEN_PATTERN = Pattern.compile("[^\\s\"]+|\"([^\"]*)\"");

  /**
   * Parses a raw command string into a list of tokens.
   *
   * @param commandString The raw input line.
   * @return A list of tokens. Quoted strings are included as single tokens.
   */
  public static List<String> parse(String commandString) {
    List<String> tokens = new ArrayList<>();
    Matcher matcher = TOKEN_PATTERN.matcher(commandString);

    while (matcher.find()) {
      tokens.add(matcher.group());
    }

    return tokens;
  }
}