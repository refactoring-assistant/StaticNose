package calendar.controller;

import calendar.model.CalendarSystem;
import calendar.model.Event;
import calendar.model.Icalendar;
import calendar.model.Ievent;
import calendar.view.CsvExporter;
import calendar.view.IcalExporter;
import calendar.view.IcalendarView;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Controller for the calendar application.
 */
public class CalendarController {
  private final CalendarSystem system;
  private final IcalendarView view;
  private final BufferedReader reader;

  /**
   * javadoc.
   *
   * @param system values.
   * @param view view.
   * @param reader reader.
   */
  public CalendarController(CalendarSystem system, IcalendarView view,
                            BufferedReader reader) {
    this.system = system;
    this.view = view;
    this.reader = reader;
  }

  /**
   * Starts the controller and processes commands.
   */
  public void start() {
    view.displayMessage("Welcome to Calendar Application");
    view.displayMessage("Type 'help' for available commands or 'quit' to exit");

    try {
      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (line.isEmpty()) {
          continue;
        }
        if (line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit")) {
          view.displayMessage("Goodbye!");
          break;
        }
        processCommand(line);
      }
    } catch (IOException e) {
      view.displayError("Error reading input: " + e.getMessage());
    }
  }

  /**
   * Processes a single command.
   */
  public void processCommand(String commandLine) {
    try {
      String[] parts = parseCommand(commandLine);
      if (parts.length == 0) {
        return;
      }

      String command = parts[0].toLowerCase();

      switch (command) {
        case "create":
          handleCreate(parts);
          break;
        case "edit":
          handleEdit(parts);
          break;
        case "use":
          handleUse(parts);
          break;
        case "add":
          handleAddEvent(parts);
          break;
        case "copy":
          handleCopy(parts);
          break;
        case "export":
          handleExport(parts);
          break;
        case "list":
          handleList(parts);
          break;
        case "help":
          displayHelp();
          break;
        default:
          view.displayError("Unknown command: " + command);
      }
    } catch (Exception e) {
      view.displayError(e.getMessage());
    }
  }

  private void handleCreate(String[] parts) {
    if (parts.length < 2 || !parts[1].equals("calendar")) {
      view.displayError("Usage: create calendar --name <name> --timezone <timezone>");
      return;
    }

    String name = null;
    String timezone = null;

    for (int i = 2; i < parts.length; i++) {
      if (parts[i].equals("--name") && i + 1 < parts.length) {
        name = parts[++i];
      } else if (parts[i].equals("--timezone") && i + 1 < parts.length) {
        timezone = parts[++i];
      }
    }

    if (name == null || timezone == null) {
      view.displayError("Both --name and --timezone are required");
      return;
    }

    try {
      ZoneId zone = ZoneId.of(timezone);
      system.createCalendar(name, zone);
      view.displayMessage("Calendar '" + name + "' created successfully");
    } catch (Exception e) {
      view.displayError("Failed to create calendar: " + e.getMessage());
    }
  }

  private void handleEdit(String[] parts) {
    if (parts.length < 2 || !parts[1].equals("calendar")) {
      view.displayError("Usage: edit calendar --name <name> --property <prop> <value>");
      return;
    }

    String name = null;
    String property = null;
    String value = null;

    for (int i = 2; i < parts.length; i++) {
      if (parts[i].equals("--name") && i + 1 < parts.length) {
        name = parts[++i];
      } else if (parts[i].equals("--property") && i + 2 < parts.length) {
        property = parts[++i];
        value = parts[++i];
      }
    }

    if (name == null || property == null || value == null) {
      view.displayError("--name, --property and value are required");
      return;
    }

    system.editCalendar(name, property, value);
    view.displayMessage("Calendar '" + name + "' updated successfully");
  }

  private void handleUse(String[] parts) {
    if (parts.length < 3 || !parts[1].equals("calendar") || !parts[2].equals("--name")) {
      view.displayError("Usage: use calendar --name <name>");
      return;
    }

    if (parts.length < 4) {
      view.displayError("Calendar name is required");
      return;
    }

    String name = parts[3];
    system.useCalendar(name);
    view.displayMessage("Now using calendar: " + name);
  }

  private void handleAddEvent(String[] parts) {
    if (system.getCurrentCalendar() == null) {
      view.displayError("No calendar in use. Use 'use calendar --name <name>' first");
      return;
    }

    // Simple implementation: add event --name <name> --start <datetime> --end <datetime>
    String name = null;
    String startStr = null;
    String endStr = null;

    for (int i = 1; i < parts.length; i++) {
      if (parts[i].equals("--name") && i + 1 < parts.length) {
        name = parts[++i];
      } else if (parts[i].equals("--start") && i + 1 < parts.length) {
        startStr = parts[++i];
      } else if (parts[i].equals("--end") && i + 1 < parts.length) {
        endStr = parts[++i];
      }
    }

    if (name == null || startStr == null || endStr == null) {
      view.displayError("--name, --start, and --end are required");
      return;
    }

    Icalendar calendar = system.getCurrentCalendar();
    ZonedDateTime start = parseDateTime(startStr, calendar.getTimezone());
    ZonedDateTime end = parseDateTime(endStr, calendar.getTimezone());

    Ievent event = new Event(name, start, end, "");
    calendar.addEvent(event);
    view.displayMessage("Event '" + name + "' added successfully");
  }

  private void handleCopy(String[] parts) {
    if (system.getCurrentCalendar() == null) {
      view.displayError("No calendar in use");
      return;
    }

    if (parts.length < 2) {
      view.displayError("Invalid copy command");
      return;
    }

    if (parts[1].equals("event")) {
      handleCopyEvent(parts);
    } else if (parts[1].equals("events")) {
      handleCopyEvents(parts);
    } else {
      view.displayError("Unknown copy command");
    }
  }

  private void handleCopyEvent(String[] parts) {
    // copy event <name> on <datetime> --target <calendar> to <datetime>
    String eventName = null;
    String sourceDateTime = null;
    String targetCalendar = null;
    String targetDateTime = null;

    for (int i = 2; i < parts.length; i++) {
      if (parts[i].equals("on") && i + 1 < parts.length) {
        if (eventName == null && i > 2) {
          eventName = parts[i - 1];
        }
        sourceDateTime = parts[++i];
      } else if (parts[i].equals("--target") && i + 1 < parts.length) {
        targetCalendar = parts[++i];
      } else if (parts[i].equals("to") && i + 1 < parts.length) {
        targetDateTime = parts[++i];
      }
    }

    if (eventName == null || sourceDateTime == null
        || targetCalendar == null || targetDateTime == null) {
      view.displayError("Invalid copy event syntax");
      return;
    }

    Icalendar source = system.getCurrentCalendar();
    Icalendar target = system.getCalendar(targetCalendar);

    if (target == null) {
      view.displayError("Target calendar does not exist");
      return;
    }

    LocalDate sourceDate = LocalDate.parse(sourceDateTime.substring(0, 10));
    Ievent event = source.findEvent(eventName, sourceDate);

    if (event == null) {
      view.displayError("Event not found");
      return;
    }

    ZonedDateTime targetStart = parseDateTime(targetDateTime, target.getTimezone());
    Ievent copiedEvent = event.copyToNewStart(targetStart);
    target.addEvent(copiedEvent);
    view.displayMessage("Event copied successfully");
  }

  private void handleCopyEvents(String[] parts) {
    // copy events on <date> --target <calendar> to <date>
    // copy events between <date> and <date> --target <calendar> to <date>
    String sourceStart = null;
    String sourceEnd = null;
    String targetCalendar = null;
    String targetDate = null;
    boolean isRange = false;

    for (int i = 2; i < parts.length; i++) {
      if (parts[i].equals("on") && i + 1 < parts.length) {
        sourceStart = parts[++i];
        sourceEnd = sourceStart;
      } else if (parts[i].equals("between") && i + 1 < parts.length) {
        sourceStart = parts[++i];
        isRange = true;
      } else if (parts[i].equals("and") && i + 1 < parts.length && isRange) {
        sourceEnd = parts[++i];
      } else if (parts[i].equals("--target") && i + 1 < parts.length) {
        targetCalendar = parts[++i];
      } else if (parts[i].equals("to") && i + 1 < parts.length) {
        targetDate = parts[++i];
      }
    }

    if (sourceStart == null || targetCalendar == null || targetDate == null) {
      view.displayError("Invalid copy events syntax");
      return;
    }

    Icalendar source = system.getCurrentCalendar();
    Icalendar target = system.getCalendar(targetCalendar);

    if (target == null) {
      view.displayError("Target calendar does not exist");
      return;
    }

    LocalDate startDate = LocalDate.parse(sourceStart);
    LocalDate endDate = sourceEnd != null ? LocalDate.parse(sourceEnd) : startDate;
    LocalDate targetStartDate = LocalDate.parse(targetDate);

    List<Ievent> eventsToCopy = source.getEventsInRange(startDate, endDate);
    long dayOffset = ChronoUnit.DAYS.between(startDate, targetStartDate);

    for (Ievent event : eventsToCopy) {
      List<ZonedDateTime> occurrences = event.getOccurrencesInRange(startDate, endDate);
      for (ZonedDateTime occurrence : occurrences) {
        ZonedDateTime newStart = occurrence.plusDays(dayOffset)
            .withZoneSameInstant(target.getTimezone());
        Ievent copied = event.copyToNewStart(newStart);
        target.addEvent(copied);
      }
    }

    view.displayMessage("Events copied successfully (" + eventsToCopy.size() + " events)");
  }

  private void handleExport(String[] parts) {
    if (system.getCurrentCalendar() == null) {
      view.displayError("No calendar in use");
      return;
    }

    if (parts.length < 3) {
      view.displayError("Usage: export <calendar-name> <filename>");
      return;
    }

    String filename = parts[parts.length - 1];
    Path path = Paths.get(filename).toAbsolutePath();

    try {
      if (filename.toLowerCase().endsWith(".csv")) {
        CsvExporter exporter = new CsvExporter();
        exporter.export(system.getCurrentCalendar(), path);
      } else if (filename.toLowerCase().endsWith(".ical")) {
        IcalExporter exporter = new IcalExporter();
        exporter.export(system.getCurrentCalendar(), path);
      } else {
        view.displayError("Unknown file format. Use .csv or .ical extension");
        return;
      }
      view.displayMessage("Calendar exported to: " + path);
    } catch (IOException e) {
      view.displayError("Failed to export: " + e.getMessage());
    }
  }

  private void handleList(String[] parts) {
    if (system.getCurrentCalendar() == null) {
      view.displayError("No calendar in use");
      return;
    }

    List<Ievent> events = system.getCurrentCalendar().getEvents();
    if (events.isEmpty()) {
      view.displayMessage("No events in calendar");
    } else {
      view.displayMessage("Events in calendar:");
      for (Ievent event : events) {
        view.displayMessage("  - " + event.getName() + " at "
            + event.getStartDateTime());
      }
    }
  }

  private void displayHelp() {
    view.displayMessage("Available commands:");
    view.displayMessage("  create calendar --name <name> --timezone <tz>");
    view.displayMessage("  edit calendar --name <name> --property <prop> <value>");
    view.displayMessage("  use calendar --name <name>");
    view.displayMessage("  add event --name <name> --start <datetime> --end <datetime>");
    view.displayMessage("  copy event <name> on <datetime> --target <cal> to <datetime>");
    view.displayMessage("  copy events on <date> --target <cal> to <date>");
    view.displayMessage("  copy events between <date> and <date> --target <cal> to <date>");
    view.displayMessage("  export <calendar> <filename.csv|filename.ical>");
    view.displayMessage("  list - list all events");
    view.displayMessage("  quit - exit the program");
  }

  private String[] parseCommand(String commandLine) {
    return commandLine.split("\\s+");
  }

  private ZonedDateTime parseDateTime(String str, ZoneId timezone) {
    // Expected format: 2025-01-15T14:00:00
    DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    if (str.length() == 10) {
      // Just a date, add time
      str = str + "T00:00:00";
    }
    return ZonedDateTime.of(LocalDate.parse(str.substring(0, 10)),
        LocalTime.parse(str.substring(11)),
        timezone);
  }
}
