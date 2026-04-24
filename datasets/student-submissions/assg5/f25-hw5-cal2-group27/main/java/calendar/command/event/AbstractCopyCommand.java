package calendar.command.event;

import calendar.model.CalendarManager;
import calendar.model.Event;
import calendar.model.MyCalendar;
import calendar.util.EventUtil;
import calendar.view.CalendarTextView;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Base class for copy commands.
 */
public abstract class AbstractCopyCommand extends AbstractEventCommand {

  /** The target calendar's name to copy the events into. */
  private final String targetCalendarName;

  /**
   * Constructs a new copy command.
   *
   * @param targetCalendarName the name of the calendar to copy into
   */
  protected AbstractCopyCommand(String targetCalendarName) {
    this.targetCalendarName = targetCalendarName;
  }

  /**
   * Executes the copy logic using the provided manager, source calendar and view.
   *
   * @param manager         calendar manager used to resolve the target calendar
   * @param sourceCalendar  the currently active calendar
   * @param view            the view used to output messages to the user
   */
  @Override
  protected void executeWithCalendar(
          CalendarManager manager,
          MyCalendar sourceCalendar,
          CalendarTextView view) {

    Optional<MyCalendar> maybeTarget = manager.getCalendar(targetCalendarName);
    if (maybeTarget.isEmpty()) {
      view.printMessage(
              "Error: Target calendar '" + targetCalendarName + "' not found.");
      return;
    }
    MyCalendar targetCalendar = maybeTarget.get();

    List<Event> eventsToCopy = getEventsToCopy(sourceCalendar);
    if (eventsToCopy.isEmpty()) {
      view.printMessage("No events found in the source calendar to copy.");
      return;
    }

    int copiedCount = 0;
    int conflictCount = 0;
    Map<UUID, UUID> seriesIdMapping = new HashMap<>();

    for (Event eventToCopy : eventsToCopy) {
      ZonedDateTime newStart = calculateNewStart(eventToCopy, targetCalendar);

      UUID newSeriesId = null;
      if (eventToCopy.getSeriesId() != null && eventToCopy.getRecurrence() != null) {
        newSeriesId = seriesIdMapping.computeIfAbsent(
                eventToCopy.getSeriesId(),
                ignored -> UUID.randomUUID());
      }

      Event newEvent = EventUtil.buildCopiedEvent(eventToCopy, newStart, newSeriesId);
      try {
        targetCalendar.addEvent(newEvent);
        copiedCount++;
      } catch (IllegalArgumentException e) {
        conflictCount++;
      }
    }

    reportSummary(view, copiedCount, conflictCount);
  }

  /**
   * Selects the source events to be copied.
   *
   * @param sourceCalendar the calendar to read from
   * @return a list of events to copy
   */
  protected abstract List<Event> getEventsToCopy(MyCalendar sourceCalendar);

  /**
   * Computes the start time for the copied event in the target calendar.
   *
   * @param eventToCopy    the source event instance
   * @param targetCalendar the calendar receiving the copy
   * @return the new start time for the copied event
   */
  protected abstract ZonedDateTime calculateNewStart(
          Event eventToCopy, MyCalendar targetCalendar);

  /**
   * Prints a summary after attempting to copy events.
   *
   * @param view       the output view
   * @param copied     the number of successfully copied events
   * @param conflicts  the number of conflicts encountered
   */
  protected void reportSummary(CalendarTextView view, int copied, int conflicts) {
    view.printMessage(
            "Copied " + copied + " events successfully. "
                    + conflicts + " events had conflicts.");
  }
}