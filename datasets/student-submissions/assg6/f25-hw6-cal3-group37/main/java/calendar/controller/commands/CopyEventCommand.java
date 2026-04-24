package calendar.controller.commands;

import calendar.controller.CalendarContext;
import calendar.controller.Command;
import calendar.model.CalendarEvent;
import calendar.model.CalendarModel;
import calendar.util.EventCopyUtil;
import calendar.view.CalendarView;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Command to copy a single event to another calendar.
 */
public class CopyEventCommand implements Command {
  private final String eventSubject;
  private final LocalDateTime sourceStartTime;
  private final String targetCalendarName;
  private final LocalDateTime targetStartTime;

  /**
   * Constructs a CopyEventCommand.
   *
   * @param eventSubject the subject of event to copy
   * @param sourceStartTime the start time in source calendar
   * @param targetCalendarName the target calendar name
   * @param targetStartTime the new start time in target calendar
   */
  public CopyEventCommand(String eventSubject,
                          LocalDateTime sourceStartTime,
                          String targetCalendarName,
                          LocalDateTime targetStartTime) {
    this.eventSubject = eventSubject;
    this.sourceStartTime = sourceStartTime;
    this.targetCalendarName = targetCalendarName;
    this.targetStartTime = targetStartTime;
  }

  @Override
  public void execute(CalendarModel calendar, CalendarView view) {
    throw new UnsupportedOperationException(
        "Use executeOnSystem() for copy commands");
  }

  /**
   * Executes copy on the calendar system.
   *
   * @param context the calendar context
   * @param view the view
   */
  public void executeOnSystem(CalendarContext context, CalendarView view) {
    try {
      // Get source calendar (current)
      CalendarModel sourceCalendar = context.getCurrentCalendar();
      String sourceCalendarName = context.getCurrentCalendarName();

      // Get target calendar
      CalendarModel targetCalendar = context.getSystem().getCalendar(targetCalendarName);

      // Get timezones
      ZoneId sourceTimezone = context.getSystem().getCalendarTimezone(sourceCalendarName);
      ZoneId targetTimezone = context.getSystem().getCalendarTimezone(targetCalendarName);

      // Find the event to copy
      List<CalendarEvent> matches = sourceCalendar.getAllEvents().stream()
          .filter(e -> e.getSubject().equals(eventSubject)
              && e.getStartDateTime().equals(sourceStartTime))
          .collect(java.util.stream.Collectors.toList());

      if (matches.isEmpty()) {
        throw new IllegalArgumentException(
            "Event '" + eventSubject + "' not found at " + sourceStartTime);
      }
      if (matches.size() > 1) {
        throw new IllegalArgumentException(
            "Multiple events match criteria. Cannot copy.");
      }

      CalendarEvent sourceEvent = matches.get(0);

      // Copy the event
      EventCopyUtil.copyEvent(sourceEvent, targetCalendar, targetStartTime,
          sourceTimezone, targetTimezone);

      view.displayMessage("Event copied successfully to calendar '"
          + targetCalendarName + "'");

    } catch (Exception e) {
      view.displayError(e.getMessage());
    }
  }
}