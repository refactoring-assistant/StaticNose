package calendar.model;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.zone.ZoneRulesException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of CalendarSystem that manages multiple calendars.
 */
public class CalendarSystemImpl implements CalendarSystem {

  private static final String PROPERTY_NAME = "name";
  private static final String PROPERTY_TIMEZONE = "timezone";

  private final Map<String, Calendar> calendars;
  private Calendar currentCalendar;

  /**
   * Constructor for the calendar system.
   */
  public CalendarSystemImpl() {
    this.calendars = new HashMap<>();
    this.currentCalendar = null;
  }

  @Override
  public void createCalendar(String name, ZoneId timezone) {
    validateNotNullOrEmpty(name, "Calendar name");
    validateNotNull(timezone, "Timezone");

    String trimmedName = name.trim();
    if (calendars.containsKey(trimmedName)) {
      throw new IllegalArgumentException("Calendar '" + trimmedName + "' already exists");
    }
    Calendar newCalendar = new CalendarImpl(trimmedName, timezone);
    calendars.put(trimmedName, newCalendar);
  }

  @Override
  public Calendar getCalendar(String name) {
    if (name == null) {
      return null;
    }
    return calendars.get(name.trim());
  }

  @Override
  public Calendar getCurrentCalendar() {
    return currentCalendar;
  }

  @Override
  public void setCurrentCalendar(String name) {
    validateNotNullOrEmpty(name, "Calendar name");

    Calendar cal = calendars.get(name.trim());
    if (cal == null) {
      throw new IllegalArgumentException("Calendar '" + name + "' not found");
    }
    this.currentCalendar = cal;
  }

  @Override
  public void editCalendar(String calendarName, String property, String newValue) {
    validateNotNullOrEmpty(calendarName, "Calendar name");
    validateNotNullOrEmpty(property, "Property");
    validateNotNullOrEmpty(newValue, "New value");

    Calendar calendar = calendars.get(calendarName.trim());
    if (calendar == null) {
      throw new IllegalArgumentException("Calendar '" + calendarName + "' not found");
    }

    String prop = property.trim().toLowerCase();
    String value = newValue.trim();

    switch (prop) {
      case PROPERTY_NAME:
        renameCalendar(calendarName.trim(), calendar, value);
        break;
      case PROPERTY_TIMEZONE:
        changeTimezone(calendar, value);
        break;
      default:
        throw new IllegalArgumentException(
            "Invalid property: " + property + ". Must be 'name' or 'timezone'");
    }
  }

  @Override
  public void copyEvent(String eventName, LocalDateTime sourceStart,
                        String targetCalName, LocalDateTime targetStart) {
    requireCurrentCalendar();
    validateNotNullOrEmpty(eventName, "Event name");
    validateNotNull(sourceStart, "Source start time");
    validateNotNull(targetStart, "Target start time");
    validateNotNullOrEmpty(targetCalName, "Target calendar name");

    Calendar targetCalendar = getCalendarOrThrow(targetCalName);

    List<Event> sourceEvents = currentCalendar.findEvents(eventName.trim(), sourceStart);
    if (sourceEvents.isEmpty()) {
      throw new IllegalArgumentException("Event '" + eventName
          + "' not found at " + sourceStart);
    }

    for (Event sourceEvent : sourceEvents) {
      long durationMinutes = Duration.between(
          sourceEvent.getStartDateTime(), sourceEvent.getEndDateTime()).toMinutes();

      LocalDateTime newStart = targetStart;
      LocalDateTime newEnd = newStart.plusMinutes(durationMinutes);

      Event newEvent = createEventCopy(sourceEvent, newStart, newEnd);
      targetCalendar.addEvent(newEvent);
    }
  }

  @Override
  public void copyEventsOnDate(LocalDate sourceDate, String targetCalName,
                               LocalDate targetDate) {
    requireCurrentCalendar();
    validateNotNull(sourceDate, "Source date");
    validateNotNull(targetDate, "Target date");
    validateNotNullOrEmpty(targetCalName, "Target calendar name");

    Calendar targetCalendar = getCalendarOrThrow(targetCalName);
    List<Event> sourceEvents = currentCalendar.getEventsOnDate(sourceDate);

    TimezoneConverter converter = new TimezoneConverter(
        currentCalendar.getTimezone(), targetCalendar.getTimezone());

    for (Event sourceEvent : sourceEvents) {
      ConvertedEventTime convertedTime = converter.convertEvent(sourceEvent);

      LocalDate convertedDate = convertedTime.getStart().toLocalDate();
      long dayOffset = Duration.between(sourceDate.atStartOfDay(),
          convertedDate.atStartOfDay()).toDays();
      LocalDate adjustedTargetDate = targetDate.plusDays(dayOffset);

      LocalDateTime newStart = adjustedTargetDate.atStartOfDay()
          .plusMinutes(convertedTime.getMinutesFromMidnight());
      LocalDateTime newEnd = newStart.plusMinutes(convertedTime.getDurationMinutes());

      Event newEvent = createEventCopy(sourceEvent, newStart, newEnd);
      targetCalendar.addEvent(newEvent);
    }
  }

  @Override
  public void copyEventsBetween(LocalDate startDate, LocalDate endDate,
                                String targetCalName, LocalDate targetStartDate) {
    requireCurrentCalendar();
    validateNotNull(startDate, "Start date");
    validateNotNull(endDate, "End date");
    validateNotNull(targetStartDate, "Target start date");
    validateNotNullOrEmpty(targetCalName, "Target calendar name");

    if (endDate.isBefore(startDate)) {
      throw new IllegalArgumentException("End date cannot be before start date");
    }

    Calendar targetCalendar = getCalendarOrThrow(targetCalName);

    LocalDateTime rangeStart = startDate.atStartOfDay();
    LocalDateTime rangeEnd = endDate.atTime(23, 59, 59);
    List<Event> eventsInRange = currentCalendar.getEventsInRange(rangeStart, rangeEnd);

    EventCopyResult result = copyEventsToCalendar(
        eventsInRange, startDate, targetStartDate, targetCalendar);

    handleCopyResult(result);
  }

  @Override
  public List<String> getAllCalendarNames() {
    return new ArrayList<>(calendars.keySet());
  }

  private void requireCurrentCalendar() {
    if (currentCalendar == null) {
      throw new IllegalStateException("No source calendar in use");
    }
  }

  private Calendar getCalendarOrThrow(String calendarName) {
    Calendar calendar = calendars.get(calendarName.trim());
    if (calendar == null) {
      throw new IllegalArgumentException("Target calendar '"
          + calendarName + "' not found");
    }
    return calendar;
  }

  private void validateNotNull(Object value, String fieldName) {
    if (value == null) {
      throw new IllegalArgumentException(fieldName + " cannot be null");
    }
  }

  private void validateNotNullOrEmpty(String value, String fieldName) {
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalArgumentException(fieldName + " cannot be null or empty");
    }
  }

  private void renameCalendar(String oldName, Calendar calendar, String newName) {
    if (calendars.containsKey(newName)) {
      throw new IllegalArgumentException("Calendar '" + newName + "' already exists");
    }
    calendars.remove(oldName);
    calendar.setName(newName);
    calendars.put(newName, calendar);
  }

  private void changeTimezone(Calendar calendar, String timezoneValue) {
    try {
      ZoneId oldTimezone = calendar.getTimezone();
      ZoneId newTimezone = ZoneId.of(timezoneValue);
      List<Event> allEvents = calendar.getAllEvents();

      validateTimezoneChangeForSeries(allEvents, oldTimezone, newTimezone);
      updateAllEventsForTimezone(allEvents, oldTimezone, newTimezone);

      calendar.setTimezone(newTimezone);
    } catch (ZoneRulesException e) {
      throw new IllegalArgumentException("Invalid timezone: " + timezoneValue);
    }
  }

  private void validateTimezoneChangeForSeries(List<Event> allEvents,
                                               ZoneId oldTimezone, ZoneId newTimezone) {
    Map<String, List<Event>> seriesMap = groupEventsBySeries(allEvents);

    for (Map.Entry<String, List<Event>> entry : seriesMap.entrySet()) {
      List<Event> seriesEvents = entry.getValue();
      Event sampleEvent = seriesEvents.get(0);

      validateSeriesDoesNotSpanMultipleDays(sampleEvent, oldTimezone, newTimezone);
      validateSeriesWeekdaysRemainSame(seriesEvents, oldTimezone, newTimezone);
    }
  }

  private Map<String, List<Event>> groupEventsBySeries(List<Event> events) {
    Map<String, List<Event>> seriesMap = new HashMap<>();
    for (Event event : events) {
      if (event.getSeriesId() != null) {
        seriesMap.computeIfAbsent(event.getSeriesId(), k -> new ArrayList<>()).add(event);
      }
    }
    return seriesMap;
  }

  private void validateSeriesDoesNotSpanMultipleDays(Event event,
                                                     ZoneId oldTimezone, ZoneId newTimezone) {
    ZonedDateTime oldStart = ZonedDateTime.of(event.getStartDateTime(), oldTimezone);
    ZonedDateTime oldEnd = ZonedDateTime.of(event.getEndDateTime(), oldTimezone);
    ZonedDateTime newStart = oldStart.withZoneSameInstant(newTimezone);
    ZonedDateTime newEnd = oldEnd.withZoneSameInstant(newTimezone);

    if (!newStart.toLocalDate().equals(newEnd.toLocalDate())) {
      throw new IllegalArgumentException("Cannot change timezone: event series '"
          + event.getSubject() + "' would span multiple days after timezone conversion");
    }
  }

  private void validateSeriesWeekdaysRemainSame(List<Event> seriesEvents,
                                                ZoneId oldTimezone, ZoneId newTimezone) {
    Event sampleEvent = seriesEvents.get(0);

    List<DayOfWeek> originalWeekdays = seriesEvents.stream()
        .map(e -> e.getStartDateTime().getDayOfWeek())
        .distinct()
        .sorted()
        .collect(Collectors.toList());

    List<DayOfWeek> newWeekdays = new ArrayList<>();
    for (Event event : seriesEvents) {
      ZonedDateTime eventOldStart = ZonedDateTime.of(event.getStartDateTime(), oldTimezone);
      ZonedDateTime eventNewStart = eventOldStart.withZoneSameInstant(newTimezone);
      DayOfWeek newWeekday = eventNewStart.getDayOfWeek();
      if (!newWeekdays.contains(newWeekday)) {
        newWeekdays.add(newWeekday);
      }
    }
    newWeekdays.sort(DayOfWeek::compareTo);

    if (!originalWeekdays.equals(newWeekdays)) {
      throw new IllegalArgumentException("Cannot change timezone: event series '"
          + sampleEvent.getSubject() + "' would change from recurring on "
          + formatWeekdays(originalWeekdays) + " to " + formatWeekdays(newWeekdays));
    }
  }

  private void updateAllEventsForTimezone(List<Event> allEvents,
                                          ZoneId oldTimezone, ZoneId newTimezone) {
    for (Event event : allEvents) {
      ZonedDateTime oldStart = ZonedDateTime.of(event.getStartDateTime(), oldTimezone);
      ZonedDateTime oldEnd = ZonedDateTime.of(event.getEndDateTime(), oldTimezone);
      ZonedDateTime newStart = oldStart.withZoneSameInstant(newTimezone);
      ZonedDateTime newEnd = oldEnd.withZoneSameInstant(newTimezone);
      LocalDateTime newStartLocal = newStart.toLocalDateTime();
      LocalDateTime newEndLocal = newEnd.toLocalDateTime();

      if (newStartLocal.isBefore(event.getStartDateTime())) {
        event.setStartDateTime(newStartLocal);
        event.setEndDateTime(newEndLocal);
      } else {
        event.setEndDateTime(newEndLocal);
        event.setStartDateTime(newStartLocal);
      }
    }
  }

  private String formatWeekdays(List<DayOfWeek> weekdays) {
    return weekdays.stream()
        .map(DayOfWeek::toString)
        .collect(Collectors.joining(", "));
  }

  private EventCopyResult copyEventsToCalendar(List<Event> events, LocalDate startDate,
                                               LocalDate targetStartDate, Calendar targetCalendar) {
    EventCopyResult result = new EventCopyResult();

    Map<String, List<Event>> seriesEvents = new HashMap<>();
    List<Event> nonSeriesEvents = new ArrayList<>();

    for (Event event : events) {
      if (event.getSeriesId() != null) {
        seriesEvents.computeIfAbsent(event.getSeriesId(), k -> new ArrayList<>()).add(event);
      } else {
        nonSeriesEvents.add(event);
      }
    }

    TimezoneConverter converter = new TimezoneConverter(
        currentCalendar.getTimezone(), targetCalendar.getTimezone());

    copySeriesEvents(seriesEvents, startDate, targetStartDate, converter, targetCalendar, result);
    copyNonSeriesEvents(nonSeriesEvents, startDate, targetStartDate,
        converter, targetCalendar, result);

    return result;
  }

  private void copySeriesEvents(Map<String, List<Event>> seriesEvents, LocalDate startDate,
                                LocalDate targetStartDate, TimezoneConverter converter,
                                Calendar targetCalendar, EventCopyResult result) {
    for (Map.Entry<String, List<Event>> entry : seriesEvents.entrySet()) {
      String seriesId = entry.getKey();
      List<Event> seriesList = entry.getValue();
      Event sample = seriesList.get(0);

      try {
        ConvertedEventTime convertedTime = converter.convertEvent(sample);

        if (!convertedTime.spansOnlyOneDay()) {
          result.addError("Failed to copy event series '" + sample.getSubject()
              + "': timezone conversion causes events to span multiple days");
          continue;
        }

        List<Event> fullSeries = currentCalendar.getEventsBySeries(seriesId);
        List<DayOfWeek> fullSeriesWeekdays = fullSeries.stream()
            .map(e -> e.getStartDateTime().getDayOfWeek())
            .distinct()
            .sorted()
            .collect(Collectors.toList());

        String newSeriesId = UUID.randomUUID().toString();
        copySeriesInstances(seriesList, fullSeriesWeekdays, targetStartDate,
            convertedTime, newSeriesId, targetCalendar);

        result.addSuccess("Successfully copied event series '" + sample.getSubject() + "'");
      } catch (Exception e) {
        result.addError("Failed to copy event series '" + sample.getSubject() + "': "
            + e.getMessage());
      }
    }
  }

  private void copySeriesInstances(List<Event> seriesList, List<DayOfWeek> fullSeriesWeekdays,
                                   LocalDate targetStartDate, ConvertedEventTime convertedTime,
                                   String newSeriesId, Calendar targetCalendar) {
    LocalDate currentTargetDate = targetStartDate;

    for (Event sourceEvent : seriesList) {
      DayOfWeek targetWeekday = findNextWeekdayInPattern(currentTargetDate, fullSeriesWeekdays);

      while (currentTargetDate.getDayOfWeek() != targetWeekday) {
        currentTargetDate = currentTargetDate.plusDays(1);
      }

      LocalDateTime newStart = currentTargetDate.atStartOfDay()
          .plusMinutes(convertedTime.getMinutesFromMidnight());
      LocalDateTime newEnd = newStart.plusMinutes(convertedTime.getDurationMinutes());

      Event newEvent = createEventCopy(sourceEvent, newStart, newEnd);
      newEvent.setSeriesId(newSeriesId);
      targetCalendar.addEvent(newEvent);

      currentTargetDate = currentTargetDate.plusDays(1);
    }
  }

  private DayOfWeek findNextWeekdayInPattern(LocalDate startDate, List<DayOfWeek> pattern) {
    DayOfWeek startWeekday = startDate.getDayOfWeek();

    for (DayOfWeek day : pattern) {
      if (day.getValue() >= startWeekday.getValue()) {
        return day;
      }
    }

    return pattern.get(0);
  }

  private void copyNonSeriesEvents(List<Event> nonSeriesEvents, LocalDate startDate,
                                   LocalDate targetStartDate, TimezoneConverter converter,
                                   Calendar targetCalendar, EventCopyResult result) {
    for (Event sourceEvent : nonSeriesEvents) {
      try {
        ConvertedEventTime convertedTime = converter.convertEvent(sourceEvent);

        LocalDate sourceEventDate = sourceEvent.getStartDateTime().toLocalDate();
        long dayOffsetFromConversion = Duration.between(sourceEventDate.atStartOfDay(),
            convertedTime.getStart().toLocalDate().atStartOfDay()).toDays();
        long dayOffsetInRange = Duration.between(startDate.atStartOfDay(),
            sourceEventDate.atStartOfDay()).toDays();

        LocalDate newDate = targetStartDate.plusDays(dayOffsetInRange
            + dayOffsetFromConversion);
        LocalDateTime newStart = newDate.atStartOfDay()
            .plusMinutes(convertedTime.getMinutesFromMidnight());
        LocalDateTime newEnd = newStart.plusMinutes(convertedTime.getDurationMinutes());

        Event newEvent = createEventCopy(sourceEvent, newStart, newEnd);
        targetCalendar.addEvent(newEvent);
        result.addSuccess("Successfully copied event '" + sourceEvent.getSubject() + "'");
      } catch (Exception e) {
        result.addError("Failed to copy event '" + sourceEvent.getSubject() + "': "
            + e.getMessage());
      }
    }
  }

  private void handleCopyResult(EventCopyResult result) {
    if (result.isEmpty()) {
      throw new IllegalArgumentException("No events found in the specified range");
    }
    if (result.hasOnlyErrors()) {
      throw new IllegalArgumentException(result.getErrorMessage());
    }
    if (result.hasPartialSuccess()) {
      throw new IllegalStateException(result.getCombinedMessage());
    }
  }

  private Event createEventCopy(Event sourceEvent, LocalDateTime newStart, LocalDateTime newEnd) {
    Event newEvent = new EventImpl(sourceEvent.getSubject(), newStart, newEnd);
    if (sourceEvent.getDescription() != null) {
      newEvent.setDescription(sourceEvent.getDescription());
    }
    if (sourceEvent.getLocation() != null) {
      newEvent.setLocation(sourceEvent.getLocation());
    }
    newEvent.setStatus(sourceEvent.getStatus());
    return newEvent;
  }

  /**
   * Helper class for timezone conversion operations.
   */
  private static class TimezoneConverter {
    private final ZoneId sourceZone;
    private final ZoneId targetZone;

    TimezoneConverter(ZoneId sourceZone, ZoneId targetZone) {
      this.sourceZone = sourceZone;
      this.targetZone = targetZone;
    }

    ConvertedEventTime convertEvent(Event event) {
      ZonedDateTime sourceStart = ZonedDateTime.of(event.getStartDateTime(), sourceZone);
      ZonedDateTime sourceEnd = ZonedDateTime.of(event.getEndDateTime(), sourceZone);
      ZonedDateTime convertedStart = sourceStart.withZoneSameInstant(targetZone);
      ZonedDateTime convertedEnd = sourceEnd.withZoneSameInstant(targetZone);

      long minutesFromMidnight = Duration.between(
          convertedStart.toLocalDate().atStartOfDay(),
          convertedStart.toLocalDateTime()).toMinutes();
      long durationMinutes = Duration.between(convertedStart, convertedEnd).toMinutes();

      return new ConvertedEventTime(convertedStart, convertedEnd,
          minutesFromMidnight, durationMinutes);
    }
  }

  /**
   * Represents a converted event's time information.
   */
  private static class ConvertedEventTime {
    private final ZonedDateTime start;
    private final ZonedDateTime end;
    private final long minutesFromMidnight;
    private final long durationMinutes;

    ConvertedEventTime(ZonedDateTime start, ZonedDateTime end,
                       long minutesFromMidnight, long durationMinutes) {
      this.start = start;
      this.end = end;
      this.minutesFromMidnight = minutesFromMidnight;
      this.durationMinutes = durationMinutes;
    }

    ZonedDateTime getStart() {
      return start;
    }

    ZonedDateTime getEnd() {
      return end;
    }

    long getMinutesFromMidnight() {
      return minutesFromMidnight;
    }

    long getDurationMinutes() {
      return durationMinutes;
    }

    boolean spansOnlyOneDay() {
      return start.toLocalDate().equals(end.toLocalDate());
    }
  }

  /**
   * Represents the result of a copy operation with successes and errors.
   */
  private static class EventCopyResult {
    private final List<String> successMessages;
    private final List<String> errorMessages;

    EventCopyResult() {
      this.successMessages = new ArrayList<>();
      this.errorMessages = new ArrayList<>();
    }

    void addSuccess(String message) {
      successMessages.add(message);
    }

    void addError(String message) {
      errorMessages.add(message);
    }

    boolean isEmpty() {
      return successMessages.isEmpty() && errorMessages.isEmpty();
    }

    boolean hasOnlyErrors() {
      return successMessages.isEmpty() && !errorMessages.isEmpty();
    }

    boolean hasPartialSuccess() {
      return !successMessages.isEmpty() && !errorMessages.isEmpty();
    }

    String getErrorMessage() {
      return String.join("\n", errorMessages);
    }

    String getCombinedMessage() {
      return String.join("\n", successMessages) + "\n"
          + String.join("\n", errorMessages);
    }
  }
}