package calendar.command;

import calendar.model.exceptions.ConflictException;
import calendar.service.CalendarService;
import calendar.utils.DateTimeUtil;
import calendar.view.textbased.CalendarView;
import java.time.LocalDateTime;

/**
 * Command to copy a single event.
 */
public class CopyEventSingleCommand implements CalendarCommand {

  private final String eventName;
  private final String onTimeStr;
  private final String targetCalName;
  private final String toTimeStr;

  /**
   * Initializes the CopyEventSingleCommand object.
   *
   * @param eventName The event name
   * @param onTimeStr The event time
   * @param targetCalName The target calendar name
   * @param toTimeStr The to time
   */
  public CopyEventSingleCommand(String eventName, String onTimeStr,
                                String targetCalName, String toTimeStr) {
    this.eventName = eventName;
    this.onTimeStr = onTimeStr;
    this.targetCalName = targetCalName;
    this.toTimeStr = toTimeStr;
  }

  @Override
  public void execute(CalendarService service, CalendarView view) {
    try {
      LocalDateTime eventStart = DateTimeUtil.parseDateTime(onTimeStr);
      LocalDateTime newTargetStart = DateTimeUtil.parseDateTime(toTimeStr);

      service.copyEvent(eventName, eventStart, targetCalName, newTargetStart);
      view.showMessage("Event '" + eventName
          + "' copied successfully to calendar '" + targetCalName + "'.");

    } catch (ConflictException | IllegalArgumentException e) {
      view.showError("Failed to copy event: " + e.getMessage());
    }
  }
}