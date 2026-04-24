package interpreter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Regex-first interpreter that classifies an input command string into one of the supported
 * command types. Each command is backed by its own Pattern so tests can verify coverage per
 * command without relying on ad-hoc tokenization.
 */
public final class CommandInterpreter {

  private static final String DATE = "\\d{4}-\\d{2}-\\d{2}";
  private static final String DATE_TIME = DATE + "T\\d{2}:\\d{2}";
  private static final String SUBJECT =
      "(?:(?<subjectQuoted>\"[^\"]+\")|(?<subjectPlain>[^\\s]+))";
  private static final String WEEKDAYS = "(?<weekdays>[MTWRFSU]+)";
  private static final String INT = "(?<count>\\d+)";

  private final List<CommandPattern> patterns = new ArrayList<>();

  /**
   * Register all known command patterns in priority order.
   */
  public CommandInterpreter() {
    register(CommandType.PRINT_ON,
        "^print\\s+events\\s+on\\s+(?<date>" + DATE + ")$");
    register(CommandType.PRINT_RANGE,
        "^print\\s+events\\s+from\\s+(?<start>" + DATE_TIME + ")\\s+to\\s+(?<end>" + DATE_TIME
            + ")$");
    register(CommandType.SHOW_STATUS,
        "^show\\s+status\\s+on\\s+(?<moment>" + DATE_TIME + ")$");
    register(CommandType.EXPORT_CAL,
        "^export\\s+cal\\s+(?<file>\\S+)$");

    register(CommandType.CREATE_TIMED_EVENT,
        "^create\\s+event\\s+" + SUBJECT + "\\s+from\\s+(?<start>" + DATE_TIME + ")\\s+to\\s+"
            + "(?<end>" + DATE_TIME + ")$");
    register(CommandType.CREATE_TIMED_SERIES_FOR,
        "^create\\s+event\\s+" + SUBJECT + "\\s+from\\s+(?<start>" + DATE_TIME + ")\\s+to\\s+"
            + "(?<end>" + DATE_TIME + ")\\s+repeats\\s+" + WEEKDAYS + "\\s+for\\s+"
            + INT + "\\s+times$");
    register(CommandType.CREATE_TIMED_SERIES_UNTIL,
        "^create\\s+event\\s+" + SUBJECT + "\\s+from\\s+(?<start>" + DATE_TIME + ")\\s+to\\s+"
            + "(?<end>" + DATE_TIME + ")\\s+repeats\\s+" + WEEKDAYS + "\\s+until\\s+"
            + "(?<until>" + DATE + ")$");
    register(CommandType.CREATE_ALLDAY_EVENT,
        "^create\\s+event\\s+" + SUBJECT + "\\s+on\\s+(?<date>" + DATE + ")$");
    register(CommandType.CREATE_ALLDAY_SERIES_FOR,
        "^create\\s+event\\s+" + SUBJECT + "\\s+on\\s+(?<date>" + DATE + ")\\s+repeats\\s+"
            + WEEKDAYS + "\\s+for\\s+" + INT + "\\s+times$");
    register(CommandType.CREATE_ALLDAY_SERIES_UNTIL,
        "^create\\s+event\\s+" + SUBJECT + "\\s+on\\s+(?<date>" + DATE + ")\\s+repeats\\s+"
            + WEEKDAYS + "\\s+until\\s+(?<until>" + DATE + ")$");

    register(CommandType.EDIT_EVENT_FROM_TO,
        "^edit\\s+event\\s+(?<property>\\w+)\\s+" + SUBJECT + "\\s+from\\s+(?<start>" + DATE_TIME
            + ")\\s+to\\s+(?<end>" + DATE_TIME + ")\\s+with\\s+(?<value>.+)$");
    register(CommandType.EDIT_EVENTS_FROM,
        "^edit\\s+events\\s+(?<property>\\w+)\\s+" + SUBJECT + "\\s+from\\s+(?<start>" + DATE_TIME
            + ")\\s+with\\s+(?<value>.+)$");
    register(CommandType.EDIT_SERIES,
        "^edit\\s+series\\s+(?<property>\\w+)\\s+" + SUBJECT + "\\s+from\\s+(?<start>" + DATE_TIME
            + ")\\s+with\\s+(?<value>.+)$");

    register(CommandType.CREATE_CALENDAR,
        "^create\\s+calendar\\s+--name\\s+(?<calendarName>\\S+)\\s+--timezone\\s+"
            + "(?<timezone>\\S+)$");
    register(CommandType.EDIT_CALENDAR,
        "^edit\\s+calendar\\s+--name\\s+(?<calendarName>\\S+)\\s+--property\\s+"
            + "(?<calendarProperty>\\w+)\\s+(?<calendarValue>.+)$");
    register(CommandType.LIST_CALENDARS, "^list\\s+calendars$");
    register(CommandType.USE_CALENDAR, "^use\\s+calendar\\s+--name\\s+(?<calendarName>\\S+)$");
    register(CommandType.COPY_EVENT_SINGLE,
        "^copy\\s+event\\s+" + SUBJECT + "\\s+on\\s+(?<sourceStart>"
            + DATE_TIME + ")\\s+--target\\s+"
            + "(?<targetCalendar>\\S+)\\s+to\\s+(?<targetStart>" + DATE_TIME + ")$");
    register(CommandType.COPY_EVENTS_ON,
        "^copy\\s+events\\s+on\\s+(?<sourceDate>" + DATE + ")\\s+--target\\s+"
            + "(?<targetCalendar>\\S+)\\s+to\\s+(?<targetDate>" + DATE + ")$");
    register(CommandType.COPY_EVENTS_BETWEEN,
        "^copy\\s+events\\s+between\\s+(?<rangeStart>" + DATE + ")\\s+and\\s+(?<rangeEnd>"
            + DATE + ")\\s+--target\\s+(?<targetCalendar>\\S+)\\s+to\\s+(?<targetStartDate>"
            + DATE + ")$");
    register(CommandType.HELP, "^help$");
    register(CommandType.EXIT, "^exit$");
  }

  /**
   * Store a compiled regex with its associated command type.
   *
   * @param type  logical command family
   * @param regex pattern describing that command
   */
  private void register(CommandType type, String regex) {
    patterns.add(new CommandPattern(type, Pattern.compile(regex, Pattern.CASE_INSENSITIVE)));
  }

  /**
   * Runs the registered patterns against the raw command string.
   *
   * @param raw user input (may be null/blank)
   * @return a {@link CommandMatch} describing the detected command (UNKNOWN if no pattern fits)
   */
  public CommandMatch interpret(String raw) {
    if (raw == null) {
      return CommandMatch.noMatch(null);
    }
    String normalized = raw.trim();
    if (normalized.isEmpty()) {
      return CommandMatch.noMatch(raw);
    }
    for (CommandPattern p : patterns) {
      Matcher matcher = p.pattern.matcher(normalized);
      if (matcher.matches()) {
        return CommandMatch.matched(raw, normalized, p);
      }
    }
    return CommandMatch.unknown(raw, normalized);
  }

  /**
   * Supported command families. Each enum represents a single regex in {@link CommandInterpreter}.
   */
  public enum CommandType {
    PRINT_ON,
    PRINT_RANGE,
    SHOW_STATUS,
    EXPORT_CAL,

    CREATE_TIMED_EVENT,
    CREATE_TIMED_SERIES_FOR,
    CREATE_TIMED_SERIES_UNTIL,
    CREATE_ALLDAY_EVENT,
    CREATE_ALLDAY_SERIES_FOR,
    CREATE_ALLDAY_SERIES_UNTIL,

    EDIT_EVENT_FROM_TO,
    EDIT_EVENTS_FROM,
    EDIT_SERIES,

    CREATE_CALENDAR,
    EDIT_CALENDAR,
    HELP,
    LIST_CALENDARS,
    USE_CALENDAR,
    COPY_EVENT_SINGLE,
    COPY_EVENTS_ON,
    COPY_EVENTS_BETWEEN,
    EXIT,
    UNKNOWN
  }

  private static final class CommandPattern {
    final CommandType type;
    final Pattern pattern;

    /**
     * Create a pairing between a command type and its compiled regex.
     *
     * @param type    logical command family represented by the regex
     * @param pattern compiled regular expression used for matching
     */
    CommandPattern(CommandType type, Pattern pattern) {
      this.type = type;
      this.pattern = pattern;
    }
  }

  /**
   * Result of interpreting a command string.
   */
  public static final class CommandMatch {
    private final CommandType type;
    private final CommandPattern pattern;
    private final String normalized;
    private final boolean matched;

    private CommandMatch(CommandType type,
                         CommandPattern pattern,
                         String raw,
                         String normalized,
                         boolean matched) {
      this.type = type;
      this.pattern = pattern;
      this.normalized = normalized;
      this.matched = matched;
    }

    /**
     * Factory for successful matches.
     */
    static CommandMatch matched(String raw, String normalized, CommandPattern pattern) {
      return new CommandMatch(pattern.type, pattern, raw, normalized, true);
    }

    /**
     * Factory for inputs that did not match any pattern.
     */
    static CommandMatch unknown(String raw, String normalized) {
      return new CommandMatch(CommandType.UNKNOWN, null, raw, normalized, false);
    }


    /**
     * Factory for the null/blank input case.
     */
    static CommandMatch noMatch(String raw) {
      return new CommandMatch(CommandType.UNKNOWN, null, raw, raw == null
          ? null : raw.trim(), false);
    }


    /**
     * method to return type.
     *
     * @return the detected command type (UNKNOWN if no pattern matched)
     */
    public CommandType type() {
      return type;
    }

    /**
     * normalized method.
     *
     * @return the trimmed version RAW
     */
    public String normalized() {
      return normalized;
    }

    /**
     * Returns a fresh matcher positioned on this command's pattern.
     *
     * @return matcher for repeated group extraction
     * @throws IllegalStateException if no pattern matched
     */
    public Matcher matcher() {
      if (!matched || pattern == null) {
        throw new IllegalStateException("No regex pattern available for type " + type);
      }
      Matcher matcher = pattern.pattern.matcher(Objects.requireNonNull(normalized));
      if (!matcher.matches()) {
        throw new IllegalStateException("Internal error rematching command: " + normalized);
      }
      return matcher;
    }
  }
}
