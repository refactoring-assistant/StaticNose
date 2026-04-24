package calendar.controller;

import calendar.CalendarProperty;
import calendar.Location;
import calendar.Property;
import calendar.Status;
import calendar.export.CalendarExporter;
import calendar.export.ExporterFactory;
import calendar.model.Event;
import calendar.model.Model;
import calendar.model.ReoccurringEvent;
import calendar.view.CalendarViewInterface;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.zone.ZoneRulesException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class implements the controller interface.
 */

public class CalendarController implements CalendarControllerInterface {

  private Model model;
  private CalendarViewInterface view;
  private final SimpleDateFormat dateTimeFormat;
  private final SimpleDateFormat dateFormat;

  /**
   * Constructs a CalendarController with given CalendarModel and CalendarView.
   *
   * @param calendarView CalendarView instance that interacts with input and displays data
   */
  public CalendarController(CalendarViewInterface calendarView, Model model) {
    this.model = model;
    this.view = calendarView;
    this.dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
    this.dateFormat = new SimpleDateFormat("yyyy-MM-dd");
  }

  @Override
  public void executeCommand(String command) {
    if (command == null || command.trim().isEmpty()) {
      view.showError("Empty command");
      return;
    }

    String trimmedCommand = command.trim();

    try {
      if (trimmedCommand.equalsIgnoreCase("exit")) {
        return;
      } else if (trimmedCommand.startsWith("create event ")) {
        handleCreateEvent(trimmedCommand);
      } else if (trimmedCommand.startsWith("edit event ")) {
        handleEditEvent(trimmedCommand);
      } else if (trimmedCommand.startsWith("edit events ")) {
        handleEditEvents(trimmedCommand);
      } else if (trimmedCommand.startsWith("edit series ")) {
        handleEditSeries(trimmedCommand);
      } else if (trimmedCommand.startsWith("print events on ")) {
        handlePrintEventsOn(trimmedCommand);
      } else if (trimmedCommand.startsWith("print events from ")) {
        handlePrintEventsFrom(trimmedCommand);
      } else if (trimmedCommand.startsWith("export cal ")) {
        handleExportCal(trimmedCommand);
      } else if (trimmedCommand.startsWith("show status on ")) {
        handleShowStatus(trimmedCommand);
      } else if (trimmedCommand.startsWith("create calendar ")) {
        handleCreateCalendar(trimmedCommand);
      } else if (trimmedCommand.startsWith("use calendar ")) {
        handleUseCalendar(trimmedCommand);
      } else if (trimmedCommand.startsWith("edit calendar ")) {
        handleEditCalendar(trimmedCommand);
      } else if (trimmedCommand.startsWith("copy event ")) {
        handleCopyEvent(trimmedCommand);
      } else if (trimmedCommand.startsWith("copy events between ")) {
        handleCopyEventsBetween(trimmedCommand);
      } else if (trimmedCommand.startsWith("copy events on ")) {
        handleCopyEventsOnDate(trimmedCommand);
      } else {
        view.showError("Unknown command. Type a valid command or 'exit' to quit.");
      }
    } catch (Exception e) {
      view.showError("Error executing command: " + e.getMessage());
    }
  }

  private void handleCopyEvent(String command) throws ParseException, IllegalStateException {
    Pattern p1 = Pattern.compile("copy event (.+?) on (\\S+) --target (.+?) to (\\S+)");
    Matcher m1 = p1.matcher(command);
    if (m1.matches()) {
      String eventName = m1.group(1).trim();
      Date originalStartDateTime = dateTimeFormat.parse(m1.group(2));
      String targetCalendarName = m1.group(3).trim();
      Date newStartDateTime = dateTimeFormat.parse(m1.group(4));
      Set<Event> eventsOnDateTime = this.model.getEventsOnDate(originalStartDateTime);
      Event eventToCopy = null;
      for (Event event : eventsOnDateTime) {
        if (event.getSubject().equals(eventName)
            && event.getStart().equals(originalStartDateTime)) {
          eventToCopy = event;
          break;
        }
      }
      if (eventToCopy == null) {
        view.showError("Event not found: " + eventName + " at " + m1.group(2));
        return;
      }
      Date newEndDateTime = getNewEndDateTime(eventToCopy, newStartDateTime);
      model.createEvent(eventToCopy.getSubject(), newStartDateTime,
          newEndDateTime, targetCalendarName);
      if (eventToCopy.getDescription() != null) {
        model.editEvent(Property.DESCRIPTION, eventToCopy.getSubject(),
            newStartDateTime, newEndDateTime, eventToCopy.getDescription(),
            targetCalendarName);
      }
      if (eventToCopy.getLocation() != null) {
        model.editEvent(Property.LOCATION, eventToCopy.getSubject(),
            newStartDateTime, newEndDateTime, eventToCopy.getLocation(),
            targetCalendarName);
      }
      if (eventToCopy.getStatus() != null) {
        model.editEvent(Property.STATUS, eventToCopy.getSubject(),
            newStartDateTime, newEndDateTime, eventToCopy.getStatus(),
            targetCalendarName);
      }
      view.showMessage("Event successfully copied");
    } else {
      view.showError("Invalid copy event command format");
    }
  }

  private Date getNewEndDateTime(Event event, Date newStartDateTime) {
    long difference = newStartDateTime.getTime() - event.getStart().getTime();
    Date newEndDateTime = event.getEnd();
    newEndDateTime.setTime(newEndDateTime.getTime() + difference);
    return newEndDateTime;
  }

  private void handleEditCalendar(String command) throws IllegalStateException {
    Pattern p1 = Pattern.compile("edit calendar --name (.+?) --property (.+?) (.+?)");
    Matcher m1 = p1.matcher(command);
    if (m1.matches()) {
      String name = m1.group(1).trim();
      CalendarProperty property = parseCalendarProperty(m1.group(2).trim());
      String newPropertyValue = m1.group(3).trim();

      if (property == CalendarProperty.TIMEZONE) {
        TimeZone newTimeZone = parseTimeZone(newPropertyValue);
        this.model.editCalendar(property, name, newTimeZone);
      } else {
        this.model.editCalendar(property, name, newPropertyValue);
      }

      view.showMessage("Calendar edited successfully");
    } else {
      view.showError("Invalid edit calendar command format");
    }
  }

  private void handleUseCalendar(String command) throws IllegalStateException {
    Pattern p1 = Pattern.compile("use calendar --name (.+?)");
    Matcher m1 = p1.matcher(command);
    if (m1.matches()) {
      String name = m1.group(1).trim();
      this.model.useCalendar(name);
    } else {
      view.showError("Invalid use calendar command format");
    }

  }

  private void handleCreateCalendar(String command) {
    Pattern p1 = Pattern.compile("create calendar --name (.+?) --timezone (.+?)");
    Matcher m1 = p1.matcher(command);
    if (m1.matches()) {
      String name = m1.group(1).trim();
      TimeZone timeZone = this.parseTimeZone(m1.group(2).trim());
      this.model.createCalendar(name, timeZone);
    } else {
      view.showError("Invalid create calendar command format");
    }

  }

  private void handleCreateEvent(String command) throws ParseException, IllegalStateException {

    Pattern p1 = Pattern.compile("create event (.+?) from (\\S+) to (\\S+)$");
    Matcher m1 = p1.matcher(command);
    if (m1.matches()) {
      String subject = m1.group(1).trim();
      Date start = dateTimeFormat.parse(m1.group(2));
      Date end = dateTimeFormat.parse(m1.group(3));
      this.model.createEvent(subject, start, end);
      this.view.showMessage("Event created: " + subject);
      return;
    }

    Pattern p2 =
        Pattern.compile("create event (.+?) from (\\S+) to (\\S+) repeats (.+?) for (\\d+) times");
    Matcher m2 = p2.matcher(command);
    if (m2.matches()) {
      String subject = m2.group(1).trim();
      Date start = dateTimeFormat.parse(m2.group(2));
      Date end = dateTimeFormat.parse(m2.group(3));
      int[] weekdays = parseWeekdays(m2.group(4));
      int repeat = Integer.parseInt(m2.group(5));
      this.model.createReoccurringEvent(subject, start, end, weekdays, repeat);
      view.showMessage("Recurring event created: " + subject);
      return;
    }
    Pattern p3 =
        Pattern.compile("create event (.+?) from (\\S+) to (\\S+) repeats (.+?) until (\\S+)");
    Matcher m3 = p3.matcher(command);
    if (m3.matches()) {
      String subject = m3.group(1).trim();
      Date start = dateTimeFormat.parse(m3.group(2));
      Date end = dateTimeFormat.parse(m3.group(3));
      int[] weekdays = parseWeekdays(m3.group(4));
      Date until = dateFormat.parse(m3.group(5));
      this.model.createEventUntil(subject, start, end, weekdays, until);
      view.showMessage("Recurring event created: " + subject);
      return;
    }
    Pattern p4 = Pattern.compile("create event (.+?) on (\\S+)$");
    Matcher m4 = p4.matcher(command);
    if (m4.matches()) {
      String subject = m4.group(1).trim();
      Date date = dateFormat.parse(m4.group(2));
      this.model.createAllDayEvent(subject, date);
      view.showMessage("All-day event created: " + subject);
      return;
    }
    Pattern p5 = Pattern.compile("create event (.+?) on (\\S+) repeats (.+?) for (\\d+) times");
    Matcher m5 = p5.matcher(command);
    if (m5.matches()) {
      String subject = m5.group(1).trim();
      Date date = dateFormat.parse(m5.group(2));
      int[] weekdays = parseWeekdays(m5.group(3));
      int repeat = Integer.parseInt(m5.group(4));
      this.model.createAllDayEventSeries(subject, date, weekdays, repeat);
      view.showMessage("All-day event series created: " + subject);
      return;
    }
    Pattern p6 = Pattern.compile("create event (.+?) on (\\S+) repeats (.+?) until (\\S+)");
    Matcher m6 = p6.matcher(command);
    if (m6.matches()) {
      String subject = m6.group(1).trim();
      Date date = dateFormat.parse(m6.group(2));
      int[] weekdays = parseWeekdays(m6.group(3));
      Date until = dateFormat.parse(m6.group(4));
      this.model.createAllDayEventUntil(subject, date, weekdays, until);
      view.showMessage("All-day event series created: " + subject);
      return;
    }

    view.showError("Invalid create event command format");
  }

  private void handleEditEvent(String command) throws ParseException, IllegalStateException {

    Pattern p = Pattern.compile("edit event (\\S+) (.+?) from (\\S+) to (\\S+) with (.+)");
    Matcher m = p.matcher(command);

    if (m.matches()) {
      Property property = parseProperty(m.group(1));
      String subject = m.group(2).trim();
      Date start = dateTimeFormat.parse(m.group(3));
      Date end = dateTimeFormat.parse(m.group(4));
      Object newValue = parsePropertyValue(property, m.group(5).trim());

      this.model.editEvent(property, subject, start, end, newValue);
      view.showMessage("Event edited successfully");
    } else {
      view.showError("Invalid edit event command format");
    }
  }

  private void handleEditEvents(String command) throws ParseException, IllegalStateException {

    Pattern p = Pattern.compile("edit events (\\S+) (.+?) from (\\S+) with (.+)");
    Matcher m = p.matcher(command);

    if (m.matches()) {
      Property property = parseProperty(m.group(1));
      String subject = m.group(2).trim();
      Date start = dateTimeFormat.parse(m.group(3));
      Object newValue = parsePropertyValue(property, m.group(4).trim());

      this.model.editEventsStartingOn(property, subject, start, newValue);
      view.showMessage("Events edited successfully");
    } else {
      view.showError("Invalid edit events command format");
    }
  }

  private void handleEditSeries(String command) throws ParseException, IllegalStateException {

    Pattern p = Pattern.compile("edit series (\\S+) (.+?) from (\\S+) with (.+)");
    Matcher m = p.matcher(command);

    if (m.matches()) {
      Property property = parseProperty(m.group(1));
      String subject = m.group(2).trim();
      Date start = dateTimeFormat.parse(m.group(3));
      Object newValue = parsePropertyValue(property, m.group(4).trim());

      this.model.editSeries(property, subject, start, newValue);
      view.showMessage("Series edited successfully");
    } else {
      view.showError("Invalid edit series command format");
    }
  }

  private void handlePrintEventsOn(String command) throws ParseException, IllegalStateException {

    Pattern p = Pattern.compile("print events on (\\S+)");
    Matcher m = p.matcher(command);

    if (m.matches()) {
      Date date = dateFormat.parse(m.group(1));
      Set<Event> events = this.model.getEventsOnDate(date);

      if (events == null || events.isEmpty()) {
        view.showMessage("No events on " + m.group(1));
      } else {
        view.showMessage("Events on " + m.group(1) + ":");
        for (Event event : events) {
          view.showMessage("  - " + event.getSubject()
              + " from " + formatDateTime(event.getStart())
              + " to " + formatDateTime(event.getEnd()));
        }
      }
    } else {
      view.showError("Invalid print events on command format");
    }
  }

  private void handlePrintEventsFrom(String command) throws ParseException, IllegalStateException {

    Pattern p = Pattern.compile("print events from (\\S+) to (\\S+)");
    Matcher m = p.matcher(command);

    if (m.matches()) {
      Date start = dateTimeFormat.parse(m.group(1));
      Date end = dateTimeFormat.parse(m.group(2));
      Set<Event> events = this.model.getEventsInRange(start, end);

      if (events == null || events.isEmpty()) {
        view.showMessage("No events in range provided.");
      } else {
        view.showMessage("Events from " + m.group(1) + " to " + m.group(2) + ":");
        for (Event event : events) {
          view.showMessage("  - " + event.getSubject()
              + " from " + formatDateTime(event.getStart())
              + " to " + formatDateTime(event.getEnd()));
        }
      }
    } else {
      view.showError("Invalid print events from command format");
    }
  }

  private void handleExportCal(String command) throws IllegalStateException {
    Pattern p = Pattern.compile("export cal (.+)");
    Matcher m = p.matcher(command);

    if (m.matches()) {
      String fileName = m.group(1).trim();

      try {
        Date startRange = new Date();
        startRange.setHours(0);
        startRange.setMinutes(0);
        startRange.setSeconds(0);

        Date endRange = (Date) startRange.clone();
        endRange.setYear(endRange.getYear() + 1);

        Set<Event> allEvents = this.model.getEventsInRange(startRange, endRange);
        TimeZone calendarTimeZone = this.model.getTimeZone();
        CalendarExporter exporter = ExporterFactory.getExporterForFile(fileName);
        exporter.export(allEvents, fileName, calendarTimeZone);
        java.io.File file = new java.io.File(fileName);
        String absolutePath = file.getAbsolutePath();

        int eventCount = (allEvents != null) ? allEvents.size() : 0;
        view.showMessage("Exported " + eventCount + " events");
        view.showMessage("Calendar exported to: " + absolutePath);

      } catch (IllegalArgumentException e) {
        view.showError("Unsupported file format: " + e.getMessage());
      } catch (IOException e) {
        view.showError("Unable to export calendar: " + e.getMessage());
      }
    } else {
      view.showError("Invalid export cal command format. "
          + "Use this format: export cal <filename>");
    }
  }

  private void handleCopyEventsOnDate(String command) throws ParseException, IllegalStateException {
    Pattern p1 = Pattern.compile("copy events on (\\S+) --target (.+?) to (\\S+)");
    Matcher m1 = p1.matcher(command);

    if (m1.matches()) {
      String sourceDateStr = m1.group(1).trim();
      String targetCalendarName = m1.group(2).trim();
      String targetDateStr = m1.group(3).trim();

      Date sourceDate = dateFormat.parse(sourceDateStr);
      Date targetDate = dateFormat.parse(targetDateStr);

      Set<Event> eventsOnDate = this.model.getEventsOnDate(sourceDate);

      if (eventsOnDate == null || eventsOnDate.isEmpty()) {
        view.showMessage("No events to copy on " + sourceDateStr);
        return;
      }

      TimeZone sourceTimeZone = this.model.getTimeZone();
      TimeZone targetTimeZone = this.model.getTimeZone(targetCalendarName);

      int copiedCount = 0;

      for (Event event : eventsOnDate) {
        Date convertedStart = convertTimeZone(event.getStart(), sourceTimeZone, targetTimeZone);
        Date convertedEnd = convertTimeZone(event.getEnd(), sourceTimeZone, targetTimeZone);

        long dateDifference = targetDate.getTime() - sourceDate.getTime();

        Date newStartDateTime = new Date(convertedStart.getTime() + dateDifference);
        Date newEndDateTime = new Date(convertedEnd.getTime() + dateDifference);

        try {
          model.createEvent(event.getSubject(), newStartDateTime, newEndDateTime,
              targetCalendarName);

          if (event.getDescription() != null) {
            model.editEvent(Property.DESCRIPTION, event.getSubject(), newStartDateTime,
                newEndDateTime, event.getDescription(), targetCalendarName);
          }
          if (event.getLocation() != null) {
            model.editEvent(Property.LOCATION, event.getSubject(), newStartDateTime,
                newEndDateTime, event.getLocation(), targetCalendarName);
          }
          if (event.getStatus() != null) {
            model.editEvent(Property.STATUS, event.getSubject(), newStartDateTime,
                newEndDateTime, event.getStatus(), targetCalendarName);
          }

          copiedCount++;
        } catch (IllegalArgumentException e) {
          view.showError("Could not copy event '" + event.getSubject()
              + "': " + e.getMessage());
        }
      }

      view.showMessage("Successfully copied " + copiedCount + " event(s)");

    } else {
      view.showError("Invalid copy events on command format");
    }
  }

  private void handleShowStatus(String command) throws ParseException, IllegalStateException {

    Pattern p = Pattern.compile("show status on (\\S+)");
    Matcher m = p.matcher(command);

    if (m.matches()) {
      Date dateTime = dateTimeFormat.parse(m.group(1));
      boolean hasEvent = this.model.eventAt(dateTime);

      if (hasEvent) {
        view.showMessage("Status: You are busy at " + m.group(1));
      } else {
        view.showMessage("Status: You are free at " + m.group(1));
      }
    } else {
      view.showError("Invalid show status command format");
    }
  }

  private int[] parseWeekdays(String weekdaysStr) {
    weekdaysStr = weekdaysStr.replace(",", " ").trim();
    if (!weekdaysStr.contains(" ")) {
      if (weekdaysStr.matches("[A-Za-z]+") && weekdaysStr.length() > 1) {
        int[] weekdays = new int[weekdaysStr.length()];
        for (int i = 0; i < weekdaysStr.length(); i++) {
          char dayChar = weekdaysStr.charAt(i);
          weekdays[i] = convertDayLetterToNumber(String.valueOf(dayChar));
        }
        return weekdays;
      }
      try {
        return new int[]{Integer.parseInt(weekdaysStr)};
      } catch (NumberFormatException e) {
        return new int[]{convertDayLetterToNumber(weekdaysStr)};
      }
    }
    String[] parts = weekdaysStr.split("\\s+");
    int[] weekdays = new int[parts.length];
    for (int i = 0; i < parts.length; i++) {
      String part = parts[i].trim();
      if (part.isEmpty()) {
        continue;
      }
      try {
        weekdays[i] = Integer.parseInt(part);
      } catch (NumberFormatException e) {
        weekdays[i] = convertDayLetterToNumber(part);
      }
    }
    return weekdays;
  }

  private int convertDayLetterToNumber(String dayLetter) {
    switch (dayLetter.toUpperCase()) {
      case "U":
        return 0;
      case "M":
        return 1;
      case "T":
        return 2;
      case "W":
        return 3;
      case "R":
        return 4;
      case "F":
        return 5;
      case "S":
        return 6;
      default:
        throw new IllegalArgumentException("Invalid day letter: " + dayLetter);
    }
  }

  private Property parseProperty(String propertyStr) {
    try {
      return Property.valueOf(propertyStr.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid property: " + propertyStr);
    }
  }

  private CalendarProperty parseCalendarProperty(String propertyStr) {
    try {
      return CalendarProperty.valueOf(propertyStr.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid property: " + propertyStr);
    }
  }

  private TimeZone parseTimeZone(String timeZone) {
    try {
      ZoneId.of(timeZone);
    } catch (ZoneRulesException e) {
      throw new IllegalStateException("Time zone not valid.");
    }
    return TimeZone.getTimeZone(timeZone);
  }

  /**
   * Converts a date from source timezone to target timezone. The absolute moment in time stays the
   * same, but the representation changes.
   *
   * @param date           the date to convert
   * @param sourceTimeZone the source timezone
   * @param targetTimeZone the target timezone
   * @return the converted date
   */
  private Date convertTimeZone(Date date, TimeZone sourceTimeZone, TimeZone targetTimeZone) {
    long sourceOffset = sourceTimeZone.getOffset(date.getTime());
    long targetOffset = targetTimeZone.getOffset(date.getTime());
    long offsetDifference = targetOffset - sourceOffset;

    return new Date(date.getTime() + offsetDifference);
  }

  private Object parsePropertyValue(Property property, String value) throws ParseException {
    switch (property) {
      case START:
      case END:
        return dateTimeFormat.parse(value);
      case LOCATION:
        return Location.valueOf(value.toUpperCase());
      case STATUS:
        return Status.valueOf(value.toUpperCase());
      case SUBJECT:
      case DESCRIPTION:
      default:
        return value;
    }
  }

  private String formatDateTime(Date date) {
    return dateTimeFormat.format(date);
  }

  @Override
  public void go() {
    String command;
    while (!(command = view.getCommand()).equalsIgnoreCase("exit")) {
      executeCommand(command);
    }
  }

  private void handleCopyEventsBetween(String command)
      throws ParseException, IllegalStateException {
    Pattern p1 = Pattern.compile("copy events between (\\S+) and (\\S+) --target (.+?) to (\\S+)");
    Matcher m1 = p1.matcher(command);

    if (m1.matches()) {
      String sourceStartDateStr = m1.group(1).trim();
      String sourceEndDateStr = m1.group(2).trim();
      final String targetCalendarName = m1.group(3).trim();
      String targetStartDateStr = m1.group(4).trim();

      Date sourceStartDate = dateFormat.parse(sourceStartDateStr);
      Date sourceEndDate = dateFormat.parse(sourceEndDateStr);
      final Date targetStartDate = dateFormat.parse(targetStartDateStr);

      sourceStartDate.setHours(0);
      sourceStartDate.setMinutes(0);
      sourceStartDate.setSeconds(0);

      Date sourceEndDateTime = (Date) sourceEndDate.clone();
      sourceEndDateTime.setHours(23);
      sourceEndDateTime.setMinutes(59);
      sourceEndDateTime.setSeconds(59);

      Set<Event> eventsInRange = this.model.getEventsInRange(sourceStartDate, sourceEndDateTime);

      if (eventsInRange == null || eventsInRange.isEmpty()) {
        view.showMessage("No events to copy in the specified range");
        return;
      }

      TimeZone sourceTimeZone = this.model.getTimeZone();
      TimeZone targetTimeZone = this.model.getTimeZone(targetCalendarName);

      long dateDifference = targetStartDate.getTime() - sourceStartDate.getTime();

      int copiedCount = 0;

      Set<Event> processedSeriesStarts = new HashSet<>();

      for (Event event : eventsInRange) {
        Event firstInSeries = event.getFirstEventInSeries();

        if (event instanceof ReoccurringEvent) {
          ReoccurringEvent recurEvent = (ReoccurringEvent) event;

          if (processedSeriesStarts.contains(firstInSeries)) {
            continue;
          }

          processedSeriesStarts.add(firstInSeries);

          copiedCount += copySeries(recurEvent, sourceStartDate, sourceEndDateTime,
              dateDifference, sourceTimeZone, targetTimeZone, targetCalendarName);

        } else {
          copiedCount += copySingleEvent(event, dateDifference, sourceTimeZone,
              targetTimeZone, targetCalendarName);
        }
      }

      view.showMessage("Successfully copied " + copiedCount + " event(s)");

    } else {
      view.showError("Invalid copy events between command format");
    }
  }

  /**
   * Helper method to copy a single event with timezone conversion.
   *
   * @param event              the event to copy
   * @param dateDifference     the time difference to shift the event
   * @param sourceTimeZone     source calendar timezone
   * @param targetTimeZone     target calendar timezone
   * @param targetCalendarName name of target calendar
   * @return 1 if successful, 0 if failed
   */
  private int copySingleEvent(Event event, long dateDifference, TimeZone sourceTimeZone,
      TimeZone targetTimeZone, String targetCalendarName) {
    try {
      Date convertedStart = convertTimeZone(event.getStart(), sourceTimeZone, targetTimeZone);
      Date convertedEnd = convertTimeZone(event.getEnd(), sourceTimeZone, targetTimeZone);

      Date newStartDateTime = new Date(convertedStart.getTime() + dateDifference);
      Date newEndDateTime = new Date(convertedEnd.getTime() + dateDifference);

      model.createEvent(event.getSubject(), newStartDateTime, newEndDateTime, targetCalendarName);

      copyEventProperties(event, newStartDateTime, newEndDateTime, targetCalendarName);

      return 1;
    } catch (IllegalArgumentException e) {
      view.showError("Could not copy event '" + event.getSubject() + "': " + e.getMessage());
      return 0;
    }
  }

  /**
   * Helper method to copy a recurring event series, only copying instances that fall within the
   * specified range, while retaining series status.
   *
   * @param recurEvent         the recurring event (any instance in the series)
   * @param rangeStart         start of the range to copy
   * @param rangeEnd           end of the range to copy
   * @param dateDifference     the time difference to shift events
   * @param sourceTimeZone     source calendar timezone
   * @param targetTimeZone     target calendar timezone
   * @param targetCalendarName name of target calendar
   * @return number of events copied
   */
  private int copySeries(ReoccurringEvent recurEvent, Date rangeStart, Date rangeEnd,
      long dateDifference, TimeZone sourceTimeZone, TimeZone targetTimeZone,
      String targetCalendarName) {
    int copiedCount = 0;

    Event firstInSeries = recurEvent.getFirstEventInSeries();

    List<Event> eventsInSeriesInRange = new ArrayList<>();
    Event currentEvent = firstInSeries;

    while (currentEvent != null) {
      if (!currentEvent.getStart().after(rangeEnd) && !currentEvent.getEnd().before(rangeStart)) {
        eventsInSeriesInRange.add(currentEvent);
      }
      currentEvent = currentEvent.getNextEvent();
    }

    if (eventsInSeriesInRange.isEmpty()) {
      return 0;
    }

    if (eventsInSeriesInRange.size() == 1) {
      return copySingleEvent(eventsInSeriesInRange.get(0), dateDifference,
          sourceTimeZone, targetTimeZone, targetCalendarName);
    }

    Set<Integer> weekdaysSet = new HashSet<>();
    for (Event evt : eventsInSeriesInRange) {
      weekdaysSet.add(evt.getStart().getDay());
    }

    int[] weekdays = weekdaysSet.stream().mapToInt(Integer::intValue).toArray();

    Event firstEventToCopy = eventsInSeriesInRange.get(0);
    Event lastEventToCopy = eventsInSeriesInRange.get(eventsInSeriesInRange.size() - 1);

    Date convertedStart =
        convertTimeZone(firstEventToCopy.getStart(), sourceTimeZone, targetTimeZone);
    Date convertedEnd = convertTimeZone(firstEventToCopy.getEnd(), sourceTimeZone, targetTimeZone);

    Date newStartDateTime = new Date(convertedStart.getTime() + dateDifference);
    Date newEndDateTime = new Date(convertedEnd.getTime() + dateDifference);

    Date convertedLastStart =
        convertTimeZone(lastEventToCopy.getStart(), sourceTimeZone, targetTimeZone);
    Date untilDate = new Date(convertedLastStart.getTime() + dateDifference);

    try {
      model.createEventUntil(firstEventToCopy.getSubject(), newStartDateTime, newEndDateTime,
          weekdays, untilDate, targetCalendarName);

      copiedCount = eventsInSeriesInRange.size();

    } catch (IllegalArgumentException e) {
      view.showError(
          "Could not copy event series '" + firstEventToCopy.getSubject() + "': " + e.getMessage());
      return 0;
    }

    return copiedCount;
  }

  /**
   * Helper method to copy optional event properties.
   *
   * @param sourceEvent        the source event
   * @param newStart           the new start time
   * @param newEnd             the new end time
   * @param targetCalendarName the target calendar name
   */
  private void copyEventProperties(Event sourceEvent, Date newStart, Date newEnd,
      String targetCalendarName) {
    try {
      if (sourceEvent.getDescription() != null) {
        model.editEvent(Property.DESCRIPTION, sourceEvent.getSubject(), newStart,
            newEnd, sourceEvent.getDescription(), targetCalendarName);
      }
      if (sourceEvent.getLocation() != null) {
        model.editEvent(Property.LOCATION, sourceEvent.getSubject(), newStart,
            newEnd, sourceEvent.getLocation(), targetCalendarName);
      }
      if (sourceEvent.getStatus() != null) {
        model.editEvent(Property.STATUS, sourceEvent.getSubject(), newStart,
            newEnd, sourceEvent.getStatus(), targetCalendarName);
      }
    } catch (IllegalArgumentException e) {
      return;
    }
  }
}