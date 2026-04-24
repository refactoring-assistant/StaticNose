package calendar.command;

import calendar.exception.CalendarException;
import calendar.service.InEventService;
import calendar.view.InCalendarView;
import java.time.LocalDateTime;

/**
 * Command for checking busy status at a specific date/time.
 */
public class ShowStatusCommand implements InCommand {

  private static final String STATUS_BUSY = "busy";
  private static final String STATUS_AVAILABLE = "available";
  private static final String MSG_STATUS_PREFIX = "Status at ";
  private static final String MSG_STATUS_SEPARATOR = ": ";
  private static final String MSG_DESC_PREFIX = "Check status at: ";

  private final InEventService eventService;
  private final InCalendarView view;
  private final LocalDateTime dateTime;

  /**
   * Constructs a ShowStatusCommand.
   *
   * @param eventService the event service
   * @param view         the view
   * @param dateTime     the date/time to check
   */
  public ShowStatusCommand(InEventService eventService, InCalendarView view,
                           LocalDateTime dateTime) {
    if (eventService == null || view == null) {
      throw new IllegalArgumentException("EventService and View cannot be null");
    }
    if (dateTime == null) {
      throw new IllegalArgumentException("DateTime cannot be null");
    }

    this.eventService = eventService;
    this.view = view;
    this.dateTime = dateTime;
  }

  @Override
  public void execute() throws CalendarException {
    boolean busy = eventService.checkBusyStatus(dateTime);
    String status = busy ? STATUS_BUSY : STATUS_AVAILABLE;
    view.displayMessage(MSG_STATUS_PREFIX + dateTime + MSG_STATUS_SEPARATOR + status);
  }

  @Override
  public String getDescription() {
    return MSG_DESC_PREFIX + dateTime;
  }
}