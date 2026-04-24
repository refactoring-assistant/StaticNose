package calendar.service;

import calendar.exception.CalendarException;
import calendar.exception.DuplicateEventException;
import calendar.exception.EventNotFoundException;
import calendar.exception.InvalidDateTimeException;
import calendar.model.EventSeries;
import calendar.model.EventStatus;
import calendar.model.InCalendar;
import calendar.model.InEvent;
import calendar.model.RecurrencePattern;
import calendar.model.RecurringEvent;
import calendar.model.SingleEvent;
import calendar.model.Weekday;
import calendar.util.DateTimeParser;
import calendar.util.ValidationUtil;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of event service handling business logic.
 * Manages event creation, editing, and querying operations.
 * - Uses InCalendar interface for loose coupling
 * - Maintains eventSeriesMap for efficient series tracking
 * - Delegates validation to ValidationUtil (SRP)
 * - Immutable collections returned to prevent external modification
 */
public class EventService implements InEventService {

  private static final int MAX_OCCURRENCES = 1000;
  private static final int MAX_DAYS_SPAN = 3650;

  private static final String PROPERTY_SUBJECT = "subject";
  private static final String PROPERTY_START = "start";
  private static final String PROPERTY_END = "end";
  private static final String PROPERTY_DESCRIPTION = "description";
  private static final String PROPERTY_LOCATION = "location";
  private static final String PROPERTY_STATUS = "status";

  private final InCalendar calendar;
  private final Map<String, EventSeries> eventSeriesMap;

  /**
   * Constructs an EventService with a calendar.
   *
   * @param calendar the calendar to manage
   * @throws IllegalArgumentException if calendar is null
   */
  public EventService(InCalendar calendar) {
    ValidationUtil.validateNotNull(calendar, "Calendar");
    this.calendar = calendar;
    this.eventSeriesMap = new HashMap<>();
  }

  @Override
  public void createSingleEvent(String subject, LocalDateTime start,
                                LocalDateTime end, Map<String, String> optionalProps)
      throws DuplicateEventException {
    ValidationUtil.validateNotEmpty(subject, "Subject");
    ValidationUtil.validateNotNull(start, "Start date/time");

    EventProperties props = extractEventProperties(optionalProps);

    SingleEvent event = new SingleEvent(subject, start, end,
        props.description, props.location, props.status);
    calendar.addEvent(event);
  }

  @Override
  public void createEventSeries(String subject, LocalDateTime start, LocalDateTime end,
                                Set<Weekday> weekdays, int occurrences,
                                Map<String, String> optionalProps)
      throws CalendarException {
    ValidationUtil.validateNotEmpty(subject, "Subject");
    ValidationUtil.validateNotNull(start, "Start date/time");

    validateSeriesParameters(weekdays, occurrences, start.toLocalDate(), null);

    if (end != null) {
      ValidationUtil.validateRecurringSameDay(start, end);
    }

    EventProperties props = extractEventProperties(optionalProps);

    RecurrencePattern pattern = new RecurrencePattern(
        weekdays,
        start.toLocalTime(),
        end != null ? end.toLocalTime() : null,
        occurrences
    );

    createAndStoreEventSeries(subject, start, pattern, props);
  }

  @Override
  public void createEventSeriesUntil(String subject, LocalDateTime start, LocalDateTime end,
                                     Set<Weekday> weekdays, LocalDate endDate,
                                     Map<String, String> optionalProps)
      throws CalendarException {
    ValidationUtil.validateNotEmpty(subject, "Subject");
    ValidationUtil.validateNotNull(start, "Start date/time");
    ValidationUtil.validateNotNull(endDate, "End date");

    validateSeriesParameters(weekdays, null, start.toLocalDate(), endDate);

    if (end != null) {
      ValidationUtil.validateRecurringSameDay(start, end);
    }

    EventProperties props = extractEventProperties(optionalProps);

    RecurrencePattern pattern = new RecurrencePattern(
        weekdays,
        start.toLocalTime(),
        end != null ? end.toLocalTime() : null,
        endDate
    );

    createAndStoreEventSeries(subject, start, pattern, props);
  }

  @Override
  public void editSingleEvent(String subject, LocalDateTime start,
                              String property, String newValue)
      throws CalendarException {
    List<InEvent> matchingEvents = findMatchingEvents(subject, start);

    validateSingleMatch(matchingEvents, subject, start);

    InEvent event = matchingEvents.get(0);
    applyPropertyChange(event, property, newValue);
  }

  @Override
  public void editSeriesFromDate(String subject, LocalDateTime start,
                                 String property, String newValue)
      throws CalendarException {
    List<InEvent> matchingEvents = findMatchingEvents(subject, start);

    if (matchingEvents.isEmpty()) {
      throw new EventNotFoundException("No event found with subject: " + subject + " at " + start);
    }

    InEvent event = matchingEvents.get(0);

    if (event instanceof RecurringEvent) {
      RecurringEvent recurringEvent = (RecurringEvent) event;
      EventSeries series = recurringEvent.getParentSeries();

      if (series != null) {
        if (PROPERTY_START.equals(property)) {
          handleSeriesStartTimeChange(subject, start, newValue, series);
          return;
        } else {
          updateSeriesFromDate(series, start.toLocalDate(), property, newValue);
          return;
        }
      }
    }

    applyPropertyChange(event, property, newValue);
  }

  @Override
  public void editEntireSeries(String subject, LocalDateTime start,
                               String property, String newValue)
      throws CalendarException {
    List<InEvent> matchingEvents = findMatchingEvents(subject, start);

    if (matchingEvents.isEmpty()) {
      throw new EventNotFoundException("No event found with subject: " + subject + " at " + start);
    }

    InEvent event = matchingEvents.get(0);

    if (event instanceof RecurringEvent) {
      RecurringEvent recurringEvent = (RecurringEvent) event;
      EventSeries series = recurringEvent.getParentSeries();

      if (series != null) {
        updateEntireSeries(series, property, newValue);
        return;
      }
    }

    applyPropertyChange(event, property, newValue);
  }

  @Override
  public List<InEvent> queryEventsOnDate(LocalDate date) {
    return Collections.unmodifiableList(calendar.getEventsOnDate(date));
  }

  @Override
  public List<InEvent> queryEventsBetween(LocalDateTime start, LocalDateTime end) {
    return Collections.unmodifiableList(calendar.getEventsBetween(start, end));
  }

  @Override
  public boolean checkBusyStatus(LocalDateTime dateTime) {
    return calendar.isBusyAt(dateTime);
  }

  @Override
  public List<InEvent> getAllEvents() {
    return Collections.unmodifiableList(calendar.getAllEvents());
  }

  private void validateSeriesParameters(Set<Weekday> weekdays, Integer occurrences,
                                        LocalDate startDate, LocalDate endDate)
      throws CalendarException {
    if (weekdays == null || weekdays.isEmpty()) {
      throw new CalendarException("Weekdays cannot be empty for recurring events");
    }

    if (occurrences != null && occurrences > MAX_OCCURRENCES) {
      throw new CalendarException(
          "Cannot create series with more than " + MAX_OCCURRENCES + " occurrences. "
              + "Requested: " + occurrences);
    }

    if (endDate != null && startDate != null) {
      long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
      if (daysBetween > MAX_DAYS_SPAN) {
        throw new CalendarException(
            "Cannot create series spanning more than 10 years. "
                + "Date range: " + daysBetween + " days");
      }
    }
  }

  private EventProperties extractEventProperties(Map<String, String> optionalProps) {
    String description = optionalProps.get(PROPERTY_DESCRIPTION);
    String location = optionalProps.get(PROPERTY_LOCATION);
    EventStatus status = parseStatus(optionalProps.get(PROPERTY_STATUS));
    return new EventProperties(description, location, status);
  }

  private void createAndStoreEventSeries(String subject, LocalDateTime start,
                                         RecurrencePattern pattern,
                                         EventProperties props)
      throws DuplicateEventException {
    EventSeries series = new EventSeries(subject, pattern,
        props.description, props.location, props.status);
    series.generateInstances(start.toLocalDate(),
        props.description, props.location, props.status);

    for (RecurringEvent event : series.getInstances()) {
      calendar.addEvent(event);
    }

    String seriesKey = generateSeriesKey(subject, start);
    eventSeriesMap.put(seriesKey, series);
  }

  private void validateSingleMatch(List<InEvent> matchingEvents, String subject,
                                   LocalDateTime start) throws CalendarException {
    if (matchingEvents.isEmpty()) {
      throw new EventNotFoundException("No event found with subject: " + subject + " at " + start);
    }

    if (matchingEvents.size() > 1) {
      throw new CalendarException(
          "Multiple events found with subject '" + subject + "' starting at " + start + ". "
              + "Cannot determine which to edit. Please be more specific.");
    }
  }

  private void handleSeriesStartTimeChange(String subject, LocalDateTime start,
                                           String newValue, EventSeries series)
      throws CalendarException {
    LocalDate fromDate = start.toLocalDate();
    List<RecurringEvent> splitEvents = series.splitSeries(fromDate);

    if (splitEvents.isEmpty()) {
      throw new EventNotFoundException(
          "No events found in series starting from " + fromDate);
    }

    LocalDateTime newStartTime = DateTimeParser.parseDateTime(newValue);
    EventProperties props = extractPropertiesFromEvent(splitEvents.get(0));
    RecurrencePattern oldPattern = series.getPattern();

    RecurrencePattern newPattern = createAdjustedPattern(oldPattern,
        newStartTime, splitEvents.size());

    EventSeries newSeries = new EventSeries(subject, newPattern,
        props.description, props.location, props.status);

    updateSplitEvents(splitEvents, newStartTime, newSeries);

    String newSeriesKey = generateSeriesKey(subject, newStartTime);
    eventSeriesMap.put(newSeriesKey, newSeries);
  }

  private RecurrencePattern createAdjustedPattern(RecurrencePattern oldPattern,
                                                  LocalDateTime newStartTime,
                                                  int eventCount) {
    if (oldPattern.hasOccurrenceCount()) {
      return new RecurrencePattern(
          oldPattern.getWeekdays(),
          newStartTime.toLocalTime(),
          oldPattern.getEndTime(),
          eventCount
      );
    } else {
      return new RecurrencePattern(
          oldPattern.getWeekdays(),
          newStartTime.toLocalTime(),
          oldPattern.getEndTime(),
          oldPattern.getEndDate()
      );
    }
  }

  private void updateSplitEvents(List<RecurringEvent> splitEvents,
                                 LocalDateTime newStartTime,
                                 EventSeries newSeries)
      throws DuplicateEventException {
    for (RecurringEvent splitEvent : splitEvents) {
      removeEventSafely(splitEvent);

      LocalDateTime oldStart = splitEvent.getStartDateTime();
      LocalDateTime oldEnd = splitEvent.getEndDateTime();

      LocalDateTime newStart = LocalDateTime.of(
          oldStart.toLocalDate(),
          newStartTime.toLocalTime()
      );

      if (!splitEvent.isAllDayEvent()) {
        long durationMinutes = java.time.Duration.between(oldStart, oldEnd).toMinutes();
        LocalDateTime newEnd = newStart.plusMinutes(durationMinutes);
        splitEvent.setEndDateTime(newEnd);
        splitEvent.setStartDateTime(newStart);
      } else {
        splitEvent.setStartDateTime(newStart);
      }

      newSeries.addInstance(splitEvent);
      calendar.addEvent(splitEvent);
    }
  }

  private void removeEventSafely(InEvent event) {
    try {
      calendar.removeEvent(event);
    } catch (EventNotFoundException e) {
      // Safe to ignore, as the event may not be in calendar if already removed.
    }
  }

  private void updateSeriesFromDate(EventSeries series, LocalDate fromDate,
                                    String property, String newValue)
      throws CalendarException {
    series.updateSeriesFrom(fromDate, e -> {
      try {
        applyPropertyChange(e, property, newValue);
      } catch (CalendarException ex) {
        throw new RuntimeException(ex);
      }
    });
  }

  private void updateEntireSeries(EventSeries series, String property, String newValue)
      throws CalendarException {
    series.updateEntireSeries(e -> {
      try {
        applyPropertyChange(e, property, newValue);
      } catch (CalendarException ex) {
        throw new RuntimeException(ex);
      }
    });
  }

  private EventProperties extractPropertiesFromEvent(RecurringEvent event) {
    return new EventProperties(
        event.getDescription().orElse(null),
        event.getLocation().orElse(null),
        event.getStatus()
    );
  }

  private List<InEvent> findMatchingEvents(String subject, LocalDateTime start) {
    return calendar.getAllEvents().stream()
        .filter(e -> e.getSubject().equals(subject) && e.getStartDateTime().equals(start))
        .collect(Collectors.toList());
  }

  private void applyPropertyChange(InEvent event, String property, String newValue)
      throws CalendarException {
    try {
      switch (property.toLowerCase()) {
        case "subject":
          event.setSubject(cleanQuotes(newValue));
          break;
        case "start":
          LocalDateTime newStart = DateTimeParser.parseDateTime(newValue);
          event.setStartDateTime(newStart);
          break;
        case "end":
          LocalDateTime newEnd = DateTimeParser.parseDateTime(newValue);
          event.setEndDateTime(newEnd);
          break;
        case "description":
          event.setDescription(newValue);
          break;
        case "location":
          event.setLocation(newValue);
          break;
        case "status":
          event.setStatus(parseStatus(newValue));
          break;
        default:
          throw new CalendarException("Unknown property: " + property);
      }
    } catch (InvalidDateTimeException e) {
      throw new CalendarException("Invalid date/time format", e);
    }
  }

  private String cleanQuotes(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() > 1) {
      return trimmed.substring(1, trimmed.length() - 1);
    }
    return trimmed;
  }

  private EventStatus parseStatus(String statusStr) {
    if (statusStr == null) {
      return EventStatus.PUBLIC;
    }
    try {
      return EventStatus.valueOf(statusStr.toUpperCase());
    } catch (IllegalArgumentException e) {
      return EventStatus.PUBLIC;
    }
  }

  private String generateSeriesKey(String subject, LocalDateTime start) {
    return subject + "_" + start.toString();
  }

  /**
   * Immutable value object holding event properties.
   * Encapsulates description, location, and status as a cohesive unit.
   */
  private static class EventProperties {
    final String description;
    final String location;
    final EventStatus status;

    EventProperties(String description, String location, EventStatus status) {
      this.description = description;
      this.location = location;
      this.status = status;
    }
  }
}