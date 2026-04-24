package calendar.model;

import static calendar.model.CalendarEventUtils.TimeWindow;
import static calendar.model.CalendarEventUtils.calculateTargetEnd;
import static calendar.model.CalendarEventUtils.calculateTimeWindowForDate;
import static calendar.model.CalendarEventUtils.calculateTimeWindowWithOffset;
import static calendar.model.CalendarEventUtils.convertToTimezone;
import static calendar.model.CalendarEventUtils.createTempEvent;
import static calendar.model.CalendarEventUtils.isMultiDayEvent;
import static calendar.model.CalendarEventUtils.isSingleDaySeries;
import static calendar.model.CalendarEventUtils.isTimeProperty;
import static calendar.model.CalendarEventUtils.preProcess;
import static calendar.model.CalendarEventUtils.setAllDayTimes;
import static calendar.model.CalendarEventUtils.withUpdatedProperty;
import static calendar.model.CalendarEventUtils.withUpdatedTime;

import calendar.model.event.CalendarEvent;
import calendar.model.event.SeriesEvent;
import calendar.model.event.SeriesLinkedList;
import calendar.model.event.SingleEvent;
import calendar.model.tree.IntervalTree;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Implementation of the calendar model managing events and event series.
 * Provides functionality for creating, editing, and querying calendar events
 * using an interval tree for efficient temporal queries and linked lists
 * for managing event series.
 *
 * <p>This model maintains two primary data structures:
 * <ul>
 *   <li>An interval tree for efficient event storage and temporal queries.</li>
 *   <li>A map of series UIDs to linked lists for managing recurring events.</li>
 * </ul>
 */
public class CalendarModel implements CalendarModelInterface {

  /**
   * Interval tree storing all calendar events for efficient temporal queries.
   */
  private final IntervalTree calendarTree;

  /**
   * Map of series UIDs to their corresponding linked lists of events.
   */
  private final Map<String, SeriesLinkedList> seriesByUid;

  /**
   * Timezone for the created Calendar Model.
   **/
  private final ZoneId timezone;

  /**
   * Constructs a new empty calendar model with default EST timezone.
   * Delegates to the parameterized constructor with America/New_York timezone.
   */
  public CalendarModel() {
    this(ZoneId.of("America/New_York"));
  }

  /**
   * Constructs a new empty calendar model with specified timezone.
   * All datetime values in operations will be converted to this timezone.
   *
   * @param timeZone the timezone for this calendar.
   */
  public CalendarModel(ZoneId timeZone) {
    this.calendarTree = new IntervalTree();
    this.seriesByUid = new HashMap<>();
    this.timezone = timeZone;
  }

  /**
   * Gets the timezone of this calendar.
   *
   * @return the calendar's timezone.
   */
  @Override
  public ZoneId getTimezone() {
    return timezone;
  }

  /**
   * Creates a new CalendarModel with all events converted to the specified timezone.
   * Single events are recreated individually, while series events are processed once
   * per series to maintain their relationships.
   *
   * @param timeZone the new timezone for the calendar
   * @return a new CalendarModel with all events in the specified timezone
   * @throws IllegalArgumentException if timezone conversion would create multi-day series events
   */
  @Override
  public CalendarModelInterface updateTimezone(ZoneId timeZone) {
    CalendarModel newModel = new CalendarModel(timeZone);
    Set<String> visitedSeries = new HashSet<>();
    for (CalendarEvent event : calendarTree.getAllEvents()) {
      String seriesUid = event.getSeriesUid();
      if (seriesUid == null) {
        newModel.createSingleEvent(event.getHashMap());
      } else if (!visitedSeries.contains(seriesUid)) {
        visitedSeries.add(seriesUid);
        SeriesLinkedList oldList = seriesByUid.get(seriesUid);
        newModel.updateSeriesListTimezone(oldList, timeZone);
      }
    }
    return newModel;
  }

  /**
   * Converts a series of events to a new timezone and adds them to this calendar.
   * All events in the series are converted and validated to ensure they don't
   * become multi-day events after timezone conversion.
   *
   * @param oldList  the series linked list containing events to convert
   * @param timeZone the timezone to convert to
   * @throws IllegalArgumentException if any event in the series would become
   *                                  a multi-day event after timezone conversion
   */
  public void updateSeriesListTimezone(SeriesLinkedList oldList, ZoneId timeZone) {
    String seriesUid = oldList.getSeriesUid();
    List<CalendarEvent> events = new ArrayList<>();
    SeriesLinkedList newList = new SeriesLinkedList(seriesUid);
    for (CalendarEvent event : oldList.getAllEvents()) {
      Map<String, Object> params = event.getHashMap();
      convertToTimezone(params, timeZone);
      CalendarEvent newEvent = new SingleEvent(params, seriesUid);
      if (isMultiDayEvent(newEvent)) {
        throw new IllegalArgumentException(
            "Converting to timezone '" + timeZone + "' changes series to become multiday.");
      }
      events.add(newEvent);
    }
    newList.addAll(events);
    this.calendarTree.insertAll(events);
    seriesByUid.put(seriesUid, newList);
  }

  /**
   * Creates a single calendar event.
   *
   * @param parameters map containing "subject", "start", "end", and optionally
   *                   "description", "location", "status".
   * @throws IllegalArgumentException if parameters are invalid or event already exists.
   */
  @Override
  public void createSingleEvent(Map<String, Object> parameters) {
    convertToTimezone(parameters, timezone);
    CalendarEvent newEvent = new SingleEvent(parameters, null);
    calendarTree.insertSingleEvent(newEvent);
  }

  /**
   * Creates an event series that repeats on specific days of the week.
   * If the series results in only one occurrence, creates a single event instead.
   *
   * @param parameters map containing "subject", "start", "end", "weekdays",
   *                   and either "ndays" or "untildate".
   * @throws IllegalArgumentException if parameters are invalid or would create duplicates.
   */
  @Override
  public void createEventSeries(Map<String, Object> parameters) {
    convertToTimezone(parameters, timezone);
    if (isSingleDaySeries(parameters)) {
      createSingleEvent(parameters);
      return;
    }
    SeriesEvent newSeries = new SeriesEvent(parameters);
    List<CalendarEvent> newEvents = newSeries.getEventsInSeries();
    calendarTree.insertAll(newEvents);
    seriesByUid.put(newSeries.getSeriesUid(), newSeries.getEventsList());
  }

  /**
   * Creates a single all-day event (8am to 5pm).
   *
   * @param parameters map containing "ondate" and optionally "description",
   *                   "location", "status".
   * @throws IllegalArgumentException if parameters are invalid.
   */
  @Override
  public void createAllDayEvent(Map<String, Object> parameters) {
    setAllDayTimes(parameters, timezone);
    createSingleEvent(parameters);
  }

  /**
   * Creates an all-day event series.
   * If the series results in only one occurrence, creates a single all-day event.
   *
   * @param parameters map containing "ondate", "weekdays", and either "ndays" or "untildate".
   * @throws IllegalArgumentException if parameters are invalid.
   */
  @Override
  public void createAllDayEventSeries(Map<String, Object> parameters) {
    setAllDayTimes(parameters, timezone);
    if (isSingleDaySeries(parameters)) {
      createAllDayEvent(parameters);
    } else {
      createEventSeries(parameters);
    }
  }

  /**
   * Edits a single event instance.
   * If the event is part of a series and the start time is changed,
   * the event is removed from the series and becomes standalone.
   *
   * @param parameters map containing "subject", "start", "end", "property", "value".
   * @throws IllegalArgumentException if event not found or edit would create duplicate.
   */
  @Override
  public void editSingleEvent(Map<String, Object> parameters) {
    preProcess(parameters, timezone);

    EditContext context = new EditContext(parameters);
    CalendarEvent original = findEventOrThrow(context);

    boolean removeFromSeries = context.isStartProperty() && original.isPartOfSeries();
    CalendarEvent newEvent = createUpdatedEvent(original, context, removeFromSeries);

    validateSingleEdit(original, newEvent);
    if (isTimeProperty(context.property)) {
      applySingleTimeEdit(original, newEvent, removeFromSeries);
    } else {
      applySingleNonTimeEdit(original, newEvent);
    }
  }

  /**
   * Edits all events in a series.
   * If the target event is not part of a series, edits it as a single event.
   *
   * @param parameters map containing "subject", "start", "property", "value".
   * @throws IllegalArgumentException if event not found, not unique, or edit creates duplicate.
   */
  @Override
  public void editSeries(Map<String, Object> parameters) {
    preProcess(parameters, timezone);

    EditContext context = new EditContext(parameters);
    CalendarEvent original = findUniqueCandidate(context);
    context = context.withNewSeriesUid(original.getSeriesUid());

    if (original.getSeriesUid() == null) {
      parameters.put("end", original.getEndDateTime());
      editSingleEvent(parameters);
    } else {
      editEntireSeries(original, context);
    }
  }

  /**
   * Edits all events in a series starting from a specific date.
   * If editing the start property, splits the series into two separate series.
   * If not part of a series, edits it as a single event.
   *
   * @param parameters map containing "subject", "start", "property", "value".
   * @throws IllegalArgumentException if event not found, not unique, or edit creates duplicate.
   */
  @Override
  public void editEvents(Map<String, Object> parameters) {
    preProcess(parameters, timezone);

    EditContext context = new EditContext(parameters);
    CalendarEvent original = findUniqueCandidate(context);

    if (original.getSeriesUid() == null) {
      parameters.put("end", original.getEndDateTime());
      editSingleEvent(parameters);
    } else {
      editSeriesFrom(original, context);
    }
  }

  /**
   * Copies a single event to a target calendar at a specified time.
   * The event's duration is preserved while the start time is changed.
   *
   * @param target     the calendar to copy the event to
   * @param parameters map containing "subject", "start", and "targetstart"
   * @throws IllegalArgumentException if the event is not found or not unique
   */
  @Override
  public void copyEvent(CalendarModelInterface target, Map<String, Object> parameters) {
    EditContext context = new EditContext(parameters);
    CalendarEvent original = findUniqueCandidate(context);

    ZonedDateTime targetStart = ((ZonedDateTime) parameters.get("targetstart"))
        .withZoneSameLocal(target.getTimezone());
    ZonedDateTime targetEnd = calculateTargetEnd(
        original.getStartDateTime(),
        original.getEndDateTime(),
        targetStart);

    Map<String, Object> params = new HashMap<>(original.getHashMap());
    params.put("start", targetStart);
    params.put("end", targetEnd);
    target.createSingleEvent(params);
  }

  /**
   * Copies all events from a source date to a target date.
   * Preserves the time-of-day for each event while changing the date.
   * Series relationships are maintained in the target calendar.
   *
   * @param target     the calendar to copy events to
   * @param parameters map containing "sourcedate" and "targetdate"
   * @throws IllegalArgumentException if copying creates multi-day series events
   *                                  or duplicate events in the target calendar
   */
  @Override
  public void copyEventsOn(CalendarModelInterface target, Map<String, Object> parameters) {
    LocalDate sourceDate = (LocalDate) parameters.get("sourcedate");
    LocalDate targetDate = (LocalDate) parameters.get("targetdate");

    List<CalendarEvent> events = this.getEventsOn(sourceDate);
    if (events.isEmpty()) {
      throw new IllegalArgumentException("No events found on date: " + sourceDate);
    }
    EventCopyHelper helper = new EventCopyHelper(target);

    for (CalendarEvent event : events) {
      TimeWindow window = calculateTimeWindowForDate(
          event, targetDate, target.getTimezone());
      helper.addCopiedEvent(event, window.start, window.end);
    }

    helper.execute();
  }

  /**
   * Copies all events within a date range to a new starting date.
   * Events are shifted by the day offset between source and target dates.
   * Series relationships are maintained in the target calendar.
   *
   * @param target     the calendar to copy events to
   * @param parameters map containing "startdate", "enddate", and "targetdate"
   * @throws IllegalArgumentException if copying creates multi-day series events
   *                                  or duplicate events in the target calendar
   */
  @Override
  public void copyEventsBetween(CalendarModelInterface target, Map<String, Object> parameters) {
    LocalDate startDate = (LocalDate) parameters.get("startdate");
    LocalDate endDate = (LocalDate) parameters.get("enddate");
    LocalDate targetDate = (LocalDate) parameters.get("targetdate");

    ZonedDateTime sourceStart = (startDate).atStartOfDay(this.getTimezone());
    ZonedDateTime sourceEnd = (endDate).plusDays(1).atStartOfDay(this.getTimezone());

    List<CalendarEvent> events = this.getEventsInRange(sourceStart, sourceEnd);
    if (events.isEmpty()) {
      throw new IllegalArgumentException(
          "No events found between " + startDate + " and " + endDate);
    }
    long dayOffset = ChronoUnit.DAYS.between(startDate, targetDate);

    EventCopyHelper helper = new EventCopyHelper(target);

    for (CalendarEvent event : events) {
      TimeWindow window = calculateTimeWindowWithOffset(
          event, dayOffset, target.getTimezone());
      helper.addCopiedEvent(event, window.start, window.end);
    }

    helper.execute();
  }

  /**
   * Inserts copied events into this calendar, maintaining series relationships.
   * Validates that no duplicate events will be created before insertion.
   *
   * @param seriesGroups map of series UIDs to their copied events
   * @param newEvents    list of all events to insert
   * @throws IllegalArgumentException if any event would create a duplicate
   */
  private void insertCopiedEvents(Map<String, List<CalendarEvent>> seriesGroups,
                                     List<CalendarEvent> newEvents) {
    validateMultipleEdit(new ArrayList<>(), newEvents);
    this.calendarTree.insertAll(newEvents);

    seriesGroups.forEach((seriesUid, seriesEvents) -> {
      SeriesLinkedList seriesList = seriesByUid.computeIfAbsent(
          seriesUid, k -> new SeriesLinkedList(seriesUid));
      seriesList.addAll(seriesEvents);
    });
  }

  /**
   * Retrieves all events in the calendar.
   *
   * @return list of all calendar events in chronological order.
   */
  @Override
  public List<CalendarEvent> getAllEvents() {
    return calendarTree.getAllEvents();
  }

  /**
   * Retrieves all events scheduled on a specific date.
   *
   * @param date the date to query.
   * @return list of events occurring on the specified date.
   */
  @Override
  public List<CalendarEvent> getEventsOn(LocalDate date) {
    ZonedDateTime rangeStart = date.atStartOfDay(timezone);
    ZonedDateTime rangeEnd = date.plusDays(1).atStartOfDay(timezone);
    return calendarTree.findEventsInRange(rangeStart, rangeEnd);
  }

  /**
   * Retrieves all events within a specified time range.
   *
   * @param startDateTime the start of the range (inclusive).
   * @param endDateTime   the end of the range (exclusive).
   * @return list of events that overlap with the specified range.
   */
  @Override
  public List<CalendarEvent> getEventsInRange(ZonedDateTime startDateTime,
                                              ZonedDateTime endDateTime) {
    ZonedDateTime startTime = startDateTime.withZoneSameInstant(this.getTimezone());
    ZonedDateTime endTime = endDateTime.withZoneSameInstant(this.getTimezone());
    return calendarTree.findEventsInRange(startTime, endTime);
  }

  /**
   * Checks if the user is available at a specific date and time.
   *
   * @param dateTime the date-time to check.
   * @return true if available (no events scheduled), false if busy.
   */
  @Override
  public boolean checkAvailability(ZonedDateTime dateTime) {
    ZonedDateTime instant = dateTime.withZoneSameInstant(timezone);
    return !calendarTree.isBusy(instant);
  }

  /**
   * Finds an event by exact match of subject, start, and end times.
   *
   * @param context the edit context containing event identifiers.
   * @return the matching calendar event.
   * @throws IllegalArgumentException if no event is found.
   */
  private CalendarEvent findEventOrThrow(EditContext context) {
    CalendarEvent tempEvent =
        createTempEvent(context.subject, context.startDateTime, context.endDateTime);
    CalendarEvent original = calendarTree.findEvent(tempEvent);

    if (original == null) {
      throw new IllegalArgumentException("Event not found");
    }

    return original;
  }

  /**
   * Finds a unique event candidate by subject and start time.
   *
   * @param context the edit context containing search criteria.
   * @return the unique matching event.
   * @throws IllegalArgumentException if no event found or multiple candidates exist.
   */
  private CalendarEvent findUniqueCandidate(EditContext context) {
    List<CalendarEvent> candidates = calendarTree.findEventsByFields(
        context.subject, context.startDateTime);

    if (candidates.isEmpty()) {
      throw new IllegalArgumentException("Event not found");
    }

    if (candidates.size() > 1) {
      throw new IllegalArgumentException(
          "Found " + candidates.size() + " events with subject '" + context.subject
              + "' starting at " + context.startDateTime.toLocalDateTime()
              + ". Cannot uniquely identify event. Please provide end time to specify which event "
              + "to edit.");
    }

    return candidates.get(0);
  }

  /**
   * Replaces events in the tree without removing and re-inserting.
   * Used for non-time property edits where tree structure doesn't change.
   *
   * @param oldEvents list of original events.
   * @param newEvents list of updated events (same size and order as oldEvents).
   */
  private void replaceInTree(List<CalendarEvent> oldEvents, List<CalendarEvent> newEvents) {
    for (int i = 0; i < oldEvents.size(); i++) {
      calendarTree.replaceEventInPlace(oldEvents.get(i), newEvents.get(i));
    }
  }

  /**
   * Edits all events in a series.
   *
   * @param original the event used to identify the series.
   * @param context  the edit context containing property and new value.
   */
  private void editEntireSeries(CalendarEvent original, EditContext context) {
    SeriesLinkedList seriesList = seriesByUid.get(original.getSeriesUid());
    List<CalendarEvent> allEvents = seriesList.getAllEvents();
    List<CalendarEvent> updatedEvents = createUpdatedEventsList(allEvents, context, false);
    validateMultipleEdit(allEvents, updatedEvents);
    applySeriesEdit(allEvents, updatedEvents, context);
  }

  /**
   * Edits events in a series from a specific event onwards.
   *
   * @param original the event to start editing from.
   * @param context  the edit context containing property and new value.
   */
  private void editSeriesFrom(CalendarEvent original, EditContext context) {
    SeriesLinkedList seriesList = seriesByUid.get(original.getSeriesUid());
    List<CalendarEvent> eventsFrom = seriesList.getEventsFrom(original);
    if (context.isStartProperty()) {
      editWithSplit(original, eventsFrom, seriesList, context);
    } else {
      List<CalendarEvent> updatedEvents = createUpdatedEventsList(eventsFrom, context, false);
      validateMultipleEdit(eventsFrom, updatedEvents);
      applySeriesFrom(eventsFrom, updatedEvents, seriesList, context);
    }
  }

  /**
   * Edits events from a specific point and splits the series.
   * Creates a new series with a new UID for the split portion.
   *
   * @param original   the event at the split point.
   * @param eventsFrom list of events from the split point onwards.
   * @param oldSeries  the original series list.
   * @param context    the edit context.
   */
  private void editWithSplit(CalendarEvent original, List<CalendarEvent> eventsFrom,
                             SeriesLinkedList oldSeries, EditContext context) {
    String newSeriesUid = UUID.randomUUID().toString();
    EditContext splitContext = context.withNewSeriesUid(newSeriesUid);
    List<CalendarEvent> updatedEvents = createUpdatedEventsList(eventsFrom, splitContext, true);

    validateMultipleEdit(eventsFrom, updatedEvents);

    calendarTree.removeAll(eventsFrom);

    SeriesLinkedList newSeries = oldSeries.splitUpdateFrom(
        original, newSeriesUid, (ZonedDateTime) context.newValue);

    calendarTree.insertAll(updatedEvents);

    seriesByUid.put(newSeriesUid, newSeries);
    if (oldSeries.isEmpty()) {
      seriesByUid.remove(original.getSeriesUid());
    }
  }

  /**
   * Creates an updated version of a single event.
   *
   * @param original         the original event.
   * @param context          the edit context.
   * @param removeFromSeries whether to remove the event from its series.
   * @return the updated calendar event.
   * @throws IllegalArgumentException if the update would create a multi-day series event.
   */
  private CalendarEvent createUpdatedEvent(CalendarEvent original, EditContext context,
                                           boolean removeFromSeries) {
    String seriesUid = removeFromSeries ? context.seriesUid : original.getSeriesUid();
    CalendarEvent updated =
        withUpdatedProperty(original, context.property, context.newValue, seriesUid);
    if (updated.isPartOfSeries() && isMultiDayEvent(updated)) {
      throw new IllegalArgumentException(
          "Cannot edit series event to span multiple days. "
              + "Event '" + updated.getSubject() + "' would start on "
              + updated.getStartDateTime().toLocalDate() + " at "
              + updated.getStartDateTime().toLocalTime()
              + " and end on " + updated.getEndDateTime().toLocalDate() + " at "
              + updated.getEndDateTime().toLocalTime() + ". "
              + "Series events must start and end on the same day.");
    }
    return updated;
  }

  /**
   * Creates a list of updated events from original events.
   * For time properties, the first event gets the full date-time change
   * while subsequent events only get the time-of-day component updated.
   *
   * @param events           list of original events.
   * @param context          the edit context.
   * @param removeFromSeries whether events should be removed from their series.
   * @return list of updated events.
   */
  private List<CalendarEvent> createUpdatedEventsList(List<CalendarEvent> events,
                                                      EditContext context,
                                                      boolean removeFromSeries) {
    List<CalendarEvent> updated = new ArrayList<>();

    for (int i = 0; i < events.size(); i++) {
      CalendarEvent event = events.get(i);
      if (isTimeProperty(context.property)) {
        if (i == 0) {
          updated.add(createUpdatedEvent(event, context, removeFromSeries));
        } else {
          updated.add(withUpdatedTime(event, context.property,
              context.newValue, context.seriesUid));
        }
      } else {
        updated.add(withUpdatedProperty(event, context.property,
            context.newValue, context.seriesUid));
      }
    }

    return updated;
  }

  /**
   * Applies edits to an entire series by replacing the series list.
   *
   * @param oldEvents list of original events.
   * @param newEvents list of updated events.
   * @param context   the edit context.
   */
  private void applySeriesEdit(List<CalendarEvent> oldEvents, List<CalendarEvent> newEvents,
                               EditContext context) {
    treeOperations(oldEvents, newEvents, isTimeProperty(context.property));
    String seriesUid = newEvents.get(0).getSeriesUid();
    SeriesLinkedList seriesList = new SeriesLinkedList(seriesUid);
    seriesList.addAll(newEvents);
    seriesByUid.replace(seriesUid, seriesList);
  }

  /**
   * Performs tree operations for editing events.
   * For time properties, removes and re-inserts to maintain tree structure.
   * For non-time properties, replaces in-place without restructuring.
   *
   * @param oldEvents      list of original events.
   * @param newEvents      list of updated events.
   * @param isTimeProperty whether the property being edited is time-related.
   */
  private void treeOperations(List<CalendarEvent> oldEvents, List<CalendarEvent> newEvents,
                              boolean isTimeProperty) {
    if (isTimeProperty) {
      calendarTree.removeAll(oldEvents);
      calendarTree.insertAll(newEvents);
    } else {
      replaceInTree(oldEvents, newEvents);
    }
  }

  /**
   * Applies edits to a portion of a series from a specific event onwards.
   *
   * @param oldEvents  list of original events.
   * @param newEvents  list of updated events.
   * @param seriesList the series linked list to update.
   * @param context    the edit context.
   */
  private void applySeriesFrom(List<CalendarEvent> oldEvents, List<CalendarEvent> newEvents,
                               SeriesLinkedList seriesList, EditContext context) {
    treeOperations(oldEvents, newEvents, isTimeProperty(context.property));
    seriesList.updateSeries(oldEvents.get(0), context.property, context.newValue);
  }

  /**
   * Applies a time property edit to a single event.
   * Handles removal from series if the start time is being changed.
   *
   * @param original         the original event.
   * @param newEvent         the updated event.
   * @param removeFromSeries whether to remove the event from its series.
   */
  private void applySingleTimeEdit(CalendarEvent original, CalendarEvent newEvent,
                                   boolean removeFromSeries) {
    calendarTree.remove(original);

    if (original.isPartOfSeries()) {
      SeriesLinkedList seriesList = seriesByUid.get(original.getSeriesUid());
      seriesList.removeSingleEvent(original.getSubject(), original.getStartDateTime());

      if (!removeFromSeries) {
        seriesList.addSingleEvent(newEvent);
      }

      if (seriesList.isEmpty()) {
        seriesByUid.remove(original.getSeriesUid());
      }
    }

    calendarTree.insertSingleEvent(newEvent);
  }

  /**
   * Applies a non-time property edit to a single event.
   * Replaces the event in-place without restructuring the tree.
   *
   * @param original the original event.
   * @param newEvent the updated event.
   */
  private void applySingleNonTimeEdit(CalendarEvent original, CalendarEvent newEvent) {
    calendarTree.replaceEventInPlace(original, newEvent);

    if (original.isPartOfSeries()) {
      SeriesLinkedList seriesList = seriesByUid.get(original.getSeriesUid());
      seriesList.replaceEvent(original, newEvent);
    }
  }

  /**
   * Validates that editing a single event won't create duplicates.
   *
   * @param original the original event being edited.
   * @param newEvent the updated event.
   * @throws IllegalArgumentException if a duplicate would be created.
   */
  private void validateSingleEdit(CalendarEvent original, CalendarEvent newEvent) {
    if (calendarTree.alreadyExists(newEvent)) {
      CalendarEvent existing = calendarTree.findEvent(newEvent);
      if (!existing.equals(original)) {
        throw new IllegalArgumentException(
            "Cannot edit: An event with subject '" + newEvent.getSubject()
                + "' already exists at " + newEvent.getStartDateTime().toLocalDateTime()
                + " to " + newEvent.getEndDateTime().toLocalTime());
      }
    }
  }

  /**
   * Validates that editing multiple events won't create duplicates.
   * Checks both within the updated events and against existing events in the tree.
   *
   * @param oldEvents list of original events being edited.
   * @param newEvents list of updated events.
   * @throws IllegalArgumentException if duplicates would be created.
   */
  private void validateMultipleEdit(List<CalendarEvent> oldEvents,
                                    List<CalendarEvent> newEvents) {
    Set<CalendarEvent> eventsToRemove = new HashSet<>(oldEvents);

    for (CalendarEvent newEvent : newEvents) {
      if (calendarTree.alreadyExists(newEvent)) {
        CalendarEvent existing = calendarTree.findEvent(newEvent);
        if (!eventsToRemove.contains(existing)) {
          throw new IllegalArgumentException(
              "Edit would create duplicate: '" + newEvent.getSubject()
                  + "' at " + newEvent.getStartDateTime());
        }
      }
    }
  }

  /**
   * Helper class to manage the copying of events to a target calendar.
   * Encapsulates event creation, series grouping, and validation logic.
   */
  private static class EventCopyHelper {
    /**
     * The target calendar where events will be inserted.
     */
    private final CalendarModelInterface target;

    /**
     * Map of series UIDs to their copied events for maintaining series relationships.
     */
    private final Map<String, List<CalendarEvent>> seriesGroups;

    /**
     * All copied events to be inserted into the target calendar.
     */
    private final List<CalendarEvent> allNewEvents;

    /**
     * Constructs a helper for copying events to the target calendar.
     *
     * @param target the calendar where events will be inserted
     */
    EventCopyHelper(CalendarModelInterface target) {
      this.target = target;
      this.seriesGroups = new HashMap<>();
      this.allNewEvents = new ArrayList<>();
    }

    /**
     * Adds a copied event with the specified time window.
     *
     * @param original    the original event to copy
     * @param targetStart the start time in the target calendar's timezone
     * @param targetEnd   the end time in the target calendar's timezone
     * @throws IllegalArgumentException if copying creates a multi-day series event
     */
    void addCopiedEvent(CalendarEvent original, ZonedDateTime targetStart,
                        ZonedDateTime targetEnd) {
      Map<String, Object> params = new HashMap<>(original.getHashMap());
      params.put("start", targetStart);
      params.put("end", targetEnd);

      String seriesUid = original.getSeriesUid();
      CalendarEvent newEvent = new SingleEvent(params, seriesUid);

      validateMultiDayConstraint(newEvent, original);

      allNewEvents.add(newEvent);
      if (seriesUid != null) {
        seriesGroups.computeIfAbsent(seriesUid, k -> new ArrayList<>()).add(newEvent);
      }
    }

    /**
     * Validates that series events don't span multiple days after copying.
     *
     * @param newEvent the copied event to validate
     * @param original the original event (for error messages)
     * @throws IllegalArgumentException if the new event is a multi-day series event
     */
    private void validateMultiDayConstraint(CalendarEvent newEvent, CalendarEvent original) {
      if (newEvent.getSeriesUid() != null && isMultiDayEvent(newEvent)) {
        throw new IllegalArgumentException(
            "Copying event '" + original.getSubject()
                + "' to timezone '" + target.getTimezone()
                + "' creates multi-day series event on "
                + newEvent.getEndDateTime().toLocalDate());
      }
    }

    /**
     * Inserts all collected events into the target calendar.
     *
     * @throws IllegalArgumentException if any copied event would create a duplicate
     */
    void execute() {
      ((CalendarModel) target).insertCopiedEvents(seriesGroups, allNewEvents);
    }
  }

}