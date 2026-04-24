package calendar.model.calendar;

import calendar.exceptions.DuplicateEventException;
import calendar.exceptions.EditConflictException;
import calendar.exceptions.EventNotFoundException;
import calendar.exceptions.InvalidDateTimeException;
import calendar.exceptions.InvalidPropertyException;
import calendar.exceptions.MultipleEventsFoundException;
import calendar.model.event.Event;
import calendar.model.event.EventBuilder;
import calendar.model.event.EventInterface;
import calendar.model.event.EventStatus;
import calendar.model.util.DateTimeParser;
import calendar.model.util.EventUtils;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Calendar implementation managing events and event series.
 *
 * <p>REPRESENTATION CHOICE:
 * - eventsByDate: Map(LocalDate, List(Event)) - Events indexed by date for O(1) lookup.
 * Multi-day events appear under every date they span (trades memory for query speed).
 * - eventsBySeriesId: Map(String, List(Event))- Events grouped by series UUID for
 * efficient series-wide editing operations.
 *
 * <p>WHY THIS DESIGN:
 * Most common operation is "show events on date X" - this makes it instant lookup.
 * Series editing requires finding all related events quickly - the series map provides this.
 *
 * <p>CLASS INVARIANTS:
 * - No duplicate events (same subject, start, end)
 * - All events have start < end
 * - Events in series are single-day only
 * - eventsByDate and eventsBySeriesId stay synchronized
 * - Multi-day events stored under every date they span
 *
 * <p>All modifications use two-phase pattern: validate first, then modify.
 * Timezone: All events in America/New_York (Eastern Time).
 */
public class Calendar implements CalendarInterface {

  private Map<LocalDate, List<EventInterface>> eventsByDate;
  private Map<String, List<EventInterface>> eventsBySeriesId;
  private String name;
  private ZoneId timezone;

  /**
   * Creates a new empty calendar with the specified name and timezone.
   *
   * @param name the name of the calendar
   * @param timezone the timezone for this calendar
   */
  public Calendar(String name, ZoneId timezone) {
    validateName(name);
    validateTimezone(timezone);

    this.name = name;
    this.timezone = timezone;
    this.eventsByDate = new HashMap<>();
    this.eventsBySeriesId = new HashMap<>();
  }

  @Override
  public String getCalendarName() {
    return name;
  }

  @Override
  public void setName(String name) {
    validateName(name);
    this.name = name;
  }

  @Override
  public ZoneId getCalendarTimeZone() {
    return timezone;
  }

  @Override
  public void setTimezone(ZoneId newTimezone) throws InvalidDateTimeException {
    validateTimezone(newTimezone);

    if (this.timezone.equals(newTimezone)) {
      return;
    }

    ZoneId oldTimezone = this.timezone;

    try {
      // Create temporary calendar with all events converted to new timezone
      // If any validation fails, temporary calendar is discarded
      Calendar tempCalendar = createConvertedCalendar(newTimezone);

      // All validations passed - atomically commit changes
      this.eventsByDate = tempCalendar.eventsByDate;
      this.eventsBySeriesId = tempCalendar.eventsBySeriesId;
      this.timezone = newTimezone;

    } catch (DuplicateEventException | IllegalArgumentException e) {
      throw new IllegalStateException(
          "Timezone change from " + oldTimezone + " to " + newTimezone
              + " failed: " + e.getMessage(),
          e
      );
    }
  }

  // ==================== TIMEZONE CONVERSION HELPERS ====================

  /**
   * Creates a new calendar with all events converted to the target timezone.
   * Uses a two-phase approach: group events by series, then convert each group.
   *
   * <p>ATOMICITY: If any event fails validation (e.g., series event becomes multi-day),
   * the entire operation fails and the temporary calendar is discarded. This ensures
   * the original calendar remains unchanged if conversion is not possible.
   *
   * <p>DESIGN: Events are grouped by their original seriesId. Each series is converted
   * as a unit and assigned a new UUID. Standalone events are converted individually.
   * The new seriesId values don't affect functionality - what matters is that events
   * that were grouped together remain grouped together.
   *
   * @param newTimezone the target timezone for conversion
   * @return a new Calendar with all events converted and validated
   * @throws InvalidDateTimeException if datetime operations fail
   * @throws DuplicateEventException if conversion creates duplicates
   * @throws IllegalArgumentException if any series event becomes multi-day
   */
  private Calendar createConvertedCalendar(ZoneId newTimezone)
      throws InvalidDateTimeException, DuplicateEventException {

    Calendar tempCalendar = new Calendar(this.name, newTimezone);

    List<EventInterface> allEvents = getAllEvents();

    Map<String, List<EventInterface>> eventsBySeries = groupEventsBySeries(allEvents);

    List<EventInterface> standaloneEvents = eventsBySeries.get(null);
    if (standaloneEvents != null) {
      for (EventInterface event : standaloneEvents) {
        convertAndAddStandaloneEvent(tempCalendar, event, newTimezone);
      }
    }

    for (Map.Entry<String, List<EventInterface>> entry : eventsBySeries.entrySet()) {
      String seriesId = entry.getKey();

      if (seriesId != null) {
        List<EventInterface> seriesEvents = entry.getValue();
        convertAndAddSeries(tempCalendar, seriesEvents, newTimezone);
      }
    }

    return tempCalendar;
  }

  /**
   * Groups events by their series ID.
   * Events without a series (seriesId = null) are grouped under the null key.
   *
   * <p>This enables processing standalone events separately from series events,
   * since they have different conversion rules (standalone events can be multi-day,
   * series events must remain single-day).
   *
   * @param events list of all events to group
   * @return map where key is seriesId (or null for standalone) and value is list of events
   */
  private Map<String, List<EventInterface>> groupEventsBySeries(List<EventInterface> events) {
    Map<String, List<EventInterface>> grouped = new HashMap<>();

    for (EventInterface event : events) {
      String seriesId = event.getSeriesId();
      grouped.computeIfAbsent(seriesId, k -> new ArrayList<>()).add(event);
    }

    return grouped;
  }

  /**
   * Converts a standalone event to new timezone and adds to temporary calendar.
   * Uses EventBuilder to leverage its validation logic.
   *
   * <p>Standalone events have no series constraints, so they can span multiple days
   * after timezone conversion without issue. The event is recreated with converted
   * times while preserving all properties (description, location, status).
   *
   * @param tempCalendar the temporary calendar to add the converted event to
   * @param originalEvent the standalone event to convert
   * @param newTimezone the target timezone
   * @throws InvalidDateTimeException if datetime operations fail
   * @throws DuplicateEventException if converted event already exists in temp calendar
   */
  private void convertAndAddStandaloneEvent(Calendar tempCalendar,
                                            EventInterface originalEvent,
                                            ZoneId newTimezone)
      throws InvalidDateTimeException, DuplicateEventException {

    ZonedDateTime convertedStart = originalEvent.getStartDateTime()
        .withZoneSameInstant(newTimezone);
    ZonedDateTime convertedEnd = originalEvent.getEndDateTime()
        .withZoneSameInstant(newTimezone);

    tempCalendar.newEvent(
            originalEvent.getSubject(),
            DateTimeParser.formatDateTime(convertedStart))
        .end(DateTimeParser.formatDateTime(convertedEnd))
        .description(originalEvent.getDescription())
        .location(originalEvent.getLocation())
        .status(originalEvent.getStatus())
        .create(tempCalendar);
  }

  /**
   * Converts a series of events to new timezone and adds to temporary calendar.
   * All events in the series are validated for single-day constraint before storage.
   *
   * <p>VALIDATION: Each event is checked to ensure it remains single-day after
   * timezone conversion. If any event would become multi-day, IllegalArgumentException
   * is thrown and the entire timezone conversion is rolled back.
   *
   * <p>SERIES ID: A new UUID is generated for the converted series. The specific
   * UUID value doesn't matter - what matters is that all events that were together
   * in the original series stay together in the converted series with a common seriesId.
   *
   * <p>PROPERTIES: Each event's individual properties (subject, description, location,
   * status) are preserved during conversion. Series events can have different properties
   * if they were edited individually (non-temporal edits preserve series membership).
   *
   * @param tempCalendar the temporary calendar to add converted events to
   * @param seriesEvents list of events in the series (will be sorted by start time)
   * @param newTimezone the target timezone
   * @throws InvalidDateTimeException if datetime operations fail
   * @throws DuplicateEventException if any converted event already exists
   * @throws IllegalArgumentException if any event becomes multi-day after conversion
   */
  private void convertAndAddSeries(Calendar tempCalendar,
                                   List<EventInterface> seriesEvents,
                                   ZoneId newTimezone)
      throws InvalidDateTimeException, DuplicateEventException {

    seriesEvents.sort((e1, e2) -> e1.getStartDateTime().compareTo(e2.getStartDateTime()));

    String newSeriesId = UUID.randomUUID().toString();

    List<EventInterface> convertedEvents = convertSeriesEvents(
        seriesEvents,
        newSeriesId,
        newTimezone
    );

    tempCalendar.storeEvents(convertedEvents);
  }

  /**
   * Converts all events in a series to the new timezone.
   * Validates each event remains single-day and collects all converted events.
   * Each event's individual properties are preserved.
   *
   * @param seriesEvents the original events in the series
   * @param newSeriesId the new series ID to assign
   * @param newTimezone the target timezone
   * @return list of converted events, ready to be stored
   * @throws IllegalArgumentException if any event becomes multi-day
   */
  private List<EventInterface> convertSeriesEvents(List<EventInterface> seriesEvents,
                                                   String newSeriesId,
                                                   ZoneId newTimezone) {
    List<EventInterface> convertedEvents = new ArrayList<>();

    for (EventInterface originalEvent : seriesEvents) {
      EventInterface convertedEvent = convertSingleSeriesEvent(
          originalEvent,
          newSeriesId,
          newTimezone
      );
      convertedEvents.add(convertedEvent);
    }

    return convertedEvents;
  }

  /**
   * Converts a single event in a series to the new timezone.
   * Validates single-day constraint and preserves all individual properties.
   *
   * <p>Each event's properties (subject, description, location, status) are
   * preserved individually because series events can have different properties
   * if they were edited via single-event edits (which preserve series membership
   * for non-temporal property changes).
   *
   * @param originalEvent the original event to convert
   * @param newSeriesId the new series ID to assign
   * @param newTimezone the target timezone
   * @return the converted event with all properties preserved
   * @throws IllegalArgumentException if event becomes multi-day after conversion
   */
  private EventInterface convertSingleSeriesEvent(EventInterface originalEvent,
                                                  String newSeriesId,
                                                  ZoneId newTimezone) {
    ZonedDateTime convertedStart = originalEvent.getStartDateTime()
        .withZoneSameInstant(newTimezone);
    ZonedDateTime convertedEnd = originalEvent.getEndDateTime()
        .withZoneSameInstant(newTimezone);

    EventUtils.validateSeriesEventSingleDay(
        originalEvent.getSubject(),
        convertedStart,
        convertedEnd,
        "timezone conversion from " + this.timezone + " to " + newTimezone
    );

    EventInterface convertedEvent = new Event(
        originalEvent.getSubject(),
        convertedStart,
        convertedEnd
    );

    convertedEvent.setSeriesId(newSeriesId);

    if (originalEvent.getDescription() != null) {
      convertedEvent.setDescription(originalEvent.getDescription());
    }
    if (originalEvent.getLocation() != null) {
      convertedEvent.setLocation(originalEvent.getLocation());
    }
    convertedEvent.setStatus(originalEvent.getStatus());

    return convertedEvent;
  }

  @Override
  public EventBuilder newEvent(String subject, String startDateTime) {
    return EventBuilder.newEvent(subject, startDateTime, this.timezone);
  }

  /**
   * Stores built events in the calendar.
   * Called by EventBuilder.create() after building events.
   *
   * <p>Note: Accepts EventInterface for flexibility, but internally casts to Event
   * since this Calendar implementation requires access to internal methods
   * like setSeriesId(). This is safe because EventBuilder only creates Event objects.
   *
   * @param events list of events to store
   * @throws DuplicateEventException if any event already exists (same subject, start, end)
   */
  @Override
  public void storeEvents(List<? extends EventInterface> events) throws DuplicateEventException {
    for (EventInterface event : events) {
      checkForDuplicates(event);
    }

    for (EventInterface event : events) {
      addEventToMaps(event);
    }
  }

  /**
   * Retrieves all events scheduled on a specific date.
   *
   * @param dates the date to query in format YYYY-MM-DD
   * @return list of events on that date, empty list if no events
   * @throws IllegalArgumentException if date format is invalid
   */
  @Override
  public List<EventInterface> getEvents(String dates) throws InvalidDateTimeException {
    LocalDate date = parseDate(dates);
    List<EventInterface> events = eventsByDate.get(date);
    if (events == null) {
      return new ArrayList<>();
    }
    List<EventInterface> copiedEvents = new ArrayList<>();
    for (EventInterface event : events) {
      copiedEvents.add(copyEvent(event));
    }
    return copiedEvents;
  }

  /**
   * Retrieves all events that occur within a date range (inclusive).
   * Events that partially or fully overlap the range are included.
   *
   * @param startDateTimes the start date in format YYYY-MM-DDThh:mm (inclusive)
   * @param endDateTimes   the end date in format YYYY-MM-DDThh:mm (inclusive)
   * @return list of events in the range with duplicates removed, empty list if no events
   * @throws IllegalArgumentException if date format is invalid
   */
  @Override
  public List<EventInterface> getEvents(String startDateTimes, String endDateTimes)
      throws InvalidDateTimeException {
    ZonedDateTime startDateTime = parseDateTime(startDateTimes);
    ZonedDateTime endDateTime = parseDateTime(endDateTimes);

    LocalDate startDate = startDateTime.toLocalDate();
    LocalDate endDate = endDateTime.toLocalDate();

    Set<EventInterface> uniqueEvents = new LinkedHashSet<>();

    LocalDate current = startDate;
    while (!current.isAfter(endDate)) {
      List<EventInterface> dailyEvents = eventsByDate.get(current);
      if (dailyEvents != null) {
        uniqueEvents.addAll(dailyEvents);
      }

      current = current.plusDays(1);
    }
    List<EventInterface> copiedEvents = new ArrayList<>();
    for (EventInterface event : uniqueEvents) {
      copiedEvents.add(copyEvent(event));
    }
    return copiedEvents;
  }

  /**
   * Retrieves all events in the calendar.
   * Returns events sorted by start datetime in ascending order.
   * Returns defensive copies to prevent external modification of internal state.
   *
   * @return list of all events sorted by start time, empty list if no events
   * @throws InvalidDateTimeException if internal date operations fail
   */
  @Override
  public List<EventInterface> getAllEvents() throws InvalidDateTimeException {
    Set<EventInterface> allEvents = new LinkedHashSet<>();

    for (List<EventInterface> dailyEvents : eventsByDate.values()) {
      allEvents.addAll(dailyEvents);
    }

    List<EventInterface> sortedEvents = new ArrayList<>(allEvents);
    sortedEvents.sort((e1, e2) -> e1.getStartDateTime().compareTo(e2.getStartDateTime()));

    List<EventInterface> copiedEvents = new ArrayList<>();
    for (EventInterface event : sortedEvents) {
      copiedEvents.add(copyEvent(event));
    }
    return copiedEvents;
  }

  /**
   * Checks if the user is busy at a specific date and time.
   * Returns true if any event is scheduled at that exact time.
   *
   * @param dateTimes the datetime to check in format YYYY-MM-DDThh:mm
   * @return true if busy (event scheduled at that time), false if available
   * @throws InvalidDateTimeException if datetime format is invalid
   */
  @Override
  public boolean busyStatus(String dateTimes) throws InvalidDateTimeException {
    ZonedDateTime dateTime = parseDateTime(dateTimes);
    LocalDate date = dateTime.toLocalDate();
    List<EventInterface> events = eventsByDate.get(date);

    if (events == null) {
      return false;
    }

    for (EventInterface event : events) {
      if (!dateTime.isBefore(event.getStartDateTime())
          && dateTime.isBefore(event.getEndDateTime())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Edits a single event instance.
   * If event is part of series and temporal property (start/end) changed,
   * it becomes standalone (removed from series).
   *
   * @param property      the property to edit: subject, start, end, description, location, status
   * @param subject       the current subject of the event to find
   * @param startDateTime the current start datetime in format YYYY-MM-DDThh:mm
   * @param endDateTime   the current end datetime in format YYYY-MM-DDThh:mm
   * @param newValue      the new value for the property
   * @throws EventNotFoundException       if no event matches the criteria
   * @throws MultipleEventsFoundException if multiple events match (ambiguous)
   * @throws EditConflictException        if edit would create duplicate or violate constraints
   * @throws InvalidDateTimeException     if datetime format is invalid
   */
  @Override
  public void editEvent(String property, String subject,
                        String startDateTime, String endDateTime,
                        String newValue)
      throws EventNotFoundException, MultipleEventsFoundException, EditConflictException,
      InvalidDateTimeException, InvalidPropertyException {

    performEdit(property, subject, startDateTime, endDateTime, newValue, EditScope.SINGLE);
  }

  /**
   * Edits events in a series from a specific datetime forward.
   * For temporal properties (start/end), splits the series into two separate series.
   * For non-temporal properties, updates all events from datetime forward in same series.
   *
   * @param property      the property to edit: subject, start, end, description, location, status
   * @param subject       the current subject of the event to find
   * @param startDateTime the start datetime of occurrence to begin editing from (YYYY-MM-DDThh:mm)
   * @param newValue      the new value for the property
   * @throws EventNotFoundException       if no event matches the criteria
   * @throws MultipleEventsFoundException if multiple events match (ambiguous)
   * @throws EditConflictException        if edit would create duplicate or violate constraints
   * @throws InvalidDateTimeException     if datetime format is invalid
   */
  @Override
  public void editEventsFrom(String property, String subject,
                             String startDateTime, String newValue)
      throws EventNotFoundException, MultipleEventsFoundException, EditConflictException,
      InvalidDateTimeException, InvalidPropertyException {

    performEdit(property, subject, startDateTime, null, newValue, EditScope.FROM_POINT);

  }

  /**
   * Edits all events in a series.
   * Updates all occurrences with the new value, maintaining series relationship.
   *
   * @param property      the property to edit: subject, start, end, description, location, status
   * @param subject       the current subject of any event in the series
   * @param startDateTime the start datetime of any event in the series (YYYY-MM-DDThh:mm)
   * @param newValue      the new value for the property
   * @throws EventNotFoundException       if no event matches the criteria
   * @throws MultipleEventsFoundException if multiple events match (ambiguous)
   * @throws EditConflictException        if edit would create duplicate or violate constraints
   * @throws InvalidDateTimeException     if datetime format is invalid
   */
  @Override
  public void editSeries(String property, String subject,
                         String startDateTime, String newValue)
      throws EventNotFoundException, MultipleEventsFoundException, EditConflictException,
      InvalidDateTimeException, InvalidPropertyException {

    performEdit(property, subject, startDateTime, null, newValue, EditScope.ENTIRE_SERIES);

  }

  // ==================== EDIT HELPERS: SINGLE EVENT ====================


  /**
   * Edits the subject of a single event.
   */
  private void editEventSubject(EventInterface event, String newSubject)
      throws EditConflictException {
    validateNoEditDuplicate(event, newSubject,
        event.getStartDateTime(),
        event.getEndDateTime());

    event.setSubjectInternal(newSubject);
  }

  private void editEventStart(EventInterface event, String newStartStr)
      throws EditConflictException, InvalidDateTimeException {

    ZonedDateTime newStart = parseDateTime(newStartStr);
    ZonedDateTime currentEnd = event.getEndDateTime();

    validateTemporalConstraints(newStart, currentEnd, false);
    validateNoEditDuplicate(event, event.getSubject(), newStart, currentEnd);

    removeEventFromDateMaps(event);

    boolean isInSeries = event.getSeriesId() != null;

    if (isInSeries) {
      removeEventFromSeries(event);
    }

    event.setStartDateTimeInternal(newStart);

    addEventToMaps(event);
  }

  private void editEventEnd(EventInterface event, String newEndStr)
      throws EditConflictException, InvalidDateTimeException {

    ZonedDateTime newEnd = parseDateTime(newEndStr);
    ZonedDateTime currentStart = event.getStartDateTime();

    validateTemporalConstraints(currentStart, newEnd, false);
    validateNoEditDuplicate(event, event.getSubject(), currentStart, newEnd);

    removeEventFromDateMaps(event);

    boolean isInSeries = event.getSeriesId() != null;

    if (isInSeries) {
      removeEventFromSeries(event);
    }

    event.setEndDateTimeInternal(newEnd);

    addEventToMaps(event);
  }

  // ==================== EDIT HELPERS: EVENTS FROM ====================

  /**
   * Edits subject for multiple events from a point forward.
   * Events stay in the same series.
   */
  private void editEventsFromSubject(List<EventInterface> eventsToEdit, String newSubject)
      throws EditConflictException {

    for (EventInterface event : eventsToEdit) {
      validateNoEditDuplicate(event, newSubject,
          event.getStartDateTime(),
          event.getEndDateTime());
    }

    for (EventInterface event : eventsToEdit) {
      event.setSubjectInternal(newSubject);
    }
  }

  /**
   * Edits start time for multiple events from a point forward.
   * Splits the series - modified events get a new seriesId.
   * Only the TIME portion is applied to each event (preserving each event's original date).
   */
  private void editEventsFromStart(List<EventInterface> eventsToEdit, String originalSeriesId,
                                   ZonedDateTime fromDateTime, String newStartStr)
      throws EditConflictException, InvalidDateTimeException {

    ZonedDateTime newStartReference = parseDateTime(newStartStr);
    LocalTime newStartTime = newStartReference.toLocalTime();

    EventInterface firstEvent = eventsToEdit.get(0);
    validateTimeOnlyChange(firstEvent.getStartDateTime(), newStartReference, "start");

    for (EventInterface event : eventsToEdit) {
      ZonedDateTime newStartForThisEvent = event.getStartDateTime()
          .withHour(newStartTime.getHour())
          .withMinute(newStartTime.getMinute())
          .withSecond(0)
          .withNano(0);

      validateTemporalConstraints(newStartForThisEvent, event.getEndDateTime(), true);
      validateNoEditDuplicate(event, event.getSubject(),
          newStartForThisEvent, event.getEndDateTime());
    }

    splitSeries(originalSeriesId, fromDateTime);

    for (EventInterface event : eventsToEdit) {
      ZonedDateTime newStartForThisEvent = event.getStartDateTime()
          .withHour(newStartTime.getHour())
          .withMinute(newStartTime.getMinute())
          .withSecond(0)
          .withNano(0);

      event.setStartDateTimeInternal(newStartForThisEvent);
    }
  }

  /**
   * Edits end time for multiple events from a point forward.
   * Splits the series - modified events get a new seriesId.
   * Only the TIME portion is applied to each event (preserving each event's original date).
   */
  private void editEventsFromEnd(List<EventInterface> eventsToEdit, String originalSeriesId,
                                 ZonedDateTime fromDateTime, String newEndStr)
      throws EditConflictException, InvalidDateTimeException {

    ZonedDateTime newEndReference = parseDateTime(newEndStr);
    LocalTime newEndTime = newEndReference.toLocalTime();

    EventInterface firstEvent = eventsToEdit.get(0);
    validateTimeOnlyChange(firstEvent.getEndDateTime(), newEndReference, "end");

    for (EventInterface event : eventsToEdit) {
      ZonedDateTime newEndForThisEvent = event.getEndDateTime()
          .withHour(newEndTime.getHour())
          .withMinute(newEndTime.getMinute())
          .withSecond(0)
          .withNano(0);

      validateTemporalConstraints(event.getStartDateTime(), newEndForThisEvent, true);
      validateNoEditDuplicate(event, event.getSubject(),
          event.getStartDateTime(), newEndForThisEvent);
    }

    splitSeries(originalSeriesId, fromDateTime);

    for (EventInterface event : eventsToEdit) {
      ZonedDateTime newEndForThisEvent = event.getEndDateTime()
          .withHour(newEndTime.getHour())
          .withMinute(newEndTime.getMinute())
          .withSecond(0)
          .withNano(0);

      event.setEndDateTimeInternal(newEndForThisEvent);
    }
  }

  /**
   * Edits description for multiple events from a point forward.
   */
  private void editEventsFromDescription(List<EventInterface> eventsToEdit, String newDescription) {
    for (EventInterface event : eventsToEdit) {
      event.setDescription(newDescription);
    }
  }

  /**
   * Edits location for multiple events from a point forward.
   */
  private void editEventsFromLocation(List<EventInterface> eventsToEdit, String newLocation) {
    for (EventInterface event : eventsToEdit) {
      event.setLocation(newLocation);
    }
  }

  /**
   * Edits status for multiple events from a point forward.
   */
  private void editEventsFromStatus(List<EventInterface> eventsToEdit, String newStatusStr) {
    EventStatus newStatus = EventStatus.valueOf(newStatusStr.toUpperCase());
    for (EventInterface event : eventsToEdit) {
      event.setStatus(newStatus);
    }
  }

  // ==================== EDIT HELPERS: ENTIRE SERIES ====================

  /**
   * Edits subject for all events in a series.
   * All events stay in the same series.
   */
  private void editSeriesSubject(List<EventInterface> eventsToEdit, String newSubject)
      throws EditConflictException {

    for (EventInterface event : eventsToEdit) {
      validateNoEditDuplicate(event, newSubject,
          event.getStartDateTime(),
          event.getEndDateTime());
    }

    for (EventInterface event : eventsToEdit) {
      event.setSubjectInternal(newSubject);
    }
  }

  /**
   * Edits start time for all events in a series.
   * Does NOT split series - all events keep same seriesId with updated times.
   * Only the TIME portion is applied to each event (preserving each event's original date).
   */
  private void editSeriesStart(List<EventInterface> eventsToEdit, String newStartStr)
      throws EditConflictException, InvalidDateTimeException {

    ZonedDateTime newStartReference = parseDateTime(newStartStr);
    LocalTime newStartTime = newStartReference.toLocalTime();

    EventInterface firstEvent = eventsToEdit.get(0);
    validateTimeOnlyChange(firstEvent.getStartDateTime(), newStartReference, "start");

    for (EventInterface event : eventsToEdit) {
      ZonedDateTime newStartForThisEvent = event.getStartDateTime()
          .withHour(newStartTime.getHour())
          .withMinute(newStartTime.getMinute())
          .withSecond(0)
          .withNano(0);

      validateTemporalConstraints(newStartForThisEvent, event.getEndDateTime(), true);
      validateNoEditDuplicate(event, event.getSubject(),
          newStartForThisEvent, event.getEndDateTime());
    }

    for (EventInterface event : eventsToEdit) {
      ZonedDateTime newStartForThisEvent = event.getStartDateTime()
          .withHour(newStartTime.getHour())
          .withMinute(newStartTime.getMinute())
          .withSecond(0)
          .withNano(0);

      event.setStartDateTimeInternal(newStartForThisEvent);
    }
  }

  /**
   * Edits end time for all events in a series.
   * Does NOT split series - all events keep same seriesId with updated times.
   * Only the TIME portion is applied to each event (preserving each event's original date).
   */
  private void editSeriesEnd(List<EventInterface> eventsToEdit, String newEndStr)
      throws EditConflictException, InvalidDateTimeException {

    ZonedDateTime newEndReference = parseDateTime(newEndStr);
    LocalTime newEndTime = newEndReference.toLocalTime();

    EventInterface firstEvent = eventsToEdit.get(0);
    validateTimeOnlyChange(firstEvent.getEndDateTime(), newEndReference, "end");

    for (EventInterface event : eventsToEdit) {
      ZonedDateTime newEndForThisEvent = event.getEndDateTime()
          .withHour(newEndTime.getHour())
          .withMinute(newEndTime.getMinute())
          .withSecond(0)
          .withNano(0);

      validateTemporalConstraints(event.getStartDateTime(), newEndForThisEvent, true);
      validateNoEditDuplicate(event, event.getSubject(),
          event.getStartDateTime(), newEndForThisEvent);
    }

    for (EventInterface event : eventsToEdit) {
      ZonedDateTime newEndForThisEvent = event.getEndDateTime()
          .withHour(newEndTime.getHour())
          .withMinute(newEndTime.getMinute())
          .withSecond(0)
          .withNano(0);

      event.setEndDateTimeInternal(newEndForThisEvent);
    }
  }

  /**
   * Edits description for all events in a series.
   */
  private void editSeriesDescription(List<EventInterface> eventsToEdit, String newDescription) {
    for (EventInterface event : eventsToEdit) {
      event.setDescription(newDescription);
    }
  }

  /**
   * Edits location for all events in a series.
   */
  private void editSeriesLocation(List<EventInterface> eventsToEdit, String newLocation) {
    for (EventInterface event : eventsToEdit) {
      event.setLocation(newLocation);
    }
  }

  /**
   * Edits status for all events in a series.
   */
  private void editSeriesStatus(List<EventInterface> eventsToEdit, String newStatusStr) {
    EventStatus newStatus = EventStatus.valueOf(newStatusStr.toUpperCase());
    for (EventInterface event : eventsToEdit) {
      event.setStatus(newStatus);
    }
  }

  // ==================== QUERY HELPERS ====================

  /**
   * Finds a unique event by subject, start datetime, and end datetime.
   *
   * @param subject       the event subject
   * @param startDateTime the start datetime
   * @param endDateTime   the end datetime
   * @return the unique event matching all criteria
   * @throws EventNotFoundException       if no event matches
   * @throws MultipleEventsFoundException if more than one event matches
   */
  private EventInterface findUniqueEvent(String subject, ZonedDateTime startDateTime,
                                ZonedDateTime endDateTime)
      throws EventNotFoundException, MultipleEventsFoundException {

    LocalDate startDate = startDateTime.toLocalDate();
    List<EventInterface> eventsOnDate = eventsByDate.get(startDate);

    if (eventsOnDate == null || eventsOnDate.isEmpty()) {
      throw new EventNotFoundException(
          String.format("No event found: '%s' from %s to %s",
              subject, startDateTime, endDateTime)
      );
    }

    List<EventInterface> matches = new ArrayList<>();
    for (EventInterface event : eventsOnDate) {
      if (event.getSubject().equals(subject)
          && event.getStartDateTime().equals(startDateTime)
          && event.getEndDateTime().equals(endDateTime)) {
        matches.add(event);
      }
    }

    if (matches.isEmpty()) {
      throw new EventNotFoundException(
          String.format("No event found: '%s' from %s to %s",
              subject, startDateTime, endDateTime)
      );
    }

    if (matches.size() > 1) {
      throw new MultipleEventsFoundException(
          String.format("Found %d events matching: '%s' from %s to %s. Cannot edit.",
              matches.size(), subject, startDateTime, endDateTime)
      );
    }

    return matches.get(0);
  }

  /**
   * Finds an event by subject and start time only.
   * Used when we don't know the end time yet (for series operations).
   *
   * @param subject       the event subject
   * @param startDateTime the start datetime
   * @return the unique event matching subject and start time
   * @throws EventNotFoundException       if no event found
   * @throws MultipleEventsFoundException if multiple events match
   */
  private EventInterface findEventBySubjectAndStart(String subject, ZonedDateTime startDateTime)
      throws EventNotFoundException, MultipleEventsFoundException {

    LocalDate startDate = startDateTime.toLocalDate();
    List<EventInterface> eventsOnDate = eventsByDate.get(startDate);

    if (eventsOnDate == null || eventsOnDate.isEmpty()) {
      throw new EventNotFoundException(
          String.format("No event found: '%s' starting at %s", subject, startDateTime)
      );
    }

    List<EventInterface> matches = new ArrayList<>();
    for (EventInterface event : eventsOnDate) {
      if (event.getSubject().equals(subject)
          && event.getStartDateTime().equals(startDateTime)) {
        matches.add(event);
      }
    }

    if (matches.isEmpty()) {
      throw new EventNotFoundException(
          String.format("No event found: '%s' starting at %s", subject, startDateTime)
      );
    }

    if (matches.size() > 1) {
      throw new MultipleEventsFoundException(
          String.format("Found %d events matching: '%s' starting at %s. Cannot edit.",
              matches.size(), subject, startDateTime)
      );
    }

    return matches.get(0);
  }

  /**
   * Gets all events in a series, sorted by start datetime.
   *
   * @param seriesId the series ID
   * @return list of events in the series, empty list if series doesn't exist
   */
  private List<EventInterface> getEventsInSeries(String seriesId) {
    List<EventInterface> events = eventsBySeriesId.get(seriesId);

    if (events == null) {
      return new ArrayList<>();
    }

    List<EventInterface> sortedEvents = new ArrayList<>(events);
    sortedEvents.sort((e1, e2) -> e1.getStartDateTime().compareTo(e2.getStartDateTime()));

    return sortedEvents;
  }

  /**
   * Gets all events in a series starting from (and including) a specific datetime.
   * Returns events sorted by start datetime.
   *
   * @param seriesId     the series ID
   * @param fromDateTime the datetime to start from (inclusive)
   * @return list of events in series with startDateTime >= fromDateTime
   */
  private List<EventInterface> getEventsInSeriesFrom(String seriesId, ZonedDateTime fromDateTime) {
    List<EventInterface> allEventsInSeries = getEventsInSeries(seriesId);

    List<EventInterface> filteredEvents = new ArrayList<>();
    for (EventInterface event : allEventsInSeries) {
      if (!event.getStartDateTime().isBefore(fromDateTime)) {
        filteredEvents.add(event);
      }
    }

    return filteredEvents;
  }

  // ==================== VALIDATION HELPERS ====================
  /**
   * Removes an event from all date indices it currently occupies.
   * Used before re-indexing an event after datetime changes.
   *
   * @param event the event to remove from date maps
   */
  private void removeEventFromDateMaps(EventInterface event) {
    LocalDate startDate = event.getStartDateTime().toLocalDate();
    LocalDate endDate = event.getEndDateTime().toLocalDate();

    LocalDate currentDate = startDate;
    while (!currentDate.isAfter(endDate)) {
      List<EventInterface> eventsOnDate = eventsByDate.get(currentDate);
      if (eventsOnDate != null) {
        eventsOnDate.remove(event);

        // Clean up empty date entries
        if (eventsOnDate.isEmpty()) {
          eventsByDate.remove(currentDate);
        }
      }
      currentDate = currentDate.plusDays(1);
    }
  }

  /**
   * Validates that editing an event won't create a duplicate.
   * Checks if another event already exists with the new subject/start/end combination.
   *
   * @param eventBeingEdited the event being modified (excluded from duplicate check)
   * @param newSubject       the new subject (or current subject if not changing)
   * @param newStart         the new start datetime (or current start if not changing)
   * @param newEnd           the new end datetime (or current end if not changing)
   * @throws EditConflictException if a duplicate would be created
   */
  private void validateNoEditDuplicate(EventInterface eventBeingEdited, String newSubject,
                                       ZonedDateTime newStart, ZonedDateTime newEnd)
      throws EditConflictException {

    LocalDate startDate = newStart.toLocalDate();
    List<EventInterface> eventsOnDate = eventsByDate.get(startDate);

    if (eventsOnDate == null) {
      return;
    }

    for (EventInterface existing : eventsOnDate) {
      if (existing == eventBeingEdited) {
        continue;
      }

      if (existing.getSubject().equals(newSubject)
          && existing.getStartDateTime().equals(newStart)
          && existing.getEndDateTime().equals(newEnd)) {
        throw new EditConflictException(
            String.format("Edit would create duplicate event: '%s' from %s to %s",
                newSubject, newStart, newEnd)
        );
      }
    }
  }

  /**
   * Validates temporal constraints for an edit operation.
   * Checks that start is before end, and for series events, that they're single-day.
   *
   * @param newStart      the new start datetime
   * @param newEnd        the new end datetime
   * @param isSeriesEvent whether this event is part of a series
   * @throws EditConflictException if constraints are violated
   */
  private void validateTemporalConstraints(ZonedDateTime newStart, ZonedDateTime newEnd,
                                           boolean isSeriesEvent)
      throws EditConflictException {

    if (!newStart.isBefore(newEnd)) {
      throw new EditConflictException(
          String.format("Start time must be before end time. Start: %s, End: %s",
              newStart, newEnd)
      );
    }

    if (isSeriesEvent) {
      LocalDate startDate = newStart.toLocalDate();
      LocalDate endDate = newEnd.toLocalDate();

      if (!startDate.equals(endDate)) {
        throw new EditConflictException(
            String.format("Series events must be single-day. Start: %s, End: %s",
                newStart, newEnd)
        );
      }
    }
  }

  /**
   * Validates that a temporal edit (start/end change) for multiple events
   * only changes the TIME, not the DATE.
   *
   * @param originalDateTime the original datetime
   * @param newDateTime      the new datetime
   * @param propertyName     "start" or "end" (for error message)
   * @throws EditConflictException if date portion changed
   */
  private void validateTimeOnlyChange(ZonedDateTime originalDateTime,
                                      ZonedDateTime newDateTime,
                                      String propertyName)
      throws EditConflictException {

    LocalDate originalDate = originalDateTime.toLocalDate();
    LocalDate newDate = newDateTime.toLocalDate();

    if (!originalDate.equals(newDate)) {
      throw new EditConflictException(
          String.format("Cannot change date when editing multiple events in a series. "
                  + "Only time changes allowed. Original %s: %s, New %s: %s",
              propertyName, originalDateTime, propertyName, newDateTime)
      );
    }
  }

  /**
   * Checks if an event is a duplicate using Event.equals().
   * Two events are duplicates if they have the same subject, start, and end times.
   *
   * @param newEvent the event to check
   * @throws DuplicateEventException if event already exists
   */
  private void checkForDuplicates(EventInterface newEvent) throws DuplicateEventException {
    LocalDate startDate = newEvent.getStartDateTime().toLocalDate();
    List<EventInterface> eventsOnStartDate = eventsByDate.get(startDate);

    if (eventsOnStartDate != null && eventsOnStartDate.contains(newEvent)) {
      throw new DuplicateEventException("Event already exists");
    }
  }

  // ==================== SERIES MANAGEMENT ====================

  /**
   * Splits a series at a specific datetime.
   * Events from the datetime forward get a new seriesId.
   * Events before the datetime remain in the original series.
   *
   * @param originalSeriesId the original series ID
   * @param fromDateTime     the datetime to split at (inclusive - this event gets new ID)
   * @return the new series ID for events from datetime forward
   */
  private String splitSeries(String originalSeriesId, ZonedDateTime fromDateTime) {
    String newSeriesId = UUID.randomUUID().toString();

    List<EventInterface> allEvents = getEventsInSeries(originalSeriesId);

    List<EventInterface> eventsInNewSeries = new ArrayList<>();
    List<EventInterface> eventsInOriginalSeries = new ArrayList<>();

    for (EventInterface event : allEvents) {
      if (!event.getStartDateTime().isBefore(fromDateTime)) {
        event.setSeriesId(newSeriesId);
        eventsInNewSeries.add(event);
      } else {
        eventsInOriginalSeries.add(event);
      }
    }

    if (eventsInOriginalSeries.isEmpty()) {
      eventsBySeriesId.remove(originalSeriesId);
    } else {
      eventsBySeriesId.put(originalSeriesId, eventsInOriginalSeries);
    }

    eventsBySeriesId.put(newSeriesId, eventsInNewSeries);

    return newSeriesId;
  }

  /**
   * Removes an event from its series.
   * Updates the eventsBySeriesId map and sets the event's seriesId to null.
   * Cleans up empty series.
   *
   * @param event the event to remove from its series
   */
  private void removeEventFromSeries(EventInterface event) {
    String seriesId = event.getSeriesId();

    if (seriesId == null) {
      return;
    }

    List<EventInterface> eventsInSeries = eventsBySeriesId.get(seriesId);
    if (eventsInSeries != null) {
      eventsInSeries.remove(event);

      if (eventsInSeries.isEmpty()) {
        eventsBySeriesId.remove(seriesId);
      }
    }

    event.removeFromSeries();
  }

  /**
   * Adds an event to the internal storage maps.
   * Multi-day events are stored under every date they span.
   */
  private void addEventToMaps(EventInterface event) {
    LocalDate startDate = event.getStartDateTime().toLocalDate();
    LocalDate endDate = event.getEndDateTime().toLocalDate();

    LocalDate currentDate = startDate;
    while (!currentDate.isAfter(endDate)) {
      eventsByDate.computeIfAbsent(currentDate, k -> new ArrayList<>()).add(event);
      currentDate = currentDate.plusDays(1);
    }

    if (event.getSeriesId() != null) {
      eventsBySeriesId.computeIfAbsent(event.getSeriesId(), k -> new ArrayList<>()).add(event);
    }
  }

  private void applySubjectEdit(List<EventInterface> eventsToEdit, String newValue, EditScope scope)
      throws EditConflictException {
    if (scope == EditScope.SINGLE) {
      editEventSubject(eventsToEdit.get(0), newValue);
    } else if (scope == EditScope.FROM_POINT) {
      editEventsFromSubject(eventsToEdit, newValue);
    } else {
      editSeriesSubject(eventsToEdit, newValue);
    }
  }

  private void applyStartEdit(List<EventInterface> eventsToEdit, String newValue, EditScope scope,
                              String seriesId, ZonedDateTime fromDateTime)
      throws EditConflictException, InvalidDateTimeException {
    if (scope == EditScope.SINGLE) {
      editEventStart(eventsToEdit.get(0), newValue);
    } else if (scope == EditScope.FROM_POINT) {
      editEventsFromStart(eventsToEdit, seriesId, fromDateTime, newValue);
    } else {
      editSeriesStart(eventsToEdit, newValue);
    }
  }

  private void applyEndEdit(List<EventInterface> eventsToEdit, String newValue, EditScope scope,
                            String seriesId, ZonedDateTime fromDateTime)
      throws EditConflictException, InvalidDateTimeException {
    if (scope == EditScope.SINGLE) {
      editEventEnd(eventsToEdit.get(0), newValue);
    } else if (scope == EditScope.FROM_POINT) {
      editEventsFromEnd(eventsToEdit, seriesId, fromDateTime, newValue);
    } else {
      editSeriesEnd(eventsToEdit, newValue);
    }
  }

  private void applyDescriptionEdit(List<EventInterface> eventsToEdit,
                                    String newValue, EditScope scope) {
    if (scope == EditScope.SINGLE) {
      eventsToEdit.get(0).setDescription(newValue);
    } else if (scope == EditScope.FROM_POINT) {
      editEventsFromDescription(eventsToEdit, newValue);
    } else {
      editSeriesDescription(eventsToEdit, newValue);
    }
  }

  private void applyLocationEdit(List<EventInterface> eventsToEdit,
                                 String newValue, EditScope scope) {
    if (scope == EditScope.SINGLE) {
      eventsToEdit.get(0).setLocation(newValue);
    } else if (scope == EditScope.FROM_POINT) {
      editEventsFromLocation(eventsToEdit, newValue);
    } else {
      editSeriesLocation(eventsToEdit, newValue);
    }
  }

  private void applyStatusEdit(List<EventInterface> eventsToEdit,
                               String newValue, EditScope scope) {
    EventStatus newStatus = EventStatus.valueOf(newValue.toUpperCase());
    if (scope == EditScope.SINGLE) {
      eventsToEdit.get(0).setStatus(newStatus);
    } else if (scope == EditScope.FROM_POINT) {
      editEventsFromStatus(eventsToEdit, newValue);
    } else {
      editSeriesStatus(eventsToEdit, newValue);
    }
  }

  private void performEdit(String property, String subject,
                           String startDateTime, String endDateTime,
                           String newValue, EditScope scope)
      throws EventNotFoundException, MultipleEventsFoundException,
      EditConflictException, InvalidDateTimeException, InvalidPropertyException {

    ZonedDateTime start = parseDateTime(startDateTime);
    EventInterface referenceEvent;

    if (scope == EditScope.SINGLE) {
      ZonedDateTime end = parseDateTime(endDateTime);
      referenceEvent = findUniqueEvent(subject, start, end);
    } else {
      referenceEvent = findEventBySubjectAndStart(subject, start);
    }

    List<EventInterface> eventsToEdit;
    String seriesId = referenceEvent.getSeriesId();

    if (scope == EditScope.SINGLE) {
      eventsToEdit = List.of(referenceEvent);
    } else if (seriesId == null) {
      String endStr = formatDateTime(referenceEvent.getEndDateTime());
      editEvent(property, subject, startDateTime, endStr, newValue);
      return;
    } else if (scope == EditScope.FROM_POINT) {
      eventsToEdit = getEventsInSeriesFrom(seriesId, start);
    } else {
      eventsToEdit = getEventsInSeries(seriesId);
    }

    switch (property.toLowerCase()) {
      case "subject":
        applySubjectEdit(eventsToEdit, newValue, scope);
        break;
      case "start":
        applyStartEdit(eventsToEdit, newValue, scope, seriesId, start);
        break;
      case "end":
        applyEndEdit(eventsToEdit, newValue, scope, seriesId, start);
        break;
      case "description":
        applyDescriptionEdit(eventsToEdit, newValue, scope);
        break;
      case "location":
        applyLocationEdit(eventsToEdit, newValue, scope);
        break;
      case "status":
        applyStatusEdit(eventsToEdit, newValue, scope);
        break;
      default:
        throw new InvalidPropertyException(property,
            new String[] {"subject", "start", "end", "description", "location", "status"});
    }
  }

  /**
   * Creates a defensive copy of an event.
   * The copy has the same properties but is a distinct object.
   */
  private EventInterface copyEvent(EventInterface original) {
    Event copy = new Event(original.getSubject(),
        original.getStartDateTime(),
        original.getEndDateTime());
    copy.setDescription(original.getDescription());
    copy.setLocation(original.getLocation());
    copy.setStatus(original.getStatus());
    if (original.getSeriesId() != null) {
      copy.setSeriesId(original.getSeriesId());
    }
    return copy;
  }

  /**
   * Parses datetime string in format YYYY-MM-DDThh:mm in the specific zone.
   *
   * @param dateTimeStr the datetime string to parse
   * @return ZonedDateTime in America/New_York timezone
   * @throws InvalidDateTimeException if format is invalid
   */
  private ZonedDateTime parseDateTime(String dateTimeStr) throws InvalidDateTimeException {
    return DateTimeParser.parseDateTime(dateTimeStr, this.timezone);
  }

  /**
   * Parses date string in format YYYY-MM-DD.
   *
   * @param dateStr the date string to parse
   * @return LocalDate
   * @throws IllegalArgumentException if format is invalid
   */
  private LocalDate parseDate(String dateStr) throws InvalidDateTimeException {
    return DateTimeParser.parseDateToLocalDate(dateStr);
  }

  /**
   * Formats ZonedDateTime to string in format YYYY-MM-DDThh:mm.
   * Delegates to DateTimeParser for consistent formatting across the application.
   */
  private String formatDateTime(ZonedDateTime dateTime) {
    return DateTimeParser.formatDateTime(dateTime);
  }

  private void validateName(String name) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be null or empty");
    }
  }

  private void validateTimezone(ZoneId timezone) {
    if (timezone == null) {
      throw new IllegalArgumentException("Timezone cannot be null");
    }
  }
}