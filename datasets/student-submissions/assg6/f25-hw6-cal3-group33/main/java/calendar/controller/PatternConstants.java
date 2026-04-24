package calendar.controller;

/**
 * Regex pattern building blocks used by CommandPattern enum.
 *
 * <p>Separated into its own class to avoid forward reference issues
 * in enum initialization.
 */
final class PatternConstants {

  private PatternConstants() {
    throw new AssertionError("Utility class should not be instantiated");
  }

  static final String QUOTED_SUBJECT = "\"([^\"]+)\"";
  static final String UNQUOTED_SUBJECT = "(\\S+)";
  static final String SUBJECT = "(?:" + QUOTED_SUBJECT + "|" + UNQUOTED_SUBJECT + ")";

  static final String QUOTED_CALENDAR_NAME = "\"([^\"]+)\"";
  static final String UNQUOTED_CALENDAR_NAME = "(\\S+)";
  static final String CALENDAR_NAME = "(?:" + QUOTED_CALENDAR_NAME + "|" + UNQUOTED_CALENDAR_NAME
      + ")";

  static final String DATE = "(\\d{4}-\\d{2}-\\d{2})";
  static final String TIME = "(\\d{2}:\\d{2})";
  static final String DATETIME = "(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})";
  static final String WEEKDAYS = "([MTWRFSU]+)";
  static final String NUMBER = "(\\d+)";
  static final String PROPERTY = "(subject|start|end|description|location|status)";
  static final String VALUE = "(.+)";
  static final String FILEPATH = "(\\S+)";
  static final String TIMEZONE = "([\\w/]+)";
  static final String CALENDAR_PROPERTY = "(name|timezone)";
}