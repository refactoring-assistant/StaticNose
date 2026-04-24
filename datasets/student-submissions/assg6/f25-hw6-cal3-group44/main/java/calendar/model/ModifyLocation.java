package calendar.model;

import calendar.model.datatypes.Location;
import calendar.model.interfaces.CalendarEditable;
import calendar.model.interfaces.EventReadOnly;
import calendar.model.interfaces.ModifyEvent;

/**
 * Implements the ModifyEvent interface to update the location of an event.
 */
public class ModifyLocation implements ModifyEvent {
  @Override
  public EventReadOnly edit(CalendarEditable calendar, EventReadOnly event, String newValue) {
    return new Event.EventBuilder(event)
        .setLocation(Location.getLocation(newValue))
        .build();
  }
}
