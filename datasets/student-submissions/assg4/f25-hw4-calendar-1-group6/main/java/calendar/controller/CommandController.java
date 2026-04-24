package calendar.controller;

import calendar.model.CalendarEvent;
import calendar.model.CalendarModel;
import calendar.model.EditScope;
import calendar.model.RecurrenceRule;
import calendar.util.DateTimeParser;
import calendar.util.Weekdays;
import calendar.view.ConsoleView;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;

/**
 * The Controller component. It is the middle person for both model and view.
 */
public final class CommandController {
  private final CalendarModel model;
  private final ConsoleView view;

  /**
   * Wires controller to model and view.
   *
   * @param model the calendar model
   * @param view  the console view
   */
  public CommandController(CalendarModel model, ConsoleView view) {
    this.model = model;
    this.view = view;
  }

  /**
   * Handles the 'create event with subject from start to end' command.
   *
   * @param subject The title of the event.
   * @param from The start date/time string (YYYY-MM-DDThh:mm).
   * @param to The end date/time string (YYYY-MM-DDThh:mm).
   */
  public void createSingle(String subject, String from, String to) {
    LocalDateTime s = DateTimeParser.parseDateTime(from);
    LocalDateTime e = DateTimeParser.parseDateTime(to);
    model.createSingleEvent(subject, s, e, null, null, null);
    view.println("Created event: " + subject);
  }

  /**
   * Creates an event series limited by a number of occurrences.
   *
   * @param subject The title of the series.
   * @param from The start date/time of the first event.
   * @param to The end date/time of the first event.
   * @param weekdays String sequence of days to repeat on.
   * @param times The number of times the series should occur.
   */
  public void createSeriesFor(String subject, String from, String to, String weekdays, int times) {
    LocalDateTime s = DateTimeParser.parseDateTime(from);
    LocalDateTime e = DateTimeParser.parseDateTime(to);
    EnumSet<java.time.DayOfWeek> wd = Weekdays.parse(weekdays);
    List<CalendarEvent> created =
        model.createSeriesByCount(subject, s, e, RecurrenceRule.forCount(wd, times), null, null,
            null);
    view.println("Created series (" + created.size() + "): " + subject);
  }

  /**
   * Creates an event series limited by an inclusive end date.
   *
   * @param subject The title of the series.
   * @param from The start date/time of the first event.
   * @param to The end date/time of the first event.
   * @param weekdays String sequence of days to repeat on.
   * @param untilDate The date string (YYYY-MM-DD) for the series' end date.
   */
  public void createSeriesUntil(String subject, String from, String to, String weekdays,
                                String untilDate) {
    LocalDateTime s = DateTimeParser.parseDateTime(from);
    LocalDateTime e = DateTimeParser.parseDateTime(to);
    EnumSet<java.time.DayOfWeek> wd = Weekdays.parse(weekdays);
    LocalDate until = DateTimeParser.parseDate(untilDate);
    List<CalendarEvent> created =
        model.createSeriesUntil(subject, s, e, RecurrenceRule.untilDate(wd, until), null, null,
            null);
    view.println("Created series (" + created.size() + "): " + subject);
  }

  /**
   * Creates a single all-day event.
   *
   * @param subject The title of the all-day event.
   * @param date The date string (YYYY-MM-DD) for the event.
   */
  public void createAllDay(String subject, String date) {
    LocalDate d = DateTimeParser.parseDate(date);
    model.createAllDayEvent(subject, d, null, null, null);
    view.println("Created all-day event: " + subject);
  }

  /**
   * Creates an all-day series limited by a number of occurrences.
   *
   * @param subject The title of the series.
   * @param date The date string for the first event's start day.
   * @param weekdays String sequence of days to repeat.
   * @param times The number of times the series should occur.
   */
  public void createAllDaySeriesFor(String subject, String date, String weekdays, int times) {
    LocalDate d0 = DateTimeParser.parseDate(date);
    EnumSet<java.time.DayOfWeek> wd = Weekdays.parse(weekdays);
    LocalDateTime s = d0.atTime(8, 0);
    LocalDateTime e = d0.atTime(17, 0);
    List<CalendarEvent> created =
        model.createSeriesByCount(subject, s, e, RecurrenceRule.forCount(wd, times), null, null,
            null);
    view.println("Created all-day series (" + created.size() + "): " + subject);
  }

  /**
   * Creates an all-day series limited by an inclusive end date.
   *
   * @param subject The title of the series.
   * @param date The date string for the first event's start day.
   * @param weekdays String sequence of days to repeat.
   * @param untilDate The date string for the series' end date.
   */
  public void createAllDaySeriesUntil(String subject, String date, String weekdays,
                                      String untilDate) {
    LocalDate d0 = DateTimeParser.parseDate(date);
    EnumSet<java.time.DayOfWeek> wd = Weekdays.parse(weekdays);
    LocalDateTime s = d0.atTime(8, 0);
    LocalDateTime e = d0.atTime(17, 0);
    LocalDate until = DateTimeParser.parseDate(untilDate);
    List<CalendarEvent> created =
        model.createSeriesUntil(subject, s, e, RecurrenceRule.untilDate(wd, until), null, null,
            null);
    view.println("Created all-day series (" + created.size() + "): " + subject);
  }

  /**
   * Edits only a single instance of an event (even if it's part of a series).
   *
   * @param property The field to change.
   * @param subject The original subject used to find the event.
   * @param from The original start date/time used to find the event.
   * @param to The original end date/time used to find the event.
   * @param newVal The new value for the specified property.
   */
  public void editSingle(String property, String subject, String from, String to, String newVal) {
    LocalDateTime s = DateTimeParser.parseDateTime(from);
    LocalDateTime e = DateTimeParser.parseDateTime(to);
    model.editEvent(property, subject, s, e, newVal, EditScope.SINGLE);
    view.println("Edited (single): " + subject + " " + property);
  }

  /**
   * Edits the specified event and all subsequent events in its series.
   * If not a series, acts like editSingle.
   *
   * @param property The field to change.
   * @param subject The original subject used to find the event.
   * @param from The original start date/time used to find the event.
   * @param newVal The new value for the specified property.
   */
  public void editFrom(String property, String subject, String from, String newVal) {
    LocalDateTime s = DateTimeParser.parseDateTime(from);
    List<CalendarEvent> candidates = model.eventsBetween(s, s);
    CalendarEvent match =
        candidates.stream().filter(ev -> ev.subject().equals(subject) && ev.start().equals(s))
            .findFirst().orElseThrow(() -> new IllegalArgumentException(
                "No event found with subject and start for edit-from."));
    model.editEvent(property, subject, s, match.end(), newVal, EditScope.FROM_THIS);
    view.println("Edited (from this): " + subject + " " + property);
  }

  /**
   * Edits all events in the entire series.
   * If not a series, acts like editSingle.
   *
   * @param property The field to change.
   * @param subject The original subject used to find the event.
   * @param from The original start date/time used to find the event.
   * @param newVal The new value for the specified property.
   */
  public void editSeries(String property, String subject, String from, String newVal) {
    LocalDateTime s = DateTimeParser.parseDateTime(from);
    List<CalendarEvent> candidates = model.eventsBetween(s, s);
    CalendarEvent match =
        candidates.stream().filter(ev -> ev.subject().equals(subject) && ev.start().equals(s))
            .findFirst().orElseThrow(() -> new IllegalArgumentException(
                "No event found with subject and start for edit-series."));
    model.editEvent(property, subject, s, match.end(), newVal, EditScope.ENTIRE_SERIES);
    view.println("Edited (entire series): " + subject + " " + property);
  }

  /**
   * Gets events from the model and prints them in a list format.
   *
   * @param date The date string to query events for.
   */
  public void printOn(String date) {
    List<CalendarEvent> evs = model.eventsOn(DateTimeParser.parseDate(date));
    view.printBulletedEvents(evs);
  }

  /**
   * Gets events from the model and prints them in a single-line interval format.
   *
   * @param from The start date/time string.
   * @param to The end date/time string. Both inclusive.
   */
  public void printBetween(String from, String to) {
    List<CalendarEvent> evs =
        model.eventsBetween(DateTimeParser.parseDateTime(from), DateTimeParser.parseDateTime(to));
    view.printOneLineInterval(evs);
  }

  /**
   * Calls the model to write the data, then prints the resulting absolute file path.
   *
   * @param fileName The output filename string.
   */
  public void exportCsv(String fileName) {
    Path path = Paths.get(fileName);
    Path out = model.exportCsv(path);
    view.printExportPath(out);
  }

  /**
   * Calls the model to check for any event overlap and prints "busy" or "available".
   *
   * @param at The specific date/time string to check.
   */
  public void showStatus(String at) {
    LocalDateTime t = DateTimeParser.parseDateTime(at);
    boolean busy = model.isBusy(t);
    view.printBusy(t, busy);
  }
}
