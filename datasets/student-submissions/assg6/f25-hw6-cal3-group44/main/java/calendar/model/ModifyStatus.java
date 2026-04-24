package calendar.model;

import calendar.model.datatypes.EventStatus;
import calendar.model.interfaces.CalendarEditable;
import calendar.model.interfaces.EventReadOnly;
import calendar.model.interfaces.ModifyEvent;

/**
 * Implements the ModifyEvent interface to update the status of an event.
 */
public class ModifyStatus implements ModifyEvent {
  @Override
  public EventReadOnly edit(CalendarEditable calendar, EventReadOnly event, String newValue) {
    return new Event.EventBuilder(event)
        .setEventStatus(EventStatus.getEventStatus(newValue))
        .build();
  }
}
