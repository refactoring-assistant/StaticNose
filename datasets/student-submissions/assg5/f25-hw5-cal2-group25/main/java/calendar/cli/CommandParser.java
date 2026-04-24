package calendar.cli;

import calendar.copy.CopyService;
import calendar.copy.CopyServiceImpl;
import calendar.model.CalendarModel;
import calendar.model.EditScope;
import calendar.model.EventSelector;
import calendar.model.EventSpec;
import calendar.model.PropertyChange;
import calendar.model.impl.Event;
import calendar.model.impl.EventId;
import calendar.model.impl.SeriesRule;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Parses user input lines into Command objects that can execute on the model.
 */
public class CommandParser {

  private static final Pattern CREATE_SINGLE =
      Pattern.compile(
          "^create event\\s+\"?(.*?)\"?\\s+from\\s+(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})\\"
              + "s+to\\s+(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})$",
          Pattern.CASE_INSENSITIVE);

  private static final Pattern CREATE_ALLDAY =
      Pattern.compile("^create event\\s+\"?(.*?)\"?\\s+on\\s+(\\d{4}-\\d{2}-\\d{2})$",
          Pattern.CASE_INSENSITIVE);

  private static final Pattern CREATE_ALLDAY_SERIES_FOR =
      Pattern.compile(
          "^create event\\s+\"?(.*?)\"?\\s+on\\s+(\\d{4}-\\d{2}-\\d{2})\\s+repeats"
              + "\\s+([MTWRFSU]+)\\s+for\\s+(\\d+)\\s+times$",
          Pattern.CASE_INSENSITIVE);

  private static final Pattern CREATE_ALLDAY_SERIES_UNTIL =
      Pattern.compile(
          "^create event\\s+\"?(.*?)\"?\\s+on\\s+(\\d{4}-\\d{2}-\\d{2})\\s+repeats"
              + "\\s+([MTWRFSU]+)\\s+until\\s+(\\d{4}-\\d{2}-\\d{2})$",
          Pattern.CASE_INSENSITIVE);

  private static final Pattern CREATE_SERIES_FOR =
      Pattern.compile(
          "^create event\\s+\"?(.*?)\"?\\s+from\\s+(\\S+)\\s+to\\s+(\\S+)\\s+repeats"
              + "\\s+([MTWRFSU]+)\\s+for\\s+(\\d+)\\s+times$",
          Pattern.CASE_INSENSITIVE);

  private static final Pattern CREATE_SERIES_UNTIL =
      Pattern.compile(
          "^create event\\s+\"?(.*?)\"?\\s+from\\s+(\\S+)\\s+to\\s+(\\S+)\\s+repeats"
              + "\\s+([MTWRFSU]+)\\s+until\\s+(\\d{4}-\\d{2}-\\d{2})$",
          Pattern.CASE_INSENSITIVE);

  // NEW: queries + status + export
  private static final Pattern PRINT_ON =
      Pattern.compile("^print\\s+events\\s+on\\s+(\\d{4}-\\d{2}-\\d{2})$",
          Pattern.CASE_INSENSITIVE);

  private static final Pattern PRINT_BETWEEN =
      Pattern.compile("^print\\s+events\\s+from\\s+(\\S+)\\s+to\\s+(\\S+)$",
          Pattern.CASE_INSENSITIVE);

  private static final Pattern STATUS_ON =
      Pattern.compile("^show\\s+status\\s+on\\s+(\\S+)$", Pattern.CASE_INSENSITIVE);

  private static final Pattern EXPORT_CAL =
      Pattern.compile("^export\\s+cal\\s+(.+\\.(csv|ics|ical))$", Pattern.CASE_INSENSITIVE);

  private static final Pattern EDIT_EVENT =
      Pattern.compile(
          "^edit\\s+event\\s+(subject|start|end|description|location|status)"
              + "\\s+\"(.*?)\"\\s+from\\s+(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})\\s+to"
              + "\\s+(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})\\s+with\\s+(.+)$",
          Pattern.CASE_INSENSITIVE);

  private static final Pattern EDIT_EVENTS =
      Pattern.compile(
          "^edit\\s+events\\s+(subject|start|end|description|location|status)"
              + "\\s+\"(.*?)\"\\s+from\\s+(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})\\s+with"
              + "\\s+\"?(.*?)\"?$",
          Pattern.CASE_INSENSITIVE);

  private static final Pattern EDIT_SERIES =
      Pattern.compile(
          "^edit\\s+series\\s+(subject|start|end|description|location|status)"
              + "\\s+\"(.*?)\"\\s+from\\s+(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})\\s+with\\s+(.+)$",
          Pattern.CASE_INSENSITIVE);

  private static final Pattern CREATE_CALENDAR =
      Pattern.compile("^create\\s+calendar\\s+--name\\s+(\\S+)\\s+--timezone\\s+(.+)$",
          Pattern.CASE_INSENSITIVE);

  private static final Pattern USE_CALENDAR =
      Pattern.compile("^use\\s+calendar\\s+--name\\s+(\\S+)$",
          Pattern.CASE_INSENSITIVE);

  private static final Pattern EDIT_CALENDAR =
      Pattern.compile(
          "^edit\\s+calendar\\s+--name\\s+(\\S+)\\s+--property\\s+(name|timezone)\\s+(.+)$",
          Pattern.CASE_INSENSITIVE);

  private static final Pattern COPY_EVENT =
      Pattern.compile(
          "^copy\\s+event\\s+\"?(.*?)\"?\\s+on\\s+(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})\\s+"
              + "--target\\s+(\\S+)\\s+to\\s+(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})$",
          Pattern.CASE_INSENSITIVE);

  private static final Pattern COPY_EVENTS_ON =
      Pattern.compile(
          "^copy\\s+events\\s+on\\s+(\\d{4}-\\d{2}-\\d{2})\\s+--target\\s+(\\S+)\\s+to\\s+"
              + "(\\d{4}-\\d{2}-\\d{2})$",
          Pattern.CASE_INSENSITIVE);

  private static final Pattern COPY_EVENTS_BETWEEN =
      Pattern.compile(
          "^copy\\s+events\\s+between\\s+(\\d{4}-\\d{2}-\\d{2})\\s+and\\s+(\\d{4}-\\d{2}-\\d"
              + "{2})\\s+--target\\s+(\\S+)\\s+to\\s+(\\d{4}-\\d{2}-\\d{2})$",
          Pattern.CASE_INSENSITIVE);

  private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

  /**
   * Parses a line of user input into an executable Command.
   *
   * @param line the raw user input string.
   * @return a Command representing the parsed operation
   */
  public Command parse(String line) {
    if (line == null) {
      throw new IllegalArgumentException("Command cannot be null");
    }
    String trimmed = line.trim();
    if (trimmed.isEmpty()) {
      throw new IllegalArgumentException("Empty command");
    }

    if (trimmed.equalsIgnoreCase("exit")) {
      return (model, out) -> {
      };
    }

    Matcher m;
    m = CREATE_SINGLE.matcher(trimmed);
    if (m.matches()) {
      return buildCreateSingle(m.group(1), m.group(2), m.group(3));
    }
    m = CREATE_ALLDAY.matcher(trimmed);
    if (m.matches()) {
      return buildCreateAllDay(m.group(1), m.group(2));
    }
    m = CREATE_SERIES_FOR.matcher(trimmed);
    if (m.matches()) {
      return buildCreateSeriesFor(m.group(1), m.group(2), m.group(3), m.group(4), m.group(5));
    }
    m = CREATE_SERIES_UNTIL.matcher(trimmed);
    if (m.matches()) {
      return buildCreateSeriesUntil(m.group(1), m.group(2), m.group(3), m.group(4), m.group(5));
    }
    m = CREATE_ALLDAY_SERIES_FOR.matcher(trimmed);
    if (m.matches()) {
      return buildCreateAllDaySeriesFor(m.group(1), m.group(2), m.group(3), m.group(4));
    }

    m = CREATE_ALLDAY_SERIES_UNTIL.matcher(trimmed);
    if (m.matches()) {
      return buildCreateAllDaySeriesUntil(m.group(1), m.group(2), m.group(3), m.group(4));
    }
    m = PRINT_ON.matcher(trimmed);
    if (m.matches()) {
      return buildPrintOn(m.group(1));
    }
    m = PRINT_BETWEEN.matcher(trimmed);
    if (m.matches()) {
      return buildPrintBetween(m.group(1), m.group(2));
    }
    m = STATUS_ON.matcher(trimmed);
    if (m.matches()) {
      return buildStatusOn(m.group(1));
    }
    m = EXPORT_CAL.matcher(trimmed);
    if (m.matches()) {
      return buildExportCal(m.group(1));
    }

    m = EDIT_EVENT.matcher(trimmed);
    if (m.matches()) {
      String property = m.group(1);
      String subject = m.group(2);
      String startStr = m.group(3);
      String endStr = m.group(4);
      String newValue = m.group(5);
      return buildEditEvent(property, subject, startStr, endStr, newValue);
    }

    m = EDIT_EVENTS.matcher(trimmed);
    if (m.matches()) {
      String property = m.group(1);
      String subject = m.group(2);
      String startStr = m.group(3);
      String newValue = m.group(4);
      return buildEditEvents(property, subject, startStr, newValue);
    }

    m = EDIT_SERIES.matcher(trimmed);
    if (m.matches()) {
      String property = m.group(1);
      String subject = m.group(2);
      String startStr = m.group(3);
      String newValue = m.group(4);
      return buildEditSeries(property, subject, startStr, newValue);
    }


    m = CREATE_CALENDAR.matcher(trimmed);
    if (m.matches()) {
      String name = m.group(1);
      String timezone = m.group(2);
      return buildCreateCalendar(name, timezone);
    }

    m = USE_CALENDAR.matcher(trimmed);
    if (m.matches()) {
      String name = m.group(1);
      return buildUseCalendar(name);
    }

    m = EDIT_CALENDAR.matcher(trimmed);
    if (m.matches()) {
      String name = m.group(1);
      String prop = m.group(2);
      String newVal = m.group(3);
      return buildEditCalendar(name, prop, newVal);
    }

    m = COPY_EVENT.matcher(trimmed);
    if (m.matches()) {
      return buildCopyEvent(m.group(1), m.group(2), m.group(3), m.group(4));
    }
    m = COPY_EVENTS_ON.matcher(trimmed);
    if (m.matches()) {
      return buildCopyEventsOn(m.group(1), m.group(2), m.group(3));
    }
    m = COPY_EVENTS_BETWEEN.matcher(trimmed);
    if (m.matches()) {
      return buildCopyEventsBetween(m.group(1), m.group(2), m.group(3), m.group(4));
    }


    throw new IllegalArgumentException("Unknown or invalid command: " + trimmed);
  }


  /**
   * Build select calendar command.
   */
  private static Command buildUseCalendar(String name) {
    return (manager, out) -> {
      try {
        manager.selectCalendar(name);
        out.println("[using calendar] " + name);
      } catch (Exception e) {
        out.println("Failed to use calendar: " + e.getMessage());
      }
    };
  }

  /**
   * Build calendar creation command.
   */
  private static Command buildCreateCalendar(String name, String timezone) {
    return (manager, out) -> {
      try {
        String tz = timezone.trim();
        java.time.ZoneId.of(tz);
        manager.createCalendar(name, new calendar.model.impl.CalendarModelImpl(tz));
        manager.selectCalendar(name);
        out.println("[created calendar] " + name + " with timezone " + tz);
      } catch (Exception e) {
        out.println("Failed to create calendar: " + e.getMessage());
      }
    };
  }

  /**
   * Build calendar edit command.
   */
  private static Command buildEditCalendar(String name, String property, String value) {
    return (manager, out) -> {
      try {
        if (property.equalsIgnoreCase("name")) {
          String newName = value.trim();
          manager.renameCalendar(name, newName);
          out.println("[calendar renamed] " + newName);
        } else if (property.equalsIgnoreCase("timezone")) {
          String tz = value.trim();
          java.time.ZoneId.of(tz);
          manager.getCalendar(name).setTimezone(tz);
          out.println("[timezone updated] " + tz);
        }
      } catch (Exception e) {
        out.println("Failed to edit calendar: " + e.getMessage());
      }
    };
  }

  /**
   * Parses a date-time string.
   *
   * @param s the string to parse
   * @return the parsed LocalDateTime
   */
  private static LocalDateTime parseDateTime(String s) {
    try {
      return LocalDateTime.parse(s);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid date-time: " + s);
    }
  }

  /**
   * Parses a date  string.
   *
   * @param s the string to parse
   * @return the parsed LocalDateTime
   */
  private static LocalDate parseDate(String s) {
    try {
      return LocalDate.parse(s);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid date: " + s);
    }
  }

  /**
   * Converts weekday abbreviations (MTWRF) into a Set of DayOfWeek.
   *
   * @param s string of weekday initials
   * @return set of corresponding days
   */
  private static Set<DayOfWeek> parseWeekdays(String s) {
    Set<DayOfWeek> set = new LinkedHashSet<>();
    char[] arr = s.toUpperCase().toCharArray();
    for (char c : arr) {
      switch (c) {
        case 'M':
          set.add(DayOfWeek.MONDAY);
          break;
        case 'T':
          set.add(DayOfWeek.TUESDAY);
          break;
        case 'W':
          set.add(DayOfWeek.WEDNESDAY);
          break;
        case 'R':
          set.add(DayOfWeek.THURSDAY);
          break;
        case 'F':
          set.add(DayOfWeek.FRIDAY);
          break;
        case 'S':
          set.add(DayOfWeek.SATURDAY);
          break;
        default:
          set.add(DayOfWeek.SUNDAY);
          break;
      }
    }
    return set;
  }

  /**
   * Builds a command to create a single non-all-day event.
   *
   * @param subject  the event subject or title.
   * @param startStr the start datetime string.
   * @param endStr   the end datetime string.
   * @return a Command that creates the event when executed
   */
  private static Command buildCreateSingle(final String subject, final String startStr,
                                           final String endStr) {
    return (manager, out) -> {
      CalendarModel model = manager.getSelectedCalendar();
      LocalDateTime start = parseDateTime(startStr);
      LocalDateTime end = parseDateTime(endStr);
      EventSpec spec = new EventSpec(subject, start, end, "", "",
          EventSpec.Status.PUBLIC, false);
      EventId id = model.createSingle(spec);
      out.println("[created single] id=" + id);
    };
  }

  /**
   * Builds a command to create an all-day event on a given date.
   *
   * @param subject the event subject or title.
   * @param dateStr the date string.
   * @return a Command that creates the all-day event when executed.
   */
  private static Command buildCreateAllDay(final String subject, final String dateStr) {
    return (manager, out) -> {
      CalendarModel model = manager.getSelectedCalendar();
      LocalDate date = parseDate(dateStr);
      LocalDateTime start = date.atTime(8, 0);
      LocalDateTime end = date.atTime(17, 0);
      EventSpec spec = new EventSpec(subject, start, end, "", "",
          EventSpec.Status.PUBLIC, true);
      EventId id = model.createSingle(spec);
      out.println("[created all-day] id=" + id);
    };
  }

  /**
   * Builds a command to create a recurring series of all-day events for a specified number
   * of occurrences.
   *
   * @param subject  the event subject or title
   * @param dateStr  the date string
   * @param weekdays a string of weekdays
   * @param ntimes   the number of times the event should recur
   * @return a Command that creates the all-day event series when executed
   */
  private static Command buildCreateAllDaySeriesFor(final String subject, final String dateStr,
                                                    final String weekdays, final String ntimes) {
    return (manager, out) -> {
      CalendarModel model = manager.getSelectedCalendar();
      LocalDate date = parseDate(dateStr);
      LocalDateTime start = date.atTime(8, 0);
      LocalDateTime end = date.atTime(17, 0);
      int n = Integer.parseInt(ntimes);
      Set<DayOfWeek> days = parseWeekdays(weekdays);
      SeriesRule rule = new SeriesRule(days, n, null, start.toLocalTime());
      EventSpec base = new EventSpec(subject, start, end, "", "",
          EventSpec.Status.PUBLIC, true);
      List<EventId> ids = model.createSeries(base, rule);
      out.println("[created all-day series " + ids.size() + " events]");
    };
  }

  /**
   * Builds a command to create a recurring series of all-day events that continues until a
   * given date.
   *
   * @param subject   the event subject or title
   * @param dateStr   the date string
   * @param weekdays  a string of weekdays
   * @param untilDate the end date for the series
   * @return a Command that creates the all-day event series when executed
   */
  private static Command buildCreateAllDaySeriesUntil(final String subject, final String dateStr,
                                                      final String weekdays,
                                                      final String untilDate) {
    return (manager, out) -> {
      CalendarModel model = manager.getSelectedCalendar();
      LocalDate date = parseDate(dateStr);
      LocalDateTime start = date.atTime(8, 0);
      LocalDateTime end = date.atTime(17, 0);
      LocalDate until = parseDate(untilDate);
      Set<DayOfWeek> days = parseWeekdays(weekdays);
      SeriesRule rule = new SeriesRule(days, null, until, start.toLocalTime());
      EventSpec base = new EventSpec(subject, start, end, "", "",
          EventSpec.Status.PUBLIC, true);
      List<EventId> ids = model.createSeries(base, rule);
      out.println("[created all-day series " + ids.size() + " events]");
    };
  }

  /**
   * Builds a command to create a recurring series of events for a specified number of occurrences.
   *
   * @param subject  the event subject or title.
   * @param startStr the start datetime string.
   * @param endStr   the end datetime string.
   * @param weekdays a string of weekdays.
   * @param ntimes   the number of times the event should recur.
   * @return a Command that creates the event series when executed
   */
  private static Command buildCreateSeriesFor(final String subject, final String startStr,
                                              final String endStr,
                                              final String weekdays, final String ntimes) {
    return (manager, out) -> {
      CalendarModel model = manager.getSelectedCalendar();
      LocalDateTime start = parseDateTime(startStr);
      LocalDateTime end = parseDateTime(endStr);
      int n = Integer.parseInt(ntimes);
      Set<DayOfWeek> days = parseWeekdays(weekdays);
      SeriesRule rule = new SeriesRule(days, n, null, start.toLocalTime());
      EventSpec base = new EventSpec(subject, start, end, "", "",
          EventSpec.Status.PUBLIC, false);
      List<EventId> ids = model.createSeries(base, rule);
      out.println("[created series " + ids.size() + " events]");
    };
  }

  /**
   * Builds a command to create a recurring series of events that continues until a given date.
   *
   * @param subject   the event subject or title
   * @param startStr  the start datetime string
   * @param endStr    the end datetime string
   * @param weekdays  a comma-separated list of weekdays
   * @param untilDate the end date for the series
   * @return a Command that creates the event series when executed
   */
  private static Command buildCreateSeriesUntil(final String subject, final String startStr,
                                                final String endStr,
                                                final String weekdays, final String untilDate) {
    return (manager, out) -> {
      CalendarModel model = manager.getSelectedCalendar();
      LocalDateTime start = parseDateTime(startStr);
      LocalDateTime end = parseDateTime(endStr);
      LocalDate until = parseDate(untilDate);
      Set<DayOfWeek> days = parseWeekdays(weekdays);
      SeriesRule rule = new SeriesRule(days, null, until, start.toLocalTime());
      EventSpec base = new EventSpec(subject, start, end, "", "",
          EventSpec.Status.PUBLIC, false);
      List<EventId> ids = model.createSeries(base, rule);
      out.println("[created series " + ids.size() + " events]");
    };
  }

  private static Command buildPrintOn(final String dateStr) {
    return (manager, out) -> {
      CalendarModel model = manager.getSelectedCalendar();
      LocalDate d = parseDate(dateStr);
      String tz = model.getTimezone();
      List<Event> list = model.eventsOn(d);
      if (list.isEmpty()) {
        out.println("No events on " + DATE_FMT.format(d) + " (" + tz + ")");
        return;
      }
      out.println("Events on " + DATE_FMT.format(d) + " (" + tz + "):");
      for (Event e : list) {
        String loc =
            (e.location().trim().isEmpty()) ? "" : (" @ " + e.location());
        out.println("->" + e.subject() + " "
            + TIME_FMT.format(e.start()) + "-" + TIME_FMT.format(e.end()) + loc);
      }
    };
  }

  private static Command buildPrintBetween(final String startStr, final String endStr) {
    return (manager, out) -> {
      CalendarModel model = manager.getSelectedCalendar();
      String tz = model.getTimezone();
      LocalDateTime a = parseDateTime(startStr);
      LocalDateTime b = parseDateTime(endStr);
      List<Event> list = model.eventsBetween(a, b);
      if (list.isEmpty()) {
        out.println("No events between " + a + " and " + b + " (" + tz + ")");
        return;
      }
      out.println("Events (" + tz + "):");
      for (Event e : list) {
        String loc =
            (e.location().trim().isEmpty()) ? "" : (" @ " + e.location());
        out.println(e.subject()
            + " starting on " + DATE_FMT.format(e.start()) + " at " + TIME_FMT.format(e.start())
            + ", ending on " + DATE_FMT.format(e.end()) + " at " + TIME_FMT.format(e.end())
            + loc);
      }
    };
  }

  /**
   * Builds a command to check calendar status at a specific datetime.
   *
   * @param dateTimeStr the datetime string
   * @return a Command that checks if the calendar is busy or available
   */
  private static Command buildStatusOn(final String dateTimeStr) {
    return (manager, out) -> {
      CalendarModel model = manager.getSelectedCalendar();
      LocalDateTime t = parseDateTime(dateTimeStr);
      boolean busy = model.isBusy(t);
      out.println(busy ? "busy" : "available");
    };
  }

  /**
   * Builds a command to export the calendar to a CSV file.
   *
   * @param filename the name or path of the output CSV file.
   * @return a Command that exports the calendar when executed.
   */
  private static Command buildExportCal(final String filename) {
    return (manager, out) -> {
      try {
        var model = manager.getSelectedCalendar();
        Path path = Paths.get(filename).toAbsolutePath().normalize();
        String lower = filename.toLowerCase(Locale.ROOT);


        calendar.model.Exporter exporter;
        if (lower.endsWith(".csv")) {
          exporter = new calendar.export.CsvExporter();
        } else if (lower.endsWith(".ics") || lower.endsWith(".ical")) {
          exporter = new calendar.export.IcalExporter();
        } else {
          throw new IllegalArgumentException("Unsupported export format: " + filename);
        }
        model.export(exporter, path);
        out.println(path);
      } catch (Exception e) {
        out.println("Export failed: " + e.getMessage());
      }

    };
  }

  /**
   * Builds a command to edit a single event instance’s property.
   *
   * @param property    the property name to edit.
   * @param subject     the event subject
   * @param startStr    the event start datetime
   * @param endStr      the event end datetime
   * @param rawNewValue the new value
   * @return Command that applies the edit when executed
   */
  private static Command buildEditEvent(final String property, final String subject,
                                        final String startStr, final String endStr,
                                        final String rawNewValue) {
    String val = stripQuotes(rawNewValue);
    if (val.isBlank()) {
      throw new IllegalArgumentException("rawNewValue must not be empty");
    }
    return (manager, out) -> {
      CalendarModel model = manager.getSelectedCalendar();
      try {
        LocalDateTime start = parseDateTime(startStr);
        LocalDateTime end = parseDateTime(endStr);
        EventSelector selector = new EventSelector(subject, start, end);
        PropertyChange change = parsePropertyChange(property, val);
        model.edit(selector, EditScope.INSTANCE, change);
      } catch (Exception e) {
        out.println("Edit failed: " + e.getMessage());
      }
    };
  }

  /**
   * Builds a command to edit all instances of an event starting at the specified time.
   *
   * @param property    the property name to edit
   * @param subject     the event subject
   * @param startStr    the start datetime string
   * @param rawNewValue the new value to apply
   * @return a Command that applies the edit when executed
   */
  private static Command buildEditEvents(final String property, final String subject,
                                         final String startStr, final String rawNewValue) {
    String val = stripQuotes(rawNewValue);
    if (val.isBlank()) {
      throw new IllegalArgumentException("rawNewValue must not be empty");
    }
    return (manager, out) -> {
      CalendarModel model = manager.getSelectedCalendar();
      try {
        LocalDateTime start = parseDateTime(startStr);
        EventSelector selector = new EventSelector(subject, start, null);
        PropertyChange change = parsePropertyChange(property, val);
        model.edit(selector, EditScope.FROM_HERE, change);
        // no output on success
      } catch (Exception e) {
        out.println("Edit failed: " + e.getMessage());
      }
    };
  }

  /**
   * Builds a command to edit all instances of a series.
   *
   * @param property    the property name to edit
   * @param subject     the event subject
   * @param startStr    the start datetime string
   * @param rawNewValue the new value to apply
   * @return a Command that applies the edit when executed
   */
  private static Command buildEditSeries(final String property, final String subject,
                                         final String startStr, final String rawNewValue) {
    String val = stripQuotes(rawNewValue);
    if (val.isBlank()) {
      throw new IllegalArgumentException("rawNewValue must not be empty");
    }
    return (manager, out) -> {
      CalendarModel model = manager.getSelectedCalendar();
      try {
        LocalDateTime start = parseDateTime(startStr);
        EventSelector selector = new EventSelector(subject, start, null);
        PropertyChange change = parsePropertyChange(property, val);
        model.edit(selector, EditScope.WHOLE_SERIES, change);
      } catch (Exception e) {
        out.println("Edit failed: " + e.getMessage());
      }
    };
  }

  /**
   * Removes surrounding quotes from a string, if present.
   *
   * @param s the input string
   * @return the unquoted and trimmed string
   */
  private static String stripQuotes(String s) {
    String t = s.trim();
    if (t.length() >= 2 && t.startsWith("\"") && t.endsWith("\"")) {
      return t.substring(1, t.length() - 1);
    }
    return t;
  }

  /**
   * Parses a property name and new value into a PropertyChange instance.
   *
   * @param property the name of the property to change
   * @param val      the new value as a string
   * @return a PropertyChange representing the requested edit
   */
  private static PropertyChange parsePropertyChange(String property, String val) {
    String prop = property.trim().toLowerCase(Locale.ROOT);
    switch (prop) {
      case "description":
        return PropertyChange.description(val);
      case "location":
        return PropertyChange.location(val);
      case "status":
        return PropertyChange.status(val);
      case "start":
        return PropertyChange.start(parseDateTime(val));
      case "end":
        return PropertyChange.end(parseDateTime(val));
      default:
        return PropertyChange.subject(val);
    }
  }


  private static Command buildCopyEvent(String subject, String srcStartStr,
                                        String targetCalName, String dstStartStr) {
    return (manager, out) -> {
      try {
        CopyService svc = new CopyServiceImpl();
        var src = manager.getSelectedCalendar();
        var dst = manager.getCalendar(targetCalName);
        var srcStart = parseDateTime(srcStartStr);
        var dstStart = parseDateTime(dstStartStr);
        svc.copyEvent(src, subject, srcStart, dst, dstStart);
        out.println(
            "[copied] \"" + subject + "\" to " + targetCalName + " at " + dstStart);
      } catch (Exception e) {
        out.println("Copy failed: " + e.getMessage());
      }
    };
  }

  private static Command buildCopyEventsOn(String srcDateStr,
                                           String targetCalName, String dstDateStr) {
    return (manager, out) -> {
      try {
        CopyService svc = new CopyServiceImpl();
        var src = manager.getSelectedCalendar();
        var dst = manager.getCalendar(targetCalName);
        var srcDate = parseDate(srcDateStr);
        var dstDate = parseDate(dstDateStr);
        svc.copyEventsOnDate(src, srcDate, dst, dstDate);
        out.println("[copied events on] " + srcDate + " → " + targetCalName + "@" + dstDate);
      } catch (Exception e) {
        out.println("Copy failed: " + e.getMessage());
      }
    };
  }

  private static Command buildCopyEventsBetween(String fromStr, String toStr,
                                                String targetCalName, String dstStartStr) {
    return (manager, out) -> {
      try {
        CopyService svc = new CopyServiceImpl();
        var src = manager.getSelectedCalendar();
        var dst = manager.getCalendar(targetCalName);
        var fromDate = parseDate(fromStr);
        var toDate = parseDate(toStr);
        var dstStartDate = parseDate(dstStartStr);
        svc.copyEventsBetween(src, fromDate, toDate, dst, dstStartDate);
        out.println("[copied events between] " + fromDate + ".." + toDate
            + " → " + targetCalName + "@" + dstStartDate);
      } catch (Exception e) {
        out.println("Copy failed: " + e.getMessage());
      }
    };
  }


}