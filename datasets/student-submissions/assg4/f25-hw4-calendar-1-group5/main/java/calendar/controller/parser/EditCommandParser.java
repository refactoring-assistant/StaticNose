package calendar.controller.parser;

import calendar.controller.Command;
import calendar.controller.Parser;
import calendar.controller.commands.EditCommand;
import calendar.controller.commands.EditProperty;
import calendar.controller.commands.EditScope;
import calendar.model.Event;
import calendar.model.EventV1;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for edit commands.
 */
public class EditCommandParser implements Parser {
  private final DateTimeFormatter dateTimeFormat =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

  private final Pattern editEventPattern = Pattern.compile(
      "^event\\s+(?<property>\\w+)\\s+(?<subject>.+?)\\s+from\\s+"
          + "(?<from>\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})\\s+to\\s+"
          + "(?<to>\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})\\s+with\\s+(?<newValue>.+)$");

  private final Pattern editEventsPattern = Pattern.compile(
      "^events\\s+(?<property>\\w+)\\s+(?<subject>.+?)\\s+from\\s+"
          + "(?<from>\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})\\s+with\\s+(?<newValue>.+)$");

  private final Pattern editSeriesPattern = Pattern.compile(
      "^series\\s+(?<property>\\w+)\\s+(?<subject>.+?)\\s+from\\s+"
          + "(?<from>\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})\\s+with\\s+(?<newValue>.+)$");

  @Override
  public Command parse(Scanner scanner) {
    String input = scanner.nextLine().trim();
    Matcher matcher;

    if ((matcher = editEventPattern.matcher(input)).matches()) {
      return buildEditCommandWithEndTime(matcher, EditScope.SINGLE);
    } else if ((matcher = editEventsPattern.matcher(input)).matches()) {
      return buildEditCommandWithoutEndTime(matcher, EditScope.FORWARD);
    } else if ((matcher = editSeriesPattern.matcher(input)).matches()) {
      return buildEditCommandWithoutEndTime(matcher, EditScope.SERIES);
    }

    return null;
  }

  private Command buildEditCommandWithEndTime(Matcher matcher, EditScope scope) {
    String subject = parseSubject(matcher.group("subject"));
    LocalDateTime startsAt = LocalDateTime.parse(matcher.group("from"), dateTimeFormat);
    LocalDateTime endsAt = LocalDateTime.parse(matcher.group("to"), dateTimeFormat);

    EventV1.Builder builder = new EventV1.Builder();
    builder.subject(subject);
    builder.startsAt(startsAt);
    builder.endsAt(endsAt);
    Event targetEvent = builder.build();

    EditProperty property = parseProperty(matcher.group("property"));
    String newValue = matcher.group("newValue").trim();

    return new EditCommand(scope, property, targetEvent, newValue);
  }

  private Command buildEditCommandWithoutEndTime(Matcher matcher, EditScope scope) {
    String subject = parseSubject(matcher.group("subject"));
    LocalDateTime startsAt = LocalDateTime.parse(matcher.group("from"), dateTimeFormat);

    EventV1.Builder builder = new EventV1.Builder();
    builder.subject(subject);
    builder.startsAt(startsAt);
    builder.endsAt(startsAt.plusHours(1));
    Event targetEvent = builder.build();

    String newValue = matcher.group("newValue").trim();
    EditProperty property = parseProperty(matcher.group("property"));

    return new EditCommand(scope, property, targetEvent, newValue);
  }

  private EditProperty parseProperty(String propertyStr) {
    switch (propertyStr.toLowerCase()) {
      case "subject":
        return EditProperty.SUBJECT;
      case "start":
        return EditProperty.START;
      case "end":
        return EditProperty.END;
      case "description":
        return EditProperty.DESCRIPTION;
      case "location":
        return EditProperty.LOCATION;
      case "status":
        return EditProperty.STATUS;
      default:
        throw new IllegalArgumentException("Invalid property: " + propertyStr);
    }
  }

  private String parseSubject(String subject) {
    subject = subject.trim();
    if (subject.startsWith("\"") && subject.endsWith("\"") && subject.length() > 1) {
      subject = subject.substring(1, subject.length() - 1);
    }
    return subject;
  }
}