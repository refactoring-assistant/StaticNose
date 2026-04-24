package calendar.controller;

import calendar.model.Calendar;
import calendar.model.CalenderManager;
import calendar.model.Events;
import calendar.view.ViewConsole;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to coordinate copy events from one calendar to another.
 */
public class CopyEvent implements CommandInterface {

  private final String textInput;

  /**
   * Constructor to initialise commands class.
   *
   * @param textInput the commands entered by user
   */
  public CopyEvent(String textInput) {
    this.textInput = textInput;
  }

  /**
   * Function to execute commands.
   *
   * @param manager instance of calendar manager
   * @param view    instance of view console
   */
  @Override
  public void execute(CalenderManager manager, ViewConsole view) {
    Calendar calendar = manager.getCurrentCalender();
    if (calendar == null) {
      view.dispError("No calendar in use. Use 'use calendar' command first");
      return;
    }

    try {
      if (textInput.contains("copy event ")) {
        copySingleEvent(manager, calendar, view);
      } else if (textInput.contains("copy events on ")) {
        copyEventsOn(manager, calendar, view);
      } else if (textInput.contains("copy events between ")) {
        copyEventsBetween(manager, calendar, view);
      } else {
        view.dispError("Invalid copy command");
      }
    } catch (Exception e) {
      view.dispError("Error copying events: " + e.getMessage());
    }
  }

  /**
   * Function to copy a single event to other calendar.
   *
   * @param manager  instance of calendarManager
   * @param calendar instance of calendar object
   * @param view     instance of view console
   */
  private void copySingleEvent(CalenderManager manager, Calendar calendar, ViewConsole view) {
    Pattern pattern = Pattern.compile("copy event (?:(\"([^\"]+)\")|(\\S+)) on (\\S+) "
        + "--target (\\S+) to (\\S+)");
    Matcher matcher = pattern.matcher(textInput);

    if (!matcher.find()) {
      view.dispError("Invalid copy event command");
      return;
    }

    String eventName = matcher.group(2) != null ? matcher.group(2) : matcher.group(3);
    LocalDateTime startTime = LocalDateTime.parse(matcher.group(4));
    Calendar targetCal = manager.fetchCal(matcher.group(5));
    LocalDateTime targetTime = LocalDateTime.parse(matcher.group(6));

    Events event = calendar.findEvent(eventName, startTime);
    if (event == null) {
      view.dispError("Unable to find event " + eventName + " at " + startTime);
      return;
    }

    ZoneId sourceZone = calendar.getTimeZone();
    ZoneId destZone = targetCal.getTimeZone();
    ZonedDateTime zonedStart = event.getStartTime().atZone(sourceZone);
    ZonedDateTime zonedEnd = event.getEndTime().atZone(sourceZone);
    ZonedDateTime destZonedStart = zonedStart.withZoneSameInstant(destZone);
    ZonedDateTime destZonedEnd = zonedEnd.withZoneSameInstant(destZone);
    Duration duration = Duration.between(destZonedStart, destZonedEnd);

    Events copiedEvent = new Events(event.getSubject(), targetTime, targetTime.plus(duration));
    copiedEvent.setDescription(event.getDescription());
    copiedEvent.setLocation(event.getLocation());
    copiedEvent.setStatus(event.getStatus().toString().toLowerCase());
    targetCal.addEvent(copiedEvent);
    view.dispSuccess("Events copied successfully to calendar: " + matcher.group(5));
  }

  /**
   * Function to copy all events on a date.
   *
   * @param manager  instance of calendarManager
   * @param calendar instance of calendar object
   * @param view     instance of view console
   */
  private void copyEventsOn(CalenderManager manager, Calendar calendar, ViewConsole view) {
    Pattern pattern = Pattern.compile("copy events on (\\S+) --target (\\S+) to (\\S+)");
    Matcher matcher = pattern.matcher(textInput);

    if (!matcher.find()) {
      view.dispError("Invalid copy events on date command");
      return;
    }

    LocalDate sourceDate = LocalDate.parse(matcher.group(1));
    String targetCalName = matcher.group(2);
    LocalDate targetDate = LocalDate.parse(matcher.group(3));

    List<Events> events = calendar.getEvents(sourceDate);
    Calendar targetCalendar = manager.fetchCal(targetCalName);
    ZoneId sourceZone = calendar.getTimeZone();
    ZoneId targetZone = targetCalendar.getTimeZone();

    for (Events event : events) {
      ZonedDateTime start = event.getStartTime().atZone(sourceZone).withZoneSameInstant(targetZone);
      LocalTime localTime = start.toLocalTime();
      LocalDateTime localDateTime = targetDate.atTime(localTime);
      Duration duration = Duration.between(event.getStartTime(), event.getEndTime());
      LocalDateTime newEndTime = localDateTime.plus(duration);
      Events copiedEvent = createEventCopyWithEnd(event, localDateTime, newEndTime);
      try {
        targetCalendar.addEvent(copiedEvent);
      } catch (IllegalArgumentException e) {
        // ignore duplicates
      }
    }
    manager.fetchCal(calendar.getName());
    String successMsg = String.format("Copied events from %s to %s",
        calendar.getName(), targetCalName);
    view.dispSuccess(successMsg);
  }

  /**
   * Function to copy all events between two dates to a particular calendar.
   *
   * @param manager  instance of the calendar manager
   * @param calendar instance of calendar in use
   * @param view     instance of the view console
   */
  private void copyEventsBetween(CalenderManager manager, Calendar calendar, ViewConsole view) {

    Pattern pattern = Pattern.compile("copy events between (\\S+) and (\\S+) --target (\\S+) "
        + "to (\\S+)");
    Matcher matcher = pattern.matcher(textInput);

    if (!matcher.find()) {
      view.dispError("Invalid copy events command");
      return;
    }

    LocalDate startDate = LocalDate.parse(matcher.group(1));
    LocalDate endDate = LocalDate.parse(matcher.group(2));
    String targetCalName = matcher.group(3);
    LocalDate targetDate = LocalDate.parse(matcher.group(4));
    LocalDateTime startDateTime = startDate.atStartOfDay();
    LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
    List<Events> events = calendar.getEventsBetween(startDateTime, endDateTime);
    Calendar targetCal = manager.fetchCal(targetCalName);
    ZoneId sourceZone = calendar.getTimeZone();
    ZoneId targetZone = targetCal.getTimeZone();

    for (Events event : events) {
      // took help from Google for Days between two DateTime
      long dayDiff = ChronoUnit.DAYS.between(startDate, event.getStartTime().toLocalDate());
      LocalDate newDate = targetDate.plusDays(dayDiff);
      ZonedDateTime sourceZoneStart = event.getStartTime().atZone(sourceZone);
      ZonedDateTime sourceZoneEnd = event.getEndTime().atZone(sourceZone);
      ZonedDateTime destZoneStart = sourceZoneStart.withZoneSameInstant(targetZone);
      ZonedDateTime destZoneEnd = sourceZoneEnd.withZoneSameInstant(targetZone);
      LocalTime translatedLocalTime = destZoneStart.toLocalTime();
      LocalDateTime newStartTime = newDate.atTime(translatedLocalTime);
      Duration duration = Duration.between(destZoneStart, destZoneEnd);
      LocalDateTime newEndTime = newStartTime.plus(duration);
      Events copiedEvent = createEventCopyWithEnd(event, newStartTime, newEndTime);
      try {
        targetCal.addEvent(copiedEvent);
      } catch (IllegalArgumentException e) {
        // skip duplicates
      }
    }
    manager.fetchCal(calendar.getName());
    String successMsg = String.format("Copied events from %s to %s",
        calendar.getName(), targetCalName);
    view.dispSuccess(successMsg);
  }

  /**
   * Helper function to create a copied event with given start and end time.
   *
   * @param event        the original event
   * @param newStartTime the new start time
   * @param newEndTime the new end time
   * @return instance of the copied event
   */
  private Events createEventCopyWithEnd(Events event, LocalDateTime newStartTime,
                                        LocalDateTime newEndTime) {
    Events copied;
    if (event.isAllDay()) {
      copied = new Events(event.getSubject(), newStartTime.toLocalDate());
    } else {
      copied = new Events(event.getSubject(), newStartTime, newEndTime);
    }

    copied.setDescription(event.getDescription());
    copied.setLocation(event.getLocation());
    copied.setStatus(event.getStatus().toString().toLowerCase());
    return copied;
  }
}