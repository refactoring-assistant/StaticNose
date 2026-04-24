package calendar.control.commands;

import calendar.control.WeekDays;
import calendar.control.results.CommandResult;
import calendar.model.Imodel;
import calendar.model.database.IcalendarDatabase;
import calendar.utils.StringUtils;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Command implementation to create a series of recurring calendar events.
 * Creates an event series that repeats N times on specific weekdays.
 * Syntax:
 * create event subject from YYYY-MM-DDThh:mm to YYYY-MM-DDThh:mm repeats
 * MRUWFSU for N times. Example:
 * create event "First" from 2025-05-05T10:00 to 2025-05-05T11:00 repeats MW for 6 times
 */
public class CreateSeriesCommand extends AbstractCommand {

  private static final Pattern patternOccurrences = Pattern.compile(
      "create\\s+event\\s+\"?([^\"]+)\"?\\s+from\\s+(\\d{4}-\\d{2}-\\d{2}T"
          + "\\d{2}:\\d{2})\\s+to\\s+(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})\\s+repeats"
          + "\\s+([MTWRFSU]+)\\s+for\\s+(\\d+)\\s+times",
      Pattern.CASE_INSENSITIVE);
  private static final Pattern patternUntil = Pattern.compile(
      "create\\s+event\\s+\"?([^\"]+)\"?\\s+from\\s+(\\d{4}-\\d{2}-\\d{2}T"
          + "\\d{2}:\\d{2})\\s+to\\s+(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})\\s+repeats"
          + "\\s+([MTWRFSU]+)\\s+until\\s+(\\d{4}-\\d{2}-\\d{2})",
      Pattern.CASE_INSENSITIVE);
  private static final Pattern patterAllDayOccurrences = Pattern.compile(
      "create\\s+event\\s+\"?([^\"]+)\"?\\s+on\\s+(\\d{4}-\\d{2}-\\d{2})\\s+repeats"
          + "\\s+([MTWRFSU]+)\\s+for\\s+(\\d+)\\s+times",
      Pattern.CASE_INSENSITIVE);
  private static final Pattern patternAllDayUntil = Pattern.compile(
      "create\\s+event\\s+\"?([^\"]+)\"?\\s+on\\s+(\\d{4}-\\d{2}-\\d{2})\\s+repeats"
          + "\\s+([MTWRFSU]+)\\s+until\\s+(\\d{4}-\\d{2}-\\d{2})",
      Pattern.CASE_INSENSITIVE);
  private static final LocalTime allDayStartTime = LocalTime.of(8, 0);
  private static final LocalTime allDayEndTime = LocalTime.of(17, 0);

  /**
   * Constructs a Create Series Command instance.
   *
   * @param multipleCalendar - multiple calendars
   * @param input            - the raw input command string to be parsed
   */
  public CreateSeriesCommand(IcalendarDatabase multipleCalendar, String input) {
    super(multipleCalendar, input);
  }

  /**
   * Convert a string like "MRU" to a set {MONDAY, THURSDAY, SUNDAY}.
   *
   * @param letters string of weekday letters (M,T,W,R,F,S,U)
   * @return Set of DayOfWeek corresponding to letters
   * @throws IllegalArgumentException if any letter is invalid
   */
  private static Set<DayOfWeek> parseDays(String letters) {
    Set<DayOfWeek> days = new LinkedHashSet<>();
    for (char c : letters.toUpperCase().toCharArray()) {
      WeekDays weekDay = WeekDays.valueOf(String.valueOf(c));
      days.add(weekDay.getDay());
    }
    return days;
  }


  @Override
  public CommandResult execute() {
    try {
      if (!multipleCalendar.hasCalendars()) {
        return CommandResult.error("Error: No calendars exist. Create a calendar first using:\n"
            + "  create calendar --name <name> --timezone <timezone>");
      }

      Optional<Imodel> currentCal = multipleCalendar.getCurrent();
      if (currentCal.isEmpty()) {
        return CommandResult.error("Error: No calendar is currently selected. Use:\n"
            + "  use calendar --name <name>\n\n"
            + "Available calendars: " + getAvailableCalendarNames());
      }
      Imodel model = currentCal.get();
      CommandResult result;

      result = eventSeriesOccurrences(model);
      if (result != null) {
        return result;
      }

      result = eventSeriesDateRange(model);
      if (result != null) {
        return result;
      }

      result = allDayEventSeriesOccurrences(model);
      if (result != null) {
        return result;
      }

      result = allDayEventSeriesDateRange(model);
      if (result != null) {
        return result;
      }

      return CommandResult.error("Invalid create series syntax.");
    } catch (Exception e) {
      return CommandResult.error("Create series failed: " + e.getMessage());
    }
  }

  /**
   * Helper functions to parse and create a timed event series with fixed
   * number of occurrences.
   *
   * @return a boolean - true if no. of occurrences provided.
   */
  private CommandResult eventSeriesOccurrences(Imodel model) {
    Matcher matcher = patternOccurrences.matcher(input);

    if (!matcher.find()) {
      return null;
    }
    String subject = StringUtils.removeQuotes(matcher.group(1));
    LocalDateTime startDateTime = LocalDateTime.parse(matcher.group(2));
    LocalDateTime endDateTime = LocalDateTime.parse(matcher.group(3));
    String letters = matcher.group(4).toUpperCase();
    int count = Integer.parseInt(matcher.group(5));

    boolean ok = model.createEventSeries(subject, startDateTime.toLocalDate(),
        startDateTime.toLocalTime(), count, endDateTime.toLocalTime(),
        parseDays(letters));

    String calendarName = multipleCalendar.getCurrentCalendarName();

    if (ok) {
      return CommandResult.success(
          "Series created: " + subject + " in calendar '" + calendarName + "'");
    } else {
      return CommandResult.error("Failed to create series (conflict may exist).");
    }
  }

  /**
   * Helper functions for parsing and creating timed event series with
   * range of dates.
   *
   * @return a boolean - true if date range is provided.
   */
  private CommandResult eventSeriesDateRange(Imodel model) {

    Matcher matcher = patternUntil.matcher(input);
    if (!matcher.find()) {
      return null;
    }
    String subject = StringUtils.removeQuotes(matcher.group(1));
    LocalDateTime startDateTime = LocalDateTime.parse(matcher.group(2));
    LocalDateTime endDateTime = LocalDateTime.parse(matcher.group(3));
    String letters = matcher.group(4).toUpperCase();
    LocalDate until = LocalDate.parse(matcher.group(5));

    boolean ok = model.createEventSeriesUntil(subject, startDateTime.toLocalDate(),
        startDateTime.toLocalTime(), until, endDateTime.toLocalTime(),
        parseDays(letters));

    String calendarName = multipleCalendar.getCurrentCalendarName();

    if (ok) {
      return CommandResult.success(
          "Series created: " + subject + " in calendar '" + calendarName + "'");
    } else {
      return CommandResult.error("Failed to create series (conflict may exist).");
    }
  }

  /**
   * Helper which parses and creates an all-day event series with a
   * fixed number of occurrences.
   *
   * @return a boolean - true if it is all day event series.
   */

  private CommandResult allDayEventSeriesOccurrences(Imodel model) {

    Matcher matcher = patterAllDayOccurrences.matcher(input);

    if (!matcher.find()) {
      return null;
    }
    String subject = StringUtils.removeQuotes(matcher.group(1));
    LocalDate startDate = LocalDate.parse(matcher.group(2));
    String letters = matcher.group(3).toUpperCase();
    int count = Integer.parseInt(matcher.group(4));

    boolean ok = model.createEventSeries(subject, startDate, allDayStartTime, count,
        allDayEndTime, parseDays(letters));

    String calendarName = multipleCalendar.getCurrentCalendarName();

    if (ok) {
      return CommandResult.success(
          "Series created: " + subject + " in calendar '" + calendarName + "'");
    } else {
      return CommandResult.error("Failed to create series (conflict may exist).");
    }
  }

  /**
   * Helper to parse and create an all-day event series with
   * an end date range.
   *
   * @return boolean.
   */
  private CommandResult allDayEventSeriesDateRange(Imodel model) {

    Matcher matcher = patternAllDayUntil.matcher(input);

    if (!matcher.find()) {
      return null;
    }
    String subject = StringUtils.removeQuotes(matcher.group(1));
    LocalDate startDate = LocalDate.parse(matcher.group(2));
    String letters = matcher.group(3).toUpperCase();
    LocalDate untilDate = LocalDate.parse(matcher.group(4));

    boolean ok = model.createEventSeriesUntil(subject, startDate, allDayStartTime,
        untilDate, allDayEndTime, parseDays(letters));

    String calendarName = multipleCalendar.getCurrentCalendarName();

    if (ok) {
      return CommandResult.success(
          "Series created: " + subject + " in calendar '" + calendarName + "'");
    } else {
      return CommandResult.error("Failed to create series (conflict may exist).");
    }
  }

}