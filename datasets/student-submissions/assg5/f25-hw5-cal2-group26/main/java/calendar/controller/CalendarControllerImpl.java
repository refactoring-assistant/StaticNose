package calendar.controller;

import calendar.model.CalendarInterface;
import calendar.model.CalendarManager;
import calendar.model.EditableField;
import calendar.model.EventInterface;
import calendar.model.RecurringEventInterface;
import calendar.util.ConflictException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CalendarController supporting multiple calendars via CalendarManager.
 * Tracks recurring series separately per calendar.
 * ALWAYS gets the active calendar from CalendarManager - does NOT track it independently.
 */
public class CalendarControllerImpl implements CalendarControllerInterface {

  private final CalendarManager manager;

  private final Map<String, Map<String, List<EventInterface>>> calendarSeriesMap = new HashMap<>();
  private final Map<String, Map<String, RecurringEventInterface>> calendarSeriesDefinitions =
      new HashMap<>();
  private final Map<String, Map<EventInterface, String>> calendarEventToSeriesMap = new HashMap<>();

  private int nextSeriesId = 0;

  /**
   * Creates a controller that works with the given CalendarManager.
   * The manager controls which calendar is active.
   */
  public CalendarControllerImpl(CalendarManager manager) {
    this.manager = manager;
  }

  /**
   * Gets the currently active calendar from the manager.
   * Ensures series tracking maps exist for this calendar.
   */
  private CalendarInterface getActiveCalendar() {
    try {
      CalendarInterface cal = manager.getActiveCalendar();
      ensureCalendarMaps(cal.getName());
      return cal;
    } catch (IllegalStateException e) {
      throw new RuntimeException("No active calendar set. Use 'use calendar' command first.", e);
    }
  }

  /**
   * Gets the name of the active calendar.
   */
  private String getActiveCalendarName() {
    return getActiveCalendar().getName();
  }

  /**
   * Ensures that the series tracking maps exist for a given calendar.
   */
  private void ensureCalendarMaps(String calendarName) {
    calendarSeriesMap.computeIfAbsent(calendarName, k -> new HashMap<>());
    calendarSeriesDefinitions.computeIfAbsent(calendarName, k -> new HashMap<>());
    calendarEventToSeriesMap.computeIfAbsent(calendarName, k -> new HashMap<>());
  }

  private String generateSeriesId() {
    return "series_" + (nextSeriesId++);
  }

  /**
   * Gets the series ID for a given event, or null if not part of a series.
   */
  public String getSeriesIdForEvent(EventInterface event) {
    String calName = getActiveCalendarName();
    return calendarEventToSeriesMap.get(calName).get(event);
  }

  /**
   * Gets the recurring event definition for a series ID.
   */
  public RecurringEventInterface getSeriesDefinition(String seriesId) {
    String calName = getActiveCalendarName();
    return calendarSeriesDefinitions.get(calName).get(seriesId);
  }

  private EventInterface findEvent(String subject, ZonedDateTime start, ZonedDateTime end) {
    for (EventInterface event : getActiveCalendar().getAllCalendarEvents()) {
      if (event.getSubject().equals(subject) && event.getStart().isEqual(start)) {
        if (end == null || event.getEnd().isEqual(end)) {
          return event;
        }
      }
    }
    return null;
  }

  private EditableField parseField(String property) {
    try {
      return EditableField.valueOf(property.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new RuntimeException("Invalid field: " + property);
    }
  }

  @Override
  public void createEvent(EventInterface event) {
    try {
      getActiveCalendar().addEvent(event);
    } catch (ConflictException e) {
      throw new RuntimeException("Event conflict: " + e.getMessage(), e);
    }
  }

  @Override
  public void createRecurringEvent(RecurringEventInterface recurringEvent) {
    try {
      String calName = getActiveCalendarName();
      String seriesId = generateSeriesId();
      List<EventInterface> instances = recurringEvent.getAllEvents();

      for (EventInterface event : instances) {
        getActiveCalendar().addEvent(event);
        calendarEventToSeriesMap.get(calName).put(event, seriesId);
      }

      calendarSeriesMap.get(calName).put(seriesId, new ArrayList<>(instances));
      calendarSeriesDefinitions.get(calName).put(seriesId, recurringEvent);
    } catch (ConflictException e) {
      throw new RuntimeException("Recurring event conflict: " + e.getMessage(), e);
    }
  }

  @Override
  public void editSingleEvent(String subject, ZonedDateTime start, ZonedDateTime end,
                              String property, Object newValue) {
    EditableField field = parseField(property);
    EventInterface target = findEvent(subject, start, end);

    if (target == null) {
      throw new RuntimeException("Event not found: " + subject + " at " + start);
    }

    try {
      if (field == EditableField.START) {
        handleSingleEventStartTimeChange(target, (ZonedDateTime) newValue);
      } else {
        getActiveCalendar().editEvent(target, field, newValue);
      }
    } catch (ConflictException e) {
      throw new RuntimeException("Edit would cause conflict: " + e.getMessage(), e);
    }
  }

  @Override
  public void editEventsFromHere(String subject, ZonedDateTime start,
                                 String property, Object newValue) {
    EditableField field = parseField(property);
    EventInterface targetEvent = findEvent(subject, start, null);

    if (targetEvent == null) {
      throw new RuntimeException("Event not found: " + subject + " at " + start);
    }

    String calName = getActiveCalendarName();
    String seriesId = calendarEventToSeriesMap.get(calName).get(targetEvent);

    if (seriesId == null) {
      editSingleEvent(subject, start, null, property, newValue);
      return;
    }

    List<EventInterface> allSeriesEvents =
        new ArrayList<>(calendarSeriesMap.get(calName).get(seriesId));
    List<EventInterface> eventsToEdit = new ArrayList<>();

    for (EventInterface event : allSeriesEvents) {
      if (!event.getStart().isBefore(targetEvent.getStart())) {
        eventsToEdit.add(event);
      }
    }
    eventsToEdit.sort(Comparator.comparing(EventInterface::getStart));

    if (eventsToEdit.isEmpty()) {
      throw new RuntimeException("No events found from " + start + " for " + subject);
    }

    try {
      if (field == EditableField.START) {
        handleSeriesSplitFromHere(seriesId, eventsToEdit, (ZonedDateTime) newValue, targetEvent);
      } else {
        for (EventInterface e : eventsToEdit) {
          getActiveCalendar().editEvent(e, field, newValue);
        }
      }
    } catch (ConflictException ex) {
      throw new RuntimeException("Edit would cause conflict: " + ex.getMessage(), ex);
    }
  }

  @Override
  public void editEntireSeries(String subject, ZonedDateTime start,
                               String property, Object newValue) {
    EditableField field = parseField(property);
    EventInterface targetEvent = findEvent(subject, start, null);
    if (targetEvent == null) {
      throw new RuntimeException("Event not found: " + subject + " at " + start);
    }
    String calName = getActiveCalendarName();
    String seriesId = calendarEventToSeriesMap.get(calName).get(targetEvent);
    if (seriesId == null) {
      editSingleEvent(subject, start, null, property, newValue);
      return;
    }
    List<EventInterface> seriesEvents =
        new ArrayList<>(calendarSeriesMap.get(calName).get(seriesId));
    if (seriesEvents.isEmpty()) {
      throw new RuntimeException("Series not found or has no events: " + subject);
    }
    try {
      if (field == EditableField.START) {
        handleEntireSeriesStartTimeChange(seriesId, seriesEvents, (ZonedDateTime) newValue,
            targetEvent);
      } else {
        for (EventInterface e : seriesEvents) {
          getActiveCalendar().editEvent(e, field, newValue);
        }
      }
    } catch (ConflictException ex) {
      throw new RuntimeException("Edit would cause conflict: " + ex.getMessage(), ex);
    }
  }

  @Override
  public List<EventInterface> queryEvents(java.util.function.Predicate<EventInterface> filter) {
    List<EventInterface> result = new ArrayList<>();
    for (EventInterface e : getActiveCalendar().getAllCalendarEvents()) {
      if (filter.test(e)) {
        result.add(e);
      }
    }
    return result;
  }

  @Override
  public boolean isUserBusy(ZonedDateTime dateTime) {
    return getActiveCalendar().isBusy(dateTime);
  }


  @Override
  public void deleteEvent(EventInterface event) {
    String calName = getActiveCalendarName();
    Map<EventInterface, String> eventToSeries = calendarEventToSeriesMap.get(calName);
    Map<String, List<EventInterface>> series = calendarSeriesMap.get(calName);

    String seriesId = eventToSeries.remove(event);
    if (seriesId != null && series.get(seriesId) != null) {
      series.get(seriesId).remove(event);
    }

    getActiveCalendar().removeEvent(event);
  }

  @Override
  public void deleteRecurringEvent(RecurringEventInterface recurringEvent) {
    String calName = getActiveCalendarName();
    Map<String, RecurringEventInterface> seriesDefs = calendarSeriesDefinitions.get(calName);
    Map<String, List<EventInterface>> seriesMapCal = calendarSeriesMap.get(calName);
    Map<EventInterface, String> eventToSeries = calendarEventToSeriesMap.get(calName);

    String seriesId = null;
    for (Map.Entry<String, RecurringEventInterface> entry : seriesDefs.entrySet()) {
      if (entry.getValue() == recurringEvent) {
        seriesId = entry.getKey();
        break;
      }
    }

    if (seriesId != null) {
      List<EventInterface> events = seriesMapCal.get(seriesId);
      if (events != null) {
        for (EventInterface e : new ArrayList<>(events)) {
          eventToSeries.remove(e);
          getActiveCalendar().removeEvent(e);
        }
      }
      seriesMapCal.remove(seriesId);
      seriesDefs.remove(seriesId);
    }
  }

  private void handleSingleEventStartTimeChange(EventInterface event, ZonedDateTime newStart)
      throws ConflictException {
    String calName = getActiveCalendarName();
    Map<EventInterface, String> eventToSeries = calendarEventToSeriesMap.get(calName);
    Map<String, List<EventInterface>> series = calendarSeriesMap.get(calName);

    String seriesId = eventToSeries.get(event);

    if (seriesId != null) {
      List<EventInterface> currentSeries = series.get(seriesId);
      if (currentSeries != null) {
        currentSeries.remove(event);
      }

      String newSeriesId = generateSeriesId();
      List<EventInterface> newSeriesList = new ArrayList<>();
      newSeriesList.add(event);
      series.put(newSeriesId, newSeriesList);
      eventToSeries.put(event, newSeriesId);
    }

    Duration eventDuration = Duration.between(event.getStart(), event.getEnd());
    getActiveCalendar().editEvent(event, EditableField.START, newStart);
    getActiveCalendar().editEvent(event, EditableField.END, newStart.plus(eventDuration));
  }

  private void handleSeriesSplitFromHere(String originalSeriesId,
                                         List<EventInterface> eventsToModify,
                                         ZonedDateTime newStartValue,
                                         EventInterface targetEvent) throws ConflictException {
    String calName = getActiveCalendarName();
    Map<String, List<EventInterface>> series = calendarSeriesMap.get(calName);
    Map<EventInterface, String> eventToSeries = calendarEventToSeriesMap.get(calName);

    List<EventInterface> originalSeries = series.get(originalSeriesId);
    if (originalSeries != null) {
      originalSeries.removeAll(eventsToModify);
    }

    String newSeriesId = generateSeriesId();
    series.put(newSeriesId, new ArrayList<>(eventsToModify));

    Duration shift = Duration.between(targetEvent.getStart(), newStartValue);

    for (EventInterface event : eventsToModify) {
      ZonedDateTime newStart = event.getStart().plus(shift);
      ZonedDateTime newEnd = newStart.plus(Duration.between(event.getStart(), event.getEnd()));

      getActiveCalendar().removeEvent(event);
      event.setStart(newStart);
      event.setEnd(newEnd);
      getActiveCalendar().addEvent(event);

      eventToSeries.put(event, newSeriesId);
    }
  }

  private void handleEntireSeriesStartTimeChange(String originalSeriesId,
                                                 List<EventInterface> eventsToModify,
                                                 ZonedDateTime newStartValue,
                                                 EventInterface targetEvent)
      throws ConflictException {
    String calName = getActiveCalendarName();
    Map<String, List<EventInterface>> series = calendarSeriesMap.get(calName);
    Map<EventInterface, String> eventToSeries = calendarEventToSeriesMap.get(calName);

    List<EventInterface> originalSeries = series.get(originalSeriesId);
    if (originalSeries != null) {
      originalSeries.clear();
    }

    String newSeriesId = generateSeriesId();
    series.put(newSeriesId, new ArrayList<>(eventsToModify));

    Duration shift = Duration.between(targetEvent.getStart(), newStartValue);

    for (EventInterface event : eventsToModify) {
      ZonedDateTime newStart = event.getStart().plus(shift);
      ZonedDateTime newEnd = newStart.plus(Duration.between(event.getStart(), event.getEnd()));

      getActiveCalendar().removeEvent(event);
      event.setStart(newStart);
      event.setEnd(newEnd);
      getActiveCalendar().addEvent(event);

      eventToSeries.put(event, newSeriesId);
    }
  }
}