package calendar.model;

import calendar.model.interfaces.AdvancedCalendar;
import calendar.model.interfaces.EditCalendar;
import calendar.model.interfaces.EventReadOnly;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.zone.ZoneRulesException;
import java.util.ArrayList;
import java.util.List;

/**
 * Command used to edit the time zone of the calendar.
 * When a timezone changes all the start and end times of the events.
 * will be updated with the time in new time zone.
 */
public class EditCalendarTimeZone implements EditCalendar {
  @Override
  public AdvancedCalendar edit(AdvancedCalendar calendar, String newValue) {
    ZoneId sourceZone = calendar.getZoneId();
    ZoneId targetZone;
    try {
      targetZone = ZoneId.of(newValue);
    } catch (ZoneRulesException e) {
      throw new IllegalArgumentException("Error: New time zone is Invalid");
    }


    if (sourceZone.equals(targetZone)) {
      throw new IllegalArgumentException("Error: The specified time zone is "
          + "same as the existing calendar timezone.");
    }


    List<EventReadOnly> events = new ArrayList<>();
    calendar.getCalendar().forEachEvent(events::add);

    for (EventReadOnly event : events) {
      LocalDateTime newStartDateTime = event
          .getStartDateTime()
          .atZone(sourceZone)
          .withZoneSameInstant(targetZone)
          .toLocalDateTime();

      LocalDateTime newEndDateTime = event
          .getEndDateTime()
          .atZone(sourceZone)
          .withZoneSameInstant(targetZone)
          .toLocalDateTime();

      EventReadOnly newEvent = new Event.EventBuilder(event)
          .setStartDateTime(newStartDateTime)
          .setEndDateTime(newEndDateTime)
          .build();

      calendar.getCalendar().removeEvent(event);
      calendar.getCalendar().addEvent(newEvent);
    }
    return new AdvancedCalendarImpl.AdvancedCalendarBuilder(calendar)
        .setZoneId(targetZone)
        .build();
  }
}
