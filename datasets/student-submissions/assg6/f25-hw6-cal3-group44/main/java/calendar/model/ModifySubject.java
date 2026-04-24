package calendar.model;


import calendar.model.interfaces.CalendarEditable;
import calendar.model.interfaces.EventReadOnly;
import calendar.model.interfaces.ModifyEvent;

/**
 * Implements the ModifyEvent interface to update the subject of an event.
 */
public class ModifySubject implements ModifyEvent {
  @Override
  public EventReadOnly edit(CalendarEditable calendar, EventReadOnly event, String newValue) {
    return new Event.EventBuilder(event)
        .setSubject(newValue)
        .build();
  }
}
