package calendar.command;

import calendar.exception.CalendarException;
import calendar.service.InEventService;
import calendar.view.InCalendarView;
import java.time.LocalDateTime;

/**
 * Command for editing events.
 * Supports editing single events, series from date, or entire series.
 */
public class EditEventCommand implements InCommand {

  private final InEventService eventService;
  private final InCalendarView view;
  private final String subject;
  private final LocalDateTime start;
  private final String property;
  private final String newValue;
  private final String editType;

  /**
   * Constructs an EditEventCommand.
   *
   * @param eventService the event service
   * @param view         the view
   * @param subject      event subject to find
   * @param start        start date/time to identify event
   * @param property     property to edit
   * @param newValue     new property value
   * @param editType     type of edit (single/from/entire)
   */
  public EditEventCommand(InEventService eventService, InCalendarView view,
                          String subject, LocalDateTime start,
                          String property, String newValue, String editType) {
    this.eventService = eventService;
    this.view = view;
    this.subject = subject;
    this.start = start;
    this.property = property;
    this.newValue = newValue;
    this.editType = editType;
  }

  @Override
  public void execute() throws CalendarException {
    switch (editType) {
      case "single":
        eventService.editSingleEvent(subject, start, property, newValue);
        view.displaySuccess("Event edited: " + subject);
        break;
      case "from":
        eventService.editSeriesFromDate(subject, start, property, newValue);
        view.displaySuccess("Event series edited from date: " + subject);
        break;
      case "entire":
        eventService.editEntireSeries(subject, start, property, newValue);
        view.displaySuccess("Entire event series edited: " + subject);
        break;
      default:
        throw new CalendarException("Unknown edit type: " + editType);
    }
  }

  @Override
  public String getDescription() {
    return "Edit event: " + subject + " (" + editType + " edit)";
  }
}
