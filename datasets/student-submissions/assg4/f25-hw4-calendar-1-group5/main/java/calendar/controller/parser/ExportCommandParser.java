package calendar.controller.parser;

import calendar.controller.Command;
import calendar.controller.commands.ExportCommand;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is responsible to parse the export command input using regular expression.
 */
public class ExportCommandParser extends AbstractParser {
  private final Pattern pattern = Pattern.compile("cal (?<filepath>.*)");

  @Override
  public Command parse(Scanner scanner) {
    String input = scanner.nextLine();

    Matcher matcher = pattern.matcher(input);
    if (!matcher.find()) {
      return null;
    }

    return new ExportCommand(matcher.group("filepath"));
  }
}
