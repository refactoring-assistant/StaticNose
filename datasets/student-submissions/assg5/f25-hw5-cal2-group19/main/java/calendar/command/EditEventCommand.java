package calendar.command;

import calendar.exception.CalendarException;
import calendar.service.InEventService;
import calendar.view.InCalendarView;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles editing calendar events in three different ways.
 * Supports editing single events, series from date, or entire series.
 * The three edit modes are single, from, entire
 */
public class EditEventCommand implements InCommand {

  private final InEventService eventService;
  private final InCalendarView view;
  private final String subject;
  private final LocalDateTime start;
  private final String property;
  private final String newValue;
  private final String editType;
  private static final String EDIT_TYPE_SINGLE = "single";
  private static final String EDIT_TYPE_FROM = "from";
  private static final String EDIT_TYPE_ENTIRE = "entire";
  private final Map<String, EditStrategy> editStrategies;

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
    this.editStrategies = initializeEditStrategies();

  }

  @Override
  public void execute() throws CalendarException {
    validateEditType(editType);
    EditStrategy strategy = editStrategies.get(editType);

    if (strategy == null) {
      throw new CalendarException("Unknown edit type: "
          + editType + ". Must be 'single', 'from', or 'entire'");
    }

    strategy.execute();
  }

  @Override
  public String getDescription() {
    return "Edit event: " + subject + " (" + editType + " edit)";
  }

  /**
   * Makes sure the edit type is valid before we do anything else.
   * Better to fail fast with a clear message than later with a confusing one.
   *
   * @throws CalendarException if the edit type is not valid
   */
  private void validateEditType(String type) throws CalendarException {
    if (type == null || (!type.equals(EDIT_TYPE_SINGLE)
        && !type.equals(EDIT_TYPE_FROM) && !type.equals(EDIT_TYPE_ENTIRE))) {
      throw new CalendarException(
          "Invalid edit type: '" + type + "'. Must be 'single', 'from', or 'entire'");
    }
  }

  /**
   * Sets up the map of edit strategies.
   * Each strategy knows how to perform its type of edit.
   */
  private Map<String, EditStrategy> initializeEditStrategies() {
    Map<String, EditStrategy> strategies = new HashMap<>();

    strategies.put(EDIT_TYPE_SINGLE, new SingleEditStrategy());
    strategies.put(EDIT_TYPE_FROM, new SeriesFromDateStrategy());
    strategies.put(EDIT_TYPE_ENTIRE, new EntireSeriesStrategy());

    return strategies;
  }

  /**
   * Strategy pattern interface - each edit type gets its own implementation.
   * Makes the code more maintainable and testable.
   */
  @FunctionalInterface
  private interface EditStrategy {
    void execute() throws CalendarException;
  }

  /**
   * Edits just one event in a series (or a standalone event).
   */
  private class SingleEditStrategy implements EditStrategy {
    @Override
    public void execute() throws CalendarException {
      eventService.editSingleEvent(subject, start, property, newValue);
      view.displaySuccess("Event edited: " + subject);
    }
  }

  /**
   * Edits this event and all future events in the series.
   */
  private class SeriesFromDateStrategy implements EditStrategy {
    @Override
    public void execute() throws CalendarException {
      eventService.editSeriesFromDate(subject, start, property, newValue);
      view.displaySuccess("Event series edited from date: " + subject);
    }
  }

  /**
   * Edits every event in the entire series.
   */
  private class EntireSeriesStrategy implements EditStrategy {
    @Override
    public void execute() throws CalendarException {
      eventService.editEntireSeries(subject, start, property, newValue);
      view.displaySuccess("Entire event series edited: " + subject);
    }
  }

}
