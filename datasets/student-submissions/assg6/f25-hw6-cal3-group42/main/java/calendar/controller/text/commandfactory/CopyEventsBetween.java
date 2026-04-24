package calendar.controller.text.commandfactory;

import calendar.model.CalendarManagerInterface;
import calendar.view.CalendarViewInterface;
import java.util.Map;

/**
 * Class to copy events between two dates from active calendar to target calendar.
 */
public class CopyEventsBetween implements CommandInterface {
  private final Map<String, Object> parameters;
  private final CalendarViewInterface view;
  private final CalendarManagerInterface calendar;

  /**
   * Constructor to initialize the parameters, model from active calendar, and view when
   * this class is created.
   *
   * @param parameters map of all placeholder command names to its values
   * @param calendar   the active calendar
   * @param view       the calendar view
   * @throws IllegalArgumentException for missing parameters
   */
  public CopyEventsBetween(Map<String, Object> parameters, CalendarManagerInterface calendar,
                           CalendarViewInterface view) throws IllegalArgumentException {
    if (calendar == null || view == null) {
      throw new IllegalArgumentException("Calendar or View is non-existent in CopyEventsBetween");
    }
    this.parameters = parameters;
    this.calendar = calendar;
    this.view = view;
  }

  @Override
  public void execute() {
    try {
      calendar.copyEventsBetween(parameters);
      String displayMessage = "Copied events between " + parameters.get("startdate") + " and "
          + parameters.get("enddate") + " from " + calendar.getCalendarName() + " to "
          + parameters.get("target") + " on "
          + parameters.get("targetdate") + " of its' timezone.";
      view.display(displayMessage);
    } catch (Exception e) {
      view.displayError("CopyEventsBetween command: " + e.getMessage());
    }
  }
}
