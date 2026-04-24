package calendar.controller.text.commandfactory;

import calendar.model.CalendarManagerInterface;
import calendar.view.CalendarViewInterface;
import java.time.ZonedDateTime;
import java.util.Map;

/**
 * Class to copy an event from active calendar to target calendar.
 */
public class CopyEvent implements CommandInterface {
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
  public CopyEvent(Map<String, Object> parameters, CalendarManagerInterface calendar,
                   CalendarViewInterface view) throws IllegalArgumentException {
    if (calendar == null || view == null) {
      throw new IllegalArgumentException("Calendar or View is non-existent in CopyEvent");
    }
    this.parameters = parameters;
    this.calendar = calendar;
    this.view = view;
  }

  @Override
  public void execute() {
    try {
      calendar.copyEvent(parameters);
      String displayMessage = "Copied event " + parameters.get("subject") + " from "
          + calendar.getCalendarName() + " on " + parameters.get("start") + " to "
          + parameters.get("target") + " on "
          + ZonedDateTime.parse(parameters.get("targetstart").toString()).toLocalDateTime()
          + " of its' timezone.";
      view.display(displayMessage);
    } catch (Exception e) {
      view.displayError("CopyEvent command: " + e.getMessage());
    }
  }
}
