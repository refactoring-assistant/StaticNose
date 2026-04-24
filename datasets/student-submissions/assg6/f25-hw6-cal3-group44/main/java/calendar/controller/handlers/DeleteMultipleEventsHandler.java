package calendar.controller.handlers;

import calendar.controller.commanddata.DeleteMultipleEventsCommandData;
import calendar.controller.filter.CalendarFilter;
import calendar.controller.filter.EventPredicates;
import calendar.model.datatypes.TypeOfEvent;
import calendar.model.interfaces.CalendarEditable;
import calendar.model.interfaces.EventReadOnly;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Handler class that performs the logic for deleting multiple events from a date onwards.
 * Takes parsed command data and interacts with the model.
 */
public class DeleteMultipleEventsHandler {

  private final CalendarEditable calendarModel;

  /**
   * Constructor for DeleteMultipleEventsHandler.
   *
   * @param calendarModel the calendar model to interact with
   */
  public DeleteMultipleEventsHandler(CalendarEditable calendarModel) {
    this.calendarModel = Objects.requireNonNull(calendarModel);
  }

  /**
   * Executes the delete multiple events logic based on the parsed command data.
   *
   * @param data the parsed command data
   * @return the result string
   */
  public String handle(DeleteMultipleEventsCommandData data) {
    CalendarFilter filter = new CalendarFilter(calendarModel);
    List<EventReadOnly> filteredEvents = filter.filter(EventPredicates.bySubject(data.getSubject())
        .and(EventPredicates.byStartDate(data.getStartDateTime())));

    if (filteredEvents.isEmpty()) {
      return "Error: No events found to delete";
    }

    StringBuilder output = new StringBuilder();
    int deletedCount = 0;

    for (EventReadOnly event : filteredEvents) {
      List<EventReadOnly> eventsToDelete = new ArrayList<>();
      if (event.getEventType() == TypeOfEvent.SERIES) {
        List<EventReadOnly> seriesEvents = filter.filter(EventPredicates.byEventId(event.getId()));
        for (EventReadOnly seriesEvent : seriesEvents) {
          if (seriesEvent.getStartDateTime().isEqual(data.getStartDateTime())
              || seriesEvent.getStartDateTime().isAfter(data.getStartDateTime())) {
            eventsToDelete.add(seriesEvent);
          }
        }
      } else {
        if (event.getStartDateTime().isEqual(data.getStartDateTime())) {
          eventsToDelete.add(event);
        }
      }

      for (EventReadOnly eventToDelete : eventsToDelete) {
        calendarModel.removeEvent(eventToDelete);
        deletedCount++;
      }
    }

    output.append("Deleted ").append(deletedCount).append(" event(s)")
        .append(System.lineSeparator());
    return output.toString();
  }
}

