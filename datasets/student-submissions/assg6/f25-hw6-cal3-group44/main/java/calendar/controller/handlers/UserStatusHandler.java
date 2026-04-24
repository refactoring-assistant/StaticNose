package calendar.controller.handlers;

import calendar.controller.commanddata.UserStatusCommandData;
import calendar.model.interfaces.CalendarEditable;
import java.util.Objects;

/**
 * Handler class that performs the logic for checking user status.
 * Takes parsed command data and interacts with the model.
 */
public class UserStatusHandler {

  private final CalendarEditable calendarModel;

  /**
   * Constructor for UserStatusHandler.
   *
   * @param calendarModel the calendar model to interact with
   */
  public UserStatusHandler(CalendarEditable calendarModel) {
    this.calendarModel = Objects.requireNonNull(calendarModel);
  }

  /**
   * Executes the user status logic based on the parsed command data.
   *
   * @param data the parsed command data
   * @return the result string
   */
  public String handle(UserStatusCommandData data) {
    if (calendarModel.isBusy(data.getDateTime())) {
      return "User is Busy";
    }
    return "User is Available";
  }
}

