package controller.command;

import controller.CommandResult;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import model.Icalendar;

/**
 * Command implementation for editing calendar events with various scopes.
 * Supports editing single events, events from a point forward, or entire series.
 */
public class EditEventCommand implements Command {


  private final String subject;
  private final LocalDateTime startDateTime;
  private final LocalDateTime endDateTime;
  private final String property;
  private final String newValue;
  private final model.EditScope scope;

  private static final DateTimeFormatter DATETIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

  /**
   * Constructs an EditEventCommand with the specified parameters.
   *
   * @param subject       the subject of the event to edit
   * @param startDateTime the start time for event identification
   * @param endDateTime   the end time for verification (may be null for series edits)
   * @param property      the property to change
   * @param newValue      the new value for the property
   * @param scope         the scope of the edit (SINGLE, FROM_THIS, ALL_IN_SERIES)
   */
  public EditEventCommand(String subject, LocalDateTime startDateTime, LocalDateTime endDateTime,
                          String property, String newValue, model.EditScope scope) {
    this.subject = subject;
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
    this.property = property;
    this.newValue = newValue;
    this.scope = scope;

  }

  @Override
  public CommandResult execute(Icalendar calendar) {

    if (newValue != null && containsMultipleWith(newValue)) {
      return new CommandResult(false, "Error: multiple 'with' detected in command");
    }
    try {
      Map<String, Object> changes = parsePropertyChange(property, newValue);

      switch (scope) {
        case SINGLE:

          calendar.editEvent(subject, startDateTime, endDateTime, changes);
          return new CommandResult(true, "Event updated");

        case FROM_THIS:
          calendar.editSeriesEvent(subject, startDateTime, endDateTime, changes,
              model.EditScope.FROM_THIS);
          return new CommandResult(true, "Events updated from this instance");

        case ALL_IN_SERIES:
          calendar.editSeriesEvent(subject, startDateTime, endDateTime, changes,
              model.EditScope.ALL_IN_SERIES);
          return new CommandResult(true, "Entire series updated");

        default:
          return new CommandResult(false, "Unknown edit scope");
      }
    } catch (Exception e) {
      return new CommandResult(false, "Error editing event: " + e.getMessage());
    }
  }

  /**
   * Checks if the value contains multiple occurrences of the word "with".
   *
   * @param value the string to check
   * @return true if multiple "with" found, false otherwise
   */
  private boolean containsMultipleWith(String value) {
    String lowerValue = value.toLowerCase();
    int count = 0;
    int index = 0;

    while ((index = lowerValue.indexOf("with", index)) != -1) {
      boolean isWordBoundaryBefore = (index == 0
          ||
          !Character.isLetterOrDigit(lowerValue.charAt(index - 1)));
      boolean isWordBoundaryAfter = (index + 4 >= lowerValue.length()
          ||
          !Character.isLetterOrDigit(lowerValue.charAt(index + 4)));

      if (isWordBoundaryBefore && isWordBoundaryAfter) {
        count++;
        if (count > 1) {
          return true;
        }
      }
      index += 4;
    }

    return false;
  }

  /**
   * Parses property changes from string values into appropriate object types.
   * Handles different property types including strings, dates, and booleans.
   *
   * @param property the property name to change
   * @param value    the new value as a string
   * @return a map containing the parsed property change
   * @throws IllegalArgumentException if property or value is invalid
   */
  private Map<String, Object> parsePropertyChange(String property, String value) {
    Map<String, Object> changes = new HashMap<>();


    switch (property.toLowerCase()) {
      case "subject":
      case "description":
      case "location":
        String stringValue = value.trim();
        if (stringValue.startsWith("\"") && stringValue.endsWith("\"")) {
          stringValue = stringValue.substring(1, stringValue.length() - 1);
        }
        changes.put(property.equals("subject") ? "subject" : property, stringValue);
        break;

      case "start":
        if (!value.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}")) {
          throw new IllegalArgumentException("Invalid datetime format for start");
        }
        changes.put("startDateTime", LocalDateTime.parse(value, DATETIME_FORMATTER));
        break;

      case "end":
        if (!value.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}")) {
          throw new IllegalArgumentException("Invalid datetime format for end");
        }
        changes.put("endDateTime", LocalDateTime.parse(value, DATETIME_FORMATTER));
        break;

      case "status":
        if (!value.equalsIgnoreCase("public") && !value.equalsIgnoreCase("private")) {
          throw new IllegalArgumentException("Status must be 'public' or 'private'");
        }
        changes.put("isPublic", value.equalsIgnoreCase("public"));
        break;

      default:
        throw new IllegalArgumentException("Unknown property: " + property);
    }

    return changes;
  }
}
