package calendar.control.commands;

import calendar.control.results.CommandResult;
import calendar.model.AbstractEvent;
import calendar.model.EventInstance;
import calendar.model.Imodel;
import calendar.model.database.IcalendarDatabase;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Abstract class for copy command.
 */

public abstract class AbstractCopyCommand extends AbstractCommand {

  /**
   * Constructor for Abstract class for Copy Command.
   *
   * @param multipleCalendar the calendar database
   * @param input            the raw command input
   */
  protected AbstractCopyCommand(IcalendarDatabase multipleCalendar, String input) {
    super(multipleCalendar, input);
  }

  /**
   * Executes the command using the model and/or view provided at construction time.
   *
   * @return the result to be printed.
   */
  @Override
  public CommandResult execute() {
    try {
      if (!multipleCalendar.hasCalendars()) {
        return CommandResult.error("No calendars exist. Create calendars first.");
      }

      Optional<Imodel> sourceCalendar = multipleCalendar.getCurrent();
      if (sourceCalendar.isEmpty()) {
        return CommandResult.error("No source calendar is selected. "
            + "Use 'use calendar --name <name>' first.");
      }

      ParsedInput parsedInput = parseInput();
      if (!parsedInput.isValid()) {
        return CommandResult.error(parsedInput.getErrorMessage());
      }

      Optional<Imodel> targetCalendarOpt = multipleCalendar.get(parsedInput.targetCalendarName);
      if (targetCalendarOpt.isEmpty()) {
        return CommandResult.error(
            "Target calendar '" + parsedInput.targetCalendarName + "' does not exist.");
      }

      List<AbstractEvent> sourceEvents = getSourceEvents(sourceCalendar.get());

      if (sourceEvents.isEmpty()) {
        return CommandResult.error("No events found to copy.");
      }

      return copyToTarget(
          sourceEvents,
          targetCalendarOpt.get(),
          parsedInput.targetCalendarName
      );

    } catch (Exception e) {
      return CommandResult.error("Copy command failed: " + e.getMessage());
    }
  }

  /**
   * Parse the input command and extract necessary information.
   *
   * @return ParsedInput containing parsed data or error information
   */
  protected abstract ParsedInput parseInput();

  /**
   * Get the events to be copied from the source calendar.
   *
   * @param sourceCalendar the source calendar
   * @return list of events to copy
   */
  protected abstract List<AbstractEvent> getSourceEvents(Imodel sourceCalendar);

  /**
   * Copy the events to the target calendar.
   *
   * @param events             the events to copy
   * @param targetCalendar     the target calendar
   * @param targetCalendarName the name of the target calendar (for messages)
   */
  protected abstract CommandResult copyToTarget(List<AbstractEvent> events,
                                                Imodel targetCalendar,
                                                String targetCalendarName);

  /**
   * Get timezone for a calendar.
   */
  protected ZoneId getCalendarTimezone(String calendarName) {
    return multipleCalendar.getTimeZone(calendarName);
  }

  /**
   * Get current calendar timezone.
   */
  protected ZoneId getSourceTimezone() {
    Optional<String> currentCalName =
        Optional.ofNullable(multipleCalendar.getCurrentCalendarName());
    return currentCalName.map(multipleCalendar::getTimeZone).orElse(null);
  }

  /**
   * Convert event time from source to target timezone.
   */
  protected LocalDateTime convertEventTime(LocalDate targetDate, LocalTime eventTime,
                                           ZoneId sourceZone, ZoneId targetZone) {
    LocalDateTime eventDateTime = LocalDateTime.of(targetDate, eventTime);
    ZonedDateTime sourceZoned = eventDateTime.atZone(sourceZone);
    ZonedDateTime targetZoned = sourceZoned.withZoneSameInstant(targetZone);
    return targetZoned.toLocalDateTime();
  }

  /**
   * Find the first date starting from startDate that matches one of the weekdays.
   */
  protected LocalDate findFirstMatchingWeekday(LocalDate startDate, Set<DayOfWeek> weekdays) {
    LocalDate current = startDate;
    for (int i = 0; i < 7; i++) {
      if (weekdays.contains(current.getDayOfWeek())) {
        return current;
      }
      current = current.plusDays(1);
    }
    return startDate;
  }

  /**
   * Separate events into single events and series groups.
   */
  protected EventGroups separateEventsBySeries(List<AbstractEvent> events) {
    Map<String, List<EventInstance>> seriesMap = new HashMap<>();
    List<AbstractEvent> singleEvents = new java.util.ArrayList<>();

    for (AbstractEvent event : events) {
      if (event instanceof EventInstance) {
        EventInstance instance = (EventInstance) event;
        String seriesId = instance.getSeriesId();
        seriesMap.computeIfAbsent(seriesId, k -> new java.util.ArrayList<>()).add(instance);
      } else {
        singleEvents.add(event);
      }
    }

    return new EventGroups(singleEvents, seriesMap);
  }

  /**
   * Helper class to hold parsed input data.
   */
  protected static class ParsedInput {
    final String targetCalendarName;
    private final String errorMessage;

    private ParsedInput(String targetCalendarName, String errorMessage) {
      this.targetCalendarName = targetCalendarName;
      this.errorMessage = errorMessage;
    }

    static ParsedInput success(String targetCalendarName) {
      return new ParsedInput(targetCalendarName, null);
    }

    static ParsedInput error(String errorMessage) {
      return new ParsedInput(null, errorMessage);
    }

    boolean isValid() {
      return errorMessage == null;
    }

    String getErrorMessage() {
      return errorMessage != null ? errorMessage : "Unknown parsing error";
    }
  }

  /**
   * Helper class to hold separated events.
   */
  protected static class EventGroups {
    final List<AbstractEvent> singleEvents;
    final Map<String, List<EventInstance>> seriesMap;

    EventGroups(List<AbstractEvent> singleEvents, Map<String, List<EventInstance>> seriesMap) {
      this.singleEvents = singleEvents;
      this.seriesMap = seriesMap;
    }
  }
}