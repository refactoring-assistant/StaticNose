package calendar.view.dialog.debug;

import calendar.view.dialog.CalendarDialogResult;
import calendar.view.dialog.SwingCalendarDialog;
import java.util.Set;

/**
 * A debug implementation of the SwingCalendarDialog which returns a preset result.
 */
public class DebugSwingCalendarDialog extends SwingCalendarDialog {
  private final CalendarDialogResult calendarDialogResult;
  /**
   * Constructs a calendar dialog.
   *
   * @param existingCalendarNames set of existing calendar names for validation
   */

  public DebugSwingCalendarDialog(Set<String> existingCalendarNames,
                                  CalendarDialogResult calendarDialogResult) {
    super(existingCalendarNames);
    this.calendarDialogResult = calendarDialogResult;
  }

  @Override
  public CalendarDialogResult showDialog() {
    return calendarDialogResult;
  }
}
