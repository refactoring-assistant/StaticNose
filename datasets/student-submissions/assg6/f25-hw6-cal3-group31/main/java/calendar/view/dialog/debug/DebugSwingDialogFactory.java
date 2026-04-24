package calendar.view.dialog.debug;

import calendar.view.dialog.CalendarDialogResult;
import calendar.view.dialog.EventDialogResult;
import calendar.view.dialog.IntDialog;
import calendar.view.dialog.SwingDialogFactory;
import java.util.Set;

/**
 * Debug implementation of SwingDialogFactory for testing.
 * Allows injection of mock dialog results for automated testing.
 */
public class DebugSwingDialogFactory extends SwingDialogFactory {
  private final CalendarDialogResult calendarDialogResult;
  private final EventDialogResult eventDialogResult;

  /**
   * Constructs a debug dialog factory with specific mock results.
   *
   * @param calendarDialogResult the mock calendar dialog result
   * @param eventDialogResult    the mock event dialog result
   */
  public DebugSwingDialogFactory(CalendarDialogResult calendarDialogResult,
                                 EventDialogResult eventDialogResult) {
    this.calendarDialogResult = calendarDialogResult;
    this.eventDialogResult = eventDialogResult;
  }

  @Override
  public IntDialog<CalendarDialogResult> createCalendarDialog(
      Set<String> existingCalendarNames) {
    return new DebugSwingCalendarDialog(existingCalendarNames, calendarDialogResult);
  }

  @Override
  public IntDialog<EventDialogResult> createEventDialog(
      int initialYear, int initialMonth, int initialDay) {
    return new DebugSwingEventDialog(initialYear, initialMonth, initialDay, eventDialogResult);
  }

  @Override
  public IntDialog<EventDialogResult> createEditEventDialog(EventDialogResult eventData) {
    return new DebugSwingEventDialog(eventData.getStartYear(), eventData.getStartMonth(),
        eventData.getStartDay(), eventDialogResult);
  }
}
