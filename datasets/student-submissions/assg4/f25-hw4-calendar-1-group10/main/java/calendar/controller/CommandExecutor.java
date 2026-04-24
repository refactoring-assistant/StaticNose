package calendar.controller;

import calendar.model.CalendarModel;
import calendar.model.Event;
import calendar.model.EventSeries;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;


/**
 * Executes parsed user commands by interacting with the CalendarModel.
 */
public class CommandExecutor {

  private final CalendarModel model;

  /**
   * Creates CommandExecutor Object out of a Calendar Model.
   *
   * @param model Calendar Model
   */
  public CommandExecutor(CalendarModel model) {
    this.model = model;
  }

  /**
   * Executes a parsed command.
   *
   * @param tokens parsed tokens from CommandParser
   */
  public void execute(List<String> tokens) {
    if (tokens.isEmpty()) {
      System.out.println("Empty command.");
      return;
    }

    String action = tokens.get(0).toLowerCase();

    try {
      switch (action) {
        case "create":
          handleCreate(tokens);
          break;
        case "edit":
          handleEdit(tokens);
          break;
        case "print":
          handlePrint(tokens);
          break;
        case "export":
          handleExport(tokens);
          break;
        case "show":
          handleShow(tokens);
          break;
        default:
          System.out.println("Invalid command: " + action);
      }
    } catch (Exception e) {
      System.out.println("Error: " + e.getMessage());
    }
  }

  /**
   * Handles the create command to create an event.
   *
   * @param tokens the parsed command tokens, expected format: ["create", "event", "subject ..."]
   */
  private void handleCreate(List<String> tokens) {
    if (tokens.size() < 3 || !"event".equalsIgnoreCase(tokens.get(1))) {
      throw new IllegalArgumentException("Usage: create event <subject> ...");
    }

    String subject = tokens.get(2);
    if (subject.startsWith("\"") && subject.endsWith("\"")) {
      subject = subject.substring(1, subject.length() - 1);
    }

    if (tokens.contains("from")) {
      LocalDateTime start = LocalDateTime.parse(tokens.get(tokens.indexOf("from") + 1));
      LocalDateTime end = LocalDateTime.parse(tokens.get(tokens.indexOf("to") + 1));

      if (tokens.contains("repeats")) {
        // Handle recurring event
        String days = tokens.get(tokens.indexOf("repeats") + 1);
        Set<DayOfWeek> repeatDays = parseDays(days);

        if (tokens.contains("for")) {
          int count = Integer.parseInt(tokens.get(tokens.indexOf("for") + 1));
          EventSeries series = new EventSeries(
              new Event(subject, start, end, null, null, null, null),
              repeatDays,
              count
          );
          model.createSeries(series);
        } else if (tokens.contains("until")) {
          LocalDate until = LocalDate.parse(tokens.get(tokens.indexOf("until") + 1));
          EventSeries series = new EventSeries(
              new Event(subject, start, end, null, null, null, null),
              repeatDays,
              until
          );
          model.createSeries(series);
        }
      } else {
        // Single event
        model.createEvent(new Event(subject, start, end, null, null, null, null));
      }
    } else if (tokens.contains("on")) {
      LocalDate date = LocalDate.parse(tokens.get(tokens.indexOf("on") + 1));

      if (tokens.contains("repeats")) {
        String days = tokens.get(tokens.indexOf("repeats") + 1);
        Set<DayOfWeek> repeatDays = parseDays(days);

        if (tokens.contains("for")) {
          int count = Integer.parseInt(tokens.get(tokens.indexOf("for") + 1));
          EventSeries series = new EventSeries(
              Event.allDay(subject, date, null, null, null, null),
              repeatDays,
              count
          );
          model.createSeries(series);
        } else if (tokens.contains("until")) {
          LocalDate until = LocalDate.parse(tokens.get(tokens.indexOf("until") + 1));
          EventSeries series = new EventSeries(
              Event.allDay(subject, date, null, null, null, null),
              repeatDays,
              until
          );
          model.createSeries(series);
        }
      } else {
        model.createEvent(Event.allDay(subject, date, null, null, null, null));
      }
    } else {
      throw new IllegalArgumentException("Invalid create event syntax.");
    }
  }

  /**
   * Handles the edit command to edit an event.
   *
   * @param tokens the parsed command tokens, expected format: ["edit", "event|series",
   *               "property ..."]
   */
  private void handleEdit(List<String> tokens) {
    if (tokens.size() < 7) {
      throw new IllegalArgumentException("Usage: edit [event|events|series] <property> ...");
    }

    String mode = tokens.get(1).toLowerCase(); // event, events, series
    String property = tokens.get(2).toLowerCase();
    String subject = tokens.get(3);
    if (subject.startsWith("\"") && subject.endsWith("\"")) {
      subject = subject.substring(1, subject.length() - 1);
    }
    LocalDateTime start = LocalDateTime.parse(tokens.get(tokens.indexOf("from") + 1));
    String newValue = tokens.get(tokens.indexOf("with") + 1);

    model.editEvent(property, subject, start, newValue, mode);
  }

  /**
   * Handles the print command to print events on a given date.
   *
   * @param tokens the parsed command tokens, expected format: ["print", "events", "on", "date"]
   */
  private void handlePrint(List<String> tokens) {
    if (tokens.size() < 3) {
      throw new IllegalArgumentException("Usage: print events on <date>");
    }

    if ("events".equalsIgnoreCase(tokens.get(1)) && "on".equalsIgnoreCase(tokens.get(2))) {
      LocalDate date = LocalDate.parse(tokens.get(3));
      List<Event> events = model.getEventsOn(date);
      if (events.isEmpty()) {
        System.out.println("No events on " + date);
      } else {
        for (Event e : events) {
          System.out.println("• " + e);
        }
      }
    } else if ("events".equalsIgnoreCase(tokens.get(1)) && "from".equalsIgnoreCase(tokens.get(2))) {
      LocalDateTime start = LocalDateTime.parse(tokens.get(3));
      LocalDateTime end = LocalDateTime.parse(tokens.get(5));
      List<Event> events = model.getEventsBetween(start, end);
      for (Event e : events) {
        System.out.println("• " + e);
      }
    } else {
      throw new IllegalArgumentException("Invalid print syntax.");
    }
  }

  /**
   * Handles the export command to export calendar data to a CSV file.
   *
   * @param tokens the parsed command tokens, expected format: ["export", "cal", "filename.csv"]
   * @throws IOException              if there is an error writing to the file during export
   * @throws IllegalArgumentException if the command is not valid or the file name is invalid
   */
  private void handleExport(List<String> tokens) throws IOException {
    if (tokens.size() != 3 || !"cal".equalsIgnoreCase(tokens.get(1))) {
      throw new IllegalArgumentException("Usage: export cal <filename.csv>");
    }
    model.exportToCsv(tokens.get(2));
  }

  /**
   * Handles the show command to show the busy status of a given time.
   *
   * @param tokens the parsed command tokens, expected format: ["show", "status", "on",
   *               "dateTime"]
   * @throws IllegalArgumentException if the command is not valid or the date time is invalid
   */
  private void handleShow(List<String> tokens) {
    if (tokens.size() < 4 || !"status".equalsIgnoreCase(tokens.get(1))) {
      throw new IllegalArgumentException("Usage: show status on <dateTime>");
    }
    LocalDateTime dateTime = LocalDateTime.parse(tokens.get(3));
    boolean busy = model.isBusy(dateTime);
    System.out.println(busy ? "Busy" : "Available");
  }

  /**
   * Parses a string of weekday codes into a Set of DayOfWeek objects.
   *
   * @param code the weekday codes, e.g. "MTWRFSU"
   * @return the Set of DayOfWeek objects
   * @throws IllegalArgumentException if the code contains invalid characters
   */
  private Set<DayOfWeek> parseDays(String code) {
    Set<DayOfWeek> days = EnumSet.noneOf(DayOfWeek.class);
    for (char c : code.toUpperCase().toCharArray()) {
      switch (c) {
        case 'M':
          days.add(DayOfWeek.MONDAY);
          break;
        case 'T':
          days.add(DayOfWeek.TUESDAY);
          break;
        case 'W':
          days.add(DayOfWeek.WEDNESDAY);
          break;
        case 'R':
          days.add(DayOfWeek.THURSDAY);
          break;
        case 'F':
          days.add(DayOfWeek.FRIDAY);
          break;
        case 'S':
          days.add(DayOfWeek.SATURDAY);
          break;
        case 'U':
          days.add(DayOfWeek.SUNDAY);
          break;
        default:
          throw new IllegalArgumentException("Invalid weekday code: " + c);
      }
    }
    return days;
  }
}

