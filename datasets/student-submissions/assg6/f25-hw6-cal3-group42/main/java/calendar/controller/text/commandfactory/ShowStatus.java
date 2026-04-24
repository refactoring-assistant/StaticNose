package calendar.controller.text.commandfactory;

import calendar.model.CalendarManagerInterface;
import calendar.model.CalendarModelInterface;
import calendar.view.CalendarViewInterface;
import java.time.ZonedDateTime;
import java.util.Map;

/**
 * Class to show availability at a given date.
 */
public class ShowStatus implements CommandInterface {
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
  public ShowStatus(Map<String, Object> parameters,
                    CalendarManagerInterface calendar,
                    CalendarViewInterface view) throws IllegalArgumentException {
    if (calendar == null || view == null) {
      throw new IllegalArgumentException(
          "Calendar or View is non-existent in ShowStatus");
    }
    this.parameters = parameters;
    this.calendar = calendar;
    this.model = calendar.getActiveCalendar();
    this.view = view;
  }

  @Override
  public void execute() {
    ZonedDateTime date = (ZonedDateTime) this.parameters.get("start");
    boolean isAvailable = model.checkAvailability(date);
    if (!isAvailable) {
      view.display("Using calendar " + calendar.getCalendarName() + ": "
          + "Busy on " + date + ".");
    } else {
      view.display("Using calendar " + calendar.getCalendarName() + ": "
          + "Available on " + date + ".");
    }
  }
}
