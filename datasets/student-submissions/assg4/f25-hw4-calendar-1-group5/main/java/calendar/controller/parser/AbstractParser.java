package calendar.controller.parser;

import calendar.controller.Command;
import calendar.controller.Parser;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

/**
 * An abstract parser class implementing the Parser interface.
 */
public abstract class AbstractParser implements Parser {
  protected final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  protected final DateTimeFormatter dateTimeFormat =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

  @Override
  public abstract Command parse(Scanner scanner);
}
