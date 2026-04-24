package calendar.model;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Implementation of CalendarModel interface that provides core calendar functionality.
 * Manages event creation, editing, querying, and export operations. Handles both
 * single events and recurring event series, enforces business rules such as preventing
 * duplicate events, and provides export capabilities in CSV and iCal formats.
 */
public class CalendarModelImpl implements CalendarModel {

  private final List<Event> events = new ArrayList<>();

  private static final DateTimeFormatter CSV_DATE =
      DateTimeFormatter.ofPattern("MM/dd/yyyy");
  private static final DateTimeFormatter CSV_TIME =
      DateTimeFormatter.ofPattern("HH:mm");

  @Override
  public void createEvent(String subject, LocalDateTime start, LocalDateTime end) {
    if (subject == null || subject.trim().isEmpty()) {
      throw new IllegalArgumentException("Subject cannot be empty.");
    }
    String normalizedSubject = normalizeSubject(subject);
    EventInterface e = new Event(normalizedSubject, start, end);
    ensureNoDuplicate((Event) e, null);
    events.add((Event) e);
  }

  @Override
  public void createAllDayEvent(String subject, LocalDate date) {
    if (subject == null || subject.trim().isEmpty()) {
      throw new IllegalArgumentException("Subject cannot be empty.");
    }
    String normalizedSubject = normalizeSubject(subject);
    LocalDateTime start = LocalDateTime.of(date, LocalTime.of(8, 0));
    LocalDateTime end = LocalDateTime.of(date, LocalTime.of(17, 0));
    EventInterface e = new Event(normalizedSubject, start, end);
    ensureNoDuplicate((Event) e, null);
    events.add((Event) e);
  }

  @Override
  public int createEventSeries(String subject, LocalDateTime start, LocalDateTime end,
                               Set<DayOfWeek> repeatDays, Integer occurrences,
                               LocalDate untilDate) {
    if (subject == null || subject.trim().isEmpty()) {
      throw new IllegalArgumentException("Subject cannot be empty.");
    }
    if (repeatDays == null || repeatDays.isEmpty()) {
      throw new IllegalArgumentException("Repeat days cannot be empty.");
    }

    String normalizedSubject = normalizeSubject(subject);
    EventInterface base = new Event(normalizedSubject, start, end);
    EventSeries series = new EventSeries();
    series.setBaseEvent((Event) base);
    series.setRepeatDays(repeatDays);
    if (occurrences != null) {
      series.setOccurrences(occurrences);
    }
    if (untilDate != null) {
      series.setEndDate(untilDate);
    }

    List<Event> instances = series.generateEvents();
    for (Event inst : instances) {
      ensureNoDuplicate(inst, null);
    }
    events.addAll(instances);
    return instances.size();
  }

  @Override
  public int createAllDayEventSeries(String subject, LocalDate startDate,
                                     Set<DayOfWeek> repeatDays, Integer occurrences,
                                     LocalDate untilDate) {
    LocalDateTime start = LocalDateTime.of(startDate, LocalTime.of(8, 0));
    LocalDateTime end = LocalDateTime.of(startDate, LocalTime.of(17, 0));
    return createEventSeries(subject, start, end, repeatDays, occurrences, untilDate);
  }

  @Override
  public void editEvent(String subject, LocalDateTime start, LocalDateTime end,
                       String property, String newValue) {
    Event anchor = findBySubjectStartEnd(subject, start, end);
    if (anchor == null) {
      throw new IllegalArgumentException("No matching event found.");
    }
    applyEditToEvent(anchor, property, newValue);
  }

  @Override
  public int editEventsFrom(String subject, LocalDateTime start,
                           String property, String newValue) {
    Event anchor = findBySubjectStart(subject, start);
    if (anchor == null) {
      throw new IllegalArgumentException("No matching event found.");
    }

    UUID sid = anchor.getSeriesId();
    if (sid == null) {
      applyEditToEvent(anchor, property, newValue);
      return 1;
    }

    List<Event> targets = new ArrayList<>();
    for (Event e : events) {
      if (sid.equals(e.getSeriesId()) && !e.startDate().isBefore(anchor.startDate())) {
        targets.add(e);
      }
    }
    detachIfBreakingSeries(property, targets, newValue);
    for (Event t : targets) {
      applyEditToEvent(t, property, newValue);
    }
    return targets.size();
  }

  @Override
  public int editSeries(String subject, LocalDateTime start,
                       String property, String newValue) {
    Event anchor = findBySubjectStart(subject, start);
    if (anchor == null) {
      throw new IllegalArgumentException("No matching event found.");
    }

    UUID sid = anchor.getSeriesId();
    if (sid == null) {
      applyEditToEvent(anchor, property, newValue);
      return 1;
    }

    List<Event> targets = findEventsBySeriesId(sid);
    if ("start".equals(property) || "end".equals(property)) {
      editSeriesTimeProperty(targets, anchor, property, newValue);
    } else {
      editSeriesNonTimeProperty(targets, property, newValue);
    }
    return targets.size();
  }

  /**
   * Finds all events that belong to the given series ID.
   *
   * @param seriesId the series ID to search for
   * @return list of events in the series
   */
  private List<Event> findEventsBySeriesId(UUID seriesId) {
    List<Event> targets = new ArrayList<>();
    for (Event e : events) {
      if (seriesId.equals(e.getSeriesId())) {
        targets.add(e);
      }
    }
    return targets;
  }

  /**
   * Edits a time property (start or end) for all events in a series.
   *
   * @param targets the events in the series to edit
   * @param anchor the anchor event used to calculate the time delta
   * @param property the property being edited ("start" or "end")
   * @param newValue the new value for the property
   */
  private void editSeriesTimeProperty(List<Event> targets, Event anchor,
                                      String property, String newValue) {
    if ("start".equals(property)) {
      editSeriesStartTime(targets, anchor, newValue);
    } else {
      editSeriesEndTime(targets, anchor, newValue);
    }
  }

  /**
   * Edits the start time for all events in a series.
   *
   * @param targets the events in the series
   * @param anchor the anchor event
   * @param newValue the new start time value
   */
  private void editSeriesStartTime(List<Event> targets, Event anchor, String newValue) {
    LocalDateTime newStart = LocalDateTime.parse(newValue);
    Duration delta = Duration.between(anchor.startDate(), newStart);
    for (Event t : targets) {
      LocalDateTime ns = t.startDate().plus(delta);
      LocalDateTime ne = t.endDate().plus(delta);
      checkNoDupOnChange(t, t.subject(), ns, ne);
    }
    for (Event t : targets) {
      LocalDateTime ns = t.startDate().plus(delta);
      LocalDateTime ne = t.endDate().plus(delta);
      t.setStart(ns);
      t.setEnd(ne);
    }
  }

  /**
   * Edits the end time for all events in a series.
   *
   * @param targets the events in the series
   * @param anchor the anchor event
   * @param newValue the new end time value
   */
  private void editSeriesEndTime(List<Event> targets, Event anchor, String newValue) {
    LocalDateTime newEnd = LocalDateTime.parse(newValue);
    Duration stretch = Duration.between(anchor.endDate(), newEnd);
    for (Event t : targets) {
      LocalDateTime ne = t.endDate().plus(stretch);
      checkNoDupOnChange(t, t.subject(), t.startDate(), ne);
    }
    for (Event t : targets) {
      LocalDateTime ne = t.endDate().plus(stretch);
      t.setEnd(ne);
    }
  }

  /**
   * Edits a non-time property for all events in a series.
   *
   * @param targets the events in the series
   * @param property the property to edit
   * @param newValue the new value for the property
   */
  private void editSeriesNonTimeProperty(List<Event> targets, String property, String newValue) {
    for (Event t : targets) {
      applyEditToEvent(t, property, newValue);
    }
  }

  @Override
  public List<Event> getEventsOn(LocalDate date) {
    List<Event> out = new ArrayList<>();
    for (Event e : events) {
      LocalDate s = e.startDate().toLocalDate();
      LocalDate t = e.endDate().toLocalDate();
      if (!s.isAfter(date) && !t.isBefore(date)) {
        out.add(e);
      }
    }
    out.sort(Comparator.comparing(Event::startDate));
    return out;
  }

  @Override
  public List<Event> getEventsBetween(LocalDateTime start, LocalDateTime end) {
    List<Event> out = new ArrayList<>();
    for (Event e : events) {
      if (!(e.endDate().isBefore(start) || e.startDate().isAfter(end))) {
        out.add(e);
      }
    }
    out.sort(Comparator.comparing(Event::startDate));
    return out;
  }

  @Override
  public boolean isBusyAt(LocalDateTime time) {
    for (Event e : events) {
      if (!time.isBefore(e.startDate()) && !time.isAfter(e.endDate())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Prepares the output path for export, handling test and production directories.
   *
   * @param filename the original filename
   * @return the prepared path
   */
  private Path prepareExportPath(String filename) {
    Path out = Paths.get(filename);

    if (isRunningInTest() && !out.isAbsolute()) {
      String filenameStr = filename.replace("\\", "/");
      if (!filenameStr.contains("test export")) {
        try {
          Path testExportDir = Paths.get("test export");
          Files.createDirectories(testExportDir);
          out = testExportDir.resolve(out.getFileName());
        } catch (IOException e) {
          // Directory might already exist
        }
      }
    } else if (!isRunningInTest() && !out.isAbsolute()) {
      String filenameStr = filename.replace("\\", "/");
      if (!filenameStr.contains("exports")) {
        try {
          Path exportsDir = Paths.get("exports");
          Files.createDirectories(exportsDir);
          out = exportsDir.resolve(out.getFileName());
        } catch (IOException e) {
          // Directory might already exist
        }
      }
    }
    
    return out.toAbsolutePath().normalize();
  }

  @Override
  public String exportToCsv(String filename) {
    Path out = prepareExportPath(filename);
    
    try (BufferedWriter w = Files.newBufferedWriter(out)) {
      writeCsvHeader(w);
      for (Event e : events) {
        String line = formatEventAsCsvLine(e);
        w.write(line);
        w.write("\n");
      }
    } catch (IOException ex) {
      throw new RuntimeException("Failed to export CSV: " + ex.getMessage(), ex);
    }
    return out.toString();
  }

  @Override
  public String exportToIcal(String filename, ZoneId timezone) {
    Path out = prepareExportPath(filename);
    
    try (BufferedWriter w = Files.newBufferedWriter(out)) {
      writeIcalHeader(w);
      for (Event e : events) {
        writeIcalEvent(w, e, timezone);
      }
      writeIcalFooter(w);
    } catch (IOException ex) {
      throw new RuntimeException("Failed to export iCal: " + ex.getMessage(), ex);
    }
    return out.toString();
  }

  /**
   * Writes the iCal header to the writer.
   *
   * @param writer the buffered writer to write to
   * @throws IOException if writing fails
   */
  private void writeIcalHeader(BufferedWriter writer) throws IOException {
    writer.write("BEGIN:VCALENDAR\r\n");
    writer.write("VERSION:2.0\r\n");
    writer.write("PRODID:-//Calendar Application//EN\r\n");
    writer.write("CALSCALE:GREGORIAN\r\n");
  }

  /**
   * Writes an event in iCal format to the writer.
   *
   * @param writer the buffered writer to write to
   * @param e the event to write
   * @param timezone the timezone for the event
   * @throws IOException if writing fails
   */
  private void writeIcalEvent(BufferedWriter writer, Event e, ZoneId timezone) throws IOException {
    writer.write("BEGIN:VEVENT\r\n");
    writer.write("UID:" + e.getId().toString() + "@calendar.app\r\n");
    writeIcalDateTime(writer, "DTSTART", e.startDate(), timezone, e.isAllDay());
    writeIcalDateTime(writer, "DTEND", e.endDate(), timezone, e.isAllDay());
    writer.write("SUMMARY:" + escapeIcalText(e.subject()) + "\r\n");
    if (e.description() != null && !e.description().isEmpty()) {
      writer.write("DESCRIPTION:" + escapeIcalText(e.description()) + "\r\n");
    }
    if (e.location() != null && !e.location().isEmpty()) {
      writer.write("LOCATION:" + escapeIcalText(e.location()) + "\r\n");
    }
    if (e.status() != null && e.status().equalsIgnoreCase("private")) {
      writer.write("CLASS:PRIVATE\r\n");
    } else {
      writer.write("CLASS:PUBLIC\r\n");
    }
    ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
    writer.write("DTSTAMP:" + now.format(
        DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")) + "\r\n");
    writer.write("END:VEVENT\r\n");
  }

  /**
   * Writes a date/time field in iCal format.
   *
   * @param writer the buffered writer to write to
   * @param fieldName the field name (DTSTART or DTEND)
   * @param dateTime the date and time to write
   * @param timezone the timezone for the date/time
   * @param isAllDay whether this is an all-day event
   * @throws IOException if writing fails
   */
  private void writeIcalDateTime(BufferedWriter writer, String fieldName,
                                 LocalDateTime dateTime, ZoneId timezone, boolean isAllDay)
      throws IOException {
    ZonedDateTime zoned = ZonedDateTime.of(dateTime, timezone);
    if (isAllDay) {
      writer.write(fieldName + ";VALUE=DATE:" + zoned.toLocalDate().format(
          DateTimeFormatter.ofPattern("yyyyMMdd")) + "\r\n");
    } else {
      ZonedDateTime utc = zoned.withZoneSameInstant(ZoneId.of("UTC"));
      writer.write(fieldName + ":" + utc.format(
          DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")) + "\r\n");
    }
  }

  /**
   * Writes the iCal footer to the writer.
   *
   * @param writer the buffered writer to write to
   * @throws IOException if writing fails
   */
  private void writeIcalFooter(BufferedWriter writer) throws IOException {
    writer.write("END:VCALENDAR\r\n");
  }

  /**
   * Escapes text for iCal format.
   * Replaces commas, semicolons, and backslashes with escaped versions.
   */
  private static String escapeIcalText(String text) {
    if (text == null) {
      return "";
    }
    return text.replace("\\", "\\\\")
        .replace(",", "\\,")
        .replace(";", "\\;")
        .replace("\n", "\\n")
        .replace("\r", "");
  }

  private void ensureNoDuplicate(Event candidate, Event skip) {
    for (Event e : events) {
      if (e == skip) {
        continue;
      }
      if (e.conflictsWith(candidate)) {
        throw new IllegalArgumentException(
            "Duplicate event (same subject, start, end) not allowed.");
      }
    }
  }

  private Event findBySubjectStartEnd(String subject, LocalDateTime start, LocalDateTime end) {
    String normalizedSubject = normalizeSubject(subject);
    for (Event e : events) {
      if (normalizeSubject(e.subject()).equalsIgnoreCase(normalizedSubject)
          && e.startDate().equals(start)
          && e.endDate().equals(end)) {
        return e;
      }
    }
    return null;
  }

  private Event findBySubjectStart(String subject, LocalDateTime start) {
    String normalizedSubject = normalizeSubject(subject);
    for (Event e : events) {
      if (normalizeSubject(e.subject()).equalsIgnoreCase(normalizedSubject)
          && e.startDate().equals(start)) {
        return e;
      }
    }
    return null;
  }

  /**
   * Normalizes a subject by trimming and removing surrounding quotes.
   * This ensures consistent matching regardless of how quotes are stored.
   *
   * @param subject the subject to normalize
   * @return the normalized subject without surrounding quotes
   */
  private static String normalizeSubject(String subject) {
    if (subject == null) {
      return "";
    }
    String normalized = subject.trim();
    if (normalized.startsWith("\"") && normalized.endsWith("\"") && normalized.length() >= 2) {
      normalized = normalized.substring(1, normalized.length() - 1);
    }
    return normalized;
  }

  private void applyEditToEvent(Event target, String prop, String newVal) {
    if ("subject".equals(prop)) {
      String normalizedSubject = normalizeSubject(newVal);
      checkNoDupOnChange(target, normalizedSubject, target.startDate(), target.endDate());
      target.setSubject(normalizedSubject);
      return;
    }
    if ("description".equals(prop)) {
      target.setDescription(normalizeSubject(newVal));
      return;
    }
    if ("location".equals(prop)) {
      target.setLocation(normalizeSubject(newVal));
      return;
    }
    if ("status".equals(prop)) {
      target.setStatus(newVal.trim());
      return;
    }

    if ("start".equals(prop)) {
      LocalDateTime ns = LocalDateTime.parse(newVal);
      checkNoDupOnChange(target, target.subject(), ns, target.endDate());
      target.setStart(ns);
      return;
    }
    if ("end".equals(prop)) {
      LocalDateTime ne = LocalDateTime.parse(newVal);
      checkNoDupOnChange(target, target.subject(), target.startDate(), ne);
      target.setEnd(ne);
      return;
    }
    throw new IllegalArgumentException("Unsupported edit field: " + prop);
  }

  private void checkNoDupOnChange(Event target, String subj, LocalDateTime ns, LocalDateTime ne) {
    Event candidate = target.copy();
    candidate.setSubject(subj);
    candidate.setStart(ns);
    candidate.setEnd(ne);
    ensureNoDuplicate(candidate, target);
  }

  private void detachIfBreakingSeries(String prop, List<Event> targets, String newVal) {
    if (!"start".equals(prop) && !"end".equals(prop)) {
      return;
    }
    UUID newSeriesId = UUID.randomUUID();
    for (Event t : targets) {
      if ("start".equals(prop)) {
        LocalDateTime ns = LocalDateTime.parse(newVal);
        checkNoDupOnChange(t, t.subject(), ns, t.endDate());
      } else {
        LocalDateTime ne = LocalDateTime.parse(newVal);
        checkNoDupOnChange(t, t.subject(), t.startDate(), ne);
      }
    }
    for (Event t : targets) {
      t.setSeriesId(newSeriesId);
      t.setRecurring(true);
    }
  }

  /**
   * Writes the CSV header line to the writer.
   *
   * @param writer the buffered writer to write to
   * @throws IOException if writing fails
   */
  private void writeCsvHeader(BufferedWriter writer) throws IOException {
    writer.write(
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,"
            + "Description,Location,Private\n");
  }

  /**
   * Formats an event as a CSV line.
   *
   * @param e the event to format
   * @return the CSV-formatted line for the event
   */
  private String formatEventAsCsvLine(Event e) {
    boolean sameDay = e.startDate().toLocalDate().equals(e.endDate().toLocalDate());
    boolean allDayLike = sameDay
        && e.startDate().toLocalTime().equals(LocalTime.of(8, 0))
        && e.endDate().toLocalTime().equals(LocalTime.of(17, 0));
    return String.join(",",
        csv(e.subject()),
        csv(CSV_DATE.format(e.startDate())),
        csv(CSV_TIME.format(e.startDate())),
        csv(CSV_DATE.format(e.endDate())),
        csv(CSV_TIME.format(e.endDate())),
        csv(allDayLike ? "True" : "False"),
        csv(orEmpty(e.description())),
        csv(orEmpty(e.location())),
        csv(e.status() != null && e.status().equalsIgnoreCase("private") ? "True" : "False")
    );
  }

  static String csv(String s) {
    if (s == null) {
      s = "";
    }
    if (s.contains(",") || s.contains("\"")) {
      s = s.replace("\"", "\"\"");
      return "\"" + s + "\"";
    }
    return s;
  }

  static String orEmpty(String s) {
    return s == null ? "" : s;
  }

  /**
   * Checks if the code is currently running in a test environment.
   * This is done by checking the stack trace for test classes.
   */
  private static boolean isRunningInTest() {
    StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
    for (StackTraceElement element : stackTrace) {
      String className = element.getClassName();
      if (className.contains("Test") || className.contains("junit")) {
        return true;
      }
    }
    return false;
  }
}
