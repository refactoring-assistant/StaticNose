package calendar.controller.commands;

import java.util.regex.Matcher;

/**
 * A Utility class that helps in extracting subject (Quoted and Unquoted).
 */
public class CommandUtils {

  /**
   * Extract subject from regex matcher. Handling both quoted "Subject" and unquoted Subject.
   *
   * @param matcher the regex matcher
   * @param startGroup the group index where subject starts
   * @return the extracted subject
   */
  public static String extractSubject(Matcher matcher, int startGroup) {
    String quoted = matcher.group(startGroup);
    String unquoted = matcher.group(startGroup + 1);
    return quoted != null ? quoted : unquoted.trim();
  }

  /**
   * Extract subject from regex matcher (default starting at group 1).
   *
   * @param matcher the regex matcher
   * @return the subject by calling extractSubject.
   */
  public static String extractSubject(Matcher matcher) {
    return extractSubject(matcher, 1);
  }
}
