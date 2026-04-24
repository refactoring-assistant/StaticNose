package calendar.controller.commands;

import calendar.model.CalendarSystem;
import java.time.LocalDate;

/**
 * Command to copy all events on a specific date to another calendar.
 */
public class CopyEventsOnDateCommand implements SystemCommand {
  private final LocalDate sourceDate;
  private final String targetCalendarName;
  private final LocalDate targetDate;

  /**
   * Copies all events on a specific date to another cal.
   *
   * @param sourceDate source date
   * @param targetCalendarName name of the target cal
   * @param targetDate date on which to copy the events
   */
  public CopyEventsOnDateCommand(LocalDate sourceDate, String targetCalendarName,
                                 LocalDate targetDate) {
    this.sourceDate = sourceDate;
    this.targetCalendarName = targetCalendarName;
    this.targetDate = targetDate;
  }

  @Override
  public String execute(CalendarSystem system) {
    try {
      system.copyEventsOnDate(sourceDate, targetCalendarName, targetDate);
      return "Events from " + sourceDate + " copied to calendar '"
          + targetCalendarName + "' on " + targetDate;
    } catch (IllegalArgumentException | IllegalStateException e) {
      return "Error: " + e.getMessage();
    } catch (Exception e) {
      return "No events were copied for " + sourceDate
          + " (possibly due to timezone conflicts or no matching events).";
    }
  }
}