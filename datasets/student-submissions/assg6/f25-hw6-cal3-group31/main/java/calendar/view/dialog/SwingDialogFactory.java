package calendar.view.dialog;

import java.util.Set;

/**
 * Swing implementation of the dialog factory.
 * Creates Swing-based dialogs for the calendar application.
 * Does not depend on model classes to maintain MVC separation.
 */
public class SwingDialogFactory extends DialogFactory {

  @Override
  public IntDialog<CalendarDialogResult> createCalendarDialog(
      Set<String> existingCalendarNames) {
    return new SwingCalendarDialog(existingCalendarNames);
  }

  @Override
  public IntDialog<EventDialogResult> createEventDialog(
      int initialYear, int initialMonth, int initialDay) {
    return new SwingEventDialog(initialYear, initialMonth, initialDay);
  }

  @Override
  public IntDialog<EventDialogResult> createEditEventDialog(EventDialogResult eventData) {
    return new SwingEventDialog(eventData);
  }
}

