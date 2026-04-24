package calendar.controller.text.commandfactory;

import calendar.model.CalendarManagerInterface;
import calendar.model.CalendarModelInterface;
import calendar.view.CalendarViewInterface;
import java.util.Map;

/**
 * Class to create an all day event.
 */
public class CreateAllDayEvent implements CommandInterface {
  private final Map<String, Object> parameters;
  private final CalendarModelInterface model;
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
  public CreateAllDayEvent(Map<String, Object> parameters, CalendarManagerInterface calendar,
                           CalendarViewInterface view) throws IllegalArgumentException {
    if (calendar == null || view == null) {
      throw new IllegalArgumentException("Calendar or View is non-existent in CreateAllDayEvent");
    }
    this.parameters = parameters;
    this.calendar = calendar;
    this.model = calendar.getActiveCalendar();
    this.view = view;
  }

  @Override
  public void execute() {
    try {
      model.createAllDayEvent(this.parameters);
      String displayMessage = "Using calendar " + calendar.getCalendarName() + ": "
          + "Successfully created an all day event called "
          + this.parameters.get("subject") + " on " + this.parameters.get("ondate") + ".";
      view.display(displayMessage);
    } catch (Exception e) {
      view.displayError("CreateAllDayEvent command: " + e.getMessage());
    }
  }
}
