package calendar.controller.handlers;

import calendar.controller.commanddata.DeleteEventCommandData;
import calendar.controller.filter.CalendarFilter;
import calendar.controller.filter.EventPredicates;
import calendar.model.interfaces.CalendarEditable;
import calendar.model.interfaces.EventReadOnly;
import java.util.List;
import java.util.Objects;

/**
 * Handler class that performs the logic for deleting a single event.
 * Takes parsed command data and interacts with the model.
 */
public class DeleteEventHandler {

  private final CalendarEditable calendarModel;

  /**
   * Constructor for DeleteEventHandler.
   *
   * @param calendarModel the calendar model to interact with
   */
  public DeleteEventHandler(CalendarEditable calendarModel) {
    this.calendarModel = Objects.requireNonNull(calendarModel);
  }

  /**
   * Executes the delete event logic based on the parsed command data.
   *
   * @param data the parsed command data
   * @return the result string
   */
  public String handle(DeleteEventCommandData data) {
    CalendarFilter filter = new CalendarFilter(calendarModel);
    List<EventReadOnly> filteredEvents = filter.filter(EventPredicates.bySubject(data.getSubject())
        .and(EventPredicates.byStartDate(data.getStartDateTime()))
        .and(EventPredicates.byEndDate(data.getEndDateTime())));

    if (filteredEvents.isEmpty()) {
      return "Error: No events found to delete";
    }

    StringBuilder output = new StringBuilder();
    output.append("Deleted event:").append(System.lineSeparator());
    for (EventReadOnly event : filteredEvents) {
      calendarModel.removeEvent(event);
      output.append(event.toString()).append(System.lineSeparator());
    }
    return output.toString();
  }
}

