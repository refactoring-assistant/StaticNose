package calendar.service;

import calendar.exception.CalendarException;
import calendar.exception.DuplicateEventException;
import calendar.exception.EventNotFoundException;
import calendar.exception.InvalidDateTimeException;
import calendar.model.EventBuilder;
import calendar.model.EventSeries;
import calendar.model.EventStatus;
import calendar.model.InCalendar;
import calendar.model.InEvent;
import calendar.model.RecurrencePattern;
import calendar.model.RecurringEvent;
import calendar.model.SingleEvent;
import calendar.model.Weekday;
import calendar.util.DateTimeParser;
import calendar.util.EventPredicates;
import calendar.util.ValidationUtil;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of event service handling business logic.
 * Manages event creation, editing, and querying operations.
 */
public class EventService implements InEventService {

  private final InCalendar calendar;
  private final Map<String, EventSeries> eventSeriesMap;

  /**
   * Constructs an EventService with a calendar.
   *
   * @param calendar the calendar to manage
   */
  public EventService(InCalendar calendar) {
    if (calendar == null) {
      throw new IllegalArgumentException("Calendar cannot be null");
    }
    this.calendar = calendar;
    this.eventSeriesMap = new HashMap<>();
  }

  @Override
  public void createSingleEvent(String subject, LocalDateTime start,
                                LocalDateTime end, Map<String, String> optionalProps)
      throws DuplicateEventException {
    if (subject == null) {
      throw new IllegalArgumentException("Subject cannot be null");
    }
    if (start == null) {
      throw new IllegalArgumentException("Start date/time cannot be null");
    }

    ValidationUtil.validateNotEmpty(subject, "Subject");

    SingleEvent event = EventBuilder.create()
        .setSubject(subject)
        .setStartDateTime(start)
        .setEndDateTime(end)
        .fromOptionalProps(optionalProps)
        .buildSingleEvent();

    calendar.addEvent(event);
  }

  /**
   * Validates that series parameters are reasonable.
   * Prevents creation of excessively large series.
   */
  private void validateSeriesParameters(Set<Weekday> weekdays, Integer occurrences,
                                        LocalDate startDate, LocalDate endDate)
      throws CalendarException {
    if (weekdays == null || weekdays.isEmpty()) {
      throw new CalendarException("Weekdays cannot be empty for recurring events");
    }

    if (occurrences != null) {
      if (occurrences > 1000) {
        throw new CalendarException(
            "Cannot create series with more than 1000 occurrences. " + "Requested: " + occurrences);
      }
    } else if (endDate != null && startDate != null) {
      long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
      if (daysBetween > 3650) {
        throw new CalendarException(
            "Cannot create series spanning more than 10 years. " + "Date range: " + daysBetween
                + " days");
      }
    }
  }

  @Override
  public void createEventSeries(String subject, LocalDateTime start, LocalDateTime end,
                                Set<Weekday> weekdays, int occurrences,
                                Map<String, String> optionalProps)
      throws CalendarException {
    if (start == null) {
      throw new IllegalArgumentException("Start date/time cannot be null");
    }
    if (weekdays == null) {
      throw new IllegalArgumentException("Weekdays cannot be null");
    }

    ValidationUtil.validateNotEmpty(subject, "Subject");

    validateSeriesParameters(weekdays, occurrences, start.toLocalDate(), null);

    if (end != null) {
      ValidationUtil.validateRecurringSameDay(start, end);
    }

    String description = optionalProps != null ? optionalProps.get("description") : null;
    String location = optionalProps != null ? optionalProps.get("location") : null;
    EventStatus status = parseStatus(optionalProps != null ? optionalProps.get("status") : null);

    RecurrencePattern pattern =
        new RecurrencePattern(weekdays, start.toLocalTime(), end != null ? end.toLocalTime() : null,
            occurrences);

    EventSeries series = new EventSeries(subject, pattern, description, location, status);
    series.generateInstances(start.toLocalDate(), description, location, status);

    for (RecurringEvent event : series.getInstances()) {
      calendar.addEvent(event);
    }

    String seriesKey = generateSeriesKey(subject, start);
    eventSeriesMap.put(seriesKey, series);
  }

  @Override
  public void createEventSeriesUntil(String subject, LocalDateTime start, LocalDateTime end,
                                     Set<Weekday> weekdays, LocalDate endDate,
                                     Map<String, String> optionalProps)
      throws CalendarException {
    if (start == null) {
      throw new IllegalArgumentException("Start date/time cannot be null");
    }
    if (endDate == null) {
      throw new IllegalArgumentException("End date cannot be null");
    }

    ValidationUtil.validateNotEmpty(subject, "Subject");

    validateSeriesParameters(weekdays, null, start.toLocalDate(), endDate);

    if (end != null) {
      ValidationUtil.validateRecurringSameDay(start, end);
    }

    String description = optionalProps != null ? optionalProps.get("description") : null;
    String location = optionalProps != null ? optionalProps.get("location") : null;
    EventStatus status = parseStatus(optionalProps != null ? optionalProps.get("status") : null);

    RecurrencePattern pattern =
        new RecurrencePattern(weekdays, start.toLocalTime(), end != null ? end.toLocalTime() : null,
            endDate);

    EventSeries series = new EventSeries(subject, pattern, description, location, status);
    series.generateInstances(start.toLocalDate(), description, location, status);

    for (RecurringEvent event : series.getInstances()) {
      calendar.addEvent(event);
    }

    String seriesKey = generateSeriesKey(subject, start);
    eventSeriesMap.put(seriesKey, series);
  }

  @Override
  public void editSingleEvent(String subject, LocalDateTime start, String property, String newValue)
      throws CalendarException {
    if (subject == null) {
      throw new IllegalArgumentException("Subject cannot be null");
    }
    if (start == null) {
      throw new IllegalArgumentException("Start date/time cannot be null");
    }
    if (property == null) {
      throw new IllegalArgumentException("Property cannot be null");
    }
    if (newValue == null) {
      throw new IllegalArgumentException("New value cannot be null");
    }

    List<InEvent> matchingEvents = findMatchingEvents(subject, start);

    if (matchingEvents.isEmpty()) {
      throw new EventNotFoundException("No event found with subject: " + subject + " at " + start);
    }

    if (matchingEvents.size() > 1) {
      throw new CalendarException(
          "Multiple events found with subject '" + subject + "' starting at " + start + ". "
              + "Cannot determine which to edit. Please be more specific.");
    }

    InEvent event = matchingEvents.get(0);
    applyPropertyChange(event, property, newValue);
  }

  @Override
  public void editSeriesFromDate(String subject, LocalDateTime start, String property,
                                 String newValue) throws CalendarException {

    List<InEvent> matchingEvents = findMatchingEvents(subject, start);

    if (matchingEvents.isEmpty()) {
      throw new EventNotFoundException("No event found with subject: " + subject + " at " + start);
    }

    InEvent event = matchingEvents.get(0);

    if (event instanceof RecurringEvent) {
      RecurringEvent recurringEvent = (RecurringEvent) event;
      EventSeries series = recurringEvent.getParentSeries();

      if (series != null) {
        LocalDate fromDate = start.toLocalDate();

        if ("start".equals(property)) {
          fromDate = start.toLocalDate();
          List<RecurringEvent> splitEvents = series.splitSeries(fromDate);

          LocalDateTime newStartTime = DateTimeParser.parseDateTime(newValue);

          RecurrencePattern oldPattern = series.getPattern();
          String description =
              splitEvents.isEmpty() ? null : splitEvents.get(0).getDescription().orElse(null);
          String location =
              splitEvents.isEmpty() ? null : splitEvents.get(0).getLocation().orElse(null);
          EventStatus status =
              splitEvents.isEmpty() ? EventStatus.PUBLIC : splitEvents.get(0).getStatus();

          RecurrencePattern newPattern;
          if (oldPattern.hasOccurrenceCount()) {
            newPattern = new RecurrencePattern(oldPattern.getWeekdays(), newStartTime.toLocalTime(),
                oldPattern.getEndTime(), splitEvents.size()
            );
          } else {
            newPattern = new RecurrencePattern(oldPattern.getWeekdays(), newStartTime.toLocalTime(),
                oldPattern.getEndTime(), oldPattern.getEndDate());
          }

          EventSeries newSeries =
              new EventSeries(subject, newPattern, description, location, status);

          for (RecurringEvent splitEvent : splitEvents) {
            try {
              calendar.removeEvent(splitEvent);
            } catch (EventNotFoundException e) {
              throw new EventNotFoundException(e.getMessage());
            }

            LocalDateTime oldStart = splitEvent.getStartDateTime();
            LocalDateTime newStart =
                LocalDateTime.of(oldStart.toLocalDate(), newStartTime.toLocalTime());
            splitEvent.setStartDateTime(newStart);

            if (!splitEvent.isAllDayEvent()) {
              LocalDateTime oldEnd = splitEvent.getEndDateTime();
              LocalDateTime newEnd = LocalDateTime.of(oldEnd.toLocalDate(),
                  newStartTime.toLocalTime()
                      .plusMinutes(java.time.Duration.between(oldStart, oldEnd).toMinutes()));
              splitEvent.setEndDateTime(newEnd);
            }

            newSeries.addInstance(splitEvent);
            calendar.addEvent(splitEvent);
          }

          String newSeriesKey = generateSeriesKey(subject, newStartTime);
          eventSeriesMap.put(newSeriesKey, newSeries);

          return;
        } else {
          series.updateSeriesFrom(fromDate, e -> {
            try {
              applyPropertyChange(e, property, newValue);
            } catch (CalendarException ex) {
              throw new RuntimeException(ex);
            }
          });
        }
        return;
      }
    }

    applyPropertyChange(event, property, newValue);
  }

  @Override
  public void editEntireSeries(String subject, LocalDateTime start, String property,
                               String newValue) throws CalendarException {

    List<InEvent> matchingEvents = findMatchingEvents(subject, start);

    if (matchingEvents.isEmpty()) {
      throw new EventNotFoundException("No event found with subject: " + subject + " at " + start);
    }

    InEvent event = matchingEvents.get(0);

    if (event instanceof RecurringEvent) {
      RecurringEvent recurringEvent = (RecurringEvent) event;
      EventSeries series = recurringEvent.getParentSeries();

      if (series != null) {
        series.updateEntireSeries(e -> {
          try {
            applyPropertyChange(e, property, newValue);
          } catch (CalendarException ex) {
            throw new RuntimeException(ex);
          }
        });
        return;
      }
    }

    applyPropertyChange(event, property, newValue);
  }

  @Override
  public List<InEvent> queryEventsOnDate(LocalDate date) {
    return calendar.getEventsOnDate(date);
  }

  @Override
  public List<InEvent> queryEventsBetween(LocalDateTime start, LocalDateTime end) {
    return calendar.getEventsBetween(start, end);
  }

  @Override
  public boolean checkBusyStatus(LocalDateTime dateTime) {
    if (dateTime == null) {
      throw new IllegalArgumentException("DateTime cannot be null");
    }
    return calendar.isBusyAt(dateTime);
  }

  @Override
  public List<InEvent> getAllEvents() {
    return calendar.getAllEvents();
  }

  private List<InEvent> findMatchingEvents(String subject, LocalDateTime start) {
    return calendar.filterEvents(
        EventPredicates.subjectEquals(subject)
            .and(EventPredicates.startsAt(start))
    );
  }

  private void applyPropertyChange(InEvent event, String property, String newValue)
      throws CalendarException {
    try {
      switch (property.toLowerCase()) {
        case "subject":
          event.setSubject(newValue);
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
}