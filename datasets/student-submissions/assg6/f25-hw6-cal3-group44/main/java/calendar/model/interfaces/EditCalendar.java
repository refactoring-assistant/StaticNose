package calendar.model.interfaces;

/**
 * Defines a function object that edits field values of an Advanced Calendar.
 */
public interface EditCalendar {

  /**
   * Performs edit operation on corresponding field value with the given new value.
   *
   * @param calendar advanced calendar on which edit to be made.
   * @param newValue the new value for the field value to edit
   * @return the advanced calendar after performing edit
   */
  AdvancedCalendar edit(AdvancedCalendar calendar, String newValue);
}
