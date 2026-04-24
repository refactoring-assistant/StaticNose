package calendar.controller.handlers;

import calendar.controller.commanddata.EditCalendarCommandData;
import calendar.model.interfaces.CalendarContainer;
import java.util.Objects;

/**
 * Handler class that performs the logic for editing calendars.
 * Takes parsed command data and interacts with the model.
 */
public class EditCalendarHandler {

  private final CalendarContainer calendarManager;

  /**
   * Constructor for EditCalendarHandler.
   *
   * @param calendarManager the calendar container to interact with
   */
  public EditCalendarHandler(CalendarContainer calendarManager) {
    this.calendarManager = Objects.requireNonNull(calendarManager);
  }

  /**
   * Executes the edit calendar logic based on the parsed command data.
   *
   * @param data the parsed command data
   * @return the result string
   */
  public String handle(EditCalendarCommandData data) {
    calendarManager.updateCalendar(data.getCalendarName(), data.getPropertyName(),
        data.getNewPropertyValue());
    return "Calendar " + data.getPropertyName() + " edited successfully";
  }
}

