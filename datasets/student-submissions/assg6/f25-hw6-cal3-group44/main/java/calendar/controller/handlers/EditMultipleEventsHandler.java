package calendar.controller.handlers;

import calendar.controller.commanddata.EditMultipleEventsCommandData;
import calendar.controller.commands.EditSeriesCommand;
import calendar.controller.filter.CalendarFilter;
import calendar.controller.filter.EventPredicates;
import calendar.model.datatypes.TypeOfEvent;
import calendar.model.interfaces.CalendarEditable;
import calendar.model.interfaces.EventReadOnly;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Handler class that performs the logic for editing multiple events.
 * Takes parsed command data and interacts with the model.
 */
public class EditMultipleEventsHandler {

  private final CalendarEditable calendarModel;

  /**
   * Constructor for EditMultipleEventsHandler.
   *
   * @param calendarModel the calendar model to interact with
   */
  public EditMultipleEventsHandler(CalendarEditable calendarModel) {
    this.calendarModel = Objects.requireNonNull(calendarModel);
  }

  /**
   * Executes the edit multiple events logic based on the parsed command data.
   *
   * @param data the parsed command data
   * @return the result string
   */
  public String handle(EditMultipleEventsCommandData data) {
    CalendarFilter filter = new CalendarFilter(calendarModel);
    List<EventReadOnly> filteredEvents = filter.filter(EventPredicates.bySubject(data.getSubject())
        .and(EventPredicates.byStartDate(data.getStartDateTime())));
    StringBuilder output = new StringBuilder();

    for (EventReadOnly event : filteredEvents) {
      List<EventReadOnly> currEvents = new ArrayList<>();
      currEvents.add(event);
      if (event.getEventType() == TypeOfEvent.SERIES) {
        currEvents = filter.filter(EventPredicates.byEventId(event.getId()));
        currEvents.removeIf(ev -> !(ev.getStartDateTime().isEqual(data.getStartDateTime())
            || ev.getStartDateTime().isAfter(data.getStartDateTime())));
      }
      calendarModel.editEvent(currEvents, data.getPropertyToUpdate(), data.getNewPropertyValue());
      List<EventReadOnly> newFilteredEvents;
      if (data.getPropertyToUpdate().equalsIgnoreCase("start")) {
        newFilteredEvents = filter.filter(EventPredicates.bySubject(data.getSubject())
            .and(EventPredicates.byStartDate(LocalDateTime.parse(data.getNewPropertyValue())))
            .and(EventPredicates.byEndDate(LocalDateTime
                .parse(event.getEndDateTime().toString()))));
        currEvents = filter.filter(EventPredicates.byEventId(newFilteredEvents.get(0).getId()));
      } else {
        currEvents = filter.filter(EventPredicates.byEventId(event.getId()));
        currEvents.removeIf(ev -> !(ev.getStartDateTime().isEqual(data.getStartDateTime())
            || ev.getStartDateTime().isAfter(data.getStartDateTime())));
      }

      EditSeriesCommand.getReturnValue(output, currEvents);
    }

    return output.toString();
  }
}

