package calendar.model.impl;

import calendar.model.CalendarModel;
import calendar.model.EditScope;
import calendar.model.EventSelector;
import calendar.model.EventSpec;
import calendar.model.Exporter;
import calendar.model.PropertyChange;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;


/**
 * Actual Model and implementation for the calendar events.
 * Implements storing and querying of events, series and editing.
 */
public class CalendarModelImpl implements CalendarModel {

  private final Map<EventId, Event> entries = new LinkedHashMap<>();
  private final Set<String> uniqueTriples = new HashSet<>();
  private final Map<LocalDate, List<EventId>> dateIndex = new HashMap<>();
  private final Map<SeriesId, NavigableSet<EventId>> seriesIndex = new HashMap<>();
  private final Map<String, List<EventId>> subjectStartIndex = new HashMap<>();
  private String timezone;


  /**
   * Constructs a unique key for subject + start + end.
   */
  private static String key(String subject, LocalDateTime start, LocalDateTime end) {
    return subject + "|" + start + "|" + end;
  }

  /**
   * Constructs a key for subject and a  normalized start time.
   */
  private static String ssKey(String subject, LocalDateTime start) {
    LocalDateTime normalized = start.withSecond(0).withNano(0);
    return subject + "|" + normalized;
  }

  /**
   * Creates a new calendar model using the specified timezone.
   *
   * @param timezone the IANA timezone identifier for this calendar
   * @throws IllegalArgumentException if the timezone is null , blank or invalid
   */
  public CalendarModelImpl(String timezone) {
    if (timezone == null || timezone.isBlank()) {
      throw new IllegalArgumentException("Timezone cannot be null or empty");
    }
    java.time.ZoneId.of(timezone);
    this.timezone = timezone;
  }


  /**
   * Inserts an event into indices.
   *
   * @param e the event to index
   */
  private void index(Event e) {
    entries.put(e.id(), e);

    LocalDate d = e.start().toLocalDate();
    List<EventId> idsForDate = dateIndex.computeIfAbsent(d, k -> new ArrayList<>());
    idsForDate.add(e.id());

    uniqueTriples.add(key(e.subject(), e.start(), e.end()));

    String ssk = ssKey(e.subject(), e.start());
    List<EventId> ssList = subjectStartIndex.computeIfAbsent(ssk, k -> new ArrayList<>());
    ssList.add(e.id());

    if (e.seriesId() != null) {
      NavigableSet<EventId> set =
          seriesIndex.computeIfAbsent(e.seriesId(), k -> new TreeSet<>((a, b) -> {
            Event ea = entries.get(a);
            Event eb = entries.get(b);
            int cmp = ea.start().compareTo(eb.start());
            if (cmp != 0) {
              return cmp;
            }
            return a.compareTo(b);
          }));
      set.add(e.id());
    }
  }

  /**
   * Removes an event from all indices.
   *
   * @param e the event to unindex
   */
  private void unindex(Event e) {
    if (e.seriesId() != null) {
      NavigableSet<EventId> set = seriesIndex.get(e.seriesId());
      if (set != null) {
        set.remove(e.id());
        if (set.isEmpty()) {
          seriesIndex.remove(e.seriesId());
        }
      }
    }
    entries.remove(e.id());
    LocalDate d = e.start().toLocalDate();
    List<EventId> idsForDate = dateIndex.get(d);
    if (idsForDate != null) {
      idsForDate.remove(e.id());
      if (idsForDate.isEmpty()) {
        dateIndex.remove(d);
      }
    }
    uniqueTriples.remove(key(e.subject(), e.start(), e.end()));
    String ssk = ssKey(e.subject(), e.start());
    List<EventId> ssList = subjectStartIndex.get(ssk);
    if (ssList != null) {
      ssList.remove(e.id());
      if (ssList.isEmpty()) {
        subjectStartIndex.remove(ssk);
      }
    }
  }

  /**
   * Creates and stores a single event in the calendar.
   *
   * @param spec the event specification
   * @return the generated EventID.
   */
  @Override
  public EventId createSingle(EventSpec spec) {
    if (spec == null) {
      throw new IllegalArgumentException("EventSpec cannot be null");
    }

    LocalDateTime start = spec.start();
    LocalDateTime end = spec.end().orElse(null);
    boolean allDay = spec.allDay();

    if (end == null) {
      LocalDate date = start.toLocalDate();
      start = date.atTime(8, 0);
      end = date.atTime(17, 0);
      allDay = true;
    }

    if (end.isBefore(start)) {
      throw new IllegalArgumentException("End time is before start time");
    }

    String triple = key(spec.subject(), start, end);
    if (uniqueTriples.contains(triple)) {
      throw new IllegalArgumentException("Duplicate event with same subject, start, and end");
    }

    EventId id = new EventId();
    Event ev = new Event(
        id,
        null,
        spec.subject(),
        start,
        end,
        spec.description(),
        spec.location(),
        spec.status(),
        allDay
    );

    index(ev);
    return id;
  }

  /**
   * Creates a recurring series of events based on a base specification and rule.
   *
   * @param base the base event specification
   * @param rule the series rule of recurrence.
   * @return list of created EventIDs
   */
  @Override
  public List<EventId> createSeries(EventSpec base, SeriesRule rule) {
    if (base == null || rule == null) {
      throw new IllegalArgumentException("Base spec and rule are required");
    }
    if ((rule.occurrences == null && rule.until == null)
        || (rule.occurrences != null && rule.until != null)) {
      throw new IllegalArgumentException("Provide either occurrences OR until (exclusively)");
    }
    if (rule.days == null || rule.days.isEmpty()) {
      throw new IllegalArgumentException("At least one weekday must be specified");
    }

    LocalDateTime baseStart = base.start();
    LocalDateTime baseEnd = base.end().orElse(null);
    boolean baseAllDay = base.allDay();

    if (baseEnd == null) {
      LocalDate d = baseStart.toLocalDate();
      baseStart = d.atTime(8, 0);
      baseEnd = d.atTime(17, 0);
      baseAllDay = true;
    }

    if (baseEnd.isBefore(baseStart)) {
      throw new IllegalArgumentException("Base end before start");
    }
    if (!baseStart.toLocalDate().equals(baseEnd.toLocalDate())) {
      throw new IllegalArgumentException("Series base event must start and end on the same day");
    }
    Duration span = Duration.between(baseStart, baseEnd);
    LocalTime seriesStartTime = rule.startTime != null ? rule.startTime : baseStart.toLocalTime();
    List<Event> planned = new ArrayList<>();
    SeriesId sid = new SeriesId();

    LocalDate cursor = baseStart.toLocalDate();
    int created = 0;

    while (true) {
      if (rule.until != null) {
        if (cursor.isAfter(rule.until)) {
          break;
        }
      } else {
        if (created >= rule.occurrences) {
          break;
        }
      }

      DayOfWeek dow = cursor.getDayOfWeek();
      if (rule.days.contains(dow)) {
        LocalDateTime s = LocalDateTime.of(cursor, seriesStartTime);
        LocalDateTime e = s.plus(span);

        if (!s.toLocalDate().equals(e.toLocalDate())) {
          throw new IllegalArgumentException(
              "Series instance would span multiple days");
        }
        String t = key(base.subject(), s, e);
        if (uniqueTriples.contains(t)) {
          throw new IllegalArgumentException(
              "Duplicate event would be created in series: " + t);
        }
        for (Event already : planned) {
          String t2 = key(already.subject(), already.start(), already.end());
          if (t.equals(t2)) {
            throw new IllegalArgumentException(
                "Duplicate within the same series batch: " + t);
          }
        }

        EventId id = new EventId();
        Event ev = new Event(
            id,
            sid,
            base.subject(),
            s,
            e,
            base.description(),
            base.location(),
            base.status(),
            baseAllDay
        );
        planned.add(ev);
        created++;
      }

      cursor = cursor.plusDays(1);

    }

    List<EventId> ids = new ArrayList<>();
    for (Event ev : planned) {
      index(ev);
      ids.add(ev.id());
    }
    return ids;
  }

  /**
   * Retrieves all events on a specific date.
   *
   * @param date the date to query.
   * @return list of events occurring on that date, sorted by start time.
   */
  @Override
  public List<Event> eventsOn(LocalDate date) {
    if (date == null) {
      throw new IllegalArgumentException("Date cannot be null");
    }
    List<EventId> ids = dateIndex.get(date);
    if (ids == null || ids.isEmpty()) {
      return Collections.emptyList();
    }
    List<Event> list = new ArrayList<>(ids.size());
    for (EventId id : ids) {
      Event e = entries.get(id);
      if (e != null) {
        list.add(e);
      }
    }
    list.sort(Comparator.comparing(Event::start));
    return Collections.unmodifiableList(list);
  }

  /**
   * Retrieves all events between two date time occurrences.
   *
   * @param start the date of start.
   * @param end   the date of end.
   * @return list of events occurring between that time frame.
   */
  @Override
  public List<Event> eventsBetween(LocalDateTime start, LocalDateTime end) {
    if (start == null || end == null) {
      throw new IllegalArgumentException("Start and end cannot be null");
    }
    if (end.isBefore(start)) {
      throw new IllegalArgumentException("Range end before start");
    }
    List<Event> list = new ArrayList<>();
    for (Event e : entries.values()) {
      boolean overlaps = !(e.end().isBefore(start) || e.start().isAfter(end));
      if (overlaps) {
        list.add(e);
      }
    }
    list.sort(Comparator.comparing(Event::start));
    return Collections.unmodifiableList(list);
  }

  /**
   * Checks if any event is occurring at a given time.
   *
   * @param at the date-time to check.
   * @return true if an event is active at that time, false otherwise.
   */
  @Override
  public boolean isBusy(LocalDateTime at) {
    if (at == null) {
      throw new IllegalArgumentException("DateTime cannot be null");
    }
    List<EventId> ids = dateIndex.get(at.toLocalDate());
    if (ids == null) {
      return false;
    }
    for (EventId id : ids) {
      Event e = entries.get(id);
      if (e == null) {
        continue;
      }
      boolean in = !e.start().isAfter(at) && e.end().isAfter(at);
      if (in) {
        return true;
      }
    }
    return false;
  }

  /**
   * Edits one or more events based on a selector, scope, and property change.
   *
   * @param selector identifies which event(s) to edit
   * @param scope    determines how broadly to apply the edit (single, from-here, or all)
   * @param change   describes the change to apply
   */
  @Override
  public void edit(EventSelector selector, EditScope scope, PropertyChange change) {
    if (selector == null || change == null || scope == null) {
      throw new IllegalArgumentException("Selector, scope, and change are required");
    }

    List<Event> matched = findBy(selector);
    if (matched.isEmpty()) {
      throw new IllegalArgumentException(
          "No event found for subject '" + selector.subject() + "' at " + selector.start());
    }
    if (matched.size() > 1) {
      throw new IllegalArgumentException("Ambiguous search — multiple matching events");
    }

    Event target = matched.get(0);

    List<Event> toEdit = new ArrayList<>();
    if (target.seriesId() == null || scope == EditScope.INSTANCE) {
      toEdit.add(target);
    } else {
      NavigableSet<EventId> seriesSet = seriesIndex.get(target.seriesId());
      if (seriesSet == null || seriesSet.isEmpty()) {
        throw new IllegalStateException("Series index inconsistent");
      }
      for (EventId id : seriesSet) {
        Event e = entries.get(id);
        if (scope == EditScope.FROM_HERE) {
          if (!e.start().isBefore(target.start())) {
            toEdit.add(e);
          }
        } else {
          toEdit.add(e);
        }
      }
    }

    if (toEdit.isEmpty()) {
      throw new IllegalStateException("No events to edit for given scope");
    }

    List<Event> newEvents = new ArrayList<>();
    for (Event e : toEdit) {
      Event updated = applyChange(e, change);

      String newKey = key(updated.subject(), updated.start(), updated.end());
      if (!newKey.equals(key(e.subject(), e.start(), e.end())) && uniqueTriples.contains(newKey)) {
        throw new IllegalArgumentException(
            "Edit would create duplicate event: " + updated.subject() + " "
                + updated.start() + "–" + updated.end());
      }
      newEvents.add(updated);
    }

    for (Event oldEvent : toEdit) {
      unindex(oldEvent);
    }
    for (int i = 0; i < newEvents.size(); i++) {
      Event oldEvent = toEdit.get(i);
      Event newEvent = newEvents.get(i);

      boolean startChanged = !oldEvent.start().equals(newEvent.start());

      if (startChanged) {
        switch (scope) {
          case INSTANCE:
            newEvent = newEvent.withSeriesId(null);
            break;
          case FROM_HERE:
            SeriesId newSid = new SeriesId();
            newEvent = newEvent.withSeriesId(newSid);
            for (int j = i + 1; j < newEvents.size(); j++) {
              newEvents.set(j, newEvents.get(j).withSeriesId(newSid));
            }
            break;
          default:
            newEvent = newEvent.withSeriesId(oldEvent.seriesId());
            break;
        }
      }
      index(newEvent);
    }

  }

  /**
   * Finds events matching the given selector.
   *
   * @param selector the selector criteria
   * @return list of matching events
   */
  private List<Event> findBy(EventSelector selector) {
    LocalDateTime normalizedStart = selector.start().withSecond(0).withNano(0);
    String ssk = ssKey(selector.subject(), normalizedStart);
    List<EventId> ids = subjectStartIndex.get(ssk);
    if (ids == null) {
      return Collections.emptyList();
    }

    List<Event> list = new ArrayList<>();
    for (EventId id : ids) {
      Event e = entries.get(id);
      if (selector.end().isPresent()) {
        if (e.end() != null && e.end().equals(selector.end().get())) {
          list.add(e);
        }
      } else {
        list.add(e);
      }
    }
    return list;
  }

  /**
   * Applies a PropertyChange to an Event.
   *
   * @param e      the original event
   * @param change the property change to apply
   * @return a new event instance with the change applied
   */
  private Event applyChange(Event e, PropertyChange change) {
    switch (change.kind()) {
      case SUBJECT:
        String newSubject = change.stringValue();
        if (newSubject == null || newSubject.isBlank()) {
          throw new IllegalArgumentException("Subject cannot be empty");
        }
        return e.withSubject(newSubject);

      case DESCRIPTION:
        return e.withDescription(change.stringValue() == null ? "" : change.stringValue());

      case LOCATION:
        return e.withLocation(change.stringValue() == null ? "" : change.stringValue());

      case STATUS:
        String statusStr = change.stringValue();
        if (statusStr == null || statusStr.isBlank()) {
          throw new IllegalArgumentException("Status cannot be empty");
        }
        EventSpec.Status st;
        try {
          st = EventSpec.Status.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException ex) {
          throw new IllegalArgumentException("Invalid status: " + statusStr);
        }
        return e.withStatus(st);

      case START:
        LocalDateTime newStart = change.dateTimeValue();
        if (newStart == null) {
          throw new IllegalArgumentException("Start date-time required");
        }
        return e.withStart(newStart);

      case END:
        LocalDateTime newEnd = change.dateTimeValue();
        if (newEnd == null) {
          throw new IllegalArgumentException("End date-time required");
        }
        return e.withEnd(newEnd);

      default:
        throw new IllegalArgumentException("Unknown property: " + change.kind());
    }
  }

  /**
   * Exports all stored events to the specified file using the given exporter.
   *
   * @param exporter the exporter implementation
   * @param file     the output file path
   * @throws java.io.IOException if export fails
   */
  @Override
  public void export(Exporter exporter, Path file) throws java.io.IOException {
    List<Event> all = new ArrayList<>(entries.values());
    all.sort(Comparator.comparing(Event::start));
    exporter.export(all, this.getTimezone(), file);
  }

  @Override
  public String getTimezone() {
    return timezone;
  }

  @Override
  public void setTimezone(String newTimezone) {
    if (newTimezone == null || newTimezone.isBlank()) {
      throw new IllegalArgumentException("Timezone cannot be null or empty");
    }
    ZoneId newZoneId;
    try {
      newZoneId = ZoneId.of(newTimezone);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid timezone: " + newTimezone);
    }
    if (this.timezone.equals(newTimezone)) {
      return;
    }
    ZoneId oldZoneId = ZoneId.of(this.timezone);
    convertAllEventsToNewTimezone(oldZoneId, newZoneId);
    this.timezone = newTimezone;
  }

  /**
   * Converts all events from one timezone to another.
   * This maintains the same absolute moment in time but adjusts the local time representation.
   *
   * @throws IllegalArgumentException if conversion would create duplicates or violate constraints
   */
  private void convertAllEventsToNewTimezone(ZoneId oldZone, ZoneId newZone) {
    Map<EventId, Event> convertedEvents = new LinkedHashMap<>();
    Set<String> newTriples = new HashSet<>();

    for (Event event : entries.values()) {
      Event convertedEvent;

      if (event.allDay()) {
        convertedEvent = event;
      } else {
        ZonedDateTime oldStartZoned = ZonedDateTime.of(event.start(), oldZone);
        ZonedDateTime newStartZoned = oldStartZoned.withZoneSameInstant(newZone);
        LocalDateTime newStart = newStartZoned.toLocalDateTime();
        ZonedDateTime oldEndZoned = ZonedDateTime.of(event.end(), oldZone);
        ZonedDateTime newEndZoned = oldEndZoned.withZoneSameInstant(newZone);
        LocalDateTime newEnd = newEndZoned.toLocalDateTime();
        if (event.seriesId() != null && !newStart.toLocalDate().equals(newEnd.toLocalDate())) {
          throw new IllegalArgumentException(
              "Timezone change would cause event '" + event.subject()
                  + "' to span multiple days");
        }

        if (event.seriesId() != null) {
          DayOfWeek oldDay = event.start().getDayOfWeek();
          DayOfWeek newDay = newStart.getDayOfWeek();
          if (oldDay != newDay) {
            throw new IllegalArgumentException(
                "Timezone change would shift series event '" + event.subject()
                    + "' to a different weekday (from " + oldDay + " to " + newDay + ")");
          }
        }

        convertedEvent = new Event(
            event.id(),
            event.seriesId(),
            event.subject(),
            newStart,
            newEnd,
            event.description(),
            event.location(),
            event.status(),
            false
        );
      }

      String triple = key(convertedEvent.subject(), convertedEvent.start(), convertedEvent.end());
      if (newTriples.contains(triple)) {
        throw new IllegalArgumentException(
            "Timezone change would create duplicate event: " + convertedEvent.subject()
                + " from " + convertedEvent.start() + " to " + convertedEvent.end());
      }
      newTriples.add(triple);
      convertedEvents.put(event.id(), convertedEvent);
    }


    List<Event> oldEvents = new ArrayList<>(entries.values());
    for (Event oldEvent : oldEvents) {
      unindex(oldEvent);
    }

    for (Event convertedEvent : convertedEvents.values()) {
      index(convertedEvent);
    }
  }

}