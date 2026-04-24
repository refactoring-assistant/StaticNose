package calendar.controller.text.commandfactory;

import calendar.model.CalendarManagerInterface;
import calendar.view.CalendarViewInterface;
import java.util.Map;

/**
 * Class to set an existing calendar as active to run all event commands on.
 */
public class UseCalendar implements CommandInterface {
  private final Map<String, Object> parameters;
  private final CalendarViewInterface view;
  private final CalendarManagerInterface calendar;

  /**
   * Constructor to initialize the parameters and view when
   * this class is created.
   *
   * @param parameters map of all placeholder command names to its values
   * @param calendar   the active calendar
   * @param view       the calendar view
   * @throws IllegalArgumentException for missing parameters
   */
  public UseCalendar(Map<String, Object> parameters,
                     CalendarManagerInterface calendar,
                     CalendarViewInterface view) throws IllegalArgumentException {
    if (calendar == null || view == null) {
      throw new IllegalArgumentException("Calendar or View is non-existent in UseCalendar");
    }
    this.parameters = parameters;
    this.calendar = calendar;
    this.view = view;
  }

  @Override
  public void execute() {
    try {
      calendar.useCalendar(parameters);
      String displayMessage = "Set active calendar to " + parameters.get("calname");
      view.display(displayMessage);
    } catch (Exception e) {
      view.displayError("UseCalendar command: " + e.getMessage());
    }
  }
}
