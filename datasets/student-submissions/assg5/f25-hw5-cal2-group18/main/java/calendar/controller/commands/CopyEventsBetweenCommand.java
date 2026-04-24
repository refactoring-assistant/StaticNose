package calendar.controller.commands;

import calendar.model.CalendarSystem;
import java.time.LocalDate;

/**
 * Command to copy all events in a date range to another calendar.
 */
public class CopyEventsBetweenCommand implements SystemCommand {
  private final LocalDate startDate;
  private final LocalDate endDate;
  private final String targetCalendarName;
  private final LocalDate targetStartDate;

  /**
   * Copies all events in the date range.
   *
   * @param startDate          start date
   * @param endDate            end date
   * @param targetCalendarName name of target cal
   * @param targetStartDate    start date of target events
   */
  public CopyEventsBetweenCommand(LocalDate startDate, LocalDate endDate, String targetCalendarName,
                                  LocalDate targetStartDate) {
    this.startDate = startDate;
    this.endDate = endDate;
    this.targetCalendarName = targetCalendarName;
    this.targetStartDate = targetStartDate;
  }

  @Override
  public String execute(CalendarSystem system) {
    try {
      system.copyEventsBetween(startDate, endDate, targetCalendarName, targetStartDate);
      return "Events between " + startDate + " and " + endDate
          + " copied to calendar '" + targetCalendarName + "' starting " + targetStartDate;
    } catch (IllegalArgumentException e) {
      return "Error: " + e.getMessage();
    } catch (IllegalStateException e) {
      return e.getMessage();
    } catch (Exception e) {
      return "Error: " + e.getMessage();
    }
  }
}