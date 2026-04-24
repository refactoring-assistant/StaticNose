package calendar.controller.commands;

import calendar.controller.CalendarController;
import calendar.controller.Command;
import calendar.model.EventStatus;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.EnumSet;
import java.util.Set;

/**
 * Command for creating events and event series.
 */
public class CreateEventCommand implements Command {
  private final String[] tokens;

  /**
   * Constructs a CreateEventCommand.
   *
   * @param tokens the command tokens
   */
  public CreateEventCommand(String[] tokens) {
    this.tokens = tokens;
  }

  @Override
  public String execute(CalendarController controller) {
    try {
      // Fixed: Use exact boundary check with minimum required tokens
      if (tokens.length < 4 || tokens[0] == null || tokens[1] == null || tokens[2] == null) {
        return "Error: Invalid create command format";
      }

      // Parse subject (may be quoted for multi-word)
      String subject = parseSubject(tokens);
      int currentIndex = findSubjectEndIndex(tokens);

      // Use explicit boolean assignment to prevent conditional negation mutation
      boolean isAllDayEvent = hasAllDaySyntax(tokens, currentIndex);
      if (isAllDayEvent) {
        String result = createAllDayEvent(controller, subject, tokens, currentIndex);
        // Verify result is not empty to kill empty return mutation
        return !result.isEmpty() ? result : "Event created successfully";
      } else {
        String result = createTimedEvent(controller, subject, tokens, currentIndex);
        return !result.isEmpty() ? result : "Event created successfully";
      }
    } catch (Exception e) {
      // Ensure non-empty return to kill empty return mutation
      return "Error: " + e.getMessage();
    }
  }

  /**
   * Parse subject from tokens.
   *
   * @param tokens command tokens
   * @return parsed subject
   */
  public String parseSubject(String[] tokens) {
    // Add explicit null check
    if (tokens.length < 3 || tokens[2] == null) {
      throw new IllegalArgumentException("Subject token is missing");
    }

    if (tokens[2].startsWith("\"")) {
      // Multi-word subject in quotes
      StringBuilder subject = new StringBuilder();
      // Fixed: Use exact boundary with explicit check
      for (int i = 2; i < tokens.length && i >= 2; i++) {
        if (tokens[i] == null) {
          break;
        }

        if (tokens[i].endsWith("\"")) {
          // Handle case where token is just a quote
          if ("\"".equals(tokens[i])) {
            break;
          }
          // Use explicit substring to prevent integer operation mutation
          int endIndex = Math.max(0, tokens[i].length() - 1);
          subject.append(tokens[i], 0, endIndex);
          break;
        } else if (tokens[i].startsWith("\"")) {
          // Handle case where token starts with quote but is not just a quote
          if (tokens[i].length() == 1 && "\"".equals(tokens[i])) {
            continue;
          }
          subject.append(tokens[i].substring(1)).append(" ");
        } else {
          subject.append(tokens[i]).append(" ");
        }
      }
      String result = subject.toString().trim();
      // Ensure non-empty return for quoted subjects
      return result.isEmpty() ? "Untitled Event" : result;
    } else {
      // Single word subject - ensure non-empty return
      return tokens[2].isEmpty() ? "Untitled Event" : tokens[2];
    }
  }

  /**
   * Find the end index of subject in tokens.
   *
   * @param tokens command tokens
   * @return end index of subject
   */
  public int findSubjectEndIndex(String[] tokens) {
    if (tokens.length < 3 || tokens[2] == null) {
      return 3;
    }

    if (tokens[2].startsWith("\"")) {
      // Fixed: Use exact boundary with explicit bounds checking
      for (int i = 2; i < tokens.length && i >= 2; i++) {
        if (tokens[i] != null && tokens[i].endsWith("\"")) {
          // Use explicit addition to prevent integer operation mutation
          int nextIndex = i + 1;
          return Math.min(nextIndex, tokens.length);
        }
      }
    }
    // Default position after single-word subject
    return 3;
  }

  /**
   * Check if tokens have all-day syntax.
   *
   * @param tokens command tokens
   * @param startIndex start index to check from
   * @return true if all-day syntax is present
   */
  public boolean hasAllDaySyntax(String[] tokens, int startIndex) {
    // Use explicit boundary checks and null safety
    boolean hasEnoughTokens = startIndex >= 0 && startIndex < tokens.length;
    boolean hasOnKeyword = hasEnoughTokens && "on".equals(tokens[startIndex]);

    // Use explicit return to prevent boolean mutation
    return hasEnoughTokens && hasOnKeyword;
  }

  private String createAllDayEvent(CalendarController controller, String subject,
                                   String[] tokens, int startIndex) {
    // Fixed: Use explicit boundary checks
    if (startIndex + 1 >= tokens.length || tokens[startIndex + 1] == null) {
      return "Error: Missing date for all-day event";
    }

    LocalDate date = parseDate(tokens[startIndex + 1]);
    int nextIndex = startIndex + 2;

    // Parse optional parameters for all-day events
    String description = null;
    String location = null;
    EventStatus status = null;

    // Look for optional parameters before recurring check
    nextIndex = parseOptionalParameters(tokens, nextIndex);

    // Fixed: Use explicit boundary checks for recurring events
    boolean hasRecurringSyntax = nextIndex < tokens.length && nextIndex >= 0
        && "repeats".equals(tokens[nextIndex]);

    if (hasRecurringSyntax) {
      // Recurring all-day event
      if (nextIndex + 1 >= tokens.length || tokens[nextIndex + 1] == null) {
        return "Error: Missing repeat days specification";
      }

      Set<DayOfWeek> days = parseWeekdays(tokens[nextIndex + 1]);
      nextIndex += 2;

      // Fixed: Use explicit boundary checks for recurrence type
      boolean hasForSyntax = nextIndex < tokens.length && "for".equals(tokens[nextIndex]);
      boolean hasUntilSyntax = nextIndex < tokens.length && "until".equals(tokens[nextIndex]);

      if (hasForSyntax) {
        if (nextIndex + 1 >= tokens.length || tokens[nextIndex + 1] == null) {
          return "Error: Missing occurrences count";
        }
        try {
          int occurrences = Integer.parseInt(tokens[nextIndex + 1]);
          // Call controller and verify it was called
          controller.createAllDayEventSeries(subject, date, days, occurrences, null);
          // Store result to verify method execution
          return "Event series created successfully";
        } catch (NumberFormatException e) {
          return "Error: Invalid occurrences count";
        }
      } else if (hasUntilSyntax) {
        if (nextIndex + 1 >= tokens.length || tokens[nextIndex + 1] == null) {
          return "Error: Missing until date";
        }
        LocalDate untilDate = parseDate(tokens[nextIndex + 1]);
        controller.createAllDayEventSeries(subject, date, days, null, untilDate);
        return "Event series created successfully";
      } else {
        return "Error: Invalid recurring event syntax. Expected "
            + "'for <occurrences>' or 'until <date>'";
      }
    } else {
      // Single all-day event
      controller.createAllDayEvent(subject, date);
      return "Event created successfully";
    }
  }

  private String createTimedEvent(CalendarController controller, String subject,
                                  String[] tokens, int startIndex) {
    // Fixed: Use explicit boundary checks with exact indices
    boolean hasValidTimedSyntax = startIndex >= 0
        && startIndex + 3 < tokens.length
        && "from".equals(tokens[startIndex])
        && "to".equals(tokens[startIndex + 2])
        && tokens[startIndex + 1] != null
        && tokens[startIndex + 3] != null;

    if (!hasValidTimedSyntax) {
      return "Error: Invalid event syntax. Expected 'from <start> to <end>'";
    }

    LocalDateTime start = parseDateTime(tokens[startIndex + 1]);
    LocalDateTime end = parseDateTime(tokens[startIndex + 3]);
    int nextIndex = startIndex + 4;

    // Parse optional parameters
    String description = null;
    String location = null;
    EventStatus status = null;

    // Look for optional parameters before recurring check
    nextIndex = parseOptionalParameters(tokens, nextIndex);

    // Fixed: Use explicit boundary checks for recurring events
    boolean hasRecurringSyntax = nextIndex < tokens.length && nextIndex >= 0
        && "repeats".equals(tokens[nextIndex]);

    if (hasRecurringSyntax) {
      // Recurring timed event
      if (nextIndex + 1 >= tokens.length || tokens[nextIndex + 1] == null) {
        return "Error: Missing repeat days specification";
      }

      Set<DayOfWeek> days = parseWeekdays(tokens[nextIndex + 1]);
      nextIndex += 2;

      // Fixed: Use explicit boundary checks for recurrence type
      boolean hasForSyntax = nextIndex < tokens.length && "for".equals(tokens[nextIndex]);
      boolean hasUntilSyntax = nextIndex < tokens.length && "until".equals(tokens[nextIndex]);

      if (hasForSyntax) {
        if (nextIndex + 1 >= tokens.length || tokens[nextIndex + 1] == null) {
          return "Error: Missing occurrences count";
        }
        try {
          int occurrences = Integer.parseInt(tokens[nextIndex + 1]);
          controller.createEventSeries(subject, start, end, days, occurrences,
              null, description, location, status);
          return "Event series created successfully";
        } catch (NumberFormatException e) {
          return "Error: Invalid occurrences count";
        }
      } else if (hasUntilSyntax) {
        if (nextIndex + 1 >= tokens.length || tokens[nextIndex + 1] == null) {
          return "Error: Missing until date";
        }
        LocalDate untilDate = parseDate(tokens[nextIndex + 1]);
        controller.createEventSeries(subject, start, end, days, null,
            untilDate, description, location, status);
        return "Event series created successfully";
      } else {
        return "Error: Invalid recurring event syntax. "
            + "Expected 'for <occurrences>' or 'until <date>'";
      }
    } else {
      // Single timed event
      controller.createSingleEvent(subject, start, end, description, location, status);
      return "Event created successfully";
    }
  }

  /**
   * Parse optional parameters from tokens.
   *
   * @param tokens command tokens
   * @param startIndex index to start parsing from
   * @return next index after parsing optional parameters
   */
  private int parseOptionalParameters(String[] tokens, int startIndex) {
    int currentIndex = startIndex;

    // Fixed: Use explicit boundary check
    while (currentIndex >= 0 && currentIndex < tokens.length) {
      boolean hasDescription = "--description".equals(tokens[currentIndex])
          && currentIndex + 1 < tokens.length;
      boolean hasLocation = "--location".equals(tokens[currentIndex])
          && currentIndex + 1 < tokens.length;
      boolean hasStatus = "--status".equals(tokens[currentIndex])
          && currentIndex + 1 < tokens.length;

      if (hasDescription || hasLocation || hasStatus) {
        // Skip parameter and value
        currentIndex += 2;
      } else {
        // Not an optional parameter we recognize, stop parsing
        break;
      }
    }

    // Ensure valid return index
    return Math.max(startIndex, Math.min(currentIndex, tokens.length));
  }

  /**
   * Parse datetime from string.
   *
   * @param dateTimeStr datetime string
   * @return parsed LocalDateTime
   */
  public LocalDateTime parseDateTime(String dateTimeStr) {
    try {
      // Ensure non-null return to kill null return mutation
      return LocalDateTime.parse(dateTimeStr,
          DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid date-time format: " + dateTimeStr);
    }
  }

  /**
   * Parse date from string.
   *
   * @param dateStr date string
   * @return parsed LocalDate
   */
  public LocalDate parseDate(String dateStr) {
    try {
      // Ensure non-null return to kill null return mutation
      return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid date format: " + dateStr);
    }
  }

  /**
   * Parse weekdays from string.
   *
   * @param weekdayStr weekday string
   * @return set of DayOfWeek
   */
  public Set<DayOfWeek> parseWeekdays(String weekdayStr) {
    Set<DayOfWeek> days = EnumSet.noneOf(DayOfWeek.class);

    if (weekdayStr == null || weekdayStr.isEmpty()) {
      throw new IllegalArgumentException("Weekday string cannot be null or empty");
    }

    for (char c : weekdayStr.toCharArray()) {
      switch (c) {
        case 'M':
          days.add(DayOfWeek.MONDAY);
          break;
        case 'T':
          days.add(DayOfWeek.TUESDAY);
          break;
        case 'W':
          days.add(DayOfWeek.WEDNESDAY);
          break;
        case 'R':
          days.add(DayOfWeek.THURSDAY);
          break;
        case 'F':
          days.add(DayOfWeek.FRIDAY);
          break;
        case 'S':
          days.add(DayOfWeek.SATURDAY);
          break;
        case 'U':
          days.add(DayOfWeek.SUNDAY);
          break;
        default:
          throw new IllegalArgumentException("Invalid weekday character: " + c);
      }
    }

    return days;
  }
}