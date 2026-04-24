package calendar.controller.handlers;

import calendar.controller.commanddata.UseCalendarCommandData;
import calendar.model.interfaces.CalendarContainer;
import java.util.Objects;

/**
 * Handler class that performs the logic for using a calendar.
 * Takes parsed command data and interacts with the model.
 */
public class UseCalendarHandler {

  private final CalendarContainer calendarManager;

  /**
   * Constructor for UseCalendarHandler.
   *
   * @param calendarManager the calendar container to interact with
   */
  public UseCalendarHandler(CalendarContainer calendarManager) {
    this.calendarManager = Objects.requireNonNull(calendarManager);
  }

  /**
   * Executes the use calendar logic based on the parsed command data.
   *
   * @param data the parsed command data
   * @return the result string
   */
  public String handle(UseCalendarCommandData data) {
    calendarManager.setActiveCalendar(data.getCalendarName());
    return "Now you are using calendar " + data.getCalendarName();
  }
}

