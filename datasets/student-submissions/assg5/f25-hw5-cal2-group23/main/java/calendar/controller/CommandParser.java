package calendar.controller;

import calendar.model.CalendarManager;
import calendar.model.Event;
import calendar.model.IcalendarModel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;

/**
 * Parses and executes commands for the calendar application.
 */
public class CommandParser {

  private IcalendarModel model;

  /**
   * Creates a CommandParser with the given calendar model.
   *
   * @param model the calendar model to change
   */
  public CommandParser(IcalendarModel model) {

    this.model = model;
  }

  /**
   * Processes user input and executes the corresponding calendar command.
   *
   * @param command the command string
   * @return the return message
   */
  public String processCommand(String command) {
    String originalCommand = command.trim();
    String lowerCommand = command.trim().toLowerCase();

    if (lowerCommand.equals(" ") || lowerCommand.isEmpty()) {
      return null;
    }

    try {
      if (lowerCommand.startsWith("create calendar")) {
        return handleCreateCalendar(originalCommand);
      } else if (lowerCommand.startsWith("edit calendar")) {
        return handleEditCalendar(originalCommand);
      } else if (lowerCommand.startsWith("use calendar")) {
        return handleUseCalendar(originalCommand);
      } else if (lowerCommand.startsWith("delete calendar")) {
        return handleDeleteCalendar(originalCommand);
      } else if (lowerCommand.startsWith("rename calendar")) {
        return handleRenameCalendar(originalCommand);
      } else if (lowerCommand.startsWith("list calendars")) {
        return handleListCalendars();
      }

      if (model.getActiveCalendarName() == null) {
        return "Error: No calendar set. Use 'use calendar --name <name>' first.";
      }

      if (lowerCommand.startsWith("create event")) {
        return createEvent(originalCommand);
      } else if (lowerCommand.startsWith("edit event")
          || lowerCommand.startsWith("edit events")
          || lowerCommand.startsWith("edit series")) {
        return editEvent(originalCommand);
      } else if (lowerCommand.startsWith("delete event ")) {
        return handleDeleteEvent(originalCommand);
      } else if (lowerCommand.startsWith("delete events ")) {
        return handleDeleteEventsFrom(originalCommand);
      } else if (lowerCommand.startsWith("delete series ")) {
        return handleDeleteSeries(originalCommand);
      } else if (lowerCommand.startsWith("copy event ")) {
        return handleCopyEvent(originalCommand);
      } else if (lowerCommand.startsWith("copy events on ")) {
        return handleCopyEventsOnDate(originalCommand);
      } else if (lowerCommand.startsWith("copy events between ")) {
        return handleCopyEventsInInterval(originalCommand);
      } else if (lowerCommand.startsWith("print events")
          || lowerCommand.startsWith("print schedule")) {
        return printEvent(originalCommand);
      } else if (lowerCommand.startsWith("export cal")) {
        return export(originalCommand);
      } else if (lowerCommand.startsWith("show status")) {
        return showStatus(originalCommand);
      } else {
        return "Error: Invalid command: " + originalCommand;
      }
    } catch (Exception e) {
      if (e instanceof java.time.format.DateTimeParseException) {
        return "Error executing: Invalid date/time format. Use YYYY-MM-DD or YYYY-MM-DDTHH:MM.";
      }
      return "Error executing: " + e.getMessage();
    }
  }

  /**
   * Processes user input and edits the corresponding calendar command.
   *
   * @param command the command string
   * @return the return message
   */
  public String createEvent(String command) {
    String subject = "";
    String description = "";

    if (command.charAt(12) == '"') {
      int endQuote = command.indexOf('"', 13);
      if (endQuote == -1) {
        return "Could not find closing quotes";
      }
      subject = command.substring(13, endQuote);
      description = command.substring(endQuote + 1).trim();
    } else {
      int fromIndex = command.toLowerCase().indexOf(" from ", 13);
      int onIndex = command.toLowerCase().indexOf(" on ", 13);

      int descriptionStart = -1;
      if (fromIndex != -1 && (onIndex == -1 || fromIndex < onIndex)) {
        descriptionStart = fromIndex;
      } else if (onIndex != -1) {
        descriptionStart = onIndex;
      }

      if (descriptionStart == -1) {
        subject = command.substring(13).trim();
        description = "";
      } else {
        subject = command.substring(13, descriptionStart).trim();
        description = command.substring(descriptionStart).trim();
      }
    }
    if (subject.isEmpty()) {
      return "Error: Event subject cannot be empty.";
    }
    if (description.toLowerCase().startsWith("from")) {
      return handleTimedEventCreation(subject, description);
    } else if (description.toLowerCase().startsWith("on")) {
      return handleAllDayEvent(subject, description);
    } else {
      return "Error: Invalid command" + command;
    }
  }

  private String handleTimedEventCreation(String subject, String command) {
    try {
      command = command.trim();
      if (!command.toLowerCase().startsWith("from") || !command.toLowerCase().contains("to")) {
        return "Error: Invalid format. Use: from <start> to <end> [repeats ...]";
      }
      int fromIndex = command.toLowerCase().indexOf("from") + 5;
      int toIndex = command.toLowerCase().indexOf("to");
      String startString = command.substring(fromIndex, toIndex).trim();

      int repeatsIndex = command.toLowerCase().indexOf("repeats");
      String endString;
      if (repeatsIndex == -1) {
        endString = command.substring(toIndex + 3).trim();
      } else {
        endString = command.substring(toIndex + 3, repeatsIndex).trim();
      }

      String startTime = startString;
      String endTime = endString;
      if (repeatsIndex == -1) {
        model.createTimedEvent(subject, startTime, endTime);
        return "Created single timed event: " + subject;
      }

      String repeatSection = command.substring(repeatsIndex + 8).trim();
      return handleRecurringTimedEvent(subject, startTime, endTime, repeatSection);

    } catch (Exception e) {
      return "Error creating timed event: " + e.getMessage();
    }

  }

  private String handleRecurringTimedEvent(String subject, String startTime,
                                           String endTime, String repeatSection) {
    try {
      String[] tokens = repeatSection.split("\\s+");
      String repeatDays = tokens[0];
      int occurences = -1;
      String untilDate = null;

      for (int i = 1; i < tokens.length; i++) {
        if (tokens[i].equalsIgnoreCase("for") && i + 1 < tokens.length) {
          occurences = Integer.parseInt(tokens[i + 1]);
        } else if (tokens[i].equalsIgnoreCase("until") && i + 1 < tokens.length) {
          untilDate = tokens[i + 1];
        }
      }
      if (occurences > 0) {
        model.createRecurringEvent(subject, startTime, endTime, repeatDays, occurences);
        return "Created recurring timed event (" + occurences + " times): " + subject;
      } else if (untilDate != null) {
        model.createRecurringEventUntil(subject, startTime, endTime, repeatDays,
            untilDate);
        return "Created recurring timed event (until " + untilDate + "): " + subject;
      } else {
        return "Error: Missing recurrence end condition ('for' or 'until').";
      }

    } catch (Exception e) {
      return "Error creating recurring event: " + e.getMessage();
    }

  }

  private String handleAllDayEvent(String subject, String command) {
    try {
      command = command.trim();
      if (!command.toLowerCase().startsWith("on")) {
        return "Error: Invalid format. Use: on <start> [repeats ...]";
      }
      int onIndex = command.toLowerCase().indexOf("on") + 3;
      int repeatsIndex = command.toLowerCase().indexOf("repeats");

      String dateString;
      if (repeatsIndex == -1) {
        dateString = command.substring(onIndex).trim();
      } else {
        dateString = command.substring(onIndex, repeatsIndex).trim();
      }
      String date = dateString;

      if (repeatsIndex == -1) {
        model.createAllDayEvent(subject, date);
        return "Created single all-day event: " + subject + " on " + date;
      }
      String repeatSection = command.substring(repeatsIndex + 8).trim();
      return handleRecurringAllDayEvent(subject, date, repeatSection);
    } catch (Exception e) {
      return "Error creating all-day event: " + e.getMessage();
    }
  }

  private String handleRecurringAllDayEvent(String subject, String startDate, String section) {
    try {
      String[] tokens = section.split("\\s+");
      String repeatDays = tokens[0];
      int occurrences = -1;
      String untilDate = null;

      for (int i = 1; i < tokens.length; i++) {
        if (tokens[i].equalsIgnoreCase("for") && i
            + 1 < tokens.length) {
          occurrences = Integer.parseInt(tokens[i + 1]);
        } else if (tokens[i].equalsIgnoreCase("until") && i + 1 < tokens.length) {
          untilDate = tokens[i + 1];
        }
      }

      if (occurrences > 0) {
        model.createAllDayRecurringEvent(subject, startDate, repeatDays, occurrences);
        return "Created recurring all-day event ("
            + occurrences + " times): " + subject;
      } else if (untilDate != null) {
        model.createAllDayRecurringEventUntil(subject, startDate, repeatDays,
            untilDate);
        return "Created recurring all-day event (until "
            + untilDate + "): " + subject;
      } else {
        return "Error: Missing recurrence end condition ('for' or 'until').";
      }

    } catch (Exception e) {
      return "Error creating recurring all-day event: " + e.getMessage();
    }
  }

  private String editEvent(String command) {
    try {
      String originalCommand = command.trim();
      command = command.trim().toLowerCase();

      if (command.startsWith("edit event ")) {
        return handleEditSingleEvent(originalCommand.substring(11).trim());
      } else if (command.startsWith("edit events ")) {
        return handleEditMultipleEvents(originalCommand.substring(12).trim());
      } else if (command.startsWith("edit series ")) {
        return handleEditSeries(originalCommand.substring(12).trim());
      } else {
        return "Error: Invalid edit command. Use 'edit event', "
            +
            "'edit events', or 'edit series'.";
      }

    } catch (Exception e) {
      return "Error handling edit command: " + e.getMessage();
    }
  }

  private String handleEditSingleEvent(String command) {
    try {
      String trimmed = command;

      int fromIndex = trimmed.indexOf(" from ");
      int withIndex = trimmed.indexOf(" with ");
      if (fromIndex == -1 || withIndex == -1) {
        return "Error: Invalid edit event format.";
      }

      String propertyAndSubject = trimmed.substring(0, fromIndex).trim();
      String startDateTime = trimmed.substring(fromIndex + 6, withIndex).trim();
      String newValue = trimmed.substring(withIndex + 6).trim();

      int firstSpace = propertyAndSubject.indexOf(' ');
      if (firstSpace == -1) {
        return "Error: Missing property or subject.";
      }
      String property = propertyAndSubject.substring(0, firstSpace).trim();
      String subject = propertyAndSubject.substring(firstSpace + 1)
          .trim();

      model.editEvent(subject, startDateTime, property, newValue);

      return "Edited event '" + subject + "' on " + startDateTime
          +
          ": set " + property + " to " + newValue;

    } catch (Exception e) {
      return "Error editing event: " + e.getMessage();
    }
  }

  private String handleEditMultipleEvents(String command) {
    try {
      String trimmed = command;

      int fromIndex = trimmed.toLowerCase().indexOf(" from ");
      int withIndex = trimmed.toLowerCase().indexOf(" with ");
      if (fromIndex == -1 || withIndex == -1) {
        return "Error: Invalid edit events format.";
      }

      String propertyAndSubject = trimmed.substring(0, fromIndex).trim();
      String startDateTime = trimmed.substring(fromIndex + 6, withIndex).trim();
      String newValue = trimmed.substring(withIndex + 6)
          .trim();

      int firstSpace = propertyAndSubject.indexOf(' ');
      if (firstSpace == -1) {
        return "Error: Missing property or subject.";
      }
      String property = propertyAndSubject.substring(0, firstSpace).trim();
      String subject = propertyAndSubject.substring(firstSpace + 1)
          .trim();

      model.editEventsFrom(subject, startDateTime, property, newValue);

      return "Edited all events from " + startDateTime
          +
          " in series of '" + subject + "': set " + property + " to "
          + newValue;

    } catch (Exception e) {
      return "Error editing events: " + e.getMessage();
    }
  }

  private String handleEditSeries(String command) {
    try {
      String trimmed = command;

      int fromIndex = trimmed.toLowerCase().indexOf(" from ");
      int withIndex = trimmed.toLowerCase().indexOf(" with ");
      if (fromIndex == -1 || withIndex == -1) {
        return "Error: Invalid edit series format.";
      }

      String propertyAndSubject = trimmed.substring(0, fromIndex).trim();
      String startDateTime = trimmed.substring(fromIndex + 6, withIndex)
          .trim();
      String newValue = trimmed.substring(withIndex + 6)
          .trim();

      int firstSpace = propertyAndSubject.indexOf(' ');
      if (firstSpace == -1) {
        return "Error: Missing property or subject.";
      }
      String property = propertyAndSubject.substring(0, firstSpace).trim();
      String subject = propertyAndSubject.substring(firstSpace + 1)
          .trim();

      model.editSeries(subject, startDateTime, property, newValue);

      return "Edited entire series for '" + subject
          +
          "' starting from " + startDateTime + ": set "
          + property
          + " to " + newValue;

    } catch (Exception e) {
      return "Error editing series: " + e.getMessage();
    }
  }

  private String printEvent(String command) {
    try {
      String lowerCommand = command.trim().toLowerCase();

      if (lowerCommand.startsWith("print events on")) {
        return handlePrintEventsOn(command.substring(16).trim());
      } else if (lowerCommand.startsWith("print schedule from")) {
        return handlePrintScheduleRange(command.substring(20).trim());
      } else {
        return "Error: Invalid print command.";
      }

    } catch (Exception e) {
      return "Error handling print command: " + e.getMessage();
    }
  }

  private String handlePrintEventsOn(String command) {
    try {
      String date = command;
      List<Event> events = model.getEventsOnDate(date);

      if (events.isEmpty()) {
        return "No events scheduled on " + date;
      } else {
        StringBuilder sb = new StringBuilder();
        sb.append("Events on ").append(date).append(":\n");
        for (Event event : events) {
          sb.append(" - ").append(event).append("\n");
        }
        return sb.toString();
      }

    } catch (Exception e) {
      return "Error printing events: " + e.getMessage();
    }
  }

  private String handlePrintScheduleRange(String command) {
    try {
      String[] parts = command.split("\\s+to\\s+");
      if (parts.length != 2) {
        return "Error: Invalid format. Use: print schedule from <date> to <date>";
      }

      String startDate = parts[0].trim();
      String endDate = parts[1].trim();

      List<Event> events = model.getEventsInInterval(startDate, endDate);

      if (events.isEmpty()) {
        return "No events between " + startDate + " and " + endDate;
      } else {
        StringBuilder sb = new StringBuilder();
        sb.append("Events from ").append(startDate).append(" to ")
            .append(endDate).append(":\n");
        for (Event event : events) {
          sb.append(" - ").append(event).append("\n");
        }
        return sb.toString();
      }

    } catch (Exception e) {
      return "Error printing schedule range: " + e.getMessage();
    }
  }

  private String export(String command) throws Exception {
    String[] tokens = command.split("\\s+");
    if (tokens.length != 3 || !tokens[1].equalsIgnoreCase("cal")) {
      return "Error: Invalid format. Use: export cal <filename>";
    }

    String fileName = tokens[2];
    String content;

    if (fileName.toLowerCase().endsWith(".ical")) {
      content = model.exportToIcal();
    } else if (fileName.toLowerCase().endsWith(".csv")) {
      content = model.exportToCsv();
    } else {
      return "Error: Unsupported export format. Use .csv or .ical extension.";
    }

    Path path = Paths.get(fileName);
    Files.writeString(path, content);

    return "Exported " + model.getActiveCalendarName()
        + " to " + fileName + " at "
        + path.toAbsolutePath();
  }

  private String showStatus(String command) {
    try {
      String[] tokens = command.split("\\s+");
      if (tokens.length != 4 || !tokens[1].equalsIgnoreCase("status")
          ||
          !tokens[2].equalsIgnoreCase("on")) {
        return "Error: Invalid format.";
      }
      String dateTimeString = tokens[3];
      boolean isbusy = model.isBusyAt(dateTimeString);
      if (isbusy) {
        return "Busy at " + dateTimeString;
      } else {
        return "Available " + dateTimeString;
      }
    } catch (Exception e) {
      return "Error checking status: " + e.getMessage();
    }
  }

  private String handleCreateCalendar(String command) throws Exception {
    String[] tokens = command.split("\\s+");
    if (tokens.length != 6 || !tokens[2].equals("--name") || !tokens[4].equals("--timezone")) {
      return "Error: Invalid format. Use: create calendar --name <name> --timezone <area/location>";
    }
    String calName = tokens[3];
    String timezone = tokens[5];
    model.createCalendar(calName, timezone);
    return "Created calendar '" + calName + "' with timezone " + timezone + ".";
  }

  private String handleEditCalendar(String command) throws Exception {
    String[] tokens = command.split("\\s+");
    if (tokens.length != 7 || !tokens[2].equals("--name") || !tokens[4].equals("--property")
        || !tokens[5].equalsIgnoreCase("timezone")) {
      return "Error: Invalid format. Use: edit calendar --name <name> --property timezone "
          + "<area/location>";
    }
    String calName = tokens[3];
    String property = tokens[5];
    String newValue = tokens[6];
    model.updateCalendarTimeZone(calName, newValue);
    return "Edited calendar '" + calName + "': set " + property + " to " + newValue + ".";
  }

  private String handleUseCalendar(String command) throws Exception {
    String[] tokens = command.split("\\s+");
    if (tokens.length != 4 || !tokens[2].equals("--name")) {
      return "Error: Invalid format. Use: use calendar --name <name>";
    }
    String calName = tokens[3];
    model.setActiveCalendar(calName);
    return "Context set to calendar: " + calName;
  }

  private String handleDeleteEvent(String command) throws Exception {
    String trimmed = command.substring("delete event ".length()).trim();
    int onIndex = trimmed.toLowerCase().indexOf(" on ");

    if (onIndex == -1) {
      return "Error: Invalid format. Use: delete event <subject> on <YYYY-MM-DDTHH:MM>";
    }

    String subject = trimmed.substring(0, onIndex).replaceAll("^\"|\"$", "")
        .trim();
    String startDateTime = trimmed.substring(onIndex + 4).trim();

    model.deleteEvent(subject, startDateTime);
    return "Deleted single event instance: '" + subject + "' on " + startDateTime;
  }

  private String handleDeleteEventsFrom(String command) throws Exception {
    String trimmed = command.substring("delete events ".length()).trim();
    int fromIndex = trimmed.toLowerCase().indexOf(" from ");

    if (fromIndex == -1) {
      return "Error: Invalid format. Use: delete events <subject> from <YYYY-MM-DDTHH:MM>";
    }

    String subject = trimmed.substring(0, fromIndex).replaceAll("^\"|\"$", "")
        .trim();
    String startDateTime = trimmed.substring(fromIndex + 6).trim();

    model.deleteEventsFrom(subject, startDateTime);
    return "Deleted all future events for series '" + subject + "' starting from " + startDateTime;
  }

  private String handleDeleteSeries(String command) throws Exception {
    String subject = command.substring("delete series ".length()).trim()
        .replaceAll("^\"|\"$", "");

    if (subject.isEmpty()) {
      return "Error: Invalid format. Use: delete series <subject>";
    }

    model.deleteSeries(subject);
    return "Deleted entire event series with subject: '" + subject + "'";
  }

  private String handleCopyEvent(String command) throws Exception {
    String trimmed = command.substring("copy event ".length()).trim();

    int fromIndex = trimmed.toLowerCase().indexOf(" from ");
    int targetIndex = trimmed.toLowerCase().indexOf(" --target ");
    int toIndex = trimmed.toLowerCase().indexOf(" to ");

    if (fromIndex == -1 || targetIndex == -1 || toIndex == -1 || fromIndex > targetIndex
        || targetIndex > toIndex) {
      return "Error: Invalid copy event format. Use: copy event <subject> from <sourceDT> "
          + "--target <targetCalName> to <targetDT>";
    }

    String subject = trimmed.substring(0, fromIndex).replaceAll("^\"|\"$", "")
        .trim();
    String sourceStartDateTime = trimmed.substring(fromIndex + 6, targetIndex).trim();
    String targetCalName = trimmed.substring(targetIndex + 10, toIndex).trim();
    String targetStartDateTime = trimmed.substring(toIndex + 4).trim();

    model.copyEvent(subject, sourceStartDateTime, targetCalName, targetStartDateTime);

    return "Copied single event instance '" + subject + "' to calendar '" + targetCalName + "' on "
        + targetStartDateTime;
  }

  private String handleCopyEventsOnDate(String command) throws Exception {
    String trimmed = command.substring("copy events on ".length()).trim();

    int targetIndex = trimmed.toLowerCase().indexOf(" --target ");
    int toIndex = trimmed.toLowerCase().indexOf(" to ");

    if (targetIndex == -1 || toIndex == -1 || targetIndex > toIndex) {
      return "Error: Invalid copy events on format. Use: copy events on <sourceDate> --target"
          + " <targetCalName> to <targetDate>";
    }

    String sourceDateStr = trimmed.substring(0, targetIndex).trim();
    String targetCalName = trimmed.substring(targetIndex + 10, toIndex).trim();
    String targetDateStr = trimmed.substring(toIndex + 4).trim();

    model.copyEventsOnDate(sourceDateStr, targetCalName, targetDateStr);

    return "Copied all events from " + sourceDateStr + " to calendar '" + targetCalName + "' on "
        + targetDateStr;
  }

  private String handleCopyEventsInInterval(String command) throws Exception {
    String trimmed = command.substring("copy events between ".length()).trim();

    int andIndex = trimmed.toLowerCase().indexOf(" and ");
    int targetIndex = trimmed.toLowerCase().indexOf(" --target ");
    int toIndex = trimmed.toLowerCase().indexOf(" to ");

    if (andIndex == -1 || targetIndex == -1 || toIndex == -1 || andIndex > targetIndex
        || targetIndex > toIndex) {
      return "Error: Invalid copy events between format. Use: copy events between <startDT> "
          + "and <endDT> --target <targetCalName> to <targetDT>";
    }

    String sourceStartStr = trimmed.substring(0, andIndex).trim();
    String sourceEndStr = trimmed.substring(andIndex + 5, targetIndex).trim();
    String targetCalName = trimmed.substring(targetIndex + 10, toIndex).trim();
    String targetDateStr = trimmed.substring(toIndex + 4).trim();

    model.copyEventsInInterval(sourceStartStr, sourceEndStr, targetCalName, targetDateStr);

    return "Copied events between " + sourceStartStr + " and " + sourceEndStr + " to calendar '"
        + targetCalName + "' starting on " + targetDateStr;
  }

  private String handleDeleteCalendar(String command) throws Exception {
    String[] tokens = command.split("\\s+");
    if (tokens.length != 4 || !tokens[2].equals("--name")) {
      return "Error: Invalid format. Use: delete calendar --name <name>";
    }
    String calName = tokens[3];
    model.deleteCalendar(calName);
    return "Deleted calendar '" + calName + "'.";
  }

  private String handleRenameCalendar(String command) throws Exception {
    String[] tokens = command.split("\\s+");
    if (tokens.length != 6 || !tokens[2].equals("--name") || !tokens[4].equals("--to")) {
      return "Error: Invalid format. Use: rename calendar --name <oldName> --to <newName>";
    }
    String oldName = tokens[3];
    String newName = tokens[5];
    model.renameCalendar(oldName, newName);
    return "Renamed calendar '" + oldName + "' to '" + newName + "'.";
  }

  private String handleListCalendars() throws Exception {
    List<String> calendars = model.getAllCalendarNames();
    if (calendars.isEmpty()) {
      return "No calendars found.";
    }
    StringBuilder sb = new StringBuilder();
    sb.append("Available calendars:\n");
    String activeCal = model.getActiveCalendarName();
    for (String cal : calendars) {
      sb.append(" - ").append(cal);
      if (cal.equals(activeCal)) {
        sb.append(" (active)");
      }
      sb.append("\n");
    }
    return sb.toString().trim();
  }
}
