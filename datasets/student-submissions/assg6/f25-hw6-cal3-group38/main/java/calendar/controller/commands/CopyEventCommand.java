package calendar.controller.commands;

import calendar.controller.CalendarController;
import calendar.controller.Command;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Command for copying events between calendars.
 */
public class CopyEventCommand implements Command {
  private final String[] tokens;

  /**
   * Constructs a CopyEventCommand.
   *
   * @param tokens the command tokens
   */
  public CopyEventCommand(String[] tokens) {
    this.tokens = tokens;
  }

  @Override
  public String execute(CalendarController controller) {
    try {
      // FIX: Changed from 10 to 9 tokens minimum
      if (tokens.length < 9) {
        return "Error: Invalid copy event command format. "
            + "Usage: copy event <eventName> on <datetime> --target <calendar> to <datetime>";
      }

      // Parse event name (handling quoted multi-word names)
      String eventName = parseEventName(tokens);
      int nameEndIndex = findEventNameEndIndex(tokens);

      // Validate command structure
      if (nameEndIndex + 6 > tokens.length
          || !"on".equals(tokens[nameEndIndex])
          || !"--target".equals(tokens[nameEndIndex + 2])
          || !"to".equals(tokens[nameEndIndex + 4])) {
        return "Error: Invalid copy event command format. "
            + "Usage: copy event <eventName> on <datetime> --target <calendar> to <datetime>";
      }

      LocalDateTime sourceDateTime = parseDateTime(tokens[nameEndIndex + 1]);
      String targetCalendar = tokens[nameEndIndex + 3];
      LocalDateTime targetDateTime = parseDateTime(tokens[nameEndIndex + 5]);

      controller.copyEvent(eventName, sourceDateTime, targetCalendar, targetDateTime);
      return "Event '" + eventName + "' copied successfully to calendar '" + targetCalendar + "'";
    } catch (Exception e) {
      return "Error: " + e.getMessage();
    }
  }

  /**
   * parseEventName.
   *
   * @param tokens tokens
   * @return return
   */
  public String parseEventName(String[] tokens) {
    if (tokens[2].startsWith("\"")) {
      // Multi-word event name in quotes
      StringBuilder name = new StringBuilder();

      // Start from token 2 and continue until we find the closing quote
      for (int i = 2; i < tokens.length; i++) {
        String token = tokens[i];

        if (token.endsWith("\"")) {
          // This token ends with quote, so it's the last part
          if (token.startsWith("\"")) {
            // Single word in quotes: "Word"
            name.append(token, 1, token.length() - 1);
          } else {
            // Last part of multi-word: Word"
            name.append(token, 0, token.length() - 1);
          }
          break;
        } else if (token.startsWith("\"")) {
          // First part of multi-word: "Word
          name.append(token.substring(1)).append(" ");
        } else {
          // Middle part of multi-word: Word
          name.append(token).append(" ");
        }
      }
      return name.toString().trim();
    } else {
      // Single word event name without quotes
      return tokens[2];
    }
  }

  /**
   * findEventNameEndIndex.
   *
   * @param tokens tokens
   * @return return
   */
  public int findEventNameEndIndex(String[] tokens) {
    if (tokens[2].startsWith("\"")) {
      // Find the token that ends with quote
      for (int i = 2; i < tokens.length; i++) {
        if (tokens[i].endsWith("\"")) {
          return i + 1; // Return index after the event name
        }
      }
      // If no closing quote found, return after token 2 (error case)
    }
    return 3;
  }

  /**
   * parseDateTime.
   *
   * @param dateTimeStr dateTimeStr
   * @return return
   */
  public LocalDateTime parseDateTime(String dateTimeStr) {
    try {
      // Handle both with and without seconds
      if (dateTimeStr.length() == 16) { // "2024-01-01T10:00"
        return LocalDateTime.parse(dateTimeStr + ":00", DateTimeFormatter.ISO_LOCAL_DATE_TIME);
      }
      return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid date-time format: " + dateTimeStr);
    }
  }
}