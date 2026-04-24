package calendar.view.dialog;

import java.util.Set;

/**
 * Abstract factory for creating dialog components.
 * This allows for different dialog implementations to be created
 * without coupling the view to specific dialog types. *
 *
 * <p>This follows the Abstract Factory pattern, making it easy to:
 * <ul>
 *   <li>Add new dialog types without modifying existing code</li>
 *   <li>Swap dialog implementations (e.g., for testing or different UI styles)</li>
 *   <li>Maintain separation of concerns between view and dialog logic</li>
 * </ul>
 */
public abstract class DialogFactory {

  /**
   * Creates a dialog for creating a new calendar.
   *
   * @param existingCalendarNames set of existing calendar names for validation
   * @return a dialog for creating calendars
   */
  public abstract IntDialog<CalendarDialogResult> createCalendarDialog(
      Set<String> existingCalendarNames);

  /**
   * Creates a dialog for creating a new event.
   *
   * @param initialYear  the initially selected year
   * @param initialMonth the initially selected month
   * @param initialDay   the initially selected day
   * @return a dialog for creating events
   */
  public abstract IntDialog<EventDialogResult> createEventDialog(
      int initialYear, int initialMonth, int initialDay);

  /**
   * Creates a dialog for editing an existing event.
   *
   * @param eventData the existing event data to edit
   * @return a dialog for editing events
   */
  public abstract IntDialog<EventDialogResult> createEditEventDialog(EventDialogResult eventData);

  /**
   * Gets the default dialog factory implementation.
   *
   * @return the default dialog factory
   */
  public static DialogFactory getDefault() {
    return new SwingDialogFactory();
  }
}

