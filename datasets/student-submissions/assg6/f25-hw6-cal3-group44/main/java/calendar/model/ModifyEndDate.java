package calendar.model;

import calendar.model.interfaces.CalendarEditable;
import calendar.model.interfaces.EventReadOnly;
import calendar.model.interfaces.ModifyEvent;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


/**
 * Implements the ModifyEvent interface to update the end date time of an event.
 * When the date time changes we also check if is an all day event and.
 * update the field accordingly.
 */
public class ModifyEndDate implements ModifyEvent {
  @Override
  public EventReadOnly edit(CalendarEditable calendar, EventReadOnly event, String newValue) {

    String regex = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}$";

    if (!newValue.matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}$")) {
      throw new IllegalArgumentException("Error: Invalid date-time format: " + newValue);
    }

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    LocalDateTime eightAm = LocalDateTime.parse(newValue, formatter).toLocalDate().atTime(8, 0);
    LocalDateTime
        fivePm = LocalDateTime.parse(newValue, formatter).toLocalDate().atTime(17, 0);

    LocalDateTime newEndDateTime = LocalDateTime.parse(newValue, formatter);
    LocalDateTime newStartDateTime = event.getStartDateTime();

    Duration duration = Duration.between(event.getStartDateTime(), event.getEndDateTime());
    newStartDateTime = newEndDateTime.minus(duration);

    EventReadOnly updatedEvent;
    boolean isAllDay = newStartDateTime.equals(eightAm)
        && newEndDateTime.equals(fivePm);
    Event.EventBuilder builder = new Event.EventBuilder(event)
        .setStartDateTime(newStartDateTime)
        .setEndDateTime(newEndDateTime);
    builder.setAllDay(isAllDay);
    updatedEvent = builder.build();
    return updatedEvent;
  }
}
