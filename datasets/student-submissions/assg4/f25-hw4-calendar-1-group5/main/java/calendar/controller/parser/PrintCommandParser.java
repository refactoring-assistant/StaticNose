package calendar.controller.parser;

import calendar.controller.Command;
import calendar.controller.commands.PrintCommand;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is responsible to parse the various print command inputs using regular expressions.
 */
public class PrintCommandParser extends AbstractParser {
  private final Pattern singleDayPattern = Pattern.compile("events"
      + " on (?<on>\\d{4}-\\d{2}-\\d{2})");
  private final Pattern rangePattern = Pattern.compile(
      "events"
          + " from (?<from>\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})"
          + " to (?<to>\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})");

  @Override
  public Command parse(Scanner scanner) {
    String input = scanner.nextLine();

    LocalDateTime from;
    LocalDateTime to;

    Matcher matcher;
    if ((matcher = singleDayPattern.matcher(input)).find()) {
      from = LocalDate.parse(matcher.group("on"), dateFormat).atStartOfDay();
      to = from.plusDays(1);
    } else if ((matcher = rangePattern.matcher(input)).find()) {
      from = LocalDateTime.parse(matcher.group("from"), dateTimeFormat);
      to = LocalDateTime.parse(matcher.group("to"), dateTimeFormat);
    } else {
      return null;
    }

    return new PrintCommand(from, to);
  }
}
