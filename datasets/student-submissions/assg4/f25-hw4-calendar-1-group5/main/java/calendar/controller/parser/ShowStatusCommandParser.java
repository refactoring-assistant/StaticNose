package calendar.controller.parser;

import calendar.controller.Command;
import calendar.controller.Parser;
import calendar.controller.commands.ShowStatusCommand;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for show status commands.
 */
public class ShowStatusCommandParser implements Parser {
  private final DateTimeFormatter dateTimeFormat =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

  private final Pattern showStatusPattern = Pattern.compile(
      "^status\\s+on\\s+(?<dateTime>\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})$");

  @Override
  public Command parse(Scanner scanner) {
    String input = scanner.nextLine().trim();
    Matcher matcher = showStatusPattern.matcher(input);

    if (matcher.matches()) {
      LocalDateTime dateTime = LocalDateTime.parse(matcher.group("dateTime"), dateTimeFormat);
      return new ShowStatusCommand(dateTime);
    }

    return null;
  }
}