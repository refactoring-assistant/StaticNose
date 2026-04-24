package calendar.controller;

import calendar.model.Icalendar;
import calendar.model.Ievent;
import calendar.view.IcalendarView;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Controller for the calendar application.
 * Handles parsing commands and coordinating between model and view.
 * This class follows the Single Responsibility Principle by focusing solely
 * on command interpretation and coordination.
 */
public class CalendarController {
  private final Icalendar model;
  private final IcalendarView view;
  private final Scanner scanner;
  private boolean running;

  /**
   * Constructs a CalendarController.
   *
   * @param model    the calendar model
   * @param view     the calendar view
   * @param readable the input source
   */
  public CalendarController(Icalendar model, IcalendarView view, Readable readable) {
    if (model == null || view == null || readable == null) {
      throw new IllegalArgumentException("Model, view, and readable cannot be null");
    }
    this.model = model;
    this.view = view;
    this.scanner = new Scanner(readable);
    this.running = false;
  }

  /**
   * Runs the controller in interactive mode.
   */
  public void runInteractive() {
    running = true;
    view.displayMessage("Calendar application started. Type 'exit' to quit.");

    while (running && scanner.hasNextLine()) {
      view.displayPrompt();
      String command = scanner.nextLine().trim();

      if (!command.isEmpty()) {
        processCommand(command);
      }
    }
  }

  /**
   * Runs the controller in headless mode with commands from a file.
   *
   * @param commandsFile path to the commands file
   */
  public void runHeadless(String commandsFile) {
    running = true;
    boolean foundExit = false;

    while (running && scanner.hasNextLine()) {
      String command = scanner.nextLine().trim();

      if (!command.isEmpty()) {
        if (command.equalsIgnoreCase("exit")) {
          foundExit = true;
        }
        processCommand(command);
      }
    }

    if (!foundExit) {
      view.displayError("Error: Command file must end with 'exit' command");
    }
  }

  /**
   * Processes a single command.
   *
   * @param command the command string
   */
  private void processCommand(String command) {
    try {
      if (command.equalsIgnoreCase("exit")) {
        running = false;
        view.displayMessage("Exiting calendar application.");
        return;
      }

      if (command.startsWith("create event")) {
        handleCreateEvent(command);
      } else if (command.startsWith("edit event ")) {
        handleEditEvent(command);
      } else if (command.startsWith("edit events ")) {
        handleEditEvents(command);
      } else if (command.startsWith("edit series ")) {
        handleEditSeries(command);
      } else if (command.startsWith("print events on ")) {
        handlePrintEventsOnDate(command);
      } else if (command.startsWith("print events from ")) {
        handlePrintEventsInRange(command);
      } else if (command.startsWith("export cal ")) {
        handleExportCal(command);
      } else if (command.startsWith("show status on ")) {
        handleShowStatus(command);
      } else {
        view.displayError("Unknown command: " + command);
      }
    } catch (Exception e) {
      view.displayError("Error: " + e.getMessage());
    }
  }

  private void handleCreateEvent(String command) {
    // Try time-based patterns first
    if (tryTimeBasedEvent(command)) {
      return;
    }

    // Try all-day patterns
    if (tryAllDayEvent(command)) {
      return;
    }

    view.displayError("Invalid create event command format");
  }

  /**
   * Attempts to parse and create a time-based event.
   *
   * @param command the command string
   * @return true if successfully parsed and created, false otherwise
   */
  private boolean tryTimeBasedEvent(String command) {
    // Pattern for: create event "Subject" from YYYY-MM-DDThh:mm to YYYY-MM-DDThh:mm
    final Pattern timeBasedPattern = Pattern.compile(
        "create event \"([^\"]+)\" from (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) "
            + "to (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})(?: repeats ([MTWRFSU]+) "
            + "(?:for (\\d+) times|until (\\d{4}-\\d{2}-\\d{2})))?");

    // Pattern for: create event Subject from ... (single word subject)
    final Pattern timeBasedPatternSingle = Pattern.compile(
        "create event (\\S+) from (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) "
            + "to (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})(?: repeats ([MTWRFSU]+) "
            + "(?:for (\\d+) times|until (\\d{4}-\\d{2}-\\d{2})))?");

    Matcher m = timeBasedPattern.matcher(command);
    if (!m.matches()) {
      m = timeBasedPatternSingle.matcher(command);
    }

    if (m.matches()) {
      String subject = m.group(1);
      LocalDateTime start = parseDateTime(m.group(2));
      LocalDateTime end = parseDateTime(m.group(3));
      String weekdays = m.group(4);
      String occurrencesStr = m.group(5);
      String untilStr = m.group(6);

      if (weekdays != null) {
        if (occurrencesStr != null) {
          int occurrences = Integer.parseInt(occurrencesStr);
          model.createEventSeries(subject, start, end, weekdays, occurrences);
          view.displayMessage("Event series created successfully.");
        } else if (untilStr != null) {
          LocalDate until = LocalDate.parse(untilStr);
          model.createEventSeriesUntil(subject, start, end, weekdays, until);
          view.displayMessage("Event series created successfully.");
        }
      } else {
        model.createEvent(subject, start, end);
        view.displayMessage("Event created successfully.");
      }
      return true;
    }

    return false;
  }

  /**
   * Attempts to parse and create an all-day event.
   *
   * @param command the command string
   * @return true if successfully parsed and created, false otherwise
   */
  private boolean tryAllDayEvent(String command) {
    // Pattern for: create event "Subject" on YYYY-MM-DD
    final Pattern allDayPattern = Pattern.compile(
        "create event \"([^\"]+)\" on (\\d{4}-\\d{2}-\\d{2})"
            + "(?: repeats ([MTWRFSU]+) (?:for (\\d+) times|until (\\d{4}-\\d{2}-\\d{2})))?");

    // Pattern for: create event Subject on YYYY-MM-DD (single word)
    final Pattern allDayPatternSingle = Pattern.compile(
        "create event (\\S+) on (\\d{4}-\\d{2}-\\d{2})"
            + "(?: repeats ([MTWRFSU]+) (?:for (\\d+) times|until (\\d{4}-\\d{2}-\\d{2})))?");

    Matcher m = allDayPattern.matcher(command);
    if (!m.matches()) {
      m = allDayPatternSingle.matcher(command);
    }

    if (m.matches()) {
      String subject = m.group(1);
      LocalDate date = LocalDate.parse(m.group(2));
      String weekdays = m.group(3);
      String occurrencesStr = m.group(4);
      String untilStr = m.group(5);

      LocalDateTime start = date.atTime(8, 0);
      LocalDateTime end = date.atTime(17, 0);

      if (weekdays != null) {
        if (occurrencesStr != null) {
          int occurrences = Integer.parseInt(occurrencesStr);
          model.createEventSeries(subject, start, end, weekdays, occurrences);
          view.displayMessage("Event series created successfully.");
        } else if (untilStr != null) {
          LocalDate until = LocalDate.parse(untilStr);
          model.createEventSeriesUntil(subject, start, end, weekdays, until);
          view.displayMessage("Event series created successfully.");
        }
      } else {
        model.createEvent(subject, start, null);
        view.displayMessage("Event created successfully.");
      }
      return true;
    }

    return false;
  }

  private void handleEditEvent(String command) {
    final Pattern pattern = Pattern.compile(
        "edit event (\\w+) \"?([^\"]+?)\"? from (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) "
            + "(?:to (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) )?with (.+)");

    Matcher m = pattern.matcher(command);
    if (m.matches()) {
      String property = m.group(1);
      String subject = m.group(2).trim().replace("\"", "");
      LocalDateTime start = parseDateTime(m.group(3));
      String newValue = m.group(5).trim().replace("\"", "");

      model.editEvent(subject, start, property, newValue);
      view.displayMessage("Event updated successfully.");
    } else {
      view.displayError("Invalid edit event command format");
    }
  }

  private void handleEditEvents(String command) {
    final Pattern pattern = Pattern.compile(
        "edit events (\\w+) \"?([^\"]+?)\"? from (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) "
            + "with (.+)");

    Matcher m = pattern.matcher(command);
    if (m.matches()) {
      String property = m.group(1);
      String subject = m.group(2).trim().replace("\"", "");
      LocalDateTime start = parseDateTime(m.group(3));
      String newValue = m.group(4).trim().replace("\"", "");

      model.editEventsFrom(subject, start, property, newValue);
      view.displayMessage("Events updated successfully.");
    } else {
      view.displayError("Invalid edit events command format");
    }
  }

  private void handleEditSeries(String command) {
    final Pattern pattern = Pattern.compile(
        "edit series (\\w+) \"?([^\"]+?)\"? from (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) "
            + "with (.+)");

    Matcher m = pattern.matcher(command);
    if (m.matches()) {
      String property = m.group(1);
      String subject = m.group(2).trim().replace("\"", "");
      LocalDateTime start = parseDateTime(m.group(3));
      String newValue = m.group(4).trim().replace("\"", "");

      model.editSeries(subject, start, property, newValue);
      view.displayMessage("Series updated successfully.");
    } else {
      view.displayError("Invalid edit series command format");
    }
  }

  private void handlePrintEventsOnDate(String command) {
    final Pattern pattern = Pattern.compile("print events on (\\d{4}-\\d{2}-\\d{2})");
    Matcher m = pattern.matcher(command);

    if (m.matches()) {
      LocalDate date = LocalDate.parse(m.group(1));
      List<Ievent> events = model.getEventsOnDate(date);
      view.displayEventsOnDate(date, events);
    } else {
      view.displayError("Invalid print events on command format");
    }
  }

  private void handlePrintEventsInRange(String command) {
    final Pattern pattern = Pattern.compile(
        "print events from (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) "
            + "to (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})");
    Matcher m = pattern.matcher(command);

    if (m.matches()) {
      LocalDateTime start = parseDateTime(m.group(1));
      LocalDateTime end = parseDateTime(m.group(2));
      List<Ievent> events = model.getEventsInRange(start, end);
      view.displayEventsInRange(start, end, events);
    } else {
      view.displayError("Invalid print events from command format");
    }
  }

  private void handleExportCal(String command) {
    final Pattern pattern = Pattern.compile("export cal (.+\\.csv)");
    Matcher m = pattern.matcher(command);

    if (m.matches()) {
      String filename = m.group(1);
      String csv = model.exportToCsv();

      try {
        Path path = Paths.get(filename);
        Files.writeString(path, csv);
        Path absolutePath = path.toAbsolutePath();
        view.displayMessage("Calendar exported successfully to: " + absolutePath);
      } catch (IOException e) {
        view.displayError("Failed to export calendar: " + e.getMessage());
      }
    } else {
      view.displayError("Invalid export cal command format");
    }
  }

  private void handleShowStatus(String command) {
    final Pattern pattern = Pattern.compile("show status on (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})");
    Matcher m = pattern.matcher(command);

    if (m.matches()) {
      LocalDateTime dateTime = parseDateTime(m.group(1));
      boolean busy = model.isBusyAt(dateTime);
      view.displayStatus(dateTime, busy);
    } else {
      view.displayError("Invalid show status command format");
    }
  }

  private LocalDateTime parseDateTime(String dateTimeStr) {
    try {
      return LocalDateTime.parse(dateTimeStr,
          DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid date-time format: " + dateTimeStr);
    }
  }
}