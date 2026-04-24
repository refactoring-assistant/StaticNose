package calendar.controller.handlers;

import calendar.controller.commanddata.EditEventCommandData;
import calendar.controller.filter.CalendarFilter;
import calendar.controller.filter.EventPredicates;
import calendar.model.interfaces.CalendarEditable;
import calendar.model.interfaces.EventReadOnly;
import java.util.List;
import java.util.Objects;

/**
 * Handler class that performs the logic for editing events.
 * Takes parsed command data and interacts with the model.
 */
public class EditEventHandler {

  private final CalendarEditable calendarModel;

  /**
   * Constructor for EditEventHandler.
   *
   * @param calendarModel the calendar model to interact with
   */
  public EditEventHandler(CalendarEditable calendarModel) {
    this.calendarModel = Objects.requireNonNull(calendarModel);
  }

  /**
   * Executes the edit event logic based on the parsed command data.
   *
   * @param data the parsed command data
   * @return the result string
   */
  public String handle(EditEventCommandData data) {
    CalendarFilter filter = new CalendarFilter(calendarModel);
    List<EventReadOnly> filteredEvents = filter.filter(EventPredicates.bySubject(data.getSubject())
        .and(EventPredicates.byStartDate(data.getStartDateTime()))
        .and(EventPredicates.byEndDate(data.getEndDateTime())));

    List<EventReadOnly> editedEvents = calendarModel
        .editEvent(filteredEvents, data.getPropertyToUpdate(), data.getNewPropertyValue());

    if (editedEvents.isEmpty()) {
      return "Error: No events updated";
    }
    StringBuilder output = new StringBuilder();
    output.append("Event updated:").append(System.lineSeparator());
    for (EventReadOnly editedEvent : editedEvents) {
      output.append(editedEvent.toString()).append(System.lineSeparator());
    }
    return output.toString();
  }
}

