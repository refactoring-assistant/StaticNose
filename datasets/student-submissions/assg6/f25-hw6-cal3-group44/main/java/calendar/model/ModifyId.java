package calendar.model;

import calendar.model.interfaces.CalendarEditable;
import calendar.model.interfaces.EventReadOnly;
import calendar.model.interfaces.ModifyEvent;
import java.util.UUID;

/**
 * Implements the ModifyEvent interface to update the ID of an event.
 */
public class ModifyId implements ModifyEvent {
  @Override
  public EventReadOnly edit(CalendarEditable calendar, EventReadOnly event, String newValue) {
    return new Event.EventBuilder(event)
        .setEventId(UUID.fromString(newValue))
        .build();
  }
}
