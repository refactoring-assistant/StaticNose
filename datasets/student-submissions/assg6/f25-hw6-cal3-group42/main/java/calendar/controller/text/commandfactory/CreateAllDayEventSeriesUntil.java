package calendar.controller.text.commandfactory;

import calendar.model.CalendarManagerInterface;
import calendar.model.CalendarModelInterface;
import calendar.view.CalendarViewInterface;
import java.util.Map;

/**
 * Class to create an all day event repeating for 'n' times.
 */
public class CreateAllDayEventSeriesUntil implements CommandInterface {
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
  public CreateAllDayEventSeriesUntil(Map<String, Object> parameters,
                                      CalendarManagerInterface calendar,
                                      CalendarViewInterface view) throws IllegalArgumentException {
    if (calendar == null || view == null) {
      throw new IllegalArgumentException(
          "Calendar or View is non-existent in CreateAllDayEventSeriesUntil");
    }
    this.parameters = parameters;
    this.calendar = calendar;
    this.model = calendar.getActiveCalendar();
    this.view = view;
  }

  @Override
  public void execute() {
    try {
      model.createAllDayEventSeries(this.parameters);
      String displayMessage = "Using calendar " + calendar.getCalendarName() + ": "
          + "Successfully created an all day event series called "
          + this.parameters.get("subject") + " starting from "
          + this.parameters.get("ondate") + " on every "
          + this.parameters.get("weekdays").toString().toUpperCase()
          + " days recurring until " + this.parameters.get("untildate") + ".";
      view.display(displayMessage);
    } catch (Exception e) {
      view.displayError("CreateAllDayEventSeriesUntil command: " + e.getMessage());
    }
  }
}
