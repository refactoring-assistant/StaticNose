package calendar.event;

/**
 * To wrap up a class with event and its calendar property.
 */
public class EventWrapper {
  String calendarName;
  String calendarTimezone;
  EventInterface event;

  /**
   * Constructor for class EventDecorator.
   *
   * @param calendarName the name of the calendar
   * @param calendarTimezone the timezone of the calendar
   * @param event the event
   */
  public EventWrapper(String calendarName, String calendarTimezone, EventInterface event) {
    this.calendarName = calendarName;
    this.calendarTimezone = calendarTimezone;
    this.event = event;
  }

  public EventInterface getEvent() {
    return this.event;
  }

  public String getCalendarName() {
    return this.calendarName;
  }

  @Override
  public String toString() {
    return "[calendarName = " + calendarName + ", calendarTimezone = " + calendarTimezone + "]";
  }

}
