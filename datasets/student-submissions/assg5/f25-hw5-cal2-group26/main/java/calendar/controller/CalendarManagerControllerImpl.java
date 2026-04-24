package calendar.controller;

import calendar.model.CalendarInterface;
import calendar.model.CalendarManager;
import calendar.model.Event;
import calendar.model.EventInterface;
import calendar.model.RecurringEvent;
import calendar.model.RecurringEventInterface;
import calendar.model.Weekday;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Implementation of CalendarManagerControllerInterface.
 * Handles multi-calendar management, timezone validation, event copying, and
 * partial recurring series copying with skipped/modified instance preservation.
 */
public class CalendarManagerControllerImpl implements CalendarManagerControllerInterface {

  private final CalendarManager manager;
  private final CalendarControllerImpl eventController;

  /**
   * Constructs a controller that manages multiple calendars using the provided.
   *
   * @param manager the calendar manager; must not be null.
   * @param eventController the controller for event operations; must not be null.
   */
  public CalendarManagerControllerImpl(CalendarManager manager,
                                       CalendarControllerImpl eventController) {
    this.manager = Objects.requireNonNull(manager, "manager");
    this.eventController = Objects.requireNonNull(eventController, "eventController");
  }

  @Override
  public CalendarInterface getActiveCalendar() {
    try {
      return manager.getActiveCalendar();
    } catch (IllegalStateException e) {
      throw new RuntimeException("No active calendar set", e);
    }
  }

  @Override
  public void createCalendar(String name, ZoneId zone) {
    try {
      manager.createCalendar(name, zone);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Failed to create calendar: " + e.getMessage(), e);
    }
  }

  @Override
  public void deleteCalendar(String name) {
    try {
      manager.deleteCalendar(name);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Failed to delete calendar: " + e.getMessage(), e);
    }
  }

  @Override
  public void switchCalendar(String name) {
    try {
      manager.useCalendar(name);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Failed to switch calendar: " + e.getMessage(), e);
    }
  }

  @Override
  public void editCalendarName(String currentName, String newName) {
    try {
      manager.editCalendarName(currentName, newName);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Failed to edit calendar name: " + e.getMessage(), e);
    }
  }

  @Override
  public void changeCalendarTimezone(String calendarName, ZoneId newZone) {
    try {
      validateTimezoneChange(calendarName, newZone);
      manager.changeCalendarTimezone(calendarName, newZone);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Failed to change calendar timezone: " + e.getMessage(),
          e);
    }
  }

  @Override
  public Collection<String> listCalendars() {
    Collection<CalendarInterface> cals = manager.getAllCalendars();
    List<String> names = new ArrayList<>();
    for (CalendarInterface cal : cals) {
      names.add(cal.getName());
    }
    return names;
  }

  @Override
  public String getActiveCalendarName() {
    return getActiveCalendar().getName();
  }

  @Override
  public ZoneId getActiveCalendarZone() {
    return getActiveCalendar().getCalendarZone();
  }

  private void validateTimezoneChange(String calendarName, ZoneId newZone) {
    CalendarInterface cal = manager.getCalendar(calendarName);
    for (EventInterface e : cal.getAllCalendarEvents()) {
      if (!e.isAllDay()) {
        ZonedDateTime start = e.getStart().withZoneSameInstant(newZone);
        ZonedDateTime end = e.getEnd() != null ? e.getEnd().withZoneSameInstant(newZone) : start;
        if (!start.toLocalDate().equals(end.toLocalDate())) {
          throw new IllegalArgumentException(
              "Timezone change would make event '" + e.getSubject()
                  + "' span multiple days. Operation rejected.");
        }
      }
    }
  }

  @Override
  public void copyEvent(String eventName, ZonedDateTime sourceStart,
                        String targetCalendarName, ZonedDateTime targetStart) {

    CalendarInterface sourceCal = manager.getActiveCalendar();
    CalendarInterface targetCal = manager.getCalendar(targetCalendarName);

    EventInterface sourceEvent = sourceCal.getEvent(
        eventName,
        sourceStart.withZoneSameInstant(sourceCal.getCalendarZone()),
        null
    );

    if (sourceEvent == null) {
      throw new IllegalArgumentException("Event not found: " + eventName + " at " + sourceStart);
    }

    Duration duration = (sourceEvent.getEnd() != null)
        ? Duration.between(sourceEvent.getStart(), sourceEvent.getEnd())
        : Duration.ZERO;

    ZonedDateTime newStart = targetStart.withZoneSameInstant(targetCal.getCalendarZone());
    ZonedDateTime newEnd =
        (duration.isZero() && sourceEvent.getEnd() == null) ? null : newStart.plus(duration);

    EventInterface copy = new Event(
        sourceEvent.getSubject(),
        newStart,
        newEnd,
        sourceEvent.getDescription(),
        sourceEvent.getLocation(),
        sourceEvent.getStatus(),
        sourceEvent.isAllDay()
    );

    enforceSingleDay(copy);

    withActiveCalendar(targetCal.getName(), () -> eventController.createEvent(copy));
  }

  @Override
  public void copyEventsOn(ZonedDateTime sourceDate, String targetCalendarName,
                           ZonedDateTime targetDate) {

    CalendarInterface sourceCal = manager.getActiveCalendar();
    CalendarInterface targetCal = manager.getCalendar(targetCalendarName);

    ZonedDateTime sourceNorm = sourceDate.withZoneSameInstant(sourceCal.getCalendarZone());
    ZonedDateTime targetNormAsSourceZone =
        targetDate.withZoneSameInstant(sourceCal.getCalendarZone());

    List<EventInterface> events = sourceCal.getEventsOn(sourceNorm);
    if (events.isEmpty()) {
      return;
    }

    long dayOffset = java.time.temporal.ChronoUnit.DAYS.between(
        sourceNorm.toLocalDate(), targetNormAsSourceZone.toLocalDate()
    );

    withActiveCalendar(targetCal.getName(), () -> {
      for (EventInterface e : events) {
        ZonedDateTime shiftedStart = e.getStart().plusDays(dayOffset)
            .withZoneSameInstant(targetCal.getCalendarZone());
        ZonedDateTime shiftedEnd = (e.getEnd() != null) ? e.getEnd().plusDays(dayOffset)
            .withZoneSameInstant(targetCal.getCalendarZone()) : null;

        EventInterface copy = new Event(
            e.getSubject(),
            shiftedStart,
            shiftedEnd,
            e.getDescription(),
            e.getLocation(),
            e.getStatus(),
            e.isAllDay()
        );

        enforceSingleDay(copy);
        eventController.createEvent(copy);
      }
    });
  }

  @Override
  public void copyEventsBetween(ZonedDateTime sourceStart, ZonedDateTime sourceEnd,
                                String targetCalendarName, ZonedDateTime targetStart) {

    CalendarInterface sourceCal = manager.getActiveCalendar();
    CalendarInterface targetCal = manager.getCalendar(targetCalendarName);

    ZonedDateTime normalizedStart = sourceStart.withZoneSameInstant(sourceCal.getCalendarZone());
    ZonedDateTime normalizedEnd = sourceEnd.withZoneSameInstant(sourceCal.getCalendarZone());

    List<EventInterface> events = sourceCal.getEventsBetween(normalizedStart, normalizedEnd);
    if (events.isEmpty()) {
      throw new IllegalArgumentException(
          "No events found in the specified range in calendar '" + sourceCal.getName() + "'"
      );
    }

    Map<String, List<EventInterface>> grouped = groupEventsBySeries(events);

    Duration shift = Duration.between(
        sourceStart.withZoneSameInstant(targetCal.getCalendarZone()),
        targetStart.withZoneSameInstant(targetCal.getCalendarZone())
    );

    List<EventInterface> eventsToCreate = new ArrayList<>();
    List<RecurringEventInterface> seriesToCreate = new ArrayList<>();

    List<EventInterface> standalone = grouped.get(null);
    if (standalone != null) {
      for (EventInterface e : standalone) {
        eventsToCreate.add(createCopiedEvent(e, shift, targetCal));
      }
    }

    for (Map.Entry<String, List<EventInterface>> entry : grouped.entrySet()) {
      String seriesId = entry.getKey();
      if (seriesId == null) {
        continue;
      }

      List<EventInterface> seriesEvents = entry.getValue();
      RecurringEventInterface copiedSeries =
          createCopiedSeries(seriesEvents, shift, targetCal, seriesId);
      if (copiedSeries != null) {
        seriesToCreate.add(copiedSeries);
      } else {
        for (EventInterface e : seriesEvents) {
          eventsToCreate.add(createCopiedEvent(e, shift, targetCal));
        }
      }
    }

    withActiveCalendar(targetCal.getName(), () -> {
      for (EventInterface event : eventsToCreate) {
        eventController.createEvent(event);
      }
      for (RecurringEventInterface series : seriesToCreate) {
        eventController.createRecurringEvent(series);
      }
    });
  }

  private Map<String, List<EventInterface>> groupEventsBySeries(List<EventInterface> events) {
    Map<String, List<EventInterface>> grouped = new HashMap<>();
    for (EventInterface event : events) {
      String seriesId = eventController.getSeriesIdForEvent(event);
      grouped.computeIfAbsent(seriesId, k -> new ArrayList<>()).add(event);
    }
    return grouped;
  }

  private EventInterface createCopiedEvent(EventInterface source, Duration shift,
                                           CalendarInterface targetCal) {
    ZonedDateTime newStart =
        source.getStart().plus(shift).withZoneSameInstant(targetCal.getCalendarZone());
    ZonedDateTime newEnd = (source.getEnd() != null)
        ? source.getEnd().plus(shift).withZoneSameInstant(targetCal.getCalendarZone()) : null;

    EventInterface copy = new Event(
        source.getSubject(),
        newStart,
        newEnd,
        source.getDescription(),
        source.getLocation(),
        source.getStatus(),
        source.isAllDay()
    );

    enforceSingleDay(copy);
    return copy;
  }

  private RecurringEventInterface createCopiedSeries(List<EventInterface> seriesEvents,
                                                     Duration shift,
                                                     CalendarInterface targetCal,
                                                     String sourceSeriesId) {
    if (seriesEvents == null || seriesEvents.isEmpty()) {
      return null;
    }

    RecurringEventInterface original = eventController.getSeriesDefinition(sourceSeriesId);
    if (original == null) {
      return null;
    }

    seriesEvents.sort(Comparator.comparing(EventInterface::getStart));
    EventInterface first = seriesEvents.get(0);

    ZonedDateTime newStart =
        first.getStart().plus(shift).withZoneSameInstant(targetCal.getCalendarZone());
    Duration dur = (first.getEnd() != null) ? Duration.between(first.getStart(), first.getEnd()) :
        Duration.ZERO;
    ZonedDateTime newEnd = (dur.isZero() && first.getEnd() == null) ? null : newStart.plus(dur);

    EventInterface base = new Event(
        first.getSubject(),
        newStart,
        newEnd,
        first.getDescription(),
        first.getLocation(),
        first.getStatus(),
        first.isAllDay()
    );

    int copiedCount = seriesEvents.size();
    List<Weekday> adjustedWeekdays =
        adjustWeekdaysForTargetZone(original.getWeekdays(), first.getStart(),
            targetCal.getCalendarZone());

    RecurringEvent truncated = new RecurringEvent(base, adjustedWeekdays, copiedCount, null);

    try {
      truncated.addSkippedInstances(original.getSkippedInstances());
      truncated.addModifiedInstances(original.getModifiedInstances());
    } catch (Exception ignored) {
      //ignore
    }

    return truncated;
  }

  private List<Weekday> adjustWeekdaysForTargetZone(List<Weekday> sourceWeekdays,
                                                    ZonedDateTime seriesStart,
                                                    ZoneId targetZone) {
    if (sourceWeekdays == null || sourceWeekdays.isEmpty()) {
      return Collections.emptyList();
    }

    LinkedHashSet<Weekday> out = new LinkedHashSet<>();
    for (Weekday wd : sourceWeekdays) {
      ZonedDateTime rep = seriesStart;
      for (int i = 0; i < 7; i++) {
        if (rep.getDayOfWeek() == wd.toDayOfWeek()) {
          ZonedDateTime repInTarget = rep.withZoneSameInstant(targetZone);
          out.add(weekdayFromDayOfWeek(repInTarget.getDayOfWeek()));
          break;
        }
        rep = rep.plusDays(1);
      }
    }
    return new ArrayList<>(out);
  }

  private Weekday weekdayFromDayOfWeek(DayOfWeek day) {
    switch (day) {
      case MONDAY:
        return Weekday.MONDAY;
      case TUESDAY:
        return Weekday.TUESDAY;
      case WEDNESDAY:
        return Weekday.WEDNESDAY;
      case THURSDAY:
        return Weekday.THURSDAY;
      case FRIDAY:
        return Weekday.FRIDAY;
      case SATURDAY:
        return Weekday.SATURDAY;
      case SUNDAY:
        return Weekday.SUNDAY;
      default:
        throw new IllegalArgumentException("Invalid day: " + day);
    }
  }

  private void enforceSingleDay(EventInterface e) {
    if (!e.isAllDay() && e.getEnd() != null) {
      ZonedDateTime start = e.getStart();
      ZonedDateTime end = e.getEnd();
      if (!start.toLocalDate().equals(end.toLocalDate())) {
        throw new RuntimeException("Event '" + e.getSubject() + "' spans multiple days.");
      }
    }
  }

  private <T> T withActiveCalendar(String calendarName, Supplier<T> action) {
    String originalActive = manager.getActiveCalendar().getName();
    try {
      manager.useCalendar(calendarName);
      return action.get();
    } finally {
      manager.useCalendar(originalActive);
    }
  }

  private void withActiveCalendar(String calendarName, Runnable action) {
    String originalActive = manager.getActiveCalendar().getName();
    try {
      manager.useCalendar(calendarName);
      action.run();
    } finally {
      manager.useCalendar(originalActive);
    }
  }
}
