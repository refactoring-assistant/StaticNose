package calendar.controller.parser;

import calendar.controller.Command;
import calendar.controller.commands.CreateCommand;
import calendar.controller.commands.CreateSeriesUntilCommand;
import calendar.controller.commands.CreateSeriesWithRepetitionCommand;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is responsible to parse the various create commands using regular expressions.
 */
public class CreateCommandParser extends AbstractParser {
  private final Pattern basePattern = Pattern.compile(
      "event (?<subject>.*)" + " from (?<from>\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})"
          + " to (?<to>\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})");
  private final Pattern repetitionPattern = Pattern.compile(
      "event (?<subject>.*)" + " from (?<from>\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})"
          + " to (?<to>\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})" + " repeats (?<weekdays>[UMTWRFS]{1,7})"
          + " for (?<repetition>\\d+) times");
  private final Pattern repeatUntilPattern = Pattern.compile(
      "event (?<subject>.*)" + " from (?<from>\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})"
          + " to (?<to>\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})" + " repeats (?<weekdays>[UMTWRFS]{1,7})"
          + " until (?<repeatUntil>\\d{4}-\\d{2}-\\d{2})");
  private final Pattern baseAllDayPattern =
      Pattern.compile("event (?<subject>.*)" + " on (?<on>\\d{4}-\\d{2}-\\d{2})");
  private final Pattern allDayRepetitionPattern = Pattern.compile(
      "event (?<subject>.*)" + " on (?<on>\\d{4}-\\d{2}-\\d{2})"
          + " repeats (?<weekdays>[UMTWRFS]{1,7})" + " for (?<repetition>\\d+) times");
  private final Pattern allDayRepeatUntilPattern = Pattern.compile(
      "event \"(?<subject>.*)\"" + " on (?<on>\\d{4}-\\d{2}-\\d{2})"
          + " repeats (?<weekdays>[UMTWRFS]{1,7})" + " until (?<repeatUntil>\\d{4}-\\d{2}-\\d{2})");

  @Override
  public Command parse(Scanner scanner) {
    String input = scanner.nextLine();
    Matcher matcher;
    if ((matcher = allDayRepeatUntilPattern.matcher(input)).find()) {
      return buildAllDayRepeatUntilCommand(matcher);
    } else if ((matcher = allDayRepetitionPattern.matcher(input)).find()) {
      return buildAllDayRepetitionCommand(matcher);
    } else if ((matcher = baseAllDayPattern.matcher(input)).find()) {
      return buildBaseAllDayCommand(matcher);
    } else if ((matcher = repeatUntilPattern.matcher(input)).find()) {
      return buildRepeatUntilCommand(matcher);
    } else if ((matcher = repetitionPattern.matcher(input)).find()) {
      return buildRepetitionCommand(matcher);
    } else if ((matcher = basePattern.matcher(input)).find()) {
      return buildBaseCommand(matcher);
    }

    return null;
  }

  private Command buildAllDayRepeatUntilCommand(Matcher matcher) {
    LocalDateTime date = LocalDate.parse(matcher.group("on"), dateFormat).atStartOfDay();
    HashSet<DayOfWeek> weekdays = buildWeekdaysArr(matcher.group("weekdays"));
    LocalDate repeatUntil = LocalDate.parse(matcher.group("repeatUntil"), dateFormat);

    return new CreateSeriesUntilCommand(matcher.group("subject"), date.withHour(8),
        date.withHour(17), weekdays, repeatUntil);
  }

  private Command buildAllDayRepetitionCommand(Matcher matcher) {
    LocalDateTime date = LocalDate.parse(matcher.group("on"), dateFormat).atStartOfDay();
    HashSet<DayOfWeek> weekdays = buildWeekdaysArr(matcher.group("weekdays"));
    int repetition = Integer.parseInt(matcher.group("repetition"));

    return new CreateSeriesWithRepetitionCommand(matcher.group("subject"), date.withHour(8),
        date.withHour(17), weekdays, repetition);
  }

  private Command buildBaseAllDayCommand(Matcher matcher) {
    LocalDateTime date = LocalDate.parse(matcher.group("on"), dateFormat).atStartOfDay();

    return new CreateCommand(matcher.group("subject"), date.withHour(8), date.withHour(17));
  }

  private Command buildRepeatUntilCommand(Matcher matcher) {
    HashSet<DayOfWeek> weekdays = buildWeekdaysArr(matcher.group("weekdays"));
    LocalDate repeatUntil = LocalDate.parse(matcher.group("repeatUntil"), dateFormat);

    return new CreateSeriesUntilCommand(matcher.group("subject"),
        LocalDateTime.parse(matcher.group("from"), dateTimeFormat),
        LocalDateTime.parse(matcher.group("to"), dateTimeFormat), weekdays, repeatUntil);
  }

  private Command buildRepetitionCommand(Matcher matcher) {
    HashSet<DayOfWeek> weekdays = buildWeekdaysArr(matcher.group("weekdays"));
    int repetition = Integer.parseInt(matcher.group("repetition"));

    return new CreateSeriesWithRepetitionCommand(matcher.group("subject"),
        LocalDateTime.parse(matcher.group("from"), dateTimeFormat),
        LocalDateTime.parse(matcher.group("to"), dateTimeFormat), weekdays, repetition);
  }

  private Command buildBaseCommand(Matcher matcher) {
    return new CreateCommand(matcher.group("subject"),
        LocalDateTime.parse(matcher.group("from"), dateTimeFormat),
        LocalDateTime.parse(matcher.group("to"), dateTimeFormat));
  }

  private HashSet<DayOfWeek> buildWeekdaysArr(String weekdaysStr) {
    HashSet<DayOfWeek> weekdays = new HashSet<>();
    for (char c : weekdaysStr.toCharArray()) {
      DayOfWeek dayOfWeek = charToDayOfWeek(c);

      if (weekdays.contains(dayOfWeek)) {
        throw new IllegalArgumentException("Weekdays cannot be repeated");
      } else {
        weekdays.add(dayOfWeek);
      }
    }

    return weekdays;
  }

  private DayOfWeek charToDayOfWeek(char c) {
    switch (c) {
      case 'U':
        return DayOfWeek.SUNDAY;
      case 'M':
        return DayOfWeek.MONDAY;
      case 'T':
        return DayOfWeek.TUESDAY;
      case 'W':
        return DayOfWeek.WEDNESDAY;
      case 'R':
        return DayOfWeek.THURSDAY;
      case 'F':
        return DayOfWeek.FRIDAY;
      case 'S':
      default:
        return DayOfWeek.SATURDAY;
    }
  }
}
