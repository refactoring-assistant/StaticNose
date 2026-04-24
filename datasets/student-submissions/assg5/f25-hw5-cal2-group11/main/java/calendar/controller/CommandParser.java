package calendar.controller;

import calendar.model.CalendarModelImpl;
import calendar.model.Event;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Parses and executes commands for the calendar application.
 */
public class CommandParser {

  private CalendarModelImpl model;

  /**
   * Creates a new CommandParser.
   *
   * @param model the calendar model
   */
  public CommandParser(CalendarModelImpl model) {
    this.model = model;
  }

  /**
   * Parses and executes a command.
   *
   * @param input the command string
   */
  public void parseAndExecute(String input) {
    String trimmed = input.trim();

    if (trimmed.isEmpty()) {
      return;
    }

    if (trimmed.equalsIgnoreCase("exit")) {
      return;
    }
    if (trimmed.startsWith("create calendar")) {
      parseCreateCalendar(trimmed);
    } else if (trimmed.startsWith("edit calendar")) {
      parseEditCalendar(trimmed);
    } else if (trimmed.startsWith("use calendar")) {
      parseUseCalendar(trimmed);
    } else if (trimmed.startsWith("copy event ") && trimmed.contains(" on ")) {
      parseCopyEvent(trimmed);
    } else if (trimmed.startsWith("copy events on ")) {
      parseCopyEventsOnDate(trimmed);
    } else if (trimmed.startsWith("copy events between ")) {
      parseCopyEventsBetween(trimmed);
    } else if (trimmed.startsWith("create event")) {
      parseCreateEvent(trimmed);
    } else if (trimmed.startsWith("edit event ")) {
      parseEditSingleEvent(trimmed);
    } else if (trimmed.startsWith("edit events ")) {
      parseEditSeriesFromEvent(trimmed);
    } else if (trimmed.startsWith("edit series ")) {
      parseEditEntireSeries(trimmed);
    } else if (trimmed.startsWith("print events on")) {
      parsePrintEventsOnDate(trimmed);
    } else if (trimmed.startsWith("print events from")) {
      parsePrintEventsInRange(trimmed);
    } else if (trimmed.startsWith("show status on")) {
      parseShowStatus(trimmed);
    } else if (trimmed.startsWith("export cal")) {
      parseExport(trimmed);
    } else {
      throw new IllegalArgumentException("Unknown command: " + trimmed);
    }
  }

  private void parseCreateCalendar(String trimmed) {
    String remainder = trimmed.substring("create calendar".length()).trim();

    if (!remainder.startsWith("--name ")) {
      throw new IllegalArgumentException("Missing --name parameter");
    }

    remainder = remainder.substring("--name ".length()).trim();
    int timezoneIndex = remainder.indexOf(" --timezone ");

    if (timezoneIndex == -1) {
      throw new IllegalArgumentException("Missing --timezone parameter");
    }

    String calendarName = remainder.substring(0, timezoneIndex).trim();
    String timezoneStr = remainder.substring(timezoneIndex + " --timezone ".length()).trim();

    try {
      ZoneId timezone = ZoneId.of(timezoneStr);
      model.createCalendar(calendarName, timezone);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid timezone: " + timezoneStr);
    }
  }

  private void parseEditCalendar(String trimmed) {
    String remainder = trimmed.substring("edit calendar".length()).trim();

    if (!remainder.startsWith("--name ")) {
      throw new IllegalArgumentException("Missing --name parameter");
    }

    remainder = remainder.substring("--name ".length()).trim();
    int propertyIndex = remainder.indexOf(" --property ");

    if (propertyIndex == -1) {
      throw new IllegalArgumentException("Missing --property parameter");
    }

    String calendarName = remainder.substring(0, propertyIndex).trim();
    remainder = remainder.substring(propertyIndex + " --property ".length()).trim();

    int spaceIndex = remainder.indexOf(' ');
    if (spaceIndex == -1) {
      throw new IllegalArgumentException("Missing property value");
    }

    String property = remainder.substring(0, spaceIndex).trim();
    String value = remainder.substring(spaceIndex + 1).trim();

    model.editCalendar(calendarName, property, value);
  }

  private void parseUseCalendar(String trimmed) {
    String remainder = trimmed.substring("use calendar".length()).trim();

    if (!remainder.startsWith("--name ")) {
      throw new IllegalArgumentException("Missing --name parameter");
    }

    String calendarName = remainder.substring("--name ".length()).trim();
    model.useCalendar(calendarName);
  }

  private void parseCopyEvent(String trimmed) {
    String remainder = trimmed.substring("copy event".length()).trim();

    int onIndex = remainder.indexOf(" on ");
    if (onIndex == -1) {
      throw new IllegalArgumentException("Missing 'on' in copy event command");
    }

    String eventName = remainder.substring(0, onIndex).trim();
    eventName = removeQuotes(eventName);

    String afterOn = remainder.substring(onIndex + 4).trim();
    int targetIndex = afterOn.indexOf(" --target ");

    if (targetIndex == -1) {
      throw new IllegalArgumentException("Missing --target parameter");
    }

    String sourceDateTimeStr = afterOn.substring(0, targetIndex).trim();
    String afterTarget = afterOn.substring(targetIndex + 10).trim();

    int toIndex = afterTarget.indexOf(" to ");
    if (toIndex == -1) {
      throw new IllegalArgumentException("Missing 'to' in copy event command");
    }

    String targetCalendarName = afterTarget.substring(0, toIndex).trim();
    String targetDateTimeStr = afterTarget.substring(toIndex + 4).trim();

    LocalDateTime sourceDateTime = parseDateTime(sourceDateTimeStr);
    LocalDateTime targetDateTime = parseDateTime(targetDateTimeStr);

    model.copyEvent(eventName, sourceDateTime, targetCalendarName, targetDateTime);
  }

  private void parseCopyEventsOnDate(String trimmed) {
    String remainder = trimmed.substring("copy events on".length()).trim();

    int targetIndex = remainder.indexOf(" --target ");
    if (targetIndex == -1) {
      throw new IllegalArgumentException("Missing --target parameter");
    }

    String sourceDateStr = remainder.substring(0, targetIndex).trim();
    String afterTarget = remainder.substring(targetIndex + 10).trim();

    int toIndex = afterTarget.indexOf(" to ");
    if (toIndex == -1) {
      throw new IllegalArgumentException("Missing 'to' in copy events command");
    }

    String targetCalendarName = afterTarget.substring(0, toIndex).trim();
    String targetDateStr = afterTarget.substring(toIndex + 4).trim();

    LocalDate sourceDate = parseDate(sourceDateStr);
    LocalDate targetDate = parseDate(targetDateStr);

    model.copyEventsOnDate(sourceDate, targetCalendarName, targetDate);
  }

  private void parseCopyEventsBetween(String trimmed) {
    String remainder = trimmed.substring("copy events between".length()).trim();

    int andIndex = remainder.indexOf(" and ");
    if (andIndex == -1) {
      throw new IllegalArgumentException("Missing 'and' in copy events command");
    }

    String startDateStr = remainder.substring(0, andIndex).trim();
    String afterAnd = remainder.substring(andIndex + 5).trim();

    int targetIndex = afterAnd.indexOf(" --target ");
    if (targetIndex == -1) {
      throw new IllegalArgumentException("Missing --target parameter");
    }

    String endDateStr = afterAnd.substring(0, targetIndex).trim();
    String afterTarget = afterAnd.substring(targetIndex + 10).trim();

    int toIndex = afterTarget.indexOf(" to ");
    if (toIndex == -1) {
      throw new IllegalArgumentException("Missing 'to' in copy events command");
    }

    String targetCalendarName = afterTarget.substring(0, toIndex).trim();
    String targetDateStr = afterTarget.substring(toIndex + 4).trim();

    LocalDate startDate = parseDate(startDateStr);
    LocalDate endDate = parseDate(endDateStr);
    LocalDate targetDate = parseDate(targetDateStr);

    model.copyEventsBetweenDates(startDate, endDate, targetCalendarName, targetDate);
  }

  /**
   * Parses create event commands.
   */
  private void parseCreateEvent(String trimmed) {
    String remainder = trimmed.substring("create event".length()).trim();

    if (remainder.contains(" repeats ")) {
      parseCreateRepeatingEvent(remainder);
    } else if (remainder.contains(" on ")) {
      parseCreateAllDayEvent(remainder);
    } else if (remainder.contains(" from ") && remainder.contains(" to ")) {
      parseCreateSingleEvent(remainder);
    } else {
      throw new IllegalArgumentException("Invalid create event syntax");
    }
  }

  /**
   * Parse single event with optional location and description.
   */
  private void parseCreateSingleEvent(String remainder) {
    int fromIndex = remainder.indexOf(" from ");
    String subject = remainder.substring(0, fromIndex).trim();
    subject = removeQuotes(subject);

    String afterFrom = remainder.substring(fromIndex + 6).trim();
    int toIndex = afterFrom.indexOf(" to ");
    String startStr = afterFrom.substring(0, toIndex).trim();
    String remainingAfterTo = afterFrom.substring(toIndex + 4).trim();

    String endStr;
    int atIndex = remainingAfterTo.indexOf(" at ");
    int descIndex = remainingAfterTo.indexOf(" description ");

    if (atIndex != -1 && (descIndex == -1 || atIndex < descIndex)) {
      endStr = remainingAfterTo.substring(0, atIndex).trim();
    } else if (descIndex != -1) {
      endStr = remainingAfterTo.substring(0, descIndex).trim();
    } else {
      endStr = remainingAfterTo.trim();
    }

    LocalDateTime startDateTime = parseDateTime(startStr);
    LocalDateTime endDateTime = parseDateTime(endStr);

    model.createSingleEvent(subject, startDateTime, endDateTime);

    setOptionalProperties(subject, startDateTime, remainingAfterTo);
  }

  /**
   * Parse all-day event with optional location and description.
   */
  private void parseCreateAllDayEvent(String remainder) {
    int onIndex = remainder.indexOf(" on ");
    String subject = remainder.substring(0, onIndex).trim();
    subject = removeQuotes(subject);

    String afterOn = remainder.substring(onIndex + 4).trim();

    String dateStr;
    int atIndex = afterOn.indexOf(" at ");
    int descIndex = afterOn.indexOf(" description ");

    if (atIndex != -1 && (descIndex == -1 || atIndex < descIndex)) {
      dateStr = afterOn.substring(0, atIndex).trim();
    } else if (descIndex != -1) {
      dateStr = afterOn.substring(0, descIndex).trim();
    } else {
      dateStr = afterOn.trim();
    }

    LocalDate date = parseDate(dateStr);
    LocalDateTime startDateTime = date.atStartOfDay();

    model.createSingleEvent(subject, startDateTime, null);

    setOptionalProperties(subject, startDateTime, afterOn);
  }

  /**
   * Parse repeating events with various patterns.
   * Supports both timed and all-day repeating events.
   */
  private void parseCreateRepeatingEvent(String remainder) {
    int repeatsIndex = remainder.indexOf(" repeats ");

    String baseEventStr = remainder.substring(0, repeatsIndex);

    String subject;
    LocalDateTime startDateTime;
    LocalDateTime endDateTime;

    if (baseEventStr.contains(" on ")) {

      int onIndex = baseEventStr.indexOf(" on ");
      subject = baseEventStr.substring(0, onIndex).trim();
      subject = removeQuotes(subject);

      String dateStr = baseEventStr.substring(onIndex + 4).trim();
      LocalDate date = parseDate(dateStr);

      startDateTime = date.atTime(8, 0);
      endDateTime = date.atTime(17, 0);
    } else if (baseEventStr.contains(" from ") && baseEventStr.contains(" to ")) {

      int fromIndex = baseEventStr.indexOf(" from ");
      subject = baseEventStr.substring(0, fromIndex).trim();
      subject = removeQuotes(subject);

      String timeRange = baseEventStr.substring(fromIndex + 6).trim();
      int toIndex = timeRange.indexOf(" to ");

      String startStr = timeRange.substring(0, toIndex).trim();
      String endStr = timeRange.substring(toIndex + 4).trim();

      startDateTime = parseDateTime(startStr);
      endDateTime = parseDateTime(endStr);
    } else {
      throw new IllegalArgumentException(
          "Event series must have either 'from...to' or 'on' syntax");
    }

    String repetitionStr = remainder.substring(repeatsIndex + 9).trim();

    if (repetitionStr.contains(" for ") && repetitionStr.contains(" times")) {

      int forIndex = repetitionStr.indexOf(" for ");
      int timesIndex = repetitionStr.indexOf(" times");

      String weekdaysStr = repetitionStr.substring(0, forIndex).trim();
      String countStr = repetitionStr.substring(forIndex + 5, timesIndex).trim();

      int count = Integer.parseInt(countStr);

      model.createEventSeries(subject, startDateTime, endDateTime, weekdaysStr, count);
    } else if (repetitionStr.contains(" until ")) {

      int untilIndex = repetitionStr.indexOf(" until ");

      String weekdaysStr = repetitionStr.substring(0, untilIndex).trim();
      String endDateStr = repetitionStr.substring(untilIndex + 7).trim();

      LocalDate endDate = parseDate(endDateStr);

      LocalDate startDate = startDateTime.toLocalDate();
      long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
      int approximateOccurrences = Math.max(1,
          (int) (daysBetween / 7 * weekdaysStr.length() + weekdaysStr.length()));

      model.createEventSeries(subject, startDateTime, endDateTime,
          weekdaysStr, approximateOccurrences);
    } else {
      throw new IllegalArgumentException(
          "Invalid repetition syntax. Use: repeats MWF for 10 times or "
              + "repeats MWF until YYYY-MM-DD");
    }
  }

  /**
   * Method to parse a single edit event.
   *
   * @param trimmed the trimmed string to be parsed.
   */
  private void parseEditSingleEvent(String trimmed) {
    String remainder = trimmed.substring("edit event".length()).trim();

    int fromIndex = remainder.indexOf(" from ");
    if (fromIndex == -1) {
      throw new IllegalArgumentException("missing 'from'");
    }

    String beforeFrom = remainder.substring(0, fromIndex).trim();
    String property;
    String subject;

    int firstQuote = beforeFrom.indexOf('"');
    if (firstQuote != -1) {
      property = beforeFrom.substring(0, firstQuote).trim();
      if (property.isEmpty()) {
        throw new IllegalArgumentException("missing property");
      }
      int closingQuote = beforeFrom.indexOf('"', firstQuote + 1);
      if (closingQuote == -1) {
        throw new IllegalArgumentException("invalid quoted subject");
      }
      subject = beforeFrom.substring(firstQuote + 1, closingQuote);
    } else {
      int firstSpace = beforeFrom.indexOf(' ');
      if (firstSpace == -1) {
        throw new IllegalArgumentException("missing property");
      }
      property = beforeFrom.substring(0, firstSpace).trim();
      subject = beforeFrom.substring(firstSpace + 1).trim();
    }

    String afterFrom = remainder.substring(fromIndex + 6).trim();

    int toIndex = afterFrom.indexOf(" to ");
    if (toIndex == -1) {
      throw new IllegalArgumentException("missing 'to'");
    }

    String startDateTimeStr = afterFrom.substring(0, toIndex).trim();
    String afterTo = afterFrom.substring(toIndex + 4).trim();

    int withIndex = afterTo.indexOf(" with ");
    if (withIndex == -1) {
      throw new IllegalArgumentException("missing 'with'");
    }

    String endDateTimeStr = afterTo.substring(0, withIndex).trim();
    String value = afterTo.substring(withIndex + 6).trim();
    value = removeQuotes(value);

    LocalDateTime startDateTime = parseDateTime(startDateTimeStr);
    LocalDateTime endDateTime = parseDateTime(endDateTimeStr);

    Event target = findEvent(subject, startDateTime, endDateTime);
    if (target == null) {
      throw new IllegalArgumentException("Event not found");
    }

    model.editEvent(target, property, value);
  }

  /**
   * Parses and handles edit commands that modify events in a series starting from a given date.
   *
   * @param trimmed the full command string to parse.
   */
  private void parseEditSeriesFromEvent(String trimmed) {
    parseEditCommandWithoutTo(trimmed, "edit events", EditType.SERIES_FROM);
  }

  /**
   * Parses and handles edit commands that modify all events in a series.
   *
   * @param trimmed the full command string to parse.
   */
  private void parseEditEntireSeries(String trimmed) {
    parseEditCommandWithoutTo(trimmed, "edit series", EditType.ENTIRE_SERIES);
  }

  /**
   * Parses and processes edit commands that do not include an end time.
   *
   * @param trimmed  the full command string to parse.
   * @param prefix   the command prefix.
   * @param editType the type of edit to perform.
   */
  private void parseEditCommandWithoutTo(String trimmed, String prefix, EditType editType) {
    String remainder = trimmed.substring(prefix.length()).trim();
    int fromIndex = remainder.indexOf(" from ");
    if (fromIndex == -1) {
      throw new IllegalArgumentException("missing 'from'");
    }

    String beforeFrom = remainder.substring(0, fromIndex).trim();
    int firstSpace = beforeFrom.indexOf(' ');
    if (firstSpace == -1) {
      throw new IllegalArgumentException("missing property");
    }


    String subject = beforeFrom.substring(firstSpace + 1).trim();
    subject = removeQuotes(subject);

    int withIndex = remainder.indexOf(" with ");
    if (withIndex == -1) {
      throw new IllegalArgumentException("missing 'with'");
    }

    String dateTimeStr = remainder.substring(fromIndex + 6, withIndex).trim();
    String value = remainder.substring(withIndex + 6).trim();
    value = removeQuotes(value);

    LocalDateTime startDateTime = parseDateTime(dateTimeStr);
    String property = beforeFrom.substring(0, firstSpace).trim();
    Event target = findEventBySubjectAndStart(subject, startDateTime);
    if (target == null) {
      throw new IllegalArgumentException("Event not found");
    }

    executeEdit(target, property, value, editType);
  }

  /**
   * Executes the edit operation on the specified event or event series based on the edit type.
   *
   * @param target   the event to be edited.
   * @param property the property of the event to be modified
   *                 (e.g., subject, start, end, description, location, status).
   * @param value    the new value to set for the specified property.
   * @param editType the type of edit to perform
   *                 (single event, series from this event, or entire series).
   */
  private void executeEdit(Event target, String property, String value, EditType editType) {
    switch (editType) {
      case SERIES_FROM:
        model.editSeriesFrom(target, property, value);
        break;
      case ENTIRE_SERIES:
        model.editEntireSeries(target, property, value);
        break;
      default:
        throw new IllegalArgumentException("Unknown edit type");
    }
  }

  /**
   * Enum to represent different types of edit operations.
   */
  private enum EditType {
    SINGLE,
    SERIES_FROM,
    ENTIRE_SERIES
  }

  /**
   * Parse: print events on YYYY-MM-DD.
   */
  private void parsePrintEventsOnDate(String trimmed) {
    String remainder = trimmed.substring("print events on".length()).trim();

    LocalDate date = parseDate(remainder);
    LocalDateTime dateTime = date.atStartOfDay();

    List<Event> events = model.getEventOnDate(dateTime);

    System.out.println("Events on " + date + ":");
    if (events.isEmpty()) {
      System.out.println("No events found");
    } else {
      for (Event event : events) {
        StringBuilder sb = new StringBuilder();
        sb.append("- ").append(event.getSubject());

        if (event.isAllDay()) {
          sb.append(" (All day)");
        } else {
          sb.append(" from ").append(event.getStartDateTime().toLocalTime())
              .append(" to ").append(event.getEndDateTime().toLocalTime());
        }

        if (event.getLocation() != null && !event.getLocation().trim().isEmpty()) {
          sb.append(" at ").append(event.getLocation());
        }

        if (event.getDescription() != null && !event.getDescription().trim().isEmpty()) {
          sb.append(" - ").append(event.getDescription());
        }

        System.out.println(sb.toString());
      }
    }
  }

  /**
   * Parse: print events from YYYY-MM-DDTHH:mm to YYYY-MM-DDTHH:mm.
   */
  private void parsePrintEventsInRange(String trimmed) {
    String remainder = trimmed.substring("print events from".length()).trim();

    int toIndex = remainder.indexOf(" to ");
    if (toIndex == -1) {
      throw new IllegalArgumentException("Missing 'to' in print events command");
    }

    String startStr = remainder.substring(0, toIndex).trim();
    String endStr = remainder.substring(toIndex + 4).trim();

    LocalDateTime startDateTime = parseDateTime(startStr);
    LocalDateTime endDateTime = parseDateTime(endStr);

    List<Event> events = model.getEventsInRange(startDateTime, endDateTime);

    System.out.println("Events from " + startDateTime + " to " + endDateTime + ":");
    if (events.isEmpty()) {
      System.out.println("No events found");
    } else {
      for (Event event : events) {
        StringBuilder sb = new StringBuilder();
        sb.append("- ").append(event.getSubject());

        sb.append(" from ").append(event.getStartDateTime())
            .append(" to ").append(event.getEndDateTime());

        if (event.getLocation() != null && !event.getLocation().trim().isEmpty()) {
          sb.append(" at ").append(event.getLocation());
        }

        if (event.getDescription() != null && !event.getDescription().trim().isEmpty()) {
          sb.append(" - ").append(event.getDescription());
        }

        System.out.println(sb.toString());
      }
    }
  }

  /**
   * Parse: show status on YYYY-MM-DDTHH:mm.
   */
  private void parseShowStatus(String trimmed) {
    String remainder = trimmed.substring("show status on".length()).trim();

    LocalDateTime dateTime = parseDateTime(remainder);
    boolean isBusy = model.isBusy(dateTime);

    System.out.println(isBusy ? "busy" : "available");
  }

  /**
   * Parse: export cal filename.csv.
   */
  private void parseExport(String trimmed) {
    String remainder = trimmed.substring("export cal".length()).trim();

    if (remainder.isEmpty()) {
      throw new IllegalArgumentException("Missing filename in export command");
    }

    String filename = remainder.trim();
    model.exportCalendar(filename);

    System.out.println("Calendar exported to " + filename);
  }

  /**
   * Parses the dateTimeString.
   *
   * @param dateTimeStr the dateTimeString to be parsed.
   * @return the parsed date time.
   */
  private LocalDateTime parseDateTime(String dateTimeStr) {
    try {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
      return LocalDateTime.parse(dateTimeStr, formatter);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException(
          "Invalid date/time format. Expected: YYYY-MM-DDTHH:mm", e);
    }
  }

  /**
   * Parses the date string.
   *
   * @param dateStr the date string to be parsed.
   * @return the parsed date string.
   */
  private LocalDate parseDate(String dateStr) {
    try {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
      return LocalDate.parse(dateStr, formatter);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException(
          "Invalid date format. Expected: YYYY-MM-DD", e);
    }
  }

  /**
   * Method that takes a string and removes its quotes.
   *
   * @param str the string whose quotes need to be removed.
   * @return the updated string without quotes.
   */
  private String removeQuotes(String str) {
    if (str.startsWith("\"") && str.endsWith("\"")) {
      return str.substring(1, str.length() - 1);
    }
    return str;
  }

  /**
   * Method to find an event with specified subject, startDateTime and endDateTime.
   *
   * @param subject       the subject used to find an event.
   * @param startDateTime the start date and time used for finding the event.
   * @param endDateTime   The end date and time used for finding the event.
   * @return the event if found.
   */
  private Event findEvent(String subject, LocalDateTime startDateTime,
                          LocalDateTime endDateTime) {
    List<Event> events = model.getEventOnDate(startDateTime);
    for (Event event : events) {
      if (event.getSubject().equals(subject)
          && event.getStartDateTime().equals(startDateTime)
          && event.getEndDateTime().equals(endDateTime)) {
        return event;
      }
    }
    return null;
  }

  /**
   * Method to find an event using subject and start only.
   *
   * @param subject       the subject used to find the event.
   * @param startDateTime the start date and time used to find the event.
   * @return the event if found.
   */
  private Event findEventBySubjectAndStart(String subject, LocalDateTime startDateTime) {
    List<Event> events = model.getEventOnDate(startDateTime);
    for (Event event : events) {
      if (event.getSubject().equals(subject)
          && event.getStartDateTime().equals(startDateTime)) {
        return event;
      }
    }
    return null;
  }

  /**
   * Helper method to parse and set optional properties (location and description) on an event.
   *
   * @param subject       the event subject
   * @param startDateTime the event start date and time
   * @param remainingText the remaining text containing optional properties
   */
  private void setOptionalProperties(String subject, LocalDateTime startDateTime,
                                     String remainingText) {
    String location = null;
    String description = null;

    int atIndex = remainingText.indexOf(" at ");
    int descIndex = remainingText.indexOf(" description ");
    if (descIndex != -1) {
      String afterDescKeyword = remainingText.substring(descIndex + 13);
      int atAfterDesc = afterDescKeyword.indexOf(" at ");

      String descValue;
      if (atAfterDesc != -1) {
        descValue = afterDescKeyword.substring(0, atAfterDesc).trim();
      } else {
        descValue = afterDescKeyword.trim();
      }

      description = removeQuotes(descValue);
    }
    if (atIndex != -1) {
      String afterAtKeyword = remainingText.substring(atIndex + 4);
      int descAfterAt = afterAtKeyword.indexOf(" description ");

      String locValue;
      if (descAfterAt != -1) {
        locValue = afterAtKeyword.substring(0, descAfterAt).trim();
      } else {
        locValue = afterAtKeyword.trim();
      }

      location = removeQuotes(locValue);
    }
    if (location != null || description != null) {
      Event event = findEventBySubjectAndStart(subject, startDateTime);
      if (event != null) {
        if (location != null) {
          event.setLocation(location);
        }
        if (description != null) {
          event.setDescription(description);
        }
      }
    }
  }
}