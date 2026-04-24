package calendar.controller.handlers;

import static calendar.controller.commands.CopySingleEventCalendarCommand.getOutputString;

import calendar.controller.commanddata.CopySingleEventCommandData;
import calendar.model.Event;
import calendar.model.interfaces.AdvancedCalendar;
import calendar.model.interfaces.CalendarContainer;
import calendar.model.interfaces.EventReadOnly;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Handler class that performs the logic for copying single events.
 * Takes parsed command data and interacts with the model.
 */
public class CopySingleEventHandler {

  private final CalendarContainer calendarManager;

  /**
   * Constructor for CopySingleEventHandler.
   *
   * @param calendarManager the calendar container to interact with
   */
  public CopySingleEventHandler(CalendarContainer calendarManager) {
    this.calendarManager = Objects.requireNonNull(calendarManager);
  }

  /**
   * Executes the copy single event logic based on the parsed command data.
   *
   * @param data the parsed command data
   * @return the result string
   */
  public String handle(CopySingleEventCommandData data) {
    AdvancedCalendar sourceCalendar = calendarManager.getActiveCalendar();
    AdvancedCalendar targetCalendar = getTargetCalendar(data.getTargetCalendarName());

    List<EventReadOnly> eventsToCopy = findEvents(sourceCalendar, data.getEventName(),
        data.getSourceStart());

    if (eventsToCopy.isEmpty()) {
      return "No matching event found to copy in: " + sourceCalendar.getName();
    }

    return copyEventsToTarget(eventsToCopy, targetCalendar, data.getTargetStart());
  }

  private AdvancedCalendar getTargetCalendar(String name) {
    AdvancedCalendar cal = calendarManager.getCalendars().get(name);
    if (cal == null) {
      throw new IllegalArgumentException("Error: Target calendar does not exist: " + name);
    }
    return cal;
  }

  private List<EventReadOnly> findEvents(AdvancedCalendar calendar,
                                         String name, LocalDateTime time) {
    List<EventReadOnly> eventsToCopy = new ArrayList<>();
    List<EventReadOnly> list = calendar
        .getAllEvents()
        .getOrDefault(time.toLocalDate(), new ArrayList<>());
    if (list.isEmpty()) {
      return eventsToCopy;
    }
    for (EventReadOnly event : list) {
      if (event.getSubject().equalsIgnoreCase(name)) {
        eventsToCopy.add(event);
      }
    }
    return eventsToCopy;
  }

  private String copyEventsToTarget(List<EventReadOnly> events,
                                    AdvancedCalendar targetCal, LocalDateTime targetStart) {

    List<EventReadOnly> copiedEvents = new ArrayList<>();
    for (EventReadOnly e : events) {
      LocalDateTime targetStartDateTime = targetStart;
      Duration duration = Duration.between(e.getStartDateTime(), e.getEndDateTime());
      LocalDateTime targetEndDateTime = targetStartDateTime.plus(duration);

      EventReadOnly toCopy = new Event.EventBuilder(e)
          .setStartDateTime(targetStartDateTime).setEndDateTime(targetEndDateTime).build();

      copiedEvents.add(toCopy);
    }
    return handleOutput(copiedEvents);
  }

  private String handleOutput(List<EventReadOnly> copiedEvents) {
    return getOutputString(copiedEvents);
  }
}

