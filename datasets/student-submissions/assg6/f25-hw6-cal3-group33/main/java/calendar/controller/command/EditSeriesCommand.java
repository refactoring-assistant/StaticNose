package calendar.controller.command;

import calendar.model.calendar.CalendarInterface;
import calendar.model.manager.CalendarManager;
import calendar.view.CalendarView;

/**
 * Command to edit all events in a series.
 *
 * <p>Updates all occurrences in the series with the new value.
 * All events remain in the same series and maintain their series relationship.
 *
 * <p>For temporal properties (start/end), only the TIME is changed across
 * all events, preserving each event's date.
 *
 * <p>Editable properties: subject, start, end, description, location, status
 */
public class EditSeriesCommand implements Command {

  private final String property;
  private final String subject;
  private final String startDateTime;
  private final String newValue;

  /**
   * Creates a command to edit all events in a series.
   *
   * @param property the property to edit (subject, start, end, description, location, status)
   * @param subject the current subject of any event in the series
   * @param startDateTime the start datetime of any event in the series (format: YYYY-MM-DDThh:mm)
   * @param newValue the new value for the property
   */
  public EditSeriesCommand(String property, String subject, String startDateTime,
                           String newValue) {
    this.property = property;
    this.subject = subject;
    this.startDateTime = startDateTime;
    this.newValue = newValue;
  }

  @Override
  public void execute(CalendarManager manager, CalendarView view) throws Exception {
    CalendarInterface calendar = manager.getCurrentCalendar();

    calendar.editSeries(property, subject, startDateTime, newValue);

    view.displayMessage("Edited Entire Event Series: " + subject);
  }
}