package calendar.model;

import calendar.model.utils.DateTimeCheck;
import calendar.model.utils.DayOfWeek;
import calendar.model.utils.EventStatus;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implements the {@link Calendar} interface for a SINGLE calendar.
 * All event times are stored as LocalDateTime, relative to this calendar's ZoneId.
 */
public class InMemoryCalendar implements Calendar {

  private final Set<EventSingle> events;
  private final Map<EventSingle, UUID> eventToSeriesId;
  private final Map<UUID, Set<EventSingle>> seriesIdToEvents;
  private final Map<UUID, EventSeries> seriesTemplates;
  private final ZoneId zoneId;

  /**
   * Constructs a new in-memory calendar for a specific timezone.
   *
   * @param zoneId The timezone for this calendar.
   */
  public InMemoryCalendar(ZoneId zoneId) {
    if (zoneId == null) {
      throw new IllegalArgumentException("ZoneId cannot be null.");
    }
    this.zoneId = zoneId;
    this.events = new HashSet<>();
    this.eventToSeriesId = new HashMap<>();
    this.seriesIdToEvents = new HashMap<>();
    this.seriesTemplates = new HashMap<>();
  }

  @Override
  public ZoneId getZoneId() {
    return this.zoneId;
  }

  @Override
  public void createSingleEvent(EventSingle newEvent) {
    if (events.contains(newEvent)) {
      throw new IllegalArgumentException("Duplicate event detected: " + newEvent);
    }
    DateTimeCheck.validateStartBeforeEnd(newEvent.getStart(), newEvent.getEnd());
    events.add(newEvent);
  }

  @Override
  public void createEventSeries(EventSeries seriesTemplate) {
    List<EventSingle> generatedEvents = generateSeriesEvents(seriesTemplate);

    for (EventSingle event : generatedEvents) {
      if (events.contains(event)) {
        throw new IllegalArgumentException(
            "Series conflicts with existing event: " + event);
      }
    }

    UUID seriesId = UUID.randomUUID();
    this.seriesTemplates.put(seriesId, seriesTemplate);

    for (EventSingle event : generatedEvents) {
      events.add(event);
      eventToSeriesId.put(event, seriesId);
      seriesIdToEvents.computeIfAbsent(seriesId, k -> new HashSet<>()).add(event);
    }
  }

  @Override
  public void editEvent(
      String subject,
      LocalDateTime start,
      LocalDateTime end,
      String propertyName,
      Object newValue) {

    EventSingle target = findUniqueEvent(subject, start, end);
    replaceEvent(target, propertyName, newValue);
  }

  @Override
  public void editEventAndFuture(
      String subject,
      LocalDateTime start,
      String propertyName,
      Object newValue) {

    EventSingle target = findEventByStart(subject, start);
    UUID seriesId = eventToSeriesId.get(target);

    if (seriesId == null) {
      replaceEvent(target, propertyName, newValue);
      return;
    }

    List<EventSingle> eventsToModify = seriesIdToEvents.get(seriesId).stream()
        .filter(e -> !e.getStart().isBefore(target.getStart()))
        .sorted(Comparator.comparing(EventSingle::getStart))
        .collect(Collectors.toList());

    UUID newSeriesId = UUID.randomUUID();
    EventSeries oldTemplate = seriesTemplates.get(seriesId);

    if (oldTemplate != null) {
      try {
        EventSeries.Builder newTemplateBuilder = new EventSeries.Builder(oldTemplate)
            .withStartDate(target.getStart().toLocalDate())
            .clearEndCondition();

        if (oldTemplate.getOccurrences() != null) {
          newTemplateBuilder.forOccurrences(eventsToModify.size());
        } else if (oldTemplate.getUntilDate() != null) {
          newTemplateBuilder.until(oldTemplate.getUntilDate());
        } else {
          newTemplateBuilder.forOccurrences(eventsToModify.size());
        }

        if (isTimingProperty(propertyName) && newValue instanceof LocalDateTime) {
          LocalTime newTime = ((LocalDateTime) newValue).toLocalTime();
          if (propertyName.equals("start")) {
            newTemplateBuilder.withStartTime(newTime);
            Duration duration = Duration.between(oldTemplate.getStartTime(),
                oldTemplate.getEndTime());
            newTemplateBuilder.withEndTime(newTime.plus(duration));
          } else {
            newTemplateBuilder.withEndTime(newTime);
          }
        } else if (propertyName.equals("subject")) {
          newTemplateBuilder.withSubject((String) newValue);
        }

        seriesTemplates.put(newSeriesId, newTemplateBuilder.build());
      } catch (Exception e) {
        // Fallback: If build fails just don't save template.
      }
    }

    for (EventSingle event : eventsToModify) {
      seriesIdToEvents.get(seriesId).remove(event);
      eventToSeriesId.put(event, newSeriesId);
      seriesIdToEvents.computeIfAbsent(newSeriesId, k -> new HashSet<>()).add(event);
    }

    for (EventSingle event : new ArrayList<>(eventsToModify)) {
      replaceEventInSeries(event, propertyName, newValue);
    }
  }

  @Override
  public void editFullSeries(
      String subject,
      LocalDateTime start,
      String propertyName,
      Object newValue) {

    EventSingle target = findEventByStart(subject, start);
    UUID seriesId = eventToSeriesId.get(target);

    if (seriesId == null) {
      replaceEvent(target, propertyName, newValue);
      return;
    }

    if (!isTimingProperty(propertyName)) {
      EventSeries oldTemplate = seriesTemplates.get(seriesId);
      if (oldTemplate != null) {
        EventSeries.Builder newTemplateBuilder = new EventSeries.Builder(oldTemplate);
        switch (propertyName.toLowerCase()) {
          case "subject":
            newTemplateBuilder.withSubject((String) newValue);
            break;
          case "description":
            newTemplateBuilder.withDescription((String) newValue);
            break;
          case "location":
            newTemplateBuilder.withLocation((String) newValue);
            break;
          case "status":
            newTemplateBuilder.withStatus((EventStatus) newValue);
            break;
          default:
            break;
        }
        seriesTemplates.put(seriesId, newTemplateBuilder.build());
      }
    }

    Set<EventSingle> eventsToModify = new HashSet<>(seriesIdToEvents.get(seriesId));

    for (EventSingle event : eventsToModify) {
      replaceEventInSeries(event, propertyName, newValue);
    }
  }

  @Override
  public List<EventSingle> getEventsOn(LocalDate date) {
    return events.stream()
        .filter(event ->
            event.getStart().toLocalDate().equals(date)
                || event.getEnd().toLocalDate().equals(date)
                || (event.getStart().toLocalDate().isBefore(date)
                && event.getEnd().toLocalDate().isAfter(date)))
        .sorted(Comparator.comparing(EventSingle::getStart))
        .collect(Collectors.toList());
  }

  @Override
  public List<EventSingle> getEventsInRange(LocalDateTime start, LocalDateTime end) {
    return events.stream()
        .filter(event -> event.overlaps(start, end))
        .sorted(Comparator.comparing(EventSingle::getStart))
        .collect(Collectors.toList());
  }

  @Override
  public List<EventSingle> getAllEvents() {
    return new ArrayList<>(events);
  }

  @Override
  public boolean isBusy(LocalDateTime dateTime) {
    return events.stream().anyMatch(event -> event.occursAt(dateTime));
  }

  /**
   * Finds a unique event by subject, start, and end.
   */
  private EventSingle findUniqueEvent(String subject, LocalDateTime start, LocalDateTime end) {
    EventSingle searchKey = new EventSingle.Builder(subject, start).withEnd(end).build();

    List<EventSingle> matches = events.stream()
        .filter(e -> e.equals(searchKey))
        .collect(Collectors.toList());

    if (matches.isEmpty()) {
      throw new IllegalArgumentException("No matching event found.");
    }
    return matches.get(0);
  }

  /**
   * Finds a unique event by subject and start time.
   */
  private EventSingle findEventByStart(String subject, LocalDateTime start) {
    List<EventSingle> matches = events.stream()
        .filter(e -> e.getSubject().equals(subject) && e.getStart().equals(start))
        .collect(Collectors.toList());

    if (matches.isEmpty()) {
      throw new IllegalArgumentException("No event found with matching subject and start time.");
    }
    if (matches.size() > 1) {
      throw new IllegalArgumentException("Multiple events match. Please use 'edit event' "
          + "and specify the end time to be unique.");
    }
    return matches.get(0);
  }

  /**
   * Replaces a single event with a modified version.
   */
  private void replaceEvent(EventSingle oldEvent, String propertyName, Object newValue) {
    EventSingle newEvent = createModifiedEvent(oldEvent, propertyName, newValue, false);

    if (events.contains(newEvent) && !newEvent.equals(oldEvent)) {
      throw new IllegalArgumentException("Modification creates a conflict with an existing event.");
    }

    events.remove(oldEvent);
    events.add(newEvent);

    UUID seriesId = eventToSeriesId.remove(oldEvent);
    if (seriesId != null) {
      eventToSeriesId.put(newEvent, seriesId);
      seriesIdToEvents.get(seriesId).remove(oldEvent);
      seriesIdToEvents.get(seriesId).add(newEvent);
    }
  }

  /**
   * Replaces an event that's part of a series, applying series-aware timing logic.
   */
  private void replaceEventInSeries(EventSingle oldEvent, String propertyName, Object newValue) {
    EventSingle newEvent = createModifiedEvent(oldEvent, propertyName, newValue, true);

    if (events.contains(newEvent) && !newEvent.equals(oldEvent)) {
      throw new IllegalArgumentException(
          "Modification creates a conflict with an existing event: " + newEvent);
    }

    events.remove(oldEvent);
    events.add(newEvent);

    UUID seriesId = eventToSeriesId.remove(oldEvent);
    if (seriesId != null) {
      eventToSeriesId.put(newEvent, seriesId);
      seriesIdToEvents.get(seriesId).remove(oldEvent);
      seriesIdToEvents.get(seriesId).add(newEvent);
    }
  }

  /**
   * Creates a modified copy of an event.
   */
  private EventSingle createModifiedEvent(EventSingle original, String propertyName,
                                          Object newValue, boolean isSeriesEdit) {
    EventSingle.Builder builder = new EventSingle.Builder(original);

    if (isSeriesEdit && isTimingProperty(propertyName) && newValue instanceof LocalDateTime) {
      LocalTime newTime = ((LocalDateTime) newValue).toLocalTime();
      Duration duration = Duration.between(original.getStart(), original.getEnd());

      if (propertyName.equals("start")) {
        LocalDateTime newStart = original.getStart().toLocalDate().atTime(newTime);
        LocalDateTime newEnd = newStart.plus(duration);
        DateTimeCheck.validateSingleDayEvent(newStart, newEnd);
        builder.withStart(newStart);
        builder.withEnd(newEnd);
      } else if (propertyName.equals("end")) {
        LocalDateTime newEnd = original.getEnd().toLocalDate().atTime(newTime);
        DateTimeCheck.validateSingleDayEvent(original.getStart(), newEnd);
        if (newEnd.isBefore(original.getStart()) || newEnd.equals(original.getStart())) {
          throw new IllegalArgumentException(
              "New end time " + newEnd + " must be after start time " + original.getStart());
        }
        builder.withEnd(newEnd);
      }
    } else {
      switch (propertyName.toLowerCase()) {
        case "subject":
          builder.withSubject((String) newValue);
          break;
        case "start":
          builder.withStart((LocalDateTime) newValue);
          break;
        case "end":
          builder.withEnd((LocalDateTime) newValue);
          break;
        case "description":
          builder.withDescription((String) newValue);
          break;
        case "location":
          builder.withLocation((String) newValue);
          break;
        case "status":
          builder.withStatus((EventStatus) newValue);
          break;
        default:
          throw new IllegalArgumentException("Unknown property: " + propertyName);
      }
    }

    return builder.build();
  }

  /**
   * Generates all concrete events for a series.
   */
  private List<EventSingle> generateSeriesEvents(EventSeries series) {
    List<EventSingle> generated = new ArrayList<>();
    LocalDate current = series.getStartDate();
    int count = 0;
    int maxIterations = 10000;

    while (shouldContinueSeries(series, count, current) && maxIterations-- > 0) {
      if (series.getDaysOfWeek().contains(getDayOfWeek(current))) {
        LocalDateTime start = LocalDateTime.of(current, series.getStartTime());
        LocalDateTime end = LocalDateTime.of(current, series.getEndTime());

        DateTimeCheck.validateSingleDayEvent(start, end);

        EventSingle event = new EventSingle.Builder(series.getSubject(), start)
            .withEnd(end)
            .withDescription(series.getDescription())
            .withLocation(series.getLocation())
            .withStatus(series.getStatus())
            .build();

        generated.add(event);
        count++;
      }
      current = current.plusDays(1);
    }
    return generated;
  }

  private boolean shouldContinueSeries(EventSeries series, int count, LocalDate current) {
    if (series.getOccurrences() != null) {
      return count < series.getOccurrences();
    } else {
      return !current.isAfter(series.getUntilDate());
    }
  }

  private DayOfWeek getDayOfWeek(LocalDate date) {
    java.time.DayOfWeek jdkDay = date.getDayOfWeek();
    switch (jdkDay) {
      case MONDAY:
        return DayOfWeek.MONDAY;
      case TUESDAY:
        return DayOfWeek.TUESDAY;
      case WEDNESDAY:
        return DayOfWeek.WEDNESDAY;
      case THURSDAY:
        return DayOfWeek.THURSDAY;
      case FRIDAY:
        return DayOfWeek.FRIDAY;
      case SATURDAY:
        return DayOfWeek.SATURDAY;
      default:
        return DayOfWeek.SUNDAY;
    }
  }

  private boolean isTimingProperty(String propertyName) {
    return "start".equals(propertyName) || "end".equals(propertyName);
  }

  /**
   * Finds the original EventSeries template for a given event.
   * Package-private for use by InMemoryCalendarApplication.
   *
   * @param event The event to check.
   * @return The EventSeries template.
   */
  public EventSeries getSeriesTemplateForEvent(EventSingle event) {
    UUID seriesId = eventToSeriesId.get(event);
    if (seriesId != null) {
      return seriesTemplates.get(seriesId);
    }
    return null;
  }

  /**
   * Gets the unique series ID for a given event.
   * Package-private for use by InMemoryCalendarApplication.
   *
   * @param event The event to check.
   * @return The UUID of the series, or null if the event is not part of a series.
   */
  UUID getSeriesIdForEvent(EventSingle event) {
    return eventToSeriesId.get(event);
  }

  /**
   * Gets the original EventSeries template by its unique ID.
   * Package-private for use by InMemoryCalendarApplication.
   *
   * @param seriesId The unique ID of the series.
   * @return The EventSeries template, or null if not found.
   */
  EventSeries getSeriesTemplateById(UUID seriesId) {
    return seriesTemplates.get(seriesId);
  }
}