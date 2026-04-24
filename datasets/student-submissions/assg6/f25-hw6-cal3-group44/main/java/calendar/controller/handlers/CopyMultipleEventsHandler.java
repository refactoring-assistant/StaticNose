package calendar.controller.handlers;

import static calendar.controller.commands.CopySingleEventCalendarCommand.getOutputString;

import calendar.controller.commanddata.CopyMultipleEventsCommandData;
import calendar.controller.filter.CalendarFilter;
import calendar.controller.filter.EventPredicates;
import calendar.model.Event;
import calendar.model.datatypes.TypeOfEvent;
import calendar.model.interfaces.AdvancedCalendar;
import calendar.model.interfaces.CalendarContainer;
import calendar.model.interfaces.EventReadOnly;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Handler class that performs the logic for copying multiple events.
 * Takes parsed command data and interacts with the model.
 */
public class CopyMultipleEventsHandler {

  private final CalendarContainer calendarManager;

  /**
   * Constructor for CopyMultipleEventsHandler.
   *
   * @param calendarManager the calendar container to interact with
   */
  public CopyMultipleEventsHandler(CalendarContainer calendarManager) {
    this.calendarManager = Objects.requireNonNull(calendarManager);
  }

  /**
   * Executes the copy multiple events logic based on the parsed command data.
   *
   * @param data the parsed command data
   * @return the result string
   */
  public String handle(CopyMultipleEventsCommandData data) {
    return copyMultipleEvents(data.getSourceStart(), data.getSourceEnd(), data.getTargetStart(),
        data.getTargetCalendarName());
  }

  private String copyMultipleEvents(LocalDate sourceStart, LocalDate sourceEnd,
                                    LocalDate targetStart, String targetCalName) {

    AdvancedCalendar sourceCal = calendarManager.getActiveCalendar();
    if (!calendarManager.getCalendars().containsKey(targetCalName)) {
      throw new IllegalArgumentException("Error: No calendar with name " + targetCalName);
    }
    AdvancedCalendar targetCal = calendarManager.getCalendars().get(targetCalName);

    ZoneId sourceZone = sourceCal.getZoneId();
    ZoneId targetZone = targetCal.getZoneId();

    List<EventReadOnly> eventsToCopy = new CalendarFilter(sourceCal)
        .filter(EventPredicates.betweenStartAndEnd(sourceStart.atStartOfDay(),
            sourceEnd.atTime(23, 59, 59)));

    List<EventReadOnly> copiedEvents = new ArrayList<>();
    Set<java.util.UUID> processedSeries = new HashSet<>();

    for (EventReadOnly event : eventsToCopy) {
      if (event.getEventType() == TypeOfEvent.SINGLE) {
        copiedEvents
            .add(copySingleEvent(event, sourceStart,
                targetStart, sourceZone, targetZone, targetCal));
      } else if (event.getEventType() == TypeOfEvent.SERIES) {
        java.util.UUID seriesId = event.getId();
        if (processedSeries.contains(seriesId)) {
          continue;
        }
        processedSeries.add(seriesId);

        List<EventReadOnly> seriesEvents = new CalendarFilter(sourceCal)
            .filter(EventPredicates.byEventId(seriesId)
                .and(EventPredicates.betweenStartAndEnd(
                    sourceStart.atStartOfDay(),
                    sourceEnd.atTime(23, 59, 59))));

        for (EventReadOnly seriesEvent : seriesEvents) {
          copiedEvents
              .add(copySingleEvent(seriesEvent, sourceStart,
                  targetStart, sourceZone, targetZone, targetCal));
        }
      }
    }

    return handleOutput(copiedEvents);
  }

  private EventReadOnly copySingleEvent(EventReadOnly event, LocalDate sourceStart,
                                        LocalDate targetStart, ZoneId sourceZone,
                                        ZoneId targetZone, AdvancedCalendar targetCal) {

    long daysOffset = ChronoUnit.DAYS.between(sourceStart, event.getStartDateTime().toLocalDate());
    LocalDate targetDate = targetStart.plusDays(daysOffset);

    if (event.getEventType() == TypeOfEvent.SERIES) {
      DayOfWeek originalDay = event.getStartDateTime().getDayOfWeek();
      if (targetDate.getDayOfWeek() != originalDay) {
        targetDate = targetDate.with(TemporalAdjusters.nextOrSame(originalDay));
      }
    }
    Duration duration = Duration.between(event.getStartDateTime(), event.getEndDateTime());
    LocalDateTime targetStartDateTime =
        convertToTargetDateTime(event.getStartDateTime(), targetDate, sourceZone, targetZone);
    LocalDateTime targetEndDateTime = targetStartDateTime.plus(duration);

    EventReadOnly targetEvent = new Event.EventBuilder(event)
        .setStartDateTime(targetStartDateTime)
        .setEndDateTime(targetEndDateTime)
        .build();

    return targetCal.addEvent(targetEvent);
  }

  private LocalDateTime convertToTargetDateTime(LocalDateTime sourceDateTime, LocalDate targetDate,
                                                ZoneId sourceZone, ZoneId targetZone) {
    LocalDateTime converted = sourceDateTime
        .atZone(sourceZone)
        .withZoneSameInstant(targetZone)
        .toLocalDateTime();

    return LocalDateTime.of(targetDate, converted.toLocalTime());
  }

  private String handleOutput(List<EventReadOnly> copiedEvents) {
    return getOutputString(copiedEvents);
  }
}

