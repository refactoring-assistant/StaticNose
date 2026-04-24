package calendar.controller.text.commandfactory;

import calendar.model.CalendarManagerInterface;
import calendar.model.CalendarModelInterface;
import calendar.model.event.CalendarEvent;
import calendar.view.CalendarViewInterface;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Class to print all events on some date.
 */
public class PrintEventsOn implements CommandInterface {
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
  public PrintEventsOn(Map<String, Object> parameters,
                       CalendarManagerInterface calendar,
                       CalendarViewInterface view) throws IllegalArgumentException {
    if (calendar == null || view == null) {
      throw new IllegalArgumentException(
          "Calendar or View is non-existent in PrintEventsOn");
    }
    this.parameters = parameters;
    this.calendar = calendar;
    this.model = calendar.getActiveCalendar();
    this.view = view;
  }

  @Override
  public void execute() {

    LocalDate date = (LocalDate) parameters.get("date");
    List<CalendarEvent> eventList = model.getEventsOn(date);
    view.display("Using calendar " + calendar.getCalendarName() + ":");
    if (eventList.isEmpty()) {
      String displayMessage = "No events found on " + this.parameters.get("date") + ".";
      view.displayError(displayMessage);
    } else {
      for (CalendarEvent event : eventList) {
        view.display(PrintEventsFrom.getMessage(event));
      }
    }
  }
}
