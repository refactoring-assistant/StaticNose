package calendar.controller.command;

import calendar.model.calendar.CalendarInterface;
import calendar.model.manager.CalendarManager;
import calendar.view.CalendarView;

/**
 * Command to edit events in a series from a specific datetime forward.
 *
 * <p>For temporal properties (start/end), this splits the series into two:
 * - Events before the datetime remain in the original series
 * - Events from the datetime forward form a new series with the changes
 *
 * <p>For non-temporal properties (subject, description, location, status),
 * all events from the datetime forward are updated in the same series.
 *
 * <p>Editable properties: subject, start, end, description, location, status
 */
public class EditEventsFromCommand implements Command {

  private final String property;
  private final String subject;
  private final String startDateTime;
  private final String newValue;

  /**
   * Creates a command to edit events in a series from a point forward.
   *
   * @param property the property to edit (subject, start, end, description, location, status)
   * @param subject the current subject of any event in the series
   * @param startDateTime the start datetime to begin editing from (format: YYYY-MM-DDThh:mm)
   * @param newValue the new value for the property
   */
  public EditEventsFromCommand(String property, String subject, String startDateTime,
                               String newValue) {
    this.property = property;
    this.subject = subject;
    this.startDateTime = startDateTime;
    this.newValue = newValue;
  }

  @Override
  public void execute(CalendarManager manager, CalendarView view) throws Exception {
    CalendarInterface calendar = manager.getCurrentCalendar();

    calendar.editEventsFrom(property, subject, startDateTime, newValue);

    view.displayMessage("Edited Events in Series from " + startDateTime + ": " + subject);
  }
}