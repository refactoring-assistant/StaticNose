package calendar.model;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** Default implementation of {@link CalendarModel}. */
class SingleCalendar {
  private static final LocalTime DEFAULT_ALL_DAY_START = LocalTime.of(8, 0);
  private static final LocalTime DEFAULT_ALL_DAY_END = LocalTime.of(17, 0);
  private static final DateTimeFormatter CSV_DATE =
      DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.US);
  private static final DateTimeFormatter CSV_TIME = DateTimeFormatter.ofPattern("HH:mm", Locale.US);
  private static final DateTimeFormatter ICS_DATE_TIME =
      DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'", Locale.US).withZone(ZoneOffset.UTC);

  private final List<CalendarEvent> events;
  private final Map<String, EventSeries> seriesById;
  private String name;
  private ZoneId zoneId;

  SingleCalendar(String name, ZoneId zoneId) {
    this.events = new ArrayList<>();
    this.seriesById = new HashMap<>();
    setName(name);
    this.zoneId = Objects.requireNonNull(zoneId, "Timezone cannot be null.");
  }

  String getName() {
    return name;
  }

  void setName(String newName) {
    if (newName == null || newName.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be empty.");
    }
    this.name = newName.trim();
  }

  ZoneId getZoneId() {
    return zoneId;
  }

  void setZoneId(ZoneId zoneId) {
    this.zoneId = Objects.requireNonNull(zoneId, "Timezone cannot be null.");
  }

  public CalendarEvent createEvent(
      String subject, LocalDateTime start, LocalDateTime end, boolean allDay) {
    Objects.requireNonNull(subject, "Subject cannot be null.");
    Objects.requireNonNull(start, "Start cannot be null.");
    Objects.requireNonNull(end, "End cannot be null.");
    if (!end.isAfter(start)) {
      throw new IllegalArgumentException("End time must be after start time.");
    }
    if (allDay) {
      ensureAllDayBounds(start, end);
    }
    CalendarEvent event =
        new CalendarEvent(subject, start, end, null, null, EventStatus.PUBLIC, null, allDay);
    ensureNoConflicts(Collections.singletonList(event));
    events.add(event);
    sortEvents();
    return event.copy();
  }

  public List<CalendarEvent> createRecurringEventsByCount(
      String subject,
      LocalDateTime start,
      LocalDateTime end,
      boolean allDay,
      Set<DayOfWeek> weekdays,
      int occurrences) {
    Objects.requireNonNull(weekdays, "Weekday set cannot be null.");
    if (weekdays.isEmpty()) {
      throw new IllegalArgumentException("Weekdays for the series cannot be empty.");
    }
    if (occurrences <= 0) {
      throw new IllegalArgumentException("Number of occurrences must be positive.");
    }
    ensureRecurringTemplateValid(start, end, allDay);
    ensureStartDayMatchesSet(start, weekdays);
    EventSeries metadata = new EventSeries(new HashSet<>(weekdays), allDay);
    List<CalendarEvent> generated =
        generateRecurringEvents(metadata, subject, start, end, occurrences, null);
    addGeneratedEvents(generated, metadata);
    return copiesOf(generated);
  }

  public List<CalendarEvent> createRecurringEventsUntil(
      String subject,
      LocalDateTime start,
      LocalDateTime end,
      boolean allDay,
      Set<DayOfWeek> weekdays,
      LocalDate until) {
    Objects.requireNonNull(weekdays, "Weekday set cannot be null.");
    Objects.requireNonNull(until, "Until date cannot be null.");
    if (weekdays.isEmpty()) {
      throw new IllegalArgumentException("Weekdays for the series cannot be empty.");
    }
    ensureRecurringTemplateValid(start, end, allDay);
    ensureStartDayMatchesSet(start, weekdays);
    if (until.isBefore(start.toLocalDate())) {
      throw new IllegalArgumentException("Until date must not be before the start date.");
    }
    EventSeries metadata = new EventSeries(new HashSet<>(weekdays), allDay);
    List<CalendarEvent> generated =
        generateRecurringEvents(metadata, subject, start, end, Integer.MAX_VALUE, until);
    addGeneratedEvents(generated, metadata);
    return copiesOf(generated);
  }

  public CalendarEvent editSingleEvent(
      String subject,
      LocalDateTime start,
      LocalDateTime end,
      EventProperty property,
      Object newValue) {
    CalendarEvent target = findSingleBySubjectStartEnd(subject, start, end);
    CalendarEvent before = target.copy();
    try {
      applyUpdate(Collections.singletonList(target), before, property, newValue, EditMode.SINGLE);
      ensureNoConflicts(Collections.singletonList(target));
    } catch (RuntimeException ex) {
      restoreFromCopies(Collections.singletonList(target), Collections.singletonList(before));
      throw ex;
    }
    if (property == EventProperty.START || property == EventProperty.END) {
      detachFromSeries(target, before.getSeriesId().orElse(null));
    }
    sortEvents();
    return target.copy();
  }

  public List<CalendarEvent> editEventsFrom(
      String subject, LocalDateTime start, EventProperty property, Object newValue) {
    CalendarEvent reference = findUniqueBySubjectAndStart(subject, start);
    List<CalendarEvent> targets = collectFrom(reference);
    List<CalendarEvent> backups = copyList(targets);
    try {
      applyUpdate(targets, reference.copy(), property, newValue, EditMode.SERIES_FROM);
      ensureNoConflicts(targets);
    } catch (RuntimeException ex) {
      restoreFromCopies(targets, backups);
      throw ex;
    }
    if (property == EventProperty.START || property == EventProperty.END) {
      splitSeries(reference, targets);
    }
    sortEvents();
    return copiesOf(targets);
  }

  public List<CalendarEvent> editEntireSeries(
      String subject, LocalDateTime start, EventProperty property, Object newValue) {
    CalendarEvent reference = findUniqueBySubjectAndStart(subject, start);
    List<CalendarEvent> targets = collectEntireSeries(reference);
    List<CalendarEvent> backups = copyList(targets);
    try {
      applyUpdate(targets, reference.copy(), property, newValue, EditMode.SERIES_ALL);
      ensureNoConflicts(targets);
    } catch (RuntimeException ex) {
      restoreFromCopies(targets, backups);
      throw ex;
    }
    sortEvents();
    return copiesOf(targets);
  }

  public List<CalendarEvent> eventsOn(LocalDate date) {
    Objects.requireNonNull(date, "Date cannot be null.");
    return events.stream()
        .filter(event -> event.occursOn(date))
        .sorted()
        .map(CalendarEvent::copy)
        .collect(Collectors.toList());
  }

  public List<CalendarEvent> eventsBetween(LocalDateTime start, LocalDateTime end) {
    Objects.requireNonNull(start, "Start cannot be null.");
    Objects.requireNonNull(end, "End cannot be null.");
    if (end.isBefore(start)) {
      throw new IllegalArgumentException("End must not be before start.");
    }
    return events.stream()
        .filter(event -> overlapsInclusive(event, start, end))
        .sorted()
        .map(CalendarEvent::copy)
        .collect(Collectors.toList());
  }

  List<CalendarEvent> eventsBySeries(String seriesId) {
    if (seriesId == null) {
      return Collections.emptyList();
    }
    return events.stream()
        .filter(event -> seriesId.equals(event.getSeriesId().orElse(null)))
        .sorted()
        .map(CalendarEvent::copy)
        .collect(Collectors.toList());
  }

  public boolean isBusy(LocalDateTime moment) {
    Objects.requireNonNull(moment, "Moment cannot be null.");
    return events.stream()
        .anyMatch(event -> !moment.isBefore(event.getStart()) && moment.isBefore(event.getEnd()));
  }

  public Path exportToCsv(Path outputFile) throws IOException {
    Objects.requireNonNull(outputFile, "Output file cannot be null.");
    Path absolute = outputFile.toAbsolutePath().normalize();
    Path parent = absolute.getParent();
    if (parent != null) {
      Files.createDirectories(parent);
    }
    List<String> lines = new ArrayList<>();
    lines.add(
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,"
            + "Location,Private");
    for (CalendarEvent event : events.stream().sorted().collect(Collectors.toList())) {
      lines.add(serializeEvent(event));
    }
    Files.write(absolute, lines, StandardCharsets.UTF_8);
    return absolute;
  }

  private static boolean overlapsInclusive(
      CalendarEvent event, LocalDateTime start, LocalDateTime end) {
    return !event.getEnd().isBefore(start) && !event.getStart().isAfter(end);
  }

  private void ensureAllDayBounds(LocalDateTime start, LocalDateTime end) {
    if (!start.toLocalTime().equals(DEFAULT_ALL_DAY_START)
        || !end.toLocalTime().equals(DEFAULT_ALL_DAY_END)) {
      throw new IllegalArgumentException("All day events must run from 8:00 to 17:00.");
    }
  }

  private void ensureRecurringTemplateValid(
      LocalDateTime start, LocalDateTime end, boolean allDay) {
    Objects.requireNonNull(start, "Start cannot be null.");
    Objects.requireNonNull(end, "End cannot be null.");
    if (!end.isAfter(start)) {
      throw new IllegalArgumentException("End time must be after start time.");
    }
    if (!start.toLocalDate().equals(end.toLocalDate())) {
      throw new IllegalArgumentException("Events in a series must start and end on the same day.");
    }
    if (allDay) {
      ensureAllDayBounds(start, end);
    }
  }

  private void ensureStartDayMatchesSet(LocalDateTime start, Set<DayOfWeek> weekdays) {
    if (!weekdays.contains(start.getDayOfWeek())) {
      throw new IllegalArgumentException("Start date does not match any of the supplied weekdays.");
    }
  }

  private List<CalendarEvent> generateRecurringEvents(
      EventSeries metadata,
      String subject,
      LocalDateTime firstStart,
      LocalDateTime firstEnd,
      int occurrences,
      LocalDate until) {
    List<CalendarEvent> generated = new ArrayList<>();
    Duration duration = Duration.between(firstStart, firstEnd);
    LocalDateTime currentStart = firstStart;
    LocalDateTime currentEnd = firstEnd;
    int created = 0;
    while (created < occurrences) {
      if (until != null && currentStart.toLocalDate().isAfter(until)) {
        break;
      }
      CalendarEvent event =
          new CalendarEvent(
              subject,
              currentStart,
              currentEnd,
              null,
              null,
              EventStatus.PUBLIC,
              metadata.getId(),
              metadata.isAllDay());
      ensureNoConflicts(Collections.singletonList(event));
      generated.add(event);
      created++;
      LocalDateTime nextStart = findNextStart(currentStart, metadata.getDaysOfWeek());
      currentStart = nextStart.withHour(firstStart.getHour()).withMinute(firstStart.getMinute());
      currentEnd = currentStart.plus(duration);
    }
    if (generated.isEmpty()) {
      throw new IllegalStateException("Could not create any events for the series.");
    }
    return generated;
  }

  private LocalDateTime findNextStart(LocalDateTime current, Set<DayOfWeek> days) {
    LocalDateTime cursor = current.plusDays(1);
    while (!days.contains(cursor.getDayOfWeek())) {
      cursor = cursor.plusDays(1);
    }
    return cursor;
  }

  private void addGeneratedEvents(List<CalendarEvent> generated, EventSeries metadata) {
    events.addAll(generated);
    seriesById.put(metadata.getId(), metadata);
    sortEvents();
  }

  private void sortEvents() {
    events.sort(Comparator.naturalOrder());
  }

  private void applyUpdate(
      List<CalendarEvent> targets,
      CalendarEvent referenceBefore,
      EventProperty property,
      Object newValue,
      EditMode mode) {
    switch (property) {
      case SUBJECT:
        String subject = requireString(newValue, "Subject");
        for (CalendarEvent event : targets) {
          event.setSubject(subject);
        }
        break;
      case DESCRIPTION:
        for (CalendarEvent event : targets) {
          event.setDescription(toNullableString(newValue));
        }
        break;
      case LOCATION:
        for (CalendarEvent event : targets) {
          event.setLocation(toNullableString(newValue));
        }
        break;
      case STATUS:
        EventStatus status = cast(newValue, EventStatus.class, "Status");
        for (CalendarEvent event : targets) {
          event.setStatus(status);
        }
        break;
      case START:
        updateStart(targets, referenceBefore, cast(newValue, LocalDateTime.class, "Start"), mode);
        break;
      case END:
        updateEnd(targets, referenceBefore, cast(newValue, LocalDateTime.class, "End"), mode);
        break;
      default:
        throw new IllegalArgumentException("Unsupported property: " + property);
    }
  }

  private void updateStart(
      List<CalendarEvent> targets,
      CalendarEvent referenceBefore,
      LocalDateTime newStart,
      EditMode mode) {
    if (!newStart.isBefore(referenceBefore.getEnd())) {
      throw new IllegalArgumentException("Start time must be before the end time.");
    }
    if (mode == EditMode.SINGLE) {
      CalendarEvent only = targets.get(0);
      Duration shift = Duration.between(only.getStart(), newStart);
      LocalDateTime newEnd = only.getEnd().plus(shift);
      if (!newEnd.isAfter(newStart)) {
        throw new IllegalArgumentException("Start change would invalidate the event duration.");
      }
      only.setStart(newStart);
      only.setEnd(newEnd);
      return;
    }
    Duration delta = Duration.between(referenceBefore.getStart(), newStart);
    for (CalendarEvent event : targets) {
      LocalDateTime candidateStart = event.getStart().plus(delta);
      LocalDateTime candidateEnd = event.getEnd().plus(delta);
      if (!candidateEnd.isAfter(candidateStart)) {
        throw new IllegalArgumentException("Start change would invalidate the event duration.");
      }
      event.setStart(candidateStart);
      event.setEnd(candidateEnd);
    }
  }

  private void updateEnd(
      List<CalendarEvent> targets,
      CalendarEvent referenceBefore,
      LocalDateTime newEnd,
      EditMode mode) {
    if (!newEnd.isAfter(referenceBefore.getStart())) {
      throw new IllegalArgumentException("End time must remain after the start.");
    }
    if (mode == EditMode.SINGLE) {
      CalendarEvent only = targets.get(0);
      if (!newEnd.isAfter(only.getStart())) {
        throw new IllegalArgumentException("End time must remain after the start.");
      }
      only.setEnd(newEnd);
      return;
    }
    Duration duration = Duration.between(referenceBefore.getStart(), newEnd);
    for (CalendarEvent event : targets) {
      LocalDateTime candidateEnd = event.getStart().plus(duration);
      if (!candidateEnd.isAfter(event.getStart())) {
        throw new IllegalArgumentException("End time must remain after the start.");
      }
      event.setEnd(candidateEnd);
    }
  }

  private List<CalendarEvent> collectFrom(CalendarEvent reference) {
    if (reference.getSeriesId().isEmpty()) {
      return new ArrayList<>(Collections.singletonList(reference));
    }
    String seriesId = reference.getSeriesId().get();
    return events.stream()
        .filter(
            event ->
                seriesId.equals(event.getSeriesId().orElse(null))
                    && !event.getStart().isBefore(reference.getStart()))
        .sorted()
        .collect(Collectors.toList());
  }

  private List<CalendarEvent> collectEntireSeries(CalendarEvent reference) {
    if (reference.getSeriesId().isEmpty()) {
      return new ArrayList<>(Collections.singletonList(reference));
    }
    String seriesId = reference.getSeriesId().get();
    return events.stream()
        .filter(event -> seriesId.equals(event.getSeriesId().orElse(null)))
        .sorted()
        .collect(Collectors.toList());
  }

  private void splitSeries(CalendarEvent reference, List<CalendarEvent> targets) {
    String originalId = reference.getSeriesId().orElse(null);
    if (originalId == null) {
      return;
    }
    EventSeries original = seriesById.get(originalId);
    if (original == null) {
      return;
    }
    EventSeries newSeries =
        new EventSeries(new HashSet<>(original.getDaysOfWeek()), original.isAllDay());
    for (CalendarEvent event : targets) {
      event.setSeriesId(newSeries.getId());
    }
    seriesById.put(newSeries.getId(), newSeries);
    purgeSeriesIfEmpty(originalId);
  }

  private void detachFromSeries(CalendarEvent event, String originalSeriesId) {
    event.setSeriesId(null);
    purgeSeriesIfEmpty(originalSeriesId);
  }

  private void restoreFromCopies(
      Collection<CalendarEvent> targets, Collection<CalendarEvent> backups) {
    Map<String, CalendarEvent> byId =
        backups.stream().collect(Collectors.toMap(CalendarEvent::getId, e -> e));
    for (CalendarEvent event : targets) {
      CalendarEvent copy = byId.get(event.getId());
      if (copy != null) {
        event.setSubject(copy.getSubject());
        restoreTimes(event, copy.getStart(), copy.getEnd());
        event.setDescription(copy.getDescription().orElse(null));
        event.setLocation(copy.getLocation().orElse(null));
        event.setStatus(copy.getStatus());
        event.setSeriesId(copy.getSeriesId().orElse(null));
      }
    }
  }

  private void restoreTimes(CalendarEvent event, LocalDateTime start, LocalDateTime end) {
    try {
      event.setStart(start);
      event.setEnd(end);
    } catch (IllegalArgumentException ex) {
      event.setEnd(end);
      event.setStart(start);
    }
  }

  private void ensureNoConflicts(List<CalendarEvent> modified) {
    ensureNoConflicts(modified, true);
  }

  private void ensureNoConflicts(List<CalendarEvent> modified, boolean checkExisting) {
    if (checkExisting) {
      for (CalendarEvent updated : modified) {
        for (CalendarEvent existing : events) {
          if (updated.getId().equals(existing.getId())) {
            continue;
          }
          if (sameSlot(updated, existing)) {
            throw new IllegalArgumentException(
                "An event with the same subject, start, and end already exists.");
          }
        }
      }
    }
    for (int i = 0; i < modified.size(); i++) {
      for (int j = i + 1; j < modified.size(); j++) {
        if (sameSlot(modified.get(i), modified.get(j))) {
          throw new IllegalArgumentException(
              "An event would conflict with another within the same command.");
        }
      }
    }
  }

  private boolean sameSlot(CalendarEvent a, CalendarEvent b) {
    return a.getSubject().equals(b.getSubject())
        && a.getStart().equals(b.getStart())
        && a.getEnd().equals(b.getEnd());
  }

  private CalendarEvent findSingleBySubjectStartEnd(
      String subject, LocalDateTime start, LocalDateTime end) {
    List<CalendarEvent> matches =
        events.stream()
            .filter(
                event ->
                    event.getSubject().equals(subject)
                        && event.getStart().equals(start)
                        && event.getEnd().equals(end))
            .collect(Collectors.toList());
    if (matches.isEmpty()) {
      throw new IllegalArgumentException("No event found with the supplied subject and times.");
    }
    if (matches.size() > 1) {
      throw new IllegalArgumentException(
          "Multiple events match the supplied subject and time range.");
    }
    return matches.get(0);
  }

  CalendarEvent findUniqueBySubjectAndStart(String subject, LocalDateTime start) {
    List<CalendarEvent> matches =
        events.stream()
            .filter(event -> event.getSubject().equals(subject) && event.getStart().equals(start))
            .collect(Collectors.toList());
    if (matches.isEmpty()) {
      throw new IllegalArgumentException(
          "No event found with the supplied subject and start time.");
    }
    if (matches.size() > 1) {
      throw new IllegalArgumentException(
          "Multiple events share the supplied subject and start time. Command ambiguous.");
    }
    return matches.get(0);
  }

  private List<CalendarEvent> copyList(List<CalendarEvent> source) {
    return source.stream().map(CalendarEvent::copy).collect(Collectors.toList());
  }

  private List<CalendarEvent> copiesOf(Collection<CalendarEvent> source) {
    return source.stream().map(CalendarEvent::copy).collect(Collectors.toList());
  }

  private String serializeEvent(CalendarEvent event) {
    String subject = escapeCsv(event.getSubject());
    String startDate = CSV_DATE.format(event.getStart());
    String startTime = CSV_TIME.format(event.getStart());
    String endDate = CSV_DATE.format(event.getEnd());
    String endTime = CSV_TIME.format(event.getEnd());
    String allDay = event.isAllDayPreferred() ? "True" : "False";
    String description = escapeCsv(event.getDescription().orElse(""));
    String location = escapeCsv(event.getLocation().orElse(""));
    String privateField = event.getStatus() == EventStatus.PRIVATE ? "True" : "False";
    return String.join(
        ",",
        subject,
        startDate,
        startTime,
        endDate,
        endTime,
        allDay,
        description,
        location,
        privateField);
  }

  private String escapeCsv(String value) {
    if (value == null) {
      return "";
    }
    String trimmed = value;
    if (trimmed.contains(",") || trimmed.contains("\"") || trimmed.contains("\n")) {
      trimmed = "\"" + trimmed.replace("\"", "\"\"") + "\"";
    }
    return trimmed;
  }

  private String requireString(Object value, String label) {
    if (value == null) {
      throw new IllegalArgumentException(label + " cannot be null.");
    }
    String text = value.toString().trim();
    if (text.isEmpty()) {
      throw new IllegalArgumentException(label + " cannot be empty.");
    }
    return text;
  }

  private String toNullableString(Object value) {
    if (value == null) {
      return null;
    }
    String text = value.toString();
    return text.isEmpty() ? null : text;
  }

  private <T> T cast(Object value, Class<T> type, String label) {
    if (value == null) {
      throw new IllegalArgumentException(label + " cannot be null.");
    }
    if (!type.isInstance(value)) {
      throw new IllegalArgumentException(
          label + " must be a " + type.getSimpleName() + " but was " + value.getClass().getName());
    }
    return type.cast(value);
  }

  void shiftToTimezone(ZoneId newZone) {
    Objects.requireNonNull(newZone, "Timezone cannot be null.");
    if (newZone.equals(this.zoneId)) {
      return;
    }
    for (CalendarEvent event : events) {
      ZonedDateTime start = event.getStart().atZone(this.zoneId).withZoneSameInstant(newZone);
      ZonedDateTime end = event.getEnd().atZone(this.zoneId).withZoneSameInstant(newZone);
      event.setStart(start.toLocalDateTime());
      event.setEnd(end.toLocalDateTime());
    }
    this.zoneId = newZone;
    sortEvents();
  }

  EventSeries getSeriesMetadata(String seriesId) {
    return seriesById.get(seriesId);
  }

  String cloneSeries(EventSeries metadata) {
    if (metadata == null) {
      return null;
    }
    EventSeries clone =
        new EventSeries(new HashSet<>(metadata.getDaysOfWeek()), metadata.isAllDay());
    seriesById.put(clone.getId(), clone);
    return clone.getId();
  }

  CalendarEvent addClonedEvent(
      CalendarEvent template, LocalDateTime newStart, LocalDateTime newEnd, String seriesId) {
    CalendarEvent event =
        new CalendarEvent(
            template.getSubject(),
            newStart,
            newEnd,
            template.getDescription().orElse(null),
            template.getLocation().orElse(null),
            template.getStatus(),
            seriesId,
            template.isAllDayPreferred());
    ensureNoConflicts(Collections.singletonList(event));
    events.add(event);
    sortEvents();
    return event.copy();
  }

  Path exportToIcs(Path outputFile) throws IOException {
    Objects.requireNonNull(outputFile, "Output file cannot be null.");
    Path absolute = outputFile.toAbsolutePath().normalize();
    Path parent = absolute.getParent();
    if (parent != null) {
      Files.createDirectories(parent);
    }
    List<String> lines = new ArrayList<>();
    lines.add("BEGIN:VCALENDAR");
    lines.add("VERSION:2.0");
    lines.add("PRODID:-//CS5010//Calendar//EN");
    lines.add("CALSCALE:GREGORIAN");
    List<CalendarEvent> sorted = events.stream().sorted().collect(Collectors.toList());
    Instant now = Instant.now();
    for (CalendarEvent event : sorted) {
      ZonedDateTime startUtc = event.getStart().atZone(zoneId).withZoneSameInstant(ZoneOffset.UTC);
      ZonedDateTime endUtc = event.getEnd().atZone(zoneId).withZoneSameInstant(ZoneOffset.UTC);
      lines.add("BEGIN:VEVENT");
      lines.add("UID:" + event.getId() + "@" + name);
      lines.add("DTSTAMP:" + ICS_DATE_TIME.format(now));
      lines.add("DTSTART:" + ICS_DATE_TIME.format(startUtc));
      lines.add("DTEND:" + ICS_DATE_TIME.format(endUtc));
      lines.add("SUMMARY:" + escapeIcs(event.getSubject()));
      event.getDescription().ifPresent(desc -> lines.add("DESCRIPTION:" + escapeIcs(desc)));
      event.getLocation().ifPresent(loc -> lines.add("LOCATION:" + escapeIcs(loc)));
      lines.add("CLASS:" + (event.getStatus() == EventStatus.PRIVATE ? "PRIVATE" : "PUBLIC"));
      lines.add("END:VEVENT");
    }
    lines.add("END:VCALENDAR");
    Files.write(absolute, lines, StandardCharsets.UTF_8);
    return absolute;
  }

  private String escapeIcs(String value) {
    if (value == null) {
      return "";
    }
    return value.replace("\\", "\\\\").replace(";", "\\;").replace(",", "\\,").replace("\n", "\\n");
  }

  private void purgeSeriesIfEmpty(String seriesId) {
    if (seriesId == null) {
      return;
    }
    boolean stillPresent =
        events.stream().anyMatch(event -> seriesId.equals(event.getSeriesId().orElse(null)));
    if (!stillPresent) {
      seriesById.remove(seriesId);
    }
  }

  private enum EditMode {
    SINGLE,
    SERIES_FROM,
    SERIES_ALL
  }
}
