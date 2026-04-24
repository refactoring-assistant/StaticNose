package calendar.controller;

import calendar.controller.commands.CopyMultipleEventsCalendarCommand;
import calendar.controller.commands.CopySingleEventCalendarCommand;
import calendar.controller.commands.CreateCalendarCommand;
import calendar.controller.commands.EditCalendarCommand;
import calendar.controller.commands.UseCalendarCommand;
import calendar.model.CalendarImpl;
import calendar.model.interfaces.CalendarContainer;
import calendar.view.CalendarView;
import java.util.Objects;

/**
 * Advanced Calendar Controller that extends the base controller
 * by adding multi-calendar management commands.
 */
public class AdvanceCalendarController extends CalendarControllerImpl {

  /**
   * Constructs an advanced calendar controller using an existing container.
   * Uses the parent constructor that accepts a container to ensure the controller
   * operates on the same instance provided by the caller.
   *
   * @param calendarContainer the container that manages multiple calendars
   * @param inputStream       input source
   * @param calendarView      output view
   */
  public AdvanceCalendarController(CalendarContainer calendarContainer,
                                   Readable inputStream,
                                   CalendarView calendarView) {
    super(Objects.requireNonNull(calendarContainer), new CalendarImpl(),
        Objects.requireNonNull(inputStream), Objects.requireNonNull(calendarView));

    this.commands.put("create calendar", (container)
        -> new CreateCalendarCommand(calendarContainer));
    this.commands.put("edit calendar", (container)
        -> new EditCalendarCommand(calendarContainer));
    this.commands.put("use calendar", (container)
        -> new UseCalendarCommand(calendarContainer));
    this.commands.put("copy event", (container)
        -> new CopySingleEventCalendarCommand(calendarContainer));
    this.commands.put("copy events", (container)
        -> new CopyMultipleEventsCalendarCommand(calendarContainer));
  }
}
