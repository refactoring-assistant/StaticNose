package calendar.view.dialog.debug;

import calendar.view.dialog.EventDialogResult;
import calendar.view.dialog.SwingEventDialog;

/**
 * A debug implementation of SwingEventDialog that returns a predefined result.
 */
public class DebugSwingEventDialog extends SwingEventDialog {
  private final EventDialogResult eventDialogResult;

  /**
   * Constructs a DebugSwingEventDialog with the specified initial date and result.
   *
   * @param initialYear        the initial year to display in the dialog
   * @param initialMonth       the initial month to display in the dialog
   * @param initialDay         the initial day to display in the dialog
   * @param eventDialogResult  the predefined result to return when showDialog is called
   */
  public DebugSwingEventDialog(int initialYear, int initialMonth, int initialDay,
                               EventDialogResult eventDialogResult) {
    super(initialYear, initialMonth, initialDay);
    this.eventDialogResult = eventDialogResult;
  }

  @Override
  public EventDialogResult showDialog() {
    return eventDialogResult;
  }
}
