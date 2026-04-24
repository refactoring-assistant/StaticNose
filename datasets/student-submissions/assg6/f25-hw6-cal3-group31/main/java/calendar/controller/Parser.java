package calendar.controller;

import calendar.model.Date;
import calendar.model.Day;
import calendar.model.Time;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Utility class for parsing command input.
 */
public class Parser {
  //  /**
  //   * Create a list of string tokens split by whitespace. A multi-worded argument inside
  //   * quotation marks treated as one token.
  //   *
  //   * @param input the input string
  //   * @return a list of the string tokens
  //   */
  //  public static List<String> getTokens(String input) {
  //    List<String> tokens = new ArrayList<>();
  //
  //    int index = 0;
  //    while (index < input.length()) {
  //      char c = input.charAt(index);
  //      if (c == '\"') {
  //        index++;
  //        if (index < input.length()) {
  //          c = input.charAt(index);
  //        }
  //        StringBuilder sb = new StringBuilder();
  //        while (index < input.length() && c != '\"') {
  //          sb.append(c);
  //          index++;
  //          if (index < input.length()) {
  //            c = input.charAt(index);
  //          }
  //        }
  //        if (input.charAt(index) == '\"') {
  //          tokens.add(sb.toString());
  //          index++;
  //        } else if (index == input.length() - 1) {
  //          throw new IllegalArgumentException("Token at index " + index + "
  //            + is missing closing \"");
  //        }
  //      } else if (c == ' ') {
  //        while (index < input.length() && c == ' ') {
  //          index++;
  //          if (index < input.length()) {
  //            c = input.charAt(index);
  //          }
  //        }
  //      } else {
  //        StringBuilder sb = new StringBuilder();
  //        while (index < input.length() && c != ' ' && c != '\"') {
  //          sb.append(c);
  //          index++;
  //          if (index < input.length()) {
  //            c = input.charAt(index);
  //          }
  //        }
  //        tokens.add(sb.toString());
  //      }
  //    }
  //
  //    return List.copyOf(tokens);
  //  }

  /**
   * Gets the index of the first space in the input string.
   *
   * @param input the input string
   * @return the index of the first space, or the length of the string if no space is found
   */
  public static int getFirstSpaceIndex(String input) {
    int firstSpace = input.indexOf(' ');
    if (firstSpace == -1) {
      firstSpace = input.length();
    }
    return firstSpace;
  }

  /**
   * Extracts the event subject from the input string.
   *
   * @param input the input string
   * @return an EventSubjectPair containing the subject and the length of the parsed string
   */
  public static EventSubjectPair extractEventSubject(String input) {
    String eventSubject = "";
    int stringLength;
    if (input.charAt(0) == '\"') {
      int firstQuoteIndex = input.indexOf('\"');
      int secondQuoteIndex = input.substring(firstQuoteIndex + 1).indexOf('\"');
      if (secondQuoteIndex == -1) {
        throw new IllegalArgumentException("Event subject is missing closing \"");
      }

      stringLength = secondQuoteIndex + 2; // + 2 for the quotes
      eventSubject = input.substring(firstQuoteIndex + 1, secondQuoteIndex + 1);
    } else {
      stringLength = getFirstSpaceIndex(input);
      eventSubject = input.substring(0, stringLength);
    }

    return new EventSubjectPair(eventSubject, stringLength);
  }

  /**
   * Extracts a date from the input string in the format YYYY-MM-DD.
   *
   * @param input the input string
   * @return a Date object
   */
  public static Date extractDate(String input) {
    int year;
    int month;
    int day;

    try {
      year = Integer.parseInt(input.substring(0, 4));
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Date is not a valid Date of the form YYYY-MM-DD");
    }
    input = input.substring(5);

    try {
      month = Integer.parseInt(input.substring(0, 2));
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Date is not a valid Date of the form YYYY-MM-DD");
    }
    input = input.substring(3);

    try {
      day = Integer.parseInt(input.substring(0, 2));
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Date is not a valid Date of the form YYYY-MM-DD");
    }

    return new Date(year, month, day);
  }

  /**
   * Extracts a time from the input string in the format hh:mm.
   *
   * @param input the input string
   * @return a Time object
   */
  public static Time extractTime(String input) {
    int hour;
    int minute;

    try {
      hour = Integer.parseInt(input.substring(0, 2));
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Time is not a valid Time of the form HH:MM");
    }
    input = input.substring(3); // 2 for hour and 1 for ":"

    try {
      minute = Integer.parseInt(input.substring(0, 2));
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Time is not a valid Time of the form HH:MM");
    }

    return new Time(hour, minute);
  }

  /**
   * Extracts repeat days from the input string.
   * Format: M=Monday, T=Tuesday, W=Wednesday, R=Thursday, F=Friday, S=Saturday, U=Sunday
   *
   * @param input the input string
   * @return a RepeatDaysPair containing the days and the length of the parsed string
   */
  public static RepeatDaysPair extractRepeatDays(String input) {
    Set<Day> repeatDays = new HashSet<>();
    int stringLength = getFirstSpaceIndex(input);
    String dayChars = input.substring(0, stringLength);

    if (dayChars.indexOf('M') != -1) {
      repeatDays.add(Day.MONDAY);
    }
    if (dayChars.indexOf('T') != -1) {
      repeatDays.add(Day.TUESDAY);
    }
    if (dayChars.indexOf('W') != -1) {
      repeatDays.add(Day.WEDNESDAY);
    }
    if (dayChars.indexOf('R') != -1) {
      repeatDays.add(Day.THURSDAY);
    }
    if (dayChars.indexOf('F') != -1) {
      repeatDays.add(Day.FRIDAY);
    }
    if (dayChars.indexOf('S') != -1) {
      repeatDays.add(Day.SATURDAY);
    }
    if (dayChars.indexOf('U') != -1) {
      repeatDays.add(Day.SUNDAY);
    }

    if (repeatDays.isEmpty()) {
      throw new IllegalArgumentException("RepeatDays is empty");
    }

    return new RepeatDaysPair(Set.copyOf(repeatDays), stringLength);
  }

  /**
   * Represents a pair of event subject and the length of the parsed string.
   */
  public static class EventSubjectPair {
    public final String eventSubject;
    public final int stringLength;

    /**
     * Constructs an EventSubjectPair.
     *
     * @param eventSubject the event subject
     * @param stringLength the length of the parsed string
     */
    public EventSubjectPair(String eventSubject, int stringLength) {
      this.eventSubject = eventSubject;
      this.stringLength = stringLength;
    }
  }

  /**
   * Represents a pair of repeat days and the length of the parsed string.
   */
  public static class RepeatDaysPair {
    public final Set<Day> repeatDays;
    public final int stringLength;

    /**
     * Constructs a RepeatDaysPair.
     *
     * @param repeatDays   the repeat days
     * @param stringLength the length of the parsed string
     * @throws IllegalArgumentException if repeatDays is null or empty
     */
    public RepeatDaysPair(Set<Day> repeatDays, int stringLength) {
      this.repeatDays = Objects.requireNonNull(repeatDays);
      this.stringLength = stringLength;
    }
  }

  /**
   * Extracts a token from the input string that may or may not have quotes around the token.
   *
   * @param input the input string
   * @return a TokenPair containing the next token in the parsed string and the length of the token
   *        and if the token was surrounded in quotes the length of the quotes as well
   * @throws IllegalArgumentException if the input token is missing a closing quote
   */
  public static TokenPair extractToken(String input) throws IllegalArgumentException {
    String token;
    int length;
    if (input.charAt(0) == '\"') {
      int firstQuoteIndex = input.indexOf('\"');
      int secondQuoteIndex = input.substring(firstQuoteIndex + 1).indexOf('\"');
      if (secondQuoteIndex == -1) {
        throw new IllegalArgumentException("Token is missing closing \"");
      }
      length = secondQuoteIndex + 2; // + 2 for the quotes
      token = input.substring(firstQuoteIndex + 1, secondQuoteIndex + 1);
    } else {
      length = getFirstSpaceIndex(input);
      token = input.substring(0, getFirstSpaceIndex(input));
    }

    return new TokenPair(token, length);
  }

  /**
   * A pair representing a token and how many spaces the token took up in the original input
   * possibly including the space for quotes.
   */
  public static class TokenPair {
    public final String token;
    public final int length;

    /**
     * TokenPair constructor.
     *
     * @param token  the token extracted
     * @param length the length of the original input possibly including space for quotes
     */
    public TokenPair(String token, int length) {
      this.token = Objects.requireNonNull(token);
      this.length = length;
    }
  }
}

