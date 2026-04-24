package calendar.model;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * In-memory single-calendar implementation.
 * Stores events in a LinkedHashSet to preserve insertion order,
 * while still allowing fast duplicate checks.
 */
public final class CalendarModelImpl implements CalendarModel {

  private static final LocalTime ALL_DAY_START = LocalTime.of(8, 0);
  private static final LocalTime ALL_DAY_END = LocalTime.of(17, 0);

  /**
   * Acts as the main event store.
   */
  private final Set<CalendarEvent> events = new LinkedHashSet<>();

  /**
   * Index to enforce the condition "no two events with same subject,start,end".
   */
  private final Map<String, CalendarEvent> uniqueIndex = new HashMap<>();

  /**
   * Creates the unique string key used for the uniqueIndex HashMap.
   * Key = combination of event subject + start time + end time.
   *
   * @param subject The event title.
   * @param start The event start time.
   * @param end The event end time.
   * @return The unique key string.
   */
  private String keyFor(String subject, LocalDateTime start, LocalDateTime end) {
    return subject + "|" + start + "|" + end;
  }

  /**
   * Checks the unique index before creating or editing an event.
   * Throws an IllegalArgumentException if a duplicate event (same key) already exists.
   *
   * @param subject The event title.
   * @param start The event start time.
   * @param end The event end time.
   * @throws IllegalArgumentException if the key already exists.
   */
  private void ensureNoDuplicate(String subject, LocalDateTime start, LocalDateTime end) {
    String k = keyFor(subject, start, end);
    if (uniqueIndex.containsKey(k)) {
      throw new IllegalArgumentException("Duplicate event (same subject, start, end) not allowed.");
    }
  }

  /**
   * Adds the given event to the unique index map.
   *
   * @param e The CalendarEvent to index.
   */
  private void index(CalendarEvent e) {
    uniqueIndex.put(keyFor(e.subject(), e.start(), e.end()), e);
  }

  /**
   * Removes the given event from the unique index map.
   *
   * @param e The CalendarEvent to remove from the index.
   */
  private void unindex(CalendarEvent e) {
    uniqueIndex.remove(keyFor(e.subject(), e.start(), e.end()));
  }

  @Override
  public CalendarEvent createSingleEvent(String subject, LocalDateTime start, LocalDateTime end,
                                         String description, String location, String status) {
    ensureNoDuplicate(subject, start, end);
    CalendarEvent e =
        CalendarEvent.builder().subject(subject).start(start).end(end).description(description)
            .location(location).status(status).build();
    events.add(e);
    index(e);
    return e;
  }

  @Override
  public CalendarEvent createAllDayEvent(String subject, LocalDate date, String description,
                                         String location, String status) {
    LocalDateTime start = LocalDateTime.of(date, ALL_DAY_START);
    LocalDateTime end = LocalDateTime.of(date, ALL_DAY_END);
    return createSingleEvent(subject, start, end, description, location, status);
  }

  @Override
  public List<CalendarEvent> createSeriesByCount(String subject, LocalDateTime start,
                                                 LocalDateTime end, RecurrenceRule rule,
                                                 String description, String location,
                                                 String status) {
    SeriesId sid = SeriesId.newId();
    List<CalendarEvent> created = new ArrayList<>();

    int remaining = rule.count().orElseThrow(() -> new IllegalStateException("count required"));
    LocalDate date = start.toLocalDate();

    while (remaining > 0) {
      DayOfWeek dow = date.getDayOfWeek();
      if (rule.weekdays().contains(dow)) {
        LocalDateTime s = LocalDateTime.of(date, start.toLocalTime());
        LocalDateTime e = LocalDateTime.of(date, end.toLocalTime());

        ensureNoDuplicate(subject, s, e);
        CalendarEvent ev =
            CalendarEvent.builder().subject(subject).start(s).end(e).description(description)
                .location(location).status(status).seriesId(sid).build();
        events.add(ev);
        index(ev);
        created.add(ev);
        remaining--;
      }
      date = date.plusDays(1);
    }
    return created;
  }

  @Override
  public List<CalendarEvent> createSeriesUntil(String subject, LocalDateTime start,
                                               LocalDateTime end, RecurrenceRule rule,
                                               String description, String location, String status) {
    SeriesId sid = SeriesId.newId();
    List<CalendarEvent> created = new ArrayList<>();
    LocalDate date = start.toLocalDate();
    LocalDate until =
        rule.untilInclusive().orElseThrow(() -> new IllegalStateException("until date required"));
    while (!date.isAfter(until)) {
      DayOfWeek dow = date.getDayOfWeek();
      if (rule.weekdays().contains(dow)) {
        LocalDateTime s = LocalDateTime.of(date, start.toLocalTime());
        LocalDateTime e = LocalDateTime.of(date, end.toLocalTime());
        ensureNoDuplicate(subject, s, e);
        CalendarEvent ev =
            CalendarEvent.builder().subject(subject).start(s).end(e).description(description)
                .location(location).status(status).seriesId(sid).build();
        events.add(ev);
        index(ev);
        created.add(ev);
      }
      date = date.plusDays(1);
    }
    return created;
  }

  @Override
  public void editEvent(String property, String subject, LocalDateTime startsAt,
                        LocalDateTime endsAt, String newValue, EditScope scope) {

    CalendarEvent anchor = uniqueIndex.get(keyFor(subject, startsAt, endsAt));
    if (anchor == null) {
      throw new IllegalArgumentException("No event found matching subject/start/end for edit.");
    }

    if (scope == EditScope.FROM_THIS && "start".equalsIgnoreCase(property)) {
      changeStartFromThisWithSplit(anchor, newValue);
      return;
    }


    List<CalendarEvent> targets = new ArrayList<>();
    switch (scope) {
      case SINGLE:
        targets.add(anchor);
        break;
      case FROM_THIS:
        if (anchor.seriesId().isEmpty()) {
          targets.add(anchor);
        } else {
          SeriesId sid = anchor.seriesId().get();
          LocalDateTime from = anchor.start();
          for (CalendarEvent e : events) {
            if (sid.equals(e.seriesId().orElse(null)) && !e.start().isBefore(from)) {
              targets.add(e);
            }
          }
        }
        break;
      case ENTIRE_SERIES:
        if (anchor.seriesId().isEmpty()) {
          targets.add(anchor);
        } else {
          SeriesId sid = anchor.seriesId().get();
          for (CalendarEvent e : events) {
            if (sid.equals(e.seriesId().orElse(null))) {
              targets.add(e);
            }
          }
        }
        break;
      default:
        throw new IllegalArgumentException("Invalid occurrence scope: " + scope);
    }


    List<CalendarEvent> edited = new ArrayList<>();
    for (CalendarEvent e : targets) {
      CalendarEvent ne = applyPropertyChange(e, property, newValue);

      boolean keyChanged = !(e.subject().equals(ne.subject())
          && e.start().equals(ne.start())
          && e.end().equals(ne.end()));
      if (keyChanged) {
        ensureNoDuplicate(ne.subject(), ne.start(), ne.end());
      }

      unindex(e);
      events.remove(e);
      events.add(ne);
      index(ne);
      edited.add(ne);
    }
  }

  /**
   * Applies a single property change (like subject or location) to an event.
   * This handles string parsing for date/time properties (start/end).
   *
   * @param e The original CalendarEvent.
   * @param property The name of the property to change.
   * @param value The new value for that property.
   * @return A new CalendarEvent instance with the updated property.
   * @throws IllegalArgumentException if the property name is unknown.
   */
  private CalendarEvent applyPropertyChange(CalendarEvent e, String property, String value) {
    String p = property.toLowerCase(Locale.ROOT).trim();
    switch (p) {
      case "subject":
        return e.withSubject(value);
      case "start":
        {
        LocalDateTime requested = LocalDateTime.parse(value);
        LocalDateTime ns = LocalDateTime.of(e.start().toLocalDate(), requested.toLocalTime());
        return e.withStart(ns);
        }
      case "end":
        {
        LocalDateTime requested = LocalDateTime.parse(value);
        LocalDateTime ne = LocalDateTime.of(e.end().toLocalDate(), requested.toLocalTime());
        return e.withEnd(ne);
        }
      case "description":
        return e.withDescription(value);
      case "location":
        return e.withLocation(value);
      case "status":
        return e.withStatus(value);
      default:
        throw new IllegalArgumentException("Unknown property: " + property);
    }
  }

  /**
   * This is a special edit method required for the tricky "edit series starting from this"
   * case only when the start time is changed.
   *
   * @param anchor The event instance the user selected to start the edit from.
   * @param newValue The new start time string. Only the time part is used.
   */
  private void changeStartFromThisWithSplit(CalendarEvent anchor, String newValue) {
    LocalDateTime requested = LocalDateTime.parse(newValue);
    LocalTime newTime = requested.toLocalTime();


    if (anchor.seriesId().isEmpty()) {
      CalendarEvent updated = anchor.withStart(LocalDateTime
          .of(anchor.start().toLocalDate(), newTime));

      boolean keyChanged = !(anchor.subject().equals(updated.subject())
          && anchor.start().equals(updated.start())
          && anchor.end().equals(updated.end()));
      if (keyChanged) {
        ensureNoDuplicate(updated.subject(), updated.start(), updated.end());
      }
      unindex(anchor);
      events.remove(anchor);
      events.add(updated);
      index(updated);
      return;
    }

    SeriesId oldSid = anchor.seriesId().get();

    List<CalendarEvent> tail = new ArrayList<>();
    for (CalendarEvent e : events) {
      if (e.seriesId().isPresent()
          && oldSid.equals(e.seriesId().get())
          && !e.start().isBefore(anchor.start())) {
        tail.add(e);
      }
    }
    if (tail.isEmpty()) {
      return;
    }

    for (CalendarEvent e : tail) {
      unindex(e);
      events.remove(e);
    }

    SeriesId newSid = SeriesId.newId();

    List<CalendarEvent> rebuilt = new ArrayList<>();
    for (CalendarEvent e : tail) {
      LocalDateTime newStart = LocalDateTime.of(e.start().toLocalDate(), newTime);
      CalendarEvent candidate = e.withSeriesId(newSid).withStart(newStart);

      ensureNoDuplicate(candidate.subject(), candidate.start(), candidate.end());
      rebuilt.add(candidate);
    }

    for (CalendarEvent ne : rebuilt) {
      events.add(ne);
      index(ne);
    }
  }

  @Override
  public List<CalendarEvent> eventsOn(LocalDate date) {
    return events.stream().filter(
            e -> !e.start().toLocalDate().isAfter(date)
                && !e.end().toLocalDate().isBefore(date))
        .sorted(Comparator.comparing(CalendarEvent::start)).collect(Collectors.toList());
  }

  @Override
  public List<CalendarEvent> eventsBetween(LocalDateTime fromInclusive, LocalDateTime toInclusive) {
    return events.stream()
        .filter(e -> !(e.end().isBefore(fromInclusive) || e.start().isAfter(toInclusive)))
        .sorted(Comparator.comparing(CalendarEvent::start)).collect(Collectors.toList());
  }

  @Override
  public boolean isBusy(LocalDateTime when) {
    for (CalendarEvent e : events) {
      if (!when.isBefore(e.start()) && !when.isAfter(e.end())) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Path exportCsv(Path outputCsvPath) {
    try {
      Files.createDirectories(outputCsvPath.getParent());
      CsvExporter.writeGoogleCsv(events, outputCsvPath);
      return outputCsvPath.toAbsolutePath();
    } catch (Exception e) {
      throw new RuntimeException("Failed to export CSV: " + e.getMessage(), e);
    }
  }

  @Override
  public boolean exists(String subject, LocalDateTime start, LocalDateTime end) {
    return uniqueIndex.containsKey(keyFor(subject, start, end));
  }
}
