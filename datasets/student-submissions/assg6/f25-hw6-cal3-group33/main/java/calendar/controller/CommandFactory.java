package calendar.controller;

import calendar.controller.command.Command;
import calendar.controller.command.CopyEventCommand;
import calendar.controller.command.CopyEventsBetweenCommand;
import calendar.controller.command.CopyEventsOnDateCommand;
import calendar.controller.command.CreateAllDayEventCommand;
import calendar.controller.command.CreateCalendarCommand;
import calendar.controller.command.CreateEventCommand;
import calendar.controller.command.CreateRepeatingAllDayCommand;
import calendar.controller.command.CreateRepeatingEventCommand;
import calendar.controller.command.EditCalendarCommand;
import calendar.controller.command.EditEventsFromCommand;
import calendar.controller.command.EditSeriesCommand;
import calendar.controller.command.EditSingleEventCommand;
import calendar.controller.command.ExportCalendarCommand;
import calendar.controller.command.PrintEventsCommand;
import calendar.controller.command.PrintEventsRangeCommand;
import calendar.controller.command.ShowStatusCommand;
import calendar.controller.command.UseCalendarCommand;
import java.util.regex.Matcher;

/**
 * Factory for creating Command objects from parsed regex matches.
 *
 * <p>Separates command creation logic from pattern matching logic, adhering to
 * Single Responsibility Principle. Each factory method handles extraction of
 * parameters from regex capture groups and instantiation of the appropriate
 * Command object.
 *
 * <p>This design eliminates the "long method" code smell by breaking down
 * command creation into focused, single-purpose methods.
 */
public class CommandFactory {

  /**
   * Creates a Command based on the matched pattern.
   *
   * <p>Uses polymorphism to delegate to appropriate factory method
   * based on the CommandPattern enum value.
   *
   * @param pattern the CommandPattern that matched
   * @param matcher the Matcher containing capture groups from the match
   * @return the appropriate Command object
   * @throws IllegalArgumentException if pattern is unrecognized
   */
  public Command createCommand(CommandPattern pattern, Matcher matcher) {
    switch (pattern) {
      case CREATE_CALENDAR:
        return createCalendarCommand(matcher);
      case EDIT_CALENDAR:
        return createEditCalendarCommand(matcher);
      case USE_CALENDAR:
        return createUseCalendarCommand(matcher);
      case COPY_EVENT:
        return createCopyEventCommand(matcher);
      case COPY_EVENTS_ON_DATE:
        return createCopyEventsOnDateCommand(matcher);
      case COPY_EVENTS_BETWEEN:
        return createCopyEventsBetweenCommand(matcher);
      case CREATE_EVENT_REPEAT_FOR:
        return createRepeatingEventForCommand(matcher);
      case CREATE_EVENT_REPEAT_UNTIL:
        return createRepeatingEventUntilCommand(matcher);
      case CREATE_EVENT:
        return createEventCommand(matcher);
      case CREATE_ALLDAY_REPEAT_FOR:
        return createAllDayRepeatForCommand(matcher);
      case CREATE_ALLDAY_REPEAT_UNTIL:
        return createAllDayRepeatUntilCommand(matcher);
      case CREATE_ALLDAY_EVENT:
        return createAllDayEventCommand(matcher);
      case EDIT_SINGLE_EVENT:
        return createEditSingleEventCommand(matcher);
      case EDIT_EVENTS_FROM:
        return createEditEventsFromCommand(matcher);
      case EDIT_SERIES:
        return createEditSeriesCommand(matcher);
      case PRINT_EVENTS_ON:
        return createPrintEventsCommand(matcher);
      case PRINT_EVENTS_RANGE:
        return createPrintEventsRangeCommand(matcher);
      case EXPORT_CALENDAR:
        return createExportCalendarCommand(matcher);
      case SHOW_STATUS:
        return createShowStatusCommand(matcher);

      default:
        throw new IllegalArgumentException("Unrecognized pattern: " + pattern);
    }
  }


  /**
   * Creates a CreateCalendarCommand from matcher groups.
   *
   * @param matcher the regex matcher with capture groups
   * @return CreateCalendarCommand instance
   */
  private Command createCalendarCommand(Matcher matcher) {
    String name = extractCalendarName(matcher, 1, 2);
    String timezone = matcher.group(3);
    return new CreateCalendarCommand(name, timezone);
  }

  /**
   * Creates an EditCalendarCommand from matcher groups.
   *
   * @param matcher the regex matcher with capture groups
   * @return EditCalendarCommand instance
   */
  private Command createEditCalendarCommand(Matcher matcher) {
    String calendarName = extractCalendarName(matcher, 1, 2);
    String property = matcher.group(3);
    String newValue = matcher.group(4).trim();
    return new EditCalendarCommand(calendarName, property, newValue);
  }

  /**
   * Creates a UseCalendarCommand from matcher groups.
   *
   * @param matcher the regex matcher with capture groups
   * @return UseCalendarCommand instance
   */
  private Command createUseCalendarCommand(Matcher matcher) {
    String name = extractCalendarName(matcher, 1, 2);
    return new UseCalendarCommand(name);
  }


  /**
   * Creates a CopyEventCommand from matcher groups.
   *
   * @param matcher the regex matcher with capture groups
   * @return CopyEventCommand instance
   */
  private Command createCopyEventCommand(Matcher matcher) {
    String subject = extractSubject(matcher, 1, 2);
    String sourceDateTime = matcher.group(3);
    String targetCalendar = extractCalendarName(matcher, 4, 5);
    String targetDateTime = matcher.group(6);
    return new CopyEventCommand(subject, sourceDateTime, targetCalendar, targetDateTime);
  }

  /**
   * Creates a CopyEventsOnDateCommand from matcher groups.
   *
   * @param matcher the regex matcher with capture groups
   * @return CopyEventsOnDateCommand instance
   */
  private Command createCopyEventsOnDateCommand(Matcher matcher) {
    String sourceDate = matcher.group(1);
    String targetCalendar = extractCalendarName(matcher, 2, 3);
    String targetDate = matcher.group(4);
    return new CopyEventsOnDateCommand(sourceDate, targetCalendar, targetDate);
  }

  /**
   * Creates a CopyEventsBetweenCommand from matcher groups.
   *
   * @param matcher the regex matcher with capture groups
   * @return CopyEventsBetweenCommand instance
   */
  private Command createCopyEventsBetweenCommand(Matcher matcher) {
    String sourceStart = matcher.group(1);
    String sourceEnd = matcher.group(2);
    String targetCalendar = extractCalendarName(matcher, 3, 4);
    String targetStart = matcher.group(5);
    return new CopyEventsBetweenCommand(sourceStart, sourceEnd, targetCalendar, targetStart);
  }


  /**
   * Creates a CreateEventCommand from matcher groups.
   *
   * @param matcher the regex matcher with capture groups
   * @return CreateEventCommand instance
   */
  private Command createEventCommand(Matcher matcher) {
    String subject = extractSubject(matcher, 1, 2);
    String startDateTime = matcher.group(3);
    String endDateTime = matcher.group(4);
    return new CreateEventCommand(subject, startDateTime, endDateTime);
  }

  /**
   * Creates a CreateRepeatingEventCommand (with count) from matcher groups.
   *
   * @param matcher the regex matcher with capture groups
   * @return CreateRepeatingEventCommand instance
   */
  private Command createRepeatingEventForCommand(Matcher matcher) {
    String subject = extractSubject(matcher, 1, 2);
    String startDateTime = matcher.group(3);
    String endDateTime = matcher.group(4);
    String weekdays = matcher.group(5);
    int count = Integer.parseInt(matcher.group(6));
    return new CreateRepeatingEventCommand(subject, startDateTime, endDateTime, weekdays, count);
  }

  /**
   * Creates a CreateRepeatingEventCommand (with until date) from matcher groups.
   *
   * @param matcher the regex matcher with capture groups
   * @return CreateRepeatingEventCommand instance
   */
  private Command createRepeatingEventUntilCommand(Matcher matcher) {
    String subject = extractSubject(matcher, 1, 2);
    String startDateTime = matcher.group(3);
    String endDateTime = matcher.group(4);
    String weekdays = matcher.group(5);
    String untilDate = matcher.group(6);
    return new CreateRepeatingEventCommand(subject, startDateTime, endDateTime, weekdays,
        untilDate);
  }

  /**
   * Creates a CreateAllDayEventCommand from matcher groups.
   *
   * @param matcher the regex matcher with capture groups
   * @return CreateAllDayEventCommand instance
   */
  private Command createAllDayEventCommand(Matcher matcher) {
    String subject = extractSubject(matcher, 1, 2);
    String date = matcher.group(3);
    return new CreateAllDayEventCommand(subject, date);
  }

  /**
   * Creates a CreateRepeatingAllDayCommand (with count) from matcher groups.
   *
   * @param matcher the regex matcher with capture groups
   * @return CreateRepeatingAllDayCommand instance
   */
  private Command createAllDayRepeatForCommand(Matcher matcher) {
    String subject = extractSubject(matcher, 1, 2);
    String date = matcher.group(3);
    String weekdays = matcher.group(4);
    int count = Integer.parseInt(matcher.group(5));
    return new CreateRepeatingAllDayCommand(subject, date, weekdays, count);
  }

  /**
   * Creates a CreateRepeatingAllDayCommand (with until date) from matcher groups.
   *
   * @param matcher the regex matcher with capture groups
   * @return CreateRepeatingAllDayCommand instance
   */
  private Command createAllDayRepeatUntilCommand(Matcher matcher) {
    String subject = extractSubject(matcher, 1, 2);
    String date = matcher.group(3);
    String weekdays = matcher.group(4);
    String untilDate = matcher.group(5);
    return new CreateRepeatingAllDayCommand(subject, date, weekdays, untilDate);
  }


  /**
   * Creates an EditSingleEventCommand from matcher groups.
   *
   * @param matcher the regex matcher with capture groups
   * @return EditSingleEventCommand instance
   */
  private Command createEditSingleEventCommand(Matcher matcher) {
    String property = matcher.group(1);
    String subject = extractSubject(matcher, 2, 3);
    String startDateTime = matcher.group(4);
    String endDateTime = matcher.group(5);
    String newValue = matcher.group(6);
    return new EditSingleEventCommand(property, subject, startDateTime, endDateTime, newValue);
  }

  /**
   * Creates an EditEventsFromCommand from matcher groups.
   *
   * @param matcher the regex matcher with capture groups
   * @return EditEventsFromCommand instance
   */
  private Command createEditEventsFromCommand(Matcher matcher) {
    String property = matcher.group(1);
    String subject = extractSubject(matcher, 2, 3);
    String startDateTime = matcher.group(4);
    String newValue = matcher.group(5);
    return new EditEventsFromCommand(property, subject, startDateTime, newValue);
  }

  /**
   * Creates an EditSeriesCommand from matcher groups.
   *
   * @param matcher the regex matcher with capture groups
   * @return EditSeriesCommand instance
   */
  private Command createEditSeriesCommand(Matcher matcher) {
    String property = matcher.group(1);
    String subject = extractSubject(matcher, 2, 3);
    String startDateTime = matcher.group(4);
    String newValue = matcher.group(5);
    return new EditSeriesCommand(property, subject, startDateTime, newValue);
  }


  /**
   * Creates a PrintEventsCommand from matcher groups.
   *
   * @param matcher the regex matcher with capture groups
   * @return PrintEventsCommand instance
   */
  private Command createPrintEventsCommand(Matcher matcher) {
    String date = matcher.group(1);
    return new PrintEventsCommand(date);
  }

  /**
   * Creates a PrintEventsRangeCommand from matcher groups.
   *
   * @param matcher the regex matcher with capture groups
   * @return PrintEventsRangeCommand instance
   */
  private Command createPrintEventsRangeCommand(Matcher matcher) {
    String startDateTime = matcher.group(1);
    String endDateTime = matcher.group(2);
    return new PrintEventsRangeCommand(startDateTime, endDateTime);
  }


  /**
   * Creates an ExportCalendarCommand from matcher groups.
   *
   * @param matcher the regex matcher with capture groups
   * @return ExportCalendarCommand instance
   */
  private Command createExportCalendarCommand(Matcher matcher) {
    String filePath = matcher.group(1);
    return new ExportCalendarCommand(filePath);
  }

  /**
   * Creates a ShowStatusCommand from matcher groups.
   *
   * @param matcher the regex matcher with capture groups
   * @return ShowStatusCommand instance
   */
  private Command createShowStatusCommand(Matcher matcher) {
    String dateTime = matcher.group(1);
    return new ShowStatusCommand(dateTime);
  }

  // ===== HELPER METHODS =====

  /**
   * Extracts subject from either quoted or unquoted capture groups.
   *
   * <p>One of the groups will be null, the other contains the subject.
   *
   * @param matcher the regex matcher containing capture groups
   * @param quotedGroup the capture group index for quoted subjects
   * @param unquotedGroup the capture group index for unquoted subjects
   * @return the extracted subject string
   */
  private String extractSubject(Matcher matcher, int quotedGroup, int unquotedGroup) {
    String quoted = matcher.group(quotedGroup);
    return quoted != null ? quoted : matcher.group(unquotedGroup);
  }

  /**
   * Extracts calendar name from either quoted or unquoted capture groups.
   *
   * <p>One of the groups will be null, the other contains the calendar name.
   *
   * @param matcher the regex matcher containing capture groups
   * @param quotedGroup the capture group index for quoted names
   * @param unquotedGroup the capture group index for unquoted names
   * @return the extracted calendar name string
   */
  private String extractCalendarName(Matcher matcher, int quotedGroup, int unquotedGroup) {
    String quoted = matcher.group(quotedGroup);
    return quoted != null ? quoted : matcher.group(unquotedGroup);
  }
}