package calendar.controller.text.commandfactory;

import calendar.model.CalendarManagerInterface;
import calendar.view.CalendarViewInterface;
import java.util.Map;

/**
 * Class to edit an existing calendar property and set to new value.
 */
public class EditCalendar implements CommandInterface {
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
  public EditCalendar(Map<String, Object> parameters,
                      CalendarManagerInterface calendar,
                      CalendarViewInterface view) throws IllegalArgumentException {
    if (calendar == null || view == null) {
      throw new IllegalArgumentException("Calendar or View is non-existent in EditCalendar");
    }
    this.parameters = parameters;
    this.calendar = calendar;
    this.view = view;
  }

  @Override
  public void execute() {
    try {
      calendar.editCalendar(parameters);
      String displayMessage = "Successfully edited calendar " + parameters.get("calname")
          + " and changed property " + parameters.get("property")
          + " to " + parameters.get("value") + ".";
      view.display(displayMessage);
    } catch (Exception e) {
      view.displayError("EditCalendar command: " + e.getMessage());
    }
  }
}