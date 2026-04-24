package calendar.controller;

import static calendar.controller.PatternConstants.CALENDAR_NAME;
import static calendar.controller.PatternConstants.CALENDAR_PROPERTY;
import static calendar.controller.PatternConstants.DATE;
import static calendar.controller.PatternConstants.DATETIME;
import static calendar.controller.PatternConstants.FILEPATH;
import static calendar.controller.PatternConstants.NUMBER;
import static calendar.controller.PatternConstants.PROPERTY;
import static calendar.controller.PatternConstants.SUBJECT;
import static calendar.controller.PatternConstants.TIMEZONE;
import static calendar.controller.PatternConstants.VALUE;
import static calendar.controller.PatternConstants.WEEKDAYS;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Enum representing all valid calendar commands with their regex patterns.
 *
 * <p>Eliminates primitive obsession by wrapping Pattern objects in a type-safe enum.
 * Each constant represents a distinct command with its matching pattern and category.
 *
 * <p>Patterns are listed in matching priority order - more specific patterns should
 * appear before more general ones to ensure correct matching.
 */
public enum CommandPattern {

  CREATE_CALENDAR(
      "^create\\s+calendar\\s+--name\\s+" + CALENDAR_NAME + "\\s+--timezone\\s+" + TIMEZONE + "$",
      CommandCategory.CALENDAR_MANAGEMENT
  ),

  EDIT_CALENDAR(
      "^edit\\s+calendar\\s+--name\\s+" + CALENDAR_NAME + "\\s+--property\\s+"
          + CALENDAR_PROPERTY + "\\s+" + VALUE + "$",
      CommandCategory.CALENDAR_MANAGEMENT
  ),

  USE_CALENDAR(
      "^use\\s+calendar\\s+--name\\s+" + CALENDAR_NAME + "$",
      CommandCategory.CALENDAR_MANAGEMENT
  ),

  COPY_EVENT(
      "^copy\\s+event\\s+" + SUBJECT + "\\s+on\\s+" + DATETIME
          + "\\s+--target\\s+" + CALENDAR_NAME + "\\s+to\\s+" + DATETIME + "$",
      CommandCategory.COPY_OPERATIONS
  ),

  COPY_EVENTS_ON_DATE(
      "^copy\\s+events\\s+on\\s+" + DATE
          + "\\s+--target\\s+" + CALENDAR_NAME + "\\s+to\\s+" + DATE + "$",
      CommandCategory.COPY_OPERATIONS
  ),

  COPY_EVENTS_BETWEEN(
      "^copy\\s+events\\s+between\\s+" + DATE + "\\s+and\\s+" + DATE
          + "\\s+--target\\s+" + CALENDAR_NAME + "\\s+to\\s+" + DATE + "$",
      CommandCategory.COPY_OPERATIONS
  ),

  CREATE_EVENT_REPEAT_FOR(
      "^create\\s+event\\s+" + SUBJECT + "\\s+from\\s+" + DATETIME + "\\s+to\\s+" + DATETIME
          + "\\s+repeats\\s+" + WEEKDAYS + "\\s+for\\s+" + NUMBER + "\\s+times$",
      CommandCategory.EVENT_CREATION
  ),

  CREATE_EVENT_REPEAT_UNTIL(
      "^create\\s+event\\s+" + SUBJECT + "\\s+from\\s+" + DATETIME + "\\s+to\\s+" + DATETIME
          + "\\s+repeats\\s+" + WEEKDAYS + "\\s+until\\s+" + DATE + "$",
      CommandCategory.EVENT_CREATION
  ),

  CREATE_EVENT(
      "^create\\s+event\\s+" + SUBJECT + "\\s+from\\s+" + DATETIME + "\\s+to\\s+" + DATETIME + "$",
      CommandCategory.EVENT_CREATION
  ),

  CREATE_ALLDAY_REPEAT_FOR(
      "^create\\s+event\\s+" + SUBJECT + "\\s+on\\s+" + DATE
          + "\\s+repeats\\s+" + WEEKDAYS + "\\s+for\\s+" + NUMBER + "\\s+times$",
      CommandCategory.EVENT_CREATION
  ),

  CREATE_ALLDAY_REPEAT_UNTIL(
      "^create\\s+event\\s+" + SUBJECT + "\\s+on\\s+" + DATE
          + "\\s+repeats\\s+" + WEEKDAYS + "\\s+until\\s+" + DATE + "$",
      CommandCategory.EVENT_CREATION
  ),

  CREATE_ALLDAY_EVENT(
      "^create\\s+event\\s+" + SUBJECT + "\\s+on\\s+" + DATE + "$",
      CommandCategory.EVENT_CREATION
  ),

  EDIT_SINGLE_EVENT(
      "^edit\\s+event\\s+" + PROPERTY + "\\s+" + SUBJECT + "\\s+from\\s+" + DATETIME
          + "\\s+to\\s+" + DATETIME + "\\s+with\\s+" + VALUE + "$",
      CommandCategory.EVENT_EDITING
  ),

  EDIT_EVENTS_FROM(
      "^edit\\s+events\\s+" + PROPERTY + "\\s+" + SUBJECT + "\\s+from\\s+" + DATETIME
          + "\\s+with\\s+" + VALUE + "$",
      CommandCategory.EVENT_EDITING
  ),

  EDIT_SERIES(
      "^edit\\s+series\\s+" + PROPERTY + "\\s+" + SUBJECT + "\\s+from\\s+" + DATETIME
          + "\\s+with\\s+" + VALUE + "$",
      CommandCategory.EVENT_EDITING
  ),

  PRINT_EVENTS_ON(
      "^print\\s+events\\s+on\\s+" + DATE + "$",
      CommandCategory.QUERY
  ),

  PRINT_EVENTS_RANGE(
      "^print\\s+events\\s+from\\s+" + DATETIME + "\\s+to\\s+" + DATETIME + "$",
      CommandCategory.QUERY
  ),

  EXPORT_CALENDAR(
      "^export\\s+cal\\s+" + FILEPATH + "$",
      CommandCategory.MISC
  ),

  SHOW_STATUS(
      "^show\\s+status\\s+on\\s+" + DATETIME + "$",
      CommandCategory.MISC
  );

  private final Pattern pattern;
  private final CommandCategory category;

  /**
   * Constructs a CommandPattern with the specified regex and category.
   *
   * @param regex    the regular expression pattern string
   * @param category the category this command belongs to
   */
  CommandPattern(String regex, CommandCategory category) {
    this.pattern = Pattern.compile(regex);
    this.category = category;
  }

  /**
   * Attempts to match this pattern against the input string.
   *
   * @param input the command string to match
   * @return Matcher object for extracting capture groups if matched
   */
  public Matcher matcher(String input) {
    return pattern.matcher(input);
  }

  /**
   * Gets the category this command belongs to.
   *
   * @return the command category
   */
  public CommandCategory getCategory() {
    return category;
  }

  /**
   * Categories for organizing commands by functionality.
   */
  public enum CommandCategory {
    CALENDAR_MANAGEMENT,
    COPY_OPERATIONS,
    EVENT_CREATION,
    EVENT_EDITING,
    QUERY,
    MISC
  }
}