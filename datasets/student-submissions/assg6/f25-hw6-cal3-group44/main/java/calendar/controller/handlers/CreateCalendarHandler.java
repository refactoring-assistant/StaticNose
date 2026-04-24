package calendar.controller.handlers;

import calendar.controller.commanddata.CreateCalendarCommandData;
import calendar.model.AdvancedCalendarImpl;
import calendar.model.interfaces.AdvancedCalendar;
import calendar.model.interfaces.CalendarContainer;
import java.util.Objects;

/**
 * Handler class that performs the logic for creating calendars.
 * Takes parsed command data and interacts with the model.
 */
public class CreateCalendarHandler {

  private final CalendarContainer calendarManager;

  /**
   * Constructor for CreateCalendarHandler.
   *
   * @param calendarManager the calendar container to interact with
   */
  public CreateCalendarHandler(CalendarContainer calendarManager) {
    this.calendarManager = Objects.requireNonNull(calendarManager);
  }

  /**
   * Executes the create calendar logic based on the parsed command data.
   *
   * @param data the parsed command data
   * @return the result string
   */
  public String handle(CreateCalendarCommandData data) {
    AdvancedCalendar newCal = new AdvancedCalendarImpl
        .AdvancedCalendarBuilder(data.getName(), data.getTimezone())
        .build();
    calendarManager.addCalendar(data.getName(), newCal);
    return "Calendar with name " + data.getName() + " has been successfully created";
  }
}

