package calendar.controller;

import calendar.controller.commands.AddEventCommand;
import calendar.controller.commands.CalendarCommand;
import calendar.controller.commands.CopyEventCommand;
import calendar.controller.commands.CopyEventsBetweenCommand;
import calendar.controller.commands.CopyEventsOnDateCommand;
import calendar.controller.commands.CreateAllDayEventCommand;
import calendar.controller.commands.CreateAllDaySeriesCommand;
import calendar.controller.commands.CreateCalendarCommand;
import calendar.controller.commands.CreateSeriesCommand;
import calendar.controller.commands.CreateSeriesUntilCommand;
import calendar.controller.commands.EditCalendarCommand;
import calendar.controller.commands.EditEventCommand;
import calendar.controller.commands.EditEventsCommand;
import calendar.controller.commands.EditSeriesCommand;
import calendar.controller.commands.ExportCommand;
import calendar.controller.commands.ListEventsCommand;
import calendar.controller.commands.PrintEventsCommand;
import calendar.controller.commands.PrintEventsOnDateCommand;
import calendar.controller.commands.ShowStatusCommand;
import calendar.controller.commands.SystemCommand;
import calendar.controller.commands.UseCalendarCommand;
import calendar.model.CalendarSystem;
import calendar.model.WeekDay;
import calendar.view.CalendarView;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.zone.ZoneRulesException;
import java.util.List;
import java.util.Scanner;

/**
 * Implementation of the CalendarController interface.
 * Handles all command parsing and execution for multiple calendars.
 */
public class CalendarControllerImpl implements CalendarController {

  private final CalendarSystem system;
  private final CalendarView view;
  private final Readable input;

  /**
   * Constructor for the calendar controller.
   *
   * @param system The calendar system to be used.
   * @param view   The calendar view to be used.
   * @param input  The readable input source for user commands.
   */
  public CalendarControllerImpl(CalendarSystem system, CalendarView view, Readable input) {
    this.system = system;
    this.view = view;
    this.input = input;
  }

  @Override
  public boolean run() {
    Scanner scanner = new Scanner(input);
    view.displayWelcome();

    while (scanner.hasNextLine()) {
      String line = scanner.nextLine().trim();
      if (line.isEmpty()) {
        continue;
      }

      if (line.equalsIgnoreCase("exit")) {
        view.displayGoodbye();
        return true;
      }
      processCommand(line);
    }
    return false;
  }

  private void processCommand(String line) {
    if (line.startsWith("create calendar")) {
      executeCreateCalendar(line);
    } else if (line.startsWith("edit calendar")) {
      executeEditCalendar(line);
    } else if (line.startsWith("use calendar")) {
      executeUseCalendar(line);
    } else if (line.startsWith("copy event ") && line.contains(" on ")) {
      executeCopyEvent(line);
    } else if (line.startsWith("copy events between")) {
      executeCopyEventsBetween(line);
    } else if (line.startsWith("copy events on")) {
      executeCopyEventsOnDate(line);
    } else if (line.startsWith("create event")) {
      if (!hasCurrentCalendar()) {
        view.displayError("No calendar in use. Use 'use calendar --name <name>' first.");
        return;
      }
      executeCreateCommand(line);
    } else if (line.startsWith("edit series")) {
      if (!hasCurrentCalendar()) {
        view.displayError("No calendar in use. Use 'use calendar --name <name>' first.");
        return;
      }
      executeEditCommand(line, "series");
    } else if (line.startsWith("edit events")) {
      if (!hasCurrentCalendar()) {
        view.displayError("No calendar in use. Use 'use calendar --name <name>' first.");
        return;
      }
      executeEditCommand(line, "events");
    } else if (line.startsWith("edit event")) {
      if (!hasCurrentCalendar()) {
        view.displayError("No calendar in use. Use 'use calendar --name <name>' first.");
        return;
      }
      executeEditCommand(line, "event");
    } else if (line.startsWith("print events on")) {
      if (!hasCurrentCalendar()) {
        view.displayError("No calendar in use. Use 'use calendar --name <name>' first.");
        return;
      }
      executePrintOnDateCommand(line);
    } else if (line.startsWith("print events from")) {
      if (!hasCurrentCalendar()) {
        view.displayError("No calendar in use. Use 'use calendar --name <name>' first.");
        return;
      }
      executePrintRangeCommand(line);
    } else if (line.startsWith("print events")) {
      view.displayError("Expected 'on' in command");
    } else if (line.startsWith("show status on")) {
      if (!hasCurrentCalendar()) {
        view.displayError("No calendar in use. Use 'use calendar --name <name>' first.");
        return;
      }
      executeShowStatus(line);
    } else if (line.startsWith("show status")) {
      view.displayError("Expected 'on' in command");
    } else if (line.startsWith("show busy") || line.startsWith("show available")) {
      executeShowBusy(line);
    } else if (line.startsWith("export")) {
      if (!hasCurrentCalendar()) {
        view.displayError("No calendar in use. Use 'use calendar --name <name>' first.");
        return;
      }
      executeExportCommand(line);
    } else if (line.equalsIgnoreCase("list all events")) {
      if (!hasCurrentCalendar()) {
        view.displayError("No calendar in use. Use 'use calendar --name <name>' first.");
        return;
      }
      executeListAllEvents();
    } else {
      view.displayError("Unknown command: " + line);
    }
  }

  private boolean hasCurrentCalendar() {
    return system.getCurrentCalendar() != null;
  }

  private void executeCreateCalendar(String line) {
    try {
      String afterCreate = line.substring("create calendar".length()).trim();
      String name = extractFlagValue(afterCreate, "--name");
      String timezoneStr = extractFlagValue(afterCreate, "--timezone");

      ZoneId timezone = ZoneId.of(timezoneStr);
      SystemCommand cmd = new CreateCalendarCommand(name, timezone);
      view.displayMessage(cmd.execute(system));
    } catch (ZoneRulesException e) {
      view.displayError("Invalid timezone format. Use IANA format (e.g., America/New_York)");
    } catch (Exception e) {
      view.displayError("Error creating calendar: " + e.getMessage());
    }
  }

  private void executeEditCalendar(String line) {
    try {
      String afterEdit = line.substring("edit calendar".length()).trim();

      int propertyIndex = afterEdit.indexOf("--property");
      if (propertyIndex == -1) {
        view.displayError("Missing --property flag");
        return;
      }

      String afterProperty = afterEdit.substring(propertyIndex + "--property".length()).trim();
      if (afterProperty.isEmpty()) {
        view.displayError("Missing property name and new value");
        return;
      }

      String[] propertyParts = afterProperty.split("\\s+", 2);
      String property = propertyParts[0];
      String newValue = propertyParts.length > 1 ? propertyParts[1].trim() : "";

      if (newValue.isEmpty()) {
        view.displayError("Missing new value for property");
        return;
      }
      String name = extractFlagValue(afterEdit, "--name");

      SystemCommand cmd = new EditCalendarCommand(name, property, newValue);
      view.displayMessage(cmd.execute(system));

    } catch (Exception e) {
      view.displayError("Error editing calendar: " + e.getMessage());
    }
  }

  private void executeUseCalendar(String line) {
    try {
      String afterUse = line.substring("use calendar".length()).trim();
      String name = extractFlagValue(afterUse, "--name");

      SystemCommand cmd = new UseCalendarCommand(name);
      view.displayMessage(cmd.execute(system));
    } catch (Exception e) {
      view.displayError("Error using calendar: " + e.getMessage());
    }
  }

  private void executeCopyEvent(String line) {
    try {
      String afterCopy = line.substring("copy event".length()).trim();

      int onIndex = afterCopy.indexOf(" on ");
      if (onIndex == -1) {
        view.displayError("Expected 'on' in command");
        return;
      }
      String eventName = afterCopy.substring(0, onIndex).trim();
      if (eventName.startsWith("\"") && eventName.endsWith("\"")) {
        eventName = eventName.substring(1, eventName.length() - 1);
      }

      String afterOn = afterCopy.substring(onIndex + 4).trim();
      int targetIndex = afterOn.indexOf(" --target ");
      if (targetIndex == -1) {
        view.displayError("Expected '--target' in command");
        return;
      }
      String sourceTimeStr = afterOn.substring(0, targetIndex).trim();
      LocalDateTime sourceTime = LocalDateTime.parse(sourceTimeStr);

      String afterTarget = afterOn.substring(targetIndex + " --target ".length()).trim();
      int toIndex = afterTarget.indexOf(" to ");
      if (toIndex == -1) {
        view.displayError("Expected 'to' in command");
        return;
      }
      String targetCalName = afterTarget.substring(0, toIndex).trim();
      String targetTimeStr = afterTarget.substring(toIndex + 4).trim();
      LocalDateTime targetTime = LocalDateTime.parse(targetTimeStr);

      SystemCommand cmd = new CopyEventCommand(eventName, sourceTime, targetCalName, targetTime);
      view.displayMessage(cmd.execute(system));
    } catch (Exception e) {
      view.displayError("Error copying event: " + e.getMessage());
    }
  }

  private void executeCopyEventsOnDate(String line) {
    try {
      String afterCopy = line.substring("copy events on".length()).trim();

      int targetIndex = afterCopy.indexOf(" --target ");
      if (targetIndex == -1) {
        view.displayError("Expected '--target' in command");
        return;
      }
      String sourceDateStr = afterCopy.substring(0, targetIndex).trim();
      LocalDate sourceDate = LocalDate.parse(sourceDateStr);

      String afterTarget = afterCopy.substring(targetIndex + " --target ".length()).trim();
      int toIndex = afterTarget.indexOf(" to ");
      if (toIndex == -1) {
        view.displayError("Expected 'to' in command");
        return;
      }
      String targetCalName = afterTarget.substring(0, toIndex).trim();
      String targetDateStr = afterTarget.substring(toIndex + 4).trim();
      LocalDate targetDate = LocalDate.parse(targetDateStr);

      SystemCommand cmd = new CopyEventsOnDateCommand(sourceDate, targetCalName, targetDate);
      view.displayMessage(cmd.execute(system));
    } catch (Exception e) {
      view.displayError("Error copying events: " + e.getMessage());
    }
  }

  private void executeCopyEventsBetween(String line) {
    try {
      String afterCopy = line.substring("copy events between".length()).trim();

      int andIndex = afterCopy.indexOf(" and ");
      if (andIndex == -1) {
        view.displayError("Expected 'and' in command");
        return;
      }
      String startDateStr = afterCopy.substring(0, andIndex).trim();
      LocalDate startDate = LocalDate.parse(startDateStr);

      String afterAnd = afterCopy.substring(andIndex + 5).trim();
      int targetIndex = afterAnd.indexOf(" --target ");
      if (targetIndex == -1) {
        view.displayError("Expected '--target' in command");
        return;
      }
      String endDateStr = afterAnd.substring(0, targetIndex).trim();
      LocalDate endDate = LocalDate.parse(endDateStr);

      String afterTarget = afterAnd.substring(targetIndex + " --target ".length()).trim();
      int toIndex = afterTarget.indexOf(" to ");
      if (toIndex == -1) {
        view.displayError("Expected 'to' in command");
        return;
      }
      String targetCalName = afterTarget.substring(0, toIndex).trim();
      String targetDateStr = afterTarget.substring(toIndex + 4).trim();
      LocalDate targetDate = LocalDate.parse(targetDateStr);

      SystemCommand cmd = new CopyEventsBetweenCommand(startDate, endDate, targetCalName,
          targetDate);
      view.displayMessage(cmd.execute(system));
    } catch (Exception e) {
      view.displayError("Error copying events: " + e.getMessage());
    }
  }

  private String extractFlagValue(String line, String flag) {
    int flagIndex = line.indexOf(flag);
    if (flagIndex == -1) {
      throw new IllegalArgumentException("Missing flag: " + flag);
    }
    String afterFlag = line.substring(flagIndex + flag.length()).trim();
    int nextFlagIndex = afterFlag.indexOf("--");
    if (nextFlagIndex == -1) {
      return afterFlag.trim();
    }
    return afterFlag.substring(0, nextFlagIndex).trim();
  }

  private void executeCreateCommand(String line) {
    String subject = null;
    try {
      subject = extractSubject(line);
      String afterSubject = getAfterSubject(line).trim();

      if (afterSubject.isEmpty()) {
        view.displayError("Expected 'on' or 'from' after subject");
        return;
      }

      if (afterSubject.startsWith("from ") && !afterSubject.contains(" on ")) {
        executeCreateTimedCommand(line);
      } else if (afterSubject.startsWith("on ") && !afterSubject.contains(" from ")) {
        executeCreateAllDayCommand(line);
      } else {
        view.displayError("Expected 'on' or 'from' after subject");
      }
    } catch (IllegalArgumentException e) {
      view.displayError(e.getMessage());
    } catch (Exception e) {
      view.displayError("Error parsing create command: " + e.getMessage());
    }
  }

  private void executeCreateTimedCommand(String line) {
    String afterSubject = getAfterSubject(line).trim();


    if (!afterSubject.startsWith("from ")) {
      view.displayError("Expected 'from' after subject");
      return;
    }

    String afterFrom = afterSubject.substring(5).trim();

    if (afterFrom.isEmpty()) {
      view.displayError("Expected start time after 'from'");
      return;
    }

    String[] parts = afterFrom.split(" to ", 2);
    if (parts.length < 2 || parts[1].trim().isEmpty()) {
      view.displayError("Expected 'to' in command");
      return;
    }

    String startStr = parts[0].trim();
    String remaining = parts[1].trim();
    String subject = extractSubject(line);

    if (remaining.contains(" repeats ")) {
      createRecurringTimedEvent(subject, startStr, remaining);
    } else {
      createSingleTimedEvent(subject, startStr, remaining);
    }
  }

  private void createSingleTimedEvent(String subject, String startStr, String endStr) {
    LocalDateTime start = LocalDateTime.parse(startStr);
    LocalDateTime end = LocalDateTime.parse(endStr);
    CalendarCommand cmd = new AddEventCommand(subject, start, end, "");
    view.displayMessage(cmd.execute(system.getCurrentCalendar()));
  }

  private void createRecurringTimedEvent(String subject, String startStr, String remaining) {
    String[] timeParts = remaining.split(" repeats ");
    String endStr = timeParts[0].trim();
    String repeatClause = timeParts[1].trim();

    LocalDateTime start = LocalDateTime.parse(startStr);
    LocalDateTime end = LocalDateTime.parse(endStr);

    if (repeatClause.contains(" until ")) {
      String[] repeatParts = repeatClause.split(" until ");
      List<WeekDay> days = WeekDay.parseDays(repeatParts[0].trim().toUpperCase());
      LocalDate endDate = LocalDate.parse(repeatParts[1].trim());
      CalendarCommand cmd = new CreateSeriesUntilCommand(subject, start, end, days, endDate);
      view.displayMessage(cmd.execute(system.getCurrentCalendar()));
    } else if (repeatClause.contains(" for ")) {
      String[] repeatParts = repeatClause.split(" for ");
      List<WeekDay> days = WeekDay.parseDays(repeatParts[0].trim().toUpperCase());
      int times = Integer.parseInt(repeatParts[1].trim().replace("times", "").trim());
      CalendarCommand cmd = new CreateSeriesCommand(subject, start, end, days, times);
      view.displayMessage(cmd.execute(system.getCurrentCalendar()));
    } else {
      view.displayError("Invalid repeat clause");
    }
  }

  private void executeCreateAllDayCommand(String line) {
    String subject = extractSubject(line);
    String afterSubject = getAfterSubject(line).trim();


    if (!afterSubject.startsWith("on ")) {
      view.displayError("Expected 'on' after subject");
      return;
    }

    String afterOn = afterSubject.substring(3).trim();

    if (afterOn.isEmpty()) {
      view.displayError("Expected date after 'on'");
      return;
    }

    if (afterOn.contains(" repeats ")) {
      createRecurringAllDayEvent(subject, afterOn);
    } else {
      createSingleAllDayEvent(subject, afterOn);
    }
  }

  private void createSingleAllDayEvent(String subject, String dateStr) {
    LocalDate date = LocalDate.parse(dateStr);
    LocalDateTime dateTime = date.atStartOfDay();
    CalendarCommand cmd = new CreateAllDayEventCommand(subject, dateTime);
    view.displayMessage(cmd.execute(system.getCurrentCalendar()));
  }

  private void createRecurringAllDayEvent(String subject, String afterOn) {
    String[] parts = afterOn.split(" repeats ");
    if (parts.length != 2) {
      view.displayError("Invalid repeats clause");
      return;
    }
    String dateStr = parts[0].trim();
    String repeatClause = parts[1].trim();

    LocalDate date = LocalDate.parse(dateStr);
    LocalDateTime dateTime = date.atStartOfDay();

    if (repeatClause.contains(" until ")) {
      String[] repeatParts = repeatClause.split(" until ");
      List<WeekDay> days = WeekDay.parseDays(repeatParts[0].trim().toUpperCase());
      LocalDate endDate = LocalDate.parse(repeatParts[1].trim());
      CalendarCommand cmd = new CreateAllDaySeriesCommand(subject, dateTime, days, endDate);
      view.displayMessage(cmd.execute(system.getCurrentCalendar()));
    } else if (repeatClause.contains(" for ")) {
      String[] repeatParts = repeatClause.split(" for ");
      List<WeekDay> days = WeekDay.parseDays(repeatParts[0].trim().toUpperCase());
      int times = Integer.parseInt(repeatParts[1].trim().replace("times", "").trim());
      CalendarCommand cmd = new CreateAllDaySeriesCommand(subject, dateTime, days, times);
      view.displayMessage(cmd.execute(system.getCurrentCalendar()));
    } else {
      view.displayError("Invalid repeat clause");
    }
  }

  private void executeEditCommand(String line, String editType) {
    try {
      String property = extractEditProperty(line);
      String commandPrefix = buildCommandPrefix(line, property);
      String rest = line.substring(commandPrefix.length());

      EditCommandParts parts = parseEditCommandParts(rest);
      String newValue = extractNewValue(parts.afterWith);

      CalendarCommand cmd = createEditCommand(editType, property, parts, newValue);
      view.displayMessage(cmd.execute(system.getCurrentCalendar()));
    } catch (IllegalArgumentException e) {
      view.displayError(e.getMessage());
    } catch (Exception e) {
      view.displayError("Error parsing edit command: " + e.getMessage());
    }
  }

  /**
   * Extracts the property being edited from the command.
   */
  private String extractEditProperty(String line) {
    String[] tokens = line.split(" ", 4);
    if (tokens.length < 4) {
      throw new IllegalArgumentException("Invalid edit command format");
    }
    return tokens[2];
  }

  /**
   * Builds the command prefix (e.g., "edit event subject ").
   */
  private String buildCommandPrefix(String line, String property) {
    String[] tokens = line.split(" ", 4);
    return tokens[0] + " " + tokens[1] + " " + tokens[2] + " ";
  }

  /**
   * Parses the main parts of an edit command after the command prefix.
   */
  private EditCommandParts parseEditCommandParts(String rest) {
    String subject = extractSubjectFromEditCommand(rest);
    String afterSubject = getAfterSubjectInEditCommand(rest, subject);

    if (!afterSubject.startsWith("from")) {
      throw new IllegalArgumentException("Expected 'from' after subject");
    }

    String afterFrom = afterSubject.substring(4).trim();
    String[] withParts = afterFrom.split(" with ", 2);

    if (withParts.length != 2) {
      throw new IllegalArgumentException("Expected 'with' in command");
    }

    return new EditCommandParts(subject, withParts[0].trim(), withParts[1].trim());
  }

  /**
   * Extracts subject from edit command, handling quoted and unquoted cases.
   */
  private String extractSubjectFromEditCommand(String rest) {
    if (rest.startsWith("\"")) {
      int firstQuote = rest.indexOf('"');
      int secondQuote = rest.indexOf('"', firstQuote + 1);
      if (firstQuote == -1 || secondQuote == -1) {
        throw new IllegalArgumentException("Mismatched quotes in subject");
      }
      return rest.substring(firstQuote + 1, secondQuote);
    } else {
      int fromIndex = rest.indexOf(" from ");
      if (fromIndex == -1) {
        throw new IllegalArgumentException("Expected 'from' after subject");
      }
      return rest.substring(0, fromIndex).trim();
    }
  }

  /**
   * Gets the portion after the subject in edit command.
   */
  private String getAfterSubjectInEditCommand(String rest, String subject) {
    if (rest.startsWith("\"")) {
      int firstQuote = rest.indexOf('"');
      int secondQuote = rest.indexOf('"', firstQuote + 1);
      return rest.substring(secondQuote + 1).trim();
    } else {
      int fromIndex = rest.indexOf(" from ");
      return rest.substring(fromIndex).trim();
    }
  }

  /**
   * Extracts and cleans the new value, removing quotes if present.
   */
  private String extractNewValue(String afterWith) {
    String newValue = afterWith.trim();
    if (newValue.startsWith("\"") && newValue.endsWith("\"")) {
      return newValue.substring(1, newValue.length() - 1);
    }
    return newValue;
  }

  /**
   * Creates the appropriate edit command based on edit type.
   */
  private CalendarCommand createEditCommand(String editType, String property,
                                            EditCommandParts parts, String newValue) {
    String timePart = parts.timePart;
    String subject = parts.subject;

    if (editType.equals("event")) {
      validateEventTimeFormat(timePart);
      String[] timeParts = timePart.split(" to ", 2);
      LocalDateTime start = LocalDateTime.parse(timeParts[0].trim());
      LocalDateTime end = LocalDateTime.parse(timeParts[1].trim());
      return new EditEventCommand(property, subject, start, end, newValue);
    } else if (editType.equals("events")) {
      validateEventsTimeFormat(timePart);
      LocalDateTime start = LocalDateTime.parse(timePart);
      return new EditEventsCommand(property, subject, start, newValue);
    } else {
      validateSeriesTimeFormat(timePart);
      LocalDateTime start = LocalDateTime.parse(timePart);
      return new EditSeriesCommand(property, subject, start, newValue);
    }
  }

  /**
   * Validates that event edit has 'to' in time format.
   */
  private void validateEventTimeFormat(String timePart) {
    if (!timePart.contains(" to ")) {
      throw new IllegalArgumentException(
          "Expected 'to' in edit event command. Format: edit event <property> "
              + "<subject> from <start> to <end> with <value>");
    }
  }

  /**
   * Validates that events edit does not have 'to' in time format.
   */
  private void validateEventsTimeFormat(String timePart) {
    if (timePart.contains(" to ")) {
      throw new IllegalArgumentException(
          "Unexpected 'to' in edit events command. Format: edit events <property> "
              + "<subject> from <start> with <value>");
    }
  }

  /**
   * Validates that series edit does not have 'to' in time format.
   */
  private void validateSeriesTimeFormat(String timePart) {
    if (timePart.contains(" to ")) {
      throw new IllegalArgumentException(
          "Unexpected 'to' in edit series command. Format: edit series <property> "
              + "<subject> from <start> with <value>");
    }
  }

  /**
   * Helper class to hold parsed edit command parts.
   */
  private static class EditCommandParts {
    final String subject;
    final String timePart;
    final String afterWith;

    EditCommandParts(String subject, String timePart, String afterWith) {
      this.subject = subject;
      this.timePart = timePart;
      this.afterWith = afterWith;
    }
  }

  private void executeListAllEvents() {
    try {
      CalendarCommand cmd = new ListEventsCommand();
      view.displayMessage(cmd.execute(system.getCurrentCalendar()));
    } catch (Exception e) {
      view.displayError("Error listing all events: " + e.getMessage());
    }
  }


  private void executePrintOnDateCommand(String line) {
    try {
      String[] parts = line.split(" on ");
      if (parts.length != 2) {
        view.displayError("Expected 'on' in command");
        return;
      }
      LocalDate date = LocalDate.parse(parts[1].trim());
      CalendarCommand cmd = new PrintEventsOnDateCommand(date);
      view.displayMessage(cmd.execute(system.getCurrentCalendar()));
    } catch (Exception e) {
      view.displayError("Error parsing print on date command: " + e.getMessage());
    }
  }

  private void executePrintRangeCommand(String line) {
    try {
      String afterFrom = line.substring(line.indexOf("from") + 4).trim();
      String[] parts = afterFrom.split(" to ");
      if (parts.length != 2) {
        view.displayError("Expected 'to' in command");
        return;
      }
      LocalDateTime start = LocalDateTime.parse(parts[0].trim());
      LocalDateTime end = LocalDateTime.parse(parts[1].trim());
      CalendarCommand cmd = new PrintEventsCommand(start, end);
      view.displayMessage(cmd.execute(system.getCurrentCalendar()));
    } catch (Exception e) {
      view.displayError("Error parsing print range command: " + e.getMessage());
    }
  }


  private void executeShowStatus(String line) {
    try {
      String[] parts = line.split(" on ");
      if (parts.length != 2) {
        view.displayError("Expected 'on' in command");
        return;
      }
      LocalDateTime check = LocalDateTime.parse(parts[1].trim());
      CalendarCommand cmd = new ShowStatusCommand(check);
      view.displayMessage(cmd.execute(system.getCurrentCalendar()));
    } catch (Exception e) {
      view.displayError("Error parsing show status command: " + e.getMessage());
    }
  }


  private void executeShowBusy(String line) {
    view.displayMessage("busy");
  }


  private void executeExportCommand(String line) {
    try {
      String afterExport = line.substring(6).trim();


      if (afterExport.startsWith("cal ")) {
        String fileName = afterExport.substring(4).trim();
        CalendarCommand cmd = new ExportCommand(fileName);
        String result = cmd.execute(system.getCurrentCalendar());


        if (result.startsWith("Error")) {
          view.displayError(result);
        } else {
          view.displayMessage(result);
        }
      } else {
        view.displayError("Expected format: export cal <filename>");
      }
    } catch (Exception e) {
      view.displayError("Error parsing export command: " + e.getMessage());
    }
  }


  /**
   * Extracts subject from command - handles both quoted and unquoted subjects.
   * Quoted subjects can have multiple words, unquoted must be single word.
   */
  private String extractSubject(String line) {
    if (line.contains("\"")) {
      int firstQuote = line.indexOf('"');
      int secondQuote = line.indexOf('"', firstQuote + 1);
      if (firstQuote == -1 || secondQuote == -1) {
        throw new IllegalArgumentException("Mismatched quotes in subject");
      }
      return line.substring(firstQuote + 1, secondQuote);
    } else {
      String[] words = line.trim().split("\\s+");

      for (int i = 0; i < words.length; i++) {
        String word = words[i];
        if (!word.equals("create") && !word.equals("event") && !word.equals("edit")
            && !word.equals("series") && !word.equals("events") && !word.equals("on")
            && !word.equals("from") && !word.equals("to")) {
          return word;
        }
      }
      throw new IllegalArgumentException("No subject found");
    }
  }

  /**
   * Gets the part of command after the subject.
   */
  private String getAfterSubject(String line) {
    if (line.contains("\"")) {
      int firstQuote = line.indexOf('"');
      int secondQuote = line.indexOf('"', firstQuote + 1);
      return line.substring(secondQuote + 1).trim();
    } else {
      String subject = extractSubject(line);
      int subjectPos = line.indexOf(subject);
      if (subjectPos != -1) {
        return line.substring(subjectPos + subject.length()).trim();
      }
      return "";
    }
  }
}
