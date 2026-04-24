package calendar.controller.text.commandfactory;

import calendar.model.CalendarManagerInterface;
import calendar.view.CalendarViewInterface;
import java.util.Map;

/**
 * Class to copy events on a specific date from active calendar to target calendar.
 */
public class CopyEventsOn implements CommandInterface {
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
  public CopyEventsOn(Map<String, Object> parameters, CalendarManagerInterface calendar,
                      CalendarViewInterface view) throws IllegalArgumentException {
    if (calendar == null || view == null) {
      throw new IllegalArgumentException("Calendar or View is non-existent in CopyEventsOn");
    }
    this.parameters = parameters;
    this.calendar = calendar;
    this.view = view;
  }

  @Override
  public void execute() {
    try {
      calendar.copyEventsOn(parameters);
      String displayMessage = "Copied events on " + parameters.get("sourcedate") + " from "
          + calendar.getCalendarName() + " to " + parameters.get("target") + " on "
          + parameters.get("targetdate") + " of its' timezone.";
      view.display(displayMessage);
    } catch (Exception e) {
      view.displayError("CopyEventsOn command: " + e.getMessage());
    }
  }
}
