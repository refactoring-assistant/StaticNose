package multicalendarmodel;

import calendarmodel.Event;
import calendarmodel.exceptions.DuplicateEventException;
import calendarmodel.exceptions.EventNotFoundException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the {@link MultiCalendarModel} interface.
 *
 * <p>This class manages a collection of named {@link ZonedCalendarModel}
 * instances, mapping unique string names to each calendar.</p>
 */
public class MultiCalendarModelImpl implements MultiCalendarModel {

  private final Map<String, ZonedCalendarModel> calendars;

  /**
   * Constructs a new, empty multi-calendar manager.
   */
  public MultiCalendarModelImpl() {
    this.calendars = new HashMap<>();
  }

  @Override
  public void createCalendar(String name, ZoneId zone) throws CalendarNameException {
    if (name == null || name.isBlank()) {
      throw new CalendarNameException("Calendar name cannot be blank.");
    }
    if (calendars.containsKey(name)) {
      throw new CalendarNameException("A calendar with this name already exists: " + name);
    }
    this.calendars.put(name, new ZonedCalendarModelImpl(zone));
  }

  @Override
  public void renameCalendar(String oldName, String newName) throws CalendarNameException {
    if (newName == null || newName.isBlank()) {
      throw new CalendarNameException("New calendar name cannot be blank.");
    }
    if (calendars.containsKey(newName)) {
      throw new CalendarNameException("A calendar with this name already exists: " + newName);
    }
    ZonedCalendarModel model = getCalendarOrThrow(oldName);
    calendars.remove(oldName);
    calendars.put(newName, model);
  }

  @Override
  public void changeCalendarZone(String calendarName, ZoneId newZone)
      throws CalendarNameException {
    getCalendarOrThrow(calendarName).setZone(newZone);
  }

  @Override
  public List<String> getAllCalendarNames() {
    return new ArrayList<>(calendars.keySet());
  }

  @Override
  public ZonedCalendarModel getCalendar(String calendarName) throws CalendarNameException {
    return getCalendarOrThrow(calendarName);
  }

  @Override
  public void copyEvent(String sourceCalendarName, String findSubject,
                        LocalDateTime findStartTime, LocalDateTime findEndTime,
                        String targetCalendarName, LocalDateTime newStartTime)
      throws CalendarNameException, EventNotFoundException, DuplicateEventException {

    ZonedCalendarModel sourceCal = getCalendarOrThrow(sourceCalendarName);
    ZonedCalendarModel targetCal = getCalendarOrThrow(targetCalendarName);

    List<Event> matches = sourceCal.getEventsFrom(findStartTime, findEndTime.plusNanos(1));
    Event eventToCopy = matches.stream()
        .filter(e -> e.getSubject().equals(findSubject)
            && e.getStartTime().equals(findStartTime)
            && e.getEndTime().equals(findEndTime))
        .findFirst()
        .orElseThrow(() -> new EventNotFoundException("Source event not found."));

    copySingleEventTo(eventToCopy, sourceCal, targetCal, newStartTime);
  }

  @Override
  public void copyEventInterval(String sourceCalendarName,
                                LocalDateTime sourceIntervalStart, LocalDateTime sourceIntervalEnd,
                                String targetCalendarName, LocalDateTime newIntervalStart)
      throws CalendarNameException, DuplicateEventException {

    ZonedCalendarModel sourceCal = getCalendarOrThrow(sourceCalendarName);
    ZonedCalendarModel targetCal = getCalendarOrThrow(targetCalendarName);

    List<Event> eventsToCopy = sourceCal.getEventsFrom(sourceIntervalStart, sourceIntervalEnd);
    if (eventsToCopy.isEmpty()) {
      return;
    }

    Event firstEvent = eventsToCopy.get(0);
    ZonedDateTime sourceBase = firstEvent.getStartTime().atZone(sourceCal.getZone());
    ZonedDateTime targetBase = newIntervalStart.atZone(targetCal.getZone());
    Duration timeDelta = Duration.between(sourceBase, targetBase);

    for (Event sourceEvent : eventsToCopy) {
      ZonedDateTime sourceEventStart = sourceEvent.getStartTime().atZone(sourceCal.getZone());
      ZonedDateTime newTargetStart = sourceEventStart.plus(timeDelta);

      try {
        copySingleEventTo(sourceEvent, sourceCal, targetCal,
            newTargetStart.withZoneSameInstant(targetCal.getZone()).toLocalDateTime());
      } catch (EventNotFoundException e) {
        throw new IllegalStateException("Internal error during copy", e);
      }
    }
  }

  /**
   * Helper method to copy one event to a target calendar at a new start time.
   *
   * <p>This method computes the wall-clock duration of the event and applies it
   * to the new start time in the target calendar. By working with LocalDateTime
   * values and letting createSingleEvent handle the UTC conversion, we ensure
   * that copies within the same calendar are handled consistently.</p>
   *
   * @param eventToCopy  The original event object (times in sourceCal's zone).
   * @param sourceCal    The source calendar (to get original zone).
   * @param targetCal    The target calendar (to get target zone).
   * @param newStartTime The new start time (in targetCal's zone).
   * @throws DuplicateEventException if a conflict occurs in the target.
   * @throws EventNotFoundException  (should not happen)
   */
  private void copySingleEventTo(Event eventToCopy, ZonedCalendarModel sourceCal,
                                 ZonedCalendarModel targetCal, LocalDateTime newStartTime)
      throws DuplicateEventException, EventNotFoundException {

    Duration eventDuration = Duration.between(
        eventToCopy.getStartTime(), eventToCopy.getEndTime());

    LocalDateTime newEndTime = newStartTime.plus(eventDuration);

    Event newEvent = Event.newBuilder(eventToCopy)
        .withStartTime(newStartTime)
        .withEndTime(newEndTime)
        .build();

    targetCal.createSingleEvent(newEvent);
  }

  /**
   * Internal helper to get a calendar or throw a clear exception.
   *
   * @param name The name of the calendar to find.
   * @return The found {@link ZonedCalendarModel}.
   * @throws CalendarNameException if the name is blank or not found.
   */
  private ZonedCalendarModel getCalendarOrThrow(String name) throws CalendarNameException {
    if (name == null || name.isBlank()) {
      throw new CalendarNameException("Calendar name cannot be blank.");
    }
    ZonedCalendarModel model = calendars.get(name);
    if (model == null) {
      throw new CalendarNameException("No calendar found with name: " + name);
    }
    return model;
  }
}