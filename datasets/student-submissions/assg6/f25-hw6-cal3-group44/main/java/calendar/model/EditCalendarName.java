package calendar.model;

import calendar.model.interfaces.AdvancedCalendar;
import calendar.model.interfaces.EditCalendar;

/**
 * Command used to edit the name of a calendar.
 * The Calendar is immutable, so we build as a new calendar with new name.
 */
public class EditCalendarName implements EditCalendar {

  @Override
  public AdvancedCalendar edit(AdvancedCalendar calendar, String newValue) {
    if (newValue.equals(calendar.getName())) {
      throw new IllegalArgumentException("Error: New name is the same as the current name");
    }
    return new AdvancedCalendarImpl.AdvancedCalendarBuilder(calendar)
        .setName(newValue)
        .build();
  }
}
