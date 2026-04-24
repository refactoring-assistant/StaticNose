package calendar.controller.text.commandfactory;

import calendar.model.CalendarManagerInterface;
import calendar.model.CalendarModelInterface;
import calendar.model.event.CalendarEvent;
import calendar.view.CalendarViewInterface;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

/**
 * Class to print all events from some date.
 */
public class PrintEventsFrom implements CommandInterface {
  private final Map<String, Object> parameters;
  private final CalendarModelInterface model;
  private final CalendarViewInterface view;
  private final CalendarManagerInterface calendar;

  /**
   * Constructor to initialize the parameters, model from active calendar, and view when
   * this class is created.
   *
   * @param parameters map of all placeholder command names to its values
   * @param calendar   the active calendar
   * @param view       the calendar view
   * @throws IllegalArgumentException for missing parameters
   */
  public PrintEventsFrom(Map<String, Object> parameters,
                         CalendarManagerInterface calendar,
                         CalendarViewInterface view) throws IllegalArgumentException {
    if (calendar == null || view == null) {
      throw new IllegalArgumentException(
          "Calendar or View is non-existent in PrintEventsFrom");
    }
    this.parameters = parameters;
    this.calendar = calendar;
    this.model = calendar.getActiveCalendar();
    this.view = view;
  }

  @Override
  public void execute() {

    ZonedDateTime start = (ZonedDateTime) this.parameters.get("start");
    ZonedDateTime end = (ZonedDateTime) this.parameters.get("end");
    List<CalendarEvent> eventList = model.getEventsInRange(start, end);
    view.display("Using calendar " + calendar.getCalendarName() + ":");
    if (eventList.isEmpty()) {
      String displayMessage = "No events found between " + this.parameters.get("start")
          + " and " + this.parameters.get("end") + ".";
      view.displayError(displayMessage);
    } else {
      for (CalendarEvent event : eventList) {
        view.display(getMessage(event));
      }
    }
  }

  /**
   * Build the print message for each event to print.
   *
   * @param event a single event of whose details is to be printed
   * @return a string message of specified display format
   */
  static String getMessage(CalendarEvent event) {
    String eventSubject = event.getSubject();
    ZonedDateTime eventStart = event.getStartDateTime();
    String eventStartDate = eventStart.toLocalDate().toString();
    String eventStartTime = eventStart.toLocalTime().toString();
    ZonedDateTime eventEnd = event.getEndDateTime();
    String eventEndDate = eventEnd.toLocalDate().toString();
    String eventEndTime = eventEnd.toLocalTime().toString();

    return "- " + eventSubject + " starting on " + eventStartDate + " at "
        + eventStartTime + ", ending on " + eventEndDate + " at " + eventEndTime;
  }
}
